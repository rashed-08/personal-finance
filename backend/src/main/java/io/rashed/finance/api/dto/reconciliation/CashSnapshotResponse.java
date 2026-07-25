package io.rashed.finance.api.dto.reconciliation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashSnapshotResponse(

        UUID id,

        BigDecimal cashAmount,

        String notes,

        LocalDateTime snapshotTime
) {
}
