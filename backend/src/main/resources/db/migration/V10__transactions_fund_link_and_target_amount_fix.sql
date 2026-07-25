-- ============================================================================
-- V10 - Link Transactions to Funds; Fix Target Amount Constraint
-- ============================================================================
--
-- docs/database/tables/ funds.md: "Version 1 stores an optional reference
-- from transactions" for fund allocation/withdrawal, deferring a dedicated
-- fund_allocations table to a future version. Fund activity is modeled as a
-- TRANSFER where one side is a real account and the other is the fund
-- itself (fund_id set, the corresponding account column left null):
--   Allocation: from_account_id set, to_account_id null, fund_id set.
--   Withdrawal: to_account_id set, from_account_id null, fund_id set.
-- Ordinary account-to-account transfers are unaffected (fund_id stays
-- null, both account columns set, exactly as before).
--
-- Separately, funds.md states a target amount "must be greater than zero
-- when specified," but the V1 CHECK allowed zero. A target of exactly zero
-- is not a meaningful goal, so the constraint is corrected here.
-- ============================================================================

ALTER TABLE transactions
    ADD COLUMN fund_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_fund
        FOREIGN KEY (fund_id)
        REFERENCES funds(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_fund_transfer
        CHECK (
            fund_id IS NULL
            OR (
                transaction_type = 'TRANSFER'
                AND (from_account_id IS NULL) != (to_account_id IS NULL)
            )
        );

CREATE INDEX idx_transactions_fund
    ON transactions(fund_id);

CREATE INDEX idx_transactions_fund_date
    ON transactions(fund_id, transaction_date);

-- ----------------------------------------------------------------------------
-- Fix target_amount constraint: > 0, not >= 0
-- ----------------------------------------------------------------------------

ALTER TABLE funds
    DROP CONSTRAINT chk_funds_target_amount;

ALTER TABLE funds
    ADD CONSTRAINT chk_funds_target_amount
        CHECK (
            target_amount IS NULL
            OR target_amount > 0
        );
