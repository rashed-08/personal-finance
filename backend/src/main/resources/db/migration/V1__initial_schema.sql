-- ============================================================================
-- Part 1
-- Extensions
-- Schemas
-- Functions
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PostgreSQL Extensions
-- ----------------------------------------------------------------------------

-- UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ----------------------------------------------------------------------------
-- Schema
-- ----------------------------------------------------------------------------

SET search_path TO public;

-- ----------------------------------------------------------------------------
-- Utility Functions
-- ----------------------------------------------------------------------------

-- Automatically update the updated_at column whenever a row changes.

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

-- ----------------------------------------------------------------------------
-- Future Functions (Reserved)
-- ----------------------------------------------------------------------------

-- The following functions may be introduced in future versions:
--
-- - calculate_account_balance()
-- - calculate_fund_balance()
-- - calculate_loan_balance()
-- - resolve_salary_cycle()
-- - generate_recurring_transactions()
--
-- Version 1 intentionally keeps business logic inside the application layer.
--
-- The database is responsible only for:
--   • Data storage
--   • Data integrity
--   • Referential integrity
--   • Trigger-based timestamp updates

-- ============================================================================
-- Part 2
-- Core Tables
-- accounts
-- categories
-- salary_cycles
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: accounts
-- ----------------------------------------------------------------------------

CREATE TABLE accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name                VARCHAR(100) NOT NULL,
    account_type        VARCHAR(30) NOT NULL,

    opening_balance     NUMERIC(18,2) NOT NULL DEFAULT 0.00,

    currency_code       CHAR(3) NOT NULL DEFAULT 'BDT',

    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    description         TEXT,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_accounts_opening_balance
        CHECK (opening_balance >= 0),

    CONSTRAINT chk_accounts_account_type
        CHECK (
            account_type IN (
                'CASH',
                'BANK',
                'MOBILE_BANKING',
                'CREDIT_CARD',
                'SAVINGS',
                'INVESTMENT',
                'E_WALLET'
            )
        )
);

CREATE TRIGGER trg_accounts_updated_at
BEFORE UPDATE ON accounts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Table: categories
-- ----------------------------------------------------------------------------

CREATE TABLE categories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name                VARCHAR(100) NOT NULL,

    description         VARCHAR(500),

    category_type       VARCHAR(20) NOT NULL,

    is_system           BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_categories_name_type
        UNIQUE (name, category_type),

    CONSTRAINT chk_categories_type
        CHECK (
            category_type IN (
                'INCOME',
                'EXPENSE'
            )
        )
);

CREATE TRIGGER trg_categories_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Table: salary_cycles
-- ----------------------------------------------------------------------------

CREATE TABLE salary_cycles (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    cycle_name              VARCHAR(100) NOT NULL,

    cycle_start_date        DATE NOT NULL,
    cycle_end_date          DATE NOT NULL,

    salary_received_date    DATE,

    carry_forward_amount    NUMERIC(18,2) NOT NULL DEFAULT 0.00,

    is_closed               BOOLEAN NOT NULL DEFAULT FALSE,

    notes                   TEXT,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_salary_cycle_dates
        CHECK (cycle_start_date <= cycle_end_date),

    CONSTRAINT chk_salary_cycle_carry_forward
        CHECK (carry_forward_amount >= 0)
);

CREATE TRIGGER trg_salary_cycles_updated_at
BEFORE UPDATE ON salary_cycles
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Part 3
-- Transactions
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: transactions
-- ----------------------------------------------------------------------------

CREATE TABLE transactions (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    transaction_type            VARCHAR(30) NOT NULL,

    transaction_status          VARCHAR(20) NOT NULL DEFAULT 'POSTED',

    transaction_date            DATE NOT NULL,

    amount                      NUMERIC(18,2) NOT NULL,

    description                 VARCHAR(255),

    notes                       TEXT,

    from_account_id             UUID,

    to_account_id               UUID,

    category_id                 UUID,

    salary_cycle_id             UUID NOT NULL,

    reference_number            VARCHAR(100),

    migration_batch_id          UUID,

    reconciliation_batch_id     UUID,

    adjustment_reason           VARCHAR(50),

    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_transactions_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_transactions_type
        CHECK (
            transaction_type IN (
                'INCOME',
                'EXPENSE',
                'TRANSFER',
                'ADJUSTMENT',
                'OPENING_BALANCE',
                'MIGRATION'
            )
        ),

    CONSTRAINT chk_transactions_status
        CHECK (
            transaction_status IN (
                'POSTED',
                'VOID',
                'REVERSED'
            )
        ),

    CONSTRAINT chk_transactions_adjustment_reason
        CHECK (
            adjustment_reason IS NULL
            OR adjustment_reason IN (
                'CASH_RECONCILIATION',
                'OPENING_BALANCE',
                'DATA_MIGRATION',
                'MANUAL_CORRECTION',
                'SYSTEM_CORRECTION'
            )
        ),

    CONSTRAINT chk_transactions_accounts_not_same
        CHECK (
            from_account_id IS NULL
            OR to_account_id IS NULL
            OR from_account_id <> to_account_id
        )
);

-- ============================================================================
-- Part 3.2
-- Trigger
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Trigger: transactions.updated_at
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_transactions_updated_at
BEFORE UPDATE ON transactions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


-- ============================================================================
-- Part 3.3
-- Foreign Keys
-- ============================================================================

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_salary_cycle
        FOREIGN KEY (salary_cycle_id)
        REFERENCES salary_cycles(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

-- ============================================================================
-- Part 4.1
-- Table: funds
-- ============================================================================

CREATE TABLE funds (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name                    VARCHAR(100) NOT NULL,

    fund_type               VARCHAR(30) NOT NULL,

    target_amount           NUMERIC(18,2),

    target_date             DATE,

    description             TEXT,

    is_active               BOOLEAN NOT NULL DEFAULT TRUE,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_funds_target_amount
        CHECK (
            target_amount IS NULL
            OR target_amount >= 0
        ),

    CONSTRAINT chk_funds_type
        CHECK (
            fund_type IN (
                'EMERGENCY',
                'SAVINGS',
                'GOAL',
                'ZAKAT',
                'INVESTMENT',
                'CUSTOM'
            )
        ),

    CONSTRAINT uk_funds_name
        UNIQUE (name)
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_funds_updated_at
BEFORE UPDATE ON funds
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Part 4.2
-- Table: loans
-- ============================================================================

CREATE TABLE loans (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    person_name             VARCHAR(150) NOT NULL,

    loan_type               VARCHAR(20) NOT NULL,

    original_amount         NUMERIC(18,2) NOT NULL,

    loan_date               DATE NOT NULL,

    expected_return_date    DATE,

    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    description             TEXT,

    is_active               BOOLEAN NOT NULL DEFAULT TRUE,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_loans_original_amount
        CHECK (original_amount > 0),

    CONSTRAINT chk_loans_type
        CHECK (
            loan_type IN (
                'GIVEN',
                'RECEIVED'
            )
        ),

    CONSTRAINT chk_loans_status
        CHECK (
            status IN (
                'ACTIVE',
                'COMPLETED',
                'CANCELLED',
                'DEFAULTED'
            )
        ),

    CONSTRAINT chk_loans_expected_return_date
        CHECK (
            expected_return_date IS NULL
            OR expected_return_date >= loan_date
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_loans_updated_at
BEFORE UPDATE ON loans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Part 4.3
-- Table: cash
-- ============================================================================

CREATE TABLE cash (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reconciliation_date         DATE NOT NULL,

    expected_cash_amount        NUMERIC(18,2) NOT NULL,

    actual_cash_amount          NUMERIC(18,2) NOT NULL,

    difference_amount           NUMERIC(18,2) NOT NULL,

    adjustment_transaction_id   UUID,

    notes                       TEXT,

    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_cash_expected_amount
        CHECK (expected_cash_amount >= 0),

    CONSTRAINT chk_cash_actual_amount
        CHECK (actual_cash_amount >= 0)
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_cash_updated_at
BEFORE UPDATE ON cash
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Foreign Keys
-- ----------------------------------------------------------------------------

ALTER TABLE cash
    ADD CONSTRAINT fk_cash_adjustment_transaction
        FOREIGN KEY (adjustment_transaction_id)
        REFERENCES transactions(id)
        ON UPDATE RESTRICT
        ON DELETE SET NULL;

-- ============================================================================
-- Part 4.4
-- Table: recurring_transactions
-- ============================================================================

CREATE TABLE recurring_transactions (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name                        VARCHAR(100) NOT NULL,

    transaction_type            VARCHAR(30) NOT NULL,

    amount                      NUMERIC(18,2) NOT NULL,

    description                 VARCHAR(255),

    notes                       TEXT,

    from_account_id             UUID,

    to_account_id               UUID,

    category_id                 UUID,

    salary_cycle_enabled        BOOLEAN NOT NULL DEFAULT TRUE,

    frequency                   VARCHAR(20) NOT NULL,

    start_date                  DATE NOT NULL,

    end_date                    DATE,

    next_execution_date         DATE NOT NULL,

    last_execution_date         DATE,

    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,

    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_recurring_amount
        CHECK (amount > 0),

    CONSTRAINT chk_recurring_transaction_type
        CHECK (
            transaction_type IN (
                'INCOME',
                'EXPENSE',
                'TRANSFER'
            )
        ),

    CONSTRAINT chk_recurring_frequency
        CHECK (
            frequency IN (
                'DAILY',
                'WEEKLY',
                'MONTHLY',
                'YEARLY'
            )
        ),

    CONSTRAINT chk_recurring_date_range
        CHECK (
            end_date IS NULL
            OR end_date >= start_date
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_recurring_transactions_updated_at
BEFORE UPDATE ON recurring_transactions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Foreign Keys
-- ----------------------------------------------------------------------------

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

-- ============================================================================
-- Part 4.5
-- Table: settings
-- ============================================================================

CREATE TABLE settings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    setting_key         VARCHAR(100) NOT NULL,

    setting_value       TEXT,

    value_type          VARCHAR(20) NOT NULL DEFAULT 'STRING',

    description         TEXT,

    is_system           BOOLEAN NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- Constraints
    ------------------------------------------------------------------------

    CONSTRAINT uk_settings_key
        UNIQUE (setting_key),

    CONSTRAINT chk_settings_value_type
        CHECK (
            value_type IN (
                'STRING',
                'INTEGER',
                'DECIMAL',
                'BOOLEAN',
                'DATE',
                'JSON'
            )
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_settings_updated_at
BEFORE UPDATE ON settings
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Part 4.6
-- Table: backup_history
-- ============================================================================

CREATE TABLE backup_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    backup_type         VARCHAR(20) NOT NULL,

    provider            VARCHAR(30) NOT NULL,

    file_name           VARCHAR(255) NOT NULL,

    file_path           TEXT,

    file_size           BIGINT,

    backup_status       VARCHAR(20) NOT NULL,

    backup_started_at   TIMESTAMP NOT NULL,

    backup_completed_at TIMESTAMP,

    checksum            VARCHAR(128),

    error_message       TEXT,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_backup_history_type
        CHECK (
            backup_type IN (
                'MANUAL',
                'AUTOMATIC'
            )
        ),

    CONSTRAINT chk_backup_history_provider
        CHECK (
            provider IN (
                'LOCAL',
                'GOOGLE_DRIVE',
                'S3'
            )
        ),

    CONSTRAINT chk_backup_history_status
        CHECK (
            backup_status IN (
                'IN_PROGRESS',
                'COMPLETED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_backup_history_file_size
        CHECK (
            file_size IS NULL
            OR file_size >= 0
        ),

    CONSTRAINT chk_backup_history_completed_at
        CHECK (
            backup_completed_at IS NULL
            OR backup_completed_at >= backup_started_at
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_backup_history_updated_at
BEFORE UPDATE ON backup_history
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Part 5
-- Additional Constraints
-- ============================================================================

-- Version 1 defines all CHECK, UNIQUE and FOREIGN KEY constraints
-- together with their respective table definitions to improve readability
-- and keep each table self-contained.
--
-- This section is intentionally reserved for future cross-table,
-- exclusion, deferred or advanced constraints.
--
-- Examples:
--
-- • DEFERRABLE constraints
-- • EXCLUDE constraints
-- • Multi-table integrity constraints
-- • Custom business constraints implemented in SQL

-- ============================================================================
-- Part 6
-- Indexes
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Accounts
-- ----------------------------------------------------------------------------

CREATE INDEX idx_accounts_account_type
    ON accounts(account_type);

CREATE INDEX idx_accounts_is_active
    ON accounts(is_active);

-- ----------------------------------------------------------------------------
-- Categories
-- ----------------------------------------------------------------------------

CREATE INDEX idx_categories_type
    ON categories(category_type);

CREATE INDEX idx_categories_active
    ON categories(is_active);

-- ----------------------------------------------------------------------------
-- Salary Cycles
-- ----------------------------------------------------------------------------

CREATE INDEX idx_salary_cycles_start_date
    ON salary_cycles(cycle_start_date);

CREATE INDEX idx_salary_cycles_end_date
    ON salary_cycles(cycle_end_date);

CREATE INDEX idx_salary_cycles_closed
    ON salary_cycles(is_closed);

-- ----------------------------------------------------------------------------
-- Transactions
-- ----------------------------------------------------------------------------

CREATE INDEX idx_transactions_date
    ON transactions(transaction_date);

CREATE INDEX idx_transactions_type
    ON transactions(transaction_type);

CREATE INDEX idx_transactions_status
    ON transactions(transaction_status);

CREATE INDEX idx_transactions_from_account
    ON transactions(from_account_id);

CREATE INDEX idx_transactions_to_account
    ON transactions(to_account_id);

CREATE INDEX idx_transactions_category
    ON transactions(category_id);

CREATE INDEX idx_transactions_salary_cycle
    ON transactions(salary_cycle_id);

CREATE INDEX idx_transactions_reference
    ON transactions(reference_number);

CREATE INDEX idx_transactions_reconciliation
    ON transactions(reconciliation_batch_id);

CREATE INDEX idx_transactions_migration
    ON transactions(migration_batch_id);

CREATE INDEX idx_transactions_adjustment_reason
    ON transactions(adjustment_reason);

-- Composite indexes

CREATE INDEX idx_transactions_cycle_date
    ON transactions (
        salary_cycle_id,
        transaction_date
    );

CREATE INDEX idx_transactions_account_date
    ON transactions (
        from_account_id,
        transaction_date
    );

CREATE INDEX idx_transactions_category_date
    ON transactions (
        category_id,
        transaction_date
    );

CREATE INDEX idx_transactions_type_date
    ON transactions (
        transaction_type,
        transaction_date
    );

-- ----------------------------------------------------------------------------
-- Funds
-- ----------------------------------------------------------------------------

CREATE INDEX idx_funds_type
    ON funds(fund_type);

CREATE INDEX idx_funds_active
    ON funds(is_active);

-- ----------------------------------------------------------------------------
-- Loans
-- ----------------------------------------------------------------------------

CREATE INDEX idx_loans_type
    ON loans(loan_type);

CREATE INDEX idx_loans_status
    ON loans(status);

CREATE INDEX idx_loans_person
    ON loans(person_name);

-- ----------------------------------------------------------------------------
-- Cash
-- ----------------------------------------------------------------------------

CREATE INDEX idx_cash_reconciliation_date
    ON cash(reconciliation_date);

CREATE INDEX idx_cash_adjustment_transaction
    ON cash(adjustment_transaction_id);

-- ----------------------------------------------------------------------------
-- Recurring Transactions
-- ----------------------------------------------------------------------------

CREATE INDEX idx_recurring_next_execution
    ON recurring_transactions(next_execution_date);

CREATE INDEX idx_recurring_frequency
    ON recurring_transactions(frequency);

CREATE INDEX idx_recurring_active
    ON recurring_transactions(is_active);

-- ----------------------------------------------------------------------------
-- Settings
-- ----------------------------------------------------------------------------

CREATE INDEX idx_settings_system
    ON settings(is_system);

-- ----------------------------------------------------------------------------
-- Backup History
-- ----------------------------------------------------------------------------

CREATE INDEX idx_backup_started_at
    ON backup_history(backup_started_at);

CREATE INDEX idx_backup_status
    ON backup_history(backup_status);

CREATE INDEX idx_backup_provider
    ON backup_history(provider);

