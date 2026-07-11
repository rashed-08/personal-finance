package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.domain.accounts.AccountId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReconciliationRepository {

    Reconciliation save(Reconciliation reconciliation);

    Optional<Reconciliation> findById(ReconciliationId id);

    List<Reconciliation> findByAccount(AccountId accountId);

    List<Reconciliation> findByDateRange(
            LocalDate fromDate,
            LocalDate toDate
    );

    void deleteById(ReconciliationId id);

    boolean existsById(ReconciliationId id);
}