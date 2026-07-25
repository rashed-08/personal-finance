package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCashFlowReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final AccountId otherAccountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private TransactionRepository transactionRepository;
    private GetCashFlowReportService service;

    @BeforeEach
    void setUp() {

        transactionRepository = mock(TransactionRepository.class);
        service = new GetCashFlowReportService(transactionRepository);
    }

    @Test
    void execute_sumsEachTypeSeparatelyAndExcludesTransfersFromNet() {

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(5000), accountId, categoryId, salaryCycleId, null);
        Transaction expense = Transaction.expense(
                TransactionId.newId(), today, Money.of(2000), accountId, categoryId, salaryCycleId, null);
        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), today, Money.of(1000), accountId, otherAccountId, salaryCycleId, null);

        when(transactionRepository.find(any(), any())).thenAnswer(invocation -> {

            TransactionFilter filter = invocation.getArgument(0);

            if (filter.transactionType() == TransactionType.INCOME) {
                return new PageImpl<>(List.of(income));
            }
            if (filter.transactionType() == TransactionType.EXPENSE) {
                return new PageImpl<>(List.of(expense));
            }
            return new PageImpl<>(List.of(transfer));
        });

        CashFlowReportResult result = service.execute(today, today);

        assertEquals(Money.of(5000), result.moneyIn());
        assertEquals(Money.of(2000), result.moneyOut());
        assertEquals(Money.of(3000), result.netCashFlow());
        assertEquals(Money.of(1000), result.totalTransferVolume());
    }
}
