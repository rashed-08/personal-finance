# Backup API

Version: 1.0

Status: Draft

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for taking backups, restoring them, auditing both, and connecting Google
Drive as a storage destination.

Base path

```
/api/backups
```

All request and response bodies are JSON unless stated otherwise. Errors follow
[RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807), returned as `application/problem+json` by
`GlobalExceptionHandler`.

Authentication is required, as for every endpoint outside `/api/auth/**`.

> **Restoring destroys data.** `POST /api/backups/{id}/restore` and `POST /api/backups/restore` replace the
> contents of the database. Read [Restore](#restore) before calling either.

---

# Concepts

## Format — what an archive contains

| Format | Produced by | Contents | Restore behaviour |
|--------|-------------|----------|-------------------|
| `JSON` | Application export, zipped | Only the tables in `BackupTables`: accounts, categories, salary cycles, funds, loans, transactions, cash reconciliations and snapshots, recurring transactions and executions, settings | Replaces those tables. Users, auth tokens, backup history and the Flyway history are untouched, so your login keeps working |
| `PG_DUMP` | `pg_dump --format=custom` | The whole database, schema included | `pg_restore --clean` drops and recreates everything in the dump — users, tokens, history and Flyway state included. Sessions are invalidated and the schema version reverts to the dump's |

`JSON` needs no external tooling. `PG_DUMP` requires the PostgreSQL client tools on the server host, at a
version at least that of the server; set `app.backup.postgres.pg-dump` / `pg-restore` when they are not on
`PATH`.

The format is recorded per operation rather than read from settings at restore time: an archive must be read
by the writer that produced it, and the configured default may have changed since.

## Provider — where an archive lives

| Provider | Notes |
|----------|-------|
| `LOCAL` | Files under `BACKUP_DIRECTORY` (default `storage/backups`). Always available |
| `GOOGLE_DRIVE` | Uploaded to `<GOOGLE_DRIVE_FOLDER_NAME>/backups/<year>/<month>/`. Requires server credentials, an encryption key, and a connected account |
| `S3` | Declared in the enum, not implemented. Requesting it returns `503` |

## Checksums

Every archive's SHA-256 is recorded when it is written and re-verified before a restore reads it. A mismatch
is refused with `400` — a corrupted archive must not reach the database. An archive recorded without a
checksum (from an older version) is allowed through as unverified.

## History

Every backup and every restore writes one `backup_history` row. The row is opened as `IN_PROGRESS` *before*
the work starts and closed as `COMPLETED` or `FAILED` afterwards, so a crash mid-operation leaves evidence
rather than silence. History is append-only: failures are kept, and deleting an archive leaves its entry in
place.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/backups` | Take a backup now |
| GET | `/api/backups` | List backup and restore history |
| GET | `/api/backups/configuration` | Everything the backup screen needs to render |
| GET | `/api/backups/{id}/download` | Download a stored archive |
| DELETE | `/api/backups/{id}` | Delete a stored archive, keeping its history entry |
| POST | `/api/backups/{id}/restore` | **Destructive.** Restore a backup from history |
| POST | `/api/backups/restore` | **Destructive.** Restore an uploaded archive |
| POST | `/api/backups/google-drive/authorize` | Start the Drive consent flow |
| POST | `/api/backups/google-drive/callback` | Finish it with Google's code |
| GET | `/api/backups/google-drive` | Drive connection status |
| DELETE | `/api/backups/google-drive` | Disconnect and revoke Drive access |

---

# POST /api/backups

Takes a backup immediately, recorded as `MANUAL`.

## Request Body

Optional. Omit it entirely to use the configured provider and format.

```json
{
  "provider": "LOCAL",
  "format": "JSON"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `provider` | enum, optional | One-off override of `BACKUP_PROVIDER` |
| `format` | enum, optional | One-off override of `BACKUP_FORMAT` |

## Response — `201 Created`

See [Backup Response Shape](#backup-response-shape).

## Errors

| Status | Condition |
|--------|-----------|
| `503 Service Unavailable` | The provider is unusable — Drive not connected, no encryption key, `pg_dump` missing, upload failed, or `S3` requested. The message says which |

A provider that is unavailable is refused **before** any history is written: an unconfigured destination is a
configuration problem, not a failed backup attempt. A failure *during* export or upload is recorded as
`FAILED` and then returned as `503`.

---

# GET /api/backups

## Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `operation` | enum, optional | `BACKUP` or `RESTORE`. Omit for both |

## Response — `200 OK`

An array of [backup responses](#backup-response-shape), newest first.

---

# GET /api/backups/configuration

One request for everything the backup screen needs.

## Response — `200 OK`

```json
{
  "provider": "LOCAL",
  "format": "JSON",
  "autoBackupEnabled": false,
  "retentionCount": 30,
  "confirmationPhrase": "REPLACE ALL DATA",
  "googleDrive": {
    "configured": true,
    "connected": false,
    "accountEmail": null,
    "folderName": "PersonalFinanceApp",
    "detail": "Google Drive is not connected. Connect it from Settings to enable Drive backups."
  }
}
```

| Field | Type | Notes |
|-------|------|-------|
| `provider` | enum | Configured destination |
| `format` | enum | Configured archive format |
| `autoBackupEnabled` | boolean | Whether the schedule is armed |
| `retentionCount` | integer | Automatic backups kept before pruning |
| `confirmationPhrase` | string | Exact text a restore request must echo. Read it from here rather than hardcoding it |
| `googleDrive.configured` | boolean | Whether the server has OAuth credentials. `false` is not user-fixable |
| `googleDrive.connected` | boolean | Whether a usable grant is stored |
| `googleDrive.detail` | string, nullable | Why Drive is unusable, when it is |

---

# GET /api/backups/{id}/download

Returns the archive bytes.

## Response — `200 OK`

```
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="finance-backup-2026-09-11T143207.zip"
```

The endpoint is authenticated, so a plain anchor tag will get a `401` — fetch it with the access token
attached and hand the blob to the browser.

A checksum mismatch is logged but does not block the download: you may be downloading precisely because
something is wrong.

## Errors

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | The entry is a restore, or a backup that failed or is still running — there is no archive |
| `404 Not Found` | No such history entry |
| `503 Service Unavailable` | The archive is missing from storage or could not be read |

---

# DELETE /api/backups/{id}

Deletes the stored archive. **The history entry is kept** — history is append-only, so a deleted backup stays
distinguishable from one that never happened.

## Response — `204 No Content`

Deleting an archive that is already gone succeeds: absence is the requested outcome.

---

# Restore

Both restore endpoints replace the contents of the database. Three things must hold before any data is
touched:

1. the request repeats `confirmationPhrase` **exactly** — a typed phrase, not a boolean, so an empty or
   default-constructed body can never authorize it;
2. the archive's SHA-256 matches the recorded checksum (history restores only);
3. the archive parses in its declared format.

The data load runs in a single transaction for `JSON` (`--single-transaction` for `PG_DUMP`), so a failure
part-way leaves the existing ledger exactly as it was. The history entry is deliberately written outside that
transaction, so a failed restore is still recorded.

## POST /api/backups/{id}/restore

Restores an archive this application stored.

### Request Body

```json
{
  "confirmation": "REPLACE ALL DATA"
}
```

### Response — `200 OK`

The `RESTORE` history entry (see [Backup Response Shape](#backup-response-shape)).

### Errors

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Wrong confirmation, checksum mismatch, or the entry is not a completed backup |
| `404 Not Found` | No such history entry |
| `503 Service Unavailable` | The archive could not be read, or the load failed |

## POST /api/backups/restore

Restores from an uploaded archive — one downloaded from Drive by hand, or copied from another machine.

`multipart/form-data`:

| Part | Type | Notes |
|------|------|-------|
| `file` | file, required | A `.zip` JSON export or a `.dump` PostgreSQL dump. The format is inferred from the extension |
| `confirmation` | text, required | Must equal `confirmationPhrase` |

There is no recorded checksum to compare against, so integrity rests on the archive parsing correctly. The
provider is recorded as `LOCAL`.

The upload limit is 256 MB by default (`MAX_BACKUP_UPLOAD_SIZE`).

---

# Google Drive

Connecting Drive uploads your complete financial history to Google. Nothing is uploaded until a grant exists
*and* `BACKUP_PROVIDER` is `GOOGLE_DRIVE`.

## Server prerequisites

| Variable | Purpose |
|----------|---------|
| `GOOGLE_CLIENT_ID` | OAuth 2.0 Web client ID. Shared with Google Sign-In |
| `GOOGLE_CLIENT_SECRET` | Client secret. Needed for the authorization-code flow; Sign-In does not use it |
| `GOOGLE_DRIVE_REDIRECT_URI` | Must match the Google Cloud console entry exactly. Defaults to `http://localhost:5173/settings/google-drive/callback` |
| `ENCRYPTION_SECRET` | AES key for the stored refresh token. Without it, Drive backup is unavailable |

The requested scope is `drive.file` — the application can see and manage only the files it created, not the
rest of your Drive. `openid email` rides along so the connected account's address can be shown.

## POST /api/backups/google-drive/authorize

### Response — `200 OK`

```json
{ "authorizationUrl": "https://accounts.google.com/o/oauth2/auth?..." }
```

Returns the URL rather than a `302`: the caller is an XHR, and a redirect would be followed inside the fetch
instead of navigating the page. Send the browser to it.

The `state` parameter embedded in the URL is single-use and expires after 10 minutes. It is held in memory, so
the flow must complete against the same server instance, and a restart mid-consent means connecting again.

## POST /api/backups/google-drive/callback

### Query Parameters

| Parameter | Type | Notes |
|-----------|------|-------|
| `code` | string, required | Authorization code from Google |
| `state` | string, required | The value from the authorize step |

### Response — `200 OK`

A `googleDrive` connection object. `connected: false` with a `detail` means Google returned a grant without
the Drive permission — reconnect and accept it.

### Errors

| Status | Condition |
|--------|-----------|
| `503 Service Unavailable` | Unknown or expired `state`, Google rejected the code, or no refresh token was returned |

Google issues a refresh token only on first authorization unless re-consent is forced, so the authorize step
sets `prompt=consent`. If a grant still arrives without one, remove the app at
`myaccount.google.com/permissions` and connect again.

## GET /api/backups/google-drive

### Response — `200 OK`

The `googleDrive` connection object described under
[GET /api/backups/configuration](#get-apibackupsconfiguration).

## DELETE /api/backups/google-drive

Deletes the stored credential, then revokes it at Google. Local deletion happens first: if revocation fails,
the application has still forgotten the credential, which is what was asked for.

### Response — `204 No Content`

---

# Backup Response Shape

```json
{
  "id": "0c1b7b0e-6f9a-4a1b-9f1e-1f2a3b4c5d6e",
  "operation": "BACKUP",
  "backupType": "MANUAL",
  "provider": "LOCAL",
  "format": "JSON",
  "fileName": "finance-backup-2026-09-11T143207.zip",
  "filePath": "C:\\...\\storage\\backups\\finance-backup-2026-09-11T143207.zip",
  "fileSize": 20480,
  "checksum": "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
  "status": "COMPLETED",
  "errorMessage": null,
  "startedAt": "2026-09-11T14:32:07",
  "completedAt": "2026-09-11T14:32:09",
  "downloadable": true
}
```

| Field | Type | Notes |
|-------|------|-------|
| `operation` | enum | `BACKUP` or `RESTORE` |
| `backupType` | enum | `MANUAL` or `AUTOMATIC`. A restore is always `MANUAL` |
| `filePath` | string, nullable | Local path, or the Drive web link. `null` for a restore |
| `fileSize` | integer, nullable | Bytes. `null` until the operation completes |
| `checksum` | string, nullable | Lowercase hex SHA-256 |
| `status` | enum | `IN_PROGRESS`, `COMPLETED`, `FAILED` |
| `errorMessage` | string, nullable | Failure reason, truncated to 2000 characters |
| `downloadable` | boolean | Whether the archive can still be fetched — true only for a completed backup |

---

# Automatic Backups

A scheduled job takes an `AUTOMATIC` backup and prunes old ones.

- **Schedule**: `app.backup.cron` (default `0 0 2 * * *`, i.e. 02:00 daily). Spring reads a cron expression
  once at startup, so this lives in configuration, not in the settings table. Set it to `-` to disable
  scheduling entirely.
- **Gate**: the `AUTO_BACKUP_ENABLED` setting decides whether a run does anything, and takes effect
  immediately.
- **Retention**: keeps the newest `BACKUP_RETENTION_COUNT` completed automatic backups and deletes the
  archives behind the rest. Manual backups are never pruned. History entries always survive; only archives are
  removed.
- A failed scheduled run is recorded as `FAILED` and logged. The exception is not propagated — the run has
  already recorded itself, and letting it escape a scheduled method only produces a second stack trace.

---

# Related Documentation

- `docs/api/Settings.md`
- `docs/operations/BackupStrategy.md`
- `docs/operations/RestoreStrategy.md`
- `docs/database/tables/backup_history.md`
