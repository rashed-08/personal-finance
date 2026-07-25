package io.rashed.finance.application.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.recurring.RecurringTransactionId;

import java.time.LocalDate;

public record UpdateRecurringTransactionCommand(

        RecurringTransactionId recurringTransactionId,

        String name,

        Money amount,

        Frequency frequency,

        LocalDate endDate,

        boolean autoGenerate,

        String description,

        String notes

) {
}
