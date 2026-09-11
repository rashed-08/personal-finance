package io.rashed.finance.infrastructure.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.BackupFileNames;
import io.rashed.finance.application.backup.BackupStorage;
import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.domain.users.GoogleOAuthScope;
import io.rashed.finance.domain.users.GoogleOAuthToken;
import io.rashed.finance.domain.users.GoogleOAuthTokenRepository;
import io.rashed.finance.infrastructure.security.GoogleOAuthProperties;
import io.rashed.finance.infrastructure.security.SecretCipher;

/**
 * Stores archives in Google Drive.
 *
 * Files go under {@code <folder>/backups/<year>/<month>/}, matching the
 * layout in docs/database/tables/backup_history.md. Folders are created
 * on demand, which the {@code drive.file} scope permits because the
 * application created them itself.
 *
 * <h2>This uploads your financial data to Google</h2>
 * An archive holds the whole ledger. Connecting Drive is therefore an
 * explicit, revocable act: nothing is uploaded until a grant exists and
 * {@code BACKUP_PROVIDER} names GOOGLE_DRIVE.
 */
@Component
public class GoogleDriveBackupStorage implements BackupStorage {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveBackupStorage.class);

    static final String NOT_CONFIGURED_MESSAGE =
            "Google Drive backup is not configured on the server. Set GOOGLE_CLIENT_ID, "
                    + "GOOGLE_CLIENT_SECRET and GOOGLE_DRIVE_REDIRECT_URI, then restart.";

    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private static final String ARCHIVE_MIME_TYPE = "application/octet-stream";

    private static final String APPLICATION_NAME = "Personal Finance";

    private final GoogleOAuthTokenRepository tokenRepository;
    private final GoogleOAuthProperties properties;
    private final SecretCipher cipher;
    private final SettingsProvider settings;

    public GoogleDriveBackupStorage(
            GoogleOAuthTokenRepository tokenRepository,
            GoogleOAuthProperties properties,
            SecretCipher cipher,
            SettingsProvider settings
    ) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.properties = Objects.requireNonNull(properties);
        this.cipher = Objects.requireNonNull(cipher);
        this.settings = Objects.requireNonNull(settings);
    }

    @Override
    public BackupProvider provider() {
        return BackupProvider.GOOGLE_DRIVE;
    }

    @Override
    public boolean isAvailable() {
        return unavailableReasonOrNull() == null;
    }

    @Override
    public String unavailableReason() {

        String reason = unavailableReasonOrNull();

        if (reason == null) {
            throw new IllegalStateException("Google Drive is available.");
        }

        return reason;
    }

    private String unavailableReasonOrNull() {

        if (!properties.isDriveConfigured()) {
            return NOT_CONFIGURED_MESSAGE;
        }

        if (!cipher.isConfigured()) {
            return "Google Drive backup needs app.security.encryption.secret (ENCRYPTION_SECRET) "
                    + "so the Drive credential can be stored encrypted.";
        }

        GoogleOAuthToken token = tokenRepository.findAnyByScope(GoogleOAuthScope.DRIVE)
                .orElse(null);

        if (token == null) {
            return "Google Drive is not connected. Connect it from Settings to enable Drive backups.";
        }

        if (!token.grants(DriveScopes.DRIVE_FILE)) {
            return "The Google account was connected without permission to manage backup files. "
                    + "Reconnect and accept the Drive permission.";
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Storage
    // -------------------------------------------------------------------------

    @Override
    public StoredArchive store(BackupArchive archive) {

        Drive drive = drive();

        LocalDate today = LocalDate.now();

        String folderId = folderPath(
                drive,
                settings.getString(
                        SettingKeys.GOOGLE_DRIVE_FOLDER_NAME,
                        SettingKeys.GOOGLE_DRIVE_FOLDER_NAME_FALLBACK
                ),
                "backups",
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue())
        );

        File metadata = new File()
                .setName(archive.fileName())
                .setParents(List.of(folderId));

        try {
            File uploaded = drive.files()
                    .create(metadata, new ByteArrayContent(ARCHIVE_MIME_TYPE, archive.content()))
                    .setFields("id, name, webViewLink")
                    .execute();

            log.info(
                    "Uploaded backup {} to Google Drive (file id {})",
                    archive.fileName(), uploaded.getId()
            );

            return new StoredArchive(uploaded.getWebViewLink(), uploaded.getId());

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not upload the backup to Google Drive: " + message(ex), ex);
        }
    }

    @Override
    public BackupArchive retrieve(StoredArchiveLocation location) {

        String fileId = requireFileId(location);

        Drive drive = drive();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            drive.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(buffer);

            return BackupArchive.of(
                    location.fileName(),
                    BackupFileNames.formatOf(location.fileName()),
                    buffer.toByteArray()
            );

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not download the backup from Google Drive: " + message(ex), ex);
        }
    }

    @Override
    public void delete(StoredArchiveLocation location) {

        String fileId = requireFileId(location);

        try {
            drive().files().delete(fileId).execute();

            log.info("Deleted Google Drive file {}", fileId);

        } catch (GoogleJsonResponseException ex) {

            if (ex.getStatusCode() == 404) {
                // Already gone, which is the outcome being asked for.
                log.info("Google Drive file {} was already deleted.", fileId);
                return;
            }

            throw new BackupFailedException(
                    "Could not delete the backup from Google Drive: " + message(ex), ex);

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not delete the backup from Google Drive: " + message(ex), ex);
        }
    }

    private String requireFileId(StoredArchiveLocation location) {

        if (location.storageReference() == null || location.storageReference().isBlank()) {
            throw new BackupFailedException(
                    "This history entry has no Google Drive file id, so the archive cannot be "
                            + "located. Download it from Drive and restore from the uploaded file.");
        }

        return location.storageReference();
    }

    // -------------------------------------------------------------------------
    // Drive client
    // -------------------------------------------------------------------------

    /**
     * A Drive client authorized as the connected account.
     *
     * Built per call rather than cached: {@link UserCredentials} refreshes
     * its own access token, but the stored grant can be replaced or
     * revoked at any time, and a cached client would keep using the old
     * one. Construction is local work — no network call happens until a
     * request is made.
     */
    private Drive drive() {

        String reason = unavailableReasonOrNull();

        if (reason != null) {
            throw new BackupFailedException(reason);
        }

        GoogleOAuthToken token = tokenRepository.findAnyByScope(GoogleOAuthScope.DRIVE)
                .orElseThrow(() -> new BackupFailedException("Google Drive is not connected."));

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(properties.clientId())
                .setClientSecret(properties.clientSecret())
                .setRefreshToken(cipher.decrypt(token.getEncryptedRefreshToken()))
                .build();

        return new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Walks (creating as needed) a chain of nested folders and returns the
     * id of the last one.
     */
    private String folderPath(Drive drive, String... names) {

        String parentId = "root";

        for (String name : names) {
            parentId = folder(drive, name, parentId);
        }

        return parentId;
    }

    private String folder(Drive drive, String name, String parentId) {

        try {
            // Only folders this application created are visible under the
            // drive.file scope, which is exactly the set worth reusing.
            FileList matches = drive.files()
                    .list()
                    .setQ(
                            "mimeType = '" + FOLDER_MIME_TYPE + "'"
                                    + " and name = '" + escape(name) + "'"
                                    + " and '" + parentId + "' in parents"
                                    + " and trashed = false"
                    )
                    .setFields("files(id, name)")
                    .setPageSize(1)
                    .execute();

            if (matches.getFiles() != null && !matches.getFiles().isEmpty()) {
                return matches.getFiles().getFirst().getId();
            }

            File created = drive.files()
                    .create(
                            new File()
                                    .setName(name)
                                    .setMimeType(FOLDER_MIME_TYPE)
                                    .setParents(List.of(parentId))
                    )
                    .setFields("id")
                    .execute();

            log.info("Created Google Drive folder {}", name);

            return created.getId();

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not prepare the Google Drive folder '" + name + "': " + message(ex), ex);
        }
    }

    /**
     * Escapes a folder name for a Drive query string, where values are
     * single-quoted. Folder names come from settings, so a stray quote is
     * a broken query rather than an attack — but a broken query is still
     * worth avoiding.
     */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * Drive reports the useful part of a failure in the JSON body; the
     * exception message alone is often just the status line.
     */
    private static String message(IOException ex) {

        if (ex instanceof GoogleJsonResponseException jsonException
                && jsonException.getDetails() != null
                && jsonException.getDetails().getMessage() != null) {

            return jsonException.getDetails().getMessage();
        }

        return ex.getMessage();
    }
}
