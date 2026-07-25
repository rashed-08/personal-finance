-- ============================================================================
-- V2 - Seed Data
-- Default categories, application settings, and starter funds.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Income Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system
)
VALUES
    ('Salary', 'INCOME', TRUE),
    ('Bonus', 'INCOME', TRUE),
    ('Business', 'INCOME', TRUE),
    ('Investment', 'INCOME', TRUE),
    ('Gift', 'INCOME', TRUE),
    ('Cashback', 'INCOME', TRUE),
    ('Refund', 'INCOME', TRUE),
    ('Other Income', 'INCOME', TRUE)
ON CONFLICT (name, category_type)
DO NOTHING;

-- ----------------------------------------------------------------------------
-- Expense Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system
)
VALUES
    ('Food', 'EXPENSE', TRUE),
    ('Groceries', 'EXPENSE', TRUE),
    ('Transport', 'EXPENSE', TRUE),
    ('Utilities', 'EXPENSE', TRUE),
    ('Rent', 'EXPENSE', TRUE),
    ('Medical', 'EXPENSE', TRUE),
    ('Education', 'EXPENSE', TRUE),
    ('Shopping', 'EXPENSE', TRUE),
    ('Entertainment', 'EXPENSE', TRUE),
    ('Travel', 'EXPENSE', TRUE),
    ('Family', 'EXPENSE', TRUE),
    ('Donation', 'EXPENSE', TRUE),
    ('Afia', 'EXPENSE', TRUE),
    ('Other Expense', 'EXPENSE', TRUE)
ON CONFLICT (name, category_type)
DO NOTHING;

-- ----------------------------------------------------------------------------
-- Default Application Settings
-- ----------------------------------------------------------------------------

INSERT INTO settings (
    setting_key,
    setting_value,
    value_type,
    description,
    is_system
)
VALUES

-- General

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

-- Salary Cycle

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

-- Cash Reconciliation

(
    'ENABLE_CASH_RECONCILIATION',
    'true',
    'BOOLEAN',
    'Enable cash reconciliation workflow.',
    TRUE
),

-- Backup

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

-- Recurring Transactions

(
    'ENABLE_RECURRING_TRANSACTIONS',
    'true',
    'BOOLEAN',
    'Enable recurring transaction scheduler.',
    TRUE
)

ON CONFLICT (setting_key)
DO NOTHING;

-- ----------------------------------------------------------------------------
-- Default Funds
-- ----------------------------------------------------------------------------

INSERT INTO funds (
    name,
    fund_type,
    description,
    is_active
)
VALUES

(
    'Emergency Fund',
    'EMERGENCY',
    'Reserved for unexpected financial emergencies.',
    TRUE
),

(
    'General Savings',
    'SAVINGS',
    'General-purpose savings for future use.',
    TRUE
),

(
    'Zakat',
    'ZAKAT',
    'Reserved for annual zakat obligations.',
    TRUE
),

(
    'Investment',
    'INVESTMENT',
    'Money allocated for long-term investments.',
    TRUE
)

ON CONFLICT (name)
DO NOTHING;
