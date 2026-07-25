# Fund API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for creating, reading, updating, and closing funds — logical reservations
of money toward a purpose (see `docs/business/FundWorkflow.md` and `docs/database/tables/ funds.md`).

A fund never stores a balance directly. Every response includes a `balance` field derived, at request time, from
posted `TRANSFER` transactions linked to the fund via `transactions.fund_id` (see
[Fund-Linked Transfers](#fund-linked-transfers) below and `docs/api/Transactions.md`).

Base path

```
/api/funds
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/funds` | Create a fund |
| GET | `/api/funds` | List funds (optionally active-only) |
| GET | `/api/funds/{id}` | Get a single fund with its derived balance |
| PUT | `/api/funds/{id}` | Update a fund's name, target, and description |
| PATCH | `/api/funds/{id}/activate` | Reopen a closed fund |
| PATCH | `/api/funds/{id}/deactivate` | Close a fund |

---

# POST /api/funds

## Request Body

```json
{
  "name": "New Laptop",
  "fundType": "GOAL",
  "targetAmount": 150000.00,
  "targetDate": "2027-01-01",
  "description": null
}
```

| Field | Type | Notes |
|-------|------|-------|
| `name` | string, required | Max 100 characters, must be unique |
| `fundType` | enum, required | `EMERGENCY`, `SAVINGS`, `GOAL`, `ZAKAT`, `INVESTMENT`, `CUSTOM` — no default |
| `targetAmount` | decimal, optional | Must be strictly positive (`> 0`) when present |
| `targetDate` | date, optional | Optional target completion date |
| `description` | string, optional | Max 500 characters |

## Response — `201 Created`

Returns the created fund (see [Fund Response Shape](#fund-response-shape)); `balance` is `0.00`.

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, missing fund type, non-positive target amount, name too long, or a fund with the same name already exists |

---

# GET /api/funds

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `false` | When `true`, returns only active funds |

## Response — `200 OK`

A list of [Fund Response Shape](#fund-response-shape) objects.

---

# GET /api/funds/{id}

Returns a single fund with its derived balance.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Fund does not exist |

---

# PUT /api/funds/{id}

Updates a fund's name, target amount, target date, and description. `fundType` cannot be changed after creation.

## Request Body

```json
{
  "name": "New Laptop",
  "targetAmount": 175000.00,
  "targetDate": "2027-06-01",
  "description": "Upgrading for video editing"
}
```

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 400 | Missing/blank name, non-positive target amount |
| 404 | Fund does not exist |

---

# PATCH /api/funds/{id}/activate

Reopens a closed fund. Idempotent.

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 404 | Fund does not exist |

---

# PATCH /api/funds/{id}/deactivate

Closes a fund. Per `docs/business/FundWorkflow.md`, a fund may only be closed while its derived balance is zero.

## Response — `200 OK`

## Errors

| Status | Cause |
|--------|-------|
| 404 | Fund does not exist |
| 409 | Fund's derived balance is not zero |

---

# Fund Response Shape

```json
{
  "id": "f1a2b3c4-...",
  "name": "New Laptop",
  "fundType": "GOAL",
  "targetAmount": 150000.00,
  "targetDate": "2027-01-01",
  "balance": 42000.00,
  "active": true,
  "description": null,
  "createdAt": "2026-07-25T10:15:00",
  "updatedAt": "2026-07-25T10:15:00"
}
```

`balance` is always `Total Allocations - Total Withdrawals` over posted transactions linked to this fund —
never a stored column.

---

# Fund-Linked Transfers

Money moves in and out of a fund through ordinary `POST /api/transactions` calls of type `TRANSFER`, with a
`fundId` set instead of a second account. See `docs/api/Transactions.md` for the full transaction request shape.

- **Allocation** (account → fund): set `fromAccountId` and `fundId`, leave `toAccountId` null.
- **Withdrawal** (fund → account): set `toAccountId` and `fundId`, leave `fromAccountId` null.

Exactly one of `fromAccountId` / `toAccountId` must be set alongside `fundId` — setting both or neither is
rejected. The referenced fund must exist and be active.

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Fund cannot be closed while it has a non-zero balance.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

| Title | Status | Thrown for |
|-------|--------|------------|
| Resource Not Found | 404 | Referenced fund does not exist |
| Validation Failed | 400 | Structural or business-rule validation failure, including a duplicate fund name |
| Request Validation Failed | 400 | Bean Validation (`@NotNull`, `@NotBlank`, ...) failure |
| Invalid State Transition | 409 | Closing a fund with a non-zero balance |
| Internal Server Error | 500 | Unexpected failure |

---

# Final Statement

This document reflects the Fund Management feature (GitHub issue #6) as implemented. Changes to endpoint
behavior should update this document alongside the code.
