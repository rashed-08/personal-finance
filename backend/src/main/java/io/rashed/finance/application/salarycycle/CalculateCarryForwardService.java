package io.rashed.finance.application.salarycycle;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Calculates Carry Forward for a salary cycle per
 * docs/business/CarryForwardWorkflow.md. Nothing here is stored — it is
 * always derived from the transaction ledger.
 */
@Service
public class CalculateCarryForwardService {

    private final SalaryCycleRepository salaryCycleRepository;
    private final TransactionRepository transactionRepository;

    public CalculateCarryForwardService(
            SalaryCycleRepository salaryCycleRepository,
            TransactionRepository transactionRepository
    ) {
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public CarryForwardResult execute(SalaryCycleId id) {

        SalaryCycle cycle = salaryCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));

        Money openingBalance = openingBalanceFor(cycle);

        Money income = Money.zero();
        Money expenses = Money.zero();
        Money adjustments = Money.zero();

        for (Transaction transaction : transactionsIn(id)) {

            switch (transaction.getTransactionType()) {

                case INCOME -> income = income.add(transaction.getAmount());

                case EXPENSE -> expenses = expenses.add(transaction.getAmount());

                case ADJUSTMENT -> adjustments = transaction.increasesBalance()
                        ? adjustments.add(transaction.getAmount())
                        : adjustments.subtract(transaction.getAmount());

                case TRANSFER, OPENING_BALANCE, MIGRATION -> {
                    // Transfers do not change net worth; opening balance and
                    // migration transactions never carry a salaryCycleId.
                }
            }
        }

        Money closingBalance = openingBalance.add(income).subtract(expenses).add(adjustments);

        return new CarryForwardResult(id, openingBalance, income, expenses, adjustments, closingBalance);
    }

    private Money openingBalanceFor(SalaryCycle cycle) {

        return salaryCycleRepository.findPrevious(cycle.getStartDate())
                .map(previous -> execute(previous.getId()).closingBalance())
                .orElseGet(() -> ledgerActivityBefore(cycle.getStartDate()));
    }

    /** Opening balance for the very first cycle: whatever the ledger recorded before it began. */
    private Money ledgerActivityBefore(LocalDate date) {

        Money total = Money.zero();

        TransactionFilter filter = new TransactionFilter(
                null, date.minusDays(1), null, TransactionStatus.POSTED, null, null, null, null, null);

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            if (transaction.getTransactionType() == TransactionType.TRANSFER) {
                continue;
            }

            total = transaction.increasesBalance()
                    ? total.add(transaction.getAmount())
                    : total.subtract(transaction.getAmount());
        }

        return total;
    }

    private List<Transaction> transactionsIn(SalaryCycleId salaryCycleId) {

        TransactionFilter filter = new TransactionFilter(
                null, null, null, TransactionStatus.POSTED, null, null, salaryCycleId, null, null);

        return transactionRepository.find(filter, Pageable.unpaged()).getContent();
    }
}
