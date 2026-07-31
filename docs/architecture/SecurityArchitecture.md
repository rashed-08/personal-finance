# Security Architecture

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Overview

Authentication is stateless JWT over a Spring Security filter chain; sessions are represented server-side only by
revocable refresh tokens. Introduced in milestone `v0.11-authentication-authorization` (issue #11, parts #31–#34).

Design decisions in force:

- **Single-user login gate.** Financial tables are not user-scoped yet; authentication protects access to the
  application as a whole. Multi-user data ownership is a separate future change.
- **Stateless API.** No server HTTP session (`SessionCreationPolicy.STATELESS`); every request authenticates via
  the `Authorization: Bearer` header.
- **Split token model.** Short-lived JWT access token (in the SPA's memory) + long-lived opaque refresh token
  (httpOnly cookie, hashed at rest, rotated on every use).

---

# Components

All in `io.rashed.finance.infrastructure.security`:

| Component | Responsibility |
|-----------|----------------|
| `SecurityConfig` | Filter chain, authorization rules, CORS, `PasswordEncoder` (BCrypt), method security |
| `JwtProperties` | `app.security.jwt.*` configuration binding |
| `JwtService` | Create/validate HMAC-SHA256 JWTs |
| `JwtAuthenticationFilter` | Reads Bearer header, populates the `SecurityContext` with an `AuthenticatedUser` |
| `AuthenticatedUser` | Principal record `(id, email, role)` — built from claims, no DB hit per request |
| `ProblemDetailAuthenticationEntryPoint` | 401 responses as RFC 7807 (filter-level failures bypass `@RestControllerAdvice`) |
| `ProblemDetailAccessDeniedHandler` | 403 responses as RFC 7807 |

Application-layer use cases live in `io.rashed.finance.application.auth`
(`RegisterUserService`, `LoginService`, `RefreshTokenService`, `LogoutService`, `IssueTokensService`), domain
aggregates in `io.rashed.finance.domain.users` (`User`, `RefreshToken`).

---

# Authorization Rules

| Pattern | Rule |
|---------|------|
| `OPTIONS /**` | permit (CORS preflight) |
| `/api/auth/**` | permit (they establish authentication) |
| `/actuator/health`, `/actuator/info` | permit |
| everything else | authenticated |

`@EnableMethodSecurity` is on; roles arrive as `ROLE_OWNER` / `ROLE_ADMIN` / `ROLE_VIEWER` authorities, so
`@PreAuthorize("hasRole('ADMIN')")` works when role-based rules are introduced. v0.11 assigns every user `OWNER`
and defines no role-restricted endpoints.

---

# Request Flow

```
Request
  → CORS filter
  → JwtAuthenticationFilter
        Bearer token present & valid → SecurityContext = AuthenticatedUser
        absent/invalid              → continue unauthenticated
  → authorization rules
        unauthenticated + protected → 401 (ProblemDetailAuthenticationEntryPoint)
        authenticated + forbidden   → 403 (ProblemDetailAccessDeniedHandler)
  → controller
```

---

# Credential & Token Storage

| Secret | At rest | Notes |
|--------|---------|-------|
| Password | BCrypt hash (`users.password_hash`) | Never logged; `User.toString()` excludes it |
| Refresh token | SHA-256 hex (`refresh_tokens.token_hash`) | Raw value only in the httpOnly cookie; high-entropy input, exact-match lookup |
| JWT signing key | `JWT_SECRET` env / config | ≥ 32 bytes enforced at startup; dev-only default in the `local` profile |

Refresh token lifecycle: issue → rotate (revoke + reissue on every `/refresh`) → revoke (logout).
**Reuse detection:** presenting an already-rotated token revokes all of the user's active tokens, since it
indicates the token may have been stolen.

---

# CSRF & CORS

- CSRF protection is disabled: no session cookie exists, and the only cookie (`refresh_token`) is scoped to
  `Path=/api/auth` with `SameSite=Lax`, whose endpoints either require nothing sensitive or only rotate/revoke.
- CORS allows `http://localhost:5173` with credentials (the Vite dev server). Production origins must be added
  when deployment exists.

---

# Transport & Environments

- `app.security.cookie-secure=true` must be set behind HTTPS so the refresh cookie is `Secure`.
- HTTPS is required in production (NFR-006); local development runs plain HTTP.

---

# Frontend

The SPA holds the access token **in memory only** (`src/auth/tokenStore.ts`) — never `localStorage`, so an XSS
payload cannot exfiltrate a long-lived credential. Sessions survive a page reload because `AuthProvider` calls
`POST /api/auth/refresh` on mount, using the httpOnly cookie.

| Concern | Where |
|---------|-------|
| Session state | `src/auth/AuthProvider.tsx`, consumed via `useAuth()` |
| Token attach + 401 recovery | axios interceptors in `src/api/api.ts` |
| Route guarding | `src/routes/ProtectedRoute.tsx` wraps every in-app route |
| Public pages | `src/layouts/AuthLayout.tsx` (login, register, reset, verify) |

The response interceptor retries a 401 exactly once after refreshing, sharing a single in-flight refresh promise
across concurrent failures. `/auth/**` responses are excluded — a 401 there is a genuine credential failure.
When refresh fails, the session is cleared, the React Query cache is dropped, and the user lands on `/login`.

---

# Testing

- Unit: `JwtServiceTest`, `RegisterUserServiceTest`, `LoginServiceTest`, `RefreshTokenServiceTest`,
  `LogoutServiceTest`.
- Integration: `AuthFlowIntegrationTest` exercises register → me → refresh rotation → reuse rejection →
  logout against the real filter chain and database (requires the compose PostgreSQL, like every
  `@SpringBootTest` in the project).

---

# Future Work

- Role-restricted endpoints once multiple users/roles exist
- Multi-user data scoping (`user_id` on the financial tables)
- Cleanup job purging expired/revoked refresh and one-time tokens
- Session listing / "sign out other devices" UI on top of `refresh_tokens`
