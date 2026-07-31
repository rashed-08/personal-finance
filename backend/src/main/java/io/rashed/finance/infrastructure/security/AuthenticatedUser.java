package io.rashed.finance.infrastructure.security;

import io.rashed.finance.common.enums.UserRole;

import java.util.UUID;

/**
 * Authentication principal placed in the SecurityContext by
 * {@link JwtAuthenticationFilter}, built purely from access-token
 * claims (no database access per request).
 */
public record AuthenticatedUser(
        UUID id,
        String email,
        UserRole role
) {
}
