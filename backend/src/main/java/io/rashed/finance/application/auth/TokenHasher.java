package io.rashed.finance.application.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 hashing for opaque refresh tokens.
 *
 * The raw token is high-entropy (256 random bits), so a fast
 * unsalted hash is appropriate — and required, because lookups
 * are by exact hash value.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256Hex(String rawToken) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(rawToken.getBytes(StandardCharsets.UTF_8))
            );

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
