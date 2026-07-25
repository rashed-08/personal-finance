package io.rashed.finance.application.recurring;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GetRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public GetRecurringTransactionService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public RecurringTransaction execute(RecurringTransactionId id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id.getValue()));
    }
}
