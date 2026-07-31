package io.rashed.finance.application.auth;

import java.util.Optional;

/**
 * Verifies a Google ID token (signature, audience, expiry) and extracts
 * its identity claims. Implemented in the infrastructure layer.
 */
public interface GoogleTokenVerifier {

    Optional<GoogleUserInfo> verify(String idToken);
}
