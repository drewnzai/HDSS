package com.andrew.hdss.round;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RoundCreationLock {

    private static final String LOCK_NAME = "hdss_round_creation";

    private final DataSource dataSource;

    public RoundCreationLock(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public Lease tryAcquire() {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT GET_LOCK(?, 0)")) {

                statement.setString(1, LOCK_NAME);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || result.getInt(1) != 1 || result.wasNull()) {
                        connection.close();
                        return null;
                    }
                }
            }

            return new Lease(connection);

        } catch (SQLException e) {
            closeQuietly(connection);
            throw new IllegalStateException(
                    "Could not acquire the round creation lock", e
            );
        }
    }

    public static class Lease implements AutoCloseable {

        private Connection connection;

        private Lease(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void close() {
            if (connection == null) {
                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {

                statement.setString(1, LOCK_NAME);
                statement.executeQuery();

            } catch (SQLException ignored) {
                // Closing the connection also releases its MySQL advisory locks.
            } finally {
                closeQuietly(connection);
                connection = null;
            }
        }
    }
}