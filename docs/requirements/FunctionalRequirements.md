# Functional Requirements

## Purpose

This document defines the functional capabilities of the Personal Finance App.

It describes **what** the system must do from a user's perspective without describing **how** it will be implemented.

The requirements in this document serve as the foundation for:

- Database Design
- API Design
- UI Design
- Business Rules
- Testing
- Future Enhancements

---

# Scope

The application is designed for **single-user personal finance management**.

The system will help users:

- Record income
- Record expenses
- Track account balances
- Track investments and savings
- Manage loans
- Generate reports
- Perform automatic cash reconciliation
- Backup financial data

Multi-user collaboration is **out of scope** for the initial version.

---

# Core Functional Areas

The application consists of the following business domains:

1. Authentication
2. Accounts
3. Categories
4. Transactions
5. Salary Cycle
6. Funds
7. Loans
8. Reports
9. Cash Reconciliation
10. Backup & Restore
11. Settings
12. Dashboard

---

# FR-001 Authentication

## Description

The system shall provide secure authentication.

## Requirements

- Login
- Logout
- Change password
- Session management

Future

- Google Login
- Two-factor authentication

---

# FR-002 Account Management

## Description

Users can maintain multiple financial accounts.

Examples

- Cash Wallet
- Bank Account
- Mobile Banking
- Credit Card (Future)

The system shall allow:

- Create account
- Edit account
- Archive account
- View account balance

The balance shall always be calculated from transactions.

Manual balance modification should not be allowed except through approved adjustment workflows.

---

# FR-003 Category Management

The user shall be able to organize transactions into categories.

Examples

Income

- Salary
- Bonus
- Gift

Expense

- Rent
- Grocery
- Medicine
- Internet
- Gym
- Utilities
- Transport

Investment

- Emergency Fund
- Zakat Fund
- Purchase Fund

Categories should support future expansion without database redesign.

---

# FR-004 Income Management

The system shall support recording income.

Examples

- Salary
- Bonus
- Refund
- Cashback
- Interest
- Other Income

Each income transaction shall include:

- Date
- Account
- Category
- Amount
- Notes (optional)

---

# FR-005 Expense Management

Users shall be able to record expenses.

Each expense shall contain:

- Date
- Account
- Category
- Amount
- Notes
- Attachments (Future)

Expenses may be entered:

- Immediately
- Later
- During reconciliation

---

# FR-006 Transfer Management

Users shall transfer money between accounts.

Examples

Bank

↓

Emergency Fund

Cash

↓

Mobile Banking

Transfers shall:

- Reduce one account
- Increase another account

Transfers shall not be treated as expenses.

---

# FR-007 Salary Cycle

The application shall support salary periods that do not match calendar months.

Example

Salary received:

10 May

Expenses

11 May → 10 June

Reports should support:

- Calendar Month
- Salary Cycle

Both views are equally important.

---

# FR-008 Investment & Funds

Users shall maintain multiple financial goals.

Examples

Emergency Fund

Zakat Fund

Purchase Fund

Investment Fund

Each fund shall support:

- Deposit
- Withdrawal
- Balance
- History

Funds should behave similarly to accounts while remaining conceptually separate.

---

# FR-009 Loan Management

The system shall support:

Loans Given

Loans Received

Loan Repayment

Loan Collection

The system shall display:

Outstanding Amount

Payment History

Current Balance

Loan reports

---

# FR-010 Recurring Transactions

The system shall support recurring transactions.

Examples

Monthly Rent

Internet Bill

Gym Fee

Recurring Donation

Insurance

Salary

Supported frequencies

Daily

Weekly

Monthly

Quarterly

Yearly

Users should be able to skip or edit individual occurrences.

---

# FR-011 Reports

The application shall generate financial reports.

Examples

Monthly Report

Yearly Report

Salary Cycle Report

Category Report

Account Report

Fund Report

Loan Report

Investment Report

Cash Flow Report

Expense Trend

Income Trend

Reports shall support filtering.

---

# FR-012 Dashboard

The dashboard shall summarize financial information.

Examples

Current Balance

Monthly Expenses

Monthly Income

Upcoming Recurring Payments

Recent Transactions

Cash Position

Fund Balances

Top Spending Categories

Monthly Comparison

---

# FR-013 Cash Reconciliation

This is a core feature.

The system shall support automatic cash reconciliation.

Workflow

1. Record ATM withdrawals.

2. Record major expenses.

3. Ignore insignificant cash spending if desired.

4. Periodically record actual cash balance.

5. Automatically calculate untracked cash differences.

6. Allow the user to classify differences.

Examples

Miscellaneous Expense

Transfer

Loan

Adjustment

The reconciliation process shall preserve a complete audit history.

---

# FR-014 Backup & Restore

The application shall support:

Manual Backup

Automatic Backup

Google Drive Synchronization

Restore from Backup

Export Database

Import Database

CSV Export

Future cloud providers may be supported.

---

# FR-015 Search

Users shall search by:

Category

Account

Amount

Date Range

Notes

Tags (Future)

Search should remain responsive even with years of financial history.

---

# FR-016 Dashboard Notifications

The dashboard may display:

Upcoming recurring payments

Pending loan repayments

Low emergency fund balance

Failed backups

Pending reconciliation

---

# FR-017 Settings

Users shall configure:

Currency

Date Format

Theme

Backup Preferences

Salary Cycle

Default Account

Default Categories

Notification Preferences

---

# FR-018 Audit History

Important financial operations shall be traceable.

Examples

Adjustment

Fund Transfer

Loan Modification

Cash Reconciliation

Restore Operation

Audit history improves transparency and future troubleshooting.

---

# FR-019 Data Import

Future versions may support importing data from:

CSV

Excel

Google Keep (custom migration)

Bank Statements

---

# FR-020 Data Export

Users shall export data as:

CSV

Excel

PDF Reports

Database Backup

---

# Business Rules

The following rules apply across the system.

- Money cannot disappear without explanation.
- Every financial movement must be represented by one or more transactions.
- Transfers are not expenses.
- Fund deposits are not expenses.
- Loan repayments are not expenses.
- Reports must remain reproducible.
- Historical data should not be silently modified.
- Every reconciliation must preserve an audit trail.

---

# Out of Scope (Version 1)

The following features are intentionally excluded from the initial release.

- Multi-user support
- Shared wallets
- Bank API integration
- OCR receipt scanning
- AI-powered categorization
- Cryptocurrency tracking
- Stock portfolio management
- Tax calculation
- Mobile applications
- Offline synchronization

These may be considered in future versions.

---

# Requirement Traceability

Every new feature should:

- Be linked to one or more functional requirements.
- Update this document if user-visible behavior changes.
- Include corresponding database, API, and UI changes where applicable.

This document is the authoritative source for functional behavior.