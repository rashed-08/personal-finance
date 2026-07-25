package io.rashed.finance.api.dto.recurring;

import io.rashed.finance.common.enums.RecurringExecutionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringTransactionExecutionResponse(

        UUID id,

        UUID recurringTransactionId,

        LocalDate scheduledDate,

        RecurringExecutionStatus status,

        UUID transactionId,

        String reason,

        LocalDateTime createdAt

) {
}
