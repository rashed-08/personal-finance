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
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.BackupImporter;
import io.rashed.finance.common.enums.BackupFormat;

/**
 * Restores a {@code pg_dump} custom-format dump with {@code pg_restore}.
 *
 * <h2>This replaces the entire database</h2>
 * {@code --clean --if-exists} drops and recreates every object in the
 * dump, so unlike {@link JsonBackupImporter} this does <em>not</em>
 * respect {@link BackupTables}' exclusions:
 *
 * <ul>
 *   <li>users and auth tokens are replaced — anyone signed in is signed
 *       out, and passwords revert to those in the dump;</li>
 *   <li>backup history is replaced — the record of this very restore is
 *       written by the calling service after {@code pg_restore} returns,
 *       so it survives, but earlier history reverts to the dump's;</li>
 *   <li>the Flyway history is replaced, so restoring an older dump also
 *       reverts the schema version. Restart the application afterwards to
 *       let Flyway migrate forward again.</li>
 * </ul>
 *
 * Choose the JSON format instead when the intent is to restore the ledger
 * without disturbing accounts and history.
 */
@Component
public class PgRestoreBackupImporter implements BackupImporter {

    private static final Logger log = LoggerFactory.getLogger(PgRestoreBackupImporter.class);

    private final PostgresToolRunner runner;
    private final PostgresToolProperties properties;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public PgRestoreBackupImporter(
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

    /**
     * Deliberately not {@code @Transactional}: {@code pg_restore} runs in
     * its own connection and manages its own transaction via
     * {@code --single-transaction}. Wrapping it in a Spring transaction
     * would add a second, pointless one that cannot roll back the child
     * process anyway.
     */
    @Override
    public void restore(BackupArchive archive) {

        Objects.requireNonNull(archive, "Archive cannot be null.");

        PostgresConnection connection = PostgresConnection.from(jdbcUrl, username, password);

        Path dumpFile = writeToTempFile(archive);

        try {
            runner.run(
                    List.of(
                            properties.pgRestore(),
                            "--host=" + connection.host(),
                            "--port=" + connection.port(),
                            "--username=" + connection.username(),
                            "--dbname=" + connection.database(),
                            // Drop existing objects first; --if-exists so a
                            // missing object is not an error.
                            "--clean",
                            "--if-exists",
                            "--no-owner",
                            "--no-privileges",
                            // All-or-nothing: a failure part-way leaves the
                            // database as it was.
                            "--single-transaction",
                            dumpFile.toAbsolutePath().toString()
                    ),
                    connection,
                    properties.timeout(),
                    null
            );

            log.warn(
                    "pg_restore replaced the whole database from {}. Sessions are invalidated and "
                            + "the Flyway history now matches the dump — restart the application.",
                    archive.fileName()
            );

        } finally {
            PgDumpBackupExporter.deleteQuietly(dumpFile);
        }
    }

    private Path writeToTempFile(BackupArchive archive) {

        try {
            Path file = Files.createTempFile("finance-restore-", ".dump");

            Files.write(file, archive.content());

            return file;

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not stage the archive for pg_restore.", ex);
        }
    }
}
