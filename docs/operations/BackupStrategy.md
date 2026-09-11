# Backup Strategy

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document explains how the application backs up its data, what each backup does and does not contain, and
which trade-offs were chosen deliberately.

The restore side is documented separately in `docs/operations/RestoreStrategy.md`.

---

# Principles

- **A backup you cannot restore is not a backup.** Every format has a matching importer, and the round trip
  is covered by an integration test (`JsonBackupRoundTripIntegrationTest`).
- **Evidence over silence.** A history row is written before the work starts, so a crash mid-backup leaves an
  `IN_PROGRESS` record. Failures are kept, never overwritten.
- **Integrity is checked, not assumed.** Every archive's SHA-256 is recorded on write and verified before a
  restore reads it.
- **Credentials never travel.** No archive contains password hashes or session tokens.
- **Nothing leaves the machine by default.** The default provider is `LOCAL`. Uploading to Google Drive
  requires an explicit, revocable grant.

---

# The Two Formats

The application ships two archive formats because they answer different questions. Neither is strictly better.

## JSON — the default

An application-level export: every backed-up table read over JDBC, serialized to one JSON document, zipped.

**Contains** the tables listed in `BackupTables`:

```
accounts, categories, salary_cycles, funds, loans, transactions,
cash_reconciliations, cash_snapshots, recurring_transactions,
recurring_transaction_executions, settings
```

**Deliberately excludes**

| Excluded | Why |
|----------|-----|
| `users`, `refresh_tokens`, `email_verification_tokens`, `password_reset_tokens`, `google_oauth_tokens` | Credentials and session state. No financial table references a user, so none of this is needed for referential integrity — and an archive that may travel to Google Drive has no business carrying password hashes or live tokens. A restore therefore leaves your login working |
| `backup_history` | The audit trail must survive the restore that is writing to it. Including it would roll history back to the archive's state and erase the record of the restore in progress |
| `flyway_schema_history` | Owned by Flyway. Restoring it would desynchronise the recorded schema version from the actual schema |

**Why every value is stored as text.** Postgres renders each type to text losslessly and casts it back on the
way in (`?::numeric`, `?::uuid`, `?::timestamp`). That keeps the archive free of a type map of its own and —
the part that matters for a money ledger — introduces no rounding. `NUMERIC(18,2)` comes back at exactly the
same scale.

**Schema drift.** The archive records the column names it captured; the importer inserts only columns that
still exist. A column added since the backup falls back to its database default; one dropped since is skipped
with a warning. A *renamed or retyped* column is not something this can paper over, which is what
`DatabaseSnapshot.SCHEMA_VERSION` is for: a future importer can detect and refuse an archive it would
mis-restore.

**Self-references.** `transactions.reference_transaction_id` points at another row in the same table, so the
importer nulls it on insert and links it in a second pass once every row exists. This avoids needing deferrable
constraints or elevated privileges.

## PG_DUMP — the faithful one

A physical `pg_dump --format=custom` dump of the entire database, schema included.

**Contains everything** — all tables, including the ones JSON excludes.

**Requires** the PostgreSQL client tools on the server host, at a version at least that of the server. Set
`app.backup.postgres.pg-dump` / `pg-restore` to absolute paths when they are not on `PATH`. The password is
passed through the environment (`PGPASSWORD`), never on a command line where a process list would expose it.

## Choosing

| If you want… | Use |
|--------------|-----|
| To recover the ledger without disturbing accounts, sessions or audit history | `JSON` |
| A portable archive you can read, diff, or load into something else | `JSON` |
| No dependency on external binaries | `JSON` |
| A byte-faithful image of the database, schema included | `PG_DUMP` |
| To move the whole installation to another machine | `PG_DUMP` |

Set the choice with the `BACKUP_FORMAT` setting. Either format can also be selected for a single run via
`POST /api/backups`.

---

# Destinations

## LOCAL

Archives are written to `BACKUP_DIRECTORY` (default `storage/backups`, relative to the application's working
directory). The directory is created on demand. `storage/` is already gitignored.

A local backup on the same disk as the database protects against application-level mistakes — a bad import, a
mis-entered reconciliation — but not against losing the machine. Download archives, or use Drive, for that.

## GOOGLE_DRIVE

Archives are uploaded to `<GOOGLE_DRIVE_FOLDER_NAME>/backups/<year>/<month>/`, the layout described in
`docs/database/tables/backup_history.md`. Folders are created on demand.

The OAuth scope is `drive.file`: the application can see and manage only the files it created, never the rest
of the Drive. Backups stay visible in Drive so they can be downloaded by hand — `drive.appdata` was rejected
for that reason.

**This uploads your complete financial history to Google.** It is off by default and requires an explicit
grant, which can be revoked from the backup screen or from your Google account.

### Credential handling

The Drive refresh token has to be replayed to Google, so unlike the application's own refresh tokens it cannot
be hashed. It is stored encrypted with AES-256-GCM under `ENCRYPTION_SECRET` (`SecretCipher`). GCM
authenticates, so tampering is detected on decrypt rather than producing garbage that gets sent to Google.
Access tokens last an hour, are derived on demand, and are never persisted.

Without `ENCRYPTION_SECRET` the application runs normally and only Drive backup is unavailable, with a message
saying so.

## S3

Declared in the `BackupProvider` enum, not implemented. Requesting it returns `503` naming the gap.

---

# Manual and Automatic Backups

## Manual

`POST /api/backups`, or the **Back up now** button on the backup screen. Recorded as `MANUAL`. Never pruned by
retention — someone took it on purpose.

## Automatic

A scheduled job, gated by two separate switches on purpose:

| Control | Where | Effect |
|---------|-------|--------|
| `app.backup.cron` | Server configuration | When the job fires. Default `0 0 2 * * *` (02:00 daily). Read once at startup |
| `AUTO_BACKUP_ENABLED` | Settings table | Whether a firing does anything. Takes effect immediately |

The split exists because Spring binds a cron expression at startup: a schedule stored in the database could
not take effect until a restart anyway, whereas an on/off switch can.

Retention keeps the newest `BACKUP_RETENTION_COUNT` completed automatic backups (default 30) and deletes the
archives behind the rest. `0` disables pruning. One unreachable archive does not stop the others being pruned,
nor fail the backup that just succeeded.

**History entries always survive pruning.** Only archives are deleted, so a pruned backup stays
distinguishable from one that never happened.

---

# Operating the Backups

## Recommended routine

1. Leave `BACKUP_PROVIDER=LOCAL` and `BACKUP_FORMAT=JSON`.
2. Switch `AUTO_BACKUP_ENABLED` on.
3. Connect Google Drive, or periodically download an archive to somewhere off the machine.
4. Every few months, actually restore one into a scratch database. An untested backup is a guess.

## From the command line

`scripts/backup.sh` and `scripts/restore.sh` drive the API, for cron jobs and for use when the UI is
unavailable.

## Checking on it

- `GET /api/backups` — full history, successes and failures.
- The backup screen shows the same, with sizes, checksums and error messages.
- Failures are logged at `ERROR` by `CreateBackupService` with the provider's own message.

---

# What This Does Not Do

Stated plainly, so none of it comes as a surprise:

- **No encryption of the archive itself.** A `JSON` archive is a readable zip. Anyone with the file has your
  financial history. Treat archives as sensitive; encrypt them yourself before moving them somewhere
  untrusted.
- **No incremental backups.** Every run writes a complete archive. At personal-ledger sizes this is cheap and
  much easier to reason about.
- **No streaming.** Archives are held whole in memory while being written, checksummed and stored. Fine for
  megabytes; the type to turn into a stream if the ledger ever outgrows that is `BackupArchive`.
- **No automatic restore verification.** A backup's checksum is recorded, not re-read after writing.
- **No cross-version migration of old archives.** `SCHEMA_VERSION` lets a future importer *detect* an archive
  it cannot read; it does not transform one.

---

# Related Documentation

- `docs/operations/RestoreStrategy.md`
- `docs/api/Backup.md`
- `docs/api/Settings.md`
- `docs/database/tables/backup_history.md`
