package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * All transactions touching one account, with a true running balance —
 * anchored on the account's real ledger-derived balance the day before
 * fromDate, not zero, so a filtered window still shows accurate figures.
 */
@Service
public class GetAccountStatementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CalculateAccountBalanceService calculateAccountBalanceService;

    public GetAccountStatementService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CalculateAccountBalanceService calculateAccountBalanceService
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.calculateAccountBalanceService = Objects.requireNonNull(calculateAccountBalanceService);
    }

    public AccountStatementResult execute(AccountId accountId, LocalDate fromDate, LocalDate toDate) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));

        Money openingBalance = fromDate != null
                ? calculateAccountBalanceService.execute(accountId, fromDate.minusDays(1))
                : Money.zero();

        TransactionFilter filter = new TransactionFilter(
                fromDate, toDate, null, TransactionStatus.POSTED, accountId, null, null, null, null);

        List<Transaction> sorted = transactionRepository.find(filter, Pageable.unpaged())
                .stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).thenComparing(Transaction::getCreatedAt))
                .toList();

        Money running = openingBalance;
        List<StatementLine> lines = new ArrayList<>();

        for (Transaction transaction : sorted) {

            Money signed = transaction.signedAmountFor(accountId);
            running = running.add(signed);

            lines.add(new StatementLine(
                    transaction.getId(),
                    transaction.getTransactionDate(),
                    transaction.getDescription(),
                    transaction.getTransactionType(),
                    signed,
                    running
            ));
        }

        return new AccountStatementResult(accountId, account.getName(), openingBalance, lines, running);
    }
}
