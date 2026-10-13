package com.andrew.hdss.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

@Component
public class RoundDatabase {

    private final HikariDataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private volatile boolean schemaReady = false;

    public RoundDatabase(
            @Value("${round.datasource.url}") String url,
            @Value("${round.datasource.username}") String username,
            @Value("${round.datasource.password}") String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("round-pool");
        config.setMaximumPoolSize(5);
        // Don't fail application startup if the round database is
        // unreachable; live pushes must keep working regardless.
        config.setInitializationFailTimeout(-1);

        this.dataSource = new HikariDataSource(config);
        this.jdbc = new JdbcTemplate(dataSource);
        this.transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    public JdbcTemplate jdbc() {
        ensureSchema();
        return jdbc;
    }

    // Everything inside commits together or rolls back together.
    public <T> T inTransaction(Function<JdbcTemplate, T> work) {
        ensureSchema();
        return transactionTemplate.execute(status -> work.apply(jdbc));
    }

    private synchronized void ensureSchema() {
        if (schemaReady) return;
        new ResourceDatabasePopulator(new ClassPathResource("db/round/schema.sql"))
                .execute(dataSource);
        schemaReady = true;
    }

    @PreDestroy
    void close() {
        dataSource.close();
    }
}