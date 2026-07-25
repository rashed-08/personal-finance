-- ============================================================================
-- V7 - Fix migration_batch_id / reconciliation_batch_id Column Types
-- ============================================================================
--
-- TransactionEntity has always mapped migrationBatchId and
-- reconciliationBatchId as VARCHAR(100) business identifiers (e.g.
-- "google-keep-2026-07"), matching the domain model and validation rules
-- in Transaction.migration(). V1 mistakenly declared both DB columns as
-- UUID, which fails Hibernate schema validation (ddl-auto=validate) as
-- soon as the entity is loaded.
--
-- Neither column has a foreign key constraint, so this is a safe type
-- change with no downstream references to update.
-- ============================================================================

ALTER TABLE transactions
    ALTER COLUMN migration_batch_id TYPE VARCHAR(100);

ALTER TABLE transactions
    ALTER COLUMN reconciliation_batch_id TYPE VARCHAR(100);
