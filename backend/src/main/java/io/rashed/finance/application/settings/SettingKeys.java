package io.rashed.finance.application.settings;

/**
 * Every setting key the application reads, with its default.
 *
 * The keys are seeded by migration (V2 and V5), but the defaults are
 * repeated here on purpose: a setting that is missing or blank must not
 * stop a module working, so every read goes through a default owned by
 * the code rather than by the database.
 *
 * Keys are append-only. Renaming one silently reverts that setting to its
 * default, so a rename is a migration plus a data copy.
 */
public final class SettingKeys {

    private SettingKeys() {
    }

    // -------------------------------------------------------------------------
    // General
    // -------------------------------------------------------------------------

    public static final String DEFAULT_CURRENCY = "DEFAULT_CURRENCY";
    public static final String DEFAULT_CURRENCY_FALLBACK = "BDT";

    public static final String FIRST_DAY_OF_WEEK = "FIRST_DAY_OF_WEEK";
    public static final String FIRST_DAY_OF_WEEK_FALLBACK = "SATURDAY";

    public static final String DATE_FORMAT = "DATE_FORMAT";
    public static final String DATE_FORMAT_FALLBACK = "yyyy-MM-dd";

    // -------------------------------------------------------------------------
    // Salary Cycle
    // -------------------------------------------------------------------------

    public static final String ENABLE_CARRY_FORWARD = "ENABLE_CARRY_FORWARD";

    public static final String AUTO_ASSIGN_SALARY_CYCLE = "AUTO_ASSIGN_SALARY_CYCLE";

    // -------------------------------------------------------------------------
    // Cash Reconciliation
    // -------------------------------------------------------------------------

    public static final String ENABLE_CASH_RECONCILIATION = "ENABLE_CASH_RECONCILIATION";

    // -------------------------------------------------------------------------
    // Recurring Transactions
    // -------------------------------------------------------------------------

    public static final String ENABLE_RECURRING_TRANSACTIONS = "ENABLE_RECURRING_TRANSACTIONS";

    // -------------------------------------------------------------------------
    // Backup
    // -------------------------------------------------------------------------

    public static final String AUTO_BACKUP_ENABLED = "AUTO_BACKUP_ENABLED";

    public static final String AUTO_BACKUP_CRON = "AUTO_BACKUP_CRON";
    public static final String AUTO_BACKUP_CRON_FALLBACK = "0 0 2 * * *";

    public static final String BACKUP_PROVIDER = "BACKUP_PROVIDER";

    public static final String BACKUP_FORMAT = "BACKUP_FORMAT";

    public static final String BACKUP_DIRECTORY = "BACKUP_DIRECTORY";
    public static final String BACKUP_DIRECTORY_FALLBACK = "storage/backups";

    public static final String BACKUP_RETENTION_COUNT = "BACKUP_RETENTION_COUNT";
    public static final int BACKUP_RETENTION_COUNT_FALLBACK = 30;

    public static final String GOOGLE_DRIVE_FOLDER_NAME = "GOOGLE_DRIVE_FOLDER_NAME";
    public static final String GOOGLE_DRIVE_FOLDER_NAME_FALLBACK = "PersonalFinanceApp";
}
