# Money Flow

## Purpose

This document defines how money moves throughout the Personal Finance App.

It is the authoritative reference for all financial movements.

Every feature that affects money must follow the rules defined in this document.

This document is intentionally independent from:

- Database
- REST API
- Java implementation
- UI

It models business behavior only.

---

# Philosophy

Money never disappears.

Money never appears without explanation.

Every movement has:

- a source
- a destination
- a reason
- a timestamp
- a transaction

If a financial movement cannot be explained, the implementation is considered incorrect.

---

# Financial Universe

```

```
                    USER

                      │

              Owns Financial Assets

                      │

        ┌─────────────┴──────────────┐

        │                            │

     Accounts                    Funds

        │                            │

        └─────────────┬──────────────┘

                      │

                Transactions

                      │

               Financial Reports

```

---

# Money Sources

Money may enter the system from:

Salary

Bonus

Gift

Refund

Loan Received

Fund Withdrawal

Interest (Future)

Investment Return (Future)

Manual Adjustment

Every incoming movement must have an identifiable source.

---

# Money Destinations

Money may leave through:

Expense

Fund Deposit

Loan Given

Loan Repayment

Transfer

Adjustment

Every outgoing movement must have a destination.

---

# Primary Flow

The most common financial flow.

```

Salary

↓

Bank Account

↓

ATM Withdrawal

↓

Cash Wallet

↓

Expenses

↓

Cash Reconciliation

↓

Carry Forward

↓

Next Salary Cycle

```

---

# Account Transfer Flow

Money can move between accounts.

Example

```

Bank

↓

Cash

↓

Mobile Banking

↓

Savings

```

Transfers change location.

They do not create income.

They do not create expenses.

---

# Expense Flow

```

Account

↓

Expense Transaction

↓

Category

↓

Reports

```

Example

Cash

↓

Groceries

↓

Food Report

---

# Fund Flow

Funds reserve money.

```

Bank Account

↓

Emergency Fund

↓

Emergency Fund Balance

↓

Emergency Expense

↓

Bank Account

```

Important:

Fund deposits are NOT expenses.

They are allocations.

---

# Loan Given

```

Bank

↓

Loan Asset

↓

Borrower

↓

Repayment

↓

Bank

```

The loan exists as an asset.

Repayment closes part of the asset.

---

# Loan Received

```

Lender

↓

Bank Account

↓

Outstanding Loan

↓

Repayment

↓

Loan Closed

```

Loan received is not salary.

Loan repayment is not an expense category.

---

# Cash Withdrawal

```

Bank

↓

Cash Wallet

```

This is a transfer.

No money is gained.

No money is lost.

---

# Cash Deposit

```

Cash

↓

Bank

```

Also a transfer.

---

# Cash Expense

```

Cash

↓

Expense

↓

Category

```

Example

Cash

↓

Medicine

↓

Healthcare Report

---

# Card Expense

```

Bank

↓

Expense

↓

Category

```

No cash wallet involved.

---

# Cash Reconciliation Flow

```

Opening Cash

+

Cash Withdrawals

-

Known Expenses

=

Expected Cash

↓

Physical Cash Count

↓

Difference

↓

Adjustment

↓

Reconciled Cash

```

Cash reconciliation never changes history.

It creates new adjustment transactions.

---

# Carry Forward Flow

```

Opening Balance

+

Income

-

Expenses

-

Transfers

± Adjustments

=

Closing Balance

↓

Carry Forward

↓

Next Salary Cycle

```

Carry Forward is calculated.

It is never entered manually.

---

# Fund Lifecycle

```

Income

↓

Fund Allocation

↓

Fund Balance

↓

Withdrawal

↓

Expense

```

Funds can accumulate for years.

---

# Loan Lifecycle

```

Loan Created

↓

Outstanding Balance

↓

Repayment

↓

Remaining Balance

↓

Loan Closed

```

---

# Salary Lifecycle

```

Salary Received

↓

Salary Cycle Starts

↓

Transactions

↓

Cash Reconciliation

↓

Carry Forward

↓

Cycle Ends

↓

Next Salary

```

---

# Transaction Classification

Every transaction must answer:

Where did money come from?

Where did money go?

Why did it move?

Which account changed?

Which report changes?

Which salary cycle owns it?

---

# Report Flow

Reports never store money.

```

Transactions

↓

Filtering

↓

Grouping

↓

Aggregation

↓

Charts

↓

Reports

```

If reports become incorrect,

the transaction history remains the source of truth.

---

# Reconciliation Flow

```

Transactions

↓

Expected Cash

↓

Physical Count

↓

Difference

↓

Adjustment Transaction

↓

Reports Updated

```

---

# Balance Flow

Balances should never be manually maintained.

Instead

```

Transactions

↓

Calculation

↓

Balance

```

Cached balances may exist for performance,

but transactions remain authoritative.

---

# Financial Integrity Rules

Every movement must preserve:

- Traceability
- Auditability
- Determinism
- Reproducibility

Money should always be explainable.

---

# Forbidden Flows

The following are prohibited.

Expense

↓

Salary

Impossible

---

Fund

↓

Expense Report

Incorrect

Funds are transfers.

---

Loan

↓

Expense

Incorrect

Loans are liabilities or assets.

---

Cash Difference

↓

Silent Balance Update

Forbidden

Must create adjustment transaction.

---

# Future Flows

The architecture should support:

Investment Portfolio

↓

Dividend

↓

Income

---

Foreign Currency

↓

Exchange

↓

Multi-Currency Accounts

---

Credit Card

↓

Statement

↓

Payment

---

Subscription

↓

Recurring Transaction

↓

Expense

---

# Example Scenario

Salary

50,000

↓

Bank

↓

ATM

10,000

↓

Cash

↓

Groceries

4,500

↓

Medicine

1,500

↓

Rickshaw

(Not Recorded)

↓

Tea

(Not Recorded)

↓

Wallet Count

3,700

↓

Expected Cash

4,000

↓

Difference

300

↓

Adjustment Transaction

↓

Cash Reconciled

↓

Salary Report Updated

↓

Carry Forward Calculated

Everything remains explainable.

---

# Guiding Principle

Every financial feature added to the application should be representable as a valid money flow.

If a proposed feature cannot be expressed using these flow rules, the domain model should be revisited before implementation.

Money Flow is the foundation upon which the database, APIs, business services, reports, and user interface are built.