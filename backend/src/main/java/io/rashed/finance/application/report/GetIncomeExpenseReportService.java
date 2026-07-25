package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Shared Income Report / Expense Report implementation — the two only
 * differ by transactionType, per docs/database/tables/transactions/07-reporting.md
 * 5.4. Grouping is done in Java over a filtered ledger fetch (consistent
 * with CalculateFundBalanceService/CalculateLoanBalanceService) rather than
 * SQL aggregation — a deliberate tradeoff appropriate at personal-app scale.
 */
@Service
public class GetIncomeExpenseReportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public GetIncomeExpenseReportService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
    }

    public IncomeExpenseReportResult execute(IncomeExpenseReportQuery query) {

        Objects.requireNonNull(query, "Query cannot be null.");

        if (query.transactionType() != TransactionType.INCOME && query.transactionType() != TransactionType.EXPENSE) {
            throw new IllegalArgumentException("This report supports only INCOME or EXPENSE.");
        }

        TransactionFilter filter = new TransactionFilter(
                query.fromDate(), query.toDate(), query.transactionType(), TransactionStatus.POSTED,
                query.accountId(), query.categoryId(), query.salaryCycleId(), null, null);

        List<Transaction> transactions = transactionRepository.find(filter, Pageable.unpaged()).getContent();

        Money total = Money.zero();
        Map<CategoryId, List<Transaction>> byCategoryId = new LinkedHashMap<>();
        Map<LocalDate, Money> byDateTotals = new TreeMap<>();

        for (Transaction transaction : transactions) {

            total = total.add(transaction.getAmount());

            if (transaction.hasCategory()) {
                byCategoryId.computeIfAbsent(transaction.getCategoryId(), key -> new ArrayList<>()).add(transaction);
            }

            byDateTotals.merge(transaction.getTransactionDate(), transaction.getAmount(), Money::add);
        }

        List<CategoryBreakdown> byCategory = byCategoryId.entrySet().stream()
                .map(entry -> {
                    Money categoryTotal = entry.getValue().stream()
                            .map(Transaction::getAmount)
                            .reduce(Money.zero(), Money::add);
                    String categoryName = categoryRepository.findById(entry.getKey())
                            .map(Category::getName)
                            .orElse("Unknown");
                    return new CategoryBreakdown(entry.getKey(), categoryName, categoryTotal, entry.getValue().size());
                })
                .sorted((a, b) -> b.total().compareTo(a.total()))
                .toList();

        List<DateBucket> byDate = byDateTotals.entrySet().stream()
                .map(entry -> new DateBucket(entry.getKey(), entry.getValue()))
                .toList();

        return new IncomeExpenseReportResult(query.transactionType(), total, transactions.size(), byCategory, byDate);
    }
}
