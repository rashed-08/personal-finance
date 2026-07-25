# Cash Reconciliation

Version: 1.0

Status: Draft

Owner: Personal Finance App

Related Documents

- `docs/business/CashReconciliationWorkflow.md` — the business rules this implements
- `docs/database/tables/cash.md` — full table specification for `cash_reconciliations` / `cash_snapshots`
- `docs/api/CashReconciliation.md` — REST API

---

# Purpose

This document explains how Cash Reconciliation is actually implemented — in particular, how "expected cash" is
calculated — since it deliberately differs from the literal formula in `CashReconciliationWorkflow.md`, and
explains why the two are equivalent in practice.

---

# Expected Cash: Full-Ledger Derivation, Not a Rolling Window

`CashReconciliationWorkflow.md` describes Expected Cash as a windowed formula: opening cash (from the previous
reconciliation) plus withdrawals, income, and adjustments since then, minus expenses and transfers out.

The implementation instead computes Expected Cash by summing **every** posted transaction that has ever touched
the account, up to and including the reconciliation date — via `CalculateAccountBalanceService`, reusing
`Transaction.signedAmountFor(accountId)` to get each transaction's signed contribution to that specific account
(positive if the account was the destination, negative if it was the source; this works uniformly for income,
expenses, transfers, and adjustments, unlike the salary-cycle carry-forward calculation, which excludes
transfers entirely because a salary cycle isn't a single account).

These two approaches produce the same number as long as every previous reconciliation actually closed the gap
between expected and actual by creating an adjustment transaction — which is exactly what
`CompleteReconciliationService` does. Once that adjustment is posted, it becomes part of the ledger like any
other transaction, so summing from the beginning of time already includes it; there is no need to separately
track "the opening balance since the last reconciliation."

This is a deliberate simplification, not an oversight:

- It requires no separate "opening cash" bookkeeping — the ledger is self-describing.
- It is robust to retroactive entries. If a transaction gets entered late, dated before the last reconciliation,
  a windowed formula anchored to "the last reconciliation's actual count" would need to be re-derived by hand;
  full-ledger summation just reflects it automatically the next time it runs.
- It matches the Constitution's own principle that balances must always be reproducible from the transaction
  history, not from a chain of incremental snapshots.

The trade-off is performance at very large transaction volumes, since every reconciliation re-sums the account's
entire history rather than a bounded window. This is an accepted cost for now — see
`docs/database/tables/transactions/09-future-design.md` for the project's general stance on deferring performance
work (materialized views, read replicas) until volume actually requires it.

---

# Reconciliation Lifecycle

```
Account (must be CASH type, active)

↓

StartReconciliationService
    - rejects if the account already has a PENDING reconciliation
    - computes expected_cash_amount via CalculateAccountBalanceService
    - creates a PENDING CashReconciliation with no snapshots yet

↓

RecordCashSnapshotService (repeatable)
    - appends a CashSnapshot
    - the latest snapshot becomes actual_cash_amount
    - difference_amount = actual_cash_amount - expected_cash_amount

↓

CompleteReconciliationService
    - requires at least one snapshot
    - if difference_amount = 0: marks COMPLETED, no adjustment created
    - if difference_amount ≠ 0: creates an ADJUSTMENT transaction
      (reason CASH_RECONCILIATION, direction per the difference's sign),
      links it via adjustment_transaction_id, marks COMPLETED

↓

Immutable history — a COMPLETED reconciliation rejects further snapshots
and cannot be completed again.
```

---

# Sign Convention

`difference_amount = actual_cash_amount − expected_cash_amount`. This matches this table's own constraint
section, not the subtraction order written in `CashReconciliationWorkflow.md`'s prose (which frames it as
expected − actual) — the two documents disagreed with each other before this was implemented; this document is
the authoritative one going forward.

- Positive difference → unexpected extra cash → the adjustment increases the account (`to_account_id`).
- Negative difference → cash is missing → the adjustment decreases the account (`from_account_id`).
- Zero difference → perfect reconciliation → no adjustment transaction is created at all.

---

# Classification

`CashReconciliationWorkflow.md` describes a menu of user-facing classifications for a difference (untracked
expense, transfer, loan, adjustment, other). Only `AdjustmentReason.CASH_RECONCILIATION` exists as a structured
reason today; the reconciliation's free-text `notes` field carries the user's explanation. Expanding
`AdjustmentReason` into a fuller classification menu is a future enhancement, not required for this to function
correctly — every adjustment is still traceable back to its reconciliation via `adjustment_transaction_id`.

---

# Final Statement

Cash Reconciliation treats the ledger as the single source of truth for "expected cash," and treats a physical
count as the single source of truth for "actual cash." The only artifact reconciliation ever produces is an
ordinary `ADJUSTMENT` transaction — no balances are stored, and no historical transaction is ever modified.
