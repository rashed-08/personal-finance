# Cash Management Table Specification

## Purpose

The Cash Management module supports reconciliation of physical cash with recorded financial transactions.

Users are not expected to record every small cash expense.

Instead, the application periodically reconciles physical cash against a ledger-derived expected balance and
automatically identifies untracked differences.

This workflow significantly reduces manual data entry while maintaining accurate financial records.

Reconciliation is scoped to a single account (an account of type `CASH`), not the wallet in general, since the
application supports more than one cash account.

---

# Aggregate

Cash Aggregate

Aggregate Root

CashReconciliation

Child Entity

CashSnapshot

---

# Tables

This module consists of two tables:

- cash_reconciliations
- cash_snapshots

---

# Overview

Cash Flow

```
ATM Withdrawal

↓

Cash Spending

↓

(Some expenses recorded)

↓

(Some expenses NOT recorded)

↓

One or more Cash Counts (Snapshots)

↓

Reconciliation Completed

↓

Adjustment Transaction (if the counts differ from expected)
```

---

# Table

cash_reconciliations

Purpose

Represents one reconciliation session for a single cash account.

Each reconciliation compares:

Expected Cash (derived from the ledger)

vs

Actual Cash (the most recently recorded physical count)

and, if they differ, creates exactly one adjustment transaction.

---

## Columns

| Column | Type | Nullable | Default | Description |
|---------|------|----------|---------|-------------|
| id | UUID | No | Generated | Primary Key |
| account_id | UUID | No | | The cash account being reconciled |
| reconciliation_date | DATE | No | | Date of reconciliation |
| expected_cash_amount | NUMERIC(18,2) | No | | Ledger-derived balance as of reconciliation_date |
| actual_cash_amount | NUMERIC(18,2) | Yes | | Most recent snapshot's amount; null until one is recorded |
| difference_amount | NUMERIC(18,2) | Yes | | actual_cash_amount − expected_cash_amount; null under the same condition |
| status | VARCHAR(20) | No | PENDING | PENDING / COMPLETED |
| adjustment_transaction_id | UUID | Yes | | The resulting adjustment transaction, if the difference was non-zero |
| notes | TEXT | Yes | | User notes / classification of the difference |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Last update |

---

## Primary Key

id

UUID

---

## Constraints

expected_cash_amount, actual_cash_amount

```
CHECK (>= 0)
```

difference_amount

Automatically calculated

```
Actual Cash Amount

-

Expected Cash Amount
```

A positive difference means unexpected extra cash was found; a negative difference means cash is missing.

---

status

Allowed values

- PENDING
- COMPLETED

A reconciliation is created PENDING with `actual_cash_amount` and `difference_amount` both null — expected cash
is already known (it only depends on the ledger), but there is no physical count yet. Both become required the
moment the reconciliation transitions to COMPLETED.

---

## Foreign Keys

account_id → accounts(id), `ON DELETE RESTRICT`

adjustment_transaction_id → transactions(id), `ON DELETE SET NULL`

---

## Business Rules

- Reconciliation is immutable after completion.
- A completed reconciliation cannot be edited or have further cash counts recorded against it.
- An account may have at most one PENDING reconciliation at a time.
- Adjustment transactions are created only when the difference is non-zero.
- Difference may be positive or negative.

---

# Table

cash_snapshots

Purpose

Stores every physical cash count performed during a reconciliation session.

Snapshots preserve historical cash counts even after the session completes, and let a user record several counts
(e.g. morning and evening) before finalizing.

---

## Columns

| Column | Type | Nullable | Default | Description |
|---------|------|----------|---------|-------------|
| id | UUID | No | Generated | Primary Key |
| reconciliation_id | UUID | No | | Parent reconciliation |
| snapshot_time | TIMESTAMP | No | CURRENT_TIMESTAMP | Count time |
| cash_amount | NUMERIC(18,2) | No | | Counted cash |
| notes | VARCHAR(500) | Yes | | Optional remarks |

---

## Relationships

One Reconciliation

↓

Many Cash Snapshots

```
Cash Reconciliation

↓

Cash Snapshot

↓

Historical Counts
```

The most recently recorded snapshot's amount becomes the reconciliation's current `actual_cash_amount`.

---

## Foreign Key

reconciliation_id

↓

cash_reconciliations(id), `ON DELETE CASCADE`

Snapshots have no meaning independent of their reconciliation, so they are deleted along with it.

---

# Lifecycle

Cash Withdrawal

↓

Cash Spending

↓

Start Reconciliation (expected cash computed immediately from the ledger)

↓

One or more Cash Counts (Snapshots)

↓

Complete Reconciliation

↓

Adjustment Transaction created only if actual ≠ expected

↓

Completed

---

# Adjustment Transaction

Completing a reconciliation with a non-zero difference creates an `ADJUSTMENT` transaction (not an `EXPENSE`)
with `adjustment_reason = CASH_RECONCILIATION`, linked back via `adjustment_transaction_id`. Its direction follows
the same convention as every other adjustment in the ledger: a positive difference (extra cash found) posts to
the account as `to_account_id`; a negative difference (cash missing) posts as `from_account_id`.

Example

```
Expected Cash

6,250

Actual Cash

5,980

Difference

-270

↓

ADJUSTMENT Transaction

Reason

CASH_RECONCILIATION

Amount

270

Direction

from_account_id = the reconciled account (missing cash)
```

Original transactions remain unchanged.

---

# Reporting Usage

Used in

- Cash Dashboard
- Cash History
- Monthly Reports
- Reconciliation History
- Adjustment History

---

# Example

ATM Withdrawal

```
20,000
```

Recorded Expenses

```
12,000
```

Expected Cash (derived from the ledger)

```
8,000
```

Actual Cash (counted)

```
7,720
```

Difference

```
-280
```

↓

Adjustment Transaction

```
ADJUSTMENT

Reason

CASH_RECONCILIATION

Amount

280

Direction

from_account_id (missing cash)
```

---

# Future Enhancements

Possible additions

- Photo attachment
- Receipt attachment
- Location
- Automatic reminders
- AI anomaly detection
- Monthly reconciliation summary

---

# Design Decisions

- Users are not required to record every small cash expense.
- Cash differences are discovered during reconciliation.
- Reconciliation creates adjustment transactions.
- Financial history is never rewritten.
- Cash balance (expected_cash_amount) is derived from the ledger, not tracked separately — see
  `CalculateAccountBalanceService`.
- Physical cash is verified using one or more snapshots per session.
- Reconciliation is scoped per cash account.

---

# Final Statement

The Cash Management module provides a practical balance between accounting accuracy and user convenience.

Instead of requiring exhaustive transaction entry, the application allows users to reconcile cash periodically,
ensuring reliable financial records while minimizing day-to-day effort.
