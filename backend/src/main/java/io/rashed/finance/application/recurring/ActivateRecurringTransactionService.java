package io.rashed.finance.application.recurring;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

@Service
public class ActivateRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public ActivateRecurringTransactionService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public RecurringTransaction execute(RecurringTransactionId id) {

        RecurringTransaction recurringTransaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id.getValue()));

        return repository.save(recurringTransaction.activate());
    }
}
