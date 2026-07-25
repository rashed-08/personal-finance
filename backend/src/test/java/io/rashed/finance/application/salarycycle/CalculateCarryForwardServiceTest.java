package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculateCarryForwardServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);
    private final AccountId account = AccountId.newId();
    private final CategoryId category = CategoryId.newId();

    private SalaryCycleRepository salaryCycleRepository;
    private TransactionRepository transactionRepository;
    private CalculateCarryForwardService service;

    @BeforeEach
    void setUp() {

        salaryCycleRepository = mock(SalaryCycleRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new CalculateCarryForwardService(salaryCycleRepository, transactionRepository);
    }

    @Test
    void execute_throwsWhenCycleNotFound() {

        SalaryCycleId id = SalaryCycleId.newId();
        when(salaryCycleRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }

    @Test
    void execute_firstCycleUsesLedgerActivityBeforeStartAsOpeningBalance() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);
        when(salaryCycleRepository.findById(cycle.getId())).thenReturn(Optional.of(cycle));
        when(salaryCycleRepository.findPrevious(july10)).thenReturn(Optional.empty());

        Transaction beforeIncome = Transaction.income(
                TransactionId.newId(), july10.minusDays(5), Money.of(1000), account, category, null, "opening funds");
        givenForBeforeDateQuery(List.of(beforeIncome));

        Transaction expenseInCycle = Transaction.expense(
                TransactionId.newId(), july10.plusDays(5), Money.of(200), account, category, cycle.getId(), "groceries");
        givenForCycleScopedQuery(cycle.getId(), List.of(expenseInCycle));

        CarryForwardResult result = service.execute(cycle.getId());

        assertEquals(Money.of(1000), result.openingBalance());
        assertEquals(Money.of(200), result.expenses());
        assertEquals(Money.of(800), result.closingBalance());
    }

    @Test
    void execute_usesPreviousCyclesClosingBalanceAsOpeningBalance() {

        SalaryCycle previous = SalaryCycle.create(
                "June 2026", july10.minusMonths(1), july10.minusDays(1), july10.minusMonths(1), null);
        SalaryCycle current = SalaryCycle.open("July 2026", july10, july10, null);

        when(salaryCycleRepository.findById(previous.getId())).thenReturn(Optional.of(previous));
        when(salaryCycleRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(salaryCycleRepository.findPrevious(previous.getStartDate())).thenReturn(Optional.empty());
        when(salaryCycleRepository.findPrevious(current.getStartDate())).thenReturn(Optional.of(previous));

        givenForBeforeDateQuery(List.of());

        Transaction incomeInPrevious = Transaction.income(
                TransactionId.newId(), previous.getStartDate().plusDays(1), Money.of(500), account, category, previous.getId(), "salary");
        givenForCycleScopedQuery(previous.getId(), List.of(incomeInPrevious));

        Transaction expenseInCurrent = Transaction.expense(
                TransactionId.newId(), july10.plusDays(1), Money.of(100), account, category, current.getId(), "rent");
        givenForCycleScopedQuery(current.getId(), List.of(expenseInCurrent));

        CarryForwardResult result = service.execute(current.getId());

        assertEquals(Money.of(500), result.openingBalance());
        assertEquals(Money.of(400), result.closingBalance());
    }

    @Test
    void execute_excludesTransfersAndSignsAdjustmentsByDirection() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);
        when(salaryCycleRepository.findById(cycle.getId())).thenReturn(Optional.of(cycle));
        when(salaryCycleRepository.findPrevious(july10)).thenReturn(Optional.empty());
        givenForBeforeDateQuery(List.of());

        AccountId other = AccountId.newId();

        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), july10.plusDays(1), Money.of(9999), account, other, cycle.getId(), "atm");

        Transaction increaseAdjustment = Transaction.adjustment(
                TransactionId.newId(), july10.plusDays(1), Money.of(50), null, account, null,
                AdjustmentReason.CASH_RECONCILIATION, "found cash", null);

        Transaction decreaseAdjustment = Transaction.adjustment(
                TransactionId.newId(), july10.plusDays(1), Money.of(30), account, null, null,
                AdjustmentReason.CASH_RECONCILIATION, "missing cash", null);

        givenForCycleScopedQuery(cycle.getId(), List.of(transfer, increaseAdjustment, decreaseAdjustment));

        CarryForwardResult result = service.execute(cycle.getId());

        assertEquals(Money.of(20), result.adjustments());
        assertEquals(Money.of(20), result.closingBalance());
    }

    private void givenForCycleScopedQuery(SalaryCycleId cycleId, List<Transaction> transactions) {

        when(transactionRepository.find(
                argThat((TransactionFilter f) -> f != null && cycleId.equals(f.salaryCycleId())),
                any(Pageable.class)
        )).thenReturn(pageOf(transactions));
    }

    private void givenForBeforeDateQuery(List<Transaction> transactions) {

        when(transactionRepository.find(
                argThat((TransactionFilter f) -> f != null && f.salaryCycleId() == null),
                any(Pageable.class)
        )).thenReturn(pageOf(transactions));
    }

    private Page<Transaction> pageOf(List<Transaction> transactions) {
        return new PageImpl<>(transactions);
    }
}
