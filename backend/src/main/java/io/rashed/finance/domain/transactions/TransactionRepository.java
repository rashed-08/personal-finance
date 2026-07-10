package io.rashed.finance.domain.transactions;

import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    Transaction save(Transaction transaction);

    List<Transaction> saveAll(List<Transaction> transactions);

    Optional<Transaction> findById(TransactionId id);

    boolean existsById(TransactionId id);

    void delete(Transaction transaction);

    void deleteById(TransactionId id);

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    List<Transaction> findAll();

    List<Transaction> findByTransactionDate(LocalDate transactionDate);

    List<Transaction> findBetweenDates(
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findBySalaryCycle(
            SalaryCycleId salaryCycleId
    );

    List<Transaction> findByCategory(
            CategoryId categoryId
    );

    List<Transaction> findByFromAccount(
            AccountId accountId
    );

    List<Transaction> findByToAccount(
            AccountId accountId
    );

    List<Transaction> findByAccount(
            AccountId accountId
    );
}