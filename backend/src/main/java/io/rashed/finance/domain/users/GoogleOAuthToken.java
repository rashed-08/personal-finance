package io.rashed.finance.domain.users;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A user's standing grant to a Google capability — currently Drive.
 *
 * Unlike {@link RefreshToken}, the refresh token here cannot be hashed:
 * the application has to <em>use</em> it to mint access tokens, not merely
 * recognise it. It is therefore held encrypted, and this aggregate only
 * ever sees the ciphertext — see {@code SecretCipher} and
 * {@code GoogleDriveConnectionService}.
 *
 * Access tokens are never stored. They last an hour, are derived on
 * demand, and persisting them would widen the blast radius for nothing.
 */
@Getter
@ToString(exclude = "encryptedRefreshToken")
@EqualsAndHashCode(of = "id")
public final class GoogleOAuthToken {

    private final GoogleOAuthTokenId id;

    private final UserId userId;

    private final GoogleOAuthScope scope;

    /**
     * The refresh token, encrypted. Never logged, never returned by the
     * API.
     */
    private final String encryptedRefreshToken;

    /**
     * Scopes Google actually granted, space-separated as returned by the
     * token endpoint. Worth keeping: a user can untick a scope on the
     * consent screen, and the resulting failure is otherwise a puzzle.
     */
    private final String grantedScopes;

    /**
     * Google account the grant belongs to, shown in the UI so it is clear
     * which Drive the backups are going to.
     */
    private final String accountEmail;

    private final LocalDateTime connectedAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public GoogleOAuthToken(
            GoogleOAuthTokenId id,
            UserId userId,
            GoogleOAuthScope scope,
            String encryptedRefreshToken,
            String grantedScopes,
            String accountEmail,
            LocalDateTime connectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.scope = Objects.requireNonNull(scope, "OAuth scope is required.");
        this.encryptedRefreshToken = requireToken(encryptedRefreshToken);

        this.grantedScopes = grantedScopes;
        this.accountEmail = accountEmail;

        this.connectedAt = Objects.requireNonNull(connectedAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static GoogleOAuthToken connect(
            UserId userId,
            GoogleOAuthScope scope,
            String encryptedRefreshToken,
            String grantedScopes,
            String accountEmail
    ) {

        LocalDateTime now = LocalDateTime.now();

        return new GoogleOAuthToken(
                GoogleOAuthTokenId.newId(),
                userId,
                scope,
                encryptedRefreshToken,
                grantedScopes,
                accountEmail,
                now,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static String requireToken(String encryptedRefreshToken) {

        Objects.requireNonNull(encryptedRefreshToken, "Refresh token is required.");

        if (encryptedRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be empty.");
        }

        return encryptedRefreshToken;
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    /**
     * Replaces the stored token after a re-consent, keeping the row's
     * identity so the unique (user, scope) constraint still holds.
     */
    public GoogleOAuthToken reconnect(
            String newEncryptedRefreshToken,
            String newGrantedScopes,
            String newAccountEmail
    ) {

        LocalDateTime now = LocalDateTime.now();

        return new GoogleOAuthToken(
                id,
                userId,
                scope,
                requireToken(newEncryptedRefreshToken),
                newGrantedScopes,
                newAccountEmail,
                now,
                createdAt,
                now
        );
    }

    /**
     * Whether Google granted the scope the application asked for. A grant
     * missing it will fail on the first upload, so it is worth checking
     * before claiming Drive is connected.
     */
    public boolean grants(String requiredScope) {

        if (grantedScopes == null || grantedScopes.isBlank()) {
            // Older rows predate scope recording; assume the grant is
            // intact and let the API report the truth.
            return true;
        }

        for (String granted : grantedScopes.split(" ")) {
            if (granted.equals(requiredScope)) {
                return true;
            }
        }

        return false;
    }
}
