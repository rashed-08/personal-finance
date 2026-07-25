package io.rashed.finance.application.recurring;

import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Live "due now" view — includes both autoGenerate and manual-confirm templates. */
@Service
public class ListDueRecurringTransactionsService {

    private final RecurringTransactionRepository repository;

    public ListDueRecurringTransactionsService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<RecurringTransaction> execute(LocalDate asOfDate) {

        return repository.findDue(asOfDate);
    }
}
