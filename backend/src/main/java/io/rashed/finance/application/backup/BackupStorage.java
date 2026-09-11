package io.rashed.finance.application.backup;

import java.util.Objects;

import io.rashed.finance.common.enums.BackupProvider;

/**
 * Where archives are kept. One implementation per
 * {@link BackupProvider}, resolved by {@link BackupStorages}.
 */
public interface BackupStorage {

    BackupProvider provider();

    /**
     * Whether this provider is usable right now. Google Drive, for
     * instance, is only usable once an account has been connected, and
     * the caller should refuse the backup up front rather than open a
     * history record it is certain to fail.
     */
    boolean isAvailable();

    /**
     * Human-readable reason {@link #isAvailable()} is false, for the API
     * to relay. Never called when the provider is available.
     */
    String unavailableReason();

    /**
     * @throws BackupFailedException if the archive cannot be stored.
     */
    StoredArchive store(BackupArchive archive);

    /**
     * Reads an archive back.
     *
     * @param location where the archive was stored, as recorded in the
     *                 history entry.
     * @throws BackupFailedException if the archive is missing or unreadable.
     */
    BackupArchive retrieve(StoredArchiveLocation location);

    /**
     * Removes a stored archive. A missing archive is not an error: the
     * point of deleting is that it is gone.
     */
    void delete(StoredArchiveLocation location);

    /**
     * Result of storing an archive.
     *
     * @param path             filesystem path or provider URI, for display
     *                         and for local retrieval. May be null.
     * @param storageReference provider-assigned object id. Null for
     *                         providers that address by path.
     */
    record StoredArchive(String path, String storageReference) {
    }

    /**
     * Everything a provider needs to find an archive it stored earlier.
     */
    record StoredArchiveLocation(String fileName, String path, String storageReference) {

        public StoredArchiveLocation {
            Objects.requireNonNull(fileName, "Archive file name cannot be null.");
        }
    }
}
