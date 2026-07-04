# Loan Workflow

## Purpose

This document defines how personal loans are managed within the Personal Finance App.

The application supports both:

- Money lent to others (Receivable)
- Money borrowed from others (Payable)

Loans are tracked independently from income and expenses.

The goal is to maintain an accurate record of outstanding obligations while preserving correct financial reports.

---

# Philosophy

Loans do not create wealth.

Loans do not consume wealth.

Loans temporarily transfer money between parties.

Therefore:

- Giving a loan is not an expense.
- Receiving a loan is not income.
- Repayment is not an expense category.
- Loan collection is not income.

Loans represent assets or liabilities.

---

# Loan Types

The system supports two primary loan types.

## Receivable

Money given to another person.

The borrower owes the user.

Examples

- Friend
- Family Member
- Colleague

---

## Payable

Money borrowed by the user.

The user owes someone else.

Examples

- Friend
- Family Member

---

# Loan Lifecycle

```
Create Loan

↓

Disbursement

↓

Outstanding Balance

↓

Repayment

↓

Remaining Balance

↓

Repayment

↓

Remaining Balance

↓

Loan Closed
```

---

# Loan Creation

Creating a loan establishes an agreement.

Required information

- Loan Type
- Borrower / Lender
- Amount
- Date
- Notes

Optional

- Due Date
- Reminder
- Interest (Future)

---

# Loan Disbursement

Example

Available Balance

50,000

↓

Loan Given

10,000

↓

Available Balance

40,000

Receivable

10,000

Total Wealth

50,000

The user still owns the money.

It is simply owed by another person.

---

# Loan Received

Example

Available Balance

20,000

↓

Loan Received

15,000

↓

Available Balance

35,000

Outstanding Liability

15,000

Available money increases.

However,

this is not income.

---

# Partial Repayment

Repayments may occur multiple times.

Example

Loan

10,000

↓

Repayment

2,000

↓

Outstanding

8,000

↓

Repayment

3,000

↓

Outstanding

5,000

↓

Repayment

5,000

↓

Closed

---

# Overpayment

Repayment must never exceed the outstanding balance.

Example

Outstanding

2,000

Attempted Repayment

3,000

Result

Validation Error

---

# Loan Collection

For Receivable loans,

money returns to available balance.

Example

Outstanding

10,000

↓

Collected

4,000

↓

Outstanding

6,000

↓

Available Balance

+4,000

This is not income.

---

# Loan Repayment

For Payable loans,

money leaves available balance.

Example

Outstanding

8,000

↓

Repayment

2,000

↓

Outstanding

6,000

↓

Available Balance

-2,000

This is not an expense category.

---

# Accounting Rules

Loan Given

Nature

Transfer

Effects

- decreases available balance
- increases receivable
- does not create expense

---

Loan Received

Nature

Transfer

Effects

- increases available balance
- increases payable
- does not create income

---

Loan Collection

Nature

Transfer

Effects

- increases available balance
- decreases receivable

---

Loan Repayment

Nature

Transfer

Effects

- decreases available balance
- decreases payable

---

# Salary Cycle

Loan activities belong to the salary cycle in which they occur.

However,

loan movements should be displayed separately from:

- income
- expenses

---

# Carry Forward

Carry Forward includes the financial effect of loan transactions.

Dashboard should distinguish between:

Available Balance

Receivable

Payable

Net Position

---

# Reports

Loan Report should display

- Active Loans
- Closed Loans
- Total Receivable
- Total Payable
- Collection History
- Repayment History

Loan activity should not appear in expense reports.

Loan activity should not appear in income reports.

---

# Loan Status

Possible statuses

- ACTIVE
- CLOSED
- CANCELLED

Future

- OVERDUE

---

# Closing a Loan

A loan may only be closed when

Outstanding Balance = 0

Closed loans remain visible in history.

---

# Editing Loans

Historical loan transactions should not be silently modified.

Corrections should create adjustment transactions whenever practical.

---

# Dashboard

Dashboard should display

- Total Receivable
- Total Payable
- Net Position
- Active Loan Count

---

# Future Enhancements

Future versions may support

- Interest
- Installments
- Due Dates
- Automatic Reminders
- Attachments
- Payment Schedule
- Guarantor
- Legal Status

without changing the accounting model.

---

# Business Rules

- Loans are not income.
- Loans are not expenses.
- Partial repayment is supported.
- Outstanding balance can never become negative.
- Closed loans remain in history.
- Every repayment references an existing loan.
- Loan history remains immutable.
- Reports distinguish receivables from payables.

---

# Example Scenario

Opening Balance

30,000

↓

Loan Given

10,000

↓

Available

20,000

Receivable

10,000

↓

Borrower Repays

3,000

↓

Available

23,000

Receivable

7,000

↓

Borrower Repays

7,000

↓

Available

30,000

Receivable

0

↓

Loan Closed

---

# Final Statement

Loans represent financial obligations rather than income or expenses.

The application tracks loan lifecycles independently from ordinary transactions, allowing accurate financial reporting, outstanding balance monitoring, and historical traceability while preserving the integrity of the ledger-based accounting model.