package io.rashed.finance.application.account;

import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;


@Service
public class GetAccountService {

    private final AccountRepository accountRepository;


    public GetAccountService(
            AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }


    public Account findById(AccountId accountId) {

        return accountRepository.findById(accountId)
                .orElseThrow(
                    () -> new ResourceNotFoundException("Account not found: " + accountId.getValue())
                );
    }
}