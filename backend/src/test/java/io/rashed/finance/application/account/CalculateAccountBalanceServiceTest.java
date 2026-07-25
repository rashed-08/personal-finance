package io.rashed.finance.application.account;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculateAccountBalanceServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final AccountId account = AccountId.newId();
    private final AccountId other = AccountId.newId();
    private final CategoryId category = CategoryId.newId();

    private TransactionRepository transactionRepository;
    private CalculateAccountBalanceService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        service = new CalculateAccountBalanceService(transactionRepository);
    }

    @Test
    void execute_sumsIncomeExpenseAndTransfersForTheAccount() {

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(1000), account, category, null, null);
        Transaction expense = Transaction.expense(
                TransactionId.newId(), today, Money.of(300), account, category, null, null);
        Transaction transferOut = Transaction.transfer(
                TransactionId.newId(), today, Money.of(100), account, other, null, null);

        when(transactionRepository.find(
                argThat((TransactionFilter f) -> f != null && account.equals(f.accountId())),
                any(Pageable.class)
        )).thenReturn(pageOf(List.of(income, expense, transferOut)));

        Money balance = service.execute(account, today);

        assertEquals(Money.of(600), balance);
    }

    @Test
    void execute_returnsZeroWithNoTransactions() {

        when(transactionRepository.find(any(TransactionFilter.class), any(Pageable.class)))
                .thenReturn(pageOf(List.of()));

        assertEquals(Money.zero(), service.execute(account, today));
    }

    private Page<Transaction> pageOf(List<Transaction> transactions) {
        return new PageImpl<>(transactions);
    }
}
