package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
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

class GetIncomeExpenseReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private GetIncomeExpenseReportService service;

    @BeforeEach
    void setUp() {

        transactionRepository = mock(TransactionRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        service = new GetIncomeExpenseReportService(transactionRepository, categoryRepository);
    }

    @Test
    void execute_rejectsUnsupportedTransactionType() {

        IncomeExpenseReportQuery query = new IncomeExpenseReportQuery(
                TransactionType.TRANSFER, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.execute(query));
    }

    @Test
    void execute_totalsAndGroupsByCategoryAndDate() {

        Category category = Category.userExpenseCategory("Groceries", null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Transaction t1 = Transaction.expense(
                TransactionId.newId(), today, Money.of(500), accountId, categoryId, salaryCycleId, "Groceries");
        Transaction t2 = Transaction.expense(
                TransactionId.newId(), today.plusDays(1), Money.of(300), accountId, categoryId, salaryCycleId, "Groceries");

        givenTransactions(List.of(t1, t2));

        IncomeExpenseReportResult result = service.execute(new IncomeExpenseReportQuery(
                TransactionType.EXPENSE, today, today.plusDays(1), salaryCycleId, accountId, categoryId));

        assertEquals(Money.of(800), result.total());
        assertEquals(2, result.transactionCount());
        assertEquals(1, result.byCategory().size());
        assertEquals(Money.of(800), result.byCategory().get(0).total());
        assertEquals(2, result.byDate().size());
    }

    @Test
    void execute_emptyResultWhenNoTransactions() {

        givenTransactions(List.of());

        IncomeExpenseReportResult result = service.execute(new IncomeExpenseReportQuery(
                TransactionType.INCOME, null, null, null, null, null));

        assertEquals(Money.zero(), result.total());
        assertEquals(0, result.transactionCount());
    }

    private void givenTransactions(List<Transaction> transactions) {

        Page<Transaction> page = new PageImpl<>(transactions);
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);
    }
}
