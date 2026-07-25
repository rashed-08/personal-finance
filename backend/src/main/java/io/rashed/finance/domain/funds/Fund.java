package io.rashed.finance.domain.funds;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A logical reservation of money toward a purpose. A fund never owns money
 * directly — its balance is always derived from allocation/withdrawal
 * transactions, never stored here. See CalculateFundBalanceService and
 * docs/database/tables/ funds.md ("Fund balances are never stored").
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Fund {

    private final FundId id;

    /**
     * Example:
     * Emergency Fund
     * Zakat Fund
     * Vacation Fund
     * New Laptop
     */
    private final String name;

    private final FundType fundType;

    /**
     * Optional savings goal.
     */
    private final Money targetAmount;

    /**
     * Optional target completion date.
     */
    private final LocalDate targetDate;

    private final boolean active;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Fund(
            FundId id,
            String name,
            FundType fundType,
            Money targetAmount,
            LocalDate targetDate,
            boolean active,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.fundType = Objects.requireNonNull(fundType);

        this.targetAmount = targetAmount;
        this.targetDate = targetDate;

        this.active = active;
        this.description = description;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Fund create(
            String name,
            FundType fundType,
            Money targetAmount,
            LocalDate targetDate,
            String description
    ) {

        validateName(name);
        validateTargetAmount(targetAmount);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new Fund(
                FundId.newId(),
                name,
                fundType,
                targetAmount,
                targetDate,
                true,
                description,
                now,
                now
        );
    }

    public static Fund emergencyFund() {
        return create("Emergency Fund", FundType.EMERGENCY, null, null, null);
    }

    public static Fund zakatFund() {
        return create("Zakat Fund", FundType.ZAKAT, null, null, null);
    }

    public static Fund savingsFund(String name, Money targetAmount, LocalDate targetDate) {
        return create(name, FundType.SAVINGS, targetAmount, targetDate, null);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Fund name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Fund name cannot be empty.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Fund name cannot exceed 100 characters.");
        }
    }

    private static void validateTargetAmount(Money targetAmount) {

        if (targetAmount != null && !targetAmount.isPositive()) {
            throw new IllegalArgumentException("Target amount must be greater than zero when specified.");
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

    public boolean isActive() {
        return active;
    }

    public boolean isInactive() {
        return !active;
    }

    public boolean hasTargetAmount() {
        return targetAmount != null;
    }

    public boolean isEmergencyFund() {
        return fundType == FundType.EMERGENCY;
    }

    public boolean isSavingsFund() {
        return fundType == FundType.SAVINGS;
    }

    public boolean isZakatFund() {
        return fundType == FundType.ZAKAT;
    }

    public Fund rename(String newName) {

        validateName(newName);

        return new Fund(
                id, newName, fundType, targetAmount, targetDate, active, description, createdAt, LocalDateTime.now());
    }

    public Fund changeDescription(String newDescription) {

        validateDescription(newDescription);

        return new Fund(
                id, name, fundType, targetAmount, targetDate, active, newDescription, createdAt, LocalDateTime.now());
    }

    public Fund changeTarget(Money newTargetAmount, LocalDate newTargetDate) {

        validateTargetAmount(newTargetAmount);

        return new Fund(
                id, name, fundType, newTargetAmount, newTargetDate, active, description, createdAt, LocalDateTime.now());
    }

    public Fund activate() {

        if (active) {
            return this;
        }

        return new Fund(
                id, name, fundType, targetAmount, targetDate, true, description, createdAt, LocalDateTime.now());
    }

    public Fund deactivate() {

        if (!active) {
            return this;
        }

        return new Fund(
                id, name, fundType, targetAmount, targetDate, false, description, createdAt, LocalDateTime.now());
    }
}
