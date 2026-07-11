package io.rashed.finance.api.dto.account;

import io.rashed.finance.common.enums.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;


public record UpdateAccountRequest(

        @NotBlank(message = "Account name is required.")
        @Size(max = 100)
        String name,


        @NotNull(message = "Account type is required.")
        AccountType accountType,


        @NotNull
        @DecimalMin("0.00")
        BigDecimal openingBalance,


        @Size(max = 500)
        String description

) {
}