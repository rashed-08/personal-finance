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
--
-- cycle_end_date is nullable: a cycle stays open until the next salary
-- payment closes it (docs/business/SalaryWorkflow.md). The existing
-- chk_salary_cycle_dates CHECK (start <= end) already passes when end is
-- NULL, so no separate constraint is needed for the open-ended case.

CREATE TABLE salary_cycles (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    cycle_name              VARCHAR(100) NOT NULL,

    cycle_start_date        DATE NOT NULL,
    cycle_end_date          DATE,

    salary_received_date    DATE,

    carry_forward_amount    NUMERIC(18,2) NOT NULL DEFAULT 0.00,

    is_closed               BOOLEAN NOT NULL DEFAULT FALSE,

    notes                   TEXT,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_salary_cycle_dates
        CHECK (cycle_end_date IS NULL OR cycle_start_date <= cycle_end_date),

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
--
-- salary_cycle_id is nullable: ADJUSTMENT, OPENING_BALANCE and MIGRATION
-- transactions correct, seed or import balances rather than track
-- day-to-day spending, so they are not part of a salary cycle. It remains
-- required for INCOME, EXPENSE and TRANSFER via chk_transactions_salary_cycle_required.
--
-- migration_batch_id / reconciliation_batch_id are free-form business
-- identifiers (e.g. "google-keep-2026-07"), not UUIDs — hence VARCHAR(100).
--
-- fund_id links a TRANSFER to a fund allocation/withdrawal instead of an
-- ordinary account-to-account transfer (see docs/database/tables/ funds.md
-- and docs/api/Fund.md):
--   Allocation: from_account_id set, to_account_id null, fund_id set.
--   Withdrawal: to_account_id set, from_account_id null, fund_id set.
-- Ordinary transfers are unaffected (fund_id stays null, both accounts set).
--
-- loan_id links a TRANSFER to a loan disbursement/receipt or repayment
-- instead of an ordinary transfer (see docs/database/tables/loans.md and
-- docs/api/Loan.md), same one-real-account-plus-the-loan shape as fund_id.
-- A transaction is never linked to both a fund and a loan at once
-- (chk_transactions_single_link).

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

    salary_cycle_id             UUID,

    reference_number            VARCHAR(100),

    migration_batch_id          VARCHAR(100),

    reconciliation_batch_id     VARCHAR(100),

    adjustment_reason           VARCHAR(50),

    reference_transaction_id    UUID,

    fund_id                     UUID,

    loan_id                     UUID,

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
                'SYSTEM_CORRECTION',
                'TRANSACTION_UPDATE'
            )
        ),

    CONSTRAINT chk_transactions_accounts_not_same
        CHECK (
            from_account_id IS NULL
            OR to_account_id IS NULL
            OR from_account_id <> to_account_id
        ),

    CONSTRAINT chk_transactions_salary_cycle_required
        CHECK (
            transaction_type IN ('ADJUSTMENT', 'OPENING_BALANCE', 'MIGRATION')
            OR salary_cycle_id IS NOT NULL
        ),

    CONSTRAINT chk_transactions_fund_transfer
        CHECK (
            fund_id IS NULL
            OR (
                transaction_type = 'TRANSFER'
                AND (from_account_id IS NULL) != (to_account_id IS NULL)
            )
        ),

    CONSTRAINT chk_transactions_loan_transfer
        CHECK (
            loan_id IS NULL
            OR (
                transaction_type = 'TRANSFER'
                AND (from_account_id IS NULL) != (to_account_id IS NULL)
            )
        ),

    CONSTRAINT chk_transactions_single_link
        CHECK (fund_id IS NULL OR loan_id IS NULL)
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

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_reference_transaction
        FOREIGN KEY (reference_transaction_id)
        REFERENCES transactions(id)
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
            OR target_amount > 0
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

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_fund
        FOREIGN KEY (fund_id)
        REFERENCES funds(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

-- ============================================================================
-- Part 4.2
-- Table: loans
-- ============================================================================
--
-- A loan contains metadata and current status only; principal_amount is the
-- one exception to "balances are never stored" (docs/database/tables/loans.md
-- explicitly stores the original amount). Outstanding balance is derived:
-- principal_amount - total repayments, from loan_id-linked transactions.
-- There is no separate is_active flag — loan_status alone is the state
-- machine (ACTIVE / CLOSED / CANCELLED).

CREATE TABLE loans (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    person_name             VARCHAR(150) NOT NULL,

    loan_type               VARCHAR(20) NOT NULL,

    principal_amount        NUMERIC(18,2) NOT NULL,

    start_date              DATE NOT NULL,

    expected_settlement_date DATE,

    loan_status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    description             TEXT,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_loans_principal_amount
        CHECK (principal_amount > 0),

    CONSTRAINT chk_loans_type
        CHECK (
            loan_type IN (
                'RECEIVABLE',
                'PAYABLE'
            )
        ),

    CONSTRAINT chk_loans_status
        CHECK (
            loan_status IN (
                'ACTIVE',
                'CLOSED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_loans_expected_settlement_date
        CHECK (
            expected_settlement_date IS NULL
            OR expected_settlement_date >= start_date
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_loans_updated_at
BEFORE UPDATE ON loans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

-- ============================================================================
-- Part 4.3
-- Tables: cash_reconciliations, cash_snapshots
-- ============================================================================
--
-- Reconciliation is scoped per-account (an account must be a CASH-type
-- account) rather than wallet-wide, since the app supports multiple cash
-- accounts. expected_cash_amount is always known (derived from the ledger
-- the moment reconciliation starts); actual_cash_amount and
-- difference_amount are nullable until at least one snapshot is recorded,
-- and required once a reconciliation is COMPLETED.

CREATE TABLE cash_reconciliations (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    account_id                  UUID NOT NULL,

    reconciliation_date         DATE NOT NULL,

    expected_cash_amount        NUMERIC(18,2) NOT NULL,

    actual_cash_amount          NUMERIC(18,2),

    difference_amount           NUMERIC(18,2),

    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    adjustment_transaction_id   UUID,

    notes                       TEXT,

    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ------------------------------------------------------------------------
    -- CHECK Constraints
    ------------------------------------------------------------------------

    CONSTRAINT chk_cash_reconciliations_expected
        CHECK (expected_cash_amount >= 0),

    CONSTRAINT chk_cash_reconciliations_actual
        CHECK (actual_cash_amount IS NULL OR actual_cash_amount >= 0),

    CONSTRAINT chk_cash_reconciliations_status
        CHECK (status IN ('PENDING', 'COMPLETED')),

    CONSTRAINT chk_cash_reconciliations_completed_has_actual
        CHECK (
            status = 'PENDING'
            OR (actual_cash_amount IS NOT NULL AND difference_amount IS NOT NULL)
        )
);

-- ----------------------------------------------------------------------------
-- Trigger
-- ----------------------------------------------------------------------------

CREATE TRIGGER trg_cash_reconciliations_updated_at
BEFORE UPDATE ON cash_reconciliations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Foreign Keys
-- ----------------------------------------------------------------------------

ALTER TABLE cash_reconciliations
    ADD CONSTRAINT fk_cash_reconciliations_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE cash_reconciliations
    ADD CONSTRAINT fk_cash_reconciliations_adjustment_transaction
        FOREIGN KEY (adjustment_transaction_id)
        REFERENCES transactions(id)
        ON UPDATE RESTRICT
        ON DELETE SET NULL;

-- ----------------------------------------------------------------------------
-- Table: cash_snapshots (child of cash_reconciliations)
-- ----------------------------------------------------------------------------

CREATE TABLE cash_snapshots (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reconciliation_id   UUID NOT NULL,

    snapshot_time       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    cash_amount         NUMERIC(18,2) NOT NULL,

    notes               VARCHAR(500),

    CONSTRAINT chk_cash_snapshots_amount
        CHECK (cash_amount >= 0)
);

ALTER TABLE cash_snapshots
    ADD CONSTRAINT fk_cash_snapshots_reconciliation
        FOREIGN KEY (reconciliation_id)
        REFERENCES cash_reconciliations(id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE;

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

CREATE INDEX idx_transactions_reference_transaction
    ON transactions(reference_transaction_id);

CREATE INDEX idx_transactions_fund
    ON transactions(fund_id);

CREATE INDEX idx_transactions_loan
    ON transactions(loan_id);

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

CREATE INDEX idx_transactions_fund_date
    ON transactions (
        fund_id,
        transaction_date
    );

CREATE INDEX idx_transactions_loan_date
    ON transactions (
        loan_id,
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
    ON loans(loan_status);

CREATE INDEX idx_loans_person
    ON loans(person_name);

-- ----------------------------------------------------------------------------
-- Cash Reconciliation
-- ----------------------------------------------------------------------------

CREATE INDEX idx_cash_reconciliations_account
    ON cash_reconciliations(account_id);

CREATE INDEX idx_cash_reconciliations_date
    ON cash_reconciliations(reconciliation_date);

CREATE INDEX idx_cash_reconciliations_status
    ON cash_reconciliations(status);

CREATE INDEX idx_cash_snapshots_reconciliation
    ON cash_snapshots(reconciliation_id);

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
