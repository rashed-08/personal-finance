package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LoanPaymentHistoryLineResponse(

        UUID transactionId,

        LocalDate date,

        BigDecimal amount,

        String description

) {
}
