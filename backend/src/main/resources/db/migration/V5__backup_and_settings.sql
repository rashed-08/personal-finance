-- ============================================================================
-- V5: Backup, restore and settings support
--
-- 1. backup_history gains operation_type + backup_format, so a RESTORE is
--    audited the same way a BACKUP is (docs/database/tables/backup_history.md
--    requires an immutable trail for both; V1 could only record backups).
-- 2. google_oauth_tokens stores the long-lived Google Drive refresh token.
-- 3. New settings keys for the backup configuration.
--
-- Specifications:
--   docs/database/tables/backup_history.md
--   docs/database/tables/settings.md
--   docs/operations/BackupStrategy.md
--   docs/operations/RestoreStrategy.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- backup_history: audit restores, not just backups
-- ----------------------------------------------------------------------------

-- Existing rows are all backups by definition: V1 had no way to record a
-- restore. The default backfills them, then the column becomes mandatory.
ALTER TABLE backup_history
    ADD COLUMN operation_type VARCHAR(20) NOT NULL DEFAULT 'BACKUP';

ALTER TABLE backup_history
    ALTER COLUMN operation_type DROP DEFAULT;

ALTER TABLE backup_history
    ADD CONSTRAINT chk_backup_history_operation_type
        CHECK (
            operation_type IN (
                'BACKUP',
                'RESTORE'
            )
        );

-- Which serializer produced the archive. A restore must use the matching
-- reader, so the format is recorded per operation rather than read from
-- settings (the setting may have changed since the backup was taken).
ALTER TABLE backup_history
    ADD COLUMN backup_format VARCHAR(20) NOT NULL DEFAULT 'JSON';

ALTER TABLE backup_history
    ALTER COLUMN backup_format DROP DEFAULT;

ALTER TABLE backup_history
    ADD CONSTRAINT chk_backup_history_backup_format
        CHECK (
            backup_format IN (
                'JSON',
                'PG_DUMP'
            )
        );

-- Provider-assigned identifier for the stored object: null for LOCAL (the
-- file path is enough), the Drive file id for GOOGLE_DRIVE. Needed to
-- download or delete a remote archive later.
ALTER TABLE backup_history
    ADD COLUMN storage_reference VARCHAR(255);

-- ----------------------------------------------------------------------------
-- backup_history: indexes
-- ----------------------------------------------------------------------------

-- The history screen lists newest-first and filters by operation/status.
CREATE INDEX idx_backup_history_started_at
    ON backup_history (backup_started_at DESC);

CREATE INDEX idx_backup_history_operation_type
    ON backup_history (operation_type);

CREATE INDEX idx_backup_history_backup_status
    ON backup_history (backup_status);

CREATE INDEX idx_backup_history_provider
    ON backup_history (provider);

-- ----------------------------------------------------------------------------
-- Table: google_oauth_tokens
--
-- Google Drive needs offline access, which means holding the refresh token
-- issued by the authorization-code exchange. Access tokens are short-lived
-- and deliberately NOT persisted — they are derived on demand and kept in
-- memory only.
--
-- One row per user per provider: re-connecting Drive replaces the token
-- rather than accumulating rows.
-- ----------------------------------------------------------------------------

CREATE TABLE google_oauth_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID NOT NULL,

    scope               VARCHAR(20) NOT NULL,

    refresh_token       TEXT NOT NULL,

    granted_scopes      TEXT,

    account_email       VARCHAR(255),

    connected_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_google_oauth_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_google_oauth_tokens_scope
        CHECK (
            scope IN (
                'DRIVE'
            )
        )
);

CREATE UNIQUE INDEX uq_google_oauth_tokens_user_scope
    ON google_oauth_tokens (user_id, scope);

CREATE TRIGGER trg_google_oauth_tokens_updated_at
BEFORE UPDATE ON google_oauth_tokens
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Settings: backup configuration
--
-- V2 already seeded AUTO_BACKUP_ENABLED and BACKUP_PROVIDER. These add the
-- remaining knobs the backup module reads. ON CONFLICT DO NOTHING keeps the
-- migration idempotent against databases where they were added by hand.
-- ----------------------------------------------------------------------------

INSERT INTO settings (
    setting_key,
    setting_value,
    value_type,
    description,
    is_system
)
VALUES

(
    'BACKUP_FORMAT',
    'JSON',
    'STRING',
    'Archive format produced by a backup. JSON (application export) or PG_DUMP (physical dump).',
    TRUE
),

(
    'BACKUP_DIRECTORY',
    'storage/backups',
    'STRING',
    'Directory holding local backup archives, relative to the application working directory.',
    TRUE
),

(
    'BACKUP_RETENTION_COUNT',
    '30',
    'INTEGER',
    'Number of automatic backups to keep. Older archives are pruned; 0 disables pruning.',
    TRUE
),

(
    'AUTO_BACKUP_CRON',
    '0 0 2 * * *',
    'STRING',
    'Schedule for automatic backups, as a Spring cron expression. Applied on restart.',
    TRUE
),

(
    'GOOGLE_DRIVE_FOLDER_NAME',
    'PersonalFinanceApp',
    'STRING',
    'Root folder created in Google Drive for uploaded backups.',
    TRUE
)

ON CONFLICT (setting_key)
DO NOTHING;
