#!/usr/bin/env bash
# Check SonarCloud for unresolved issues in the new-code period.
#
# Required env:
#   SONAR_TOKEN — SonarCloud token (never passed on the argv)
# Optional env:
#   PULL_REQUEST — PR number (CI PR analysis)
#   BRANCH — branch name (non-default branch analysis)
#            Omit both for default-branch (main) analysis.
#
# Usage:
#   ./check-new-code-issues.sh                    # print total; exit 0 if total is a valid number
#   ./check-new-code-issues.sh --list             # also list issue key + message lines (all pages)
#   ./check-new-code-issues.sh --require-zero     # exit 1 when total != 0
#   ./check-new-code-issues.sh --list --require-zero
#
# Exit codes:
#   0 — HTTP/JSON ok and .total is a non-negative number (and zero when --require-zero)
#   non-zero — missing deps/token, HTTP error, malformed response, or non-zero total with --require-zero
set -euo pipefail

PAGE_SIZE=100
LIST=0
REQUIRE_ZERO=0
for arg in "$@"; do
  case "$arg" in
    --list) LIST=1 ;;
    --require-zero) REQUIRE_ZERO=1 ;;
    *)
      echo "usage: $0 [--list] [--require-zero]" >&2
      exit 2
      ;;
  esac
done

if [[ -z "${SONAR_TOKEN:-}" ]]; then
  echo "SONAR_TOKEN is required" >&2
  exit 1
fi

for cmd in curl jq; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "required command not found: $cmd" >&2
    exit 1
  fi
done

COMPONENT_KEY="kazemek_jsonapi-java"
BASE_URL="https://sonarcloud.io/api/issues/search?componentKeys=${COMPONENT_KEY}&resolved=false&inNewCodePeriod=true&ps=${PAGE_SIZE}"
if [[ -n "${PULL_REQUEST:-}" ]]; then
  BASE_URL="${BASE_URL}&pullRequest=${PULL_REQUEST}"
elif [[ -n "${BRANCH:-}" ]]; then
  BASE_URL="${BASE_URL}&branch=${BRANCH}"
fi

fetch_page() {
  local page="$1"
  curl -sS --fail-with-body -q --config - \
    "${BASE_URL}&p=${page}" \
    <<EOF
user = "${SONAR_TOKEN}:"
EOF
}

RESPONSE="$(fetch_page 1)"
TOTAL="$(printf '%s' "$RESPONSE" | jq -e '.total | select(type == "number" and . >= 0)')"
printf '%s\n' "$TOTAL"

if [[ "$LIST" -eq 1 ]]; then
  ISSUES_JSON='[]'
  page=1
  collected=0
  while true; do
    if [[ "$page" -gt 1 ]]; then
      RESPONSE="$(fetch_page "$page")"
    fi

    if ! printf '%s' "$RESPONSE" | jq -e '.issues | type == "array"' >/dev/null; then
      echo ".issues must be a JSON array" >&2
      exit 1
    fi

    page_count="$(printf '%s' "$RESPONSE" | jq '.issues | length')"
    ISSUES_JSON="$(
      jq -n --argjson acc "$ISSUES_JSON" --argjson page "$(printf '%s' "$RESPONSE" | jq '.issues')" \
        '$acc + $page'
    )"
    collected=$((collected + page_count))

    if [[ "$collected" -ge "$TOTAL" || "$page_count" -lt "$PAGE_SIZE" ]]; then
      break
    fi
    page=$((page + 1))
  done

  printf '%s' "$ISSUES_JSON" | jq -r '.[] | "\(.key) \(.message)"'
fi

if [[ "$REQUIRE_ZERO" -eq 1 && "$TOTAL" -ne 0 ]]; then
  echo "expected 0 unresolved new-code issues, found ${TOTAL}" >&2
  exit 1
fi
