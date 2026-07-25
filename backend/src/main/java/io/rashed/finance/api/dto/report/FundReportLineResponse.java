package io.rashed.finance.api.dto.report;

import io.rashed.finance.common.enums.FundType;

import java.math.BigDecimal;
import java.util.UUID;

public record FundReportLineResponse(

        UUID fundId,

        String fundName,

        FundType fundType,

        BigDecimal targetAmount,

        BigDecimal allocatedAmount,

        BigDecimal usedAmount,

        BigDecimal remainingBalance,

        BigDecimal progressPercentage

) {
}
