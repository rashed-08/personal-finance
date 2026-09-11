# Settings API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for reading and changing application configuration.

Settings hold configuration only — never financial data. A change therefore affects future behaviour and never
rewrites history: recorded transactions and past reports are unaffected (see `docs/database/tables/settings.md`).

Base path

```
/api/settings
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

Authentication is required, as for every endpoint outside `/api/auth/**`.

---

# Data Model

Settings are stored key-value rather than as typed columns: one row per setting, with the value held as text and
a `valueType` saying how to read it.

> **Note on `docs/database/tables/settings.md`**
>
> That specification describes a different design — a single row with typed columns (`currency`, `locale`,
> `salary_day`, `theme`, …). The shipped schema (`V1__initial_schema.sql`) implements the key-value form, and
> `V2__seed_data.sql` seeds nine keys into it. This API documents what is actually built. The table
> specification is the older design and has not been reconciled.

Keys are append-only. Renaming one silently reverts that setting to its code-side default, so a rename is a
migration plus a data copy — not an edit.

## Value Types

| `valueType` | Accepted values | Read as |
|-------------|-----------------|---------|
| `STRING` | Any text | String |
| `INTEGER` | A base-10 long | `long` / `int` |
| `DECIMAL` | A decimal number | `BigDecimal` |
| `BOOLEAN` | `true` or `false`, any case | `boolean` |
| `DATE` | ISO-8601 date, `2026-07-25` | `LocalDate` |
| `JSON` | Stored verbatim, not validated | String |

A value that does not parse as its declared type is rejected with `400`. A `null` or blank value means
**unset**: readers fall back to the default defined in `SettingKeys`, so a missing or cleared setting can never
stop a feature working.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/settings` | List every setting |
| PUT | `/api/settings` | Change one or more settings, atomically |

---

# GET /api/settings

Returns all settings, ordered by key.

## Response — `200 OK`

```json
[
  {
    "key": "AUTO_BACKUP_ENABLED",
    "value": "false",
    "valueType": "BOOLEAN",
    "description": "Enable scheduled automatic backups.",
    "system": true,
    "updatedAt": "2026-09-11T14:32:07"
  },
  {
    "key": "BACKUP_RETENTION_COUNT",
    "value": "30",
    "valueType": "INTEGER",
    "description": "Number of automatic backups to keep. Older archives are pruned; 0 disables pruning.",
    "system": true,
    "updatedAt": "2026-09-11T14:32:07"
  }
]
```

| Field | Type | Notes |
|-------|------|-------|
| `key` | string | Stable identifier, e.g. `DEFAULT_CURRENCY` |
| `value` | string, nullable | Raw stored text. `null` means unset |
| `valueType` | enum | How to interpret `value` |
| `description` | string, nullable | What the setting does |
| `system` | boolean | Seeded by migration and understood by code: editable, never deletable |
| `updatedAt` | timestamp | Last change |

---

# PUT /api/settings

Applies a batch of changes.

## Request Body

```json
{
  "values": {
    "AUTO_BACKUP_ENABLED": "true",
    "BACKUP_PROVIDER": "GOOGLE_DRIVE",
    "BACKUP_RETENTION_COUNT": "14"
  }
}
```

| Field | Type | Notes |
|-------|------|-------|
| `values` | object, required | Key to new value. Must contain at least one entry |

Pass `null` as a value to unset that setting.

## Atomicity

The batch is **all-or-nothing**. The settings screen submits several keys at once, and a partial apply would
leave a configuration nobody chose — auto-backup switched on while the provider it needs was rejected, for
example. One invalid value rolls the whole request back.

## Response — `200 OK`

Returns only the settings that changed, in the order given.

```json
[
  {
    "key": "AUTO_BACKUP_ENABLED",
    "value": "true",
    "valueType": "BOOLEAN",
    "description": "Enable scheduled automatic backups.",
    "system": true,
    "updatedAt": "2026-09-11T15:02:41"
  }
]
```

## Errors

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | A value does not parse as its declared `valueType`, or `values` is empty |
| `404 Not Found` | A key does not exist. Keys are seeded by migration, so an unknown key is a client bug rather than a reason to create a row no code will read |

---

# Settings Reference

## General

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `DEFAULT_CURRENCY` | `STRING` | `BDT` | Default currency for the application |
| `DATE_FORMAT` | `STRING` | `yyyy-MM-dd` | Default date format |
| `FIRST_DAY_OF_WEEK` | `STRING` | `SATURDAY` | First day of week used by reports and calendars |

## Salary Cycle

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `AUTO_ASSIGN_SALARY_CYCLE` | `BOOLEAN` | `true` | Assign new transactions to a salary cycle automatically |
| `ENABLE_CARRY_FORWARD` | `BOOLEAN` | `true` | Carry leftover money between salary cycles |

## Workflows

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `ENABLE_CASH_RECONCILIATION` | `BOOLEAN` | `true` | Enable the cash reconciliation workflow |
| `ENABLE_RECURRING_TRANSACTIONS` | `BOOLEAN` | `true` | Enable the recurring transaction scheduler |

## Backup

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `AUTO_BACKUP_ENABLED` | `BOOLEAN` | `false` | Whether the scheduled backup does anything |
| `AUTO_BACKUP_CRON` | `STRING` | `0 0 2 * * *` | Informational. The schedule Spring actually uses is `app.backup.cron`, read once at startup |
| `BACKUP_PROVIDER` | `STRING` | `LOCAL` | `LOCAL` or `GOOGLE_DRIVE` |
| `BACKUP_FORMAT` | `STRING` | `JSON` | `JSON` or `PG_DUMP` |
| `BACKUP_DIRECTORY` | `STRING` | `storage/backups` | Where local archives are written |
| `BACKUP_RETENTION_COUNT` | `INTEGER` | `30` | Automatic backups kept before pruning; `0` disables pruning |
| `GOOGLE_DRIVE_FOLDER_NAME` | `STRING` | `PersonalFinanceApp` | Root Drive folder for uploaded archives |

Defaults live in `SettingKeys` as well as in the seed migration. The code-side default is what applies when a
key is missing or blank, so the application behaves sensibly against a partially seeded database.

---

# Related Documentation

- `docs/api/Backup.md`
- `docs/database/tables/settings.md`
- `docs/operations/BackupStrategy.md`
