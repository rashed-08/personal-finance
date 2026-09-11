package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "backup_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackupHistoryEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20, updatable = false)
    private BackupOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_type", nullable = false, length = 20, updatable = false)
    private BackupType backupType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private BackupProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_format", nullable = false, length = 20, updatable = false)
    private BackupFormat format;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", columnDefinition = "text")
    private String filePath;

    @Column(name = "storage_reference", length = 255)
    private String storageReference;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_status", nullable = false, length = 20)
    private BackupStatus status;

    @Column(length = 128)
    private String checksum;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "backup_started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "backup_completed_at")
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public BackupHistoryEntity(
            UUID id,
            BackupOperation operation,
            BackupType backupType,
            BackupProvider provider,
            BackupFormat format,
            String fileName,
            String filePath,
            String storageReference,
            Long fileSize,
            BackupStatus status,
            String checksum,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.operation = operation;
        this.backupType = backupType;
        this.provider = provider;
        this.format = format;
        this.fileName = fileName;
        this.filePath = filePath;
        this.storageReference = storageReference;
        this.fileSize = fileSize;
        this.status = status;
        this.checksum = checksum;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
