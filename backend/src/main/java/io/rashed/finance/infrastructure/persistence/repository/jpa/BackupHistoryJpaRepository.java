package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.infrastructure.persistence.entity.BackupHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BackupHistoryJpaRepository extends JpaRepository<BackupHistoryEntity, UUID> {

    List<BackupHistoryEntity> findAllByOrderByStartedAtDesc();

    List<BackupHistoryEntity> findByOperationOrderByStartedAtDesc(BackupOperation operation);

    List<BackupHistoryEntity> findByOperationAndBackupTypeAndStatusOrderByStartedAtDesc(
            BackupOperation operation,
            BackupType backupType,
            BackupStatus status
    );
}
