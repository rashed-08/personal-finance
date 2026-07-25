package io.rashed.finance.application.reconciliation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;

@Service
public class ListReconciliationsService {

    private final CashReconciliationRepository repository;

    public ListReconciliationsService(CashReconciliationRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<CashReconciliation> execute(Optional<AccountId> accountId) {

        return accountId
                .map(repository::findByAccount)
                .orElseGet(repository::findAll);
    }
}
