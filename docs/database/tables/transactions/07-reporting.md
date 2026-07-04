# Part 5 — Reporting Rules

---

## 5.1 Overview

The reporting system is entirely ledger-driven.

All reports are generated from transaction records.

No report owns financial data.

Reports never modify transactions.

The ledger remains the single source of truth.

---

## 5.2 General Reporting Principles

The reporting engine follows these principles.

- Reports are read-only.
- Reports are generated dynamically.
- Reports never store balances.
- Historical reports remain reproducible.
- Every reported value must be traceable to one or more transactions.
- No financial summary is entered manually.

---

## 5.3 Reporting Data Source

All reports derive their data from

```
transactions
```

Additional information may be joined from

```
accounts
categories
salary_cycles
funds
loans
cash_reconciliation_batches
```

However, transactions always remain the primary financial source.

---

## 5.4 Supported Reports

Version 1 supports the following reports.

### Dashboard

Displays

- Current Balance
- Monthly Income
- Monthly Expense
- Cash Balance
- Fund Balance
- Outstanding Loans
- Recent Transactions

---

### Income Report

Groups income by

- Date
- Category
- Salary Cycle
- Account

Supports

- Daily
- Weekly
- Monthly
- Custom Date Range

---

### Expense Report

Groups expenses by

- Category
- Date
- Salary Cycle
- Account

Supports

- Monthly Summary
- Custom Range
- Category Breakdown

---

### Cash Flow Report

Displays

Money In

↓

Money Out

↓

Net Cash Flow

Transfers are displayed separately.

---

### Salary Cycle Report

Displays

Opening Balance

+

Income

-

Expense

+

Adjustments

=

Closing Balance

Carry Forward

---

### Category Report

Shows

- Total Spending
- Transaction Count
- Monthly Trend
- Average Spending

Income categories follow the same structure.

---

### Account Statement

Displays all transactions for one account.

Includes

- Running Balance
- Transfers
- Adjustments

Supports export.

---

### Fund Report

Displays

- Target Amount
- Allocated Amount
- Used Amount
- Remaining Balance
- Progress Percentage

---

### Loan Report

Displays

- Original Amount
- Paid Amount
- Remaining Amount
- Payment History

Supports both

- Loans Given
- Loans Received

---

### Cash Reconciliation Report

Displays

- Opening Cash
- Cash Withdrawals
- Recorded Expenses
- Physical Cash Count
- Difference
- Adjustment

---

### Recurring Transaction Report

Displays

- Scheduled Transactions
- Executed Transactions
- Failed Executions
- Next Execution Date

---

## 5.5 Reporting Rules

Reports always include

```
POSTED
```

transactions only.

VOID transactions are ignored.

REVERSED transactions remain visible only for audit purposes.

---

Transfers

- Never increase income.
- Never increase expense.
- Never affect net worth.

Transfers only change account balances.

---

Adjustments

Must appear separately.

Users should always understand

Why

an adjustment happened.

---

Opening Balance

Included only once.

Never recalculated.

---

Migration Transactions

Behave like normal transactions.

Reports may optionally filter

Imported Data

versus

User Entered Data.

---

## 5.6 Date Rules

Reports primarily use

```
transaction_date
```

rather than

```
created_at
```

Historical transactions therefore appear in their correct financial period.

---

## 5.7 Salary Cycle Rules

Salary cycle reports always calculate

Opening Balance

↓

Income

↓

Expense

↓

Transfer

↓

Adjustment

↓

Closing Balance

↓

Carry Forward

No balance is permanently stored.

---

## 5.8 Balance Calculation Rules

Current Balance

```
Opening Balance

+

Income

-

Expense

+

Transfer In

-

Transfer Out

+

Adjustment
```

Fund Balance

```
Allocated

-

Used
```

Loan Balance

```
Original Amount

-

Repayments
```

Cash Balance

```
Opening Cash

+

Cash In

-

Cash Out

+

Cash Adjustments
```

---

## 5.9 Filtering Rules

Every report should support

- Date Range
- Salary Cycle
- Account
- Category
- Transaction Type
- Amount Range

Future versions may support

- Tags
- Notes
- Attachments
- Merchant

---

## 5.10 Sorting Rules

Default

Newest Transaction First

Optional

- Oldest First
- Amount Ascending
- Amount Descending
- Category
- Account

---

## 5.11 Export Rules

Every major report should support

- CSV
- Excel
- PDF

Future

- JSON
- Google Sheets

---

## 5.12 Performance Rules

Reports must never modify ledger data.

Indexes should support

- transaction_date
- salary_cycle_id
- category_id
- account_id
- transaction_type

Large reports should use pagination.

Summary calculations should be optimized through SQL aggregation.

---

## 5.13 Audit Rules

Every number shown in a report must be reproducible.

A user should always be able to trace

Dashboard

↓

Report

↓

Transaction

↓

Database Record

No hidden calculations are allowed.

---

## 5.14 Design Principles

The reporting engine follows

- Ledger-first architecture
- Read-only reporting
- Immutable financial history
- Reproducible calculations
- No duplicated balances
- Transparent financial summaries

---

## 5.15 Final Statement

Reporting is a projection of the financial ledger.

Reports never own financial data.

The transactions table remains the authoritative source for every balance, summary, dashboard, export and analytical report generated by the Personal Finance Application.