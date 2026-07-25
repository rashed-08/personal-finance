package io.rashed.finance.application.report;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

@Service
public class GetRecurringTransactionReportService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionExecutionRepository executionRepository;

    public GetRecurringTransactionReportService(
            RecurringTransactionRepository recurringTransactionRepository,
            RecurringTransactionExecutionRepository executionRepository
    ) {
        this.recurringTransactionRepository = Objects.requireNonNull(recurringTransactionRepository);
        this.executionRepository = Objects.requireNonNull(executionRepository);
    }

    public List<RecurringTransactionReportLine> execute(boolean activeOnly) {

        List<RecurringTransaction> templates =
                activeOnly ? recurringTransactionRepository.findActive() : recurringTransactionRepository.findAll();

        return templates.stream().map(this::toLine).toList();
    }

    private RecurringTransactionReportLine toLine(RecurringTransaction template) {

        List<RecurringTransactionExecution> executions =
                executionRepository.findByRecurringTransactionId(template.getId());

        long generated = executions.stream().filter(RecurringTransactionExecution::isGenerated).count();
        long skipped = executions.stream().filter(RecurringTransactionExecution::isSkipped).count();

        return new RecurringTransactionReportLine(
                template.getId(),
                template.getName(),
                template.isActive(),
                template.getNextExecutionDate(),
                template.getLastExecutionDate(),
                generated,
                skipped
        );
    }
}
