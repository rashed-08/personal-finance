package io.rashed.finance.application.recurring;

import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListRecurringTransactionExecutionsService {

    private final RecurringTransactionExecutionRepository repository;

    public ListRecurringTransactionExecutionsService(RecurringTransactionExecutionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<RecurringTransactionExecution> execute(RecurringTransactionId id) {

        return repository.findByRecurringTransactionId(id);
    }
}
