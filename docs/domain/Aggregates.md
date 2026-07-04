# Domain Aggregates

## Purpose

This document defines the Aggregate boundaries of the Personal Finance App.

Aggregates establish transactional consistency boundaries within the domain.

Every Aggregate has exactly one Aggregate Root.

External modules communicate only through Aggregate Roots.

This document follows Domain-Driven Design (DDD) principles.

---

# What is an Aggregate?

An Aggregate is:

- a cluster of related domain objects
- a consistency boundary
- a transaction boundary
- owned by one Aggregate Root

Rules inside an Aggregate must always remain consistent.

---

# Aggregate Design Principles

Every Aggregate should:

- have one Aggregate Root
- protect business invariants
- own its child objects
- expose behavior instead of internal state
- remain small
- avoid references to other aggregates except by ID

---

# Aggregate Overview

```

Account Aggregate

Category Aggregate

Transaction Aggregate

Salary Aggregate

Fund Aggregate

Loan Aggregate

Cash Aggregate

Recurring Transaction Aggregate

Settings Aggregate

Backup Aggregate

```

---

# Transaction Aggregate

## Aggregate Root

Transaction

---

## Contains

- Ledger Entries (future)
- Transaction Metadata

---

## Responsibilities

- Record business events
- Preserve financial history
- Validate accounting rules
- Prevent invalid transactions

---

## Owns

- Transaction Status
- Transaction Amount
- Transaction Description

---

## References

- Account ID
- Category ID
- Salary Cycle ID

References only.

Never object ownership.

---

# Account Aggregate

## Aggregate Root

Account

---

## Responsibilities

- Manage account lifecycle
- Hold account metadata
- Validate account status

---

## Does NOT Own

Transactions

Transactions reference Account.

Accounts never contain transaction history.

---

# Category Aggregate

## Aggregate Root

Category

---

## Responsibilities

- Manage category hierarchy
- Expense classification
- Income classification

---

Transactions reference categories.

---

# Salary Aggregate

## Aggregate Root

SalaryCycle

---

## Owns

CarryForward

---

## Responsibilities

- Open salary cycle
- Close salary cycle
- Calculate carry forward
- Produce salary reports

---

Transactions reference Salary Cycle.

Salary Cycle never owns transactions.

---

# Fund Aggregate

## Aggregate Root

Fund

---

## Owns

Allocation History

Future

Target Rules

Contribution Rules

---

## Responsibilities

- Reserve money
- Release money
- Track reserved balance

---

Fund does not own transactions.

Transactions reference Fund.

---

# Loan Aggregate

## Aggregate Root

Loan

---

## Owns

Repayment History

Collection History

Outstanding Balance

---

## Responsibilities

- Record repayments
- Record collections
- Calculate outstanding amount
- Close loan

---

Transactions reference Loan.

Loan owns repayment history.

---

# Cash Aggregate

## Aggregate Root

CashReconciliation

---

## Owns

CashSnapshot

Adjustment History

---

## Responsibilities

- Detect missing cash
- Generate adjustments
- Preserve reconciliation history

---

# Recurring Transaction Aggregate

## Aggregate Root

RecurringTransaction

---

## Responsibilities

- Schedule execution
- Generate transactions
- Track next execution

Generated transactions become part of Transaction Aggregate.

---

# Settings Aggregate

## Aggregate Root

Settings

---

## Responsibilities

- Application configuration
- User preferences

---

# Backup Aggregate

## Aggregate Root

BackupHistory

---

## Responsibilities

- Backup metadata
- Restore metadata
- Synchronization history

---

# Aggregate Relationships

```

Salary Aggregate

↓

Transaction Aggregate

↓

Account Aggregate

↓

Category Aggregate

↓

Fund Aggregate

↓

Loan Aggregate

↓

Cash Aggregate

```

Each aggregate communicates by identity.

Never by object ownership.

---

# Aggregate Communication

Correct

```

Transaction

↓

Account ID

```

Incorrect

```

Transaction

↓

Account Object

↓

Transactions

↓

Salary Cycle

↓

Loan

↓

Fund

```

Avoid aggregate nesting.

---

# Transaction Boundaries

One business operation should modify only one aggregate whenever possible.

Examples

Expense

Transaction Aggregate

Fund Allocation

Fund Aggregate

+

Transaction Aggregate

Loan Repayment

Loan Aggregate

+

Transaction Aggregate

Salary Close

Salary Aggregate

Cash Reconciliation

Cash Aggregate

---

# Repository Ownership

Each Aggregate Root owns exactly one Repository.

Examples

TransactionRepository

AccountRepository

CategoryRepository

LoanRepository

FundRepository

SalaryRepository

---

Child objects never own repositories.

---

# Aggregate Size

Aggregates should remain small.

Avoid loading

Thousands of transactions

inside

Account Aggregate.

Instead

Query

TransactionRepository

---

# Aggregate Consistency

Consistency is guaranteed only inside an Aggregate.

Across Aggregates

Eventual consistency is acceptable.

---

# Domain Services

Business rules involving multiple aggregates belong in Domain Services.

Examples

Salary Closing Service

Cash Reconciliation Service

Fund Allocation Service

Loan Repayment Service

Backup Service

---

# Future Aggregates

Possible future additions

Investment Aggregate

Budget Aggregate

Notification Aggregate

Exchange Rate Aggregate

Portfolio Aggregate

Tax Aggregate

---

# Design Rules

- Every aggregate has one root.
- Child entities cannot exist independently.
- Aggregates communicate by ID.
- Large aggregates should be avoided.
- Business invariants are protected by Aggregate Roots.
- Repositories belong only to Aggregate Roots.
- Cross-aggregate workflows use Domain Services.

---

# Final Statement

Aggregates define the consistency boundaries of the financial domain.

A well-designed aggregate model keeps business rules centralized, minimizes transactional complexity, improves scalability, and allows the application to evolve without tightly coupling unrelated business concepts.

The Personal Finance App intentionally favors many small aggregates connected by identity references rather than a few large object graphs.