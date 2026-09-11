package io.rashed.finance.infrastructure.backup;

import java.util.List;

/**
 * Which tables a backup covers, and in what order.
 *
 * <h2>Order</h2>
 * Parents before children, so a restore can insert straight into a table
 * without deferring foreign keys. A restore deletes in the reverse of
 * this order.
 *
 * <h2>What is deliberately excluded</h2>
 * <ul>
 *   <li>{@code users}, {@code refresh_tokens},
 *       {@code email_verification_tokens}, {@code password_reset_tokens},
 *       {@code google_oauth_tokens} — credentials and session state. No
 *       financial table references a user, so none of this is needed for
 *       referential integrity, and an archive that travels to Google
 *       Drive has no business carrying password hashes or live tokens.
 *       A restore therefore leaves the current login working.</li>
 *   <li>{@code backup_history} — the audit trail must survive the restore
 *       that is writing to it. Including it would roll history back to
 *       whatever it was when the archive was taken and erase the record
 *       of the restore in progress.</li>
 *   <li>{@code flyway_schema_history} — owned by Flyway. Restoring it
 *       would desynchronise the schema version from the actual schema.</li>
 * </ul>
 *
 * <h2>Schema compatibility</h2>
 * An archive stores the column names it captured, and the importer
 * inserts only columns that still exist. A column added after the archive
 * was taken therefore falls back to its database default, and a column
 * that has since been dropped is skipped. A renamed or retyped column is
 * not something this can paper over — that needs a migration-aware
 * importer, and {@link DatabaseSnapshot#SCHEMA_VERSION} exists so such a
 * change can be detected rather than silently mis-restored.
 */
public final class BackupTables {

    private BackupTables() {
    }

    /**
     * Insert order: every table appears after the tables it references.
     */
    private static final List<String> ORDERED_TABLES = List.of(
            "accounts",
            "categories",
            "salary_cycles",
            "funds",
            "loans",
            "transactions",
            "cash_reconciliations",
            "cash_snapshots",
            "recurring_transactions",
            "recurring_transaction_executions",
            "settings"
    );

    /**
     * Columns that reference a row in the same table and so cannot be set
     * during the initial insert: the referenced row may not exist yet.
     * The importer nulls them on insert and fills them in afterwards.
     */
    private static final List<SelfReference> SELF_REFERENCES = List.of(
            new SelfReference("transactions", "reference_transaction_id")
    );

    public static List<String> insertOrder() {
        return ORDERED_TABLES;
    }

    public static List<String> deleteOrder() {
        return ORDERED_TABLES.reversed();
    }

    /**
     * Self-referencing columns on the given table, or an empty list.
     */
    public static List<String> selfReferencingColumns(String table) {

        return SELF_REFERENCES.stream()
                .filter(reference -> reference.table().equals(table))
                .map(SelfReference::column)
                .toList();
    }

    private record SelfReference(String table, String column) {
    }
}
