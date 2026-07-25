package io.rashed.finance.api.dto.fund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFundRequest(

        @NotBlank(message = "Fund name is required.")
        @Size(max = 100)
        String name,

        @DecimalMin(
                value = "0.00",
                inclusive = false,
                message = "Target amount must be greater than zero when specified."
        )
        BigDecimal targetAmount,

        LocalDate targetDate,

        @Size(max = 500)
        String description

) {
}
