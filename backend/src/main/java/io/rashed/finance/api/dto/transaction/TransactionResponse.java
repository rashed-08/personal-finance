package io.rashed.finance.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;

public record TransactionResponse(

        UUID id,

        TransactionType transactionType,

        TransactionStatus transactionStatus,

        LocalDate transactionDate,

        BigDecimal amount,

        UUID fromAccountId,

        UUID toAccountId,

        UUID categoryId,

        UUID salaryCycleId,

        String referenceNumber,

        UUID fundId,

        UUID loanId,

        AdjustmentReason adjustmentReason,

        String description,

        String notes,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}