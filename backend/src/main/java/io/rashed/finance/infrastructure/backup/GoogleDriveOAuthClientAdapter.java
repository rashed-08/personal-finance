package io.rashed.finance.infrastructure.backup;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.GoogleOAuthClient;
import io.rashed.finance.infrastructure.security.GoogleOAuthProperties;

/**
 * Runs the Google authorization-code flow with the official client
 * library.
 */
@Component
public class GoogleDriveOAuthClientAdapter implements GoogleOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveOAuthClientAdapter.class);

    private static final String REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke";

    /**
     * {@code openid email} rides along so the token response carries an ID
     * token, which is how the connected account's address is learned
     * without needing a broader Drive scope to call about.get.
     */
    private static final List<String> SCOPES = List.of(DRIVE_FILE_SCOPE, "openid", "email");

    private final GoogleOAuthProperties properties;

    private final NetHttpTransport transport = new NetHttpTransport();
    private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GoogleDriveOAuthClientAdapter(GoogleOAuthProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public String authorizationUrl(String state) {

        requireConfigured();

        return new GoogleAuthorizationCodeRequestUrl(
                properties.clientId(),
                properties.driveRedirectUri(),
                SCOPES
        )
                // offline: ask for a refresh token, without which a
                // scheduled backup could never run unattended.
                .setAccessType("offline")
                // consent: Google issues a refresh token only on first
                // authorization unless re-consent is forced. Without this,
                // reconnecting yields no refresh token and the grant is
                // useless.
                .setApprovalPrompt("force")
                .setState(state)
                .build();
    }

    @Override
    public GoogleTokens exchangeCode(String code) {

        requireConfigured();

        if (code == null || code.isBlank()) {
            throw new BackupFailedException("Google returned no authorization code.");
        }

        try {
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    transport,
                    jsonFactory,
                    properties.clientId(),
                    properties.clientSecret(),
                    code,
                    properties.driveRedirectUri()
            ).execute();

            if (response.getRefreshToken() == null || response.getRefreshToken().isBlank()) {
                throw new BackupFailedException(
                        "Google did not return a refresh token. Remove this application's access "
                                + "at myaccount.google.com/permissions and connect again."
                );
            }

            return new GoogleTokens(
                    response.getRefreshToken(),
                    response.getScope(),
                    response.getIdToken()
            );

        } catch (HttpResponseException ex) {
            // The body names the actual problem (redirect_uri_mismatch,
            // invalid_client, …); the status alone never does.
            throw new BackupFailedException(
                    "Google rejected the authorization code: " + ex.getStatusCode()
                            + " " + ex.getContent(), ex);

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not reach Google to exchange the authorization code: "
                            + ex.getMessage(), ex);
        }
    }

    @Override
    public void revoke(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            GenericUrl url = new GenericUrl(REVOKE_ENDPOINT);
            url.set("token", refreshToken);

            transport.createRequestFactory()
                    .buildPostRequest(url, null)
                    .execute()
                    .disconnect();

            log.info("Revoked the Google Drive grant at Google.");

        } catch (IOException ex) {
            // Already-revoked and expired tokens both land here. The local
            // credential is deleted regardless, so this is informational.
            log.warn(
                    "Could not revoke the Google Drive grant at Google ({}). "
                            + "The stored credential has been deleted locally; remove the app at "
                            + "myaccount.google.com/permissions to complete the revocation.",
                    ex.getMessage()
            );
        }
    }

    private void requireConfigured() {

        if (!properties.isDriveConfigured()) {
            throw new BackupFailedException(GoogleDriveBackupStorage.NOT_CONFIGURED_MESSAGE);
        }
    }
}
