package io.rashed.finance.api.dto.recurring;

import io.rashed.finance.common.enums.Frequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateRecurringTransactionRequest(

        @NotBlank(message = "Name is required.")
        @Size(max = 100)
        String name,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Frequency is required.")
        Frequency frequency,

        LocalDate endDate,

        boolean autoGenerate,

        @Size(max = 255)
        String description,

        String notes

) {
}
