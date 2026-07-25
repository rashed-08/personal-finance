package io.rashed.finance.application.migration;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Imports historical financial data from a Google Keep text export, per
 * docs/business/GoogleKeepMigration.md. Categorization is best-effort only —
 * per business decision, an unmatched label silently falls back to "Other
 * Expense" rather than blocking the import or requiring manual review; the
 * goal is getting accurate amounts into the ledger for totals/graphs, not
 * perfect category fidelity.
 */
@Service
public class ImportGoogleKeepDataService {

    private static final String LEGACY_IMPORT_ACCOUNT_NAME = "Legacy Import";

    private static final String FALLBACK_CATEGORY_NAME = "Other Expense";

    private static final DateTimeFormatter CYCLE_NAME_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    /**
     * Known Bengali category labels mapped to existing category names
     * (see V2__seed_data.sql). Anything not listed here falls back to
     * {@link #FALLBACK_CATEGORY_NAME} — extend this map as new labels
     * are encountered in future imports.
     */
    private static final Map<String, String> CATEGORY_SYNONYMS = Map.ofEntries(
            Map.entry("গ্যাস", "Utilities"),
            Map.entry("বাসা ভাড়া", "Rent"),
            Map.entry("ইন্টারনেট বিল", "Utilities"),
            Map.entry("ইলেকট্রিসিটি বিল", "Utilities"),
            Map.entry("ঔষধ", "Medical"),
            Map.entry("বাজার", "Groceries"),
            Map.entry("আফিয়া", "Afia"),
            Map.entry("দুধ", "Groceries"),
            Map.entry("রিচার্জ", "Utilities"),
            Map.entry("টেকনাফ", "Travel"),
            Map.entry("আপা", "Family"),
            Map.entry("বেল্ট", "Shopping"),
            Map.entry("ব্লেন্ডার", "Shopping"),
            Map.entry("মরিচ", "Groceries"),
            Map.entry("খালাম্মা কুকার", "Shopping"),
            Map.entry("ধান", "Groceries"),
            Map.entry("দরগাহ", "Donation"),
            Map.entry("কোরবানি", "Donation")
    );

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SalaryCycleRepository salaryCycleRepository;
    private final TransactionRepository transactionRepository;

    public ImportGoogleKeepDataService(
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            SalaryCycleRepository salaryCycleRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public GoogleKeepMigrationResult execute(String content) {

        long startMillis = System.currentTimeMillis();

        GoogleKeepParseResult parseResult = GoogleKeepParser.parse(content);

        List<String> warnings = new ArrayList<>(parseResult.warnings());
        List<String> errors = new ArrayList<>();

        int imported = 0;
        int skipped = 0;

        Account account = getOrCreateLegacyImportAccount();
        String migrationBatchId = "google-keep-" + UUID.randomUUID();

        for (GoogleKeepMonth month : parseResult.months()) {

            SalaryCycle salaryCycle = getOrCreateSalaryCycle(month.yearMonth());

            Money computedSum = Money.zero();

            for (GoogleKeepLine line : month.lines()) {

                try {

                    computedSum = computedSum.add(line.amount());

                    CategoryId categoryId = resolveCategory(line.categoryLabel());

                    if (isDuplicate(salaryCycle.getId(), categoryId, line.amount())) {
                        skipped++;
                        continue;
                    }

                    Transaction transaction = Transaction.importedExpense(
                            TransactionId.newId(),
                            salaryCycle.getStartDate(),
                            line.amount(),
                            account.getId(),
                            categoryId,
                            salaryCycle.getId(),
                            line.categoryLabel(),
                            line.breakdownNotes(),
                            migrationBatchId
                    );

                    transactionRepository.save(transaction);
                    imported++;

                } catch (RuntimeException e) {
                    errors.add("Failed to import \"" + line.rawLine() + "\": " + e.getMessage());
                }
            }

            if (month.statedTotal() != null && !month.statedTotal().equals(computedSum)) {
                warnings.add("Month " + month.yearMonth() + ": stated total " + month.statedTotal().getAmount()
                        + " does not match the sum of imported lines " + computedSum.getAmount()
                        + " (reference only — not enforced).");
            }
        }

        deactivateLegacyImportAccount(account);

        long durationMillis = System.currentTimeMillis() - startMillis;

        return new GoogleKeepMigrationResult(imported, skipped, List.copyOf(warnings), List.copyOf(errors), durationMillis);
    }

    private Account getOrCreateLegacyImportAccount() {

        return accountRepository.findByName(LEGACY_IMPORT_ACCOUNT_NAME)
                .orElseGet(() -> accountRepository.save(
                        Account.createCashAccount(LEGACY_IMPORT_ACCOUNT_NAME, Money.zero())));
    }

    /**
     * The account has no offsetting income — only ever debited by imported
     * expenses — so its derived balance is meaningless and, left active,
     * would distort Total Balance / Cash Balance (which sum active accounts
     * only). Deactivating it removes that distortion without affecting any
     * date/category-based report, none of which filter by account status.
     */
    private void deactivateLegacyImportAccount(Account account) {

        if (account.isActive()) {
            accountRepository.save(account.deactivate());
        }
    }

    private SalaryCycle getOrCreateSalaryCycle(YearMonth yearMonth) {

        LocalDate start = yearMonth.atDay(1);

        return salaryCycleRepository.findByDate(start)
                .orElseGet(() -> salaryCycleRepository.save(
                        SalaryCycle.create(
                                yearMonth.format(CYCLE_NAME_FORMAT),
                                start,
                                yearMonth.atEndOfMonth(),
                                start,
                                "Auto-created for Google Keep migration."
                        )));
    }

    private CategoryId resolveCategory(String label) {

        String canonicalName = CATEGORY_SYNONYMS.getOrDefault(label, FALLBACK_CATEGORY_NAME);

        return categoryRepository.findByName(canonicalName)
                .or(() -> categoryRepository.findByName(FALLBACK_CATEGORY_NAME))
                .map(category -> category.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No '" + FALLBACK_CATEGORY_NAME + "' category found — check seed data."));
    }

    private boolean isDuplicate(SalaryCycleId salaryCycleId, CategoryId categoryId, Money amount) {

        TransactionFilter filter = new TransactionFilter(
                null, null, TransactionType.EXPENSE, TransactionStatus.POSTED, null, categoryId, salaryCycleId, null, null, null);

        return transactionRepository.find(filter, Pageable.unpaged())
                .stream()
                .anyMatch(transaction -> transaction.getAmount().equals(amount));
    }
}
