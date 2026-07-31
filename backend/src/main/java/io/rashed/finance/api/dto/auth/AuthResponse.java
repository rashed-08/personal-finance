package io.rashed.finance.api.dto.auth;

/**
 * Successful authentication response.
 *
 * The refresh token is intentionally absent — it travels only in an
 * httpOnly cookie.
 */
public record AuthResponse(

        String accessToken,

        String tokenType,

        long expiresIn,

        UserResponse user

) {
}
