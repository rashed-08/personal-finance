-- ============================================================================
-- V9 - Redesign Cash Reconciliation as a Two-Table Aggregate
-- ============================================================================
--
-- The V1 `cash` table was never wired to any application code (its entity,
-- mapper and repository were empty stub classes) and does not match either
-- the domain model or docs/database/tables/cash.md, which describes a
-- CashReconciliation aggregate root with a child CashSnapshot entity
-- supporting multiple physical counts before finalizing. Since nothing has
-- ever written to `cash`, it is dropped and replaced outright rather than
-- altered in place.
--
-- Reconciliation is scoped per-account (an account must be a CASH-type
-- account) rather than wallet-wide, since the app supports multiple cash
-- accounts. expected_cash_amount is always known (derived from the ledger
-- the moment reconciliation starts); actual_cash_amount and
-- difference_amount are nullable until at least one snapshot is recorded,
-- and required once a reconciliation is COMPLETED.
-- ============================================================================

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

CREATE TRIGGER trg_cash_reconciliations_updated_at
BEFORE UPDATE ON cash_reconciliations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

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

-- ----------------------------------------------------------------------------
-- Indexes
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
-- Drop the old, unused single-table design
-- ----------------------------------------------------------------------------

DROP TABLE IF EXISTS cash;
