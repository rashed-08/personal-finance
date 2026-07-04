# Value Objects

## Purpose

This document defines the immutable Value Objects used throughout the Personal Finance App.

Unlike Entities, Value Objects do not have identity.

They are defined entirely by their values.

Value Objects encapsulate business concepts, improve readability, centralize validation, and reduce duplication.

---

# What is a Value Object?

A Value Object:

- has no identity
- is immutable
- is compared by value
- represents a business concept
- contains validation and behavior

Example

Incorrect

```
BigDecimal amount
```

Correct

```
Money amount
```

---

# Design Principles

Every Value Object should

- be immutable
- validate itself
- implement equality by value
- never expose invalid state

---

# Money

## Purpose

Represents a monetary value.

---

## Attributes

- Amount
- Currency

---

## Responsibilities

- Addition
- Subtraction
- Comparison
- Zero check
- Positive check
- Negative check
- Rounding

---

## Business Rules

- Currency cannot be null.
- Amount cannot exceed supported precision.
- Money objects with different currencies cannot be added.
- Scale is fixed.

---

## Examples

```
Money.of(500)

Money.of(1200.50)

Money.zero()
```

---

# Currency

## Purpose

Represents a supported currency.

Version 1 supports only

- BDT

Future versions may support

- USD
- EUR
- GBP

---

## Responsibilities

- Currency validation
- Formatting

---

# Percentage

## Purpose

Represents a percentage.

Future use cases

- Investment Return
- Interest Rate
- Savings Progress

---

## Business Rules

- Minimum
- Maximum
- Precision

---

# DateRange

## Purpose

Represents a period between two dates.

---

## Examples

Salary Cycle

Cash Reconciliation

Reports

Recurring Transactions

---

## Responsibilities

- Contains date
- Overlap detection
- Inclusion check
- Duration calculation

---

# SalaryCyclePeriod

## Purpose

Represents one salary cycle.

Example

10 May

↓

9 June

---

## Responsibilities

- Opening date
- Closing date
- Contains transaction check

---

# TransactionType

## Purpose

Represents the business meaning of a transaction.

Possible values

- INCOME
- EXPENSE
- TRANSFER
- ADJUSTMENT

Future

- IMPORT
- REVERSAL

---

# AccountType

## Purpose

Represents account classifications.

Examples

- CASH
- BANK
- MOBILE_BANKING
- SAVINGS

Future

- CREDIT_CARD

---

# CategoryType

## Purpose

Represents category classification.

Values

- EXPENSE
- INCOME

---

# FundType

## Purpose

Represents different kinds of funds.

Version 1

- EMERGENCY
- ZAKAT
- PURCHASE_GOAL
- CUSTOM

Future

- INVESTMENT
- EDUCATION
- TRAVEL

---

# LoanType

## Purpose

Represents loan direction.

Values

- RECEIVABLE
- PAYABLE

---

# LoanStatus

Possible values

- ACTIVE
- CLOSED
- CANCELLED

Future

- OVERDUE

---

# FundStatus

Possible values

- ACTIVE
- CLOSED

---

# TransactionStatus

Possible values

- POSTED
- RECONCILED

Future

- DRAFT
- VOID
- REVERSED

---

# BackupType

Possible values

- GOOGLE_DRIVE
- LOCAL
- EXPORT

---

# BackupStatus

Possible values

- SUCCESS
- FAILED
- RUNNING

---

# RecurrenceType

Represents recurrence frequency.

Values

- DAILY
- WEEKLY
- MONTHLY
- YEARLY

---

# ReminderType

Future feature.

Examples

- ONE_DAY_BEFORE
- THREE_DAYS_BEFORE
- ONE_WEEK_BEFORE

---

# Notes

Represents user-entered notes.

Responsibilities

- Length validation
- Whitespace normalization

---

# Color

Represents UI colors.

Examples

Category colors

Dashboard colors

Fund colors

---

# Icon

Represents icon identifiers used by the UI.

Examples

- Grocery
- Medicine
- Gym
- Rent

---

# EmailAddress

Future use.

Google Backup

Authentication

Sharing

---

# PhoneNumber

Future use.

Reminder

Sharing

Contact Management

---

# UUIDIdentifier

Represents domain identifiers.

Examples

- TransactionId
- AccountId
- LoanId
- FundId

This allows stronger typing than passing raw UUID values throughout the application.

---

# Value Object Ownership

| Value Object | Primary Module |
|--------------|----------------|
| Money | Shared Domain |
| Currency | Shared Domain |
| DateRange | Shared Domain |
| SalaryCyclePeriod | Salary |
| TransactionType | Transactions |
| AccountType | Accounts |
| CategoryType | Categories |
| FundType | Funds |
| LoanType | Loans |
| LoanStatus | Loans |
| FundStatus | Funds |
| TransactionStatus | Transactions |
| BackupType | Backup |
| BackupStatus | Backup |
| RecurrenceType | Transactions |

---

# Design Guidelines

Prefer Value Objects over primitive types whenever the value carries business meaning.

Examples

Good

```
Money

FundType

LoanType

SalaryCyclePeriod
```

Avoid

```
BigDecimal

String

Integer

Long
```

unless they truly represent primitive data.

---

# Immutability

Every Value Object should

- expose no setters
- validate in constructor or factory
- be thread-safe
- be reusable

---

# Equality

Value Objects compare by value.

Example

```
Money.of(500)

==

Money.of(500)
```

Identity is irrelevant.

---

# Serialization

Value Objects should serialize cleanly to JSON while preserving their business meaning.

The API should expose clear and predictable representations.

---

# Future Value Objects

Potential additions

- ExchangeRate
- InterestRate
- TaxRate
- Progress
- GoalAmount
- FileChecksum
- BackupLocation

These can be introduced without modifying existing entities.

---

# Final Statement

Value Objects represent the shared language of the financial domain.

They encapsulate validation, prevent invalid state, eliminate primitive obsession, and improve readability throughout the codebase.

Business concepts should be modeled as Value Objects whenever identity is unnecessary and behavior naturally belongs with the value itself.