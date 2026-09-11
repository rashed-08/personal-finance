package io.rashed.finance.application.backup;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * The backup and restore audit trail, newest first.
 */
@Service
public class ListBackupHistoryService {

    private final BackupRepository repository;

    public ListBackupHistoryService(BackupRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * @param operation restrict to backups or restores; null for both.
     */
    public List<BackupRecord> execute(BackupOperation operation) {

        return operation == null
                ? repository.findAll()
                : repository.findByOperation(operation);
    }
}
