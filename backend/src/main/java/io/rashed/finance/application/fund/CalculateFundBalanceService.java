package io.rashed.finance.application.fund;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * A fund's balance is never stored — it is always derived from posted
 * allocation/withdrawal transactions: Total Allocations - Total Withdrawals.
 * See docs/business/FundWorkflow.md and docs/database/tables/ funds.md.
 */
@Service
public class CalculateFundBalanceService {

    private final TransactionRepository transactionRepository;

    public CalculateFundBalanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public Money execute(FundId fundId) {

        Objects.requireNonNull(fundId, "Fund cannot be null.");

        TransactionFilter filter = new TransactionFilter(
                null, null, TransactionType.TRANSFER, TransactionStatus.POSTED, null, null, null, fundId, null);

        Money balance = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            balance = transaction.increasesFundBalance()
                    ? balance.add(transaction.getAmount())
                    : balance.subtract(transaction.getAmount());
        }

        return balance;
    }
}
