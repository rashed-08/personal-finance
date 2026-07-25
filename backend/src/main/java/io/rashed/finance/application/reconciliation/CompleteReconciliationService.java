package io.rashed.finance.application.reconciliation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Finalizes a reconciliation. When the actual and expected cash amounts
 * differ, this creates the adjustment transaction that brings the ledger
 * back in line with reality — the original transactions are never touched.
 */
@Service
public class CompleteReconciliationService {

    private final CashReconciliationRepository repository;
    private final TransactionRepository transactionRepository;

    public CompleteReconciliationService(
            CashReconciliationRepository repository,
            TransactionRepository transactionRepository
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public CashReconciliation execute(CashReconciliationId id) {

        CashReconciliation reconciliation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found."));

        CashReconciliation completed = reconciliation.complete();

        if (completed.requiresAdjustment()) {

            AccountId accountId = completed.getAccountId();
            boolean increase = completed.getDifference().isPositive();
            Money amount = completed.getDifference().abs();

            Transaction adjustment = Transaction.adjustment(
                    TransactionId.newId(),
                    completed.getReconciliationDate(),
                    amount,
                    increase ? null : accountId,
                    increase ? accountId : null,
                    null,
                    AdjustmentReason.CASH_RECONCILIATION,
                    "Cash reconciliation adjustment",
                    completed.getNotes()
            );

            Transaction saved = transactionRepository.save(adjustment);
            completed = completed.linkAdjustment(saved.getId());
        }

        return repository.save(completed);
    }
}
