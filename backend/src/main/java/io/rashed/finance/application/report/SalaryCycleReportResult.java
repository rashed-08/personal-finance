package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;

public record SalaryCycleReportResult(

        SalaryCycleId salaryCycleId,

        String cycleName,

        LocalDate startDate,

        LocalDate endDate,

        boolean closed,

        Money openingBalance,

        Money income,

        Money expenses,

        Money adjustments,

        Money closingBalance

) {
}
