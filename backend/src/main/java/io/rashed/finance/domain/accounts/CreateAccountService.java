package io.rashed.finance.domain.accounts;

import java.util.Objects;

public class CreateAccountService {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    public Account execute(CreateAccountCommand command) {

        Objects.requireNonNull(command);

        if (accountRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Account already exists: " + command.name());
        }

        Account account = Account.create(command.name(), command.accountType(), command.openingBalance(), command.description());

        return accountRepository.save(account);
    }
}