# Cash Reconciliation Workflow

## Purpose

This document defines the cash reconciliation process used by the Personal Finance App.

Unlike traditional expense trackers that require users to record every small cash transaction, this application recognizes that real-world users often forget or intentionally skip insignificant cash expenses.

The purpose of this workflow is to maintain accurate financial records while minimizing manual data entry.

Cash reconciliation is considered a core business capability of the application.

---

# Problem Statement

Users commonly perform transactions such as:

- Bus fare
- Rickshaw fare
- Tea
- Snacks
- Parking
- Tips
- Small grocery purchases

These transactions are usually:

- forgotten
- too small to record
- inconvenient to enter immediately

Over time, cash disappears from the wallet, but the application cannot explain why.

Traditional finance applications expect perfect bookkeeping.

This project does not.

---

# Philosophy

The user should record:

- important transactions
- planned expenses
- large purchases
- transfers
- withdrawals
- deposits

The system should intelligently account for the remaining cash through periodic reconciliation.

The objective is to reduce effort without sacrificing financial accuracy.

---

# Definitions

## Cash Withdrawal

Money moved from a tracked account into physical cash.

Example:

Bank

↓

Cash Wallet

This is a transfer, not an expense.

---

## Known Cash Expense

A cash expense explicitly recorded by the user.

Example:

Grocery

Medicine

Gym Fee

Rent

---

## Untracked Cash Expense

Cash spent but not individually recorded.

Examples:

Tea

Bus Fare

Rickshaw Fare

Parking

Snacks

Tips

Small Purchases

These expenses are expected and acceptable.

---

## Cash Snapshot

The actual amount of physical cash currently held by the user.

Example

Wallet Count

= 1,320 BDT

A snapshot represents reality.

---

## Reconciliation Period

The time interval between two cash reconciliations.

Example

Previous Reconciliation

5 May

↓

Current Reconciliation

2 June

Only transactions inside this window participate in the calculation.

---

# Business Objectives

The workflow should:

- reduce manual bookkeeping
- preserve financial accuracy
- explain cash differences
- maintain auditability
- support long-term reporting

---

# High-Level Workflow

```
Previous Reconciliation

↓

ATM Withdrawals

↓

Known Cash Expenses

↓

Known Transfers

↓

Known Loan Transactions

↓

Expected Cash

↓

Physical Cash Count

↓

Cash Difference

↓

User Classification

↓

Adjustment Transaction

↓

Reconciliation Completed
```

---

# Expected Cash Calculation

Expected Cash is calculated using tracked transactions.

Formula

Expected Cash

=

Opening Cash

+

Cash Withdrawals

+

Cash Income

-

Known Cash Expenses

-

Transfers from Cash

-

Loan Payments from Cash

+

Loan Collections to Cash

± Previous Adjustments

---

# Physical Cash Count

At reconciliation time,

the user counts the actual cash in hand.

Example

Expected Cash

3,450

Actual Cash

3,180

Difference

270

---

# Difference Analysis

Difference

=

Expected Cash

-

Actual Cash

Possible outcomes

Difference = 0

Perfect reconciliation.

---

Difference > 0

Cash is missing.

Usually represents untracked cash spending.

---

Difference < 0

Unexpected extra cash exists.

Possible reasons

- forgotten income
- returned loan
- reimbursement
- manual correction

---

# User Classification

The application should never silently decide why money differs.

Instead,

the application suggests possible explanations.

The user chooses one.

Examples

- Untracked Cash Expense
- Transfer
- Loan
- Adjustment
- Other

---

# Adjustment Transaction

After user confirmation,

the system creates an adjustment transaction.

The adjustment preserves:

- amount
- date
- reason
- reconciliation session
- notes

Historical balances remain reproducible.

---

# Reconciliation Session

Every reconciliation creates a session.

Each session records:

Session ID

Start Date

End Date

Opening Cash

Expected Cash

Actual Cash

Difference

Classification

Adjustment

Completed Date

Notes

---

# Automatic Window Detection

The system should automatically determine the reconciliation period.

Window Start

=

Previous Reconciliation Date

Window End

=

Current Reconciliation Date

The user should not need to manually enter dates during normal usage.

---

# Suggested Workflow

1.

Record ATM withdrawals.

↓

2.

Record important expenses.

↓

3.

Ignore insignificant cash spending if desired.

↓

4.

When convenient,

count physical cash.

↓

5.

Application calculates expected cash.

↓

6.

Application calculates difference.

↓

7.

Application suggests classification.

↓

8.

User confirms.

↓

9.

Adjustment transaction created automatically.

↓

10.

New reconciliation window begins.

---

# Salary Cycle Interaction

Cash reconciliation does not create or close salary cycles.

Adjustment transactions belong to the active salary cycle.

Reports should remain consistent across both:

Calendar Month

Salary Cycle

---

# Carry Forward Interaction

Cash reconciliation occurs before carry-forward calculations.

Only reconciled balances participate in salary carry-forward.

This guarantees that carry-forward reflects actual available money rather than theoretical balances.

---

# Fund Interaction

Transfers into:

Emergency Fund

Purchase Fund

Investment Fund

Zakat Fund

must not appear as missing cash.

They are already tracked.

---

# Loan Interaction

Loan repayment

↓

reduces expected cash.

Loan collection

↓

increases expected cash.

Loan activity should never become an unexplained cash difference.

---

# Business Rules

- Every reconciliation creates a permanent reconciliation session.
- Historical reconciliation sessions are immutable.
- Adjustment transactions are ordinary financial transactions with a special transaction type.
- Every cash difference must have a user-approved explanation.
- Reconciliation never modifies historical transactions.
- Reports remain reproducible after reconciliation.

---

# Edge Cases

The system should correctly handle:

- Multiple ATM withdrawals
- No withdrawals during the period
- Negative cash difference
- Positive cash difference
- Retroactive expense entry
- Retroactive withdrawal
- Multiple reconciliations on the same day
- Salary received during reconciliation period
- Loan activity
- Fund transfers
- Currency changes (future)

---

# Future Enhancements

Possible future improvements include:

- Suggested classifications based on history
- Machine learning categorization
- OCR cash receipts
- Weekly reconciliation reminders
- Cash reconciliation statistics
- Confidence score for suggested adjustments

---

# Example

Opening Cash

500

ATM Withdrawal

5,000

Known Expenses

2,800

Fund Transfer

500

Expected Cash

2,200

Actual Wallet Cash

1,940

Difference

260

User selects:

Untracked Cash Expense

Application creates:

Adjustment Transaction

Type:

Untracked Cash Expense

Amount:

260

Cash after reconciliation

1,940

The system is now fully synchronized with reality.

---

# Final Statement

Cash reconciliation is one of the defining capabilities of the Personal Finance App.

Rather than requiring perfect bookkeeping, the system embraces real-world financial behavior by allowing users to record only meaningful transactions while periodically reconciling physical cash.

The result is a balance between convenience, accuracy, and long-term financial integrity.