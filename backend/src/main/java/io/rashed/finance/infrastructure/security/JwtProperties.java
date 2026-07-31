package io.rashed.finance.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT configuration, bound from {@code app.security.jwt}.
 *
 * @param secret          HMAC-SHA256 signing key, at least 32 bytes.
 * @param issuer          Value of the {@code iss} claim.
 * @param accessTokenTtl  Lifetime of access tokens (e.g. 15m).
 * @param refreshTokenTtl Lifetime of refresh tokens (e.g. 14d).
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
}
