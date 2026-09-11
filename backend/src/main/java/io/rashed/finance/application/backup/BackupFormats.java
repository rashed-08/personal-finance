package io.rashed.finance.application.backup;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import io.rashed.finance.common.enums.BackupFormat;

/**
 * Resolves a {@link BackupFormat} to the exporter and importer that
 * implement it.
 *
 * Spring injects every implementation it finds, so adding a format is a
 * matter of adding a bean — nothing here changes.
 */
@Component
public class BackupFormats {

    private final Map<BackupFormat, BackupExporter> exporters = new EnumMap<>(BackupFormat.class);
    private final Map<BackupFormat, BackupImporter> importers = new EnumMap<>(BackupFormat.class);

    public BackupFormats(List<BackupExporter> exporters, List<BackupImporter> importers) {

        for (BackupExporter exporter : exporters) {
            this.exporters.put(exporter.format(), exporter);
        }

        for (BackupImporter importer : importers) {
            this.importers.put(importer.format(), importer);
        }
    }

    public BackupExporter exporter(BackupFormat format) {

        Objects.requireNonNull(format, "Backup format cannot be null.");

        BackupExporter exporter = exporters.get(format);

        if (exporter == null) {
            throw new BackupFailedException("No exporter is registered for format " + format + ".");
        }

        return exporter;
    }

    public BackupImporter importer(BackupFormat format) {

        Objects.requireNonNull(format, "Backup format cannot be null.");

        BackupImporter importer = importers.get(format);

        if (importer == null) {
            throw new BackupFailedException("No importer is registered for format " + format + ".");
        }

        return importer;
    }
}
