package io.rashed.finance.common.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final int SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    // -------------------------------------------------------------------------
    // Arithmetic
    // -------------------------------------------------------------------------

    public Money add(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return new Money(amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Multiplier cannot be null");
        return new Money(amount.multiply(multiplier));
    }

    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        return new Money(amount.divide(divisor, SCALE, ROUNDING_MODE));
    }

    public Money negate() {
        return new Money(amount.negate());
    }

    public Money abs() {
        return new Money(amount.abs());
    }

    // -------------------------------------------------------------------------
    // Comparison
    // -------------------------------------------------------------------------

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean greaterThan(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return amount.compareTo(other.amount) > 0;
    }

    public boolean greaterThanOrEqual(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean lessThan(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return amount.compareTo(other.amount) < 0;
    }

    public boolean lessThanOrEqual(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return amount.compareTo(other.amount) <= 0;
    }

    @Override
    public int compareTo(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");
        return amount.compareTo(other.amount);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public BigDecimal getAmount() {
        return amount;
    }

    // -------------------------------------------------------------------------
    // Object Methods
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Money money)) {
            return false;
        }

        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}