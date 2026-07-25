package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowReportResponse(

        LocalDate fromDate,

        LocalDate toDate,

        BigDecimal moneyIn,

        BigDecimal moneyOut,

        BigDecimal netCashFlow,

        BigDecimal totalTransferVolume

) {
}
