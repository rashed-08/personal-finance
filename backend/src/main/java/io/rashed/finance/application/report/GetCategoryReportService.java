package io.rashed.finance.application.report;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

@Service
public class GetCategoryReportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public GetCategoryReportService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public CategoryReportResult execute(CategoryReportQuery query) {

        Objects.requireNonNull(query, "Query cannot be null.");

        Category category = categoryRepository.findById(query.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        TransactionFilter filter = new TransactionFilter(
                query.fromDate(), query.toDate(), null, TransactionStatus.POSTED, null, query.categoryId(), null, null, null);

        List<Transaction> transactions = transactionRepository.find(filter, Pageable.unpaged()).getContent();

        Money total = Money.zero();
        Map<YearMonth, Money> monthlyTotals = new TreeMap<>();

        for (Transaction transaction : transactions) {

            total = total.add(transaction.getAmount());

            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            monthlyTotals.merge(month, transaction.getAmount(), Money::add);
        }

        List<MonthlyAmount> monthlyTrend = monthlyTotals.entrySet().stream()
                .map(entry -> new MonthlyAmount(entry.getKey(), entry.getValue()))
                .toList();

        Money averagePerMonth = monthlyTrend.isEmpty()
                ? Money.zero()
                : total.divide(BigDecimal.valueOf(monthlyTrend.size()));

        return new CategoryReportResult(
                query.categoryId(), category.getName(), total, transactions.size(), monthlyTrend, averagePerMonth);
    }
}
