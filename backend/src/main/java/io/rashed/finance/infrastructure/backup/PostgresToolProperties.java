package io.rashed.finance.infrastructure.backup;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Location of the Postgres client tools, bound from
 * {@code app.backup.postgres}.
 *
 * The PG_DUMP format shells out to these binaries, so they must be on the
 * host and their version must be at least that of the server. Defaults
 * assume they are on PATH; set an absolute path when they are not.
 *
 * @param pgDump    {@code pg_dump} executable.
 * @param pgRestore {@code pg_restore} executable.
 * @param timeout   how long either tool may run before being killed.
 */
@ConfigurationProperties(prefix = "app.backup.postgres")
public record PostgresToolProperties(
        String pgDump,
        String pgRestore,
        Duration timeout
) {

    public PostgresToolProperties {

        if (pgDump == null || pgDump.isBlank()) {
            pgDump = "pg_dump";
        }

        if (pgRestore == null || pgRestore.isBlank()) {
            pgRestore = "pg_restore";
        }

        if (timeout == null) {
            timeout = Duration.ofMinutes(10);
        }
    }
}
