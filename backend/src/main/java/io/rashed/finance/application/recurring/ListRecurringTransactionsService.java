package io.rashed.finance.application.recurring;

import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListRecurringTransactionsService {

    private final RecurringTransactionRepository repository;

    public ListRecurringTransactionsService(RecurringTransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<RecurringTransaction> execute(boolean activeOnly) {

        return activeOnly ? repository.findActive() : repository.findAll();
    }
}
