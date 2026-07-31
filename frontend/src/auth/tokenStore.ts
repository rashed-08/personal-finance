/**
 * Access token holder.
 *
 * Kept in memory only — never localStorage/sessionStorage, so an XSS
 * payload cannot read a long-lived credential. Sessions survive reloads
 * through the httpOnly refresh cookie instead (see AuthProvider bootstrap).
 *
 * Lives outside React because the axios interceptor needs it too.
 */
let accessToken: string | null = null;

export function getAccessToken(): string | null {
    return accessToken;
}

export function setAccessToken(token: string | null): void {
    accessToken = token;
}
