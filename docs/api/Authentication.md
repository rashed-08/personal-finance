# Authentication API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for authentication and the current-user profile: registration, email/password
login, access-token refresh, and logout.

Backing tables are described in `docs/database/tables/users.md` and `docs/database/tables/refresh_tokens.md`.
The overall design (token model, cookie strategy, filter chain) is described in
`docs/architecture/SecurityArchitecture.md`.

Base paths

```
/api/auth
/api/users
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json`.

---

# Token Model

- **Access token** — a short-lived (default 15 minutes) HMAC-SHA256 signed JWT, returned in the response body.
  The client sends it on every request as `Authorization: Bearer <token>`. It is never stored server-side.
- **Refresh token** — a long-lived (default 14 days) opaque random value, delivered **only** as an
  `httpOnly` cookie named `refresh_token`, scoped to `Path=/api/auth`. The server stores its SHA-256 hash.
  Refresh tokens are **rotated**: every call to `/api/auth/refresh` revokes the presented token and issues a
  new one. Reusing an already-rotated token revokes **all** of the user's sessions.

JWT claims: `sub` (user id), `email`, `role`, `iss` (`personal-finance`), `iat`, `exp`.

---

# Endpoints Overview

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/api/auth/register` | none | Create an account, returns tokens (auto-login) |
| POST | `/api/auth/login` | none | Email/password login |
| POST | `/api/auth/google` | none | Sign in with a Google ID token |
| POST | `/api/auth/refresh` | refresh cookie | Rotate refresh token, new access token |
| POST | `/api/auth/logout` | refresh cookie | Revoke the session, clear the cookie |
| POST | `/api/auth/verify-email` | none | Consume an emailed verification token |
| POST | `/api/auth/resend-verification` | none | Resend the verification email |
| POST | `/api/auth/forgot-password` | none | Email a password-reset link |
| POST | `/api/auth/reset-password` | none | Consume a reset token, set a new password |
| GET | `/api/users/me` | Bearer | Current user profile |
| PUT | `/api/users/me/password` | Bearer | Change password (requires current password) |

Every other `/api/**` endpoint requires a valid Bearer access token and returns `401` otherwise.

---

# POST /api/auth/register

Creates a user and immediately authenticates them. Status `201`.

## Request Body

```json
{
  "email": "user@example.com",
  "password": "at-least-8-chars",
  "name": "User Name"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `email` | string, required | Valid email, max 255 chars, unique (case-insensitive) |
| `password` | string, required | 8–72 characters (BCrypt limit) |
| `name` | string, required | Max 100 chars |

## Response — 201 Created

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "b1a2c3d4-...",
    "email": "user@example.com",
    "name": "User Name",
    "role": "OWNER",
    "emailVerified": false,
    "createdAt": "2026-07-31T10:00:00"
  }
}
```

Plus a `Set-Cookie: refresh_token=...; Path=/api/auth; HttpOnly; SameSite=Lax; Max-Age=1209600` header.

## Errors

| Status | Cause |
|--------|-------|
| 400 | Validation failure, or email already registered |

---

# POST /api/auth/login

## Request Body

```json
{
  "email": "user@example.com",
  "password": "secret"
}
```

## Response — 200 OK

Same shape as `/register` (body + refresh cookie).

## Errors

| Status | Cause |
|--------|-------|
| 401 | Unknown email, wrong password, or Google-only account (detail is intentionally generic: "Invalid email or password.") |

---

# POST /api/auth/google

Signs in with Google. The frontend obtains an **ID token** from Google Identity Services and posts it here;
the backend verifies it against Google's public keys and the configured client ID (audience).

Account resolution, in order:

1. A user already linked to this Google subject → sign in.
2. A user with the same email (which Google has verified) → the Google identity is linked to that account;
   an existing local password keeps working.
3. Otherwise a new user is registered (`provider = GOOGLE`, no local password, email pre-verified).

## Request Body

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

## Response — 200 OK

Same shape as `/login` (body + refresh cookie).

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing `idToken` |
| 401 | Token invalid/expired/wrong audience, or Google reports the email as unverified |
| 409 | `GOOGLE_CLIENT_ID` is not configured on the server |

## Setup

Google Sign-In stays **switched off until a client ID exists** — the backend answers 409 and the frontend hides
the button (in development it shows a note explaining why instead of rendering nothing).

1. In [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials), create an
   **OAuth client ID** of type **Web application**.
2. Add the dev origin under *Authorised JavaScript origins*: `http://localhost:5173`.
   Google Identity Services refuses to render the button on an origin that is not listed.
   No redirect URI is needed — this is the ID-token flow, not the redirect flow.
3. Put the same value in the root `.env` under **both** names — the backend verifies the audience, the
   frontend renders the button:

   ```
   GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
   VITE_GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
   ```

4. Restart both the backend and the Vite dev server. `VITE_*` values are read at startup, so a running dev
   server will not pick up the change.

Vite reads that root `.env` because `frontend/vite.config.ts` sets `envDir: '..'`. Only `VITE_`-prefixed
variables reach the browser, so the backend secrets in the same file are not bundled.

---

# POST /api/auth/refresh

Reads the `refresh_token` cookie. No request body.

On success the presented refresh token is **revoked** and replaced (rotation); the response carries a new
access token in the body and the new refresh token in a fresh `Set-Cookie`.

## Response — 200 OK

Same shape as `/login`.

## Errors

| Status | Cause |
|--------|-------|
| 401 | Cookie missing, token unknown, expired, or already revoked. Reuse of a rotated token additionally revokes every active session for that user (possible token theft). |

---

# POST /api/auth/logout

Reads the `refresh_token` cookie, revokes it server-side, and clears the cookie (`Max-Age=0`).
Idempotent: succeeds even when the cookie is absent or already revoked. No request body.

## Response — 204 No Content

---

# Email flows

Registration triggers a verification email automatically (best-effort — a mail outage never fails the
registration). Emailed links point at the frontend: `{FRONTEND_BASE_URL}/verify-email?token=…` and
`{FRONTEND_BASE_URL}/reset-password?token=…`. All one-time tokens are stored hashed, are single-use, and expire.

## Local development: two options

**No SMTP server (default).** `app.mail.enabled` is `false` in the `local` profile, so each message is written
to the application log instead of being sent — copy the link straight from the console:

```
[mail disabled] would send to you@example.com
  Subject: Verify your email — Personal Finance
  http://localhost:5173/verify-email?token=V6r5drHJhS0P9DIm…
```

Because that puts one-time tokens in the log, **only ever disable mail in local development.**

**Real delivery via Mailpit.** Start the service and switch mail on:

```
docker compose -f infra/compose.yaml up -d mailpit
MAIL_ENABLED=true ./mvnw spring-boot:run    # inbox at http://localhost:8025
```

If mail is enabled but the server is unreachable, a single warning is logged and the request still succeeds.

## POST /api/auth/verify-email

Body `{ "token": "..." }`. Marks the user's email verified and consumes the token.
**204** on success, **400** for an unknown/expired/used token.

## POST /api/auth/resend-verification

Body `{ "email": "user@example.com" }`. Always **204** — whether the email exists or is already verified is
not observable (no user enumeration).

## POST /api/auth/forgot-password

Body `{ "email": "user@example.com" }`. Always **204** (no user enumeration). A reset token (TTL 1h) is only
issued and emailed when the account exists **and** has a local password (Google-only accounts get nothing).

## POST /api/auth/reset-password

Body `{ "token": "...", "newPassword": "..." }` (8–72 chars). Consumes the token, sets the password, and
**revokes every refresh token** — all sessions must log in again.
**204** on success, **400** for an unknown/expired/used token.

## PUT /api/users/me/password

Requires `Authorization: Bearer`. Body `{ "currentPassword": "...", "newPassword": "..." }`.
Verifies the current password, sets the new one, and revokes every refresh token.
**204** on success, **401** for a wrong current password, **400** when the account has no local password
(Google-only — use forgot-password to set one).

---

# GET /api/users/me

Requires `Authorization: Bearer <accessToken>`.

## Response — 200 OK

```json
{
  "id": "b1a2c3d4-...",
  "email": "user@example.com",
  "name": "User Name",
  "role": "OWNER",
  "emailVerified": false,
  "createdAt": "2026-07-31T10:00:00"
}
```

## Errors

| Status | Cause |
|--------|-------|
| 401 | Missing/invalid/expired access token |
| 404 | Token valid but user no longer exists |

---

# Configuration

| Property | Env var | Default | Purpose |
|----------|---------|---------|---------|
| `app.security.jwt.secret` | `JWT_SECRET` | — (dev value in `local` profile) | HMAC key, ≥ 32 bytes |
| `app.security.jwt.access-token-ttl` | `JWT_ACCESS_TOKEN_TTL` | `15m` | Access token lifetime |
| `app.security.jwt.refresh-token-ttl` | `JWT_REFRESH_TOKEN_TTL` | `14d` | Refresh token lifetime |
| `app.security.cookie-secure` | `COOKIE_SECURE` | `false` | Mark refresh cookie `Secure` (enable behind HTTPS) |
| `app.security.google.client-id` | `GOOGLE_CLIENT_ID` | — (feature disabled when blank) | Google OAuth Web client ID (ID-token audience) |
| `app.security.tokens.email-verification-ttl` | `EMAIL_VERIFICATION_TTL` | `24h` | Verification token lifetime |
| `app.security.tokens.password-reset-ttl` | `PASSWORD_RESET_TTL` | `1h` | Reset token lifetime |
| `app.mail.enabled` | `MAIL_ENABLED` | `true` (`false` in `local`) | Send email, or log it instead |
| `app.mail.from` | `MAIL_FROM` | `noreply@personal-finance.local` | Sender address |
| `app.frontend-base-url` | `FRONTEND_BASE_URL` | `http://localhost:5173` | Base URL for emailed links |
| `spring.mail.host` / `port` / … | `SPRING_MAIL_*` | Mailpit (localhost:1025) in `local` | SMTP server |
| `app.default-user.enabled` | `DEFAULT_USER_ENABLED` | `false` (`true` in `local`) | Seed a login on startup |
| `app.default-user.email` | `DEFAULT_USER_EMAIL` | `owner@personal-finance.local` | Seeded account's email |
| `app.default-user.password` | `DEFAULT_USER_PASSWORD` | `password123` | Seeded account's password |
| `app.default-user.name` | `DEFAULT_USER_NAME` | `Default Owner` | Seeded account's display name |

---

# Default Development User

On a fresh database there is no account to log in with, so the `local` profile seeds one at startup
(`DefaultUserInitializer`) and prints the credentials:

```
 Created the default development user
   email:    owner@personal-finance.local
   password: password123
```

It is created pre-verified with the `OWNER` role, and creation is skipped when the email already exists — so
restarting never overwrites a password you changed.

**This is off by default and only enabled in the `local` profile.** A known password must never exist in a
deployed environment; set `DEFAULT_USER_ENABLED=false` to disable it locally too.

---

# Client Integration

The React SPA (`frontend/`) consumes these endpoints via `src/services/auth.service.ts`. Token handling,
the 401-refresh-retry interceptor, and route guarding are described in
`docs/architecture/SecurityArchitecture.md`. `VITE_API_URL` points the SPA at this API.
