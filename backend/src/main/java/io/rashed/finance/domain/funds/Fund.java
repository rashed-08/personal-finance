package io.rashed.finance.domain.funds;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

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
     * Target amount (optional).
     */
    private final Money targetAmount;

    /**
     * Current balance is calculated from ledger.
     */
    private final Money openingBalance;

    private final boolean active;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Fund(
            FundId id,
            String name,
            FundType fundType,
            Money targetAmount,
            Money openingBalance,
            boolean active,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.fundType = Objects.requireNonNull(fundType);

        this.targetAmount = targetAmount;
        this.openingBalance = Objects.requireNonNull(openingBalance);

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
            Money openingBalance,
            String description
    ) {

        validateName(name);
        validateOpeningBalance(openingBalance);
        validateTargetAmount(targetAmount);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new Fund(
                FundId.newId(),
                name,
                fundType,
                targetAmount,
                openingBalance,
                true,
                description,
                now,
                now
        );
    }

    public static Fund emergencyFund(Money openingBalance) {

        return create(
                "Emergency Fund",
                FundType.EMERGENCY,
                null,
                openingBalance,
                null
        );
    }

    public static Fund zakatFund(Money openingBalance) {

        return create(
                "Zakat Fund",
                FundType.ZAKAT,
                null,
                openingBalance,
                null
        );
    }

    public static Fund savingsFund(
            String name,
            Money targetAmount,
            Money openingBalance
    ) {

        return create(
                name,
                FundType.SAVINGS,
                targetAmount,
                openingBalance,
                null
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(
                name,
                "Fund name cannot be null."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Fund name cannot be empty."
            );
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Fund name cannot exceed 100 characters."
            );
        }
    }

    private static void validateOpeningBalance(Money openingBalance) {

        Objects.requireNonNull(
                openingBalance,
                "Opening balance cannot be null."
        );

        if (openingBalance.isNegative()) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative."
            );
        }
    }

    private static void validateTargetAmount(Money targetAmount) {

        if (targetAmount != null && targetAmount.isNegative()) {
            throw new IllegalArgumentException(
                    "Target amount cannot be negative."
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
                id,
                newName,
                fundType,
                targetAmount,
                openingBalance,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Fund changeDescription(String newDescription) {

        validateDescription(newDescription);

        return new Fund(
                id,
                name,
                fundType,
                targetAmount,
                openingBalance,
                active,
                newDescription,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Fund changeTargetAmount(Money newTargetAmount) {

        validateTargetAmount(newTargetAmount);

        return new Fund(
                id,
                name,
                fundType,
                newTargetAmount,
                openingBalance,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Fund activate() {

        if (active) {
            return this;
        }

        return new Fund(
                id,
                name,
                fundType,
                targetAmount,
                openingBalance,
                true,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Fund deactivate() {

        if (!active) {
            return this;
        }

        return new Fund(
                id,
                name,
                fundType,
                targetAmount,
                openingBalance,
                false,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }
}