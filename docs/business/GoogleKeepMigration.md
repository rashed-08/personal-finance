# Google Keep Migration

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document defines the strategy for migrating historical financial data from Google Keep into the Personal Finance Application.

The migration is intended to preserve historical financial information while minimizing manual effort.

Version 1 focuses on importing summary-level monthly records. Future versions may support detailed transaction extraction.

---

# Background

For approximately one year, financial records were maintained in Google Keep using a simple text format.

Example:

04-26
=========

Gym 1500

House Rent 10000

Internet 900

Medicine 2770 (980+1790)

Market 10735
(900+725+1200+480+1380+...)

Milk 1200

Recharge 490

Total
75800

The application must convert these notes into structured financial data.

---

# Migration Goals

The migration should:

- Preserve historical financial records
- Preserve reporting accuracy
- Reduce manual data entry
- Allow optional corrections after import
- Support future re-imports

---

# Migration Scope

Version 1 imports:

- Categories
- Monthly expense totals
- Salary cycle mapping
- Notes
- Imported metadata

Version 1 does NOT import:

- Individual market items
- Receipt images
- Attachments
- OCR

---

# Source Format

Expected Google Keep structure

Month Header

```
04-26
```

Category

```
Market 10735
```

Optional Breakdown

```
(900+725+1200+...)
```

Total

```
=75800
```

---

# Parsing Rules

Each non-empty line is analyzed.

Possible types

- Month
- Category
- Amount
- Category with breakdown
- Total
- Comment

---

# Category Detection

Known categories

Examples

```
Market

Milk

Medicine

Internet

Gym

House Rent

Recharge

Electricity

Donation

Travel
```

Unknown categories

↓

Imported as

```
Uncategorized
```

The user may reclassify later.

---

# Amount Detection

Examples

```
1500

900

10735
```

Supported

Whole numbers

Future

Decimal amounts

---

# Breakdown Handling

Example

```
Market

10735

(900+725+1200+...)
```

Version 1

Only

```
10735
```

is imported.

Breakdown text is stored inside

Transaction Notes.

Future versions may create individual transactions automatically.

---

# Month Mapping

Google Keep

```
04-26
```

↓

Application

Salary Cycle

↓

Transactions

The migration engine maps each note into the appropriate salary cycle.

If salary cycles do not yet exist,

they are generated automatically.

---

# Salary Cycle Mapping

Example

Salary Day

10

Google Keep

April

↓

Salary Cycle

10 April

↓

9 May

Transactions are assigned automatically.

---

# Transaction Date

Historical notes usually contain only monthly summaries.

Version 1 uses

```
Salary Cycle Start Date
```

as the default transaction date.

Future versions may allow manual adjustment.

---

# Cash Transactions

Historical notes usually omit small daily cash expenses.

Therefore

Cash Reconciliation begins only after migration.

Imported history is treated as reconciled.

No historical cash reconciliation records are generated.

---

# Funds

Version 1 attempts to recognize

Examples

```
Emergency

Zakat

Savings
```

If detected

↓

Fund Allocation

Otherwise

↓

Normal Expense

---

# Loans

Examples

```
Borrowed

Loan

Paid Back

Lent
```

These are imported as Loan Transactions whenever possible.

Unknown records require manual review.

---

# Validation Rules

Each imported note is validated.

Checks include

- Amount exists
- Category detected
- Total calculated
- Duplicate month detection
- Invalid syntax

Invalid rows are skipped.

Errors are recorded.

---

# Duplicate Detection

A month is considered duplicated when

Salary Cycle

+

Category

+

Amount

already exist.

The user decides whether to

- Skip
- Replace
- Import Anyway

---

# Import Result

After migration

The application displays

Imported

Skipped

Warnings

Errors

Duration

---

# Import Log

Each migration creates one import report.

The report contains

- Source file
- Import date
- Imported rows
- Failed rows
- Warnings

Future

Persistent import history.

---

# Future Enhancements

Possible additions

- Google Keep API
- Automatic Synchronization
- OCR
- AI Category Detection
- AI Transaction Splitting
- AI Duplicate Detection
- Receipt Import
- Photo Parsing

---

# Design Decisions

- Google Keep is treated as a legacy data source.
- Imported transactions remain editable.
- Monthly summaries are imported as summarized transactions.
- Breakdown values are preserved as notes.
- Salary cycles are assigned automatically.
- Historical cash reconciliation is not generated.
- Migration is idempotent where possible.

---

# Example

Google Keep

```
Market

10735

(900+725+1200+...)
```

↓

Imported Transaction

Category

Market

Amount

10735

Notes

900+725+1200+480+1380+380+...

Salary Cycle

April 2026

---

# Final Statement

The Google Keep migration process provides a reliable bridge between legacy financial records and the new Personal Finance Application.

By preserving historical summaries while allowing future refinement, the migration minimizes manual effort and establishes a clean foundation for long-term financial tracking.