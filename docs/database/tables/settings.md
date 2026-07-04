# Settings Table Specification

## Purpose

The `settings` table stores global application configuration and user preferences.

Version 1 is designed for a single-user application.

Only one settings record exists in the database.

The settings table does not store financial data.

Instead, it stores configuration used by the application.

---

# Aggregate

Settings Aggregate

Aggregate Root

ApplicationSettings

---

# Responsibilities

- Store application preferences
- Store reporting preferences
- Store salary cycle configuration
- Store backup configuration
- Store synchronization preferences
- Store UI preferences

---

# Table Definition

Table Name

settings

Primary Key

id

---

# Columns

| Column | Type | Nullable | Default | Description |
|---------|------|----------|---------|-------------|
| id | UUID | No | Generated | Primary Key |
| currency | VARCHAR(10) | No | BDT | Default currency |
| locale | VARCHAR(20) | No | en-BD | Regional locale |
| timezone | VARCHAR(50) | No | Asia/Dhaka | Application timezone |
| salary_day | SMALLINT | No | 10 | Expected salary day |
| fiscal_year_start_month | SMALLINT | No | 1 | Fiscal year start month |
| default_cash_account_id | UUID | Yes | | Default cash account |
| auto_create_salary_cycles | BOOLEAN | No | TRUE | Automatically create salary cycles |
| enable_google_drive_backup | BOOLEAN | No | FALSE | Enable Google Drive backup |
| enable_auto_reconciliation | BOOLEAN | No | TRUE | Enable cash reconciliation workflow |
| theme | VARCHAR(20) | No | SYSTEM | LIGHT / DARK / SYSTEM |
| created_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | No | CURRENT_TIMESTAMP | Last update |

---

# Primary Key

id

UUID

---

# Foreign Keys

default_cash_account_id

↓

accounts(id)

(Optional)

---

# Constraints

## NOT NULL

- id
- currency
- locale
- timezone
- salary_day
- fiscal_year_start_month
- auto_create_salary_cycles
- enable_google_drive_backup
- enable_auto_reconciliation
- theme
- created_at
- updated_at

---

## CHECK

salary_day

Allowed values

1–31

---

fiscal_year_start_month

Allowed values

1–12

---

theme

Allowed values

- LIGHT
- DARK
- SYSTEM

---

# Singleton Rule

Version 1 allows only one row.

The application always reads and updates this single configuration record.

---

# Relationships

Settings

↓

Application Configuration

↓

All Modules

---

# Business Rules

- Settings do not contain transactional data.
- Changes affect future behavior only.
- Historical reports remain unchanged.
- Settings are editable at any time.
- Configuration is loaded during application startup.

---

# Reporting Usage

Settings influence

- Currency formatting
- Date formatting
- Salary cycle generation
- Dashboard preferences
- Backup configuration

They do not directly appear in financial reports.

---

# Example Record

| Field | Value |
|--------|-------|
| Currency | BDT |
| Locale | en-BD |
| Timezone | Asia/Dhaka |
| Salary Day | 10 |
| Theme | SYSTEM |
| Google Drive Backup | TRUE |

---

# Future Enhancements

Possible additions

- Preferred Language
- Number Formatting
- Email Notifications
- Push Notifications
- Default Dashboard
- Export Preferences
- Security Settings
- Two-Factor Authentication
- OCR Preferences
- AI Assistant Preferences

---

# Design Decisions

- Single-user configuration.
- One row only.
- Configuration is separate from financial data.
- Default values minimize setup.
- Settings are extensible for future features.

---

# Final Statement

The `settings` table centralizes application-wide configuration while remaining independent from the financial domain.

This separation keeps the business model clean, simplifies future enhancements, and allows application behavior to be customized without affecting historical financial records.