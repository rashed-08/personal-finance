package io.rashed.finance.domain.recurring;

import java.util.List;

public interface RecurringTransactionExecutionRepository {

    RecurringTransactionExecution save(RecurringTransactionExecution execution);

    /** Most recent first. */
    List<RecurringTransactionExecution> findByRecurringTransactionId(RecurringTransactionId recurringTransactionId);
}
