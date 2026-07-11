package io.rashed.finance.application.account;

import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public final class CreateAccountService {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository =
                Objects.requireNonNull(
                        accountRepository,
                        "AccountRepository cannot be null."
                );
    }

    public Account execute(CreateAccountCommand command) {

        Objects.requireNonNull(
                command,
                "CreateAccountCommand cannot be null."
        );

        if (accountRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "An account with the same name already exists."
            );
        }

        Account account = Account.create(
                command.name(),
                command.accountType(),
                command.openingBalance(),
                command.description()
        );

        return accountRepository.save(account);
    }
}