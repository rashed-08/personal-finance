package io.rashed.finance.api.dto.account;

import io.rashed.finance.common.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(

        UUID id,

        String name,

        AccountType accountType,

        BigDecimal openingBalance,

        boolean active,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}