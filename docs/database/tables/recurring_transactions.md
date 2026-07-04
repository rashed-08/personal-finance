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
| transaction_type | VARCHAR(30) | No | | EXPENSE / INCOME |
| account_id | UUID | No | | Default account |
| category_id | UUID | No | | Default category |
| amount | NUMERIC(18,2) | No | | Default amount |
| frequency | VARCHAR(20) | No | MONTHLY | DAILY / WEEKLY / MONTHLY / YEARLY |
| interval_value | INTEGER | No | 1 | Repeat interval |
| start_date | DATE | No | | Schedule starts |
| end_date | DATE | Yes | | Optional end |
| next_execution_date | DATE | No | | Next scheduled run |
| auto_generate | BOOLEAN | No | FALSE | Auto-create transaction |
| is_active | BOOLEAN | No | TRUE | Template active |
| notes | VARCHAR(1000) | Yes | | Optional notes |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Last update |

---

# Primary Key

id

UUID

---

# Foreign Keys

account_id

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
- account_id
- category_id
- amount
- frequency
- interval_value
- start_date
- next_execution_date
- auto_generate
- is_active
- created_at
- updated_at

---

## CHECK

transaction_type

Allowed values

- INCOME
- EXPENSE

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

interval_value

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

# Lifecycle

Create

↓

Activate

↓

Generate Transactions

↓

Update Next Execution

↓

Deactivate

↓

Archive

Generated transactions remain unchanged.

---

# Business Rules

- Templates do not affect balances.
- Only generated transactions affect reports.
- Users may manually edit generated transactions.
- Editing a generated transaction does not modify the template.
- Disabling a template stops future generation only.

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