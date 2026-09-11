package io.rashed.finance.application.backup;

/**
 * The Google end of the authorization-code flow.
 *
 * An interface so the connection service can be tested without talking to
 * Google; the real implementation is
 * {@code GoogleDriveOAuthClientAdapter}.
 */
public interface GoogleOAuthClient {

    /**
     * Scope that lets the application manage only the files it created.
     *
     * Deliberately not {@code drive} or {@code drive.appdata}: this app
     * has no business reading the user's other documents, and backups
     * should stay visible in Drive so they can be downloaded by hand.
     */
    String DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file";

    /**
     * URL to send the user to for consent.
     *
     * @param state opaque value returned unchanged by Google; the caller
     *              uses it to tie the callback back to the request.
     */
    String authorizationUrl(String state);

    /**
     * Exchanges an authorization code for tokens.
     *
     * @throws BackupFailedException if the exchange fails or Google
     *         returns no refresh token.
     */
    GoogleTokens exchangeCode(String code);

    /**
     * Revokes a refresh token at Google, so disconnecting in this
     * application also drops the grant on Google's side.
     *
     * Best-effort: a failure here is logged, not propagated. The local
     * credential is being deleted either way, and leaving it in place
     * because Google was unreachable would be worse.
     */
    void revoke(String refreshToken);

    /**
     * @param refreshToken  long-lived credential to store.
     * @param grantedScopes space-separated scopes Google actually granted.
     * @param idToken       ID token from the same response, carrying the
     *                      account's identity. May be null if the
     *                      {@code openid} scope was not granted.
     */
    record GoogleTokens(String refreshToken, String grantedScopes, String idToken) {
    }
}
