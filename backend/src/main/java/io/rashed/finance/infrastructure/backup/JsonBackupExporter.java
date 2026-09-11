package io.rashed.finance.infrastructure.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupExporter;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.common.enums.BackupFormat;

/**
 * Exports every backed-up table to a single JSON document, zipped.
 *
 * Rows are read straight over JDBC rather than through the repositories,
 * for two reasons: the archive then contains exactly what the database
 * holds (no aggregate can quietly omit a column it does not model), and
 * adding a column to a table needs no change here.
 *
 * Every value is read with {@code getString}, which is what makes the
 * round-trip lossless — see {@link DatabaseSnapshot}.
 */
@Component
public class JsonBackupExporter implements BackupExporter {

    private static final Logger log = LoggerFactory.getLogger(JsonBackupExporter.class);

    /**
     * Sole entry inside the zip. Fixed name so the importer can find it
     * without guessing.
     */
    static final String ARCHIVE_ENTRY = "backup.json";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JsonBackupExporter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public BackupFormat format() {
        return BackupFormat.JSON;
    }

    @Override
    public BackupArchive export(String fileName) {

        DatabaseSnapshot snapshot = readSnapshot();

        log.info(
                "Exported {} rows from {} tables for backup {}",
                snapshot.rowCount(), snapshot.tables().size(), fileName
        );

        return BackupArchive.of(fileName, BackupFormat.JSON, zip(snapshot));
    }

    private DatabaseSnapshot readSnapshot() {

        LinkedHashMap<String, List<Map<String, String>>> tables = new LinkedHashMap<>();

        for (String table : BackupTables.insertOrder()) {
            tables.put(table, readTable(table));
        }

        return new DatabaseSnapshot(
                DatabaseSnapshot.SCHEMA_VERSION,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                tables
        );
    }

    /**
     * The table name is interpolated rather than bound because a table
     * name cannot be a bind parameter. It is safe here: the values come
     * from {@link BackupTables}, which is a hardcoded list, never from a
     * request.
     */
    private List<Map<String, String>> readTable(String table) {

        try {
            return jdbcTemplate.query(
                    "SELECT * FROM " + table,
                    JsonBackupExporter::toRows
            );

        } catch (Exception ex) {
            throw new BackupFailedException(
                    "Could not read table " + table + " for backup: " + ex.getMessage(), ex);
        }
    }

    private static List<Map<String, String>> toRows(ResultSet resultSet) throws SQLException {

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<String> columns = new ArrayList<>(columnCount);

        for (int column = 1; column <= columnCount; column++) {
            columns.add(metaData.getColumnLabel(column));
        }

        List<Map<String, String>> rows = new ArrayList<>();

        while (resultSet.next()) {

            // LinkedHashMap: column order is stable across rows, which
            // keeps the JSON diffable between two backups.
            Map<String, String> row = new LinkedHashMap<>(columnCount);

            for (int column = 1; column <= columnCount; column++) {
                row.put(columns.get(column - 1), resultSet.getString(column));
            }

            rows.add(row);
        }

        return rows;
    }

    private byte[] zip(DatabaseSnapshot snapshot) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {

            zip.putNextEntry(new ZipEntry(ARCHIVE_ENTRY));
            zip.write(toJson(snapshot));
            zip.closeEntry();

        } catch (IOException ex) {
            // Writing to a byte array cannot fail for I/O reasons; this is
            // here because ZipOutputStream declares it.
            throw new BackupFailedException("Could not write the backup archive.", ex);
        }

        return buffer.toByteArray();
    }

    private byte[] toJson(DatabaseSnapshot snapshot) {

        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(snapshot);

        } catch (JacksonException ex) {
            // Unchecked in Jackson 3, but caught anyway: a serialization
            // failure must be reported as a failed backup, not an
            // anonymous 500.
            throw new BackupFailedException("Could not serialize the backup.", ex);
        }
    }
}
