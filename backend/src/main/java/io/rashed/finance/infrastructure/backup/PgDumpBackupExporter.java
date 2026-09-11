package io.rashed.finance.infrastructure.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupExporter;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.common.enums.BackupFormat;

/**
 * Produces a physical {@code pg_dump} custom-format dump.
 *
 * Unlike {@link JsonBackupExporter} this captures the <em>whole</em>
 * database — schema included, and every table, not the curated list in
 * {@link BackupTables}. That means users, auth tokens, backup history and
 * the Flyway history are all in the dump. It is the more faithful backup
 * and the blunter restore; see docs/operations/BackupStrategy.md for the
 * trade-off.
 */
@Component
public class PgDumpBackupExporter implements BackupExporter {

    private static final Logger log = LoggerFactory.getLogger(PgDumpBackupExporter.class);

    private final PostgresToolRunner runner;
    private final PostgresToolProperties properties;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public PgDumpBackupExporter(
            PostgresToolRunner runner,
            PostgresToolProperties properties,
            @Value("${spring.datasource.url:}") String jdbcUrl,
            @Value("${spring.datasource.username:}") String username,
            @Value("${spring.datasource.password:}") String password
    ) {
        this.runner = Objects.requireNonNull(runner);
        this.properties = Objects.requireNonNull(properties);
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public BackupFormat format() {
        return BackupFormat.PG_DUMP;
    }

    @Override
    public BackupArchive export(String fileName) {

        PostgresConnection connection = PostgresConnection.from(jdbcUrl, username, password);

        // pg_dump writes to a file rather than stdout here: the custom
        // format is binary, and --file avoids any question of how the
        // platform treats the stream.
        Path dumpFile = createTempFile();

        try {
            runner.run(
                    List.of(
                            properties.pgDump(),
                            "--host=" + connection.host(),
                            "--port=" + connection.port(),
                            "--username=" + connection.username(),
                            "--dbname=" + connection.database(),
                            "--format=custom",
                            // The restoring role is whatever the app runs
                            // as, which may not match the dumping role.
                            "--no-owner",
                            "--no-privileges",
                            "--file=" + dumpFile.toAbsolutePath()
                    ),
                    connection,
                    properties.timeout(),
                    null
            );

            byte[] content = Files.readAllBytes(dumpFile);

            log.info("pg_dump produced {} bytes for backup {}", content.length, fileName);

            return BackupArchive.of(fileName, BackupFormat.PG_DUMP, content);

        } catch (IOException ex) {
            throw new BackupFailedException("Could not read the pg_dump output file.", ex);

        } finally {
            deleteQuietly(dumpFile);
        }
    }

    private Path createTempFile() {

        try {
            return Files.createTempFile("finance-backup-", ".dump");

        } catch (IOException ex) {
            throw new BackupFailedException("Could not create a temporary file for pg_dump.", ex);
        }
    }

    static void deleteQuietly(Path path) {

        try {
            Files.deleteIfExists(path);

        } catch (IOException ex) {
            // The archive is already safely stored (or the backup already
            // failed); a leftover temp file is not worth failing over.
            log.warn("Could not delete temporary file {}: {}", path, ex.getMessage());
        }
    }
}
