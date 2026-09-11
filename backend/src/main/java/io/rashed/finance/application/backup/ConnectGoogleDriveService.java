package io.rashed.finance.application.backup;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.rashed.finance.application.auth.GoogleTokenVerifier;
import io.rashed.finance.application.auth.OpaqueTokenGenerator;
import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.domain.users.GoogleOAuthScope;
import io.rashed.finance.domain.users.GoogleOAuthToken;
import io.rashed.finance.domain.users.GoogleOAuthTokenRepository;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.security.SecretCipher;

/**
 * Manages the Google Drive link: start consent, finish it, report it,
 * drop it.
 *
 * Not final: {@code @Transactional} is applied through a CGLIB proxy,
 * which subclasses this type.
 */
@Service
public class ConnectGoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(ConnectGoogleDriveService.class);

    /**
     * How long a started authorization may sit unfinished. Long enough to
     * read a consent screen, short enough that an abandoned attempt cannot
     * be completed later.
     */
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final GoogleOAuthClient oauthClient;
    private final GoogleTokenVerifier tokenVerifier;
    private final GoogleOAuthTokenRepository tokenRepository;
    private final SecretCipher cipher;
    private final SettingsProvider settings;

    /**
     * Pending {@code state} values, each mapped to the user who started
     * the flow and when.
     *
     * In memory on purpose. The value is single-use, expires in minutes,
     * and a restart mid-consent simply means connecting again — none of
     * which justifies a table. It does mean the flow must complete against
     * the same instance, which is true of this single-node application.
     */
    private final Map<String, PendingAuthorization> pending = new ConcurrentHashMap<>();

    public ConnectGoogleDriveService(
            GoogleOAuthClient oauthClient,
            GoogleTokenVerifier tokenVerifier,
            GoogleOAuthTokenRepository tokenRepository,
            SecretCipher cipher,
            SettingsProvider settings
    ) {
        this.oauthClient = Objects.requireNonNull(oauthClient);
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier);
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.cipher = Objects.requireNonNull(cipher);
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Begins authorization and returns the Google URL to send the user to.
     *
     * The returned {@code state} must come back on the callback; it is
     * what stops a third party from completing a connection on this
     * user's behalf (OAuth 2.0 CSRF).
     */
    public String beginAuthorization(UserId userId) {

        Objects.requireNonNull(userId, "User cannot be null.");

        requireEncryptionConfigured();

        expireStaleStates();

        String state = OpaqueTokenGenerator.generate();

        pending.put(state, new PendingAuthorization(userId, Instant.now()));

        return oauthClient.authorizationUrl(state);
    }

    /**
     * Completes authorization and stores the grant.
     *
     * @throws BackupFailedException if {@code state} is unknown or expired,
     *         or Google rejects the code.
     */
    @Transactional
    public GoogleDriveConnection completeAuthorization(String code, String state) {

        requireEncryptionConfigured();

        UserId userId = consume(state);

        GoogleOAuthClient.GoogleTokens tokens = oauthClient.exchangeCode(code);

        String accountEmail = emailFrom(tokens.idToken());

        String encrypted = cipher.encrypt(tokens.refreshToken());

        GoogleOAuthToken stored = tokenRepository
                .findByUserAndScope(userId, GoogleOAuthScope.DRIVE)
                .map(existing -> existing.reconnect(encrypted, tokens.grantedScopes(), accountEmail))
                .orElseGet(() -> GoogleOAuthToken.connect(
                        userId, GoogleOAuthScope.DRIVE, encrypted, tokens.grantedScopes(), accountEmail));

        tokenRepository.save(stored);

        log.info("Connected Google Drive for backups (account {})", accountEmail);

        if (!stored.grants(GoogleOAuthClient.DRIVE_FILE_SCOPE)) {
            return new GoogleDriveConnection(
                    true,
                    false,
                    accountEmail,
                    folderName(),
                    "The Drive permission was not granted. Reconnect and accept it."
            );
        }

        return GoogleDriveConnection.connected(accountEmail, folderName());
    }

    public GoogleDriveConnection status(UserId userId) {

        if (!cipher.isConfigured()) {
            return GoogleDriveConnection.notConfigured(
                    "Google Drive backup needs app.security.encryption.secret (ENCRYPTION_SECRET) "
                            + "so the Drive credential can be stored encrypted.");
        }

        Optional<GoogleOAuthToken> token =
                tokenRepository.findByUserAndScope(userId, GoogleOAuthScope.DRIVE);

        if (token.isEmpty()) {
            return GoogleDriveConnection.disconnected(folderName());
        }

        return GoogleDriveConnection.connected(token.get().getAccountEmail(), folderName());
    }

    /**
     * Drops the grant locally and revokes it at Google.
     *
     * Local deletion happens first: if revocation fails, the application
     * has still forgotten the credential, which is the outcome the user
     * asked for.
     */
    @Transactional
    public void disconnect(UserId userId) {

        Optional<GoogleOAuthToken> token =
                tokenRepository.findByUserAndScope(userId, GoogleOAuthScope.DRIVE);

        if (token.isEmpty()) {
            return;
        }

        String refreshToken = null;

        if (cipher.isConfigured()) {
            try {
                refreshToken = cipher.decrypt(token.get().getEncryptedRefreshToken());

            } catch (IllegalStateException ex) {
                // Undecryptable, e.g. the encryption key changed. Nothing
                // can be revoked, but the row must still go.
                log.warn("Stored Drive credential could not be decrypted for revocation: {}",
                        ex.getMessage());
            }
        }

        tokenRepository.deleteByUserAndScope(userId, GoogleOAuthScope.DRIVE);

        if (refreshToken != null) {
            oauthClient.revoke(refreshToken);
        }

        log.info("Disconnected Google Drive for backups.");
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private void requireEncryptionConfigured() {

        if (!cipher.isConfigured()) {
            throw new BackupFailedException(
                    "Google Drive backup needs app.security.encryption.secret (ENCRYPTION_SECRET) "
                            + "so the Drive credential can be stored encrypted. "
                            + "Generate one with: openssl rand -base64 48"
            );
        }
    }

    /**
     * Validates and removes a pending state, so a code can be redeemed
     * once and only once.
     */
    private UserId consume(String state) {

        expireStaleStates();

        if (state == null || state.isBlank()) {
            throw new BackupFailedException("Google returned no state parameter.");
        }

        PendingAuthorization authorization = pending.remove(state);

        if (authorization == null) {
            throw new BackupFailedException(
                    "This Google authorization is unknown or has expired. Start again from Settings.");
        }

        return authorization.userId();
    }

    private void expireStaleStates() {

        Instant cutoff = Instant.now().minus(STATE_TTL);

        pending.entrySet().removeIf(entry -> entry.getValue().startedAt().isBefore(cutoff));
    }

    /**
     * Reads the account address from the ID token Google returned.
     *
     * Verified rather than merely decoded: it is cheap, it is already
     * implemented for Sign-In, and an unverified token is not evidence of
     * anything. A null result is tolerated — the address is for display,
     * and losing it must not fail a working connection.
     */
    private String emailFrom(String idToken) {

        if (idToken == null || idToken.isBlank()) {
            return null;
        }

        return tokenVerifier.verify(idToken)
                .map(info -> info.email())
                .orElse(null);
    }

    private String folderName() {

        return settings.getString(
                SettingKeys.GOOGLE_DRIVE_FOLDER_NAME,
                SettingKeys.GOOGLE_DRIVE_FOLDER_NAME_FALLBACK
        );
    }

    private record PendingAuthorization(UserId userId, Instant startedAt) {
    }
}
