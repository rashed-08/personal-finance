-- ============================================================================
-- V3: Authentication schema
-- users
-- refresh_tokens
--
-- Specifications:
--   docs/database/tables/users.md
--   docs/database/tables/refresh_tokens.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: users
-- ----------------------------------------------------------------------------

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(100),

    name                VARCHAR(100) NOT NULL,

    role                VARCHAR(30) NOT NULL DEFAULT 'OWNER',

    provider            VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_subject    VARCHAR(255),

    is_email_verified   BOOLEAN NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role
        CHECK (
            role IN (
                'OWNER',
                'ADMIN',
                'VIEWER'
            )
        ),

    CONSTRAINT chk_users_provider
        CHECK (
            provider IN (
                'LOCAL',
                'GOOGLE'
            )
        ),

    CONSTRAINT chk_users_local_password
        CHECK (provider <> 'LOCAL' OR password_hash IS NOT NULL)
);

CREATE UNIQUE INDEX uq_users_email
    ON users (lower(email));

CREATE UNIQUE INDEX uq_users_provider_subject
    ON users (provider, provider_subject)
    WHERE provider_subject IS NOT NULL;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Table: refresh_tokens
-- ----------------------------------------------------------------------------

CREATE TABLE refresh_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID NOT NULL,

    token_hash          VARCHAR(64) NOT NULL,

    expires_at          TIMESTAMP NOT NULL,
    revoked_at          TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_refresh_tokens_token_hash
    ON refresh_tokens (token_hash);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);
