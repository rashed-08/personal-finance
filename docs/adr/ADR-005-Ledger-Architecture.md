# ADR-005: Ledger-Based Transaction Model

## Status

Accepted

---

## Date

2026-07-04

---

## Context

The Personal Finance App is designed around long-term financial history rather than short-term balance tracking.

The application supports:

- Salary Cycles
- Calendar Reports
- Cash Reconciliation
- Fund Management
- Loan Tracking
- Carry Forward
- Historical Reporting
- Google Keep Migration
- Future Google Drive Backup

Every one of these features depends on accurate financial history.

A traditional CRUD-oriented balance model cannot reliably reproduce historical financial states after corrections or future feature additions.

Therefore, a different architecture is required.

---

# Problem

Many finance applications maintain balances directly.

Example

Account

Current Balance

25,000

Whenever a transaction changes,

the balance field is updated.

Although simple,

this approach introduces several problems.

---

## Problem 1

Historical balances cannot always be reproduced.

Example

A transaction from three months ago is edited.

The current balance changes.

Historical reports may also change unexpectedly.

---

## Problem 2

Audit history becomes difficult.

Questions such as

"Why is today's balance different?"

cannot always be answered.

---

## Problem 3

Cash reconciliation becomes unreliable.

Manual balance updates may overwrite important history.

---

## Problem 4

Carry Forward depends on previously calculated balances rather than actual financial events.

---

## Problem 5

Future features such as:

- Investments
- Multi-Currency
- Net Worth
- Budget Forecasting

become increasingly difficult.

---

# Decision

The application will use a Ledger-Based Architecture.

Transactions become the single source of truth.

Balances are derived from transactions.

Reports are derived from transactions.

Carry Forward is derived from transactions.

Salary Cycles are derived from transactions.

Cash Reconciliation creates new transactions rather than modifying existing ones.

---

# Ledger Principle

Every financial event is represented as a transaction.

Nothing changes financial history except creating another transaction.

Historical transactions are never silently rewritten.

---

# Source of Truth

The following data is authoritative.

Transactions

Nothing else.

The following are derived:

- Account Balance
- Dashboard
- Reports
- Salary Reports
- Category Reports
- Fund Balance
- Loan Balance
- Carry Forward
- Cash Balance

---

# Financial Timeline

```
Salary

↓

Transaction

↓

Expense

↓

Transfer

↓

Loan

↓

Fund

↓

Adjustment

↓

Reports

↓

Carry Forward
```

Every event extends the financial timeline.

History remains complete.

---

# Immutable History

Historical transactions should be treated as immutable.

If a mistake occurs,

the preferred solution is

Correction Transaction

rather than

History Rewrite

Certain administrative corrections may still be permitted,

but must remain auditable.

---

# Reconciliation

Cash reconciliation never modifies history.

Instead

Difference

↓

Adjustment Transaction

↓

Updated Balance

The original transactions remain unchanged.

---

# Carry Forward

Carry Forward is never stored as the primary source of truth.

Instead

Opening Balance

+

Income

-

Expenses

± Adjustments

=

Closing Balance

↓

Carry Forward

Carry Forward can always be reproduced.

---

# Reports

Reports never store financial values.

Instead

Transactions

↓

Filtering

↓

Grouping

↓

Aggregation

↓

Visualization

This guarantees consistency.

---

# Balance Strategy

Balances should normally be calculated.

The implementation may introduce cached balances for performance.

Cached balances are optimization only.

Transactions remain authoritative.

If cache becomes inconsistent,

it can always be rebuilt.

---

# Benefits

The Ledger Architecture provides:

- Complete audit history
- Deterministic reports
- Reproducible balances
- Reliable carry forward
- Reliable reconciliation
- Easier debugging
- Extensible architecture
- Long-term maintainability

---

# Consequences

The application becomes slightly more complex.

Balance calculation requires aggregation.

Reporting requires querying transactions.

However,

the long-term benefits significantly outweigh the additional implementation complexity.

---

# Alternatives Considered

## Option 1

Mutable Balance Model

Rejected

Reason

Historical reconstruction becomes difficult.

---

## Option 2

Balance-Only Storage

Rejected

Reason

Cannot support reconciliation correctly.

---

## Option 3

Hybrid Model

Store balances and transactions together.

Accepted partially.

Balances may be cached for performance,

but transactions remain the source of truth.

---

# Future Compatibility

The Ledger Architecture naturally supports:

- Investment Portfolio
- Budget
- Credit Cards
- Assets
- Liabilities
- Net Worth
- Multi-Currency
- Exchange Rates
- Forecasting
- AI Financial Insights

without redesigning the accounting engine.

---

# Implementation Principles

Future implementation should follow these principles.

- Every financial event becomes a transaction.
- Transactions remain immutable whenever practical.
- Corrections create new transactions.
- Reports never own financial data.
- Cached balances may exist.
- Ledger remains authoritative.

---

# Risks

Ledger architectures require:

- careful transaction design
- good indexing
- efficient reporting queries
- proper reconciliation logic

These risks are manageable and acceptable.

---

# Final Decision

The Personal Finance App adopts a Ledger-Based Transaction Model.

Transactions are the authoritative financial record.

Everything else—including balances, reports, salary cycles, carry forward, dashboards, and reconciliation—is derived from the ledger.

This decision establishes the architectural foundation for the entire application and should remain stable throughout the lifetime of the project.