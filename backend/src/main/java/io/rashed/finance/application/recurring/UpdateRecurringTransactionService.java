package io.rashed.finance.application.recurring;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

/**
 * transactionType, accounts and category are fixed at creation — only the
 * template's own scheduling/metadata can change afterward.
 */
@Service
public class UpdateRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public UpdateRecurringTransactionService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public RecurringTransaction execute(UpdateRecurringTransactionCommand command) {

        Objects.requireNonNull(command, "Command cannot be null.");

        RecurringTransaction existing = repository.findById(command.recurringTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recurring transaction not found: " + command.recurringTransactionId().getValue()));

        RecurringTransaction updated = existing
                .rename(command.name())
                .changeAmount(command.amount())
                .changeFrequency(command.frequency())
                .changeDateRange(existing.getStartDate(), command.endDate())
                .changeAutoGenerate(command.autoGenerate())
                .changeDescription(command.description())
                .changeNotes(command.notes());

        return repository.save(updated);
    }
}
