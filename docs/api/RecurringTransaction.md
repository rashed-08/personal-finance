# Recurring Transaction API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for recurring transaction templates — reusable rules that generate
ordinary `EXPENSE`/`INCOME`/`TRANSFER` transactions on a schedule (see
`docs/database/tables/recurring_transactions.md`).

A template never affects balances or reports directly; only the transactions it generates do. Version 1 has no
background scheduler — generation is always triggered on-demand, either in bulk (`/run-due`) or per template
(`/generate-now`). See [Generation](#generation) below.

Base path

```
/api/recurring-transactions
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/recurring-transactions` | Create a template |
| GET | `/api/recurring-transactions` | List templates (optionally active-only) |
| GET | `/api/recurring-transactions/due` | Live "due now" list (any active template, regardless of `autoGenerate`) |
| GET | `/api/recurring-transactions/{id}` | Get a single template |
| GET | `/api/recurring-transactions/{id}/executions` | Execution history for a template |
| PUT | `/api/recurring-transactions/{id}` | Update a template's scheduling/metadata |
| PATCH | `/api/recurring-transactions/{id}/activate` | Reactivate a template |
| PATCH | `/api/recurring-transactions/{id}/deactivate` | Stop future generation |
| POST | `/api/recurring-transactions/{id}/generate-now` | Manually generate the next due occurrence |
| POST | `/api/recurring-transactions/run-due` | Bulk-generate every due `autoGenerate` template |
| DELETE | `/api/recurring-transactions/{id}` | Delete a template (generated transactions are untouched) |

---

# POST /api/recurring-transactions

## Request Body

```json
{
  "name": "House Rent",
  "transactionType": "EXPENSE",
  "fromAccountId": "b1a2c3d4-...",
  "toAccountId": null,
  "categoryId": "c1a2b3c4-...",
  "amount": 10000.00,
  "frequency": "MONTHLY",
  "startDate": "2026-08-01",
  "endDate": null,
  "autoGenerate": true,
  "description": "Monthly rent",
  "notes": null
}
```

| Field | Type | Notes |
|-------|------|-------|
| `name` | string, required | Max 100 characters |
| `transactionType` | enum, required | `EXPENSE`, `INCOME`, or `TRANSFER` |
| `fromAccountId` | UUID, conditional | Required for `EXPENSE`/`TRANSFER` |
| `toAccountId` | UUID, conditional | Required for `INCOME`/`TRANSFER` |
| `categoryId` | UUID, conditional | Required for `EXPENSE`/`INCOME`, forbidden for `TRANSFER` |
| `amount` | decimal, required | Must be strictly positive (`> 0`); the amount generated each occurrence |
| `frequency` | enum, required | `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` |
| `startDate` | date, required | Also becomes `nextExecutionDate` initially, and the first occurrence's transaction date |
| `endDate` | date, optional | Occurrences past this date are no longer due |
| `autoGenerate` | boolean, default `false` | `true`: `/run-due` generates it automatically. `false`: requires `/generate-now` per occurrence |
| `description` | string, optional | Max 255 characters; copied onto every generated transaction |
| `notes` | string, optional | Copied onto every generated transaction |

## Cross-Aggregate Validation

- Any referenced account exists and is active.
- Any referenced category exists, is active, and its type matches (`INCOME`/`EXPENSE`).

## Response — `201 Created`

Returns the created template (see [Response Shape](#recurring-transaction-response-shape)).

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, non-positive amount, end date before start date, structural mismatch for the transaction type |
| 404 | Referenced account or category does not exist |

---

# GET /api/recurring-transactions

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `false` | When `true`, returns only active templates |

---

# GET /api/recurring-transactions/due

Live query — not a persisted list — of every active template whose `nextExecutionDate` has arrived, regardless
of `autoGenerate`. Templates with `autoGenerate = false` will keep appearing here until manually confirmed via
`/generate-now`.

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `asOfDate` | date, optional | Defaults to today |

---

# GET /api/recurring-transactions/{id}/executions

Returns the template's full execution history, most recent first (see
[Execution Response Shape](#execution-response-shape)).

---

# PUT /api/recurring-transactions/{id}

`transactionType`, accounts, and `categoryId` are fixed at creation — only scheduling/metadata can change.

## Request Body

```json
{
  "name": "House Rent",
  "amount": 10500.00,
  "frequency": "MONTHLY",
  "endDate": null,
  "autoGenerate": true,
  "description": "Monthly rent (increased)",
  "notes": null
}
```

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, non-positive amount, end date before start date |
| 404 | Template does not exist |

---

# PATCH /api/recurring-transactions/{id}/activate

Idempotent.

---

# PATCH /api/recurring-transactions/{id}/deactivate

Idempotent. Stops future generation only — already-generated transactions and execution history are untouched.

---

# Generation

## POST /api/recurring-transactions/{id}/generate-now

Generates exactly the template's next due occurrence, regardless of `autoGenerate` — this is the manual
confirmation action for `autoGenerate = false` templates, and can also be used to force an occurrence early.
The generated transaction is dated on the occurrence's scheduled date (`nextExecutionDate`), not today.

Internally resolves the salary cycle to whichever one is open at generation time. If none is open, or
transaction creation otherwise fails (e.g. the account was deactivated since the template was created), the
occurrence is recorded as `SKIPPED` with a reason instead of failing the request — the response still returns
`200 OK` with the template's advanced schedule.

### Errors

| Status | Cause |
|--------|-------|
| 404 | Template does not exist |
| 409 | Template is not active |

## POST /api/recurring-transactions/run-due

The bulk "Run due transactions" action. Processes every `autoGenerate = true` template whose `nextExecutionDate`
has arrived, generating **every** occurrence still due for each (not just one), so a template that's been due
for months catches up fully in a single call.

### Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `asOfDate` | date, optional | Defaults to today |

### Response — `200 OK`

Returns the list of processed templates with their advanced schedules (see
[Response Shape](#recurring-transaction-response-shape)). Check `/executions` per template for exactly which
occurrences were generated vs. skipped.

---

# DELETE /api/recurring-transactions/{id}

Deletes the template. Transactions it already generated, and its execution history, are unaffected.

---

# Recurring Transaction Response Shape

```json
{
  "id": "e5f6a7b8-...",
  "name": "House Rent",
  "transactionType": "EXPENSE",
  "fromAccountId": "b1a2c3d4-...",
  "toAccountId": null,
  "categoryId": "c1a2b3c4-...",
  "amount": 10000.00,
  "frequency": "MONTHLY",
  "startDate": "2026-08-01",
  "endDate": null,
  "nextExecutionDate": "2026-09-01",
  "lastExecutionDate": "2026-08-01",
  "autoGenerate": true,
  "active": true,
  "description": "Monthly rent",
  "notes": null,
  "createdAt": "2026-07-25T10:15:00",
  "updatedAt": "2026-08-01T09:00:00"
}
```

---

# Execution Response Shape

```json
{
  "id": "f1a2b3c4-...",
  "recurringTransactionId": "e5f6a7b8-...",
  "scheduledDate": "2026-08-01",
  "status": "GENERATED",
  "transactionId": "a1b2c3d4-...",
  "reason": null,
  "createdAt": "2026-08-01T09:00:00"
}
```

`transactionId` is set only when `status` is `GENERATED`; `reason` is set only when `status` is `SKIPPED`.

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Cannot generate an occurrence of an inactive template.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

| Title | Status | Thrown for |
|-------|--------|------------|
| Resource Not Found | 404 | Referenced template, account, or category does not exist |
| Validation Failed | 400 | Structural or business-rule validation failure |
| Request Validation Failed | 400 | Bean Validation (`@NotNull`, `@NotBlank`, ...) failure |
| Invalid State Transition | 409 | `/generate-now` on an inactive template |
| Internal Server Error | 500 | Unexpected failure |

---

# Final Statement

This document reflects the Recurring Transactions feature (GitHub issue #8) as implemented. Changes to endpoint
behavior should update this document alongside the code.
