package io.rashed.finance.domain.backup;

import java.time.LocalDateTime;
import java.util.Objects;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * One entry in the backup/restore audit trail.
 *
 * The record is written before the work starts, in {@code IN_PROGRESS},
 * and closed out as {@code COMPLETED} or {@code FAILED}. That ordering is
 * the point: a crash mid-backup leaves an {@code IN_PROGRESS} row that
 * says something was attempted, which a record written only on success
 * could never do.
 *
 * History is append-only and failures are kept — see
 * docs/database/tables/backup_history.md.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class BackupRecord {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final BackupId id;

    private final BackupOperation operation;

    private final BackupType backupType;

    private final BackupProvider provider;

    private final BackupFormat format;

    private final String fileName;

    /**
     * Where the archive lives: a filesystem path for LOCAL, null for a
     * provider that addresses objects by id instead.
     */
    private final String filePath;

    /**
     * Provider-assigned object id — the Drive file id for GOOGLE_DRIVE,
     * null for LOCAL. Needed to fetch or delete the archive later.
     */
    private final String storageReference;

    private final Long fileSize;

    /**
     * SHA-256 of the archive bytes, used to detect corruption before a
     * restore is allowed to touch the database.
     */
    private final String checksum;

    private final BackupStatus status;

    private final String errorMessage;

    private final LocalDateTime startedAt;

    private final LocalDateTime completedAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public BackupRecord(
            BackupId id,
            BackupOperation operation,
            BackupType backupType,
            BackupProvider provider,
            BackupFormat format,
            String fileName,
            String filePath,
            String storageReference,
            Long fileSize,
            String checksum,
            BackupStatus status,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.operation = Objects.requireNonNull(operation, "Backup operation is required.");
        this.backupType = Objects.requireNonNull(backupType, "Backup type is required.");
        this.provider = Objects.requireNonNull(provider, "Backup provider is required.");
        this.format = Objects.requireNonNull(format, "Backup format is required.");
        this.fileName = requireFileName(fileName);
        this.status = Objects.requireNonNull(status, "Backup status is required.");
        this.startedAt = Objects.requireNonNull(startedAt, "Start time is required.");

        this.filePath = filePath;
        this.storageReference = storageReference;
        this.fileSize = requireNonNegative(fileSize);
        this.checksum = checksum;
        this.errorMessage = truncate(errorMessage);
        this.completedAt = requireNotBeforeStart(completedAt, startedAt);

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    /**
     * Opens an audit record for a backup that is about to run.
     */
    public static BackupRecord startBackup(
            BackupType backupType,
            BackupProvider provider,
            BackupFormat format,
            String fileName
    ) {

        return start(BackupOperation.BACKUP, backupType, provider, format, fileName);
    }

    /**
     * Opens an audit record for a restore that is about to run. A restore
     * is always {@link BackupType#MANUAL}: nothing schedules one.
     */
    public static BackupRecord startRestore(
            BackupProvider provider,
            BackupFormat format,
            String fileName
    ) {

        return start(BackupOperation.RESTORE, BackupType.MANUAL, provider, format, fileName);
    }

    private static BackupRecord start(
            BackupOperation operation,
            BackupType backupType,
            BackupProvider provider,
            BackupFormat format,
            String fileName
    ) {

        LocalDateTime now = LocalDateTime.now();

        return new BackupRecord(
                BackupId.newId(),
                operation,
                backupType,
                provider,
                format,
                fileName,
                null,
                null,
                null,
                null,
                BackupStatus.IN_PROGRESS,
                null,
                now,
                null,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static String requireFileName(String fileName) {

        Objects.requireNonNull(fileName, "Backup file name is required.");

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("Backup file name cannot be empty.");
        }

        if (fileName.length() > 255) {
            throw new IllegalArgumentException("Backup file name cannot exceed 255 characters.");
        }

        return fileName;
    }

    private static Long requireNonNegative(Long fileSize) {

        if (fileSize != null && fileSize < 0) {
            throw new IllegalArgumentException("Backup file size cannot be negative.");
        }

        return fileSize;
    }

    private static LocalDateTime requireNotBeforeStart(LocalDateTime completedAt, LocalDateTime startedAt) {

        if (completedAt != null && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Backup cannot complete before it started.");
        }

        return completedAt;
    }

    /**
     * A provider error message can be arbitrarily long (stack traces,
     * HTML error pages); the column holds 2000 characters and the useful
     * part is at the front.
     */
    private static String truncate(String errorMessage) {

        if (errorMessage == null) {
            return null;
        }

        return errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isBackup() {
        return operation == BackupOperation.BACKUP;
    }

    public boolean isRestore() {
        return operation == BackupOperation.RESTORE;
    }

    public boolean isInProgress() {
        return status == BackupStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == BackupStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == BackupStatus.FAILED;
    }

    public boolean isAutomatic() {
        return backupType == BackupType.AUTOMATIC;
    }

    /**
     * Records where the archive ended up and marks the operation done.
     *
     * @throws IllegalStateException if the record was already closed —
     *         history is append-only, so a second outcome for one
     *         operation is a bug in the calling service.
     */
    public BackupRecord complete(
            String filePath,
            String storageReference,
            long fileSize,
            String checksum
    ) {

        requireOpen();

        return new BackupRecord(
                id,
                operation,
                backupType,
                provider,
                format,
                fileName,
                filePath,
                storageReference,
                fileSize,
                checksum,
                BackupStatus.COMPLETED,
                null,
                startedAt,
                LocalDateTime.now(),
                createdAt,
                LocalDateTime.now()
        );
    }

    public BackupRecord fail(String errorMessage) {

        requireOpen();

        return new BackupRecord(
                id,
                operation,
                backupType,
                provider,
                format,
                fileName,
                filePath,
                storageReference,
                fileSize,
                checksum,
                BackupStatus.FAILED,
                errorMessage,
                startedAt,
                LocalDateTime.now(),
                createdAt,
                LocalDateTime.now()
        );
    }

    private void requireOpen() {

        if (!isInProgress()) {
            throw new IllegalStateException(
                    "Backup history is append-only: this record is already " + status + ".");
        }
    }

    public boolean hasChecksum() {
        return checksum != null && !checksum.isBlank();
    }

    /**
     * Whether the archive this record describes still exists.
     *
     * False once it has been deleted or pruned, and for a restore, which
     * consumes an archive rather than producing one.
     */
    public boolean hasStoredArchive() {

        return (filePath != null && !filePath.isBlank())
                || (storageReference != null && !storageReference.isBlank());
    }

    /**
     * Records that the archive is gone, keeping the entry itself.
     *
     * Clearing the location is what makes a deletion observable: the entry
     * survives — with its name, size, checksum and timestamps, so it stays
     * distinguishable from a backup that never happened — but it no longer
     * claims to have a file behind it, and
     * {@link #hasStoredArchive()} stops offering it for download or
     * restore.
     *
     * Not guarded by {@code requireOpen()}: unlike
     * {@link #complete} and {@link #fail} this is not a second outcome for
     * the operation, it is the later fate of its artefact. Idempotent, so
     * deleting twice is not an error.
     */
    public BackupRecord archiveRemoved() {

        if (!hasStoredArchive()) {
            return this;
        }

        return new BackupRecord(
                id,
                operation,
                backupType,
                provider,
                format,
                fileName,
                null,
                null,
                fileSize,
                checksum,
                status,
                errorMessage,
                startedAt,
                completedAt,
                createdAt,
                LocalDateTime.now()
        );
    }

    public boolean isStoredLocally() {
        return provider == BackupProvider.LOCAL;
    }
}
