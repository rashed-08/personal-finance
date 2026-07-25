package io.rashed.finance.api.dto.migration;

import java.util.List;

public record GoogleKeepMigrationResponse(

        int importedCount,

        int skippedCount,

        List<String> warnings,

        List<String> errors,

        long durationMillis

) {
}
