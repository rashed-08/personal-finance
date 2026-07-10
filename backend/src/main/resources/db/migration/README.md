# Flyway Database Migrations

This directory contains all database migrations managed by Flyway.

Each migration is immutable once it has been executed in a shared environment.

---

# Migration Order

| Version | Description |
|----------|-------------|
| V1 | Initial database schema |
| V2 | Seed default categories |
| V3 | Default application settings |
| V4+ | Future schema changes |

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
V2__seed_default_categories.sql
V3__default_settings.sql
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