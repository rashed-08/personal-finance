package io.rashed.finance.application.recurring;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

/**
 * The on-demand "Run due transactions" action. Processes only autoGenerate
 * templates, catching each one fully up to asOfDate (not just one occurrence
 * per template) so the app stays correct even if it wasn't opened on the
 * exact due date.
 */
@Service
public class RunDueRecurringTransactionsService {

    private final RecurringTransactionRepository repository;
    private final GenerateRecurringTransactionNowService generateService;

    public RunDueRecurringTransactionsService(
            RecurringTransactionRepository repository,
            GenerateRecurringTransactionNowService generateService
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.generateService = Objects.requireNonNull(generateService);
    }

    public List<RecurringTransaction> execute(LocalDate asOfDate) {

        Objects.requireNonNull(asOfDate, "Date cannot be null.");

        return repository.findDueForAutoGeneration(asOfDate)
                .stream()
                .map(template -> generateService.generateAllDueOccurrences(template, asOfDate))
                .toList();
    }
}
