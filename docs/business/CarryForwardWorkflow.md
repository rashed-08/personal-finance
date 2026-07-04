# Carry Forward Workflow

## Purpose

This document defines how available money is carried from one salary cycle to the next.

Carry Forward is one of the core business concepts of the Personal Finance App.

Unlike many personal finance applications, Carry Forward is not manually entered by the user. Instead, it is calculated automatically from financial activity recorded during a salary cycle.

This document defines:

- Carry Forward calculation
- Opening Balance
- Closing Balance
- Salary Cycle continuity
- Interaction with Funds
- Interaction with Loans
- Interaction with Cash Reconciliation

---

# Philosophy

Money does not disappear between salary cycles.

If money remains available after a salary cycle ends, that money becomes the opening balance of the next salary cycle.

Carry Forward represents continuity of financial ownership.

It is not:

- new income
- salary
- profit

It is simply money that already belongs to the user.

---

# Definitions

## Opening Balance

The amount of available money at the beginning of a salary cycle.

Opening Balance is automatically calculated.

It is never entered manually.

---

## Closing Balance

The available money remaining at the end of a salary cycle after all financial activity has been considered.

---

## Carry Forward

Carry Forward equals the Closing Balance of the previous salary cycle.

Carry Forward becomes the Opening Balance of the next salary cycle.

---

# Workflow

```

Previous Salary Cycle

↓

Opening Balance

↓

Income

↓

Expenses

↓

Transfers

↓

Loans

↓

Cash Reconciliation

↓

Closing Balance

↓

Carry Forward

↓

Next Salary Cycle

↓

Opening Balance

```

---

# Calculation Order

Carry Forward is calculated only after all financial events have been processed.

Calculation sequence:

1. Opening Balance
2. Income
3. Expenses
4. Transfers
5. Fund Activity
6. Loan Activity
7. Cash Reconciliation
8. Adjustments
9. Closing Balance
10. Carry Forward

Changing this order may produce incorrect reports.

---

# Closing Balance Formula

Conceptually:

```

Closing Balance

=

Opening Balance

+

Income

-

Expenses

± Adjustments

```

Transfers require special handling because they usually move money rather than create or destroy money.

---

# Income

Included

- Salary
- Bonus
- Refund
- Other Income

Income increases Closing Balance.

---

# Expenses

Included

- Grocery
- Medicine
- Utilities
- Transport
- Gym
- Donation
- Shopping

Expenses reduce Closing Balance.

---

# Transfers

Transfers do not change total wealth.

Examples

Bank → Cash

Cash → Bank

Savings → Mobile Banking

These only move money between accounts.

Transfers do not directly change Carry Forward.

---

# Fund Activity

Fund Deposit

Money moves from an account into a fund.

The user still owns the money.

Therefore:

Fund Deposit does not reduce Carry Forward.

---

Fund Withdrawal

Money returns from a fund into an account.

This also does not create income.

It simply changes where the money is stored.

---

# Loan Activity

Loan Given

The money leaves the user's immediate control but becomes a receivable.

Carry Forward should still reflect that this value belongs to the user.

Loan Given is not treated as an expense.

---

Loan Received

Creates additional available cash.

However,

Loan Received is not income.

Reports should distinguish borrowed money from earned money.

---

Loan Repayment

Reduces liability.

Repayment decreases available balance but should not be classified as an expense category.

---

# Cash Reconciliation

Carry Forward is calculated only after reconciliation.

Reason:

Expected cash may differ from actual cash.

Adjustment transactions created during reconciliation become part of the financial history before Carry Forward is calculated.

This guarantees that Carry Forward reflects actual money.

---

# Adjustments

Adjustment transactions may:

Increase balance

or

Decrease balance

depending on reconciliation results.

Adjustments are included in Carry Forward calculations.

---

# Negative Carry Forward

Negative Carry Forward is allowed.

Example

Opening Balance

1,000

Expenses

1,800

Closing Balance

-800

This indicates the user owes money or has exceeded available cash.

Negative Carry Forward should remain visible.

It should never be silently converted to zero.

---

# Positive Carry Forward

Example

Opening Balance

10,000

Income

50,000

Expenses

42,000

Closing Balance

18,000

Carry Forward

18,000

Next Salary Cycle Opening Balance

18,000

---

# Multiple Accounts

Carry Forward is calculated across all participating accounts.

Example

Bank

10,000

Cash

2,000

Mobile Banking

3,000

Available Balance

15,000

Carry Forward considers the combined available balance rather than individual account balances.

---

# Salary Cycle Example

Salary Received

10 May

Opening Balance

5,000

Salary

50,000

Expenses

30,000

Fund Deposit

5,000

Loan Given

3,000

Cash Adjustment

-250

Closing Balance

21,750

Carry Forward

21,750

---

# Reporting

Carry Forward should appear in:

Salary Cycle Report

Dashboard

Financial Summary

Yearly Report

Trend Analysis

Carry Forward should not appear as Income.

Carry Forward should not appear as Expense.

---

# Business Rules

- Carry Forward is always calculated.
- Users cannot manually edit Carry Forward.
- Carry Forward equals the previous cycle's Closing Balance.
- Closing Balance is calculated after reconciliation.
- Historical Carry Forward values remain immutable.
- Reports must always reproduce the same Carry Forward from historical transactions.

---

# Edge Cases

The system must correctly handle:

- Late salary
- Early salary
- Missing salary
- Multiple salary payments
- Retroactive transactions
- Retroactive reconciliation
- Fund transfers
- Loan repayment
- Loan collection
- Multiple accounts
- Negative balances

---

# Future Enhancements

Possible future improvements:

- Carry Forward forecast
- Estimated month-end balance
- Future salary simulation
- Budget impact analysis
- Financial health score

---

# Final Statement

Carry Forward preserves continuity between salary cycles.

Rather than treating each month as an isolated period, the application models personal finance as a continuous financial timeline.

Carry Forward is therefore a calculated business concept derived from the user's financial history and reconciled balances.

It should never be manually maintained or independently stored as a source of truth.