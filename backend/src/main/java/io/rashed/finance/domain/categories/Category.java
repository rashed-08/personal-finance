package io.rashed.finance.domain.categories;

import io.rashed.finance.common.enums.CategoryType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Category {

    private final CategoryId id;

    private final String name;

    private final CategoryType categoryType;

    /**
     * Default categories shipped with the application.
     * System categories cannot be deleted.
     */
    private final boolean systemDefined;

    private final boolean active;

    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Category(CategoryId id, String name, CategoryType categoryType, boolean systemDefined, boolean active, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.categoryType = Objects.requireNonNull(categoryType);

        this.systemDefined = systemDefined;
        this.active = active;
        this.description = description;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Category create(String name, CategoryType categoryType, boolean systemDefined, String description) {

        validateName(name);
        validateCategoryType(categoryType);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        return new Category(CategoryId.newId(), name, categoryType, systemDefined, true, description, now, now);
    }

    public static Category systemExpenseCategory(String name, String description) {
        return create(name, CategoryType.EXPENSE, true, description);
    }

    public static Category systemIncomeCategory(String name, String description) {
        return create(name, CategoryType.INCOME, true, description);
    }

    public static Category userExpenseCategory(String name, String description) {
        return create(name, CategoryType.EXPENSE, false, description);
    }

    public static Category userIncomeCategory(String name, String description) {
        return create(name, CategoryType.INCOME, false, description);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Category name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters.");
        }
    }

    private static void validateCategoryType(CategoryType categoryType) {

        Objects.requireNonNull(categoryType, "Category type cannot be null.");
    }

    private static void validateDescription(String description) {

        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters.");
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isIncome() {
        return categoryType == CategoryType.INCOME;
    }

    public boolean isExpense() {
        return categoryType == CategoryType.EXPENSE;
    }

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public boolean isUserDefined() {
        return !systemDefined;
    }

    public boolean isActive() {
        return active;
    }

    public Category rename(String newName) {

        if (systemDefined) {
            throw new IllegalStateException("System-defined categories cannot be renamed.");
        }

        validateName(newName);

        return new Category(id, newName.trim(), categoryType, systemDefined, active, description, createdAt, LocalDateTime.now());
    }

    public Category changeDescription(String newDescription) {

        validateDescription(newDescription);

        return new Category(id, name, categoryType, systemDefined, active, newDescription, createdAt, LocalDateTime.now());
    }

    public Category activate() {

        if (active) {
            return this;
        }

        return new Category(id, name, categoryType, systemDefined, true, description, createdAt, LocalDateTime.now());
    }

    public Category deactivate() {

        if (systemDefined) {
            throw new IllegalStateException("System-defined categories cannot be deactivated.");
        }

        if (!active) {
            return this;
        }

        return new Category(id, name, categoryType, systemDefined, false, description, createdAt, LocalDateTime.now());
    }

    public Category update(
        String name,
        CategoryType categoryType,
        String description
    ) {

        if (systemDefined) {
            throw new IllegalStateException(
                    "System-defined categories cannot be modified."
            );
        }

        validateName(name);
        validateCategoryType(categoryType);
        validateDescription(description);

        return new Category(
                id,
                name.trim(),
                categoryType,
                systemDefined,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }
}