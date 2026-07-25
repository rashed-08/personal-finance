package io.rashed.finance.application.recurring;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.application.transaction.CreateTransactionCommand;
import io.rashed.finance.application.transaction.CreateTransactionService;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;

/**
 * Generates the currently-due occurrence of a single template — used both
 * for manual confirmation (autoGenerate = false templates, or forcing an
 * early occurrence) and as the shared step RunDueRecurringTransactionsService
 * loops over for autoGenerate = true templates.
 *
 * The generated transaction is dated on the occurrence's scheduled date
 * (nextExecutionDate), not "today", and its salary cycle is always resolved
 * to whichever cycle is open at generation time — never stored on the
 * template. If no cycle is open, or the underlying transaction creation
 * fails for any reason (e.g. the account was deactivated since the template
 * was created), the occurrence is recorded as SKIPPED with the reason
 * rather than failing the whole run.
 */
@Service
public class GenerateRecurringTransactionNowService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionExecutionRepository executionRepository;
    private final SalaryCycleRepository salaryCycleRepository;
    private final CreateTransactionService createTransactionService;

    public GenerateRecurringTransactionNowService(
            RecurringTransactionRepository recurringTransactionRepository,
            RecurringTransactionExecutionRepository executionRepository,
            SalaryCycleRepository salaryCycleRepository,
            CreateTransactionService createTransactionService
    ) {
        this.recurringTransactionRepository = Objects.requireNonNull(recurringTransactionRepository);
        this.executionRepository = Objects.requireNonNull(executionRepository);
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.createTransactionService = Objects.requireNonNull(createTransactionService);
    }

    public RecurringTransaction execute(RecurringTransactionId id) {

        RecurringTransaction template = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id.getValue()));

        if (!template.isActive()) {
            throw new IllegalStateException("Cannot generate an occurrence of an inactive template.");
        }

        return generateOccurrence(template);
    }

    /** Generates every occurrence still due as of the given date, in schedule order. */
    public RecurringTransaction generateAllDueOccurrences(RecurringTransaction template, LocalDate asOfDate) {

        RecurringTransaction current = template;

        while (current.isDue(asOfDate)) {
            current = generateOccurrence(current);
        }

        return current;
    }

    RecurringTransaction generateOccurrence(RecurringTransaction template) {

        LocalDate scheduledDate = template.getNextExecutionDate();

        RecurringTransaction updated;
        RecurringTransactionExecution execution;

        try {

            SalaryCycle openCycle = salaryCycleRepository.findOpen()
                    .orElseThrow(() -> new IllegalStateException("No salary cycle is currently open."));

            CreateTransactionCommand command = new CreateTransactionCommand(
                    template.getTransactionType(),
                    scheduledDate,
                    template.getAmount(),
                    template.getDescription(),
                    template.getNotes(),
                    template.getFromAccountId(),
                    template.getToAccountId(),
                    template.getCategoryId(),
                    openCycle.getId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false
            );

            Transaction generated = createTransactionService.execute(command);

            updated = template.markExecuted();
            execution = RecurringTransactionExecution.generated(template.getId(), scheduledDate, generated.getId());

        } catch (RuntimeException e) {

            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            updated = template.markSkipped();
            execution = RecurringTransactionExecution.skipped(template.getId(), scheduledDate, reason);
        }

        recurringTransactionRepository.save(updated);
        executionRepository.save(execution);

        return updated;
    }
}
