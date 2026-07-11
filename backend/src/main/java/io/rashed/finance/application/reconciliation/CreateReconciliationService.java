package io.rashed.finance.application.reconciliation;

import io.rashed.finance.domain.reconciliation.Reconciliation;
import io.rashed.finance.domain.reconciliation.ReconciliationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CreateReconciliationService {

    private final ReconciliationRepository repository;

    public Reconciliation create(
            CreateReconciliationCommand command
    ) {

        Reconciliation reconciliation =
                Reconciliation.create(
                        command.accountId(),
                        command.reconciliationDate(),
                        command.expectedBalance(),
                        command.actualBalance(),
                        command.description()
                );

        return repository.save(reconciliation);
    }
}