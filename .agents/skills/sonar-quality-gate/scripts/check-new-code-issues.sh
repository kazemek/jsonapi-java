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
#   ./check-new-code-issues.sh --list    # also list issue key + message lines
#
# Exit codes:
#   0 — HTTP/JSON ok and .total is a non-negative number (printed on stdout)
#   non-zero — missing deps/token, HTTP error, or malformed response
set -euo pipefail

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
URL="https://sonarcloud.io/api/issues/search?componentKeys=${COMPONENT_KEY}&resolved=false&inNewCodePeriod=true&ps=100"
if [[ -n "${PULL_REQUEST:-}" ]]; then
  URL="${URL}&pullRequest=${PULL_REQUEST}"
elif [[ -n "${BRANCH:-}" ]]; then
  URL="${URL}&branch=${BRANCH}"
fi

RESPONSE="$(
  curl -sS --fail-with-body -q --config - \
    "$URL" \
    <<EOF
user = "${SONAR_TOKEN}:"
EOF
)"

TOTAL="$(printf '%s' "$RESPONSE" | jq -e '.total | select(type == "number" and . >= 0)')"
printf '%s\n' "$TOTAL"

if [[ "$LIST" -eq 1 ]]; then
  printf '%s' "$RESPONSE" | jq -r '.issues[]? | "\(.key) \(.message)"'
fi
