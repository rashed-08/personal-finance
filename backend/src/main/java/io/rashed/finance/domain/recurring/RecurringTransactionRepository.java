package io.rashed.finance.domain.recurring;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository {

    RecurringTransaction save(RecurringTransaction recurringTransaction);

    Optional<RecurringTransaction> findById(
            RecurringTransactionId id
    );

    List<RecurringTransaction> findAll();

    List<RecurringTransaction> findActive();

    /** Active templates due as of the given date, regardless of autoGenerate. */
    List<RecurringTransaction> findDue(LocalDate asOfDate);

    /** Active, autoGenerate templates due as of the given date. */
    List<RecurringTransaction> findDueForAutoGeneration(LocalDate asOfDate);

    void deleteById(
            RecurringTransactionId id
    );

    boolean existsById(
            RecurringTransactionId id
    );
}