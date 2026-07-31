package io.rashed.finance.application.auth;

/**
 * Identity claims extracted from a verified Google ID token.
 *
 * @param subject       Google's stable user identifier ({@code sub} claim).
 * @param email         Email address at Google.
 * @param emailVerified Whether Google has verified the email.
 * @param name          Display name, may be null.
 */
public record GoogleUserInfo(
        String subject,
        String email,
        boolean emailVerified,
        String name
) {
}
