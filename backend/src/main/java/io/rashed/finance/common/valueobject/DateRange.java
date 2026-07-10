package io.rashed.finance.common.valueobject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Immutable value object representing a date range.
 *
 * Used for:
 * - Salary Cycles
 * - Reports
 * - Filtering
 * - Recurring Transactions
 * - Budget Periods
 */
public final class DateRange implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate startDate;
    private final LocalDate endDate;

    private DateRange(LocalDate startDate, LocalDate endDate) {

        this.startDate = Objects.requireNonNull(startDate, "Start date cannot be null");
        this.endDate = Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // -------------------------------------------------------------------------
    // Query Methods
    // -------------------------------------------------------------------------

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date);

        return !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }

    public boolean contains(DateRange other) {
        Objects.requireNonNull(other);

        return !other.startDate.isBefore(startDate)
                && !other.endDate.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other);

        return !endDate.isBefore(other.startDate)
                && !startDate.isAfter(other.endDate);
    }

    public boolean isBefore(DateRange other) {
        Objects.requireNonNull(other);

        return endDate.isBefore(other.startDate);
    }

    public boolean isAfter(DateRange other) {
        Objects.requireNonNull(other);

        return startDate.isAfter(other.endDate);
    }

    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    // -------------------------------------------------------------------------
    // Object Methods
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DateRange other)) {
            return false;
        }

        return startDate.equals(other.startDate)
                && endDate.equals(other.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "DateRange[" +
                startDate +
                " -> " +
                endDate +
                "]";
    }
}