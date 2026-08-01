package io.rashed.finance.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.rashed.finance.application.auth.GoogleTokenVerifier;
import io.rashed.finance.application.auth.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

/**
 * Verifies Google ID tokens against Google's public keys using the
 * official client library (keys are fetched and cached automatically).
 */
@Component
public class GoogleIdTokenVerifierAdapter implements GoogleTokenVerifier {

    private final String clientId;

    private volatile GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifierAdapter(
            @Value("${app.security.google.client-id:}") String clientId
    ) {
        this.clientId = clientId;
    }

    @Override
    public Optional<GoogleUserInfo> verify(String idToken) {

        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }

        // Resolved outside the catch on purpose: a missing client ID is a
        // server misconfiguration, not a bad token, and must not be reported
        // to the caller as a credential failure.
        GoogleIdTokenVerifier verifier = verifier();

        try {
            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = token.getPayload();

            return Optional.of(new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    Boolean.TRUE.equals(payload.getEmailVerified()),
                    (String) payload.get("name")
            ));

        } catch (GeneralSecurityException | IOException | IllegalArgumentException ex) {
            // Bad signature, wrong audience, expired, or malformed.
            return Optional.empty();
        }
    }

    private GoogleIdTokenVerifier verifier() {

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Google Sign-In is not configured: set app.security.google.client-id (GOOGLE_CLIENT_ID)."
            );
        }

        if (verifier == null) {
            synchronized (this) {
                if (verifier == null) {
                    verifier = new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            new GsonFactory()
                    )
                            .setAudience(List.of(clientId))
                            .build();
                }
            }
        }

        return verifier;
    }
}
