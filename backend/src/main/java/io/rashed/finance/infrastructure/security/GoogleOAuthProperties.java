package io.rashed.finance.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Google OAuth configuration, bound from {@code app.security.google}.
 *
 * The same OAuth client covers both Google features, but they need
 * different things from it:
 *
 * <ul>
 *   <li>Sign-In only verifies ID tokens, so {@link #clientId()} alone is
 *       enough (and that is all
 *       {@link GoogleIdTokenVerifierAdapter} asks for).</li>
 *   <li>Drive backup runs the authorization-code flow for offline
 *       access, which also needs {@link #clientSecret()} and a redirect
 *       URI registered in the Google Cloud console.</li>
 * </ul>
 *
 * Both are blank by default: each feature reports its own missing
 * configuration rather than failing startup for the other's sake.
 *
 * @param clientId          OAuth 2.0 Web client ID.
 * @param clientSecret      OAuth 2.0 client secret. Drive only.
 * @param driveRedirectUri  Where Google returns the authorization code.
 *                          Must match the console entry exactly.
 */
@ConfigurationProperties(prefix = "app.security.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret,
        String driveRedirectUri
) {

    public boolean hasClientId() {
        return clientId != null && !clientId.isBlank();
    }

    public boolean hasClientSecret() {
        return clientSecret != null && !clientSecret.isBlank();
    }

    public boolean hasDriveRedirectUri() {
        return driveRedirectUri != null && !driveRedirectUri.isBlank();
    }

    /**
     * Whether the authorization-code flow can run at all.
     */
    public boolean isDriveConfigured() {
        return hasClientId() && hasClientSecret() && hasDriveRedirectUri();
    }
}
