# Constraints Specification

Version: 1.0

Status: Draft

Owner: Personal Finance App

Related Documents

- 01-overview.md
- 02-transaction-types.md
- 03-business-rules.md
- 04-relationships.md
- 06-indexes.md

---

# Purpose

This document defines all database constraints applied to the `transactions` table.

Constraints protect data integrity and prevent invalid financial records from being stored.

Business rules that depend on application logic should be enforced in the domain layer, while structural rules should be enforced by the database whenever possible.

---

# Constraint Philosophy

The transaction ledger follows these principles.

- Preserve financial integrity
- Prevent invalid records
- Keep historical data immutable
- Separate structural validation from business validation
- Avoid overly complex database logic

The database guarantees structural consistency.

The application guarantees business correctness.

---

# Constraint Categories

The transaction table uses the following categories of constraints.

- Primary Key
- Foreign Keys
- Check Constraints
- Unique Constraints
- Referential Integrity
- Delete Strategy
- Update Strategy

---

# Primary Key Constraint

```
PRIMARY KEY (id)
```

Rules

- Every transaction must have a unique identifier.
- The identifier is immutable.
- IDs are generated using UUID.

---

# Foreign Key Constraints

## Source Account

```
from_account_id

REFERENCES accounts(id)
```

Purpose

Represents the account from which money originates.

Nullable.

---

## Destination Account

```
to_account_id

REFERENCES accounts(id)
```

Purpose

Represents the account receiving money.

Nullable.

---

## Category

```
category_id

REFERENCES categories(id)
```

Purpose

Classifies income and expense transactions.

Nullable.

---

## Salary Cycle

```
salary_cycle_id

REFERENCES salary_cycles(id)
```

Purpose

Associates a transaction with a reporting period.

Nullable.

---

## Migration Batch

```
migration_batch_id

REFERENCES migration_batches(id)
```

Nullable.

---

## Cash Reconciliation Batch

```
reconciliation_batch_id

REFERENCES cash_reconciliation_batches(id)
```

Nullable.

---

# Check Constraints

## Positive Amount

```
CHECK (amount > 0)
```

Every transaction amount must be positive.

Zero and negative values are prohibited.

---

## Transaction Type

Allowed values

```
INCOME

EXPENSE

TRANSFER

ADJUSTMENT

OPENING_BALANCE

MIGRATION
```

Future transaction types require a schema migration.

---

## Transaction Status

Allowed values

```
POSTED

VOID

REVERSED
```

Version 1 creates transactions directly as

```
POSTED
```

---

## Transaction Date

```
transaction_date IS NOT NULL
```

Every financial event must have an effective date.

---

## Created Timestamp

```
created_at IS NOT NULL
```

---

## Updated Timestamp

```
updated_at IS NOT NULL
```

---

# Unique Constraints

Version 1 requires very few unique constraints.

Primary Key uniqueness is sufficient for most transactions.

---

## Opening Balance

Business Rule

Each account may contain only one active opening balance transaction.

Implementation may use

- Partial Unique Index
- Domain Validation

rather than a standard UNIQUE constraint.

---

# Referential Integrity

The database must guarantee

- Every referenced account exists.
- Every referenced category exists.
- Every referenced salary cycle exists.
- Every migration batch exists.
- Every reconciliation batch exists.

Transactions must never contain orphaned references.

---

# Delete Strategy

Historical financial data must never disappear automatically.

Recommended

```
ON DELETE RESTRICT
```

or

```
ON DELETE NO ACTION
```

must be used for all foreign keys.

```
ON DELETE CASCADE
```

must never be used.

---

# Update Strategy

Primary Keys

Never updated.

Foreign Keys

Updates should be extremely rare.

Historical relationships should remain stable after posting.

---

# Database Validation

The database should validate

- Positive amount
- Existing foreign keys
- Allowed enum values
- Required timestamps

The database should NOT validate

- Salary cycle resolution
- Loan balance
- Fund balance
- Carry forward
- Cash reconciliation
- Reporting logic

These belong to the domain layer.

---

# Domain Validation

The application is responsible for validating

Income

- Destination account required.

Expense

- Source account required.
- Category required.

Transfer

- Source account required.
- Destination account required.
- Source and destination accounts must differ.

Adjustment

- Adjustment reason required.

Opening Balance

- Only one opening balance per account.

Migration

- Valid migration batch required.

---

# Constraint Responsibility Matrix

| Validation | Database | Domain |
|------------|----------|--------|
| Primary Key | ✅ | ❌ |
| Foreign Keys | ✅ | ❌ |
| Positive Amount | ✅ | ✅ |
| Enum Values | ✅ | ✅ |
| Required Accounts | ❌ | ✅ |
| Salary Cycle Resolution | ❌ | ✅ |
| Loan Rules | ❌ | ✅ |
| Fund Rules | ❌ | ✅ |
| Cash Reconciliation | ❌ | ✅ |
| Carry Forward | ❌ | ✅ |

---

# Future Constraints

Future versions may introduce additional constraints for

- Split Transactions
- Multi-currency
- Investments
- Attachments
- Tags
- Merchant Directory
- Budget Planning

These constraints should be implemented without changing the existing ledger principles.

---

# Design Principles

The constraint model follows these principles.

- Structural validation belongs to the database.
- Business validation belongs to the domain.
- Historical data is immutable.
- Financial records are auditable.
- The ledger remains generic.
- Future modules extend the ledger rather than modifying it.

---

# Final Statement

The constraint model protects the integrity of the financial ledger while keeping business rules within the domain layer.

This separation ensures that the database remains reliable, the application remains flexible, and future extensions can be introduced without compromising historical financial data.