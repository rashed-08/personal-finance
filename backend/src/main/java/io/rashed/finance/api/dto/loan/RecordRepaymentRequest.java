package io.rashed.finance.api.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordRepaymentRequest(

        @NotNull(message = "Account is required.")
        UUID accountId,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Payment date is required.")
        LocalDate paymentDate,

        @NotNull(message = "Salary cycle is required.")
        UUID salaryCycleId,

        @Size(max = 255)
        String description

) {
}
