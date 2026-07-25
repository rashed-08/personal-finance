package io.rashed.finance.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(

        @NotNull
        TransactionType transactionType,

        @NotNull
        LocalDate transactionDate,

        @NotNull
        @Positive
        BigDecimal amount,

        UUID fromAccountId,

        UUID toAccountId,

        UUID categoryId,

        UUID salaryCycleId,

        String description,

        String notes,

        AdjustmentReason adjustmentReason,

        String migrationBatchId,

        UUID referenceTransactionId,

        /**
         * INCOME only. When true, the server closes the currently open
         * salary cycle (if any) the day before transactionDate and opens a
         * new one, ignoring salaryCycleId.
         */
        boolean startsNewSalaryCycle
) {
}