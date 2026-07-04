# Module Dependencies

## Purpose

This document defines how backend modules interact with one another.

The objective is to:

- reduce coupling
- improve maintainability
- preserve module ownership
- prevent architectural erosion

Every module has clear responsibilities.

No module should directly manipulate another module's internal implementation.

---

# Architectural Principle

Modules communicate through services.

Modules never communicate through repositories.

Modules never manipulate another module's entities directly.

Business rules remain inside the owning module.

---

# Dependency Direction

Allowed

```
Controller

↓

Application Service

↓

Domain Service

↓

Repository
```

Forbidden

```
Controller

↓

Repository
```

Forbidden

```
Repository

↓

Repository
```

Forbidden

```
Service

↓

Other Module Repository
```

---

# Core Modules

The backend consists of the following business modules.

```
Accounts

Categories

Transactions

Salary

Funds

Loans

Cash

Reports

Backup

Settings
```

Each module owns its:

- entities
- repositories
- services
- validation
- business rules

---

# Dependency Graph

```
                Reports

                    │

        ┌───────────┴───────────┐

        │                       │

Transactions              Salary

        │                       │

        ├──────────────┐        │

        │              │        │

Accounts        Categories      │

        │              │        │

        ├──────┬───────┘        │

               │

            Funds

               │

            Loans

               │

              Cash

               │

           Backup

               │

           Settings
```

The diagram represents logical communication rather than package hierarchy.

---

# Accounts Module

Owns

- financial accounts
- balances (derived or cached)
- account metadata

Provides

- AccountService

May depend on

None

---

# Categories Module

Owns

- expense categories
- income categories
- reporting categories

Provides

- CategoryService

May depend on

None

---

# Transactions Module

Owns

- financial ledger
- transaction creation
- transaction queries
- accounting validation

Provides

- TransactionService
- TransactionQueryService

May depend on

Accounts

Categories

Transactions is the core financial engine.

---

# Salary Module

Owns

- salary cycles
- salary workflow
- carry forward

Provides

- SalaryService

Depends on

TransactionService

TransactionQueryService

AccountService

---

# Funds Module

Owns

- emergency fund
- zakat fund
- purchase goal
- reserved balances

Provides

- FundService

Depends on

TransactionService

AccountService

---

# Loans Module

Owns

- receivables
- payables
- repayments

Provides

- LoanService

Depends on

TransactionService

AccountService

---

# Cash Module

Owns

- reconciliation
- adjustments
- cash snapshots

Provides

- CashReconciliationService

Depends on

TransactionService

AccountService

SalaryService

---

# Reports Module

Owns

- dashboard
- summaries
- statistics
- charts

Provides

- ReportService

Depends on

TransactionQueryService

SalaryService

FundService

LoanService

CashReconciliationService

CategoryService

Reports never access repositories directly.

---

# Backup Module

Owns

- Google Drive backup
- restore
- export

Provides

- BackupService

Depends on

Read-only services only.

Backup must never change business rules.

---

# Settings Module

Owns

- user preferences
- application configuration
- backup settings
- report preferences

Provides

- SettingsService

Independent from financial modules.

---

# Allowed Dependencies

| Module | May Use |
|---------|---------|
| Accounts | None |
| Categories | None |
| Transactions | Accounts, Categories |
| Salary | Transactions, Accounts |
| Funds | Transactions, Accounts |
| Loans | Transactions, Accounts |
| Cash | Transactions, Salary, Accounts |
| Reports | TransactionQuery, Salary, Funds, Loans, Cash, Categories |
| Backup | Read-only Services |
| Settings | None |

---

# Forbidden Dependencies

Examples

Reports

↓

TransactionRepository

Forbidden

---

Funds

↓

LoanRepository

Forbidden

---

Salary

↓

CategoryRepository

Forbidden

---

Cash

↓

FundRepository

Forbidden

---

Modules communicate only through services.

---

# Repository Ownership

Each repository belongs to exactly one module.

Example

TransactionRepository

belongs only to

Transactions Module

No other module should inject it.

---

# Entity Ownership

Entities remain private to their modules whenever practical.

Expose DTOs or domain objects instead of entities.

---

# Shared Components

Allowed shared components

- ApiResponse
- ErrorResponse
- Constants
- BaseEntity
- AuditEntity

Shared should remain infrastructure-only.

Business logic never belongs in shared.

---

# Eventual Expansion

Future modules

- Budget
- Investment
- Notification
- AI Insights
- Forecast
- Analytics

should integrate using service contracts.

No existing module should require restructuring.

---

# Circular Dependency Policy

Circular dependencies are prohibited.

If Module A requires Module B

and

Module B requires Module A,

the design should be reconsidered.

Usually,

a new shared abstraction should be introduced.

---

# Transaction Ownership

Every financial operation eventually becomes a transaction.

However,

only the Transactions module is responsible for creating ledger entries.

Other modules request transaction creation through services.

---

# Reporting Principle

Reports consume data.

Reports never own business rules.

Reports never modify financial data.

---

# Backup Principle

Backup exports data.

Backup never performs accounting.

Backup never recalculates balances.

---

# Design Rules

Every new module should answer:

- What does it own?
- What services does it expose?
- Which modules may depend on it?
- Which repositories belong to it?
- Which business rules belong to it?

If ownership is unclear,

the module boundary should be reconsidered.

---

# Final Statement

Each module owns a clearly defined business capability.

Modules collaborate through service interfaces while preserving encapsulation.

This dependency model minimizes coupling, prevents architectural erosion, and provides a stable foundation for future expansion, including microservice extraction if required.