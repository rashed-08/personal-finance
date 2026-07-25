package io.rashed.finance.domain.salarycycle;

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

    private final LocalDate startDate;

    /**
     * Null while the cycle is still open. A cycle's end is not known until
     * the next salary payment closes it.
     */
    private final LocalDate endDate;

    /**
     * Expected/actual salary payment date.
     */
    private final LocalDate salaryDate;

    /**
     * Only one salary cycle should normally remain open at a time.
     */
    private final boolean closed;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public SalaryCycle(
            SalaryCycleId id,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate salaryDate,
            boolean closed,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.startDate = Objects.requireNonNull(startDate);

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        if (closed && endDate == null) {
            throw new IllegalArgumentException("A closed salary cycle must have an end date.");
        }

        this.endDate = endDate;
        this.salaryDate = Objects.requireNonNull(salaryDate);

        this.closed = closed;
        this.description = description;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    /** Opens a new, ongoing cycle with no known end date yet. */
    public static SalaryCycle open(String name, LocalDate startDate, LocalDate salaryDate, String description) {

        validateName(name);
        Objects.requireNonNull(startDate, "Salary cycle start date cannot be null.");
        validateSalaryDate(salaryDate);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new SalaryCycle(SalaryCycleId.newId(), name, startDate, null, salaryDate, false, description, now, now);
    }

    /**
     * Creates a cycle with an already-known start and end date, e.g. when
     * backfilling historical data. endDate may be null to create it open.
     */
    public static SalaryCycle create(String name, LocalDate startDate, LocalDate endDate, LocalDate salaryDate, String description) {

        validateName(name);
        Objects.requireNonNull(startDate, "Salary cycle start date cannot be null.");
        validateSalaryDate(salaryDate);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new SalaryCycle(SalaryCycleId.newId(), name, startDate, endDate, salaryDate, endDate != null, description, now, now);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Salary cycle name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Salary cycle name cannot be empty.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Salary cycle name cannot exceed 100 characters.");
        }
    }

    private static void validateSalaryDate(LocalDate salaryDate) {

        Objects.requireNonNull(salaryDate, "Salary date cannot be null.");

        if (salaryDate.isAfter(LocalDate.now().plusYears(10))) {
            throw new IllegalArgumentException("Salary date is too far in the future.");
        }

        if (salaryDate.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalArgumentException("Salary date is invalid.");
        }
    }

    private static void validateDescription(String description) {

        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters.");
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

    public boolean containsDate(LocalDate date) {

        Objects.requireNonNull(date, "Date cannot be null.");

        if (date.isBefore(startDate)) {
            return false;
        }

        return endDate == null || !date.isAfter(endDate);
    }

    public boolean isCurrent() {
        return containsDate(LocalDate.now());
    }

    public SalaryCycle update(String newName, LocalDate newSalaryDate, String newDescription) {

        validateName(newName);
        validateSalaryDate(newSalaryDate);
        validateDescription(newDescription);

        return new SalaryCycle(id, newName, startDate, endDate, newSalaryDate, closed, newDescription, createdAt, LocalDateTime.now());
    }

    /**
     * Closes an open cycle as of the given date. Reject re-closing an
     * already-closed cycle; reopen it first to change its end date.
     */
    public SalaryCycle close(LocalDate closingDate) {

        if (closed) {
            throw new IllegalStateException("Salary cycle is already closed.");
        }

        Objects.requireNonNull(closingDate, "Closing date cannot be null.");

        if (closingDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Closing date cannot be before the cycle's start date.");
        }

        return new SalaryCycle(id, name, startDate, closingDate, salaryDate, true, description, createdAt, LocalDateTime.now());
    }

    /** Reopens a closed cycle, clearing its end date so it becomes ongoing again. */
    public SalaryCycle reopen() {

        if (!closed) {
            throw new IllegalStateException("Salary cycle is already open.");
        }

        return new SalaryCycle(id, name, startDate, null, salaryDate, false, description, createdAt, LocalDateTime.now());
    }
}
