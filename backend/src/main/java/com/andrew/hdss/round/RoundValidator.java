package com.andrew.hdss.round;

import com.andrew.hdss.dtos.MappableFieldDto;
import com.andrew.hdss.models.enums.MappedEntity;
import com.andrew.hdss.services.MappedFieldService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoundValidator {

    private static final int MAX_SAMPLES = 100;
    private static final int ANSWER_BATCH = 5_000;
    private static final Pattern EXPRESSION_REFERENCE = Pattern.compile("\\$\\{([^}]+)}");

    private static final String RESPONSES_UNDER = """
            SELECT COUNT(*) FROM form_responses fr
            JOIN visits v ON v.id = fr.visit_id
            WHERE fr.is_synced = 0 AND v.status %s 'COMPLETED'""";

    private static final String ANSWERS_UNDER = """
            SELECT COUNT(*) FROM answers a
            JOIN form_responses fr ON fr.id = a.form_response_id
            JOIN visits v ON v.id = fr.visit_id
            WHERE a.is_synced = 0 AND v.status %s 'COMPLETED'""";

    private final NamedParameterJdbcTemplate jdbc;
    private final MappedFieldService mappedFieldService;
    private final TransactionTemplate snapshot;

    public RoundValidator(NamedParameterJdbcTemplate jdbc,
                          PlatformTransactionManager transactionManager,
                          MappedFieldService mappedFieldService) {
        this.jdbc = jdbc;
        this.mappedFieldService = mappedFieldService;
        this.snapshot = new TransactionTemplate(transactionManager);
        this.snapshot.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.snapshot.setReadOnly(true);
    }

    private static void checkReferences(Collector c, String where, String field, String expression, Set<String> names) {
        if (expression == null || expression.isBlank()) return;
        Matcher m = EXPRESSION_REFERENCE.matcher(expression);
        while (m.find()) {
            if (!names.contains(m.group(1))) {
                c.add(where, field + " references ${" + m.group(1) + "}, which is not a question in this form");
            }
        }
    }

    private static String problemWith(AnswerRow r, Map<String, Set<String>> choices, Set<String> individuals) {
        String v = r.value().trim();
        return switch (r.type()) {
            case "INTEGER" -> v.matches("-?\\d+") ? null : quote(v) + " is not a whole number";
            case "DECIMAL" -> v.matches("-?\\d+(\\.\\d+)?") ? null : quote(v) + " is not a number";
            case "DATE" -> isDate(v) ? null : quote(v) + " is not a yyyy-MM-dd date";
            case "DATETIME" -> isDateTime(v) ? null : quote(v) + " is not an ISO date-time";
            case "GEOPOINT" -> geopointProblem(v);
            case "SELECT_ONE" -> choices.getOrDefault(r.choiceList(), Set.of()).contains(v)
                    ? null : quote(v) + " is not a choice in list " + quote(r.choiceList());
            case "SELECT_MULTIPLE" -> {
                Set<String> allowed = choices.getOrDefault(r.choiceList(), Set.of());
                yield Arrays.stream(v.split("\\s+")).filter(t -> !allowed.contains(t)).findFirst()
                        .map(t -> quote(t) + " is not a choice in list " + quote(r.choiceList()))
                        .orElse(null);
            }
            case "SELECT_HOUSEHOLD_MEMBER" -> individuals.contains(v)
                    ? null : "no individual with client id " + quote(v);
            default -> null;
        };
    }

    // ---- SQL checks ----

    private static String geopointProblem(String v) {
        String[] parts = v.split("\\s+");
        if (parts.length < 2) return quote(v) + " is not a 'lat lon' pair";
        try {
            double lat = Double.parseDouble(parts[0]);
            double lon = Double.parseDouble(parts[1]);
            return (lat < -90 || lat > 90 || lon < -180 || lon > 180) ? "coordinates out of range: " + v : null;
        } catch (NumberFormatException e) {
            return quote(v) + " is not a 'lat lon' pair";
        }
    }

    private static boolean isDate(String v) {
        try {
            LocalDate.parse(v);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isDateTime(String v) {
        try {
            LocalDateTime.parse(v);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // ---- F3, F4: published form definitions ----

    private static String quote(String s) {
        return "'" + s + "'";
    }

    // Dry run: one consistent snapshot of live, nothing written anywhere.
    public ValidationReport validate() {
        return snapshot.execute(status -> validateInSnapshot());
    }

    // Must be called inside a REPEATABLE READ read-only transaction. The
    // move will call this and then copy in the same transaction, so what
    // is copied is exactly what was validated.
    public ValidationReport validateInSnapshot() {
        LocalDate today = LocalDate.now();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", java.sql.Date.valueOf(today))
                .addValue("gap", RoundSqlChecks.MIN_PARENT_AGE_GAP_YEARS)
                .addValue("maxAge", RoundSqlChecks.MAX_AGE_YEARS);

        DeltaSummary delta = deltaSummary();
        List<CheckFailure> failures = new ArrayList<>();

        List<SqlCheck> sqlChecks = RoundSqlChecks.all();
        for (SqlCheck check : sqlChecks) {
            runSql(check, params).ifPresent(failures::add);
        }

        failures.addAll(formDefinitionChecks());
        failures.addAll(answerValueChecks());

        if (delta.movable() == 0) {
            failures.add(new CheckFailure("R2", "The delta is not empty", 1, false,
                    List.of(new Violation("R2", "-", "Nothing new or changed to move"))));
        }

        // F3, F4, V5 and R2 are the four checks not in the SQL list.
        return new ValidationReport(failures.isEmpty(), today, sqlChecks.size() + 4, delta, failures);
    }

    private Optional<CheckFailure> runSql(SqlCheck check, SqlParameterSource params) {
        String limited = "SELECT * FROM (" + check.sql() + ") v LIMIT " + (MAX_SAMPLES + 1);
        List<Violation> rows = jdbc.query(limited, params,
                (rs, i) -> new Violation(check.id(), rs.getString("ref"), rs.getString("detail")));
        if (rows.isEmpty()) return Optional.empty();

        boolean more = rows.size() > MAX_SAMPLES;
        List<Violation> samples = more ? List.copyOf(rows.subList(0, MAX_SAMPLES)) : rows;
        return Optional.of(new CheckFailure(check.id(), check.description(), samples.size(), more, samples));
    }

    // ---- V5: answer values fit their question type ----

    private DeltaSummary deltaSummary() {
        return new DeltaSummary(
                count("SELECT COUNT(*) FROM individuals WHERE is_synced = 0"),
                count("SELECT COUNT(*) FROM households WHERE is_synced = 0"),
                count("SELECT COUNT(*) FROM memberships WHERE is_synced = 0"),
                count("SELECT COUNT(*) FROM visits WHERE is_synced = 0 AND status = 'COMPLETED'"),
                count(RESPONSES_UNDER.formatted("=")),
                count(ANSWERS_UNDER.formatted("=")),
                count("SELECT COUNT(*) FROM visits WHERE is_synced = 0 AND status <> 'COMPLETED'"),
                count(RESPONSES_UNDER.formatted("<>")),
                count(ANSWERS_UNDER.formatted("<>")),
                count("SELECT COUNT(*) FROM forms WHERE status = 'PUBLISHED'"),
                count("""
                        SELECT COUNT(*) FROM questions q
                        JOIN forms f ON f.id = q.form_id WHERE f.status = 'PUBLISHED'"""));
    }

    private long count(String sql) {
        Long n = jdbc.getJdbcOperations().queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }

    private List<CheckFailure> formDefinitionChecks() {
        List<QuestionRow> rows = jdbc.getJdbcOperations().query("""
                        SELECT q.form_id, f.name AS form_name, q.name, q.type, q.relevant, q.constraint_value,
                               q.calculation, q.choice_list_name, q.mapped_entity, q.mapped_field
                        FROM questions q
                        JOIN forms f ON f.id = q.form_id
                        WHERE f.status = 'PUBLISHED'
                        """,
                (rs, i) -> new QuestionRow(rs.getLong("form_id"), rs.getString("form_name"),
                        rs.getString("name"), rs.getString("type"), rs.getString("relevant"),
                        rs.getString("constraint_value"), rs.getString("calculation"),
                        rs.getString("choice_list_name"), rs.getString("mapped_entity"),
                        rs.getString("mapped_field")));

        Map<Long, Set<String>> namesByForm = new HashMap<>();
        for (QuestionRow r : rows) {
            namesByForm.computeIfAbsent(r.formId(), k -> new HashSet<>()).add(r.name());
        }
        Set<String> choiceLists = new HashSet<>(
                jdbc.getJdbcOperations().queryForList("SELECT DISTINCT list_name FROM choices", String.class));

        Collector f3 = new Collector("F3", "Expressions only reference questions that exist in the same form");
        Collector f4 = new Collector("F4", "Choice lists exist and mapped fields are valid for their entity");

        for (QuestionRow r : rows) {
            String where = r.formName() + "." + r.name();

            checkReferences(f3, where, "relevant", r.relevant(), namesByForm.get(r.formId()));
            checkReferences(f3, where, "constraint", r.constraint(), namesByForm.get(r.formId()));
            checkReferences(f3, where, "calculation", r.calculation(), namesByForm.get(r.formId()));

            if ("SELECT_ONE".equals(r.type()) || "SELECT_MULTIPLE".equals(r.type())) {
                if (r.choiceList() == null || r.choiceList().isBlank()) {
                    f4.add(where, "select question has no choice list");
                } else if (!choiceLists.contains(r.choiceList())) {
                    f4.add(where, "choice list '" + r.choiceList() + "' has no choices");
                }
            }

            if (r.mappedEntity() != null && !"NONE".equals(r.mappedEntity())) {
                checkMapping(f4, where, r);
            }
        }
        return Stream.of(f3, f4).map(Collector::toFailure).filter(Objects::nonNull).toList();
    }

    private void checkMapping(Collector c, String where, QuestionRow r) {
        MappedEntity entity;
        try {
            entity = MappedEntity.valueOf(r.mappedEntity());
        } catch (IllegalArgumentException e) {
            c.add(where, "unknown mapped entity " + r.mappedEntity());
            return;
        }

        List<String> fields = r.mappedField() == null ? List.of()
                : Arrays.stream(r.mappedField().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        int expected = "GEOPOINT".equals(r.type()) ? 2 : 1;
        if (fields.size() != expected) {
            c.add(where, "expected " + expected + " mapped field(s) but found '" + r.mappedField() + "'");
            return;
        }

        Set<String> valid = mappedFieldService.getMappableFields(entity).stream()
                .map(MappableFieldDto::value).collect(Collectors.toSet());
        for (String field : fields) {
            if (!valid.contains(field)) {
                c.add(where, "'" + field + "' is not a mappable field of " + entity);
            }
        }
    }

    private List<CheckFailure> answerValueChecks() {
        Collector v5 = new Collector("V5", "Answer values fit their question type");

        Map<String, Set<String>> choices = new HashMap<>();
        jdbc.getJdbcOperations().query("SELECT list_name, name FROM choices", rs -> {
            choices.computeIfAbsent(rs.getString(1), k -> new HashSet<>()).add(rs.getString(2));
        });

        long lastId = 0;
        while (true) {
            // Keyset batches rather than one big result set: bounded memory,
            // and still the same snapshot.
            List<AnswerRow> batch = jdbc.query("""
                            SELECT a.id, a.client_id, a.value, q.type, q.choice_list_name
                            FROM answers a
                            JOIN form_responses fr ON fr.id = a.form_response_id
                            JOIN visits v ON v.id = fr.visit_id
                            JOIN questions q ON q.id = a.question_id
                            WHERE a.is_synced = 0 AND v.status = 'COMPLETED'
                              AND a.id > :lastId AND a.value IS NOT NULL AND a.value <> ''
                            ORDER BY a.id
                            LIMIT :batch
                            """,
                    new MapSqlParameterSource().addValue("lastId", lastId).addValue("batch", ANSWER_BATCH),
                    (rs, i) -> new AnswerRow(rs.getLong("id"), rs.getString("client_id"),
                            rs.getString("value"), rs.getString("type"), rs.getString("choice_list_name")));
            if (batch.isEmpty()) break;

            Set<String> memberRefs = batch.stream()
                    .filter(r -> "SELECT_HOUSEHOLD_MEMBER".equals(r.type()))
                    .map(r -> r.value().trim())
                    .collect(Collectors.toSet());
            Set<String> knownIndividuals = memberRefs.isEmpty() ? Set.of()
                    : new HashSet<>(jdbc.query(
                    "SELECT client_id FROM individuals WHERE client_id IN (:ids)",
                    new MapSqlParameterSource("ids", memberRefs),
                    (rs, i) -> rs.getString(1)));

            for (AnswerRow r : batch) {
                String problem = problemWith(r, choices, knownIndividuals);
                if (problem != null) v5.add(r.clientId(), problem);
            }
            lastId = batch.get(batch.size() - 1).id();
        }

        CheckFailure failure = v5.toFailure();
        return failure == null ? List.of() : List.of(failure);
    }

    private record QuestionRow(long formId, String formName, String name, String type,
                               String relevant, String constraint, String calculation,
                               String choiceList, String mappedEntity, String mappedField) {
    }

    private record AnswerRow(long id, String clientId, String value, String type, String choiceList) {
    }

    // Counts every violation but keeps only the first MAX_SAMPLES.
    private static final class Collector {
        private final String id;
        private final String description;
        private final List<Violation> samples = new ArrayList<>();
        private int count;

        Collector(String id, String description) {
            this.id = id;
            this.description = description;
        }

        void add(String ref, String detail) {
            count++;
            if (samples.size() < MAX_SAMPLES) samples.add(new Violation(id, ref, detail));
        }

        CheckFailure toFailure() {
            return count == 0 ? null : new CheckFailure(id, description, count, false, samples);
        }
    }
}
