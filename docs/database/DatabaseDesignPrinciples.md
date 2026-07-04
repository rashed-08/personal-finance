# Database Design Principles

## Purpose

This document defines the guiding principles for designing the database of the Personal Finance App.

The database is an implementation detail of the domain model.

Business rules are defined by the Domain Model, not by database tables.

The objective is to create a database that is reliable, maintainable, extensible, and capable of supporting future architectural evolution.

---

# Design Philosophy

The database exists to persist domain state.

It should not become the place where business logic lives.

The application layer remains responsible for enforcing business rules.

---

# Design Goals

The database should be:

- Correct
- Consistent
- Extensible
- Auditable
- Performant
- Easy to migrate
- Easy to back up
- Easy to restore

---

# Domain First

Database design always follows the domain model.

The order of design is:

Business Requirements

↓

Domain Model

↓

Aggregates

↓

Database Schema

↓

Tables

↓

Indexes

↓

Migrations

Never reverse this process.

---

# Normalization

Version 1 targets Third Normal Form (3NF).

Goals

- eliminate duplication
- preserve consistency
- simplify maintenance

Controlled denormalization is allowed only after measuring performance problems.

---

# Primary Keys

Every table should use a surrogate primary key.

Preferred type

UUID

Reasons

- globally unique
- API friendly
- future synchronization
- backup merging
- offline capability

Natural keys should not be primary keys.

---

# Foreign Keys

Relationships should always be explicit.

Foreign key constraints must be enabled unless there is a compelling reason not to.

Referential integrity should be enforced by the database.

---

# Immutable Financial History

Historical financial records should never be physically deleted.

Preferred approach

- adjustment transaction
- reversal transaction (future)
- archive (future)

DELETE statements should rarely be used.

---

# Soft Delete

Business entities should support soft deletion where appropriate.

Recommended fields

- deleted_at
- deleted_by (future)

Financial transactions should never be soft deleted.

They should remain immutable.

---

# Audit Columns

Every persistent entity should contain:

- created_at
- updated_at

Future

- created_by
- updated_by
- deleted_at
- deleted_by

---

# Monetary Values

Money should never use floating-point data types.

Preferred type

NUMERIC / DECIMAL

Scale and precision should remain consistent across all monetary columns.

Application code should represent money using the Money Value Object.

---

# Currency

Version 1 supports only:

BDT

The schema should still allow future multi-currency support without redesign.

---

# Dates and Time

Use UTC internally whenever timestamps are stored.

Business reports should apply user timezone during presentation.

Date-only fields should use DATE.

Timestamp fields should use TIMESTAMP WITH TIME ZONE where appropriate.

---

# Naming Convention

Naming rules are documented separately in:

NamingConvention.md

General principles:

- snake_case
- singular table names
- lowercase identifiers
- descriptive names

---

# Constraints

The database should use constraints whenever possible.

Examples

- NOT NULL
- CHECK
- UNIQUE
- FOREIGN KEY

Business logic should not rely solely on application validation.

---

# Indexing Strategy

Indexes should exist only where they provide measurable value.

Expected index candidates

- transaction_date
- salary_cycle_id
- account_id
- category_id
- fund_id
- loan_id

Composite indexes should be introduced only after query analysis.

---

# Reporting Strategy

Reports should be generated from transactional data.

Avoid maintaining redundant summary tables in Version 1.

Future versions may introduce materialized views if necessary.

---

# Backup Strategy

The database should support:

- local backup
- Google Drive backup
- restore
- export

Backups should preserve complete financial history.

---

# Migration Strategy

All schema changes must be version controlled.

Flyway will manage:

- schema creation
- schema evolution
- seed data

Manual production schema modifications are prohibited.

---

# Transaction Management

Application transactions should remain small.

Long-running transactions should be avoided.

Business workflows involving multiple aggregates should be coordinated by the application layer.

---

# Scalability

The schema should support future features without redesign.

Examples

- multiple users
- multiple currencies
- investments
- budgets
- recurring transactions
- notifications
- AI insights

---

# Performance Philosophy

Correctness takes priority over optimization.

Optimization should be driven by measurement rather than assumptions.

Premature optimization should be avoided.

---

# Data Ownership

Each Aggregate owns its corresponding tables.

Examples

Account Aggregate

↓

accounts

Transaction Aggregate

↓

transactions

Loan Aggregate

↓

loans

Fund Aggregate

↓

funds

Cross-aggregate ownership is prohibited.

---

# Historical Data

Historical records should remain queryable indefinitely.

Future archival mechanisms may move old data to archive tables without changing the business model.

---

# Versioning

Schema changes should always be:

- backward compatible whenever practical
- incremental
- documented

Breaking changes require new migrations.

Existing migrations must never be edited after release.

---

# Security

Sensitive data should never be stored unless required.

Future enhancements

- encryption
- row-level security
- audit logging

---

# Guiding Principles

1. Domain drives database design.
2. Financial history is immutable.
3. UUID is the preferred identifier.
4. Money uses NUMERIC.
5. Constraints protect integrity.
6. Flyway manages schema evolution.
7. Reports derive from transactional data.
8. Business logic belongs in the application layer.
9. Optimization follows measurement.
10. The schema should evolve without requiring redesign.

---

# Final Statement

The database is designed as a durable persistence layer for the financial domain.

Its responsibility is to preserve correctness, maintain referential integrity, and support future evolution without sacrificing simplicity.

Business behavior belongs in the domain model, while the database provides a stable and reliable foundation for storing financial history.