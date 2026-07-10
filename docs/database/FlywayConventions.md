    
# Flyway Conventions

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document defines the conventions used for all Flyway database migrations in the Personal Finance Application.

The goal is to keep every migration predictable, readable, reviewable and consistent throughout the lifetime of the project.

---

# Guiding Principles

- Documentation-first development
- Forward-only migrations
- Ledger-first database design
- Explicit over implicit
- Consistent naming
- Immutable migration history

---

# Migration Naming

Use Flyway's standard naming convention:

```
V1__initial_schema.sql
V2__seed_default_categories.sql
V3__default_settings.sql
```

Migration names should be lowercase with words separated by underscores.

---

# Table Naming

Rules:

- lowercase
- snake_case
- plural nouns

Examples:

- accounts
- categories
- salary_cycles
- transactions
- funds
- loans

---

# Column Naming

Rules:

- lowercase
- snake_case
- descriptive names
- foreign keys end with `_id`

Examples:

- transaction_date
- from_account_id
- salary_cycle_id
- created_at

---

# Primary Keys

- UUID for all primary keys
- Column name: `id`
- Primary keys are immutable

---

# Foreign Keys

Foreign key naming:

```
fk_<table>_<referenced_table>
```

Example:

```
fk_transactions_accounts
```

---

# Constraint Naming

Patterns:

```
pk_<table>
fk_<table>_<reference>
chk_<table>_<rule>
uk_<table>_<column>
```

Examples:

```
pk_transactions
chk_transactions_amount_positive
```

---

# Index Naming

Patterns:

```
idx_<table>_<column>
idx_<table>_<column1>_<column2>
```

Examples:

```
idx_transactions_transaction_date
idx_transactions_salary_cycle_date
```

---

# Data Types

- UUID
- NUMERIC(18,2)
- DATE
- TIMESTAMP
- TEXT
- VARCHAR(n)
- BOOLEAN

Avoid floating-point types for financial values.

---

# Audit Columns

Every business table should include:

- created_at
- updated_at

Future versions may introduce created_by and updated_by.

---

# Default Values

Use sensible defaults only when they represent universal behaviour.

Avoid business-specific defaults in the database.

---

# Soft Delete Policy

Version 1 does not use soft deletes.

Historical financial records remain immutable.

Corrections are performed using new ledger transactions.

---

# Enum Strategy

Application enums and database values must remain synchronized.

Adding a new enum value requires a new Flyway migration.

---

# Timestamp Strategy

Store timestamps consistently using PostgreSQL TIMESTAMP.

Application code is responsible for timezone handling.

---

# Migration Guidelines

- Never modify an executed migration.
- Create a new migration for every schema change.
- Keep migrations idempotent where appropriate.
- One logical change per migration.
- Review SQL before execution.

---

# Rollback Philosophy

Production migrations are forward-only.

Rollback is performed through corrective migrations rather than editing history.

---

# Best Practices

- Keep migrations small.
- Keep names descriptive.
- Add constraints after table creation when appropriate.
- Create indexes based on real query patterns.
- Keep documentation synchronized with schema.

---

# Final Checklist

Before merging a migration:

- Naming follows conventions.
- Constraints are documented.
- Indexes are justified.
- Documentation is updated.
- Migration reviewed.
- Flyway validates successfully.