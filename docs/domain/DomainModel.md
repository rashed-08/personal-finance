# Domain Model

## Purpose

This document defines the business domain model of the Personal Finance App.

The purpose of the domain model is to identify the major business concepts, their responsibilities, and their relationships before designing the database or writing implementation code.

This document is technology independent.

It describes the business—not the database.

---

# Domain Philosophy

The application models real-world personal finance rather than accounting software.

The goal is to accurately represent how money moves through a person's financial life while minimizing unnecessary manual bookkeeping.

Every important financial concept should exist as a first-class domain object.

---

# Domain Overview

```

```
                 User
                  │
                  │
         ┌────────┴────────┐
         │                 │
     Accounts         Categories
         │                 │
         │                 │
         └──────┬──────────┘
                │
          Transactions
                │
    ┌───────────┼────────────┐
    │           │            │
SalaryCycle   Funds       Loans
    │           │            │
    └──────┬────┴────┬───────┘
           │         │
     Cash Reconciliation
           │
     Adjustment Transactions
           │
         Reports
```

---

# Core Domain Objects

The application is built around the following business entities.

- User
- Account
- Transaction
- Category
- Salary Cycle
- Fund
- Loan
- Cash Reconciliation
- Cash Snapshot
- Adjustment
- Report

These objects collectively describe the user's financial world.

---

# User

Represents the owner of the financial data.

Responsibilities:

- owns accounts
- owns categories
- owns salary cycles
- owns reports
- owns funds
- owns loans

Normally there is a single user.

Future versions may support multiple users.

---

# Account

Represents a place where money exists.

Examples

- Cash Wallet
- Bank Account
- Savings Account
- Mobile Banking
- Credit Card (future)

Responsibilities

- holds balance
- receives transactions
- sends transactions
- participates in transfers

An account never knows business rules.

It only represents where money is stored.

---

# Transaction

The most important entity in the system.

Everything begins with transactions.

Responsibilities

- records movement of money
- belongs to an account
- belongs to a category
- belongs to a salary cycle
- participates in reports

Reports never own data.

Reports calculate from transactions.

---

# Transaction Types

Examples include

Income

Expense

Transfer

Salary

Loan Given

Loan Received

Loan Repayment

Fund Deposit

Fund Withdrawal

Adjustment

Recurring Transaction

Future versions may introduce new transaction types without redesigning the domain.

---

# Category

Represents the purpose of a transaction.

Examples

Rent

Groceries

Medicine

Gym

Internet

Utilities

Transport

Donation

Categories organize reports.

Categories never contain money.

---

# Salary Cycle

Represents the user's financial period.

A salary cycle begins when salary is received.

It ends the day before the next salary.

Responsibilities

- groups transactions
- calculates available balance
- supports carry forward
- supports reporting

Salary Cycle is independent from calendar months.

---

# Fund

Represents reserved money.

Examples

Emergency Fund

Zakat Fund

Purchase Fund

Travel Fund

Investment Fund

Responsibilities

- stores reserved money
- receives transfers
- returns money when needed

Funds are not expenses.

Funds are intentional allocations.

---

# Loan

Represents money temporarily exchanged between people.

Examples

Money Borrowed

Money Lent

Responsibilities

- records principal
- records repayments
- tracks outstanding balance

Loans should not distort expense reports.

---

# Cash Reconciliation

Represents a reconciliation session.

Responsibilities

- compares expected cash
- compares actual cash
- calculates difference
- creates adjustment transactions

This is one of the application's core business features.

---

# Cash Snapshot

Represents the physical cash counted by the user.

Responsibilities

- stores counted cash
- represents reality
- starts reconciliation

Snapshots are historical records.

They should never be overwritten.

---

# Adjustment

Represents a correction created during reconciliation.

Responsibilities

- explains balance difference
- preserves history
- keeps reports reproducible

Adjustments are ordinary transactions with a special purpose.

---

# Report

Represents calculated financial information.

Reports never own financial data.

Examples

Monthly Report

Salary Report

Category Report

Fund Report

Loan Report

Cash Flow Report

All reports are derived from transactions.

---

# Aggregate Overview

The following aggregates define consistency boundaries.

## Account Aggregate

Aggregate Root

Account

Contains

Transactions

Transfers

Balance Calculation

---

## Salary Aggregate

Aggregate Root

Salary Cycle

Contains

Cycle Summary

Carry Forward

Cycle Reports

---

## Fund Aggregate

Aggregate Root

Fund

Contains

Fund Transactions

Current Balance

History

---

## Loan Aggregate

Aggregate Root

Loan

Contains

Loan Transactions

Outstanding Balance

Repayment History

---

## Cash Aggregate

Aggregate Root

Cash Reconciliation

Contains

Cash Snapshot

Adjustment

Difference

Reconciliation Window

---

# Relationships

User

↓

owns

↓

Accounts

↓

contain

↓

Transactions

↓

reference

↓

Category

Salary Cycle

Funds

Loans

Reports

Cash Reconciliation references transactions.

Reports calculate from transactions.

Funds use transactions.

Loans use transactions.

Salary cycles organize transactions.

---

# Value Objects

The following concepts are immutable value objects.

Money

Currency

Date Range

Reconciliation Window

Reporting Period

Transaction Reference

Future implementations may introduce additional value objects.

---

# Domain Rules

The following rules govern the domain.

- Every transaction belongs to exactly one account.
- Every expense belongs to one category.
- Every salary creates a salary cycle.
- Every transfer produces balanced money movement.
- Every reconciliation creates a permanent session.
- Every report is generated from transactions.
- Historical transactions should never be silently modified.
- Carry Forward is calculated rather than stored manually.
- Funds are transfers, not expenses.
- Loans are financial obligations, not expenses.
- Adjustments preserve financial history.

---

# Domain Invariants

The following conditions must always remain true.

Money cannot disappear.

Money cannot appear without explanation.

Balances must always be reproducible.

Reports must always be deterministic.

Transactions are immutable history.

Historical reconciliation sessions are preserved.

Every balance can be explained through transactions.

---

# Future Domain Objects

The architecture should support adding:

- Budget
- Asset
- Liability
- Investment Portfolio
- Subscription
- Goal Tracking
- Insurance
- Tax
- Recurring Bills
- Notifications
- Exchange Rates
- Multi-Currency Accounts

These additions should require minimal changes to existing business rules.

---

# Ubiquitous Language

The following terminology should remain consistent across:

- documentation
- code
- API
- database
- UI

Preferred terms:

- Account
- Transaction
- Salary Cycle
- Fund
- Loan
- Cash Reconciliation
- Cash Snapshot
- Carry Forward
- Adjustment
- Category
- Report

Avoid introducing synonyms for these concepts.

Consistency improves readability and long-term maintainability.

---

# Final Statement

The Domain Model defines the language of the Personal Finance App.

Database tables, REST APIs, Java entities, React components, reports, and business services should all reflect the concepts described in this document.

Technology may evolve over time.

The domain should remain stable.