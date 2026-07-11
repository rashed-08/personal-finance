package io.rashed.finance.api.dto.account;

import io.rashed.finance.common.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank(message = "Account name is required.")
        @Size(max = 100, message = "Account name cannot exceed 100 characters.")
        String name,

        @NotNull(message = "Account type is required.")
        AccountType accountType,

        @NotNull(message = "Opening balance is required.")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Opening balance cannot be negative."
        )
        BigDecimal openingBalance,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters."
        )
        String description

) {
}