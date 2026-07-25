package io.rashed.finance.application.fund;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculateFundBalanceServiceTest {

    private final FundId fundId = FundId.newId();
    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private TransactionRepository transactionRepository;
    private CalculateFundBalanceService service;

    @BeforeEach
    void setUp() {

        transactionRepository = mock(TransactionRepository.class);
        service = new CalculateFundBalanceService(transactionRepository);
    }

    @Test
    void execute_isZeroWithNoTransactions() {

        givenTransactions(List.of());

        assertEquals(Money.zero(), service.execute(fundId));
    }

    @Test
    void execute_sumsAllocationsAndSubtractsWithdrawals() {

        Transaction allocation = Transaction.fundTransfer(
                TransactionId.newId(), today, Money.of(1000), accountId, null, fundId, salaryCycleId, null);

        Transaction withdrawal = Transaction.fundTransfer(
                TransactionId.newId(), today, Money.of(300), null, accountId, fundId, salaryCycleId, null);

        givenTransactions(List.of(allocation, withdrawal));

        assertEquals(Money.of(700), service.execute(fundId));
    }

    @Test
    void execute_filtersByFundIdTransferTypeAndPostedStatusOnly() {

        givenTransactions(List.of());

        service.execute(fundId);

        var captor = org.mockito.ArgumentCaptor.forClass(TransactionFilter.class);
        org.mockito.Mockito.verify(transactionRepository).find(captor.capture(), any(Pageable.class));

        TransactionFilter filter = captor.getValue();

        assertEquals(fundId, filter.fundId());
        assertEquals(io.rashed.finance.common.enums.TransactionType.TRANSFER, filter.transactionType());
        assertEquals(io.rashed.finance.common.enums.TransactionStatus.POSTED, filter.transactionStatus());
    }

    private void givenTransactions(List<Transaction> transactions) {

        Page<Transaction> page = new PageImpl<>(transactions);

        when(transactionRepository.find(any(TransactionFilter.class), any(Pageable.class)))
                .thenReturn(page);
    }
}
