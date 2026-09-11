package io.rashed.finance.api.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.rashed.finance.api.dto.backup.BackupConfigurationResponse;
import io.rashed.finance.api.dto.backup.BackupDtoMapper;
import io.rashed.finance.api.dto.backup.BackupResponse;
import io.rashed.finance.api.dto.backup.CreateBackupRequest;
import io.rashed.finance.api.dto.backup.RestoreBackupRequest;
import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.ConnectGoogleDriveService;
import io.rashed.finance.application.backup.CreateBackupService;
import io.rashed.finance.application.backup.DeleteBackupService;
import io.rashed.finance.application.backup.DownloadBackupService;
import io.rashed.finance.application.backup.GoogleDriveConnection;
import io.rashed.finance.application.backup.ListBackupHistoryService;
import io.rashed.finance.application.backup.RestoreBackupService;
import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final CreateBackupService createBackupService;
    private final RestoreBackupService restoreBackupService;
    private final ListBackupHistoryService listBackupHistoryService;
    private final DownloadBackupService downloadBackupService;
    private final DeleteBackupService deleteBackupService;
    private final ConnectGoogleDriveService connectGoogleDriveService;
    private final SettingsProvider settings;

    public BackupController(
            CreateBackupService createBackupService,
            RestoreBackupService restoreBackupService,
            ListBackupHistoryService listBackupHistoryService,
            DownloadBackupService downloadBackupService,
            DeleteBackupService deleteBackupService,
            ConnectGoogleDriveService connectGoogleDriveService,
            SettingsProvider settings
    ) {
        this.createBackupService = createBackupService;
        this.restoreBackupService = restoreBackupService;
        this.listBackupHistoryService = listBackupHistoryService;
        this.downloadBackupService = downloadBackupService;
        this.deleteBackupService = deleteBackupService;
        this.connectGoogleDriveService = connectGoogleDriveService;
        this.settings = settings;
    }

    // -------------------------------------------------------------------------
    // Backup
    // -------------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupResponse create(@RequestBody(required = false) CreateBackupRequest request) {

        BackupRecord record = createBackupService.execute(
                BackupType.MANUAL,
                request == null || request.provider() == null
                        ? createBackupService.configuredProvider()
                        : request.provider(),
                request == null || request.format() == null
                        ? createBackupService.configuredFormat()
                        : request.format()
        );

        return BackupDtoMapper.toResponse(record);
    }

    @GetMapping
    public List<BackupResponse> history(
            @RequestParam(required = false) BackupOperation operation
    ) {

        return listBackupHistoryService.execute(operation)
                .stream()
                .map(BackupDtoMapper::toResponse)
                .toList();
    }

    @GetMapping("/configuration")
    public BackupConfigurationResponse configuration(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return new BackupConfigurationResponse(
                createBackupService.configuredProvider(),
                createBackupService.configuredFormat(),
                settings.getBoolean(SettingKeys.AUTO_BACKUP_ENABLED, false),
                settings.getInt(
                        SettingKeys.BACKUP_RETENTION_COUNT,
                        SettingKeys.BACKUP_RETENTION_COUNT_FALLBACK
                ),
                RestoreBackupService.CONFIRMATION,
                connectGoogleDriveService.status(UserId.of(principal.id()))
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {

        BackupArchive archive = downloadBackupService.execute(BackupId.of(id));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(archive.fileName())
                                .build()
                                .toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(archive.size())
                .body(new ByteArrayResource(archive.content()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {

        deleteBackupService.execute(BackupId.of(id));
    }

    // -------------------------------------------------------------------------
    // Restore
    // -------------------------------------------------------------------------

    /**
     * Restores a backup this application stored. Replaces existing data —
     * see {@link RestoreBackupService}.
     */
    @PostMapping("/{id}/restore")
    public BackupResponse restore(
            @PathVariable UUID id,
            @Valid @RequestBody RestoreBackupRequest request
    ) {

        return BackupDtoMapper.toResponse(
                restoreBackupService.restoreFromHistory(BackupId.of(id), request.confirmation())
        );
    }

    /**
     * Restores from an uploaded archive — one downloaded from Drive by
     * hand, or copied from another machine.
     */
    @PostMapping("/restore")
    public BackupResponse restoreFromUpload(
            @RequestPart("file") MultipartFile file,
            // @RequestParam, not @RequestPart: a browser sends plain
            // FormData string fields without a Content-Type, which leaves
            // @RequestPart without a converter to pick and yields a 415.
            @RequestParam("confirmation") String confirmation
    ) {

        if (file.isEmpty()) {
            throw new TransactionValidationException("The uploaded archive is empty.");
        }

        try {
            return BackupDtoMapper.toResponse(
                    restoreBackupService.restoreFromUpload(
                            file.getOriginalFilename(),
                            file.getBytes(),
                            confirmation
                    )
            );

        } catch (IOException ex) {
            throw new BackupFailedException("Could not read the uploaded archive.", ex);
        }
    }

    // -------------------------------------------------------------------------
    // Google Drive
    // -------------------------------------------------------------------------

    /**
     * Starts the Drive authorization flow.
     *
     * Returns the Google consent URL rather than redirecting: the caller
     * is an XHR from the SPA, and a 302 to Google would be followed by
     * the browser inside the fetch instead of navigating the page.
     */
    @PostMapping("/google-drive/authorize")
    public AuthorizationUrlResponse authorizeGoogleDrive(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return new AuthorizationUrlResponse(
                connectGoogleDriveService.beginAuthorization(UserId.of(principal.id()))
        );
    }

    /**
     * Completes the flow with the code Google handed back.
     *
     * The frontend's callback route forwards {@code code} and
     * {@code state} here. It is a POST because it stores a credential,
     * and it is not idempotent — the code is single-use.
     */
    @PostMapping("/google-drive/callback")
    public GoogleDriveConnection completeGoogleDrive(
            @RequestParam String code,
            @RequestParam String state
    ) {

        return connectGoogleDriveService.completeAuthorization(code, state);
    }

    @GetMapping("/google-drive")
    public GoogleDriveConnection googleDriveStatus(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return connectGoogleDriveService.status(UserId.of(principal.id()));
    }

    @DeleteMapping("/google-drive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnectGoogleDrive(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        connectGoogleDriveService.disconnect(UserId.of(principal.id()));
    }

    /**
     * @param authorizationUrl Google consent URL for the browser to visit.
     */
    public record AuthorizationUrlResponse(String authorizationUrl) {
    }
}
