package io.rashed.finance.application.backup;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import io.rashed.finance.common.enums.BackupProvider;

/**
 * Resolves a {@link BackupProvider} to its storage implementation.
 *
 * {@code S3} is a declared provider with no implementation yet, so an
 * unregistered provider is reported as unconfigured rather than treated
 * as a bug.
 */
@Component
public class BackupStorages {

    private final Map<BackupProvider, BackupStorage> storages =
            new EnumMap<>(BackupProvider.class);

    public BackupStorages(List<BackupStorage> storages) {

        for (BackupStorage storage : storages) {
            this.storages.put(storage.provider(), storage);
        }
    }

    public BackupStorage get(BackupProvider provider) {

        Objects.requireNonNull(provider, "Backup provider cannot be null.");

        BackupStorage storage = storages.get(provider);

        if (storage == null) {
            throw new BackupFailedException(
                    "Backup provider " + provider + " is not supported by this build.");
        }

        return storage;
    }

    /**
     * The same lookup, but refusing a provider that is registered yet not
     * usable — an unconnected Google Drive, an unwritable directory. Use
     * this before opening a history record.
     */
    public BackupStorage requireAvailable(BackupProvider provider) {

        BackupStorage storage = get(provider);

        if (!storage.isAvailable()) {
            throw new BackupFailedException(storage.unavailableReason());
        }

        return storage;
    }
}
