package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

import java.time.YearMonth;

public record MonthlyAmount(

        YearMonth yearMonth,

        Money total

) {
}
