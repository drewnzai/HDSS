package com.andrew.hdss.round;

import java.util.List;

final class RoundSqlChecks {

    static final int MIN_PARENT_AGE_GAP_YEARS = 12;
    static final int MAX_AGE_YEARS = 120;
    // A household counts as touched when it, or any of its memberships, is unsynced.
    private static final String HOUSEHOLD_TOUCHED = """
            (h.is_synced = 0 OR EXISTS (
                SELECT 1 FROM memberships tm WHERE tm.household_id = h.id AND tm.is_synced = 0))""";
    // Same idea per person, with the membership row aliased m.
    private static final String MEMBERSHIP_TOUCHED = """
            (m.is_synced = 0 OR EXISTS (
                SELECT 1 FROM memberships tm WHERE tm.individual_id = m.individual_id AND tm.is_synced = 0))""";

    private RoundSqlChecks() {
    }

    private static String missing(String table, String fkColumn, String target) {
        return """
                SELECT t.client_id AS ref,
                       CONCAT('%1$s ', t.%1$s, ' does not exist in %2$s') AS detail
                FROM %3$s t
                LEFT JOIN %2$s x ON x.id = t.%1$s
                WHERE t.is_synced = 0 AND t.%1$s IS NOT NULL AND x.id IS NULL
                """.formatted(fkColumn, target, table);
    }

    private static SqlCheck check(String id, String description, String... selects) {
        return new SqlCheck(id, description, String.join("\nUNION ALL\n", selects));
    }

    static List<SqlCheck> all() {
        return List.of(

                // ---- Links resolve ----
                check("L1", "Individual.mother, if set, is in the system",
                        missing("individuals", "mother_id", "individuals")),
                check("L2", "Individual.father, if set, is in the system",
                        missing("individuals", "father_id", "individuals")),
                check("L3", "Household.location exists and is a leaf",
                        missing("households", "location_id", "locations"),
                        """
                                SELECT h.client_id AS ref, CONCAT('location ', h.location_id, ' has child locations') AS detail
                                FROM households h
                                WHERE h.is_synced = 0
                                  AND EXISTS (SELECT 1 FROM locations c WHERE c.parent_id = h.location_id)
                                """),
                check("L4", "Household.head, if set, is in the system",
                        missing("households", "head_id", "individuals")),
                check("L5", "Membership individual and household exist",
                        missing("memberships", "individual_id", "individuals"),
                        missing("memberships", "household_id", "households")),
                check("L6", "A visit points to a household, an individual, or both, and they exist",
                        """
                                SELECT v.client_id AS ref, 'visit references neither a household nor an individual' AS detail
                                FROM visits v
                                WHERE v.is_synced = 0 AND v.status = 'COMPLETED'
                                  AND v.household_id IS NULL AND v.individual_id IS NULL
                                """,
                        missing("visits", "household_id", "households"),
                        missing("visits", "individual_id", "individuals")),
                check("L7", "FormResponse visit and form exist",
                        missing("form_responses", "visit_id", "visits"),
                        missing("form_responses", "form_id", "forms")),
                check("L8", "Answer response and question exist",
                        missing("answers", "form_response_id", "form_responses"),
                        missing("answers", "question_id", "questions")),
                check("L9", "createdBy resolves to a user",
                        missing("individuals", "created_by", "users"),
                        missing("households", "created_by", "users"),
                        missing("memberships", "created_by", "users"),
                        missing("visits", "created_by", "users"),
                        missing("form_responses", "created_by", "users"),
                        missing("answers", "created_by", "users")),
                check("L10", "Location tree is consistent (parent type one level up, ancestor path matches)",
                        """
                                SELECT COALESCE(c.code, CAST(c.id AS CHAR)) AS ref,
                                       CONCAT(c.type, ' "', c.name, '" sits under ', p.type, ' "', p.name, '" (path ',
                                              c.ancestor_path, ', expected ', p.ancestor_path, p.id, '/)') AS detail
                                FROM locations c
                                JOIN locations p ON p.id = c.parent_id
                                WHERE c.type = 'COUNTRY'
                                   OR p.type <> CASE c.type
                                        WHEN 'COUNTY' THEN 'COUNTRY'
                                        WHEN 'SUB_COUNTY' THEN 'COUNTY'
                                        WHEN 'LOCATION' THEN 'SUB_COUNTY'
                                        WHEN 'SUB_LOCATION' THEN 'LOCATION'
                                     END
                                   OR c.ancestor_path <> CONCAT(p.ancestor_path, p.id, '/')
                                """),

                // ---- Family relations ----
                check("P1", "A mother is FEMALE and a father is MALE",
                        """
                                SELECT i.client_id AS ref, 'mother is not FEMALE' AS detail
                                FROM individuals i
                                JOIN individuals p ON p.id = i.mother_id
                                WHERE (i.is_synced = 0 OR p.is_synced = 0) AND p.sex <> 'FEMALE'
                                """,
                        """
                                SELECT i.client_id AS ref, 'father is not MALE' AS detail
                                FROM individuals i
                                JOIN individuals p ON p.id = i.father_id
                                WHERE (i.is_synced = 0 OR p.is_synced = 0) AND p.sex <> 'MALE'
                                """),
                check("P2", "Nobody is their own parent, and mother and father differ",
                        """
                                SELECT i.client_id AS ref, 'is recorded as their own parent' AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0 AND (i.mother_id = i.id OR i.father_id = i.id)
                                """,
                        """
                                SELECT i.client_id AS ref, 'mother and father are the same individual' AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0 AND i.mother_id IS NOT NULL AND i.mother_id = i.father_id
                                """),
                check("P3", "A parent is at least " + MIN_PARENT_AGE_GAP_YEARS + " years older than the child",
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('mother born ', p.date_of_birth, ', child born ', i.date_of_birth) AS detail
                                FROM individuals i
                                JOIN individuals p ON p.id = i.mother_id
                                WHERE (i.is_synced = 0 OR p.is_synced = 0)
                                  AND DATE_ADD(p.date_of_birth, INTERVAL :gap YEAR) > i.date_of_birth
                                """,
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('father born ', p.date_of_birth, ', child born ', i.date_of_birth) AS detail
                                FROM individuals i
                                JOIN individuals p ON p.id = i.father_id
                                WHERE (i.is_synced = 0 OR p.is_synced = 0)
                                  AND DATE_ADD(p.date_of_birth, INTERVAL :gap YEAR) > i.date_of_birth
                                """),

                // ---- Households and memberships ----
                check("M1", "ACTIVE household has a head, NOT_VIABLE has none",
                        """
                                SELECT h.client_id AS ref,
                                       CONCAT('status ', h.status, IF(h.head_id IS NULL, ' but no head', ' but has a head')) AS detail
                                FROM households h
                                WHERE h.is_synced = 0
                                  AND ((h.status = 'ACTIVE' AND h.head_id IS NULL)
                                    OR (h.status = 'NOT_VIABLE' AND h.head_id IS NOT NULL))
                                """),
                check("M2", "A household's head has an open HEAD membership in that household",
                        """
                                SELECT h.client_id AS ref, 'head has no open HEAD membership in this household' AS detail
                                FROM households h
                                WHERE %s
                                  AND h.head_id IS NOT NULL
                                  AND NOT EXISTS (
                                      SELECT 1 FROM memberships m
                                      WHERE m.household_id = h.id AND m.individual_id = h.head_id
                                        AND m.relationship_to_head = 'HEAD' AND m.end_date IS NULL)
                                """.formatted(HOUSEHOLD_TOUCHED)),
                check("M3", "A household has at most one open HEAD membership",
                        """
                                SELECT h.client_id AS ref, CONCAT(COUNT(*), ' open HEAD memberships') AS detail
                                FROM households h
                                JOIN memberships m ON m.household_id = h.id
                                                  AND m.relationship_to_head = 'HEAD' AND m.end_date IS NULL
                                WHERE %s
                                GROUP BY h.id, h.client_id
                                HAVING COUNT(*) > 1
                                """.formatted(HOUSEHOLD_TOUCHED)),
                check("M4", "One person's memberships never overlap in time",
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('memberships ', m1.client_id, ' and ', m2.client_id, ' overlap') AS detail
                                FROM memberships m1
                                JOIN memberships m2 ON m2.individual_id = m1.individual_id AND m2.id > m1.id
                                JOIN individuals i ON i.id = m1.individual_id
                                WHERE (m1.is_synced = 0 OR m2.is_synced = 0)
                                  AND m1.start_date < COALESCE(m2.end_date, :today)
                                  AND m2.start_date < COALESCE(m1.end_date, :today)
                                """),
                check("M5", "End date is not before start date, and end type is set exactly when end date is",
                        """
                                SELECT m.client_id AS ref,
                                       CASE WHEN m.end_date < m.start_date THEN 'ends before it starts'
                                            WHEN m.end_date IS NULL THEN CONCAT('open membership has end type ', m.end_type)
                                            ELSE 'has an end date but no end type' END AS detail
                                FROM memberships m
                                WHERE m.is_synced = 0
                                  AND (m.end_date < m.start_date OR (m.end_date IS NULL) <> (m.end_type IS NULL))
                                """),
                check("M6", "Membership dates are not in the future",
                        """
                                SELECT m.client_id AS ref, 'start or end date is in the future' AS detail
                                FROM memberships m
                                WHERE m.is_synced = 0 AND (m.start_date > :today OR m.end_date > :today)
                                """),
                check("M7", "A membership starts on or after the person's date of birth",
                        """
                                SELECT m.client_id AS ref,
                                       CONCAT('starts ', m.start_date, ' before birth on ', i.date_of_birth) AS detail
                                FROM memberships m
                                JOIN individuals i ON i.id = m.individual_id
                                WHERE (m.is_synced = 0 OR i.is_synced = 0) AND m.start_date < i.date_of_birth
                                """),
                check("M8", "A BIRTH membership starts exactly on the date of birth",
                        """
                                SELECT m.client_id AS ref,
                                       CONCAT('BIRTH membership starts ', m.start_date, ' but birth is ', i.date_of_birth) AS detail
                                FROM memberships m
                                JOIN individuals i ON i.id = m.individual_id
                                WHERE (m.is_synced = 0 OR i.is_synced = 0)
                                  AND m.start_type = 'BIRTH' AND m.start_date <> i.date_of_birth
                                """),
                check("M9", "Every household and every individual has at least one membership",
                        """
                                SELECT h.client_id AS ref, 'household has no memberships' AS detail
                                FROM households h
                                WHERE h.is_synced = 0
                                  AND NOT EXISTS (SELECT 1 FROM memberships m WHERE m.household_id = h.id)
                                """,
                        """
                                SELECT i.client_id AS ref, 'individual has no memberships' AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0
                                  AND NOT EXISTS (SELECT 1 FROM memberships m WHERE m.individual_id = i.id)
                                """),
                check("M10", "Nothing follows a death (no later or open membership)",
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('membership ', m2.client_id, ' follows a DEATH recorded on ', m1.end_date) AS detail
                                FROM memberships m1
                                JOIN memberships m2 ON m2.individual_id = m1.individual_id AND m2.id <> m1.id
                                JOIN individuals i ON i.id = m1.individual_id
                                WHERE m1.end_type = 'DEATH'
                                  AND (m1.is_synced = 0 OR m2.is_synced = 0)
                                  AND (m2.start_date > m1.end_date OR m2.end_date IS NULL)
                                """),
                check("M11", "Transfers pair up (TRANSFER_IN starts the day a TRANSFER_OUT ended, and vice versa)",
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('TRANSFER_IN membership ', m.client_id, ' has no TRANSFER_OUT ending on ', m.start_date) AS detail
                                FROM memberships m
                                JOIN individuals i ON i.id = m.individual_id
                                WHERE m.start_type = 'TRANSFER_IN'
                                  AND %s
                                  AND NOT EXISTS (
                                      SELECT 1 FROM memberships o
                                      WHERE o.individual_id = m.individual_id AND o.household_id <> m.household_id
                                        AND o.end_type = 'TRANSFER_OUT' AND o.end_date = m.start_date)
                                """.formatted(MEMBERSHIP_TOUCHED),
                        """
                                SELECT i.client_id AS ref,
                                       CONCAT('TRANSFER_OUT membership ', m.client_id, ' has no TRANSFER_IN starting on ', m.end_date) AS detail
                                FROM memberships m
                                JOIN individuals i ON i.id = m.individual_id
                                WHERE m.end_type = 'TRANSFER_OUT'
                                  AND %s
                                  AND NOT EXISTS (
                                      SELECT 1 FROM memberships n
                                      WHERE n.individual_id = m.individual_id AND n.household_id <> m.household_id
                                        AND n.start_type = 'TRANSFER_IN' AND n.start_date = m.end_date)
                                """.formatted(MEMBERSHIP_TOUCHED)),

                // ---- Individuals ----
                check("I1", "First name, last name and sex are present",
                        """
                                SELECT i.client_id AS ref, 'first name, last name or sex is missing' AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0
                                  AND (TRIM(i.first_name) = '' OR TRIM(i.last_name) = '' OR i.sex IS NULL)
                                """),
                check("I2", "Date of birth is not in the future and age is at most " + MAX_AGE_YEARS,
                        """
                                SELECT i.client_id AS ref,
                                       CASE WHEN i.date_of_birth > :today
                                            THEN CONCAT('date of birth ', i.date_of_birth, ' is in the future')
                                            ELSE CONCAT('age ', TIMESTAMPDIFF(YEAR, i.date_of_birth, :today), ' exceeds ', :maxAge)
                                       END AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0
                                  AND (i.date_of_birth > :today
                                    OR TIMESTAMPDIFF(YEAR, i.date_of_birth, :today) > :maxAge)
                                """),
                check("I3", "Date of birth is present for everyone",
                        """
                                SELECT i.client_id AS ref, 'date of birth is missing' AS detail
                                FROM individuals i
                                WHERE i.is_synced = 0 AND i.date_of_birth IS NULL
                                """),
                check("I4", "No likely duplicate individuals within a household",
                        """
                                SELECT DISTINCT ia.client_id AS ref,
                                       CONCAT('looks like a duplicate of ', ib.client_id, ' in household ', ma.household_id) AS detail
                                FROM memberships ma
                                JOIN memberships mb ON mb.household_id = ma.household_id AND mb.individual_id > ma.individual_id
                                JOIN individuals ia ON ia.id = ma.individual_id
                                JOIN individuals ib ON ib.id = mb.individual_id
                                WHERE (ia.is_synced = 0 OR ib.is_synced = 0 OR ma.is_synced = 0 OR mb.is_synced = 0)
                                  AND ia.first_name = ib.first_name AND ia.last_name = ib.last_name
                                  AND ia.sex = ib.sex AND ia.date_of_birth = ib.date_of_birth
                                """),

                // ---- Visits, responses, answers (movable = under a COMPLETED visit) ----
                check("V2", "A FormResponse is COMPLETED",
                        """
                                SELECT fr.client_id AS ref, CONCAT('response status is ', fr.status) AS detail
                                FROM form_responses fr
                                JOIN visits v ON v.id = fr.visit_id
                                WHERE fr.is_synced = 0 AND v.status = 'COMPLETED' AND fr.status <> 'COMPLETED'
                                """),
                check("V3", "An answer's question belongs to the form its response was filled in",
                        """
                                SELECT a.client_id AS ref,
                                       CONCAT('question ', q.id, ' belongs to form ', q.form_id,
                                              ' but the response is for form ', fr.form_id) AS detail
                                FROM answers a
                                JOIN form_responses fr ON fr.id = a.form_response_id
                                JOIN visits v ON v.id = fr.visit_id
                                JOIN questions q ON q.id = a.question_id
                                WHERE a.is_synced = 0 AND v.status = 'COMPLETED' AND q.form_id <> fr.form_id
                                """),
                check("V4", "At most one answer per question per response",
                        """
                                SELECT MIN(a.client_id) AS ref,
                                       CONCAT(COUNT(*), ' answers for question ', a.question_id, ' in response ', fr.client_id) AS detail
                                FROM answers a
                                JOIN form_responses fr ON fr.id = a.form_response_id
                                JOIN visits v ON v.id = fr.visit_id
                                WHERE v.status = 'COMPLETED'
                                  AND a.form_response_id IN (SELECT a2.form_response_id FROM answers a2 WHERE a2.is_synced = 0)
                                GROUP BY a.form_response_id, a.question_id, fr.client_id
                                HAVING COUNT(*) > 1
                                """),
                check("V6", "FormResponse.formVersion equals the version of its form",
                        """
                                SELECT fr.client_id AS ref,
                                       CONCAT('response used version ', fr.form_version, ' but the form is at version ', f.version) AS detail
                                FROM form_responses fr
                                JOIN visits v ON v.id = fr.visit_id
                                JOIN forms f ON f.id = fr.form_id
                                WHERE fr.is_synced = 0 AND v.status = 'COMPLETED' AND fr.form_version <> f.version
                                """),

                // ---- Forms and questions ----
                check("F1", "Every form that has responses is PUBLISHED",
                        """
                                SELECT DISTINCT f.name AS ref, CONCAT('form is ', f.status, ' but has responses') AS detail
                                FROM form_responses fr
                                JOIN visits v ON v.id = fr.visit_id
                                JOIN forms f ON f.id = fr.form_id
                                WHERE fr.is_synced = 0 AND v.status = 'COMPLETED' AND f.status <> 'PUBLISHED'
                                """),
                check("F2", "Question name and order index are unique within a published form",
                        """
                                SELECT f.name AS ref,
                                       CONCAT('order index ', q.order_index, ' used by ', COUNT(*), ' questions') AS detail
                                FROM questions q
                                JOIN forms f ON f.id = q.form_id
                                WHERE f.status = 'PUBLISHED'
                                GROUP BY f.id, f.name, q.order_index
                                HAVING COUNT(*) > 1
                                """,
                        """
                                SELECT f.name AS ref,
                                       CONCAT('question name ', q.name, ' used ', COUNT(*), ' times') AS detail
                                FROM questions q
                                JOIN forms f ON f.id = q.form_id
                                WHERE f.status = 'PUBLISHED'
                                GROUP BY f.id, f.name, q.name
                                HAVING COUNT(*) > 1
                                """)
        );
    }
}
