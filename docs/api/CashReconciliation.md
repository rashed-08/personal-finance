# Cash Reconciliation API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for reconciling physical cash against the ledger. See
`docs/database/CashReconciliation.md` for how expected cash is calculated and
`docs/business/CashReconciliationWorkflow.md` for the underlying business rules.

Base path

```
/api/cash-reconciliations
```

All request and response bodies are JSON. Errors follow RFC 7807 Problem Details, same as the other APIs.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/cash-reconciliations` | Start a reconciliation session for a cash account |
| GET | `/api/cash-reconciliations?accountId=` | List reconciliation history, optionally filtered by account |
| GET | `/api/cash-reconciliations/{id}` | Get one reconciliation, including all recorded snapshots |
| POST | `/api/cash-reconciliations/{id}/snapshots` | Record a physical cash count |
| PATCH | `/api/cash-reconciliations/{id}/complete` | Finalize — creates an adjustment transaction if needed |

---

# POST /api/cash-reconciliations

Starts a new reconciliation session. `expected_cash_amount` is computed immediately from the ledger — the
account's full posted transaction history up to `reconciliationDate` — and never entered manually.

## Request Body

```json
{
  "accountId": "b1a2c3d4-...",
  "reconciliationDate": "2026-07-25",
  "notes": null
}
```

| Field | Type | Notes |
|-------|------|-------|
| `accountId` | UUID, required | Must be an active account of type `CASH` |
| `reconciliationDate` | date, required | Ledger activity up to and including this date is included |
| `notes` | string, optional | Carried through to the eventual adjustment transaction's notes |

## Response — `201 Created`

Returns the new reconciliation in `PENDING` status with `actualCashAmount`/`differenceAmount` both `null` and an
empty `snapshots` array (see [Response Shape](#response-shape)).

## Errors

| Status | Cause |
|--------|-------|
| 404 | Account does not exist |
| 400 | Account is not active, or is not a `CASH`-type account |
| 409 | This account already has a `PENDING` reconciliation |

---

# GET /api/cash-reconciliations

Lists reconciliation history. Pass `accountId` to scope to one account; omit it to list across all accounts.

---

# GET /api/cash-reconciliations/{id}

Returns one reconciliation with its full snapshot history.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Reconciliation does not exist |

---

# POST /api/cash-reconciliations/{id}/snapshots

Records a physical cash count. Can be called more than once per session (e.g. a morning and an evening count);
the most recent snapshot becomes the reconciliation's current `actualCashAmount`.

## Request Body

```json
{
  "cashAmount": 1940.00,
  "notes": "wallet count"
}
```

## Response — `201 Created`

Returns the reconciliation with the new snapshot appended and `actualCashAmount`/`differenceAmount` updated.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Reconciliation does not exist |
| 409 | Reconciliation is already `COMPLETED` |

---

# PATCH /api/cash-reconciliations/{id}/complete

Finalizes the session using the most recently recorded snapshot.

- If `actualCashAmount == expectedCashAmount`: marks the reconciliation `COMPLETED`. No adjustment transaction is
  created — a perfect reconciliation needs no explanation.
- Otherwise: creates one `ADJUSTMENT` transaction (`adjustmentReason: CASH_RECONCILIATION`) for the absolute
  difference, directed to increase the account if extra cash was found or decrease it if cash is missing, links
  it via `adjustmentTransactionId`, then marks the reconciliation `COMPLETED`.

## Response — `200 OK`

Returns the completed reconciliation. Check `adjustmentTransactionId` to see whether a correction was posted.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Reconciliation does not exist |
| 409 | Already `COMPLETED`, or no cash count has been recorded yet |

---

# Response Shape

```json
{
  "id": "e5f6a7b8-...",
  "accountId": "b1a2c3d4-...",
  "reconciliationDate": "2026-07-25",
  "expectedCashAmount": 2200.00,
  "actualCashAmount": 1940.00,
  "differenceAmount": -260.00,
  "status": "PENDING",
  "adjustmentTransactionId": null,
  "notes": null,
  "snapshots": [
    {
      "id": "f1a2b3c4-...",
      "cashAmount": 1940.00,
      "notes": "wallet count",
      "snapshotTime": "2026-07-25T18:30:00"
    }
  ],
  "createdAt": "2026-07-25T18:00:00",
  "updatedAt": "2026-07-25T18:30:00"
}
```

`actualCashAmount` and `differenceAmount` are `null` until at least one snapshot has been recorded.

---

# Final Statement

Cash reconciliation never edits or deletes a transaction. Its only side effect is, at most, one new `ADJUSTMENT`
transaction per session — history stays intact and every correction is traceable back to the reconciliation that
produced it.
