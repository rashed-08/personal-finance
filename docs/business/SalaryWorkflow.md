# Salary Workflow

## Purpose

This document defines how salary is managed within the Personal Finance App.

Unlike traditional expense trackers that assume salaries are received on the first day of each month, this application supports real-world salary cycles.

The workflow defined here governs:

- Salary recording
- Salary cycle generation
- Expense attribution
- Carry-forward balances
- Reporting
- Financial continuity

This workflow is considered one of the core business processes of the system.

---

# Problem Statement

Most financial applications organize reports by calendar month.

Example:

January

1 Jan → 31 Jan

However, many users receive their salary on different dates.

Example:

10 January

10 February

11 March

Financial behavior therefore follows salary periods rather than calendar months.

The application should support both perspectives.

---

# Definitions

## Calendar Month

A fixed reporting period.

Example:

1 May

↓

31 May

---

## Salary Cycle

A reporting period beginning on one salary payment and ending the day before the next salary payment.

Example

Salary Received

10 May

↓

Cycle Start

10 May

↓

Cycle End

9 June

↓

Next Salary

10 June

---

## Carry Forward

The remaining available balance from one salary cycle that continues into the next cycle.

Carry Forward does not represent new income.

It represents unused money.

---

# Business Objectives

The salary workflow should:

- Reflect real financial behavior.
- Support delayed salary payments.
- Preserve financial continuity.
- Produce meaningful reports.
- Avoid forcing manual calculations.

---

# Workflow Overview

```
Salary Received

↓

Salary Cycle Opens

↓

Expenses

↓

Transfers

↓

Fund Deposits

↓

Loan Payments

↓

Cash Reconciliation

↓

Cycle Ends

↓

Carry Forward Calculated

↓

Next Salary Arrives

↓

New Salary Cycle Begins
```

---

# Salary Recording

A salary transaction shall contain:

- Payment Date
- Amount
- Receiving Account
- Employer (optional)
- Notes (optional)

Salary is recorded as an income transaction.

No special balance table should be updated.

---

# Salary Cycle Creation

Each salary transaction creates a new salary cycle.

Cycle Start

=

Salary Payment Date

Cycle End

=

One day before the next salary payment.

If the next salary has not yet been received,

the cycle remains open.

---

# Active Salary Cycle

Only one salary cycle can be active at any time.

The active cycle closes automatically when the next salary transaction is recorded.

---

# Expense Attribution

Every expense belongs to:

A Calendar Month

AND

A Salary Cycle

Example

Expense Date

15 May

Salary Date

10 May

Expense belongs to:

Calendar Month

May

Salary Cycle

10 May → 9 June

---

# Income Attribution

Income transactions are associated with the salary cycle active on the transaction date.

Special income such as bonuses may also belong to the active cycle.

---

# Carry Forward

At the end of each salary cycle:

Remaining Balance

=

Available Money

-

Expenses

-

Transfers Out

+

Transfers In

± Adjustments

Carry Forward

=

Remaining Balance

The carry-forward becomes the opening balance of the next salary cycle.

---

# Salary Cycle Balance

Each cycle should display:

Opening Balance

+

Salary

+

Other Income

-

Expenses

-

Transfers

-

Fund Deposits

-

Loan Payments

± Adjustments

=

Closing Balance

---

# Multiple Salary Payments

Future versions may support multiple income sources.

Examples

Primary Salary

Part-Time Income

Business Income

The workflow should remain compatible with multiple recurring income streams.

---

# Late Salary

If salary is received later than expected,

the previous salary cycle simply continues.

Example

Expected

10 June

Actual

15 June

The cycle becomes:

10 May

↓

14 June

No manual intervention should be required.

---

# Early Salary

If salary is received earlier than expected,

the previous cycle ends early.

Example

Expected

10 June

Actual

7 June

Previous cycle ends

6 June

New cycle starts

7 June

---

# Missing Salary

If salary is skipped,

the active cycle remains open until another salary transaction is recorded.

Reports should correctly reflect the extended cycle.

---

# Reports

The application shall support:

## Calendar Reports

Monthly

Yearly

Quarterly

---

## Salary Reports

Current Salary Cycle

Previous Salary Cycle

Custom Salary Cycle

Salary Comparison

Expense by Salary Cycle

Fund Growth by Salary Cycle

Loan Activity by Salary Cycle

---

# Dashboard

Dashboard should display:

Current Salary Cycle

Salary Date

Days Until Next Expected Salary (optional)

Current Spending

Remaining Balance

Carry Forward Estimate

---

# Interaction with Funds

Money transferred into:

Emergency Fund

Purchase Fund

Investment Fund

Zakat Fund

is treated as a transfer of money,

not as salary consumption.

Funds remain independently tracked.

---

# Interaction with Loans

Loan repayment reduces available balance.

Loan collection increases available balance.

Loans should appear in salary reports without being classified as expenses.

---

# Interaction with Cash Reconciliation

Cash reconciliation adjusts available cash,

but does not alter the salary cycle itself.

Adjustment transactions belong to the active salary cycle.

---

# Business Rules

- Every salary starts a new salary cycle.
- Only one salary cycle may be active.
- Carry Forward belongs to the next cycle.
- Salary reports and calendar reports are independent views of the same transactions.
- Expenses always belong to both a calendar month and a salary cycle.
- Salary cycles should be created automatically.
- Historical salary cycles should never change after creation, except through explicit data correction.

---

# Edge Cases

The system should correctly handle:

- Late salary
- Early salary
- Missing salary
- Bonus income
- Retroactive salary entry
- Salary correction
- Multiple income sources
- Salary account changes

---

# Future Enhancements

Potential future improvements:

- Multiple employers
- Expected salary notifications
- Salary forecasting
- Salary trends
- Annual salary summaries
- Tax calculations
- Increment history

---

# Examples

## Example 1

Salary

10 May

50,000

Expenses

32,000

Fund Transfer

5,000

Loan Repayment

3,000

Closing Balance

10,000

Carry Forward

10,000

---

## Example 2

Opening Balance

10,000

Salary

50,000

Available

60,000

Expenses

41,000

Closing

19,000

Carry Forward

19,000

---

# Final Statement

The salary workflow is the foundation of the application's financial timeline.

Rather than forcing financial activity into calendar months, the system models salary-based financial behavior while preserving compatibility with traditional calendar reporting.

This workflow should remain the authoritative reference for all salary-related business logic.