-- ============================================================================
-- V4: One-time tokens for email flows
-- email_verification_tokens
-- password_reset_tokens
--
-- Specifications:
--   docs/database/tables/email_verification_tokens.md
--   docs/database/tables/password_reset_tokens.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: email_verification_tokens
-- ----------------------------------------------------------------------------

CREATE TABLE email_verification_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID NOT NULL,

    token_hash          VARCHAR(64) NOT NULL,

    expires_at          TIMESTAMP NOT NULL,
    used_at             TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verification_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_email_verification_tokens_token_hash
    ON email_verification_tokens (token_hash);

CREATE INDEX idx_email_verification_tokens_user_id
    ON email_verification_tokens (user_id);

-- ----------------------------------------------------------------------------
-- Table: password_reset_tokens
-- ----------------------------------------------------------------------------

CREATE TABLE password_reset_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID NOT NULL,

    token_hash          VARCHAR(64) NOT NULL,

    expires_at          TIMESTAMP NOT NULL,
    used_at             TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);
