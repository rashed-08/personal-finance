package io.rashed.finance.domain.recurring;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository {

    RecurringTransaction save(RecurringTransaction recurringTransaction);

    Optional<RecurringTransaction> findById(
            RecurringTransactionId id
    );

    List<RecurringTransaction> findAll();

    List<RecurringTransaction> findActive();

    void deleteById(
            RecurringTransactionId id
    );

    boolean existsById(
            RecurringTransactionId id
    );
}