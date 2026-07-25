package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryCycleReportResponse(

        UUID salaryCycleId,

        String cycleName,

        LocalDate startDate,

        LocalDate endDate,

        boolean closed,

        BigDecimal openingBalance,

        BigDecimal income,

        BigDecimal expenses,

        BigDecimal adjustments,

        BigDecimal closingBalance

) {
}
