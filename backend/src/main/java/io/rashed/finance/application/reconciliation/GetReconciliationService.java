package io.rashed.finance.application.reconciliation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;

@Service
public class GetReconciliationService {

    private final CashReconciliationRepository repository;

    public GetReconciliationService(CashReconciliationRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public CashReconciliation execute(CashReconciliationId id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found."));
    }
}
