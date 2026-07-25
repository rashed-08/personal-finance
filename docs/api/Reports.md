# Reports API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for reports and the dashboard — read-only views assembled entirely from
live ledger derivation (see `docs/database/tables/transactions/07-reporting.md` and
`docs/requirements/FunctionalRequirements.md` FR-011/FR-012).

No report stores a balance, total, or aggregate. Every figure returned is computed at request time from posted
`transactions` rows (plus, where relevant, the domain services already used elsewhere in the API —
`CalculateAccountBalanceService`, `CalculateFundBalanceService`, `CalculateLoanBalanceService`,
`CalculateCarryForwardService`). If a number can't be traced back to one or more transactions, it doesn't belong
in a report.

Base path

```
/api/reports
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`. Every endpoint is `GET` — reports never
mutate state.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/reports/dashboard` | Home-screen summary: balances, loan position, month figures, recent activity |
| GET | `/api/reports/income` | Income breakdown by category and date, with filters |
| GET | `/api/reports/expense` | Expense breakdown by category and date, with filters |
| GET | `/api/reports/category` | Single category's spending history and monthly trend |
| GET | `/api/reports/accounts/balances` | Every account's derived balance as of a date |
| GET | `/api/reports/accounts/{id}/statement` | One account's transaction history with a running balance |
| GET | `/api/reports/funds` | Every fund's allocated/used/remaining/progress |
| GET | `/api/reports/funds/{id}` | Single fund's report line |
| GET | `/api/reports/loans` | Every loan's principal/paid/remaining and payment history |
| GET | `/api/reports/loans/{id}` | Single loan's report line |
| GET | `/api/reports/salary-cycles/{id}` | One salary cycle's carry-forward breakdown |
| GET | `/api/reports/monthly` | One calendar month's income/expense digest vs. the previous month |
| GET | `/api/reports/cash-flow` | Money in / out / net over an arbitrary date range |
| GET | `/api/reports/cash-reconciliation` | Cash reconciliation history, filtered by account or date range |
| GET | `/api/reports/recurring-transactions` | Recurring transaction templates with generated/skipped execution counts |

---

# GET /api/reports/dashboard

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `asOfDate` | date, optional | Defaults to today. Balances and the loan summary are as of this date; income/expense figures cover the calendar month containing it |

## Response — `200 OK`

```json
{
  "totalBalance": 125000.00,
  "cashBalance": 5000.00,
  "totalFundBalance": 42000.00,
  "loanSummary": {
    "totalReceivable": 10000.00,
    "totalPayable": 3000.00,
    "netPosition": 7000.00,
    "activeLoanCount": 2
  },
  "monthlyIncome": 60000.00,
  "monthlyExpense": 38000.00,
  "recentTransactions": [ "...see docs/api/Transactions.md..." ],
  "dueRecurringTransactions": [ "...see docs/api/RecurringTransaction.md..." ],
  "topSpendingCategories": [
    { "categoryId": "c1a2b3c4-...", "categoryName": "Groceries", "totalSpent": 12000.00 }
  ]
}
```

| Field | Notes |
|-------|-------|
| `totalBalance` | Sum of every active account's derived balance as of `asOfDate` |
| `cashBalance` | Same, restricted to `CASH`-type accounts |
| `totalFundBalance` | Sum of every active fund's derived balance (allocations − withdrawals) |
| `loanSummary` | See [Loan Summary Shape](#loan-summary-shape) |
| `monthlyIncome` / `monthlyExpense` | Sum of posted `INCOME` / `EXPENSE` transactions for the calendar month containing `asOfDate` |
| `recentTransactions` | Up to 10 most recent posted transactions, newest first — see `docs/api/Transactions.md` |
| `dueRecurringTransactions` | Recurring transaction templates due on or before `asOfDate` — see `docs/api/RecurringTransaction.md` |
| `topSpendingCategories` | Up to 5 expense categories for the month, sorted by amount descending |

---

# GET /api/reports/income

# GET /api/reports/expense

Identical shape and query parameters; `/income` filters to `INCOME` transactions, `/expense` to `EXPENSE`.

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `fromDate` | date, optional | Inclusive lower bound |
| `toDate` | date, optional | Inclusive upper bound |
| `salaryCycleId` | UUID, optional | Restrict to one salary cycle |
| `accountId` | UUID, optional | Restrict to one account |
| `categoryId` | UUID, optional | Restrict to one category |

## Response — `200 OK`

```json
{
  "transactionType": "EXPENSE",
  "total": 38000.00,
  "transactionCount": 42,
  "byCategory": [
    { "categoryId": "c1a2b3c4-...", "categoryName": "Groceries", "total": 12000.00, "transactionCount": 15 }
  ],
  "byDate": [
    { "date": "2026-07-01", "total": 1500.00 }
  ]
}
```

Both `byCategory` and `byDate` cover every matching transaction — `byCategory` groups by `categoryId`,
`byDate` groups by `transactionDate`.

---

# GET /api/reports/category

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `categoryId` | UUID, required | Category to report on |
| `fromDate` | date, optional | Inclusive lower bound |
| `toDate` | date, optional | Inclusive upper bound |

## Response — `200 OK`

```json
{
  "categoryId": "c1a2b3c4-...",
  "categoryName": "Groceries",
  "totalSpending": 36000.00,
  "transactionCount": 24,
  "monthlyTrend": [
    { "yearMonth": "2026-06", "total": 18000.00 },
    { "yearMonth": "2026-07", "total": 18000.00 }
  ],
  "averagePerMonth": 18000.00
}
```

`monthlyTrend` groups every matching transaction by calendar month (`YearMonth`), ordered chronologically.
`averagePerMonth` is `totalSpending` divided by the number of distinct months present in `monthlyTrend` —
months with no transactions are not counted as zero.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Category does not exist |

---

# GET /api/reports/accounts/balances

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `asOfDate` | date, optional | Defaults to today |
| `activeOnly` | boolean, default `true` | When `true`, only active accounts are included |

## Response — `200 OK`

```json
[
  { "accountId": "a1b2c3d4-...", "accountName": "Cash", "accountType": "CASH", "balance": 5000.00 }
]
```

---

# GET /api/reports/accounts/{id}/statement

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `fromDate` | date, optional | Inclusive lower bound. When omitted, the statement starts at the account's opening balance (zero) |
| `toDate` | date, optional | Inclusive upper bound. When omitted, includes every transaction up to now |

## Response — `200 OK`

```json
{
  "accountId": "a1b2c3d4-...",
  "accountName": "Cash",
  "openingBalance": 1000.00,
  "lines": [
    {
      "transactionId": "t1a2b3c4-...",
      "transactionDate": "2026-07-10",
      "description": "Salary",
      "transactionType": "INCOME",
      "signedAmount": 500.00,
      "runningBalance": 1500.00
    }
  ],
  "endingBalance": 1300.00
}
```

`openingBalance` is the account's true derived balance as of the day before `fromDate` — not zero — so a
filtered date window never misrepresents the account's actual running balance. `signedAmount` is positive when
the transaction increases the account balance and negative when it decreases it; `runningBalance` accumulates
from `openingBalance` in transaction-date order.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Account does not exist |

---

# GET /api/reports/funds

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `true` | When `true`, only active funds are included |

## Response — `200 OK`

A list of [Fund Report Line Shape](#fund-report-line-shape) objects.

---

# GET /api/reports/funds/{id}

Returns a single [Fund Report Line Shape](#fund-report-line-shape).

## Errors

| Status | Cause |
|--------|-------|
| 404 | Fund does not exist |

---

# GET /api/reports/loans

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `true` | When `true`, only active loans are included |

## Response — `200 OK`

A list of [Loan Report Line Shape](#loan-report-line-shape) objects.

---

# GET /api/reports/loans/{id}

Returns a single [Loan Report Line Shape](#loan-report-line-shape).

## Errors

| Status | Cause |
|--------|-------|
| 404 | Loan does not exist |

---

# GET /api/reports/salary-cycles/{id}

## Response — `200 OK`

```json
{
  "salaryCycleId": "s1a2b3c4-...",
  "cycleName": "July 2026",
  "startDate": "2026-07-01",
  "endDate": "2026-07-31",
  "closed": false,
  "openingBalance": 1000.00,
  "income": 60000.00,
  "expenses": 38000.00,
  "adjustments": 0.00,
  "closingBalance": 23000.00
}
```

Thin wrapper over the same carry-forward calculation used elsewhere (`Opening Balance + Income − Expenses ±
Adjustments = Closing Balance`, per `docs/business/CarryForwardWorkflow.md`), plus the cycle's own metadata.
Transfers are excluded — they move money between accounts without changing net worth.

## Errors

| Status | Cause |
|--------|-------|
| 404 | Salary cycle does not exist |

---

# GET /api/reports/monthly

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `yearMonth` | string, optional | ISO format `YYYY-MM`, e.g. `2026-07`. Defaults to the current month |

## Response — `200 OK`

```json
{
  "yearMonth": "2026-07",
  "totalIncome": 60000.00,
  "totalExpense": 38000.00,
  "netCashFlow": 22000.00,
  "expenseByCategory": [ "...see Category Breakdown Shape..." ],
  "incomeByCategory": [ "...see Category Breakdown Shape..." ],
  "comparisonToPreviousMonth": {
    "currentIncome": 60000.00,
    "previousIncome": 55000.00,
    "currentExpense": 38000.00,
    "previousExpense": 40000.00
  }
}
```

`netCashFlow` is `totalIncome − totalExpense` for the requested month only; `comparisonToPreviousMonth` carries
the same two totals for `yearMonth - 1` so the frontend can render a month-over-month delta without a second
round trip.

---

# GET /api/reports/cash-flow

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `fromDate` | date, required | Inclusive lower bound |
| `toDate` | date, required | Inclusive upper bound |

## Response — `200 OK`

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "moneyIn": 60000.00,
  "moneyOut": 38000.00,
  "netCashFlow": 22000.00,
  "totalTransferVolume": 15000.00
}
```

`moneyIn` / `moneyOut` sum posted `INCOME` / `EXPENSE` transactions over the range. `totalTransferVolume` sums
posted `TRANSFER` transactions over the same range and is reported separately — transfers move money between
accounts (or into/out of funds and loans) without changing net worth, so they are never folded into
`netCashFlow`.

## Errors

| Status | Cause |
|--------|-------|
| 400 | `fromDate` or `toDate` missing |

---

# GET /api/reports/cash-reconciliation

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `accountId` | UUID, optional | When present, returns all reconciliations for this account (date parameters are ignored) |
| `fromDate` | date, optional | Inclusive lower bound; requires `toDate` when `accountId` is absent |
| `toDate` | date, optional | Inclusive upper bound; requires `fromDate` when `accountId` is absent |

Filter precedence: `accountId` first; otherwise both dates together; otherwise every reconciliation is returned.

## Response — `200 OK`

A list of the existing `CashReconciliationResponse` shape — see `docs/api/CashReconciliation.md` for the full
field reference (`expectedCashAmount`, `actualCashAmount`, `differenceAmount`, `status`,
`adjustmentTransactionId`, `snapshots`, ...). This endpoint is a filtered read over existing reconciliation
records, not a separate computation.

---

# GET /api/reports/recurring-transactions

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `activeOnly` | boolean, default `true` | When `true`, only active templates are included |

## Response — `200 OK`

```json
[
  {
    "recurringTransactionId": "r1a2b3c4-...",
    "name": "Rent",
    "active": true,
    "nextExecutionDate": "2026-08-01",
    "lastExecutionDate": "2026-07-01",
    "generatedCount": 6,
    "skippedCount": 1
  }
]
```

`generatedCount` / `skippedCount` are the total number of `GENERATED` / `SKIPPED` executions recorded for the
template — see `docs/api/RecurringTransaction.md` for how executions are created.

---

# Shared Response Shapes

## Loan Summary Shape

```json
{
  "totalReceivable": 10000.00,
  "totalPayable": 3000.00,
  "netPosition": 7000.00,
  "activeLoanCount": 2
}
```

`netPosition` is `totalReceivable − totalPayable` across active loans only.

## Category Breakdown Shape

```json
{ "categoryId": "c1a2b3c4-...", "categoryName": "Groceries", "total": 12000.00, "transactionCount": 15 }
```

## Fund Report Line Shape

```json
{
  "fundId": "f1a2b3c4-...",
  "fundName": "New Laptop",
  "fundType": "GOAL",
  "targetAmount": 150000.00,
  "allocatedAmount": 60000.00,
  "usedAmount": 18000.00,
  "remainingBalance": 42000.00,
  "progressPercentage": 28.00
}
```

`allocatedAmount` / `usedAmount` split the fund's linked `TRANSFER` transactions by direction (account → fund
vs. fund → account). `remainingBalance` is the same derived balance reported by `docs/api/Fund.md`'s `balance`
field, reused here rather than recomputed independently, so the two endpoints never disagree. `progressPercentage`
is `remainingBalance / targetAmount × 100` (rounded to 2 decimals) and is `null` when the fund has no target
amount, or the target is not strictly positive.

## Loan Report Line Shape

```json
{
  "loanId": "l1a2b3c4-...",
  "name": "Friend Loan",
  "loanType": "RECEIVABLE",
  "principalAmount": 5000.00,
  "paidAmount": 2000.00,
  "remainingAmount": 3000.00,
  "loanStatus": "ACTIVE",
  "paymentHistory": [
    { "transactionId": "t1a2b3c4-...", "date": "2026-07-15", "amount": 2000.00, "description": null }
  ]
}
```

`remainingAmount` reuses the same derived balance reported by `docs/api/Loan.md`. `paidAmount` is
`principalAmount − remainingAmount`. `paymentHistory` includes only the repayment-direction transfers for the
loan — for a `RECEIVABLE` loan that means money coming in (`toAccountId` set); for a `PAYABLE` loan, money going
out (`fromAccountId` set).

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Fund not found.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

| Title | Status | Thrown for |
|-------|--------|------------|
| Resource Not Found | 404 | Referenced account, category, fund, loan, or salary cycle does not exist |
| Validation Failed | 400 | Structural or business-rule validation failure (e.g. unsupported transaction type for `/income` or `/expense`) |
| Request Validation Failed | 400 | Missing required query parameter (e.g. `categoryId` on `/category`, `fromDate`/`toDate` on `/cash-flow`) |
| Internal Server Error | 500 | Unexpected failure |

---

# Final Statement

This document reflects the Reports & Dashboard feature (GitHub issue #9) as implemented. Every figure in every
endpoint above is derived live from posted `transactions` at request time — none is a stored column. Changes to
endpoint behavior should update this document alongside the code.
