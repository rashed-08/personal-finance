# Import API

Version: 1.0

Status: Draft (Google Keep phase only — CSV Import is a later phase of Issue #14)

Owner: Personal Finance App

---

# Purpose

This document describes the REST API for importing historical financial data. It currently covers the
**Google Keep Migration** phase of GitHub issue #14; CSV Import, and Export (CSV/Excel/JSON) are later phases
and are not yet implemented.

See `docs/business/GoogleKeepMigration.md` for the full migration design and its "Implementation Notes"
section for exactly how the implementation resolved open questions in that spec.

Base path

```
/api/migrations
```

All request and response bodies are JSON. Errors follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807),
returned as `application/problem+json` by `GlobalExceptionHandler`.

---

# Endpoints Overview

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/migrations/google-keep` | Parse and import a Google Keep text export in one call |

---

# POST /api/migrations/google-keep

Parses the given text (per the format described in `docs/business/GoogleKeepMigration.md`) and immediately
imports every recognized line as a posted `EXPENSE` transaction — there is no separate preview/confirm step in
v1. The call is idempotent: re-running it with the same (or overlapping) content skips lines that already exist
as an exact match (same salary cycle, category, and amount) rather than creating duplicates.

## Request Body

```json
{
  "content": "০৫-২৬\n=========\n\nগ্যাস ১৮৫০\n\n...\n\n=৪২০০০\n"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `content` | string, required | The raw pasted/exported Keep note text. May contain multiple months. Bengali numerals and common HTML entity artifacts (e.g. `&#x20;`) are handled automatically. |

## What happens

- **Account**: every imported transaction posts against a dedicated `Legacy Import` cash account, created
  automatically on first use. Real accounts are never guessed at, since Keep notes never name one.
- **Salary Cycle**: each month header (`MM-YY`) maps to a calendar-month salary cycle (1st to end of month),
  reusing one if it already covers that period, otherwise creating it.
- **Category**: each line's label is matched against a small set of known synonyms mapped to existing category
  names (see `V2__seed_data.sql`); anything unmatched falls back to `Other Expense` silently. This is
  best-effort by design — the priority is getting accurate amounts into the ledger for totals/graphs, not
  perfect category fidelity.
- **Amount**: taken from the line's own stated number when present; computed as the sum of a "+"-joined
  breakdown when no number is stated. A month's own total line is stored only as a reference — a mismatch
  against the sum of that month's imported lines produces a warning, never a failure.
- **Duplicates**: a line matching an existing transaction's salary cycle + category + amount exactly is
  skipped and counted, not re-imported.
- **Traceability**: every created transaction is tagged with a `migrationBatchId` (a fresh id per call), and
  its original breakdown text (if any) is preserved in `notes`.

## Response — `200 OK`

```json
{
  "importedCount": 32,
  "skippedCount": 0,
  "warnings": [
    "Month 2026-05: stated total 42000 does not match the sum of imported lines 42940 (reference only — not enforced)."
  ],
  "errors": [],
  "durationMillis": 184
}
```

| Field | Notes |
|-------|-------|
| `importedCount` | Number of transactions created |
| `skippedCount` | Number of lines skipped as exact duplicates |
| `warnings` | Non-blocking issues: unparseable lines, total/sum mismatches |
| `errors` | Lines that failed to import (e.g. an unexpected validation failure) — the rest of the import still proceeds |
| `durationMillis` | Wall-clock time for the whole call |

## Errors

| Status | Cause |
|--------|-------|
| 400 | `content` missing or blank |

---

# Error Response Shape

All errors follow RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Request Validation Failed",
  "status": 400,
  "detail": "Google Keep export content is required.",
  "timestamp": "2026-07-25T10:15:00Z"
}
```

---

# Final Statement

This document reflects the Google Keep Migration phase of Issue #14 as implemented. CSV Import, CSV Export,
Excel Export, and JSON Export are separate, not-yet-implemented phases — this document will be extended as
each lands.
