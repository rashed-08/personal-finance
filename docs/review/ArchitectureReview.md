# Architecture Review

Version: 1.0

Status: Living Document

Owner: Personal Finance App

---

# Purpose

This document records architectural observations, design improvements, and important technical decisions discovered during analysis and documentation.

Unlike ADRs, Architecture Reviews are incremental findings that emerge as the project evolves.

Accepted reviews may later become ADRs or implementation tasks.

---

# Status Definitions

| Status | Meaning |
|---------|---------|
| ACCEPTED | Will be implemented |
| PROPOSED | Under consideration |
| DEFERRED | Postponed for a future version |
| REJECTED | Considered but intentionally not adopted |

---

# AR-001

## Title

Modular Monolith First

Status

ACCEPTED

Reason

The application is developed by a single developer.

Microservices would introduce unnecessary operational complexity.

Modules must remain independently deployable in the future.

---

# AR-002

## Title

Salary Cycle instead of Calendar Month

Status

ACCEPTED

Reason

Financial reporting follows salary periods rather than calendar months.

Reports, carry-forward calculations and dashboards are based on salary cycles.

---

# AR-003

## Title

Ledger-first Financial Model

Status

ACCEPTED

Reason

Transactions become the single source of truth.

Balances are derived instead of stored.

---

# AR-004

## Title

Derived Balances

Status

ACCEPTED

Reason

Account balances

Fund balances

Loan balances

Cash balances

must always be calculated from transactions.

Duplicate financial state is avoided.

---

# AR-005

## Title

Historical Data is Immutable

Status

ACCEPTED

Reason

Historical financial events should never be modified silently.

Corrections must create adjustment transactions instead.

---

# AR-006

## Title

Salary Cycle Assignment

Status

ACCEPTED

Reason

Transactions store salary_cycle_id.

The application assigns it automatically.

Users never select it manually.

---

# AR-007

## Title

Carry Forward Calculation

Status

ACCEPTED

Reason

Carry forward is calculated from transaction history.

It is never stored as a permanent balance.

---

# AR-008

## Title

Cash Reconciliation

Status

ACCEPTED

Reason

Users are not required to record every small cash expense.

Periodic reconciliation creates adjustment transactions.

---

# AR-009

## Title

Fund Balance Calculation

Status

ACCEPTED

Reason

Funds are logical financial goals.

Balances are calculated from transaction history.

---

# AR-010

## Title

Loan Accounting Model

Status

ACCEPTED

Reason

Loans represent relationships.

Money movement is stored in transactions.

Outstanding balance is calculated.

---

# AR-011

## Title

Recurring Transactions

Status

ACCEPTED

Reason

Recurring transactions are templates only.

Generated transactions become independent financial events.

---

# AR-012

## Title

Settings Singleton

Status

ACCEPTED

Reason

Version 1 supports one user.

Only one settings record exists.

---

# AR-013

## Title

Persist Salary Cycle ID

Status

ACCEPTED

Reason

Although salary cycle can be calculated from transaction dates, storing salary_cycle_id improves reporting performance and preserves historical accuracy when salary rules change.

---

# AR-014

## Title

Future Fund Allocation Table

Status

PROPOSED

Reason

A single transaction may allocate money to multiple funds.

Future versions may introduce a dedicated `fund_allocations` table instead of a single `fund_id`.

---

# AR-015

## Title

Loan Amount Evolution

Status

PROPOSED

Reason

The current `original_amount` field is sufficient for Version 1.

Future ledger-based versions may derive the total loan amount entirely from transactions, allowing additional disbursements without updating the loan record.

---

# AR-016

## Title

Cash Reconciliation Snapshot

Status

ACCEPTED

Reason

Cash reconciliation stores the calculated and actual cash values at the time of reconciliation.

These values remain unchanged even if historical transactions are added later.

This preserves an auditable financial history.

---

# AR-017

## Title

Variable Recurring Amounts

Status

DEFERRED

Reason

Some recurring transactions (electricity, gas, etc.) have variable amounts.

Version 1 supports fixed amounts only.

Variable templates may be introduced later.

---

# AR-018

## Title

Separate Backup Configuration

Status

PROPOSED

Reason

Backup history stores audit records only.

Future configuration (schedule, retention, frequency) should be managed independently rather than inside the settings table.

---

# AR-019

## Title

Backup Storage Provider

Status

ACCEPTED

Reason

Backup storage providers should be extensible.

The application must support multiple providers such as Google Drive, OneDrive, Dropbox or local storage.

---

# AR-020

## Title

Migration Batch Tracking

Status

PROPOSED

Reason

Imported transactions should optionally reference a migration batch.

This enables rollback, auditing and future imports from Google Keep, CSV or bank statements.

---

# Future Reviews

Expected review topics

- Split Transactions
- Multi-currency
- Attachments
- Receipt OCR
- Investment Portfolio
- Bank Synchronization
- Mobile Offline Mode
- AI Categorization
- Budget Engine

---

# Review Summary

Accepted

11

Proposed

4

Deferred

1

Rejected

0

---

# Guiding Principles

Every architecture review should satisfy at least one of the following goals:

- Reduce complexity
- Preserve historical correctness
- Improve maintainability
- Improve extensibility
- Reduce data duplication
- Improve reporting performance
- Improve developer experience

---

# Final Statement

Architecture Reviews capture the evolution of the system design.

They provide historical context for implementation decisions and ensure that future development remains aligned with the project's long-term architectural vision.