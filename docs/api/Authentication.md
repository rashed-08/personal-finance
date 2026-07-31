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
| GET | `/api/users/me` | Bearer | Current user profile |

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

---

# Future (tracked in the auth epic #11)

- Email verification, forgot/reset/change password (#33)
- Frontend integration, including the Google Sign-In button (#34)
