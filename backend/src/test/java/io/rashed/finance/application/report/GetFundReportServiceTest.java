package io.rashed.finance.application.report;

import io.rashed.finance.application.fund.CalculateFundBalanceService;
import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetFundReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private FundRepository fundRepository;
    private TransactionRepository transactionRepository;
    private CalculateFundBalanceService calculateFundBalanceService;
    private GetFundReportService service;

    @BeforeEach
    void setUp() {

        fundRepository = mock(FundRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        calculateFundBalanceService = mock(CalculateFundBalanceService.class);
        service = new GetFundReportService(fundRepository, transactionRepository, calculateFundBalanceService);
    }

    @Test
    void executeOne_rejectsUnknownFund() {

        FundId fundId = FundId.newId();
        when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.executeOne(fundId));
    }

    @Test
    void executeOne_splitsAllocatedAndUsedAndComputesProgress() {

        Fund fund = Fund.create("New Laptop", FundType.GOAL, Money.of(10000), null, null);
        when(fundRepository.findById(fund.getId())).thenReturn(Optional.of(fund));
        when(calculateFundBalanceService.execute(fund.getId())).thenReturn(Money.of(4000));

        Transaction allocation = Transaction.fundTransfer(
                TransactionId.newId(), today, Money.of(5000), accountId, null, fund.getId(), salaryCycleId, null);
        Transaction withdrawal = Transaction.fundTransfer(
                TransactionId.newId(), today, Money.of(1000), null, accountId, fund.getId(), salaryCycleId, null);

        Page<Transaction> page = new PageImpl<>(List.of(allocation, withdrawal));
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);

        FundReportLine line = service.executeOne(fund.getId());

        assertEquals(Money.of(5000), line.allocatedAmount());
        assertEquals(Money.of(1000), line.usedAmount());
        assertEquals(Money.of(4000), line.remainingBalance());
        assertEquals(0, new BigDecimal("40.00").compareTo(line.progressPercentage()));
    }

    @Test
    void executeOne_progressIsNullWithoutTarget() {

        Fund fund = Fund.create("Emergency", FundType.EMERGENCY, null, null, null);
        when(fundRepository.findById(fund.getId())).thenReturn(Optional.of(fund));
        when(calculateFundBalanceService.execute(fund.getId())).thenReturn(Money.zero());
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        FundReportLine line = service.executeOne(fund.getId());

        assertNull(line.progressPercentage());
        assertNull(line.targetAmount());
    }
}
