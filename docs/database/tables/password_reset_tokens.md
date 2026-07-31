# Password Reset Tokens Table Specification

## Purpose

The `password_reset_tokens` table stores single-use tokens for the forgot-password flow.

`/api/auth/forgot-password` emails a link containing the raw token; `/api/auth/reset-password` consumes it and
sets the new password.

The Auth module owns this table.

---

# Columns

| Column | Type | Nullable | Default | Description |
|----------|--------|----------|----------|-------------|
| id | UUID | No | Generated | Primary Key |
| user_id | UUID | No | | Owning user |
| token_hash | VARCHAR(64) | No | | SHA-256 hex of the raw token, unique |
| expires_at | TIMESTAMP | No | | Expiry (default TTL 1h) |
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
- A token is usable when `used_at IS NULL` and `expires_at` is in the future; strictly single-use.
- Short TTL (1 hour default) because the token grants account takeover.
- Consuming a token revokes **all** of the user's refresh tokens (every session must re-authenticate).
- `/forgot-password` always answers 200 to prevent user enumeration; tokens are only issued for users
  with a local password.

---

# Java Entity Notes

Aggregate Root: `PasswordResetToken` — Repository: `PasswordResetTokenRepository`
Services: `ForgotPasswordService`, `ResetPasswordService`

---

# Flyway Notes

Created in `V4__auth_email_tokens.sql`. No seed data.
