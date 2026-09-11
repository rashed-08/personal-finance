package io.rashed.finance.infrastructure.backup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * On-disk shape of a JSON backup.
 *
 * Every value is a string (or null), whatever its SQL type. Postgres
 * renders each type to text losslessly and casts it back on the way in
 * ({@code ?::numeric}, {@code ?::uuid}, …), so the archive needs no type
 * map of its own and gains no rounding of its own — which matters when
 * the payload is a money ledger.
 *
 * @param schemaVersion {@link #SCHEMA_VERSION} at the time of writing, so
 *                      a future importer can refuse or migrate an archive
 *                      it does not understand.
 * @param exportedAt    ISO-8601 timestamp, for display.
 * @param tables        table name to rows, in {@link BackupTables}
 *                      insert order. A {@link LinkedHashMap} because that
 *                      order is what makes the archive restorable.
 */
public record DatabaseSnapshot(
        int schemaVersion,
        String exportedAt,
        LinkedHashMap<String, List<Map<String, String>>> tables
) {

    /**
     * Bumped only when the archive layout changes in a way an older
     * importer would misread. Adding a table does not require a bump:
     * an older importer simply ignores tables it does not know.
     */
    public static final int SCHEMA_VERSION = 1;

    public int rowCount() {

        return tables.values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }
}
