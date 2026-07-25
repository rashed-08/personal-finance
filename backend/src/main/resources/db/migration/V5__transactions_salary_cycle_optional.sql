-- ============================================================================
-- V5 - Make salary_cycle_id Optional for Non-Cycle Transactions
-- ============================================================================
--
-- ADJUSTMENT, OPENING_BALANCE and MIGRATION transactions are not part of a
-- salary cycle: they correct, seed or import balances rather than track
-- day-to-day spending. Version 1 required salary_cycle_id on every
-- transaction, which made these three types impossible to persist.
--
-- This migration relaxes the NOT NULL constraint and replaces it with a
-- CHECK that still requires salary_cycle_id for INCOME, EXPENSE and
-- TRANSFER, where it remains meaningful.
-- ============================================================================

ALTER TABLE transactions
    ALTER COLUMN salary_cycle_id DROP NOT NULL;

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_salary_cycle_required
    CHECK (
        transaction_type IN ('ADJUSTMENT', 'OPENING_BALANCE', 'MIGRATION')
        OR salary_cycle_id IS NOT NULL
    );
