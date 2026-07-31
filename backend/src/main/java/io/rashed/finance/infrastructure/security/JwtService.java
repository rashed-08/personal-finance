package io.rashed.finance.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.rashed.finance.common.enums.UserRole;
import io.rashed.finance.domain.users.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates and validates HMAC-SHA256 signed JWT access tokens.
 */
@Component
public class JwtService {

    private final JwtProperties properties;

    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {

        this.properties = properties;

        if (properties.secret() == null || properties.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be configured and at least 32 bytes long."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.accessTokenTtl());

        return Jwts.builder()
                .subject(user.getId().asString())
                .issuer(properties.issuer())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }

    /**
     * Parses and validates a token, returning the authenticated principal.
     * Empty when the token is malformed, expired, or has a bad signature.
     */
    public Optional<AuthenticatedUser> parse(String token) {

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(claims.get("role", String.class))
            ));

        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
