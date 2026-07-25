package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyAmountResponse(

        YearMonth yearMonth,

        BigDecimal total

) {
}
