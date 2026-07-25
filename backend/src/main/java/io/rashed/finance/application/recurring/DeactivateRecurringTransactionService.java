package io.rashed.finance.application.recurring;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

/** Disabling a template only stops future generation; past occurrences are untouched. */
@Service
public class DeactivateRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public DeactivateRecurringTransactionService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public RecurringTransaction execute(RecurringTransactionId id) {

        RecurringTransaction recurringTransaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id.getValue()));

        return repository.save(recurringTransaction.deactivate());
    }
}
