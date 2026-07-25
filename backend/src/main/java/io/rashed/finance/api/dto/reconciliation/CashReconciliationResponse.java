package io.rashed.finance.api.dto.reconciliation;

import io.rashed.finance.common.enums.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CashReconciliationResponse(

        UUID id,

        UUID accountId,

        LocalDate reconciliationDate,

        BigDecimal expectedCashAmount,

        BigDecimal actualCashAmount,

        BigDecimal differenceAmount,

        ReconciliationStatus status,

        UUID adjustmentTransactionId,

        String notes,

        List<CashSnapshotResponse> snapshots,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
