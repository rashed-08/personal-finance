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
         * TRANSFER only. When set, this is a fund allocation/withdrawal:
         * exactly one of fromAccountId/toAccountId must also be set (the
         * real account side), and the fund occupies the other side.
         */
        UUID fundId,

        /**
         * INCOME only. When true, the server closes the currently open
         * salary cycle (if any) the day before transactionDate and opens a
         * new one, ignoring salaryCycleId. Boxed (rather than primitive) so
         * a client omitting the field or sending it as JSON null doesn't
         * fail deserialization — it's normalized to false below.
         */
        Boolean startsNewSalaryCycle
) {
    public CreateTransactionRequest {
        startsNewSalaryCycle = startsNewSalaryCycle != null && startsNewSalaryCycle;
    }
}