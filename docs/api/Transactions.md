# Transactions API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for creating, reading, updating and closing out ledger transactions.

Transactions are the single source of truth for the application. Every endpoint here ultimately reads from or
writes to the `transactions` table described in `docs/database/tables/transactions/`.

Base path

```
/api/transactions
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/transactions` | Create a transaction |
| GET | `/api/transactions` | List/filter transactions (paginated) |
| GET | `/api/transactions/{id}` | Get a single transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| PATCH | `/api/transactions/{id}/void` | Void a transaction |
| PATCH | `/api/transactions/{id}/reverse` | Reverse a transaction |

---

# POST /api/transactions

Creates a new transaction. The transaction is always created as `POSTED` — Version 1 has no draft state.

## Request Body

```json
{
  "transactionType": "EXPENSE",
  "transactionDate": "2026-07-25",
  "amount": 2300.00,
  "fromAccountId": "b1a2c3d4-...",
  "toAccountId": null,
  "categoryId": "c1a2b3c4-...",
  "salaryCycleId": "d1a2b3c4-...",
  "description": "Groceries",
  "notes": null,
  "adjustmentReason": null,
  "migrationBatchId": null,
  "referenceTransactionId": null,
  "fundId": null,
  "startsNewSalaryCycle": false
}
```

| Field | Type | Notes |
|-------|------|-------|
| `transactionType` | enum, required | `INCOME`, `EXPENSE`, `TRANSFER`, `ADJUSTMENT`, `OPENING_BALANCE`, `MIGRATION` |
| `transactionDate` | date, required | Effective financial date used for reporting |
| `amount` | decimal, required | Must be strictly positive (`> 0`) |
| `fromAccountId` | UUID, conditional | Source account; see type table below |
| `toAccountId` | UUID, conditional | Destination account; see type table below |
| `categoryId` | UUID, conditional | Required for INCOME/EXPENSE, forbidden for TRANSFER |
| `salaryCycleId` | UUID, conditional | Required for INCOME/EXPENSE/TRANSFER unless `startsNewSalaryCycle` is true |
| `description` | string, optional | Short human-readable label |
| `notes` | string, optional | Required when `adjustmentReason` is `MANUAL_CORRECTION` |
| `adjustmentReason` | enum, conditional | Required for ADJUSTMENT |
| `migrationBatchId` | string, conditional | Required for MIGRATION |
| `referenceTransactionId` | UUID, optional | For ADJUSTMENT, the transaction being corrected (if any) |
| `fundId` | UUID, conditional | TRANSFER only. See [Fund-Linked Transfers](#fund-linked-transfers) |
| `startsNewSalaryCycle` | boolean, optional, default `false` | INCOME only. See [Automatic Salary Cycle Creation](#automatic-salary-cycle-creation) |

## Required Fields by Transaction Type

| Type | Required | Forbidden |
|------|----------|-----------|
| INCOME | `toAccountId`, `categoryId` (income category), `salaryCycleId` (unless `startsNewSalaryCycle`) | `fromAccountId` |
| EXPENSE | `fromAccountId`, `categoryId` (expense category), `salaryCycleId` | `toAccountId` |
| TRANSFER (ordinary) | `fromAccountId`, `toAccountId` (must differ), `salaryCycleId` | `categoryId`, `fundId` |
| TRANSFER (fund-linked) | exactly one of `fromAccountId`/`toAccountId`, `fundId`, `salaryCycleId` | `categoryId` |
| ADJUSTMENT | `adjustmentReason` | not both `fromAccountId` and `toAccountId` at once |
| OPENING_BALANCE | `toAccountId` | Only one per account, ever |
| MIGRATION | `toAccountId`, `migrationBatchId` | — |

## Fund-Linked Transfers

Setting `fundId` on a `TRANSFER` turns it into a fund allocation or withdrawal instead of an ordinary
account-to-account transfer — see `docs/api/Fund.md` and `docs/business/FundWorkflow.md`. Exactly one of
`fromAccountId` / `toAccountId` must be set alongside `fundId`:

- Allocation (account → fund): set `fromAccountId`, leave `toAccountId` null.
- Withdrawal (fund → account): set `toAccountId`, leave `fromAccountId` null.

The referenced fund must exist and be active.

## Automatic Salary Cycle Creation

Setting `startsNewSalaryCycle: true` on an `INCOME` transaction closes the currently open salary cycle (if any)
the day before `transactionDate` and opens a new one starting on it, per `docs/business/SalaryWorkflow.md`
("every salary starts a new salary cycle"). `salaryCycleId` is ignored in this case — the server resolves it and
uses the new cycle's id. Setting this flag on any other transaction type is rejected. See
`docs/api/SalaryCycle.md` for the full cycle lifecycle.

## Cross-Aggregate Validation

Beyond structural checks, the server verifies:

- Any referenced account exists and is active.
- Any referenced category exists, is active, and its type matches the transaction (an INCOME category for
  INCOME, an EXPENSE category for EXPENSE).
- Any referenced salary cycle exists.
- Any referenced fund exists and is active.
- An account may have at most one `OPENING_BALANCE` transaction.
- A `MANUAL_CORRECTION` adjustment must include non-blank `notes`.

## Response — `201 Created`

Returns the created transaction (see [Transaction Response Shape](#transaction-response-shape)).

## Errors

| Status | Cause |
|--------|-------|
| 400 | Structural validation failed (missing/forbidden field, non-positive amount, invalid transfer, duplicate opening balance, inactive account/category/fund, category type mismatch) |
| 404 | Referenced account, category, salary cycle, or fund does not exist |

---

# GET /api/transactions

Lists transactions, filtered and paginated.

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `fromDate` | date | Inclusive lower bound on `transactionDate` |
| `toDate` | date | Inclusive upper bound on `transactionDate` |
| `transactionType` | enum | Exact match |
| `transactionStatus` | enum | Exact match |
| `accountId` | UUID | Matches either `fromAccountId` or `toAccountId` |
| `categoryId` | UUID | Exact match |
| `salaryCycleId` | UUID | Exact match |
| `fundId` | UUID | Exact match |
| `page`, `size`, `sort` | standard Spring pagination | Defaults to `size=20`, sorted by `transactionDate` descending |

## Response — `200 OK`

A Spring `Page` of [Transaction Response Shape](#transaction-response-shape) objects.

---

# GET /api/transactions/{id}

Returns a single transaction by id.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Transaction does not exist |

---

# PUT /api/transactions/{id}

Updates a transaction's description, category and notes, and — separately — records any amount correction as a
new `ADJUSTMENT` transaction rather than rewriting history.

## Request Body

```json
{
  "transactionDate": "2026-07-25",
  "amount": 2500.00,
  "categoryId": "c1a2b3c4-...",
  "description": "Groceries (corrected)",
  "notes": "Receipt included a delivery fee"
}
```

`amount` is the transaction's *corrected* amount, not a delta. If it differs from the stored amount, the server
creates a second, linked `ADJUSTMENT` transaction (`adjustmentReason = TRANSACTION_UPDATE`) for the difference,
signed so it correctly increases or decreases the affected account depending on whether the original transaction
was an increasing or decreasing entry. The original transaction's own `amount` field is never rewritten.

## Response — `200 OK`

Returns the transaction with its updated description/category/notes (not the generated adjustment).

## Errors

| Status | Cause |
|--------|-------|
| 400 | Clearing a required category on an INCOME/EXPENSE transaction |
| 404 | Transaction does not exist |
| 409 | Transaction is VOID or REVERSED; or the amount changed on a TRANSFER (transfers cannot be corrected in place — void and re-record instead) |

---

# PATCH /api/transactions/{id}/void

Marks a transaction as `VOID`. Idempotent — voiding an already-void transaction returns it unchanged.

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 404 | Transaction does not exist |
| 409 | Transaction is already `REVERSED` |

---

# PATCH /api/transactions/{id}/reverse

Marks a transaction as `REVERSED`. Idempotent — reversing an already-reversed transaction returns it unchanged.

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 404 | Transaction does not exist |
| 409 | Transaction is already `VOID` |

---

# Transaction Response Shape

```json
{
  "id": "e5f6a7b8-...",
  "transactionType": "EXPENSE",
  "transactionStatus": "POSTED",
  "transactionDate": "2026-07-25",
  "amount": 2300.00,
  "fromAccountId": "b1a2c3d4-...",
  "toAccountId": null,
  "categoryId": "c1a2b3c4-...",
  "salaryCycleId": "d1a2b3c4-...",
  "referenceNumber": null,
  "fundId": null,
  "adjustmentReason": null,
  "description": "Groceries",
  "notes": null,
  "createdAt": "2026-07-25T10:15:00",
  "updatedAt": "2026-07-25T10:15:00"
}
```

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Expense requires categoryId.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

| Title | Status | Thrown for |
|-------|--------|------------|
| Resource Not Found | 404 | Referenced transaction, account, category, salary cycle, or fund does not exist |
| Validation Failed | 400 | Structural or business-rule validation failure |
| Request Validation Failed | 400 | Bean Validation (`@NotNull`, `@Positive`, ...) failure |
| Invalid State Transition | 409 | An operation is not legal for the transaction's current status |
| Internal Server Error | 500 | Unexpected failure |

---

# Business Rules Enforced by This API

These mirror `docs/domain/FinancialAccountingModel.md` and
`docs/database/tables/transactions/03-business-rules.md`:

- Transaction history is immutable. Corrections use `ADJUSTMENT` transactions, never in-place amount edits.
- Amounts are always positive; direction is determined by transaction type and which of `fromAccountId` /
  `toAccountId` is populated.
- Transfers are excluded from income/expense reporting and never carry a category.
- A fund-linked transfer (`fundId` set) touches exactly one real account; an ordinary transfer touches two.
  See `docs/api/Fund.md`.
- Only `POSTED` transactions are reported; `VOID` and `REVERSED` transactions are excluded.
- An account may have at most one `OPENING_BALANCE` transaction.
- A `MIGRATION` transaction always references a `migrationBatchId`.

---

# Final Statement

This document reflects the Transaction Management feature (`docs/requirements/ProductBacklog.md` v0.4) as
implemented. Changes to endpoint behavior should update this document alongside the code.
