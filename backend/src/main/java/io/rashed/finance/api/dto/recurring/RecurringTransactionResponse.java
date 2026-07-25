package io.rashed.finance.api.dto.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringTransactionResponse(

        UUID id,

        String name,

        TransactionType transactionType,

        UUID fromAccountId,

        UUID toAccountId,

        UUID categoryId,

        BigDecimal amount,

        Frequency frequency,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate nextExecutionDate,

        LocalDate lastExecutionDate,

        boolean autoGenerate,

        boolean active,

        String description,

        String notes,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
