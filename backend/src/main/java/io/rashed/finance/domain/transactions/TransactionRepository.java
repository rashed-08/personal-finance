package io.rashed.finance.domain.transactions;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.rashed.finance.domain.accounts.AccountId;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    List<Transaction> findAll();

    Page<Transaction> find(TransactionFilter filter, Pageable pageable);

    boolean existsOpeningBalanceForAccount(AccountId accountId);
}