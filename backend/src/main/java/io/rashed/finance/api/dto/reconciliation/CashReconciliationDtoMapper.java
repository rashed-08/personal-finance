package io.rashed.finance.api.dto.reconciliation;

import io.rashed.finance.application.reconciliation.StartReconciliationCommand;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashSnapshot;

import java.util.List;

public final class CashReconciliationDtoMapper {

    private CashReconciliationDtoMapper() {
    }

    public static StartReconciliationCommand toCommand(StartReconciliationRequest request) {

        return new StartReconciliationCommand(
                AccountId.of(request.accountId()),
                request.reconciliationDate(),
                request.notes()
        );
    }

    public static CashReconciliationResponse toResponse(CashReconciliation reconciliation) {

        return new CashReconciliationResponse(
                reconciliation.getId().getValue(),
                reconciliation.getAccountId().getValue(),
                reconciliation.getReconciliationDate(),
                reconciliation.getExpectedCashAmount().getAmount(),
                reconciliation.getActualCashAmount() == null ? null : reconciliation.getActualCashAmount().getAmount(),
                reconciliation.getDifference() == null ? null : reconciliation.getDifference().getAmount(),
                reconciliation.getStatus(),
                reconciliation.getAdjustmentTransactionId() == null ? null : reconciliation.getAdjustmentTransactionId().getValue(),
                reconciliation.getNotes(),
                reconciliation.getSnapshots().stream().map(CashReconciliationDtoMapper::toResponse).toList(),
                reconciliation.getCreatedAt(),
                reconciliation.getUpdatedAt()
        );
    }

    public static CashSnapshotResponse toResponse(CashSnapshot snapshot) {

        return new CashSnapshotResponse(
                snapshot.getId().getValue(),
                snapshot.getCashAmount().getAmount(),
                snapshot.getNotes(),
                snapshot.getSnapshotTime()
        );
    }

    public static List<CashReconciliationResponse> toResponseList(List<CashReconciliation> reconciliations) {

        return reconciliations.stream()
                .map(CashReconciliationDtoMapper::toResponse)
                .toList();
    }
}
