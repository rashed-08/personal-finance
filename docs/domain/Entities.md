# Domain Entities

## Purpose

This document defines the core business entities of the Personal Finance App.

Entities represent business concepts that have identity, lifecycle, and behavior.

This document is independent of database implementation.

Database tables may evolve over time, but the business entities defined here should remain stable.

---

# What is an Entity?

An Entity is an object that:

- has a unique identity
- changes over time
- owns business state
- participates in business workflows

Examples

A Transaction

A Loan

A Salary Cycle

A Fund

These objects remain the same entity even when their attributes change.

---

# Entity Relationships

```

SalaryCycle

│

├──────────────┐

│              │

Transactions   CarryForward

│

├──────┬─────────────┬─────────────┐

│      │             │             │

Account Category Fund Loan

│

CashSnapshot

↓

CashReconciliation

```

---

# Account

## Description

Represents a financial account where money exists.

Examples

- Cash
- Bank
- Mobile Banking
- Savings Account

---

## Identity

Account ID

---

## Attributes

- Name
- Type
- Currency
- Status

---

## Responsibilities

- Hold balance
- Participate in transfers
- Participate in transactions

---

# Category

## Description

Represents a logical classification for income or expenses.

Examples

- Grocery
- Rent
- Internet
- Gym
- Medicine
- Salary

---

## Identity

Category ID

---

## Attributes

- Name
- Type
- Parent Category
- Color
- Icon

---

## Responsibilities

- Classify transactions
- Enable reporting

---

# Transaction

## Description

Represents a financial movement recorded in the ledger.

Every financial operation eventually becomes one or more transactions.

---

## Identity

Transaction ID

---

## Attributes

- Date
- Amount
- Description
- Transaction Type
- Status
- Salary Cycle
- Account
- Category

---

## Responsibilities

- Record financial history
- Preserve accounting integrity
- Support reporting

---

# SalaryCycle

## Description

Represents a salary period.

Salary cycles are independent from calendar months.

Example

Salary Received

10 May

↓

Cycle

10 May → 9 June

---

## Identity

Salary Cycle ID

---

## Attributes

- Start Date
- End Date
- Salary Amount
- Status

---

## Responsibilities

- Group transactions
- Calculate carry forward
- Produce salary reports

---

# CarryForward

## Description

Represents money remaining after a salary cycle ends.

Carry Forward is not manually entered.

It is calculated.

---

## Identity

Carry Forward ID

---

## Attributes

- Opening Balance
- Closing Balance
- Available Balance
- Reserved Balance

---

## Responsibilities

- Connect salary cycles
- Preserve financial continuity

---

# Fund

## Description

Represents reserved money.

A Fund never owns money.

It reserves money.

Examples

- Emergency Fund
- Zakat Fund
- Laptop Fund
- Vacation Fund

---

## Identity

Fund ID

---

## Attributes

- Name
- Type
- Reserved Amount
- Target Amount (Future)
- Status

---

## Responsibilities

- Reserve money
- Track allocation history
- Support financial goals

---

# Loan

## Description

Represents money temporarily owed.

Supports

- Receivable
- Payable

---

## Identity

Loan ID

---

## Attributes

- Loan Type
- Outstanding Balance
- Counterparty
- Status
- Notes

---

## Responsibilities

- Track outstanding balance
- Record repayments
- Record collections

---

# CashSnapshot

## Description

Represents the actual physical cash counted by the user at a point in time.

Used during reconciliation.

---

## Identity

Snapshot ID

---

## Attributes

- Snapshot Date
- Physical Cash
- Notes

---

## Responsibilities

- Support reconciliation
- Detect missing cash

---

# CashReconciliation

## Description

Represents the reconciliation process between expected cash and actual cash.

---

## Identity

Reconciliation ID

---

## Attributes

- Period Start
- Period End
- Expected Cash
- Actual Cash
- Difference

---

## Responsibilities

- Detect untracked expenses
- Create adjustment transactions
- Preserve accounting accuracy

---

# RecurringTransaction

## Description

Represents a transaction template that repeats automatically.

Examples

- Rent
- Internet
- Gym
- Charity
- Donation

---

## Identity

Recurring Transaction ID

---

## Attributes

- Frequency
- Start Date
- End Date
- Next Execution
- Status

---

## Responsibilities

- Generate transactions
- Reduce manual work

---

# BackupHistory

## Description

Represents backup operations.

Supports

- Google Drive Backup
- Restore
- Export

---

## Identity

Backup ID

---

## Attributes

- Backup Time
- Backup Type
- File Size
- Status

---

## Responsibilities

- Preserve user data
- Support recovery

---

# Settings

## Description

Represents user preferences.

Examples

- Default Currency
- Theme
- Backup Preferences
- Salary Cycle Rules

---

## Identity

Settings ID

---

## Responsibilities

- Configure application behavior

---

# Entity Ownership

| Entity | Owner Module |
|----------|--------------|
| Account | Accounts |
| Category | Categories |
| Transaction | Transactions |
| SalaryCycle | Salary |
| CarryForward | Salary |
| Fund | Funds |
| Loan | Loans |
| CashSnapshot | Cash |
| CashReconciliation | Cash |
| RecurringTransaction | Transactions |
| BackupHistory | Backup |
| Settings | Settings |

---

# Entity Lifecycle

```
Create

↓

Update

↓

Business Validation

↓

Persist

↓

Historical Reporting

↓

Archive (Future)
```

Entities are never physically deleted.

Historical financial records must remain immutable.

---

# Business Rules

- Every entity has a unique identity.
- Financial entities are immutable whenever possible.
- Historical records must remain available.
- Business rules belong to entities and domain services, not controllers.
- Entity ownership must never overlap between modules.
- Entities should expose business behavior rather than acting as data containers.

---

# Future Entities

The architecture should allow introducing additional entities without breaking existing modules.

Examples

- Budget
- Investment
- Portfolio
- Exchange Rate
- Tax Profile
- Notification
- AI Recommendation

---

# Final Statement

Entities model the core business concepts of the Personal Finance App.

They define the language of the domain, own business identity, and provide the foundation for aggregates, repositories, database schema, and application services.

The stability of these entities is critical to maintaining a consistent and extensible financial system.