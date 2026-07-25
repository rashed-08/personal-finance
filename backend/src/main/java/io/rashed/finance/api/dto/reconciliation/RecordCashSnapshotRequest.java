package io.rashed.finance.api.dto.reconciliation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record RecordCashSnapshotRequest(

        @NotNull
        @PositiveOrZero
        BigDecimal cashAmount,

        String notes
) {
}
