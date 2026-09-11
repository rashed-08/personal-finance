# Restore Strategy

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document explains what restoring does, what guards stand in front of it, and how to recover in the cases
the happy path does not cover.

Read `docs/operations/BackupStrategy.md` first — what a restore does depends entirely on which format wrote
the archive.

---

# Restoring Replaces Data

A restore is not a merge. It deletes the existing contents of the affected tables and loads the archive's
contents in their place. There is no undo inside the application.

A merge mode was considered and rejected: inserting missing rows and updating matching ids silently produces a
state that matches neither the archive nor the prior data. For a ledger, a hybrid is worse than either
outcome.

## What each format replaces

| Format | Replaced | Left alone |
|--------|----------|------------|
| `JSON` | accounts, categories, salary cycles, funds, loans, transactions, cash reconciliations and snapshots, recurring transactions and executions, settings | Users and passwords, sessions, Drive credential, backup history, Flyway history. **Your login keeps working** |
| `PG_DUMP` | The entire database — every table in the dump | Nothing. Sessions are invalidated, passwords revert to the dump's, and the Flyway history reverts too |

After a `PG_DUMP` restore, **restart the application**: the recorded schema version now matches the dump, and
Flyway needs a fresh start to migrate forward again.

---

# The Guards

Three conditions must hold before any data is touched. All three are enforced server-side in
`RestoreBackupService`; the UI mirrors them but is not what enforces them.

## 1. An exact confirmation phrase

The request must carry `confirmation` equal to the server's phrase — currently `REPLACE ALL DATA`, published
at `GET /api/backups/configuration` so clients never hardcode it.

A typed phrase rather than a boolean flag, specifically so that an empty or default-constructed request body
can never authorize replacing the ledger.

## 2. A matching checksum

The archive's SHA-256 is compared against the value recorded when it was written. A mismatch is refused with
`400` and recorded as a failed restore.

An archive recorded *without* a checksum — from a version predating them — is allowed through as unverified,
so old backups remain restorable.

Uploaded archives have no recorded checksum to compare against. Integrity there rests on the archive parsing
correctly, and the confirmation dialog says as much.

## 3. A readable archive

The archive must parse in its declared format. A zip without a `backup.json`, a truncated file, or an archive
written by a *newer* `SCHEMA_VERSION` than this build understands are all refused before any delete happens.

---

# Atomicity

The `JSON` restore runs every delete and every insert inside one transaction (`JsonBackupImporter` is
`@Transactional`). A failure part-way — a constraint violation, a type mismatch, a dropped connection — rolls
the whole thing back and leaves the existing ledger exactly as it was.

`PG_DUMP` gets the same property from `pg_restore --single-transaction`.

Deletes go children-before-parents (the reverse of `BackupTables.insertOrder()`), so a plain `DELETE` never
trips a foreign key. `TRUNCATE` is deliberately not used: it would need `CASCADE`, which reaches tables the
backup excludes.

## The history record is outside that transaction

Deliberately. `RestoreBackupService` is *not* `@Transactional`, because a rollback would erase the record of
the failure it is rolling back. The sequence is:

```
write RESTORE history row (IN_PROGRESS)
    ↓
[ transaction: delete + load ]          ← rolls back on failure
    ↓
close history row (COMPLETED or FAILED) ← survives either way
```

---

# How to Restore

## From history — the normal case

1. Open **Backup** in the sidebar.
2. Find the backup in the history table and press **Restore**.
3. Read the warning, type the confirmation phrase, confirm.

Only completed backups offer the button. A restore entry cannot itself be restored.

## From a file

For an archive downloaded from Drive by hand, or copied from another machine:

1. Open **Backup**.
2. Under **Restore**, choose the `.zip` or `.dump` file.
3. Type the confirmation phrase and confirm.

The format is inferred from the extension. The upload limit is 256 MB (`MAX_BACKUP_UPLOAD_SIZE`).

## From the command line

```
scripts/restore.sh --file storage/backups/finance-backup-2026-09-11T143207.zip
```

The script requires the same confirmation and will prompt for it.

---

# Recovering When the Application Will Not Start

The endpoints above need a running application. When that is the thing that is broken, restore underneath it.

## A PG_DUMP archive

```
pg_restore \
  --host=localhost --port=5432 --username=postgres \
  --dbname=personal_finance \
  --clean --if-exists --no-owner --no-privileges --single-transaction \
  finance-backup-2026-09-11T143207.dump
```

Then start the application so Flyway can migrate forward from whatever version the dump recorded.

## A JSON archive

There is no external loader for the JSON format — the importer is the application. Recover in two steps:

1. Bring the application up against an empty database (`scripts/db-reset.sh` recreates one; Flyway builds the
   schema on startup).
2. Restore the archive through the UI or `scripts/restore.sh`.

The archive is a plain zip containing `backup.json`, so the data is readable and recoverable by hand if it ever
comes to that.

---

# After a Restore

- **Re-check balances.** Every balance is derived from the ledger, never stored, so they recompute
  automatically — but the dashboard is the quickest confirmation the ledger came back whole.
- **`JSON`**: settings were replaced too, so `BACKUP_PROVIDER`, `AUTO_BACKUP_ENABLED` and the rest are now the
  archive's values. Re-check them before relying on the next scheduled backup.
- **`PG_DUMP`**: restart the application. Sign in again; credentials are the dump's.
- **Take a fresh backup.** The restored state is now the state worth protecting.

---

# Known Limitations

- **A restore cannot be undone.** Take a backup before restoring one if the current data has any value.
- **A restore is not a point-in-time recovery.** You get the archive's contents, not "the database as of
  14:31". Postgres PITR is a different mechanism and is not configured here.
- **Concurrent use during a restore is not guarded.** Nothing stops another request writing while the restore
  transaction runs; that write will be lost or will fail. This is a single-user application and the restore
  takes seconds, so no lock was added — but do not restore while something else is importing.
- **`PG_DUMP` restores log everyone out**, including the person performing the restore.

---

# Related Documentation

- `docs/operations/BackupStrategy.md`
- `docs/api/Backup.md`
- `docs/database/tables/backup_history.md`
