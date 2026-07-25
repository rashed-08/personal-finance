package io.rashed.finance.application.recurring;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

@Service
public class DeleteRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public DeleteRecurringTransactionService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void execute(RecurringTransactionId id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurring transaction not found: " + id.getValue());
        }

        repository.deleteById(id);
    }
}
