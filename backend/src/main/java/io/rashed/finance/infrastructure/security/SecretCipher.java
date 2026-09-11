package io.rashed.finance.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Symmetric encryption for third-party credentials held at rest.
 *
 * The application's own refresh tokens are hashed, because it only needs
 * to recognise them. A Google Drive refresh token has to be replayed to
 * Google, so it must be recoverable — which makes plaintext in the
 * database the wrong answer and this the right one.
 *
 * AES-256-GCM, so the ciphertext is authenticated: tampering is detected
 * on decrypt rather than producing garbage that gets sent to Google. A
 * fresh 12-byte nonce per encryption is prepended to the ciphertext, base64
 * over the whole thing.
 *
 * The key comes from {@code app.security.encryption.secret}. It is
 * optional: without it the application runs normally and only the
 * features that need reversible secrets — currently Google Drive backup —
 * refuse to start up, with an explanatory message.
 */
@Component
public class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int NONCE_LENGTH = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKeySpec key;

    private final SecureRandom random = new SecureRandom();

    public SecretCipher(
            @Value("${app.security.encryption.secret:}") String secret
    ) {
        this.key = secret == null || secret.isBlank() ? null : deriveKey(secret);
    }

    /**
     * Whether a key is configured. Callers that need reversible secrets
     * check this and report the requirement themselves, so the message can
     * name the feature that needs it.
     */
    public boolean isConfigured() {
        return key != null;
    }

    public String encrypt(String plaintext) {

        requireConfigured();

        byte[] nonce = new byte[NONCE_LENGTH];
        random.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not encrypt the secret.", ex);
        }
    }

    /**
     * @throws IllegalStateException if the value was not produced by
     *         {@link #encrypt} or the key has changed since — either way
     *         the stored credential is unusable and must be re-obtained.
     */
    public String decrypt(String encrypted) {

        requireConfigured();

        try {
            byte[] combined = Base64.getDecoder().decode(encrypted);

            if (combined.length <= NONCE_LENGTH) {
                throw new IllegalStateException("Stored secret is too short to be valid.");
            }

            byte[] nonce = new byte[NONCE_LENGTH];
            System.arraycopy(combined, 0, nonce, 0, NONCE_LENGTH);

            byte[] ciphertext = new byte[combined.length - NONCE_LENGTH];
            System.arraycopy(combined, NONCE_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);

        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Could not decrypt the stored secret. It was encrypted with a different "
                            + "app.security.encryption.secret, or has been tampered with. "
                            + "Reconnect the affected account to store a fresh credential.",
                    ex
            );
        }
    }

    private void requireConfigured() {

        if (!isConfigured()) {
            throw new IllegalStateException(
                    "app.security.encryption.secret is not configured (ENCRYPTION_SECRET)."
            );
        }
    }

    /**
     * SHA-256 of the configured secret, giving a 256-bit key from a
     * passphrase of any length. A per-installation salt would be better
     * still, but it would have to be stored somewhere, and the only
     * available somewhere is the database this key protects.
     */
    private static SecretKeySpec deriveKey(String secret) {

        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "app.security.encryption.secret must be at least "
                            + MIN_SECRET_LENGTH + " bytes long."
            );
        }

        try {
            return new SecretKeySpec(
                    MessageDigest.getInstance("SHA-256")
                            .digest(secret.getBytes(StandardCharsets.UTF_8)),
                    "AES"
            );

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
