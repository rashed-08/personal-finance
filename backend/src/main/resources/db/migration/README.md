# Flyway Database Migrations

This directory contains all database migrations managed by Flyway.

Each migration is immutable once it has been executed in a shared environment.

---

# Migration Order

| Version | Description |
|----------|-------------|
| V1 | Initial database schema (all tables, constraints, triggers, indexes) |
| V2 | Seed data (default categories, application settings, starter funds) |
| V3+ | Future schema/data changes |

Consolidated from a longer chain of incremental migrations (formerly V1–V10) while the project is still in
development and every local database can be recreated from scratch. Schema fixes that were originally separate
migrations (nullable `salary_cycles.cycle_end_date` and `transactions.salary_cycle_id`, `reference_transaction_id`,
`migration_batch_id`/`reconciliation_batch_id` as VARCHAR, `fund_id` + its constraints, `funds.target_amount > 0`)
are now folded directly into V1. Data seeding (categories, settings, funds) is now a single V2. This is safe only
because no shared/production environment has these migrations applied yet — never do this once a migration has
run anywhere outside local development.

---

# Rules

- Never modify an executed migration.
- Create a new migration for every schema change.
- Keep one logical change per migration.
- Review SQL before committing.
- Follow the conventions defined in `docs/database/FlywayConventions.md`.

---

# Naming Convention

```
V1__initial_schema.sql
V2__seed_data.sql
V3__add_recurring_transactions_index.sql
```

Use:

```
V<version>__<description>.sql
```

Descriptions must use lowercase letters and underscores.

---

# Migration Workflow

```
Documentation

↓

Flyway Migration

↓

Database Validation

↓

Java Entity

↓

Repository

↓

Domain Service

↓

REST API
```

---

# Related Documentation

- `docs/database/FlywayConventions.md`
- `docs/database/Schema.md`
- `docs/database/ERD.md`
- `docs/database/DatabaseDesignPrinciples.md`

---

# Final Statement

Flyway migrations represent the executable version of the database design.

All schema changes must be implemented through versioned migrations to preserve database history and ensure reproducible deployments.