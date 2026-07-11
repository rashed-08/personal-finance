package io.rashed.finance.application.account;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateAccountService {

    private final AccountRepository repository;

    public UpdateAccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account execute(
            AccountId id,
            String name,
            AccountType type,
            Money balance,
            String description
    ) {

        Account account = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found: " + id.getValue()
                        )
                );

        Account updatedAccount = account.update(
                name,
                type,
                balance,
                description
        );

        return repository.save(updatedAccount);
    }
}