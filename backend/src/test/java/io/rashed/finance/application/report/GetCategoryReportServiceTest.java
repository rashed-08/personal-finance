package io.rashed.finance.application.report;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCategoryReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();

    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;
    private GetCategoryReportService service;

    @BeforeEach
    void setUp() {

        categoryRepository = mock(CategoryRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new GetCategoryReportService(categoryRepository, transactionRepository);
    }

    @Test
    void execute_rejectsUnknownCategory() {

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.execute(new CategoryReportQuery(categoryId, null, null)));
    }

    @Test
    void execute_computesMonthlyTrendAndAverage() {

        Category category = Category.userExpenseCategory("Groceries", null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Transaction jan = Transaction.expense(
                TransactionId.newId(), LocalDate.of(2026, 1, 15), Money.of(1000), accountId, categoryId, salaryCycleId, null);
        Transaction feb = Transaction.expense(
                TransactionId.newId(), LocalDate.of(2026, 2, 15), Money.of(2000), accountId, categoryId, salaryCycleId, null);

        Page<Transaction> page = new PageImpl<>(List.of(jan, feb));
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);

        CategoryReportResult result = service.execute(new CategoryReportQuery(categoryId, null, null));

        assertEquals(Money.of(3000), result.totalSpending());
        assertEquals(2, result.transactionCount());
        assertEquals(2, result.monthlyTrend().size());
        assertEquals(Money.of(1500), result.averagePerMonth());
    }
}
