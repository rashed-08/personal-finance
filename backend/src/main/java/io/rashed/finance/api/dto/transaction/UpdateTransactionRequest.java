package io.rashed.finance.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTransactionRequest(

        LocalDate transactionDate,

        @NotNull
        @Positive
        BigDecimal amount,

        UUID fromAccountId,

        UUID toAccountId,

        UUID categoryId,

        String description,

        String notes
) {
}