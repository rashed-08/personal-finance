package io.rashed.finance.api.dto.migration;

import io.rashed.finance.application.migration.GoogleKeepMigrationResult;

public final class MigrationDtoMapper {

    private MigrationDtoMapper() {
    }

    public static GoogleKeepMigrationResponse toResponse(GoogleKeepMigrationResult result) {

        return new GoogleKeepMigrationResponse(
                result.importedCount(),
                result.skippedCount(),
                result.warnings(),
                result.errors(),
                result.durationMillis()
        );
    }
}
