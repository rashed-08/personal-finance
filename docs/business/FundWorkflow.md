# Fund Workflow

## Purpose

This document defines how funds are managed within the Personal Finance App.

Funds allow money to be reserved for a specific purpose without treating the reservation as an expense.

Typical examples include:

- Emergency Fund
- Zakat Fund
- Purchase Goal
- Education Fund
- Travel Fund
- Charity Reserve

The workflow ensures that reserved money remains part of the user's total wealth while reducing the amount available for everyday spending.

---

# Philosophy

A fund does not own money.

A fund reserves money.

Moving money into a fund does not spend money.

Moving money out of a fund does not earn money.

Funds simply represent financial intentions.

---

# Definitions

## Fund

A logical container representing reserved money for a specific purpose.

---

## Fund Allocation

The act of reserving money into a fund.

---

## Fund Withdrawal

The act of releasing reserved money back into available balance.

---

## Fund Balance

The amount currently reserved inside a fund.

---

## Available Balance

Money that is immediately available for spending.

---

## Total Wealth

The sum of:

- Available Balance
- Reserved Funds
- Receivables

(Loans payable are tracked separately.)

---

# Workflow

```

Available Money

↓

Allocate to Fund

↓

Reserved Balance

↓

(Optional)

Withdraw from Fund

↓

Available Money

↓

Spend

```

---

# Example

Salary Received

60,000

↓

Emergency Fund

10,000

↓

Zakat Fund

5,000

↓

Purchase Goal

8,000

↓

Available Balance

37,000

The user still owns the full 60,000.

Only 37,000 is freely available.

---

# Fund Creation

A user may create multiple funds.

Examples

Emergency Fund

Zakat Fund

Laptop Fund

Vacation Fund

Home Improvement Fund

Wedding Fund

Future fund types may be added without changing the accounting model.

---

# Fund Allocation

Fund Allocation reserves money.

Example

Available Balance

50,000

↓

Allocate

10,000

↓

Emergency Fund

Available Balance

40,000

Fund Balance

10,000

Total Wealth

50,000

---

# Fund Withdrawal

Money returns to available balance.

Example

Emergency Fund

10,000

↓

Withdraw

3,000

↓

Available Balance

+3,000

Emergency Fund

7,000

---

# Spending Reserved Money

Funds are never spent directly.

Correct flow

```

Emergency Fund

↓

Withdraw

↓

Available Balance

↓

Expense

```

Incorrect flow

```

Emergency Fund

↓

Expense

```

Funds must first be released.

---

# Accounting Rules

Fund Allocation

Nature

Transfer

Effects

- decreases available balance
- increases reserved balance
- does not create expense

---

Fund Withdrawal

Nature

Transfer

Effects

- increases available balance
- decreases reserved balance
- does not create income

---

# Reports

Fund Allocation should appear in

- Fund Report
- Dashboard
- Fund History

Fund Allocation should not appear in

- Expense Report
- Income Report

---

# Salary Cycle

Fund activity belongs to the salary cycle in which it occurred.

However,

Fund Allocation does not reduce earned income.

It only changes money availability.

---

# Carry Forward

Reserved funds remain owned by the user.

Therefore,

Carry Forward includes reserved money.

Available Carry Forward and Total Carry Forward may be displayed separately.

Example

Closing Balance

50,000

Reserved

15,000

Available

35,000

---

# Cash Reconciliation

Fund balances do not participate directly in cash reconciliation.

Only actual accounts (Cash, Bank, Mobile Banking) are reconciled.

---

# Editing Fund Transactions

Historical allocations should not be silently modified.

Corrections should create adjustment transactions whenever practical.

This preserves financial history.

---

# Closing a Fund

A fund may only be closed when its balance is zero.

Workflow

Withdraw Remaining Balance

↓

Fund Balance = 0

↓

Close Fund

Closed funds remain visible in historical reports.

---

# Dashboard

Dashboard should display:

- Total Reserved
- Available Balance
- Total Wealth
- Fund Summary
- Individual Fund Balances

---

# Future Enhancements

Future versions may support:

- Target Amount
- Target Date
- Monthly Contribution Goal
- Progress Percentage
- Automatic Allocation Rules
- Investment-backed Funds

without changing the accounting model.

---

# Business Rules

- A fund is not a bank account.
- Fund Allocation is not an expense.
- Fund Withdrawal is not income.
- Funds reserve money.
- Spending requires withdrawal first.
- Funds belong to salary cycles.
- Closed funds remain in history.
- Fund balances contribute to total wealth.
- Funds never rewrite financial history.

---

# Example Scenario

Opening Balance

20,000

↓

Salary

50,000

↓

Available

70,000

↓

Emergency Fund

10,000

↓

Zakat Fund

5,000

↓

Available

55,000

↓

Emergency Withdrawal

2,000

↓

Available

57,000

↓

Medical Expense

2,000

↓

Available

55,000

↓

End of Cycle

Reserved

13,000

Available

55,000

Total Wealth

68,000

---

# Final Statement

Funds represent intentional reservation of money rather than spending.

They help the user organize future financial commitments while preserving the integrity of accounting reports.

The application distinguishes between available money and reserved money, ensuring that reports, carry forward calculations, and financial history remain accurate and explainable.