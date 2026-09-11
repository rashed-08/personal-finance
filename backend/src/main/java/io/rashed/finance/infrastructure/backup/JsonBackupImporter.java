package io.rashed.finance.infrastructure.backup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.BackupImporter;
import io.rashed.finance.common.enums.BackupFormat;

/**
 * Replaces the contents of the backed-up tables with an archive written
 * by {@link JsonBackupExporter}.
 *
 * Not final: {@code @Transactional} is applied through a CGLIB proxy,
 * which subclasses this type.
 *
 * The whole restore — every delete and every insert — runs in one
 * transaction, so a failure half-way leaves the existing ledger exactly
 * as it was. That is the entire reason this is destructive-but-safe
 * rather than destructive-and-hopeful.
 */
@Component
public class JsonBackupImporter implements BackupImporter {

    private static final Logger log = LoggerFactory.getLogger(JsonBackupImporter.class);

    /**
     * Rows per batch. Large enough that a full ledger is a handful of
     * round trips, small enough not to build a multi-megabyte statement.
     */
    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JsonBackupImporter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public BackupFormat format() {
        return BackupFormat.JSON;
    }

    @Override
    @Transactional
    public void restore(BackupArchive archive) {

        Objects.requireNonNull(archive, "Archive cannot be null.");

        DatabaseSnapshot snapshot = read(archive);

        requireSupportedVersion(snapshot);

        deleteExistingRows();

        int restored = 0;

        for (String table : BackupTables.insertOrder()) {

            List<Map<String, String>> rows = snapshot.tables().get(table);

            if (rows == null) {
                // Archive predates this table. Leaving it empty is correct:
                // the backup genuinely held nothing for it.
                log.info("Archive contains no data for table {}; leaving it empty.", table);
                continue;
            }

            restored += insertRows(table, rows);
        }

        // Self-references are nulled during insert and linked here, once
        // every row exists. See BackupTables.selfReferencingColumns.
        for (String table : BackupTables.insertOrder()) {

            List<Map<String, String>> rows = snapshot.tables().get(table);

            if (rows != null) {
                linkSelfReferences(table, rows);
            }
        }

        log.info("Restored {} rows from archive {}", restored, archive.fileName());
    }

    // -------------------------------------------------------------------------
    // Reading the archive
    // -------------------------------------------------------------------------

    private DatabaseSnapshot read(BackupArchive archive) {

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive.content()))) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {

                if (JsonBackupExporter.ARCHIVE_ENTRY.equals(entry.getName())) {
                    return objectMapper.readValue(zip.readAllBytes(), DatabaseSnapshot.class);
                }
            }

            throw new BackupFailedException(
                    "Archive " + archive.fileName() + " does not contain "
                            + JsonBackupExporter.ARCHIVE_ENTRY + "; it is not a JSON backup.");

        } catch (IOException ex) {
            // Not a zip at all, or truncated.
            throw new BackupFailedException(
                    "Could not read archive " + archive.fileName() + ": " + ex.getMessage(), ex);

        } catch (JacksonException ex) {
            // A zip holding a backup.json that is not a snapshot document.
            throw new BackupFailedException(
                    "Archive " + archive.fileName() + " is not a valid JSON backup: "
                            + ex.getMessage(), ex);
        }
    }

    private void requireSupportedVersion(DatabaseSnapshot snapshot) {

        if (snapshot.schemaVersion() > DatabaseSnapshot.SCHEMA_VERSION) {
            throw new BackupFailedException(
                    "Archive was written by a newer version of the application (archive format "
                            + snapshot.schemaVersion() + ", this build understands up to "
                            + DatabaseSnapshot.SCHEMA_VERSION + ").");
        }
    }

    // -------------------------------------------------------------------------
    // Writing to the database
    // -------------------------------------------------------------------------

    private void deleteExistingRows() {

        for (String table : BackupTables.deleteOrder()) {

            // Break self-references before deleting the table.
            //
            // fk_transactions_reference_transaction is ON DELETE RESTRICT,
            // and Postgres checks RESTRICT immediately, per row — unlike
            // NO ACTION it cannot be deferred to the end of the statement.
            // So a single "DELETE FROM transactions" fails as soon as it
            // reaches a row that another row references, even though that
            // other row is being deleted by the same statement. Any ledger
            // containing a reversal or a reconciliation adjustment has
            // such a pair.
            for (String column : BackupTables.selfReferencingColumns(table)) {
                jdbcTemplate.update("UPDATE " + table + " SET " + column + " = NULL");
            }

            // Children first, so plain DELETE never trips a foreign key
            // between tables. Not TRUNCATE: it cannot be rolled back as
            // cheaply and would need CASCADE, which reaches tables this
            // backup excludes.
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    private int insertRows(String table, List<Map<String, String>> rows) {

        if (rows.isEmpty()) {
            return 0;
        }

        Map<String, String> columnTypes = columnTypes(table);

        // Only columns the archive has AND the table still has. A column
        // added since the backup falls back to its database default; one
        // dropped since is skipped.
        List<String> columns = rows.getFirst()
                .keySet()
                .stream()
                .filter(columnTypes::containsKey)
                .toList();

        if (columns.isEmpty()) {
            throw new BackupFailedException(
                    "No column in the archive matches table " + table
                            + "; the archive does not fit this schema.");
        }

        warnAboutDroppedColumns(table, rows.getFirst().keySet(), columnTypes);

        List<String> selfReferences = BackupTables.selfReferencingColumns(table);

        String sql = insertStatement(table, columns, columnTypes);

        List<Object[]> batch = rows.stream()
                .map(row -> toParameters(row, columns, selfReferences))
                .toList();

        for (int start = 0; start < batch.size(); start += BATCH_SIZE) {

            int end = Math.min(start + BATCH_SIZE, batch.size());

            try {
                jdbcTemplate.batchUpdate(sql, batch.subList(start, end));

            } catch (Exception ex) {
                throw new BackupFailedException(
                        "Could not restore table " + table + ": " + ex.getMessage(), ex);
            }
        }

        return rows.size();
    }

    /**
     * Values travel as text and are cast to the column's own type in SQL
     * ({@code ?::numeric}, {@code ?::uuid}, …). That keeps the importer
     * free of a Java type map and lets Postgres do the parsing it already
     * does correctly.
     */
    private String insertStatement(String table, List<String> columns, Map<String, String> columnTypes) {

        String columnList = String.join(", ", columns);

        String placeholders = columns.stream()
                .map(column -> "?::" + columnTypes.get(column))
                .reduce((left, right) -> left + ", " + right)
                .orElseThrow();

        return "INSERT INTO " + table + " (" + columnList + ") VALUES (" + placeholders + ")";
    }

    private Object[] toParameters(
            Map<String, String> row,
            List<String> columns,
            List<String> selfReferences
    ) {

        Object[] parameters = new Object[columns.size()];

        for (int index = 0; index < columns.size(); index++) {

            String column = columns.get(index);

            parameters[index] = selfReferences.contains(column) ? null : row.get(column);
        }

        return parameters;
    }

    private void linkSelfReferences(String table, List<Map<String, String>> rows) {

        List<String> selfReferences = BackupTables.selfReferencingColumns(table);

        if (selfReferences.isEmpty() || rows.isEmpty()) {
            return;
        }

        Map<String, String> columnTypes = columnTypes(table);

        for (String column : selfReferences) {

            if (!columnTypes.containsKey(column)) {
                continue;
            }

            String sql = "UPDATE " + table
                    + " SET " + column + " = ?::" + columnTypes.get(column)
                    + " WHERE id = ?::" + columnTypes.get("id");

            List<Object[]> batch = rows.stream()
                    .filter(row -> row.get(column) != null)
                    .map(row -> new Object[]{row.get(column), row.get("id")})
                    .toList();

            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(sql, batch);
                log.info("Linked {} self-references on {}.{}", batch.size(), table, column);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Schema introspection
    // -------------------------------------------------------------------------

    /**
     * Column name to Postgres type name for the live table, read from
     * result-set metadata rather than {@code information_schema} so the
     * names are exactly the ones a cast will accept.
     */
    private Map<String, String> columnTypes(String table) {

        try {
            return jdbcTemplate.query(
                    // WHERE false: metadata only, no rows fetched.
                    "SELECT * FROM " + table + " WHERE false",
                    resultSet -> {

                        ResultSetMetaData metaData = resultSet.getMetaData();

                        Map<String, String> types = new LinkedHashMap<>();

                        for (int column = 1; column <= metaData.getColumnCount(); column++) {
                            types.put(
                                    metaData.getColumnLabel(column),
                                    metaData.getColumnTypeName(column)
                            );
                        }

                        return types;
                    }
            );

        } catch (Exception ex) {
            throw new BackupFailedException(
                    "Could not inspect table " + table + ": " + ex.getMessage(), ex);
        }
    }

    private void warnAboutDroppedColumns(
            String table,
            java.util.Set<String> archiveColumns,
            Map<String, String> columnTypes
    ) {

        List<String> dropped = new ArrayList<>();

        for (String column : archiveColumns) {
            if (!columnTypes.containsKey(column)) {
                dropped.add(column);
            }
        }

        if (!dropped.isEmpty()) {
            log.warn(
                    "Archive column(s) {} no longer exist on {} and will not be restored.",
                    dropped, table
            );
        }
    }
}
