# Email Verification Tokens Table Specification

## Purpose

The `email_verification_tokens` table stores single-use tokens sent to a user's email address to prove ownership.

Clicking the emailed link posts the raw token to `/api/auth/verify-email`, which marks the user's email verified.

The Auth module owns this table.

---

# Columns

| Column | Type | Nullable | Default | Description |
|----------|--------|----------|----------|-------------|
| id | UUID | No | Generated | Primary Key |
| user_id | UUID | No | | Owning user |
| token_hash | VARCHAR(64) | No | | SHA-256 hex of the raw token, unique |
| expires_at | TIMESTAMP | No | | Expiry (default TTL 24h) |
| used_at | TIMESTAMP | Yes | | Set when consumed |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Issue timestamp |

---

# Foreign Keys

| Column | References | On Delete |
|--------|------------|-----------|
| user_id | users(id) | CASCADE |

---

# Indexes

- PK(id)
- UQ(token_hash)
- IDX(user_id)

---

# Business Rules

- Raw token value is never stored — only its SHA-256 hash.
- A token is usable when `used_at IS NULL` and `expires_at` is in the future.
- Consuming a token sets `used_at`; tokens are strictly single-use.
- Requesting a new verification email issues a new token; older ones remain valid until expiry (v1 simplicity).

---

# Java Entity Notes

Aggregate Root: `EmailVerificationToken` — Repository: `EmailVerificationTokenRepository`
Services: `SendEmailVerificationService`, `VerifyEmailService`

---

# Flyway Notes

Created in `V4__auth_email_tokens.sql`. No seed data.
