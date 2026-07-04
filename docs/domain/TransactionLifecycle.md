# Transaction Lifecycle

## Purpose

This document defines the lifecycle of financial transactions within the Personal Finance App.

A transaction represents a business event that affects one or more financial accounts.

The lifecycle ensures that every transaction progresses through a predictable set of states while preserving financial history and accounting integrity.

This document is independent of database implementation.

---

# Philosophy

Financial records are historical facts.

Once a transaction becomes part of the financial history, it should never be silently rewritten.

Corrections should be represented by adjustment transactions whenever possible.

The application prioritizes auditability over convenience.

---

# Transaction Definition

A transaction represents a completed business event.

Examples

- Salary Received
- Grocery Expense
- ATM Withdrawal
- Account Transfer
- Loan Repayment
- Loan Collection
- Fund Allocation
- Fund Withdrawal
- Cash Adjustment
- Miscellaneous Reconciliation Expense

Every transaction belongs to exactly one business event.

---

# Lifecycle Overview

```
Create

↓

Validate

↓

Post

↓

Reconcile (optional)

↓

Archive (future)
```

Transactions never move backwards.

Historical records remain immutable.

---

# State Definitions

## CREATED

The transaction has been created.

Business validation has not yet completed.

Example

User starts entering an expense.

The transaction is not yet part of financial history.

---

## VALIDATED

Business rules have been checked.

Examples

- Amount > 0
- Valid account
- Valid category
- Salary cycle exists
- Account is active

The transaction is still not committed.

---

## POSTED

The transaction becomes part of the financial ledger.

Account balances are updated.

Reports include the transaction.

The transaction becomes historical.

---

## RECONCILED

The transaction has participated in a successful reconciliation process.

Example

Cash reconciliation confirms that physical cash matches expected balance.

Not every transaction will become reconciled.

---

## ARCHIVED

Future feature.

Old transactions may be archived for performance while remaining searchable.

---

# Lifecycle Diagram

```
CREATE

↓

VALIDATE

↓

POST

↓

RECONCILE

↓

ARCHIVE
```

---

# Validation Rules

Before POSTED, the following must be true

- Amount is positive
- Currency is supported
- Account exists
- Category exists (if applicable)
- Salary cycle exists
- Transaction date is valid
- Transaction type is supported

Validation failure prevents posting.

---

# Posting Rules

Posting a transaction

- updates account balance
- becomes visible in reports
- becomes part of history

Posting is irreversible.

Corrections require additional transactions.

---

# Editing Transactions

Rules

Before POSTED

Editing is allowed.

After POSTED

Editing should be restricted.

Preferred approach

Create an Adjustment Transaction.

Example

Original Expense

500

Correction

-100

Net

400

History remains complete.

---

# Deleting Transactions

Financial transactions should never be physically deleted.

Instead

- Void (future)
- Reverse (future)
- Adjustment Transaction

Historical integrity must always be preserved.

---

# Reconciliation

Cash reconciliation occurs after transactions have been posted.

Workflow

```
Transactions

↓

Expected Cash

↓

Physical Cash

↓

Difference

↓

Adjustment Transaction
```

The original transactions remain unchanged.

---

# Salary Cycle Integration

Every posted transaction belongs to exactly one salary cycle.

Closing a salary cycle does not modify transactions.

Instead

Reports are generated from existing posted transactions.

---

# Fund Integration

Fund Allocation

↓

POSTED

↓

Fund Balance Updated

↓

Available Balance Updated

Fund history remains permanent.

---

# Loan Integration

Loan Repayment

↓

POSTED

↓

Outstanding Balance Updated

↓

Loan History Updated

Loan history cannot be rewritten.

---

# Transfer Integration

Transfers follow the same lifecycle.

Example

Cash

↓

Bank

↓

POSTED

↓

Account balances updated

Transfers are not income.

Transfers are not expenses.

---

# Cash Reconciliation Integration

Cash adjustments are normal posted transactions.

The reconciliation process creates them automatically.

Users do not edit them manually.

---

# Reporting

Only POSTED transactions appear in

- Dashboard
- Reports
- Salary Summary
- Monthly Reports
- Category Reports
- Fund Reports
- Loan Reports

CREATED and VALIDATED transactions are invisible.

---

# Audit Trail

Every transaction should preserve

- Creation Time
- Posted Time
- Last Modification Time
- User (future)

Future

Adjustment relationships

Reversal relationships

Import source

---

# Future Lifecycle States

Possible future additions

- DRAFT
- VOID
- REVERSED
- IMPORTED
- SYNCED

The lifecycle is intentionally extensible.

---

# Business Rules

- Posted transactions become historical facts.
- Historical records should not be silently modified.
- Financial history is immutable.
- Corrections use adjustment transactions.
- Reports use only posted transactions.
- Reconciliation never edits existing transactions.
- Salary cycles reference transactions.
- Funds reference transactions.
- Loans reference transactions.

---

# Example Lifecycle

User enters

```
Grocery

800
```

↓

Validation succeeds

↓

Transaction becomes POSTED

↓

Cash balance decreases

↓

Monthly report updated

↓

Salary cycle updated

↓

Cash reconciliation later confirms the balance

↓

Transaction becomes RECONCILED

History remains unchanged.

---

# Final Statement

The transaction lifecycle ensures that every financial event progresses through a consistent, auditable, and immutable workflow.

By separating creation, validation, posting, reconciliation, and future archival, the application preserves accounting integrity while remaining flexible enough to support future features such as synchronization, imports, reversals, and double-entry accounting.