package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;

public record LoanSummaryResponse(

        BigDecimal totalReceivable,

        BigDecimal totalPayable,

        BigDecimal netPosition,

        long activeLoanCount

) {
}
