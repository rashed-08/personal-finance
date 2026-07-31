package io.rashed.finance.application.auth;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates high-entropy opaque tokens (256 random bits, base64url).
 */
public final class OpaqueTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OpaqueTokenGenerator() {
    }

    public static String generate() {

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
