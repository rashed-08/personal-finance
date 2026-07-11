package io.rashed.finance.application.account;

import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAccountsService {

    private final AccountRepository accountRepository;

    public ListAccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> execute() {
        return accountRepository.findAll();
    }
}