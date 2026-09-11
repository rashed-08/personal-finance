package io.rashed.finance.application.backup;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.BackupFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupArchiveTest {

    private static BackupArchive archive(String content) {

        return BackupArchive.of(
                "archive.zip",
                BackupFormat.JSON,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void of_computesTheSha256OfTheContent() {

        // Known-answer test: SHA-256 of the ASCII string "abc".
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                archive("abc").checksum()
        );
    }

    @Test
    void size_isTheContentLength() {

        assertEquals(3, archive("abc").size());
    }

    @Test
    void matches_acceptsTheChecksumItProduced() {

        BackupArchive archive = archive("ledger");

        assertTrue(archive.matches(archive.checksum()));
    }

    @Test
    void matches_isCaseAndWhitespaceInsensitive() {

        BackupArchive archive = archive("ledger");

        assertTrue(archive.matches("  " + archive.checksum().toUpperCase() + "  "));
    }

    @Test
    void matches_rejectsAModifiedArchive() {

        // The point of the checksum: one byte different, and a restore
        // must be refused.
        assertFalse(archive("ledger").matches(archive("1edger").checksum()));
    }

    @Test
    void matches_treatsAMissingExpectationAsVerified() {

        // Archives written before checksums were recorded are still
        // restorable, just unverified.
        BackupArchive archive = archive("ledger");

        assertTrue(archive.matches(null));
        assertTrue(archive.matches("   "));
    }
}
