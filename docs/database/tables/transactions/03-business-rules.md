# Part 3 — Business Rules

---

## 3.1 General Business Rules

The following rules apply to every transaction in the system.

### Rule 1

Every financial event must create exactly one transaction.

No balance may be updated directly.

---

### Rule 2

Transaction history is immutable.

Existing transactions must never be edited to change financial history.

Corrections must be recorded using new adjustment transactions.

---

### Rule 3

Transaction amounts must always be positive.

Negative values are not allowed.

Money direction is determined by the transaction type.

---

### Rule 4

Every transaction belongs to exactly one transaction type.

A transaction cannot belong to multiple types simultaneously.

---

### Rule 5

Every transaction must have an effective financial date.

Reports are generated using the transaction date.

---

### Rule 6

Every transaction must belong to one user.

Future versions may support multiple users.

---

## 3.2 Validation Rules

Before saving a transaction, the following validations must succeed.

### Common Validation

- Amount must be greater than zero.
- Transaction date is required.
- Transaction type is required.
- Transaction status is required.

---

### Income Validation

Required

- Destination account
- Income category

Forbidden

- Source account

---

### Expense Validation

Required

- Source account
- Expense category

Forbidden

- Destination account

---

### Transfer Validation

Required

- Source account
- Destination account

Validation

```
from_account_id != to_account_id
```

Transfer amount must be positive.

---

### Adjustment Validation

Required

- Adjustment reason

Optional

- Account
- Category

Notes should be mandatory for manual corrections.

---

### Opening Balance Validation

Allowed only during

- Initial setup
- New account creation
- Data migration

---

### Migration Validation

Migration transactions must reference

```
migration_batch_id
```

---

## 3.3 Transaction Lifecycle

Every transaction follows the same lifecycle.

```
Draft (Future)

↓

Validation

↓

POSTED

↓

Reported

↓

Archived (Future)
```

Version 1 creates transactions directly as

```
POSTED
```

Transactions are never physically deleted.

---

## 3.4 Salary Cycle Rules

Salary cycles are reporting periods.

They do not own transactions.

Each transaction may optionally reference a salary cycle.

### Rules

- A salary cycle has a start date.
- A salary cycle has an end date.
- Transactions belong to a salary cycle based on their transaction date.
- Carry forward is calculated automatically.
- Historical salary cycles remain unchanged.

Changing salary cycle dates must not rewrite historical reports.

---

## 3.5 Cash Reconciliation Rules

Cash reconciliation exists to account for untracked cash spending.

### Workflow

ATM Withdrawal

↓

Cash Balance

↓

Untracked Spending

↓

Cash Count

↓

Difference

↓

Adjustment Transaction

---

Rules

Cash is not expected to have every expense recorded.

Only reconciliation creates adjustment transactions.

Every reconciliation must preserve historical audit information.

Cash reconciliation never modifies previous transactions.

---

## 3.6 Carry Forward Rules

Carry Forward represents the remaining available balance between salary cycles.

Formula

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

=

Closing Balance
```

Closing Balance becomes the next cycle's opening balance.

Carry forward is always calculated.

It is never manually entered.

Historical carry forward values never change after a cycle is finalized.

---

## 3.7 Account Balance Rules

Account balances are derived values.

They are never stored permanently.

General Formula

```
Current Balance

=

Opening Balance

+

Incoming Transactions

-

Outgoing Transactions

+

Adjustments
```

Transfers affect individual account balances but do not change total net worth.

---

## 3.8 Category Rules

Categories classify financial activity.

Rules

- Income transactions may use only income categories.
- Expense transactions may use only expense categories.
- Transfer transactions never use categories.
- Archived categories cannot be selected for new transactions.
- Historical transactions continue referencing archived categories.

---

## 3.9 Reporting Rules

All reports are generated from transaction data.

No report stores calculated values permanently.

Reports include

- Income Report
- Expense Report
- Cash Flow
- Salary Cycle Report
- Fund Report
- Loan Report
- Dashboard Summary

Reports always use posted transactions only.

---

## 3.10 Error Handling Rules

Transactions must be rejected when

- Amount is zero.
- Amount is negative.
- Required accounts are missing.
- Required categories are missing.
- Source and destination accounts are identical.
- Transaction type is invalid.
- Transaction date is missing.

Future versions may introduce additional validation.

---

## 3.11 Design Decisions

The transaction module follows these architectural decisions.

- Ledger-first architecture
- Immutable financial history
- Calculated balances
- Generic transaction model
- Business modules built on top of the ledger
- Explicit adjustment transactions
- Salary-cycle based reporting
- Automatic carry forward
- Automatic cash reconciliation support

These principles should remain stable throughout future versions of the application.