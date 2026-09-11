#!/usr/bin/env bash
#
# Shared helpers for the scripts that drive the REST API.
#
# Sourced, not executed. Provides:
#
#   api_require_tools     fail early if curl / a JSON reader is missing
#   json_field            read one field out of a JSON document
#   api_login             exchange credentials for an access token
#   api_get / api_post    authenticated requests
#
# Configuration comes from the environment, or from a .env file in the
# repository root if one exists:
#
#   API_BASE_URL   default http://localhost:8080/api
#   API_EMAIL      account to sign in as
#   API_PASSWORD   its password

set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"

# ---------------------------------------------------------------------------
# Environment
# ---------------------------------------------------------------------------

# Loads KEY=VALUE lines from the repository .env, without overriding anything
# already set in the environment.
api_load_env() {
    local env_file="$1"

    [ -f "$env_file" ] || return 0

    while IFS='=' read -r key value; do

        case "$key" in
            ''|'#'*) continue ;;
        esac

        # Only the keys these scripts use; .env holds server config too.
        case "$key" in
            API_BASE_URL|API_EMAIL|API_PASSWORD)
                if [ -z "${!key:-}" ]; then
                    export "$key=$value"
                fi
                ;;
        esac

    done < "$env_file"
}

# ---------------------------------------------------------------------------
# Tools
# ---------------------------------------------------------------------------

# JSON reader, resolved once. jq if present, otherwise python — one of the
# two is on nearly every machine, and hand-rolling a parser in sed would be
# fragile against nested objects.
JSON_READER=""

api_require_tools() {

    if ! command -v curl > /dev/null 2>&1; then
        echo "error: curl is required but not installed." >&2
        exit 1
    fi

    if command -v jq > /dev/null 2>&1; then
        JSON_READER="jq"
    elif command -v python3 > /dev/null 2>&1; then
        JSON_READER="python3"
    elif command -v python > /dev/null 2>&1; then
        JSON_READER="python"
    else
        echo "error: this script needs jq or python to read JSON responses." >&2
        echo "       Install jq (https://jqlang.github.io/jq/) and try again." >&2
        exit 1
    fi
}

# json_field <json> <dotted.path>
#
# Prints the value, or nothing when the path is absent.
json_field() {
    local json="$1"
    local path="$2"

    case "$JSON_READER" in
        jq)
            printf '%s' "$json" | jq -r ".$path // empty"
            ;;
        *)
            printf '%s' "$json" | "$JSON_READER" -c "
import json, sys
value = json.load(sys.stdin)
for key in '$path'.split('.'):
    if not isinstance(value, dict) or key not in value:
        sys.exit(0)
    value = value[key]
if value is not None:
    print(value)
"
            ;;
    esac
}

# ---------------------------------------------------------------------------
# Authentication
# ---------------------------------------------------------------------------

ACCESS_TOKEN=""

api_login() {

    if [ -z "${API_EMAIL:-}" ] || [ -z "${API_PASSWORD:-}" ]; then
        echo "error: set API_EMAIL and API_PASSWORD (in the environment or .env)." >&2
        exit 1
    fi

    local response
    response=$(curl --silent --show-error --fail-with-body \
        --request POST "$API_BASE_URL/auth/login" \
        --header 'Content-Type: application/json' \
        --data "{\"email\":\"$API_EMAIL\",\"password\":\"$API_PASSWORD\"}" \
        2>&1) || {
            echo "error: could not sign in to $API_BASE_URL" >&2
            echo "$response" >&2
            exit 1
        }

    ACCESS_TOKEN=$(json_field "$response" "accessToken")

    if [ -z "$ACCESS_TOKEN" ]; then
        echo "error: sign-in succeeded but returned no access token." >&2
        exit 1
    fi
}

# ---------------------------------------------------------------------------
# Requests
# ---------------------------------------------------------------------------

# api_get <path> [curl args...]
api_get() {
    local path="$1"
    shift

    curl --silent --show-error --fail-with-body \
        --request GET "$API_BASE_URL$path" \
        --header "Authorization: Bearer $ACCESS_TOKEN" \
        "$@"
}

# api_post <path> <json-body-or-empty> [curl args...]
api_post() {
    local path="$1"
    local body="${2:-}"
    shift 2 || shift 1

    if [ -n "$body" ]; then
        curl --silent --show-error --fail-with-body \
            --request POST "$API_BASE_URL$path" \
            --header "Authorization: Bearer $ACCESS_TOKEN" \
            --header 'Content-Type: application/json' \
            --data "$body" \
            "$@"
    else
        curl --silent --show-error --fail-with-body \
            --request POST "$API_BASE_URL$path" \
            --header "Authorization: Bearer $ACCESS_TOKEN" \
            "$@"
    fi
}

# Prints the "detail" of an RFC-7807 problem body, or the body itself.
api_error_detail() {
    local body="$1"
    local detail

    detail=$(json_field "$body" "detail" 2>/dev/null || true)

    if [ -n "$detail" ]; then
        printf '%s' "$detail"
    else
        printf '%s' "$body"
    fi
}
