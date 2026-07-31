package io.rashed.finance.domain.users;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Server-side state of an issued refresh token.
 *
 * Only the SHA-256 hash of the raw token is stored; the raw value
 * exists solely in the httpOnly cookie held by the client.
 */
@Getter
@ToString(exclude = "tokenHash")
@EqualsAndHashCode(of = "id")
public final class RefreshToken {

    private final RefreshTokenId id;

    private final UserId userId;

    private final String tokenHash;

    private final LocalDateTime expiresAt;

    private final LocalDateTime revokedAt;

    private final LocalDateTime createdAt;

    public RefreshToken(RefreshTokenId id, UserId userId, String tokenHash, LocalDateTime expiresAt, LocalDateTime revokedAt, LocalDateTime createdAt) {

        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revokedAt = revokedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static RefreshToken issue(UserId userId, String tokenHash, LocalDateTime expiresAt) {

        Objects.requireNonNull(expiresAt, "Expiry cannot be null.");

        LocalDateTime now = LocalDateTime.now();

        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Refresh token expiry must be in the future.");
        }

        return new RefreshToken(RefreshTokenId.newId(), userId, tokenHash, expiresAt, null, now);
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    public RefreshToken revoke() {

        if (isRevoked()) {
            return this;
        }

        return new RefreshToken(id, userId, tokenHash, expiresAt, LocalDateTime.now(), createdAt);
    }
}
