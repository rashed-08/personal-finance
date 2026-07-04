# Entity Relationship Diagram (ERD)

## Purpose

This document defines the conceptual Entity Relationship Diagram (ERD) for the Personal Finance App.

The ERD represents business entities and their relationships.

It intentionally omits implementation details such as:

- column definitions
- data types
- indexes
- constraints

Those are documented separately.

---

# Design Principles

The ERD is based on the Domain Model.

Business requirements determine entities.

Entities determine relationships.

Relationships determine the database schema.

The database is not the source of business rules.

---

# Core Modules

The current system consists of the following domains:

- Accounts
- Categories
- Transactions
- Salary
- Funds
- Loans
- Cash Reconciliation
- Recurring Transactions
- Backup
- Settings

---

# Conceptual ER Diagram

```text

                         +----------------+
                         |    Account     |
                         +----------------+
                                 ▲
                                 |
                                 |
                        (Many Transactions)
                                 |
                                 |
+---------------+        +--------------------+        +----------------+
|   Category    |------->|    Transaction     |<-------|  Salary Cycle  |
+---------------+        +--------------------+        +----------------+
          ▲                        ▲
          |                        |
          |                        |
          |                 +--------------+
          |                 |    Fund      |
          |                 +--------------+
          |                        ▲
          |                        |
          |                 +--------------+
          |                 |    Loan      |
          |                 +--------------+
          |
          |
          |
  +--------------------+
  |Recurring Template  |
  +--------------------+




                 +----------------------+
                 | Cash Reconciliation  |
                 +----------------------+
                           ▲
                           |
                    +---------------+
                    | Cash Snapshot |
                    +---------------+



+----------------+
| Backup History |
+----------------+

+----------------+
|    Settings    |
+----------------+

```

---

# Relationships

## Account → Transaction

One Account

↓

Many Transactions

Each transaction belongs to exactly one primary account.

---

## Category → Transaction

One Category

↓

Many Transactions

Categories classify transactions.

---

## Salary Cycle → Transaction

One Salary Cycle

↓

Many Transactions

Every transaction belongs to one salary cycle.

---

## Fund → Transaction

One Fund

↓

Many Transactions

A transaction may optionally allocate money to a fund.

---

## Loan → Transaction

One Loan

↓

Many Transactions

Loan repayment and collection transactions reference a loan.

---

## Recurring Transaction → Transaction

One recurring template

↓

Generates many transactions

Generated transactions are normal transactions.

---

## Cash Reconciliation → Cash Snapshot

One reconciliation

↓

Contains multiple snapshots.

Snapshots are historical.

---

## Cash Reconciliation → Transaction

A reconciliation may create one or more adjustment transactions.

Original transactions remain unchanged.

---

## Backup History

Independent module.

Stores backup metadata.

Does not participate in financial calculations.

---

## Settings

Independent module.

Stores application preferences.

---

# Relationship Cardinality

| Parent | Child | Cardinality |
|---------|--------|-------------|
| Account | Transaction | 1 : N |
| Category | Transaction | 1 : N |
| Salary Cycle | Transaction | 1 : N |
| Fund | Transaction | 1 : N (Optional) |
| Loan | Transaction | 1 : N (Optional) |
| Recurring Transaction | Transaction | 1 : N |
| Cash Reconciliation | Cash Snapshot | 1 : N |
| Cash Reconciliation | Transaction | 1 : N (Adjustment Only) |

---

# Aggregate Ownership

| Aggregate | Root Entity |
|------------|-------------|
| Account | Account |
| Category | Category |
| Transaction | Transaction |
| Salary | Salary Cycle |
| Fund | Fund |
| Loan | Loan |
| Cash | Cash Reconciliation |
| Backup | Backup History |
| Settings | Settings |

---

# Data Flow

Salary

↓

Transaction

↓

Account Balance

↓

Reports

↓

Cash Reconciliation

↓

Adjustment Transaction

↓

Updated Reports

---

# External Systems

Version 1 supports:

Google Drive Backup

Future versions may support:

- Google Keep Import
- Bank Statement Import
- CSV Import
- API Import

These systems remain outside the financial domain.

---

# Version 1 Scope

Included

- Single User
- Single Currency
- Salary Cycle
- Carry Forward
- Funds
- Loans
- Cash Reconciliation
- Reports
- Backup

Future

- Multiple Users
- Multiple Currencies
- Budgets
- Investments
- Notifications
- AI Insights

---

# Future Evolution

The current ERD intentionally leaves room for future entities.

Potential additions:

- Ledger Entry
- Budget
- Investment
- Goal
- Notification
- Exchange Rate
- Portfolio

These can be added without redesigning the existing relationships.

---

# Final Statement

This ERD represents the conceptual structure of the Personal Finance App.

It is intentionally technology-independent and focuses on business relationships rather than implementation details.

Subsequent documents define the logical schema, physical tables, constraints, indexes, and migration strategy.