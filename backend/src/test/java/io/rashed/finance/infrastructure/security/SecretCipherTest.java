package io.rashed.finance.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretCipherTest {

    private static final String SECRET =
            "test-only-encryption-secret-at-least-32-bytes-long";

    private static final String REFRESH_TOKEN = "1//0gk-example-google-refresh-token";

    @Test
    void roundTripsASecret() {

        SecretCipher cipher = new SecretCipher(SECRET);

        assertEquals(REFRESH_TOKEN, cipher.decrypt(cipher.encrypt(REFRESH_TOKEN)));
    }

    @Test
    void encryptingTwiceProducesDifferentCiphertext() {

        // A fresh nonce per encryption, so identical plaintext does not
        // produce identical ciphertext.
        SecretCipher cipher = new SecretCipher(SECRET);

        assertNotEquals(cipher.encrypt(REFRESH_TOKEN), cipher.encrypt(REFRESH_TOKEN));
    }

    @Test
    void ciphertextDoesNotContainThePlaintext() {

        assertFalse(new SecretCipher(SECRET).encrypt(REFRESH_TOKEN).contains(REFRESH_TOKEN));
    }

    @Test
    void decryptingWithADifferentKeyIsRejected() {

        String encrypted = new SecretCipher(SECRET).encrypt(REFRESH_TOKEN);

        SecretCipher other = new SecretCipher(
                "a-completely-different-secret-also-32-bytes-plus");

        assertThrows(IllegalStateException.class, () -> other.decrypt(encrypted));
    }

    @Test
    void tamperedCiphertextIsRejected() {

        // GCM authenticates, so a modified archive of the credential is
        // detected rather than decrypting to garbage that gets sent to
        // Google.
        SecretCipher cipher = new SecretCipher(SECRET);

        String encrypted = cipher.encrypt(REFRESH_TOKEN);

        char[] characters = encrypted.toCharArray();
        characters[characters.length - 2] = characters[characters.length - 2] == 'A' ? 'B' : 'A';

        String tampered = new String(characters);

        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));
    }

    @Test
    void garbageIsRejected() {

        SecretCipher cipher = new SecretCipher(SECRET);

        assertThrows(IllegalStateException.class, () -> cipher.decrypt("not base64 at all !!"));
        assertThrows(IllegalStateException.class, () -> cipher.decrypt("c2hvcnQ="));
    }

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    @Test
    void isConfigured_reflectsWhetherAKeyWasSupplied() {

        assertTrue(new SecretCipher(SECRET).isConfigured());
        assertFalse(new SecretCipher(null).isConfigured());
        assertFalse(new SecretCipher("   ").isConfigured());
    }

    @Test
    void anUnconfiguredCipherConstructsButRefusesToWork() {

        // The application must still boot; only the features that need
        // reversible secrets are unavailable.
        SecretCipher cipher = new SecretCipher(null);

        assertThrows(IllegalStateException.class, () -> cipher.encrypt(REFRESH_TOKEN));
        assertThrows(IllegalStateException.class, () -> cipher.decrypt("anything"));
    }

    @Test
    void aShortKeyIsRejectedAtConstruction() {

        assertThrows(IllegalStateException.class, () -> new SecretCipher("too-short"));
    }
}
