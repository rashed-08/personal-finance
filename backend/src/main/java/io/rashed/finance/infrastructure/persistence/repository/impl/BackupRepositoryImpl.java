package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;
import io.rashed.finance.infrastructure.persistence.mapper.BackupHistoryEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.BackupHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BackupRepositoryImpl implements BackupRepository {

    private final BackupHistoryJpaRepository jpaRepository;

    @Override
    public BackupRecord save(BackupRecord record) {

        return BackupHistoryEntityMapper.toDomain(
                jpaRepository.save(
                        BackupHistoryEntityMapper.toEntity(record)
                )
        );
    }

    @Override
    public Optional<BackupRecord> findById(BackupId id) {

        return jpaRepository.findById(id.getValue())
                .map(BackupHistoryEntityMapper::toDomain);
    }

    @Override
    public List<BackupRecord> findAll() {

        return jpaRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(BackupHistoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<BackupRecord> findByOperation(BackupOperation operation) {

        return jpaRepository.findByOperationOrderByStartedAtDesc(operation)
                .stream()
                .map(BackupHistoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<BackupRecord> findCompletedAutomaticBackups() {

        return jpaRepository
                .findByOperationAndBackupTypeAndStatusOrderByStartedAtDesc(
                        BackupOperation.BACKUP,
                        BackupType.AUTOMATIC,
                        BackupStatus.COMPLETED
                )
                .stream()
                .map(BackupHistoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(BackupId id) {

        jpaRepository.deleteById(id.getValue());
    }
}
