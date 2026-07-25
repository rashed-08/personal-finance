package io.rashed.finance.application.account;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Derives an account's balance from the ledger, per the Constitution's
 * "balances are calculated, never stored" rule. Sums every posted
 * transaction touching the account up to (and including) the given date.
 */
@Service
public class CalculateAccountBalanceService {

    private final TransactionRepository transactionRepository;

    public CalculateAccountBalanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public Money execute(AccountId accountId, LocalDate asOfDate) {

        Objects.requireNonNull(accountId, "Account cannot be null.");
        Objects.requireNonNull(asOfDate, "Date cannot be null.");

        TransactionFilter filter = new TransactionFilter(
                null, asOfDate, null, TransactionStatus.POSTED, accountId, null, null, null, null, null);

        Money balance = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {
            balance = balance.add(transaction.signedAmountFor(accountId));
        }

        return balance;
    }
}
