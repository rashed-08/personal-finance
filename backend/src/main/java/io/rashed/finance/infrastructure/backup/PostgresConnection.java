package io.rashed.finance.infrastructure.backup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import io.rashed.finance.application.backup.BackupFailedException;

/**
 * Connection details the Postgres client tools need, pulled apart from the
 * JDBC URL the application already has.
 *
 * {@code pg_dump} takes host, port and database as separate arguments, so
 * the single {@code spring.datasource.url} has to be split. Query
 * parameters are ignored: they configure the JDBC driver, not the tools.
 *
 * @param host     server host.
 * @param port     server port; 5432 when the URL omits it.
 * @param database database name.
 * @param username role to connect as.
 * @param password password, passed to the tools through the environment so
 *                 it never appears in a command line or process list.
 */
public record PostgresConnection(
        String host,
        int port,
        String database,
        String username,
        String password
) {

    private static final int DEFAULT_PORT = 5432;

    private static final String JDBC_PREFIX = "jdbc:";

    public PostgresConnection {
        Objects.requireNonNull(host, "Database host cannot be null.");
        Objects.requireNonNull(database, "Database name cannot be null.");
    }

    /**
     * @param jdbcUrl e.g. {@code jdbc:postgresql://localhost:5432/personal_finance}
     * @throws BackupFailedException if the URL is not a Postgres JDBC URL
     *         naming a database — a physical dump cannot be produced
     *         without knowing what to dump.
     */
    public static PostgresConnection from(String jdbcUrl, String username, String password) {

        if (jdbcUrl == null || !jdbcUrl.startsWith(JDBC_PREFIX + "postgresql://")) {
            throw new BackupFailedException(
                    "The PG_DUMP backup format requires a PostgreSQL datasource; "
                            + "spring.datasource.url is " + jdbcUrl + ".");
        }

        try {
            URI uri = new URI(jdbcUrl.substring(JDBC_PREFIX.length()));

            String path = uri.getPath();

            if (path == null || path.length() <= 1) {
                throw new BackupFailedException(
                        "spring.datasource.url does not name a database: " + jdbcUrl);
            }

            return new PostgresConnection(
                    uri.getHost(),
                    uri.getPort() == -1 ? DEFAULT_PORT : uri.getPort(),
                    path.substring(1),
                    username,
                    password
            );

        } catch (URISyntaxException ex) {
            throw new BackupFailedException(
                    "Could not parse spring.datasource.url: " + jdbcUrl, ex);
        }
    }
}
