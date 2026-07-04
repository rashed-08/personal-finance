# Part 4 — Relationships, Foreign Keys, Constraints & Indexes

---

## 4.1 Overview

The `transactions` table acts as the central ledger of the application.

Every business module references or derives information from this table.

Relationships are intentionally kept simple to ensure a stable and extensible database design.

---

## 4.2 Entity Relationships

```
                    +----------------+
                    |   Accounts     |
                    +-------+--------+
                            |
                     from_account_id
                            |
                            |
+-----------+       +--------v---------+       +----------------+
| Categories|------>|   Transactions   |<------| Salary Cycles  |
+-----------+       +--------+---------+       +----------------+
                            |
                            |
                migration_batch_id
                            |
                     +------v------+
                     | Migration   |
                     +-------------+

                            |
              reconciliation_batch_id
                            |
                     +------v------+
                     | Cash Recon  |
                     +-------------+

Future

Transactions
     |
     +---- transaction_loans ------> Loans

Transactions
     |
     +---- transaction_funds ------> Funds

Transactions
     |
     +---- transaction_tags -------> Tags
```

---

## 4.3 Foreign Keys

### from_account_id

References

```
accounts(id)
```

Purpose

Represents the source of money.

Nullable.

---

### to_account_id

References

```
accounts(id)
```

Purpose

Represents the destination of money.

Nullable.

---

### category_id

References

```
categories(id)
```

Purpose

Classifies income or expense transactions.

Nullable.

---

### salary_cycle_id

References

```
salary_cycles(id)
```

Purpose

Groups transactions into reporting periods.

Nullable.

---

### migration_batch_id

References

```
migration_batches(id)
```

Purpose

Identifies imported historical data.

Nullable.

---

### reconciliation_batch_id

References

```
cash_reconciliation_batches(id)
```

Purpose

Links adjustment transactions to reconciliation sessions.

Nullable.

---

## 4.4 Future Relationships

The following relationships are intentionally excluded from Version 1.

### Loans

```
transactions

↓

transaction_loans

↓

loans
```

---

### Funds

```
transactions

↓

transaction_funds

↓

funds
```

---

### Tags

```
transactions

↓

transaction_tags

↓

tags
```

---

This approach prevents unnecessary nullable columns while keeping the ledger generic.

---

## 4.5 Database Constraints

### Primary Key

```
PRIMARY KEY (id)
```

---

### Amount

```
CHECK(amount > 0)
```

Zero and negative amounts are not allowed.

---

### Transfer Validation

```
CHECK(from_account_id <> to_account_id)
```

Applies only when both accounts exist.

---

### Transaction Type

Must be one of

```
INCOME
EXPENSE
TRANSFER
ADJUSTMENT
OPENING_BALANCE
MIGRATION
```

---

### Transaction Status

Must be one of

```
POSTED

VOID

REVERSED
```

---

### Required Accounts

Income

```
to_account_id
```

must exist.

Expense

```
from_account_id
```

must exist.

Transfer

Both accounts must exist.

---

### Adjustment

```
adjustment_reason
```

Required only for adjustment transactions.

---

### Opening Balance

Only one opening balance transaction may exist per account.

---

### Immutable History

Transactions cannot be physically deleted.

Only status changes or adjustment transactions may alter financial history.

---

## 4.6 Recommended Database Indexes

Primary Index

```
id
```

---

Reporting

```
transaction_date
```

---

Salary Reports

```
salary_cycle_id
```

---

Category Reports

```
category_id
```

---

Account History

```
from_account_id
```

```
to_account_id
```

---

Migration

```
migration_batch_id
```

---

Cash Reconciliation

```
reconciliation_batch_id
```

---

Transaction Type

```
transaction_type
```

---

Transaction Status

```
transaction_status
```

---

## 4.7 Composite Indexes

Recommended.

### Salary Reporting

```
(transaction_date, salary_cycle_id)
```

---

### Category Reports

```
(category_id, transaction_date)
```

---

### Account History

```
(from_account_id, transaction_date)
```

```
(to_account_id, transaction_date)
```

---

### Dashboard

```
(transaction_type, transaction_date)
```

---

### Migration

```
(migration_batch_id, transaction_date)
```

---

## 4.8 Cascade Strategy

Transactions represent historical financial records.

Therefore

```
ON DELETE CASCADE
```

must never be used.

Recommended

```
ON DELETE RESTRICT
```

or

```
ON DELETE NO ACTION
```

This prevents accidental financial data loss.

---

## 4.9 Referential Integrity Rules

The database must guarantee

- Every referenced account exists.
- Every referenced category exists.
- Every referenced salary cycle exists.
- Every migration batch exists.
- Every reconciliation batch exists.

Historical references must never become orphaned.

---

## 4.10 Design Principles

Relationship design follows these principles.

- Generic ledger model
- Immutable history
- No duplicated balances
- Future-proof extensibility
- Minimal nullable columns
- Stable foreign keys
- Business modules remain independent

---

## 4.11 Final Statement

The `transactions` table intentionally owns very few direct relationships.

Business complexity is implemented through higher-level modules rather than by expanding the ledger schema.

This approach keeps the financial ledger stable, scalable and maintainable for future versions of the application.