package io.rashed.finance.api.dto.reconciliation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record StartReconciliationRequest(

        @NotNull
        UUID accountId,

        @NotNull
        LocalDate reconciliationDate,

        String notes
) {
}
