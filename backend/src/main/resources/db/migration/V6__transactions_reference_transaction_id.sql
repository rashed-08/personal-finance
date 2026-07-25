-- ============================================================================
-- V6 - Add Missing reference_transaction_id Column
-- ============================================================================
--
-- TransactionEntity has always mapped a reference_transaction_id column
-- (used to link an ADJUSTMENT back to the transaction it corrects), but no
-- prior migration ever created it. Under ddl-auto=validate this mismatch
-- causes the application to fail on startup once the entity is validated
-- against the real schema.
-- ============================================================================

ALTER TABLE transactions
    ADD COLUMN reference_transaction_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_reference_transaction
        FOREIGN KEY (reference_transaction_id)
        REFERENCES transactions(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

CREATE INDEX idx_transactions_reference_transaction
    ON transactions(reference_transaction_id);
