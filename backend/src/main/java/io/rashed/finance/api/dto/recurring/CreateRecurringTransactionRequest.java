package io.rashed.finance.api.dto.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringTransactionRequest(

        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name cannot exceed 100 characters.")
        String name,

        @NotNull(message = "Transaction type is required.")
        TransactionType transactionType,

        UUID fromAccountId,

        UUID toAccountId,

        UUID categoryId,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Frequency is required.")
        Frequency frequency,

        @NotNull(message = "Start date is required.")
        LocalDate startDate,

        LocalDate endDate,

        boolean autoGenerate,

        @Size(max = 255, message = "Description cannot exceed 255 characters.")
        String description,

        String notes

) {
}
