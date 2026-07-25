package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;

/**
 * CashReconciliation already stores exactly the figures the doc's Cash
 * Reconciliation Report calls for (expected/actual/difference/status/
 * adjustment) — this service is a filtered read, not a transformation.
 */
@Service
public class GetCashReconciliationReportService {

    private final CashReconciliationRepository repository;

    public GetCashReconciliationReportService(CashReconciliationRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<CashReconciliation> execute(AccountId accountId, LocalDate fromDate, LocalDate toDate) {

        if (accountId != null) {
            return repository.findByAccount(accountId);
        }

        if (fromDate != null && toDate != null) {
            return repository.findByDateRange(fromDate, toDate);
        }

        return repository.findAll();
    }
}
