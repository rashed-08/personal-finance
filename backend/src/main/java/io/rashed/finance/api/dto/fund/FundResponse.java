package io.rashed.finance.api.dto.fund;

import io.rashed.finance.common.enums.FundType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FundResponse(

        UUID id,

        String name,

        FundType fundType,

        BigDecimal targetAmount,

        LocalDate targetDate,

        BigDecimal balance,

        boolean active,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
