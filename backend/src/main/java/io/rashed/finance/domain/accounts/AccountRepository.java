package io.rashed.finance.domain.accounts;

import io.rashed.finance.common.enums.AccountType;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    List<Account> saveAll(List<Account> accounts);

    Optional<Account> findById(AccountId id);

    Optional<Account> findByName(String name);

    boolean existsById(AccountId id);

    boolean existsByName(String name);

    List<Account> findAll();

    List<Account> findActive();

    List<Account> findInactive();

    List<Account> findByType(AccountType accountType);

    void delete(Account account);

    void deleteById(AccountId id);
}