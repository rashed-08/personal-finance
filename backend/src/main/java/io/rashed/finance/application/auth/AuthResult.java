package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.User;

import java.time.Duration;

/**
 * Result of a successful authentication: the user plus a freshly
 * issued token pair.
 *
 * @param accessTokenExpiresIn Access token lifetime in seconds.
 * @param refreshToken         Raw refresh token — only ever handed to the
 *                             client as an httpOnly cookie, never stored.
 * @param refreshTokenTtl      Refresh token lifetime, used for the cookie max age.
 */
public record AuthResult(
        User user,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        Duration refreshTokenTtl
) {
}
