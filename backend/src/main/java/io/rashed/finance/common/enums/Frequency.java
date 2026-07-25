package io.rashed.finance.common.enums;

import java.time.LocalDate;

/**
 * Recurring transaction frequency.
 */
public enum Frequency {

    DAILY,

    WEEKLY,

    MONTHLY,

    YEARLY;

    /** The next occurrence after the given date, anchored on that date rather than "today". */
    public LocalDate advance(LocalDate from) {

        return switch (this) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
        };
    }
}
