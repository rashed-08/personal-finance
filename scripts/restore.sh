#!/usr/bin/env bash
#
# Restores a backup through the REST API.
#
# THIS REPLACES EXISTING DATA. Read docs/operations/RestoreStrategy.md before
# using it. The confirmation phrase must be supplied every time; there is no
# way to skip it.
#
# Usage
#   scripts/restore.sh --file PATH            restore an archive from disk
#   scripts/restore.sh --id BACKUP_ID         restore an archive from history
#   scripts/restore.sh --list                 show recent backups and exit
#
# Options
#   --yes        take the confirmation from the CONFIRM environment variable
#                instead of prompting (for unattended use — think first)
#
# Configuration (environment or .env in the repository root)
#   API_BASE_URL   default http://localhost:8080/api
#   API_EMAIL      account to sign in as
#   API_PASSWORD   its password
#
# Exit status
#   0  restore completed
#   1  configuration, confirmation or request error

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# shellcheck source=lib/api.sh
. "$SCRIPT_DIR/lib/api.sh"

FILE=""
BACKUP_ID=""
LIST="false"
ASSUME_YES="false"

usage() {
    sed -n '2,26p' "${BASH_SOURCE[0]}" | sed 's/^#\{0,1\} \{0,1\}//'
}

while [ $# -gt 0 ]; do
    case "$1" in
        --file)
            FILE="${2:-}"
            shift 2
            ;;
        --id)
            BACKUP_ID="${2:-}"
            shift 2
            ;;
        --list)
            LIST="true"
            shift
            ;;
        --yes)
            ASSUME_YES="true"
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "error: unknown option $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

api_load_env "$ROOT_DIR/.env"
api_require_tools
api_login

# ---------------------------------------------------------------------------
# --list
# ---------------------------------------------------------------------------

if [ "$LIST" = "true" ]; then

    HISTORY=$(api_get "/backups?operation=BACKUP")

    if [ "$JSON_READER" = "jq" ]; then
        printf '%s' "$HISTORY" | jq -r '
            .[] | select(.downloadable) |
            "\(.startedAt)  \(.id)  \(.format)  \(.fileName)"'
    else
        printf '%s' "$HISTORY" | "$JSON_READER" -c "
import json, sys
for entry in json.load(sys.stdin):
    if entry.get('downloadable'):
        print('%s  %s  %s  %s' % (
            entry['startedAt'], entry['id'], entry['format'], entry['fileName']))
"
    fi

    exit 0
fi

# ---------------------------------------------------------------------------
# Validate the target
# ---------------------------------------------------------------------------

if [ -z "$FILE" ] && [ -z "$BACKUP_ID" ]; then
    echo "error: pass --file PATH or --id BACKUP_ID (or --list to see what is available)." >&2
    usage >&2
    exit 1
fi

if [ -n "$FILE" ] && [ -n "$BACKUP_ID" ]; then
    echo "error: pass either --file or --id, not both." >&2
    exit 1
fi

if [ -n "$FILE" ] && [ ! -f "$FILE" ]; then
    echo "error: no such file: $FILE" >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# Confirmation
#
# The phrase comes from the server so this script never hardcodes it.
# ---------------------------------------------------------------------------

CONFIGURATION=$(api_get "/backups/configuration")
PHRASE=$(json_field "$CONFIGURATION" "confirmationPhrase")

if [ -z "$PHRASE" ]; then
    echo "error: the server did not report a confirmation phrase." >&2
    exit 1
fi

cat >&2 <<WARNING

  ============================================================
   This replaces ALL existing accounts, transactions, funds,
   loans, salary cycles and settings with the contents of the
   archive. It cannot be undone.

   Target: ${FILE:-history entry $BACKUP_ID}
  ============================================================

WARNING

if [ "$ASSUME_YES" = "true" ]; then
    TYPED="${CONFIRM:-}"

    if [ -z "$TYPED" ]; then
        echo "error: --yes requires the CONFIRM environment variable to hold the phrase." >&2
        exit 1
    fi
else
    printf 'Type %s to proceed: ' "$PHRASE" >&2
    IFS= read -r TYPED
fi

if [ "$TYPED" != "$PHRASE" ]; then
    echo "Aborted: confirmation did not match. Nothing was changed." >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# Restore
# ---------------------------------------------------------------------------

echo "Restoring …" >&2

if [ -n "$FILE" ]; then
    RESPONSE=$(curl --silent --show-error --fail-with-body \
        --request POST "$API_BASE_URL/backups/restore" \
        --header "Authorization: Bearer $ACCESS_TOKEN" \
        --form "file=@$FILE" \
        --form "confirmation=$PHRASE") || {
            echo "error: restore failed." >&2
            api_error_detail "$RESPONSE" >&2
            echo >&2
            exit 1
        }
else
    RESPONSE=$(api_post "/backups/$BACKUP_ID/restore" "{\"confirmation\":\"$PHRASE\"}") || {
        echo "error: restore failed." >&2
        api_error_detail "$RESPONSE" >&2
        echo >&2
        exit 1
    }
fi

STATUS=$(json_field "$RESPONSE" "status")
FILE_NAME=$(json_field "$RESPONSE" "fileName")
FORMAT=$(json_field "$RESPONSE" "format")

if [ "$STATUS" != "COMPLETED" ]; then
    echo "error: restore finished with status $STATUS" >&2
    api_error_detail "$RESPONSE" >&2
    echo >&2
    exit 1
fi

echo "Restore completed from $FILE_NAME."

if [ "$FORMAT" = "PG_DUMP" ]; then
    cat <<NEXT

  A PG_DUMP restore replaced the whole database, including users and the
  Flyway history. Restart the application now, and sign in with the
  credentials from the archive.

NEXT
fi
