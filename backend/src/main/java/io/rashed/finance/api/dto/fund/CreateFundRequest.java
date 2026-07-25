package io.rashed.finance.api.dto.fund;

import io.rashed.finance.common.enums.FundType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFundRequest(

        @NotBlank(message = "Fund name is required.")
        @Size(max = 100, message = "Fund name cannot exceed 100 characters.")
        String name,

        @NotNull(message = "Fund type is required.")
        FundType fundType,

        @DecimalMin(
                value = "0.00",
                inclusive = false,
                message = "Target amount must be greater than zero when specified."
        )
        BigDecimal targetAmount,

        LocalDate targetDate,

        @Size(max = 500, message = "Description cannot exceed 500 characters.")
        String description

) {
}
