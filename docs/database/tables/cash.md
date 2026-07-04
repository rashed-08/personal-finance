# Cash Management Table Specification

## Purpose

The Cash Management module supports reconciliation of physical cash with recorded financial transactions.

Users are not expected to record every small cash expense.

Instead, the application periodically reconciles physical cash against calculated cash balance and automatically identifies untracked differences.

This workflow significantly reduces manual data entry while maintaining accurate financial records.

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

Cash Count

↓

Reconciliation

↓

Adjustment Transaction
```

---

# Table

cash_reconciliations

Purpose

Represents one reconciliation session.

Each reconciliation compares:

Expected Cash

vs

Actual Cash

and optionally creates adjustment transactions.

---

## Columns

| Column | Type | Nullable | Default | Description |
|---------|------|----------|---------|-------------|
| id | UUID | No | Generated | Primary Key |
| reconciliation_date | DATE | No | | Date of reconciliation |
| expected_cash | NUMERIC(18,2) | No | | Calculated cash |
| actual_cash | NUMERIC(18,2) | No | | User counted cash |
| difference_amount | NUMERIC(18,2) | No | | Calculated difference |
| status | VARCHAR(20) | No | PENDING | PENDING / COMPLETED |
| notes | VARCHAR(1000) | Yes | | User notes |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Last update |

---

## Primary Key

id

UUID

---

## Constraints

difference_amount

Automatically calculated

```
Actual Cash

-

Expected Cash
```

---

status

Allowed values

- PENDING
- COMPLETED

---

## Business Rules

- Reconciliation is immutable after completion.
- A completed reconciliation cannot be edited.
- Every reconciliation belongs to a specific reporting period.
- Adjustment transactions are optional.
- Difference may be positive or negative.

---

# Table

cash_snapshots

Purpose

Stores every physical cash count performed by the user.

Snapshots preserve historical cash balances.

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

---

## Foreign Key

reconciliation_id

↓

cash_reconciliations(id)

---

# Lifecycle

Cash Withdrawal

↓

Cash Spending

↓

Periodic Cash Count

↓

Snapshot

↓

Reconciliation

↓

Optional Adjustment Transaction

↓

Completed

---

# Adjustment Transaction

Version 1 creates a normal transaction when the user approves reconciliation.

Example

```
Expected Cash

6,250

Actual Cash

5,980

Difference

-270

↓

Expense Transaction

Category

Miscellaneous Adjustment

Amount

270
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

Expected Cash

```
8,000
```

Actual Cash

```
7,720
```

Difference

```
280
```

↓

Adjustment Transaction

```
Expense

Category

Miscellaneous Adjustment

Amount

280
```

---

# Future Enhancements

Possible additions

- Multiple snapshots per day
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
- Cash balance is derived from transactions.
- Physical cash is verified using snapshots.

---

# Final Statement

The Cash Management module provides a practical balance between accounting accuracy and user convenience.

Instead of requiring exhaustive transaction entry, the application allows users to reconcile cash periodically, ensuring reliable financial records while minimizing day-to-day effort.