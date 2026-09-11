#!/usr/bin/env bash
#
# Takes a backup through the REST API.
#
# Intended for cron and for use when the UI is unavailable. The work is done
# by the running application, so this needs the backend up — it is not an
# offline dump. For that, see pg_dump in docs/operations/RestoreStrategy.md.
#
# Usage
#   scripts/backup.sh [--provider LOCAL|GOOGLE_DRIVE] [--format JSON|PG_DUMP]
#                     [--download DIR] [--quiet]
#
# Options
#   --provider   one-off override of the BACKUP_PROVIDER setting
#   --format     one-off override of the BACKUP_FORMAT setting
#   --download   also save the archive into DIR (useful when the provider is
#                LOCAL but the server is not this machine)
#   --quiet      only print errors
#
# Configuration (environment or .env in the repository root)
#   API_BASE_URL   default http://localhost:8080/api
#   API_EMAIL      account to sign in as
#   API_PASSWORD   its password
#
# Exit status
#   0  backup completed
#   1  configuration or request error
#
# Example crontab entry — nightly at 02:30, logging failures only:
#   30 2 * * * cd /srv/personal-finance && scripts/backup.sh --quiet >> storage/logs/backup.log 2>&1

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# shellcheck source=lib/api.sh
. "$SCRIPT_DIR/lib/api.sh"

PROVIDER=""
FORMAT=""
DOWNLOAD_DIR=""
QUIET="false"

usage() {
    sed -n '2,30p' "${BASH_SOURCE[0]}" | sed 's/^#\{0,1\} \{0,1\}//'
}

while [ $# -gt 0 ]; do
    case "$1" in
        --provider)
            PROVIDER="${2:-}"
            shift 2
            ;;
        --format)
            FORMAT="${2:-}"
            shift 2
            ;;
        --download)
            DOWNLOAD_DIR="${2:-}"
            shift 2
            ;;
        --quiet)
            QUIET="true"
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

say() {
    [ "$QUIET" = "true" ] || echo "$@"
}

api_load_env "$ROOT_DIR/.env"
api_require_tools
api_login

# Build the override body. An empty object means "use the configured
# provider and format", which is the normal case.
BODY="{}"

if [ -n "$PROVIDER" ] && [ -n "$FORMAT" ]; then
    BODY="{\"provider\":\"$PROVIDER\",\"format\":\"$FORMAT\"}"
elif [ -n "$PROVIDER" ]; then
    BODY="{\"provider\":\"$PROVIDER\"}"
elif [ -n "$FORMAT" ]; then
    BODY="{\"format\":\"$FORMAT\"}"
fi

say "Backing up via $API_BASE_URL …"

if ! RESPONSE=$(api_post "/backups" "$BODY"); then
    echo "error: backup failed." >&2
    api_error_detail "$RESPONSE" >&2
    echo >&2
    exit 1
fi

BACKUP_ID=$(json_field "$RESPONSE" "id")
FILE_NAME=$(json_field "$RESPONSE" "fileName")
FILE_SIZE=$(json_field "$RESPONSE" "fileSize")
STATUS=$(json_field "$RESPONSE" "status")
FILE_PATH=$(json_field "$RESPONSE" "filePath")

if [ "$STATUS" != "COMPLETED" ]; then
    echo "error: backup finished with status $STATUS" >&2
    api_error_detail "$RESPONSE" >&2
    echo >&2
    exit 1
fi

say "Backup completed: $FILE_NAME (${FILE_SIZE:-0} bytes)"
say "Stored at: ${FILE_PATH:-<provider-managed>}"

# ---------------------------------------------------------------------------
# Optional local copy
# ---------------------------------------------------------------------------

if [ -n "$DOWNLOAD_DIR" ]; then

    mkdir -p "$DOWNLOAD_DIR"

    TARGET="$DOWNLOAD_DIR/$FILE_NAME"

    say "Downloading a copy to $TARGET …"

    if ! api_get "/backups/$BACKUP_ID/download" --output "$TARGET"; then
        echo "error: the backup was taken but could not be downloaded." >&2
        exit 1
    fi

    say "Saved $TARGET"
fi

say "Done."
