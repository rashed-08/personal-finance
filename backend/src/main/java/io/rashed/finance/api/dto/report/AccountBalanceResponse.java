package io.rashed.finance.api.dto.report;

import io.rashed.finance.common.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(

        UUID accountId,

        String accountName,

        AccountType accountType,

        BigDecimal balance

) {
}
