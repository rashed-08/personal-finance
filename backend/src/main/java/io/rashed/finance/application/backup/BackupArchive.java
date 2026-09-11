package io.rashed.finance.application.backup;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import io.rashed.finance.common.enums.BackupFormat;

/**
 * A backup archive held in memory, together with the checksum of its bytes.
 *
 * Archives are kept whole rather than streamed: a personal finance ledger
 * is measured in megabytes, and having the complete byte array lets the
 * checksum be computed once and verified before a restore is allowed to
 * touch the database. If the ledger ever outgrows that, this is the type
 * to turn into a stream.
 *
 * @param fileName name the archive is stored under, extension included.
 * @param format   serializer that produced {@code content}; a restore must
 *                 use the matching reader.
 * @param content  the archive bytes. Not copied — callers must not mutate
 *                 the array after handing it over.
 * @param checksum lowercase hex SHA-256 of {@code content}.
 */
public record BackupArchive(
        String fileName,
        BackupFormat format,
        byte[] content,
        String checksum
) {

    public BackupArchive {

        Objects.requireNonNull(fileName, "Archive file name cannot be null.");
        Objects.requireNonNull(format, "Archive format cannot be null.");
        Objects.requireNonNull(content, "Archive content cannot be null.");
        Objects.requireNonNull(checksum, "Archive checksum cannot be null.");

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("Archive file name cannot be empty.");
        }
    }

    /**
     * Wraps bytes and computes their checksum.
     */
    public static BackupArchive of(String fileName, BackupFormat format, byte[] content) {

        Objects.requireNonNull(content, "Archive content cannot be null.");

        return new BackupArchive(fileName, format, content, sha256(content));
    }

    public long size() {
        return content.length;
    }

    /**
     * Whether these bytes still hash to {@code expectedChecksum}.
     *
     * A null or blank expectation counts as verified: archives written
     * before checksums were recorded are still restorable, just unverified.
     */
    public boolean matches(String expectedChecksum) {

        if (expectedChecksum == null || expectedChecksum.isBlank()) {
            return true;
        }

        return MessageDigest.isEqual(
                checksum.getBytes(StandardCharsets.UTF_8),
                expectedChecksum.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String sha256(byte[] content) {

        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(content)
            );

        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is required of every Java platform implementation.
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
