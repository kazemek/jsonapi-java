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
#   ./check-new-code-issues.sh           # print total; exit 0 if total is a valid number
#   ./check-new-code-issues.sh --list    # also list issue key + message lines (all pages)
#
# Exit codes:
#   0 — HTTP/JSON ok and .total is a non-negative number (printed on stdout)
#   non-zero — missing deps/token, HTTP error, or malformed response
set -euo pipefail

PAGE_SIZE=100
LIST=0
if [[ "${1:-}" == "--list" ]]; then
  LIST=1
elif [[ $# -gt 0 ]]; then
  echo "usage: $0 [--list]" >&2
  exit 2
fi

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

if [[ "$LIST" -ne 1 ]]; then
  exit 0
fi

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
