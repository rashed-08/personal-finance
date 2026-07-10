package io.rashed.finance.domain.accounts;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.valueobject.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Account {

    private final AccountId id;

    private final String name;

    private final AccountType accountType;

    /**
     * Initial balance at the time the account was created.
     *
     * Current balance is always calculated from the ledger.
     */
    private final Money openingBalance;

    private final boolean active;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Account(AccountId id, String name, AccountType accountType, Money openingBalance, boolean active, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = Objects.requireNonNull(id);
        validateName(name);
        this.name = name.trim();
        this.accountType = Objects.requireNonNull(accountType);
        this.openingBalance = Objects.requireNonNull(openingBalance);

        this.active = active;
        this.description = description;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Account create(String name, AccountType accountType, Money openingBalance, String description) {

        validateName(name);
        validateOpeningBalance(openingBalance);

        LocalDateTime now = LocalDateTime.now();

        return new Account(AccountId.newId(), name, accountType, openingBalance, true, description, now, now);
    }

    public static Account createCashAccount(String name, Money openingBalance) {

        return create(name, AccountType.CASH, openingBalance, null);
    }

    public static Account createBankAccount(String name, Money openingBalance) {

        return create(name, AccountType.BANK, openingBalance, null);
    }

    public static Account createMobileBankingAccount(String name, Money openingBalance) {

        return create(name, AccountType.MOBILE_BANKING, openingBalance, null);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Account name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }
    }

    private static void validateOpeningBalance(Money openingBalance) {

        Objects.requireNonNull(openingBalance, "Opening balance cannot be null.");

        if (openingBalance.isNegative()) {
            throw new IllegalArgumentException("Opening balance cannot be negative."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isCashAccount() {
        return accountType == AccountType.CASH;
    }

    public boolean isBankAccount() {
        return accountType == AccountType.BANK;
    }

    public boolean isMobileBankingAccount() {
        return accountType == AccountType.MOBILE_BANKING;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInactive() {
        return !active;
    }

    public Account rename(String newName) {

        validateName(newName);

        return new Account(id, newName.trim(), accountType, openingBalance, active, description, createdAt, LocalDateTime.now());
    }

    public Account changeDescription(String newDescription) {

        return new Account(id, name, accountType, openingBalance, active, newDescription, createdAt, LocalDateTime.now());
    }

    public Account activate() {

        if (active) {
            return this;
        }

        return new Account(id, name, accountType, openingBalance, true, description, createdAt, LocalDateTime.now());
    }

    public Account deactivate() {

        if (!active) {
            return this;
        }

        return new Account(id, name, accountType, openingBalance, false, description, createdAt, LocalDateTime.now());
    }
}