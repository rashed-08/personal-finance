# Loan API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for creating, reading, updating, repaying, and closing loans — money lent
to another person (`RECEIVABLE`) or borrowed from another person (`PAYABLE`), tracked independently from income
and expenses (see `docs/business/LoanWorkflow.md` and `docs/database/tables/loans.md`).

A loan never stores an outstanding balance directly. Every response includes an `outstandingBalance` field
derived, at request time, from posted `TRANSFER` transactions linked to the loan via `transactions.loan_id` (see
[Loan-Linked Transfers](#loan-linked-transfers) below).

Base path

```
/api/loans
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/loans` | Create a loan (also posts the disbursement/receipt transaction) |
| GET | `/api/loans` | List loans (optionally active-only) |
| GET | `/api/loans/{id}` | Get a single loan with its outstanding balance |
| PUT | `/api/loans/{id}` | Update a loan's name, due date, and description |
| PATCH | `/api/loans/{id}/repay` | Record a repayment or collection |
| PATCH | `/api/loans/{id}/close` | Close a loan |

---

# POST /api/loans

Creating a loan immediately posts its disbursement/receipt as a loan-linked ledger transaction — per
`docs/business/LoanWorkflow.md`'s worked example, giving or receiving a loan changes the account's available
balance right away, it is not a separate step.

## Request Body

```json
{
  "name": "Rahim",
  "loanType": "RECEIVABLE",
  "principalAmount": 10000.00,
  "startDate": "2026-07-25",
  "dueDate": null,
  "accountId": "b1a2c3d4-...",
  "salaryCycleId": "d1a2b3c4-...",
  "description": "Emergency loan to a friend"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `name` | string, required | Person or organization, max 150 characters |
| `loanType` | enum, required | `RECEIVABLE` (money lent out) or `PAYABLE` (money borrowed) |
| `principalAmount` | decimal, required | Must be strictly positive (`> 0`) |
| `startDate` | date, required | Also becomes the disbursement/receipt transaction's date |
| `dueDate` | date, optional | Optional target settlement date; cannot be in the past |
| `accountId` | UUID, required | The real account the money leaves (`RECEIVABLE`) or enters (`PAYABLE`) |
| `salaryCycleId` | UUID, required | Salary cycle the disbursement/receipt belongs to |
| `description` | string, optional | Max 1000 characters |

## Cross-Aggregate Validation

- The referenced account exists and is active.
- The referenced salary cycle exists.

## Response — `201 Created`

Returns the created loan (see [Loan Response Shape](#loan-response-shape)); `outstandingBalance` equals
`principalAmount`.

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, non-positive principal amount, due date in the past |
| 404 | Referenced account or salary cycle does not exist |

---

# GET /api/loans

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `false` | When `true`, returns only active loans |

## Response — `200 OK`

A list of [Loan Response Shape](#loan-response-shape) objects.

---

# GET /api/loans/{id}

Returns a single loan with its outstanding balance.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Loan does not exist |

---

# PUT /api/loans/{id}

Updates a loan's name, due date, and description. `loanType` and `principalAmount` cannot be changed after
creation — void the disbursement transaction and record a new loan instead.

## Request Body

```json
{
  "name": "Rahim",
  "dueDate": "2027-01-01",
  "description": "Emergency loan, extended due date"
}
```

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, due date in the past |
| 404 | Loan does not exist |

---

# PATCH /api/loans/{id}/repay

Records a repayment (`PAYABLE` loan) or collection (`RECEIVABLE` loan) as a loan-linked ledger transaction.

## Request Body

```json
{
  "accountId": "b1a2c3d4-...",
  "amount": 3000.00,
  "paymentDate": "2026-08-10",
  "salaryCycleId": "d1a2b3c4-...",
  "description": "First repayment"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `accountId` | UUID, required | The real account the money enters (`RECEIVABLE` collection) or leaves (`PAYABLE` repayment) |
| `amount` | decimal, required | Must be strictly positive and cannot exceed the current outstanding balance |
| `paymentDate` | date, required | |
| `salaryCycleId` | UUID, required | |
| `description` | string, optional | Max 255 characters |

## Response — `200 OK`

Returns the loan with its updated `outstandingBalance`.

## Errors

| Status | Cause |
|--------|-------|
| 400 | Non-positive amount, or amount exceeds the outstanding balance |
| 404 | Loan, referenced account, or salary cycle does not exist |
| 409 | Loan is not `ACTIVE` (already `CLOSED` or `CANCELLED`) |

---

# PATCH /api/loans/{id}/close

Closes a loan. Per `docs/business/LoanWorkflow.md`, a loan may only be closed while its outstanding balance is
zero.

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 404 | Loan does not exist |
| 409 | Loan's outstanding balance is not zero |

---

# Loan Response Shape

```json
{
  "id": "f1a2b3c4-...",
  "name": "Rahim",
  "loanType": "RECEIVABLE",
  "principalAmount": 10000.00,
  "startDate": "2026-07-25",
  "dueDate": null,
  "outstandingBalance": 7000.00,
  "loanStatus": "ACTIVE",
  "description": "Emergency loan to a friend",
  "createdAt": "2026-07-25T10:15:00",
  "updatedAt": "2026-08-10T09:00:00"
}
```

`outstandingBalance` is always `principalAmount - Total Repayments` over posted transactions linked to this
loan — never a stored column.

---

# Loan-Linked Transfers

Loan disbursement/receipt and repayment/collection are recorded as ordinary ledger `TRANSFER` transactions with
`loanId` set instead of two accounts — the same shape as Fund-linked transfers (see `docs/api/Fund.md`). Exactly
one of `fromAccountId` / `toAccountId` is set, the loan occupies the other side:

- Receivable disbursement / Payable repayment (account → loan): `fromAccountId` set, `toAccountId` null.
- Payable receipt / Receivable collection (loan → account): `toAccountId` set, `fromAccountId` null.

Unlike Fund-linked transfers, loan-linked transactions are only ever created through the dedicated `POST
/api/loans` and `PATCH /api/loans/{id}/repay` endpoints above — `loanId` is not a settable field on the generic
`POST /api/transactions` request. `GET /api/transactions?loanId=...` can still be used to retrieve a loan's full
transaction history (see `docs/api/Transactions.md`).

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Loan cannot be closed while it has a non-zero outstanding balance.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

| Title | Status | Thrown for |
|-------|--------|------------|
| Resource Not Found | 404 | Referenced loan, account, or salary cycle does not exist |
| Validation Failed | 400 | Structural or business-rule validation failure, including a repayment exceeding the outstanding balance |
| Request Validation Failed | 400 | Bean Validation (`@NotNull`, `@NotBlank`, ...) failure |
| Invalid State Transition | 409 | Repaying a non-active loan; closing a loan with a non-zero outstanding balance |
| Internal Server Error | 500 | Unexpected failure |

---

# Final Statement

This document reflects the Loan Management feature (GitHub issue #7) as implemented. Changes to endpoint
behavior should update this document alongside the code.
