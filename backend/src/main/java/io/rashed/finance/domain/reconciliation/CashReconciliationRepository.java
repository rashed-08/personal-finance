package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.domain.accounts.AccountId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CashReconciliationRepository {

    CashReconciliation save(CashReconciliation reconciliation);

    Optional<CashReconciliation> findById(CashReconciliationId id);

    List<CashReconciliation> findByAccount(AccountId accountId);

    List<CashReconciliation> findByDateRange(LocalDate fromDate, LocalDate toDate);

    List<CashReconciliation> findAll();

    /** Whether the account already has a reconciliation still in progress. */
    boolean existsPendingForAccount(AccountId accountId);
}
