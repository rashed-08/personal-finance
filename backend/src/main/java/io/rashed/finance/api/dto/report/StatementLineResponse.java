package io.rashed.finance.api.dto.report;

import io.rashed.finance.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StatementLineResponse(

        UUID transactionId,

        LocalDate transactionDate,

        String description,

        TransactionType transactionType,

        BigDecimal signedAmount,

        BigDecimal runningBalance

) {
}
