package com.andrew.hdss.round;

import com.andrew.hdss.exceptions.RoundCopyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class RoundDatabaseCopier {

    private static final Pattern DATABASE_NAME =
            Pattern.compile("[a-zA-Z0-9_]{1,64}");

    private static final int MAX_ERROR_LENGTH = 32_000;

    private final JdbcTemplate jdbc;
    private final String datasourceUrl;
    private final String username;
    private final String password;
    private final Duration timeout;

    public RoundDatabaseCopier(
            JdbcTemplate jdbc,
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${hdss.round.copy-timeout-minutes:30}") long timeoutMinutes) {

        this.jdbc = jdbc;
        this.datasourceUrl = datasourceUrl;
        this.username = username;
        this.password = password;
        this.timeout = Duration.ofMinutes(timeoutMinutes);

        if (timeoutMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Round copy timeout must be greater than zero");
        }
    }

    /**
     * Creates a complete snapshot of the live database.
     * <p>
     * The destination database must not already exist.
     *
     * @param databaseName generated destination database name
     */
    public void copyDatabase(String databaseName) {

        validateDatabaseName(databaseName);

        DatabaseConnection source = parseDatasourceUrl();

        Path credentialsFile = null;
        boolean databaseCreated = false;

        try {
            credentialsFile = createCredentialsFile(source);

            createDatabase(databaseName);
            databaseCreated = true;

            executeDump(source, databaseName, credentialsFile);

        } catch (Exception e) {

            if (databaseCreated) {
                try {
                    dropDatabase(databaseName);
                } catch (Exception cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            }

            throw new RoundCopyException(
                    "Failed to create database snapshot: " + e.getMessage(), e);

        } finally {

            if (credentialsFile != null) {
                try {
                    Files.deleteIfExists(credentialsFile);
                } catch (IOException ignored) {
                    // Do not expose credentials or mask the original failure.
                }
            }
        }
    }

    private void createDatabase(String databaseName) {

        // Preserve the source database's default character set and collation.
        String[] defaults = jdbc.queryForObject("""
                        SELECT DEFAULT_CHARACTER_SET_NAME,
                               DEFAULT_COLLATION_NAME
                        FROM information_schema.SCHEMATA
                        WHERE SCHEMA_NAME = ?
                        """,
                (rs, rowNum) -> new String[]{
                        rs.getString(1),
                        rs.getString(2)
                },
                parseDatasourceUrl().database()
        );

        if (defaults == null) {
            throw new IllegalStateException(
                    "Could not determine source database character set");
        }

        String charset = defaults[0];
        String collation = defaults[1];

        if (!charset.matches("[a-zA-Z0-9_]+")
                || !collation.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalStateException(
                    "Invalid source database character set or collation");
        }

        jdbc.execute("""
                CREATE DATABASE `%s`
                CHARACTER SET %s
                COLLATE %s
                """.formatted(databaseName, charset, collation));
    }

    private void dropDatabase(String databaseName) {

        validateDatabaseName(databaseName);

        jdbc.execute("DROP DATABASE IF EXISTS `" + databaseName + "`");
    }

    private void executeDump(
            DatabaseConnection source,
            String destinationDatabase,
            Path credentialsFile) throws Exception {

        List<String> dumpCommand = List.of(
                "mysqldump",
                "--defaults-extra-file=" + credentialsFile,
                "--single-transaction",
                "--quick",
                "--skip-lock-tables",
                "--routines",
                "--triggers",
                "--events",
                "--hex-blob",
                "--no-tablespaces",
                "--set-gtid-purged=OFF",
                "--default-character-set=utf8mb4",
                source.database()
        );

        List<String> importCommand = List.of(
                "mysql",
                "--defaults-extra-file=" + credentialsFile,
                "--default-character-set=utf8mb4",
                destinationDatabase
        );

        ProcessBuilder dumpBuilder = new ProcessBuilder(dumpCommand);
        ProcessBuilder importBuilder = new ProcessBuilder(importCommand);

        // Do not merge stderr into stdout: stdout is the SQL pipeline.
        List<Process> processes = ProcessBuilder.startPipeline(
                List.of(dumpBuilder, importBuilder));

        Process dumpProcess = processes.get(0);
        Process importProcess = processes.get(1);

        // Drain both error streams concurrently to prevent deadlocks.
        CompletableFuture<String> dumpErrors =
                CompletableFuture.supplyAsync(
                        () -> readErrorStream(dumpProcess.getErrorStream()));

        CompletableFuture<String> importErrors =
                CompletableFuture.supplyAsync(
                        () -> readErrorStream(importProcess.getErrorStream()));

        long deadline = System.nanoTime() + timeout.toNanos();

        try {
            waitForProcess(dumpProcess, deadline, "mysqldump");
            waitForProcess(importProcess, deadline, "mysql");

            String dumpError = dumpErrors.get(30, TimeUnit.SECONDS);
            String importError = importErrors.get(30, TimeUnit.SECONDS);

            if (dumpProcess.exitValue() != 0
                    || importProcess.exitValue() != 0) {

                throw new IllegalStateException(
                        "Database copy failed."
                                + "\nmysqldump exit code: "
                                + dumpProcess.exitValue()
                                + "\nmysql exit code: "
                                + importProcess.exitValue()
                                + "\nmysqldump errors: " + dumpError
                                + "\nmysql errors: " + importError);
            }

        } catch (Exception e) {

            processes.forEach(process -> {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            });

            throw e;
        }
    }

    private void waitForProcess(
            Process process,
            long deadline,
            String processName) throws InterruptedException {

        long remaining = deadline - System.nanoTime();

        if (remaining <= 0
                || !process.waitFor(remaining, TimeUnit.NANOSECONDS)) {

            throw new IllegalStateException(
                    processName + " exceeded the configured timeout");
        }
    }

    private String readErrorStream(InputStream stream) {

        StringBuilder errors = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (errors.length() < MAX_ERROR_LENGTH) {
                    errors.append(line).append('\n');
                }
            }

        } catch (IOException e) {
            errors.append("Could not read process error output: ")
                    .append(e.getMessage());
        }

        return errors.toString();
    }

    private Path createCredentialsFile(DatabaseConnection source)
            throws IOException {

        Path file = Files.createTempFile("hdss-mysql-", ".cnf");

        try {
            Files.setPosixFilePermissions(
                    file,
                    PosixFilePermissions.fromString("rw-------"));

            String content = """
                    [client]
                    user="%s"
                    password="%s"
                    host="%s"
                    port=%d
                    protocol=TCP
                    """.formatted(
                    escapeOptionValue(username),
                    escapeOptionValue(password),
                    escapeOptionValue(source.host()),
                    source.port()
            );

            Files.writeString(file, content, StandardCharsets.UTF_8);

            return file;

        } catch (Exception e) {
            Files.deleteIfExists(file);
            throw e;
        }
    }

    private String escapeOptionValue(String value) {

        if (value == null || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(
                    "Invalid MySQL option value");
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private DatabaseConnection parseDatasourceUrl() {

        final String prefix = "jdbc:mysql://";

        if (!datasourceUrl.startsWith(prefix)) {
            throw new IllegalStateException(
                    "Only jdbc:mysql URLs are supported for round creation");
        }

        try {
            URI uri = URI.create(
                    "mysql://" + datasourceUrl.substring(prefix.length()));

            String database = uri.getPath();

            if (database == null || database.length() <= 1) {
                throw new IllegalStateException(
                        "No source database found in datasource URL");
            }

            database = database.substring(1);

            if (!database.matches("[a-zA-Z0-9_]+")) {
                throw new IllegalStateException(
                        "Unsupported source database name");
            }

            String host = uri.getHost();

            if (host == null || host.isBlank()) {
                throw new IllegalStateException(
                        "Could not determine MySQL host");
            }

            int port = uri.getPort() == -1 ? 3306 : uri.getPort();

            return new DatabaseConnection(host, port, database);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Could not parse MySQL datasource URL", e);
        }
    }

    private void validateDatabaseName(String databaseName) {

        if (databaseName == null
                || !DATABASE_NAME.matcher(databaseName).matches()) {
            throw new IllegalArgumentException(
                    "Invalid round database name");
        }
    }

    private record DatabaseConnection(
            String host,
            int port,
            String database) {
    }
}