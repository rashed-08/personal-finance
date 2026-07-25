package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;

@Service
public class GetAccountBalancesReportService {

    private final AccountRepository accountRepository;
    private final CalculateAccountBalanceService calculateAccountBalanceService;

    public GetAccountBalancesReportService(
            AccountRepository accountRepository,
            CalculateAccountBalanceService calculateAccountBalanceService
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.calculateAccountBalanceService = Objects.requireNonNull(calculateAccountBalanceService);
    }

    public List<AccountBalance> execute(LocalDate asOfDate, boolean activeOnly) {

        Objects.requireNonNull(asOfDate, "Date cannot be null.");

        List<Account> accounts = activeOnly ? accountRepository.findActive() : accountRepository.findAll();

        return accounts.stream()
                .map(account -> new AccountBalance(
                        account.getId(),
                        account.getName(),
                        account.getAccountType(),
                        calculateAccountBalanceService.execute(account.getId(), asOfDate)
                ))
                .toList();
    }
}
