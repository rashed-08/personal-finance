# Recurring Transactions Table Specification

## Purpose

The `recurring_transactions` table stores reusable templates for transactions that occur on a regular schedule.

Recurring transactions do not represent actual financial events.

Instead, they define when and how future transactions should be generated.

Generated transactions become normal records in the `transactions` table.

---

# Aggregate

Recurring Transaction Aggregate

Aggregate Root

RecurringTransaction

---

# Responsibilities

- Store recurring transaction templates
- Automate repetitive data entry
- Generate scheduled transactions
- Track template status
- Support recurring financial planning

---

# Examples

Monthly

- House Rent
- Internet Bill
- Gym Membership

Weekly

- Grocery Budget

Yearly

- Zakat
- Domain Renewal

Future

- Insurance
- EMI
- Netflix
- Spotify

---

# Table Definition

Table Name

recurring_transactions

Primary Key

id

---

# Columns

| Column | Type | Nullable | Default | Description |
|---------|------|----------|---------|-------------|
| id | UUID | No | Generated | Primary Key |
| name | VARCHAR(100) | No | | Template name |
| transaction_type | VARCHAR(30) | No | | EXPENSE / INCOME / TRANSFER |
| from_account_id | UUID | Conditional | | Source account (EXPENSE, TRANSFER) |
| to_account_id | UUID | Conditional | | Destination account (INCOME, TRANSFER) |
| category_id | UUID | Conditional | | Required for EXPENSE/INCOME, null for TRANSFER |
| amount | NUMERIC(18,2) | No | | Amount per occurrence |
| description | VARCHAR(255) | Yes | | Optional label |
| notes | TEXT | Yes | | Optional notes |
| frequency | VARCHAR(20) | No | | DAILY / WEEKLY / MONTHLY / YEARLY |
| start_date | DATE | No | | Schedule starts; also the first occurrence's date |
| end_date | DATE | Yes | | Optional end |
| next_execution_date | DATE | No | | Next scheduled occurrence |
| last_execution_date | DATE | Yes | | Scheduled date of the most recent successful generation |
| auto_generate | BOOLEAN | No | FALSE | Generate due occurrences automatically vs. require manual confirmation |
| is_active | BOOLEAN | No | TRUE | Template active |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Last update |

There is no `interval_value` (repeat-every-N) or per-template salary cycle column: the salary cycle for a
generated transaction is always resolved to whichever cycle is open at generation time, never stored on the
template. See [Future Enhancements](#future-enhancements) for custom intervals.

Also present: `recurring_transaction_executions`, a separate append-only log table — see
[Execution History](#execution-history) below.

---

# Primary Key

id

UUID

---

# Foreign Keys

from_account_id / to_account_id

↓

accounts(id)

category_id

↓

categories(id)

---

# Constraints

## NOT NULL

- id
- name
- transaction_type
- amount
- frequency
- start_date
- next_execution_date
- auto_generate
- is_active
- created_at
- updated_at

`from_account_id`/`to_account_id`/`category_id` are conditionally required depending on `transaction_type`, same
as the `transactions` table itself.

---

## CHECK

transaction_type

Allowed values

- INCOME
- EXPENSE
- TRANSFER

---

frequency

Allowed values

- DAILY
- WEEKLY
- MONTHLY
- YEARLY

---

amount

Must be greater than zero.

---

# Relationships

One Template

↓

Generates

↓

Many Transactions

```
Recurring Template

↓

Transaction

↓

Reports
```

The generated transaction has no dependency on the template after creation.

---

# Execution History

Every due occurrence a scheduler run looks at is recorded in `recurring_transaction_executions` — one row per
occurrence, whether it produced a transaction or not:

| Column | Type | Description |
|--------|------|--------------|
| id | UUID | Primary Key |
| recurring_transaction_id | UUID | FK to `recurring_transactions.id` |
| scheduled_date | DATE | The occurrence's scheduled date (not when the run happened) |
| status | VARCHAR(20) | `GENERATED` / `SKIPPED` |
| transaction_id | UUID | Set only when `status = GENERATED`; FK to `transactions.id` |
| reason | VARCHAR(500) | Set only when `status = SKIPPED` (e.g. "No salary cycle is currently open") |
| created_at | TIMESTAMP | When this row was written |

This satisfies "Missed Scheduled Transactions" reporting (see [Reporting Usage](#reporting-usage)) and gives
"history is maintained" a concrete, queryable answer.

---

# Lifecycle

Create (active, `next_execution_date` = `start_date`)

↓

Due (`next_execution_date <= today`)

↓

Generate — on-demand, not a background job (see below)

↓

Schedule advances from the occurrence's own scheduled date, recorded as GENERATED or SKIPPED

↓

Deactivate (stops future generation only) / Delete

Generated transactions remain unchanged after creation, and are unaffected by later changes to the template.

## Generation triggers

Version 1 has no background scheduler. Two on-demand triggers exist instead:

- **Run due transactions** — processes every `auto_generate = TRUE` template whose `next_execution_date` has
  arrived, catching up *all* missed occurrences in one call (not just one), so the schedule stays correct even
  if the app wasn't opened on the exact due date.
- **Generate now** (per template) — the manual-confirmation path for `auto_generate = FALSE` templates (or to
  force an occurrence early). Generates exactly the next due occurrence.

Both paths resolve the generated transaction's salary cycle to whichever cycle is open at generation time, and
both record the outcome in `recurring_transaction_executions`.

---

# Business Rules

- Templates do not affect balances.
- Only generated transactions affect reports.
- Users may manually edit generated transactions.
- Editing a generated transaction does not modify the template.
- Disabling a template stops future generation only.
- A transaction type, its accounts, and its category are fixed at creation — only scheduling/metadata can
  change afterward.
- If a due occurrence cannot be generated (no open salary cycle, an account was deactivated, etc.), it is
  recorded as `SKIPPED` with a reason, and the schedule still advances to the next occurrence rather than
  retrying the same date indefinitely.

---

# Scheduling

Version 1 supports

- Daily
- Weekly
- Monthly
- Yearly

Future

- Custom Cron Expression
- Last Working Day
- First Business Day
- Lunar Calendar

---

# Reporting Usage

Used in

- Upcoming Payments
- Upcoming Income
- Automation Dashboard
- Missed Scheduled Transactions

---

# Example Records

| Name | Frequency | Amount |
|------|-----------|--------|
| House Rent | Monthly | 10000 |
| Internet | Monthly | 900 |
| Gym | Monthly | 1500 |
| Donation | Monthly | 500 |

---

# Future Enhancements

Possible additions

- Custom repeat interval (e.g. every 2 weeks), previously drafted as an `interval_value` column
- A background scheduler, rather than the current on-demand triggers
- Reminder Before Due Date
- Notification
- Percentage-based Amount
- Variable Amount Formula
- Fund Allocation
- Loan Repayment Automation
- Google Calendar Sync

---

# Design Decisions

- Templates are not financial transactions.
- Generated transactions become independent.
- Templates never modify historical transactions.
- Automation is optional.
- Business logic remains in the application layer.

---

# Final Statement

The `recurring_transactions` table automates repetitive financial activities while preserving the integrity of the financial ledger.

By separating recurring rules from actual transactions, the application remains flexible, auditable, and easy to maintain.