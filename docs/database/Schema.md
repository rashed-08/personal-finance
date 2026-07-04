# Database Schema

## Purpose

This document defines the logical database schema of the Personal Finance App.

The schema maps the Domain Model to relational tables.

This document intentionally excludes:

- column definitions
- data types
- indexes
- SQL

Those are documented separately.

---

# Design Goals

The schema should:

- Represent the business domain
- Preserve financial history
- Be easy to extend
- Minimize duplication
- Support reporting
- Support future accounting improvements

---

# Schema Overview

The application is organized into the following logical modules.

| Module | Tables |
|----------|--------|
| Accounts | accounts |
| Categories | categories |
| Transactions | transactions |
| Salary | salary_cycles |
| Funds | funds |
| Loans | loans |
| Cash | cash_snapshots, cash_reconciliations |
| Recurring | recurring_transactions |
| Backup | backup_history |
| Settings | settings |

---

# Tables

## accounts

Represents financial accounts.

Examples

- Cash
- Bank
- Mobile Banking
- Savings

Owned by

Account Aggregate

Referenced by

- transactions

---

## categories

Represents income and expense categories.

Examples

- Grocery
- Rent
- Internet
- Salary
- Medicine

Owned by

Category Aggregate

Referenced by

- transactions

---

## salary_cycles

Represents one salary period.

Example

10 May

↓

9 June

Responsibilities

- Reporting
- Carry Forward
- Salary Analytics

Referenced by

- transactions

---

## transactions

Represents financial events.

Examples

- Expense
- Income
- Transfer
- Loan Payment
- Fund Allocation
- Cash Adjustment

This is the central table of the application.

Owned by

Transaction Aggregate

References

- accounts
- categories
- salary_cycles

Future

- ledger_entries

---

## funds

Represents reserved money.

Examples

- Emergency Fund
- Zakat
- Laptop Fund

Referenced by

Transactions (optional)

---

## loans

Represents receivable and payable loans.

Examples

- Money Lent
- Money Borrowed

Referenced by

Transactions (optional)

---

## recurring_transactions

Stores recurring transaction templates.

Examples

- Rent
- Internet
- Gym
- Donation

Generates

Transactions

---

## cash_snapshots

Represents physical cash counted by the user.

Used during reconciliation.

---

## cash_reconciliations

Represents reconciliation sessions.

Owns

Cash Snapshots

Creates

Adjustment Transactions

---

## backup_history

Stores backup metadata.

Examples

- Google Drive Backup
- Local Export

Independent module.

---

## settings

Stores user preferences.

Examples

- Currency
- Theme
- Salary Cycle Rules
- Backup Preferences

Expected to contain exactly one row.

---

# Relationships

| Parent | Child |
|---------|--------|
| accounts | transactions |
| categories | transactions |
| salary_cycles | transactions |
| funds | transactions (optional) |
| loans | transactions (optional) |
| recurring_transactions | transactions |
| cash_reconciliations | cash_snapshots |

---

# Aggregate Mapping

| Aggregate | Table |
|------------|-------|
| Account | accounts |
| Category | categories |
| Transaction | transactions |
| Salary | salary_cycles |
| Fund | funds |
| Loan | loans |
| Cash | cash_reconciliations |
| Backup | backup_history |
| Settings | settings |

---

# Expected Table Count

| Area | Count |
|------|------:|
| Core Financial Tables | 6 |
| Cash Management | 2 |
| Automation | 1 |
| Infrastructure | 2 |

Total

11 Tables

Future versions will introduce additional tables without modifying the existing schema.

---

# Reporting Sources

Monthly Reports

↓

transactions

Category Reports

↓

transactions

Salary Reports

↓

salary_cycles

Fund Reports

↓

funds

Loan Reports

↓

loans

Cash Reports

↓

cash_reconciliations

Backup Reports

↓

backup_history

---

# Ownership Rules

Every table has exactly one owner.

Examples

transactions

↓

Transaction Aggregate

accounts

↓

Account Aggregate

Cross-module ownership is prohibited.

Relationships use foreign keys.

---

# Future Tables

Potential additions

- ledger_entries
- budgets
- investments
- goals
- notifications
- exchange_rates
- portfolio_positions
- audit_logs
- attachments

These tables should be introduced through new migrations.

---

# Schema Evolution

The schema will evolve incrementally.

Version 1 focuses on:

- correctness
- simplicity
- maintainability

Advanced accounting features will be introduced in future versions without redesigning the existing schema.

---

# Final Statement

The schema defines the logical persistence model for the Personal Finance App.

It balances simplicity for Version 1 with sufficient flexibility to support future features such as double-entry accounting, investments, budgeting, synchronization, and multi-user support.