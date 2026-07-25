package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.FundId;

import java.math.BigDecimal;

public record FundReportLine(

        FundId fundId,

        String fundName,

        FundType fundType,

        /** Null when the fund has no target amount. */
        Money targetAmount,

        Money allocatedAmount,

        Money usedAmount,

        Money remainingBalance,

        /** Null when the fund has no target amount. */
        BigDecimal progressPercentage

) {
}
