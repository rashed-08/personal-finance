# Financial Accounting Model

## Purpose

This document defines how financial events affect the accounting model of the Personal Finance App.

It specifies the accounting behavior of every transaction category.

This document is the authoritative reference for:

- Database Design
- Business Logic
- Reporting
- Carry Forward
- Salary Cycle
- Cash Reconciliation

---

# Philosophy

Every financial event must answer the following questions.

- Does money move?
- Where does it come from?
- Where does it go?
- Does it affect balance?
- Does it affect reports?
- Does it affect carry forward?
- Does it require reconciliation?

The answers must remain consistent across the entire application.

---

# Transaction Structure

Every transaction consists of two independent concepts.

## Transaction Nature

Defines how money behaves.

Possible values

- Income
- Expense
- Transfer
- Adjustment

Nature changes very rarely.

---

## Transaction Reason

Defines why the transaction exists.

Examples

Salary

ATM Withdrawal

Groceries

Medicine

Gym

Emergency Fund Deposit

Emergency Fund Withdrawal

Loan Given

Loan Received

Loan Repayment

Refund

Donation

Interest (Future)

Dividend (Future)

Purchase Goal Deposit

Purchase Goal Withdrawal

Untracked Cash Expense

Reason is extensible.

Future versions may introduce additional reasons without changing the accounting model.

---

# Accounting Rules

## Income

Characteristics

Money enters the user's financial world.

Examples

Salary

Bonus

Gift

Refund

Interest

Investment Return

Effects

- increases available money
- affects income reports
- affects salary reports
- affects carry forward

---

## Expense

Characteristics

Money permanently leaves the user's financial world.

Examples

Groceries

Medicine

Utilities

Transport

Gym

Donation

Effects

- decreases available money
- affects expense reports
- affects category reports
- affects salary reports
- affects carry forward

---

## Transfer

Characteristics

Money changes location.

Money is neither created nor destroyed.

Examples

Bank → Cash

Cash → Bank

Bank → Emergency Fund

Emergency Fund → Bank

Savings → Mobile Banking

Effects

- changes account balances
- does not affect income
- does not affect expense totals
- should remain visible in account history

---

## Adjustment

Characteristics

Corrects financial history without modifying historical transactions.

Examples

Cash Reconciliation

Manual Balance Correction

Migration Adjustment

Effects

- changes balances
- remains auditable
- may affect reports depending on adjustment type

---

# Accounting Matrix

| Transaction Reason | Nature | Balance | Income Report | Expense Report | Category Report | Salary Report | Carry Forward | Cash Reconciliation |
|--------------------|---------|---------|---------------|----------------|-----------------|---------------|----------------|---------------------|
| Salary | Income | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ |
| Bonus | Income | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ |
| Refund | Income | ✅ | ✅ | ❌ | Optional | ✅ | ✅ | ❌ |
| Grocery | Expense | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | Depends on account |
| Medicine | Expense | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | Depends on account |
| Gym | Expense | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | Depends on account |
| Donation | Expense | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | Depends on account |
| ATM Withdrawal | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | ❌ | Creates cash |
| Cash Deposit | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | ❌ | Removes cash |
| Fund Deposit | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | ❌ | ❌ |
| Fund Withdrawal | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | ❌ | ❌ |
| Loan Given | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | Asset change | ❌ |
| Loan Received | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | Liability change | ❌ |
| Loan Repayment | Transfer | ✅ | ❌ | ❌ | ❌ | Optional | Liability change | ❌ |
| Untracked Cash Expense | Adjustment | ✅ | ❌ | ✅ | Optional | ✅ | ✅ | ✅ |
| Manual Adjustment | Adjustment | ✅ | Optional | Optional | Optional | Optional | Depends | Optional |

---

# Balance Model

Available Balance

=

Opening Balance

+

Income

-

Expenses

± Adjustments

Transfers change where money exists but do not change the total amount of money owned by the user.

---

# Fund Accounting

Funds represent earmarked money.

Important principles

- Fund deposits are not expenses.
- Fund withdrawals are not income.
- Fund balances are tracked independently.
- Reports should distinguish between spending and reserving money.

---

# Loan Accounting

Loans do not represent income or expense.

Instead

Loan Given

creates a receivable (asset).

Loan Received

creates a payable (liability).

Repayments reduce the outstanding balance.

---

# Salary Accounting

Salary creates:

- income
- new salary cycle

Salary should never be implemented as a transfer.

---

# Cash Accounting

Cash is an account.

Cash reconciliation verifies:

Expected Cash

vs

Actual Cash

Differences are resolved through adjustment transactions.

---

# Category Accounting

Categories classify purpose.

Categories do not own money.

Categories do not affect balances.

Categories only classify transactions for reporting.

---

# Report Accounting

Reports never own financial data.

Reports aggregate transactions.

Changing a report must never modify transactions.

---

# Carry Forward Rules

Carry Forward is calculated from reconciled financial data.

It considers

- opening balance
- income
- expenses
- adjustments

It does not duplicate transactions.

---

# Immutable History

Historical transactions should not be silently modified.

Corrections should be represented by new transactions or adjustments.

This preserves auditability and reproducibility.

---

# Future Compatibility

The accounting model should support future features such as

- Multi-Currency
- Investment Portfolio
- Credit Cards
- Assets
- Liabilities
- Net Worth
- Tax
- Budget
- Subscription Management

without changing the core accounting principles.

---

# Accounting Principles

The application follows these principles.

- Every movement has a financial explanation.
- Every balance can be reproduced.
- Reports are derived from transactions.
- Transfers are not income or expense.
- Funds are allocations, not spending.
- Loans are obligations or receivables, not income or expense.
- Adjustments preserve history.
- Reconciliation corrects balances without rewriting the past.

---

# Final Statement

The Financial Accounting Model defines how financial events are interpreted throughout the Personal Finance App.

All implementations—including the database schema, Java domain model, REST APIs, reporting engine, and reconciliation logic—must follow the accounting rules defined in this document to ensure consistency, auditability, and long-term maintainability.