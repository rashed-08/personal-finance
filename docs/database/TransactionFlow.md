# Transaction Flow

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes how money moves through the Personal Finance Application.

It explains the lifecycle of financial transactions from creation to reporting without describing database implementation details.

The transaction flow acts as the bridge between business workflows and the database design.

---

# Guiding Principles

The application follows several core financial principles.

- Every financial event becomes a transaction.
- Transactions are immutable.
- Reports are derived from transactions.
- Balances are calculated.
- Money is never created or destroyed without an explicit transaction.

---

# Financial Event Lifecycle

```
Financial Event

↓

Validation

↓

Transaction Created

↓

Salary Cycle Assigned

↓

Reports Updated

↓

Balances Calculated
```

---

# Transaction Sources

Transactions may originate from different sources.

Examples

```
Manual Entry

Salary

Recurring Template

Cash Reconciliation

Google Keep Migration

Future

CSV Import

Bank Import

API
```

Regardless of the source, every financial event becomes a standard transaction.

---

# Income Flow

Example

Salary Received

```
Employer

↓

Income Transaction

↓

Account Balance

↓

Salary Cycle

↓

Reports
```

Examples

- Salary
- Bonus
- Refund
- Gift

---

# Expense Flow

Example

Market Expense

```
Cash

↓

Expense Transaction

↓

Category

↓

Reports
```

Examples

- Market
- Rent
- Internet
- Medicine
- Travel

---

# Transfer Flow

Transfers move money between accounts.

Example

```
Cash

↓

Savings Account
```

Creates

```
Transfer Out

+

Transfer In
```

Net worth remains unchanged.

---

# Fund Allocation Flow

Money reserved for future purposes.

Example

```
Salary

↓

Emergency Fund
```

Money is still owned by the user.

Only its purpose changes.

Future versions may use dedicated allocation records.

---

# Loan Flow

Money Lent

```
Cash

↓

Loan

↓

Receivable
```

Repayment

```
Loan

↓

Cash
```

Money Borrowed

```
Loan

↓

Cash
```

Repayment

```
Cash

↓

Loan
```

Outstanding balances are calculated.

---

# Cash Reconciliation Flow

Users are not expected to record every small cash expense.

Instead

```
ATM Withdrawal

↓

Cash Spending

↓

Cash Count

↓

Difference

↓

Adjustment Transaction
```

Historical transactions remain unchanged.

---

# Recurring Transaction Flow

```
Recurring Template

↓

Scheduler

↓

Transaction Created

↓

Reports
```

Editing the generated transaction does not modify the template.

---

# Google Keep Migration Flow

```
Google Keep Note

↓

Parser

↓

Category Detection

↓

Salary Cycle Assignment

↓

Transaction Created
```

Breakdown values remain inside transaction notes.

---

# Backup Flow

```
Database

↓

Backup

↓

Google Drive

↓

Backup History
```

Restore

```
Backup

↓

Restore

↓

Application
```

---

# Salary Cycle Flow

```
Salary Cycle Created

↓

Transactions Assigned

↓

Reports Generated

↓

Carry Forward Calculated

↓

Cycle Closed
```

Transactions always belong to one salary cycle.

---

# Carry Forward Flow

```
Opening Balance

+

Income

-

Expense

=

Carry Forward
```

Carry forward is derived.

It is never permanently stored.

---

# Reporting Flow

```
Transactions

↓

Aggregation

↓

Monthly Report

↓

Dashboard
```

Reports never modify transactions.

---

# Money Movement Overview

```
Income

↓

Account

↓

Expense
        ↓
    Category

↓

Reports

↓

Carry Forward
```

Transfers

```
Account

↓

Account
```

Loans

```
Account

↓

Loan

↓

Account
```

Funds

```
Account

↓

Fund

↓

Account
```

Cash

```
ATM

↓

Cash

↓

Reconciliation

↓

Adjustment
```

---

# Validation Rules

Before a transaction is accepted

The application verifies

- Transaction type
- Account exists
- Category exists
- Salary cycle exists
- Amount > 0
- Business rules satisfied

Invalid transactions are rejected.

---

# Error Handling

Examples

- Missing category
- Invalid amount
- Closed salary cycle
- Deleted account
- Invalid transfer

Errors never create partial transactions.

---

# Future Transaction Sources

Possible additions

- CSV Import
- Excel Import
- Bank Synchronization
- OCR
- Receipt Scanner
- REST API
- AI Assistant

The transaction engine remains unchanged.

Only new sources are added.

---

# Design Decisions

- Transactions are the single source of truth.
- Reports are read-only projections.
- Balances are derived.
- Historical records remain immutable.
- Business workflows produce transactions.
- Every module integrates through the transaction engine.

---

# Final Statement

The Transaction Flow defines how financial events move through the Personal Finance Application.

It establishes a consistent financial lifecycle across every module and ensures that reporting, reconciliation, salary cycles, loans, funds, recurring transactions, and future integrations all rely on a single transaction engine.