# Part 2 — Transaction Types

---

# Overview

Every transaction belongs to exactly one transaction type.

The transaction type defines how the financial event behaves inside the ledger.

Additional business meaning (Loan, Fund, Recurring, Migration, etc.) is provided by related domain entities rather than by introducing new transaction types.

This keeps the ledger simple, extensible and consistent.

---

# Supported Transaction Types

Version 1 supports the following transaction types.

| Type | Description |
|------|-------------|
| INCOME | Money enters the financial system |
| EXPENSE | Money leaves the financial system |
| TRANSFER | Money moves between accounts |
| ADJUSTMENT | Financial correction |
| OPENING_BALANCE | Initial balance during setup |
| MIGRATION | Historical imported transaction |

Future versions may introduce additional transaction types if required.

---

# Income

## Purpose

Represents money received by the user.

Examples

- Salary
- Bonus
- Refund
- Gift
- Cashback
- Profit

---

## Money Flow

```
External

↓

User Account
```

---

## Required Fields

```
transaction_type

INCOME

amount

to_account_id

salary_cycle_id
```

Optional

```
category_id

description

notes
```

---

## Reporting

Included in

- Income Report
- Dashboard
- Salary Cycle
- Carry Forward

---

# Expense

## Purpose

Represents money spent by the user.

Examples

- Market
- Rent
- Medicine
- Internet
- Fuel
- Shopping

---

## Money Flow

```
User Account

↓

External
```

---

## Required Fields

```
transaction_type

EXPENSE

amount

from_account_id

category_id

salary_cycle_id
```

---

## Reporting

Included in

- Expense Report
- Category Report
- Dashboard
- Carry Forward

---

# Transfer

## Purpose

Represents movement of money between two user-owned accounts.

Transfers do not change net worth.

Examples

- Bank → Cash
- Cash → Savings
- BKash → Bank
- Bank → Credit Card Payment Account

---

## Money Flow

```
Account A

↓

Account B
```

---

## Required Fields

```
transaction_type

TRANSFER

from_account_id

to_account_id

amount
```

---

## Reporting

Excluded from

- Income
- Expense

Included in

- Account History
- Cash Flow
- Audit

---

# Adjustment

## Purpose

Represents manual or automatic financial corrections.

Examples

- Cash reconciliation
- Migration correction
- Opening correction
- Manual correction

---

## Money Flow

Depends on adjustment reason.

Examples

```
Cash

↓

Adjustment
```

or

```
Adjustment

↓

Cash
```

---

## Required Fields

```
transaction_type

ADJUSTMENT

amount

adjustment_reason
```

---

## Reporting

Visible separately.

Should not be mixed with normal expenses.

---

# Opening Balance

## Purpose

Represents the initial financial state when the application starts.

Created only during onboarding or major migration.

---

## Money Flow

```
System

↓

User Account
```

---

## Rules

Created once.

Never edited.

Future corrections use adjustment transactions.

---

# Migration

## Purpose

Represents historical data imported from legacy systems.

Examples

- Google Keep
- CSV
- Excel

---

## Rules

Imported transactions behave exactly like manually created transactions.

The only difference is

```
migration_batch_id
```

---

# Loan Transactions

Loans are not transaction types.

A loan is a business relationship.

Loan transactions use existing transaction types.

Examples

Loan Given

```
Expense

+

Loan Relationship
```

Loan Received

```
Income

+

Loan Relationship
```

Loan Repayment Received

```
Income

+

Loan Relationship
```

Loan Repayment Paid

```
Expense

+

Loan Relationship
```

The loan module calculates outstanding balance from related transactions.

---

# Fund Transactions

Funds are not transaction types.

Funds represent financial goals.

Money allocated to a fund is represented using a transfer between the user's available balance and the logical fund allocation.

Examples

Emergency Fund

Vacation Fund

Zakat Fund

Future versions may introduce dedicated allocation tables.

---

# Recurring Transactions

Recurring transactions are not transaction types.

They are transaction generators.

Workflow

```
Recurring Template

↓

Scheduler

↓

Normal Transaction

↓

Ledger
```

Generated transactions become normal ledger entries.

Editing them never changes the recurring template.

---

# Business Context

Transaction Type defines

```
How money moves
```

Business Modules define

```
Why money moves
```

This separation keeps the ledger independent from business features.

---

# Design Principles

- Every financial event becomes one ledger transaction.
- Transaction type defines financial behavior.
- Business modules enrich transactions through relationships.
- The ledger remains generic.
- Reports rely on transaction type, not module names.

---

# Final Statement

The transaction type model intentionally remains small.

Income, Expense, Transfer, Adjustment, Opening Balance and Migration are sufficient to describe every financial event in Version 1.

Higher-level concepts such as loans, funds and recurring schedules are modeled as business domains built on top of the ledger rather than as additional transaction types.