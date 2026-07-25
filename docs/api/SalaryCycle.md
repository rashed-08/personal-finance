# Salary Cycle API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for managing salary cycles — reporting periods that begin on one salary
payment and end the day before the next, independent of calendar months. See
`docs/business/SalaryWorkflow.md` and `docs/business/CarryForwardWorkflow.md` for the underlying business rules.

Base path

```
/api/salary-cycles
```

All request and response bodies are JSON. Errors follow RFC 7807 Problem Details, same as the Transactions API.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/salary-cycles` | Manually create a salary cycle (e.g. backfilling historical data) |
| GET | `/api/salary-cycles` | List all salary cycles |
| GET | `/api/salary-cycles/current` | Get the cycle containing today's date |
| GET | `/api/salary-cycles/{id}` | Get a single salary cycle |
| PUT | `/api/salary-cycles/{id}` | Update name, salary date, and description |
| PATCH | `/api/salary-cycles/{id}/close` | Close an open cycle as of a given end date |
| PATCH | `/api/salary-cycles/{id}/reopen` | Reopen a closed cycle |
| GET | `/api/salary-cycles/{id}/carry-forward` | Calculate opening/closing balance and carry forward |

---

# Automatic Cycle Creation

Manual creation via `POST` exists for backfilling or importing historical cycles with an already-known date range.
In normal use, salary cycles are created **automatically**: setting `startsNewSalaryCycle: true` on an
`INCOME` transaction (see `docs/api/Transactions.md`) closes the currently open cycle the day before the new
transaction's date and opens a new one starting on it, per `docs/business/SalaryWorkflow.md` ("every salary
starts a new salary cycle"). The transaction's `salaryCycleId` is resolved server-side in that case — the client
does not supply one.

Only one salary cycle may be open (`endDate = null`) at a time. Creating or reopening a cycle while another is
already open is rejected.

---

# POST /api/salary-cycles

## Request Body

```json
{
  "name": "July 2026",
  "startDate": "2026-07-10",
  "endDate": "2026-08-09",
  "salaryDate": "2026-07-10",
  "description": null
}
```

| Field | Type | Notes |
|-------|------|-------|
| `name` | string, required | Must be unique |
| `startDate` | date, required | |
| `endDate` | date, optional | Omit or send `null` to create an ongoing (open) cycle |
| `salaryDate` | date, required | Between 2000-01-01 and 10 years from today |
| `description` | string, optional | |

## Response — `201 Created`

Returns the created cycle (see [Salary Cycle Response Shape](#salary-cycle-response-shape)).

## Errors

| Status | Cause |
|--------|-------|
| 400 | Name already exists; `endDate` before `startDate`; another cycle is already open and this one has no `endDate` |

---

# GET /api/salary-cycles

Returns all salary cycles as a plain list (no pagination — cycle counts are small by nature).

# GET /api/salary-cycles/current

Returns the cycle whose `[startDate, endDate]` range contains today. An open cycle (`endDate = null`) always
contains today and every future date.

## Errors

| Status | Cause |
|--------|-------|
| 404 | No cycle covers today's date |

# GET /api/salary-cycles/{id}

## Errors

| Status | Cause |
|--------|-------|
| 404 | Cycle does not exist |

---

# PUT /api/salary-cycles/{id}

Updates `name`, `salaryDate`, and `description`. `startDate`/`endDate` are not editable here — they change only
through close/reopen, since they define the cycle's identity relative to the ledger.

## Request Body

```json
{
  "name": "July 2026",
  "salaryDate": "2026-07-10",
  "description": "Late by a few days"
}
```

## Errors

| Status | Cause |
|--------|-------|
| 404 | Cycle does not exist |

---

# PATCH /api/salary-cycles/{id}/close

## Request Body

```json
{ "endDate": "2026-08-09" }
```

## Errors

| Status | Cause |
|--------|-------|
| 404 | Cycle does not exist |
| 409 | Cycle is already closed |
| 400 | `endDate` is before the cycle's `startDate` |

---

# PATCH /api/salary-cycles/{id}/reopen

Clears the cycle's end date, making it ongoing again.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Cycle does not exist |
| 409 | Cycle is already open |
| 400 | Another cycle is already open |

---

# GET /api/salary-cycles/{id}/carry-forward

Calculates, but never stores, this cycle's opening balance, income, expenses, adjustments, and closing balance —
per `docs/business/CarryForwardWorkflow.md`:

```
Closing Balance = Opening Balance + Income - Expenses ± Adjustments
```

- **Opening Balance** is the previous cycle's Closing Balance, computed recursively. For the very first cycle
  (no predecessor), it is the net of all posted, non-transfer ledger activity dated before the cycle's start —
  this is how account opening balances and migrated history flow in without a special case.
- **Transfers** are excluded — they move money between accounts without changing net worth.
- **Adjustments** are signed by direction: increasing (`toAccountId` set) add, decreasing (`fromAccountId` set)
  subtract.
- **Funds, Loans, and Cash Reconciliation** are not yet reflected — those modules don't exist yet. Once they do,
  they participate as ordinary ledger transactions and this calculation picks them up automatically; no change
  to this endpoint should be needed.

## Response — `200 OK`

```json
{
  "salaryCycleId": "d1a2b3c4-...",
  "openingBalance": 5000.00,
  "income": 80000.00,
  "expenses": 62000.00,
  "adjustments": -250.00,
  "closingBalance": 22750.00
}
```

## Errors

| Status | Cause |
|--------|-------|
| 404 | Cycle does not exist |

---

# Salary Cycle Response Shape

```json
{
  "id": "d1a2b3c4-...",
  "name": "July 2026",
  "startDate": "2026-07-10",
  "endDate": null,
  "salaryDate": "2026-07-10",
  "closed": false,
  "description": null,
  "createdAt": "2026-07-10T09:00:00",
  "updatedAt": "2026-07-10T09:00:00"
}
```

`endDate` is `null` exactly when `closed` is `false`.

---

# Final Statement

Salary cycles are reporting periods, not owners of money — every figure they expose is derived from the
transaction ledger. This document should be updated alongside `docs/business/SalaryWorkflow.md` and
`docs/business/CarryForwardWorkflow.md` if the underlying rules change.
