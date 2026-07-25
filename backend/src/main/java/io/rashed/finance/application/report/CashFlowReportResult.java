package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

import java.time.LocalDate;

public record CashFlowReportResult(

        LocalDate fromDate,

        LocalDate toDate,

        Money moneyIn,

        Money moneyOut,

        Money netCashFlow,

        /** Transfers never affect net worth — shown separately per 07-reporting.md 5.2. */
        Money totalTransferVolume

) {
}
