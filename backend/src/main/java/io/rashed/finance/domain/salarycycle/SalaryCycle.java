package io.rashed.finance.domain.salarycycle;

import io.rashed.finance.common.valueobject.DateRange;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class SalaryCycle {

    private final SalaryCycleId id;

    /**
     * Example:
     * "July 2026"
     * "August 2026"
     */
    private final String name;

    /**
     * Expense period covered by this salary.
     */
    private final DateRange period;

    /**
     * Expected salary payment date.
     */
    private final LocalDate salaryDate;

    /**
     * Only one salary cycle should normally remain open.
     */
    private final boolean closed;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public SalaryCycle(
            SalaryCycleId id,
            String name,
            DateRange period,
            LocalDate salaryDate,
            boolean closed,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.period = Objects.requireNonNull(period);
        this.salaryDate = Objects.requireNonNull(salaryDate);

        this.closed = closed;
        this.description = description;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static SalaryCycle create(
            String name,
            DateRange period,
            LocalDate salaryDate,
            String description
    ) {

        validateName(name);
        validatePeriod(period);
        validateSalaryDate(salaryDate);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new SalaryCycle(
                SalaryCycleId.newId(),
                name,
                period,
                salaryDate,
                false,
                description,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(
                name,
                "Salary cycle name cannot be null."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Salary cycle name cannot be empty."
            );
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Salary cycle name cannot exceed 100 characters."
            );
        }
    }

    private static void validatePeriod(DateRange period) {

        Objects.requireNonNull(
                period,
                "Salary cycle period cannot be null."
        );
    }

    private static void validateSalaryDate(LocalDate salaryDate) {

        Objects.requireNonNull(
                salaryDate,
                "Salary date cannot be null."
        );

        if (salaryDate.isAfter(LocalDate.now().plusYears(10))) {
            throw new IllegalArgumentException(
                    "Salary date is too far in the future."
            );
        }

        if (salaryDate.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalArgumentException(
                    "Salary date is invalid."
            );
        }
    }

    private static void validateDescription(String description) {

        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 500 characters."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpen() {
        return !closed;
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null.");
        return period.contains(date);
    }

    public boolean startsOn(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null.");
        return period.getStartDate().equals(date);
    }

    public boolean endsOn(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null.");
        return period.getEndDate().equals(date);
    }

    public boolean isCurrent() {
        return contains(LocalDate.now());
    }

    public SalaryCycle rename(String newName) {

        validateName(newName);

        return new SalaryCycle(
                id,
                newName,
                period,
                salaryDate,
                closed,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public SalaryCycle changeDescription(String newDescription) {

        validateDescription(newDescription);

        return new SalaryCycle(
                id,
                name,
                period,
                salaryDate,
                closed,
                newDescription,
                createdAt,
                LocalDateTime.now()
        );
    }

    public SalaryCycle close() {

        if (closed) {
            return this;
        }

        return new SalaryCycle(
                id,
                name,
                period,
                salaryDate,
                true,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public SalaryCycle reopen() {

        if (!closed) {
            return this;
        }

        return new SalaryCycle(
                id,
                name,
                period,
                salaryDate,
                false,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }
}