# Transactions Table Specification

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

The `transactions` table is the core of the Personal Finance Application.

Every financial event is recorded as a transaction.

No module is allowed to modify balances directly.

All balances, reports, summaries, dashboards, salary cycles, fund balances, loan balances, and cash reconciliation are derived from transaction records.

This table is the single source of truth for the financial system.

---

# Design Philosophy

The application follows a Ledger-first architecture.

Money never appears or disappears automatically.

Every financial movement must be represented by a transaction.

Historical transactions remain immutable.

Corrections are made through new adjustment transactions.

Balances are calculated.

They are never permanently stored.

---

# Aggregate

Transaction Aggregate

Aggregate Root

Transaction

Supporting Aggregates

Account

Category

Salary Cycle

Loan

Fund

Cash Reconciliation

Recurring Transaction

---

# Responsibilities

The transaction aggregate is responsible for

- Recording every financial event
- Recording money movement
- Recording account transfers
- Recording loan activity
- Recording fund allocation
- Recording adjustments
- Recording imported historical data
- Supporting reporting
- Supporting auditing

The transaction aggregate is NOT responsible for

- Calculating reports
- Calculating balances
- Managing UI state
- Generating charts

---

# Table Definition

Table Name

transactions

Primary Key

id

---

# Core Concept

Every transaction represents exactly one financial event.

Examples

Salary

↓

Income Transaction

Market

↓

Expense Transaction

ATM Withdrawal

↓

Transfer Transaction

Loan Payment

↓

Loan Repayment Transaction

Cash Difference

↓

Adjustment Transaction

---

# Table Columns

| Column | Type | Nullable | Description |
|---------|------|----------|-------------|
| id | UUID | No | Primary Key |
| transaction_type | VARCHAR(40) | No | Financial event type |
| transaction_status | VARCHAR(20) | No | Transaction lifecycle |
| transaction_date | DATE | No | Financial event date |
| amount | NUMERIC(18,2) | No | Transaction amount |
| description | VARCHAR(255) | Yes | Short description |
| notes | TEXT | Yes | Optional notes |
| from_account_id | UUID | Yes | Money source |
| to_account_id | UUID | Yes | Money destination |
| category_id | UUID | Yes | Expense/Income category |
| salary_cycle_id | UUID | No | Salary cycle |
| reference_number | VARCHAR(100) | Yes | Optional external reference |
| migration_batch_id | UUID | Yes | Import batch |
| reconciliation_batch_id | UUID | Yes | Cash reconciliation batch |
| created_at | TIMESTAMP | No | Creation timestamp |
| updated_at | TIMESTAMP | No | Last modification timestamp |

---

# Why Two Accounts?

Instead of storing

```
Account
```

the system stores

```
From Account

↓

To Account
```

Examples

Salary

```
External

↓

Bank Account
```

Market

```
Cash

↓

External
```

ATM Withdrawal

```
Bank

↓

Cash
```

Fund Allocation

```
Cash

↓

Emergency Fund Account (logical allocation)
```

This creates a consistent money movement model.

---

# Money Direction

Every transaction follows

```
Source

↓

Destination

↓

Amount
```

No transaction exists without money movement.

---

# Required Columns

The following columns are always required

- id
- transaction_type
- transaction_status
- transaction_date
- amount
- salary_cycle_id
- created_at
- updated_at

---

# Nullable Columns

Nullable columns exist because different transaction types require different information.

Example

Expense

Needs

```
Category
```

Transfer

Needs

```
From Account

To Account
```

Adjustment

Needs

```
Neither Category nor Destination
```

Therefore nullable fields reduce unnecessary duplication.

---

# Transaction Type Enumeration

Version 1 supports

| Type | Purpose |
|------|----------|
| INCOME | Money received |
| EXPENSE | Money spent |
| TRANSFER | Money moved between accounts |
| ADJUSTMENT | Financial correction |
| OPENING_BALANCE | Initial imported balance |
| MIGRATION | Imported historical transaction |

Future versions may add

- INVESTMENT
- DIVIDEND
- TAX
- INTEREST
- EXCHANGE

---

# Transaction Status Enumeration

| Status | Description |
|---------|-------------|
| POSTED | Active transaction |
| VOID | Cancelled before posting |
| REVERSED | Reversed by another transaction |

Version 1

Every transaction is created as

```
POSTED
```

---

# Adjustment Reason Enumeration

Applicable only when

```
transaction_type

=

ADJUSTMENT
```

Supported reasons

| Reason | Description |
|----------|-------------|
| CASH_RECONCILIATION | Cash count difference |
| OPENING_BALANCE | Initial balance correction |
| DATA_MIGRATION | Legacy import correction |
| MANUAL_CORRECTION | User correction |
| SYSTEM_CORRECTION | Automatic correction |

Future

- BANK_RECONCILIATION
- ROUNDING
- IMPORT_ERROR

---

# Data Integrity Principles

The transaction table follows the following principles

- Immutable history
- No duplicated balances
- Every amount is positive
- Transaction type determines behavior
- Reports are derived
- Adjustments are explicit
- Financial history is auditable

---

# High-Level Relationships

```
Transactions

├── Account

├── Category

├── Salary Cycle

├── Migration Batch

└── Cash Reconciliation
```

Additional domain relationships such as Loans, Funds and Recurring Transactions are described in the following sections of this document.

The transactions table remains independent from those modules while serving as the central financial ledger.

---

# Final Statement

The `transactions` table is the financial ledger of the application.

Every module ultimately records information here.

Because all balances, reports, salary cycles, funds, loans, and cash reconciliation are derived from this table, it represents the single source of truth for the entire Personal Finance Application.