package io.rashed.finance.infrastructure.backup;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exports the database, wipes it, restores it, and checks the ledger came
 * back intact — the one property the whole backup module exists to
 * provide.
 *
 * Requires the local PostgreSQL from infra/compose.yaml, like all
 * {@code @SpringBootTest} tests in this project.
 *
 * <strong>This test replaces the contents of the database it runs
 * against.</strong> It restores its own export, so a local development
 * database ends up as it started, but never point it at data you care
 * about. {@code @DirtiesContext} is here because the restore truncates
 * tables underneath the shared context.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JsonBackupRoundTripIntegrationTest {

    @Autowired
    private JsonBackupExporter exporter;

    @Autowired
    private JsonBackupImporter importer;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void exportThenRestoreReproducesTheLedger() {

        // A distinctive fixture, so the assertions cannot pass on
        // pre-existing seed data.
        String marker = "round-trip-" + UUID.randomUUID();

        Account account = accountRepository.save(
                Account.create(marker, AccountType.BANK, Money.of(1000), null));

        Category category = categoryRepository.save(
                Category.create(marker, CategoryType.EXPENSE, false, null));

        Transaction expense = transactionRepository.save(
                Transaction.expense(
                        TransactionId.newId(),
                        java.time.LocalDate.of(2026, 7, 25),
                        Money.of("1234.56"),
                        account.getId(),
                        category.getId(),
                        null,
                        marker
                )
        );

        long accountsBefore = count("accounts");
        long transactionsBefore = count("transactions");

        // Export.
        BackupArchive archive = exporter.export("round-trip.zip");

        assertTrue(archive.size() > 0);
        assertEquals(archive.checksum(), BackupArchive.sha256(archive.content()));

        // Prove the restore actually restores, by removing the fixture
        // first. Children before parents, as the FK order requires.
        jdbcTemplate.update("DELETE FROM transactions");
        assertEquals(0, count("transactions"));

        // Restore.
        importer.restore(archive);

        assertEquals(accountsBefore, count("accounts"));
        assertEquals(transactionsBefore, count("transactions"));

        Transaction restored = transactionRepository.findById(expense.getId()).orElseThrow();

        // Money must survive the text round trip exactly — scale included.
        assertEquals(Money.of("1234.56"), restored.getAmount());
        assertEquals(marker, restored.getDescription());
        assertEquals(expense.getTransactionDate(), restored.getTransactionDate());
        assertEquals(expense.getTransactionType(), restored.getTransactionType());
        assertEquals(expense.getTransactionStatus(), restored.getTransactionStatus());
        assertEquals(account.getId(), restored.getFromAccountId());
        assertEquals(category.getId(), restored.getCategoryId());
    }

    @Test
    void restorePreservesSelfReferencingTransactions() {

        // reference_transaction_id points at another row in the same
        // table, so the importer nulls it on insert and links it in a
        // second pass. If that pass is broken, the link is silently lost.
        String marker = "self-ref-" + UUID.randomUUID();

        Account account = accountRepository.save(
                Account.create(marker, AccountType.CASH, Money.of(500), null));

        Category category = categoryRepository.save(
                Category.create(marker, CategoryType.EXPENSE, false, null));

        Transaction original = transactionRepository.save(
                Transaction.expense(
                        TransactionId.newId(),
                        java.time.LocalDate.of(2026, 7, 25),
                        Money.of(100),
                        account.getId(),
                        category.getId(),
                        null,
                        marker
                )
        );

        Transaction correction = transactionRepository.save(
                Transaction.adjustment(
                        TransactionId.newId(),
                        java.time.LocalDate.of(2026, 7, 26),
                        Money.of(100),
                        account.getId(),
                        null,
                        original.getId(),
                        io.rashed.finance.common.enums.AdjustmentReason.MANUAL_CORRECTION,
                        marker,
                        "Reverses the duplicate"
                )
        );

        BackupArchive archive = exporter.export("self-ref.zip");

        jdbcTemplate.update("DELETE FROM transactions");

        importer.restore(archive);

        Transaction restored = transactionRepository.findById(correction.getId()).orElseThrow();

        assertEquals(original.getId(), restored.getReferenceTransactionId());
        assertTrue(restored.isAdjustmentFor(original.getId()));
    }

    @Test
    void archiveExcludesAuthAndHistoryTables() {

        // Credentials must never travel in an archive, and the audit trail
        // has to survive the restore that writes to it. Both are
        // guaranteed by BackupTables; this pins the guarantee.
        List<String> covered = BackupTables.insertOrder();

        for (String excluded : List.of(
                "users",
                "refresh_tokens",
                "email_verification_tokens",
                "password_reset_tokens",
                "google_oauth_tokens",
                "backup_history",
                "flyway_schema_history"
        )) {
            assertTrue(
                    !covered.contains(excluded),
                    excluded + " must not be part of a backup archive"
            );
        }
    }

    @Test
    void deleteOrderIsTheReverseOfInsertOrder() {

        // Parents before children on the way in, children before parents
        // on the way out — otherwise the restore's DELETE trips a foreign
        // key.
        assertEquals(
                BackupTables.insertOrder().reversed(),
                BackupTables.deleteOrder()
        );
    }

    private long count(String table) {

        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);

        return count == null ? 0 : count;
    }
}
