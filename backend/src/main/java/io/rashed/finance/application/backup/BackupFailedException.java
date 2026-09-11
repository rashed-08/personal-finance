package io.rashed.finance.application.backup;

/**
 * A backup, restore or storage operation could not be completed.
 *
 * Thrown by exporters, importers and storage providers so the calling
 * service can record the failure in the audit trail and report it as a
 * 500 with a usable message, rather than leaking provider-specific
 * exception types into the API layer.
 */
public class BackupFailedException extends RuntimeException {

    public BackupFailedException(String message) {
        super(message);
    }

    public BackupFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
