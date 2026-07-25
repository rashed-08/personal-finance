package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;

public record MonthComparisonResponse(

        BigDecimal currentIncome,

        BigDecimal previousIncome,

        BigDecimal currentExpense,

        BigDecimal previousExpense

) {
}
