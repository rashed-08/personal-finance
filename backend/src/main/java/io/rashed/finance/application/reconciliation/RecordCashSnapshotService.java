package io.rashed.finance.application.reconciliation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;

@Service
public class RecordCashSnapshotService {

    private final CashReconciliationRepository repository;

    public RecordCashSnapshotService(CashReconciliationRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public CashReconciliation execute(CashReconciliationId id, Money cashAmount, String notes) {

        CashReconciliation reconciliation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found."));

        return repository.save(reconciliation.addSnapshot(cashAmount, notes));
    }
}
