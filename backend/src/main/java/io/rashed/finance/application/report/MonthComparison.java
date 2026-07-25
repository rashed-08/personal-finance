package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

public record MonthComparison(

        Money currentIncome,

        Money previousIncome,

        Money currentExpense,

        Money previousExpense

) {
}
