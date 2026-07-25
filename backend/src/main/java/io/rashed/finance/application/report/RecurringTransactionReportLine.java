package io.rashed.finance.application.report;

import io.rashed.finance.domain.recurring.RecurringTransactionId;

import java.time.LocalDate;

public record RecurringTransactionReportLine(

        RecurringTransactionId recurringTransactionId,

        String name,

        boolean active,

        LocalDate nextExecutionDate,

        LocalDate lastExecutionDate,

        long generatedCount,

        long skippedCount

) {
}
