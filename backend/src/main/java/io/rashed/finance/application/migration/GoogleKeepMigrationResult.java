package io.rashed.finance.application.migration;

import java.util.List;

/** Per docs/business/GoogleKeepMigration.md's "Import Result" section — no persistent import log in v1, this is returned directly from the API call. */
public record GoogleKeepMigrationResult(

        int importedCount,

        int skippedCount,

        List<String> warnings,

        List<String> errors,

        long durationMillis

) {
}
