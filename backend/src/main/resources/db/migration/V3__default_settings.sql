-- ============================================================================
-- V3 - Default Application Settings
-- ============================================================================

INSERT INTO settings (
    setting_key,
    setting_value,
    value_type,
    description,
    is_system
)
VALUES

-- -------------------------------------------------------------------------
-- General
-- -------------------------------------------------------------------------

(
    'DEFAULT_CURRENCY',
    'BDT',
    'STRING',
    'Default currency for the application.',
    TRUE
),

(
    'FIRST_DAY_OF_WEEK',
    'SATURDAY',
    'STRING',
    'First day of the week used by reports and calendars.',
    TRUE
),

(
    'DATE_FORMAT',
    'yyyy-MM-dd',
    'STRING',
    'Default application date format.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Salary Cycle
-- -------------------------------------------------------------------------

(
    'ENABLE_CARRY_FORWARD',
    'true',
    'BOOLEAN',
    'Enable automatic carry forward between salary cycles.',
    TRUE
),

(
    'AUTO_ASSIGN_SALARY_CYCLE',
    'true',
    'BOOLEAN',
    'Automatically assign new transactions to a salary cycle.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Cash Reconciliation
-- -------------------------------------------------------------------------

(
    'ENABLE_CASH_RECONCILIATION',
    'true',
    'BOOLEAN',
    'Enable cash reconciliation workflow.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Backup
-- -------------------------------------------------------------------------

(
    'AUTO_BACKUP_ENABLED',
    'false',
    'BOOLEAN',
    'Enable scheduled automatic backups.',
    TRUE
),

(
    'BACKUP_PROVIDER',
    'LOCAL',
    'STRING',
    'Default backup provider.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Recurring Transactions
-- -------------------------------------------------------------------------

(
    'ENABLE_RECURRING_TRANSACTIONS',
    'true',
    'BOOLEAN',
    'Enable recurring transaction scheduler.',
    TRUE
)

ON CONFLICT (setting_key)
DO NOTHING;