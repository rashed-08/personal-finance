package io.rashed.finance.domain.users;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Single-use token proving ownership of an email address.
 * Only the SHA-256 hash of the raw token is stored.
 */
@Getter
@ToString(exclude = "tokenHash")
@EqualsAndHashCode(of = "id")
public final class EmailVerificationToken {

    private final EmailVerificationTokenId id;

    private final UserId userId;

    private final String tokenHash;

    private final LocalDateTime expiresAt;

    private final LocalDateTime usedAt;

    private final LocalDateTime createdAt;

    public EmailVerificationToken(EmailVerificationTokenId id, UserId userId, String tokenHash, LocalDateTime expiresAt, LocalDateTime usedAt, LocalDateTime createdAt) {

        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.usedAt = usedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static EmailVerificationToken issue(UserId userId, String tokenHash, LocalDateTime expiresAt) {

        Objects.requireNonNull(expiresAt, "Expiry cannot be null.");

        LocalDateTime now = LocalDateTime.now();

        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Token expiry must be in the future.");
        }

        return new EmailVerificationToken(EmailVerificationTokenId.newId(), userId, tokenHash, expiresAt, null, now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsable() {
        return !isUsed() && !isExpired();
    }

    public EmailVerificationToken markUsed() {

        if (isUsed()) {
            return this;
        }

        return new EmailVerificationToken(id, userId, tokenHash, expiresAt, LocalDateTime.now(), createdAt);
    }
}
