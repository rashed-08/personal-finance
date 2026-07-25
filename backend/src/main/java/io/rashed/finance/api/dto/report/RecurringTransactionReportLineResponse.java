package io.rashed.finance.api.dto.report;

import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionReportLineResponse(

        UUID recurringTransactionId,

        String name,

        boolean active,

        LocalDate nextExecutionDate,

        LocalDate lastExecutionDate,

        long generatedCount,

        long skippedCount

) {
}
