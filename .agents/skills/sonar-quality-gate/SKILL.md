---
name: sonar-quality-gate
description: Runs SonarCloud analysis with Quality Gate wait and verifies zero unresolved new-code issues via the Issues API. Use before declaring an implementation task complete, or when the user asks to run Sonar, check the Quality Gate, or verify SonarCloud status.
disable-model-invocation: true
---

# Sonar Quality Gate

Verify SonarCloud analysis before declaring implementation work complete. Ordinary
`./gradlew clean build` stays token-free; this skill only covers the Sonar completion gate.

## Project policy

**Completion requires zero unresolved issues in the new-code period**, regardless of Quality Gate
status. On SonarCloud free tier, custom Quality Gate conditions (including fail-on
`new_violations`) are not available; the built-in gate can stay green while new smells exist.
Agents must therefore treat a green gate as necessary but **not sufficient**.

Do **not** claim completion from `sonar.qualitygate.wait` alone. Always confirm with an Issues API
search (below). Changing SonarCloud Quality Gate conditions needs an explicit user request.

## Prerequisites

1. Confirm `./gradlew clean build` already passes for the change under review.
2. Confirm `SONAR_TOKEN` is available in the environment (or can be provided by the user).
   - If the token is missing: do **not** claim the task is complete. Report that Sonar is blocked,
     that CI must still pass Sonar analysis, and ask for a token if a local check is required.
3. When running in CI-like contexts, also provide `GITHUB_TOKEN` when PR decoration is relevant.
4. The Issues API check below needs `jq`, and `curl` 7.76+ (for `--fail-with-body`). If either is
   missing, stop and ask the user rather than skipping the validation.

## Run the gate

1. Run:

```bash
./gradlew clean build jacocoTestReport sonar
```

with `SONAR_TOKEN` set. `sonar.qualitygate.wait=true` is configured in the root build, so the
`sonar` task blocks until SonarCloud returns the gate result and fails on a red gate.

2. Interpret the Gradle result:
   - **Non-zero exit / Quality Gate failure:** Task is incomplete. Summarize failing conditions
     from the scanner output, then fix the issues or escalate to the user.
   - **Exit 0:** Quality Gate passed. **Continue to step 3** — do not stop here.

3. Confirm zero new-code issues via the SonarCloud Issues API (required):

Scope the query to the analysis being verified:
   - **Pull request analysis** (CI on a PR): append `&pullRequest=<PR number>`.
   - **Non-default branch analysis**: append `&branch=<branch name>`.
   - **Default branch (main) analysis**: omit both; without an identity the API reports the
     project's default-branch new-code period.

```bash
set -o pipefail
curl -sS --fail-with-body -q --config - \
  "https://sonarcloud.io/api/issues/search?componentKeys=kazemek_jsonapi-java&resolved=false&inNewCodePeriod=true&ps=100" \
  <<EOF | jq -e '.total | type == "number" and . >= 0'
user = "${SONAR_TOKEN}:"
EOF
```

The pipeline fails closed: non-zero exit on HTTP errors (`--fail-with-body`), malformed JSON,
missing or non-numeric `total` (`jq -e`), or network failures (`pipefail`). The token is read
from stdin, never from the argument list.

Interpret the result:
   - **Non-zero exit:** Do **not** declare completion and do **not** treat the result as zero
     issues. Report the failure (API/credential/network error) and retry or escalate.
   - **Exit 0:** `total` is a valid number. If `total == 0`, the task may be declared complete
     for Sonar. If `total > 0`, re-fetch the same request without the `jq -e` guard (or pipe it
     to `jq -r '.issues[] | "\(.key) \(.message)"'`) to list issues, fix every one (or get an
     explicit user waiver), re-run steps 1–3, and do not claim completion while any remain.

Project key / organization: `kazemek_jsonapi-java` / `kazemek` (see root `build.gradle.kts`).

## Notes

- Do not attach `sonar` to `build`/`check`. Developers who only build and test locally do not need
  `SONAR_TOKEN`.
- Prefer fixing smells over suppressions. Use suppressions only when the user explicitly agrees.
- Missing blame / SCM warnings can affect new-code detection; still fix any issues the API returns
  for `inNewCodePeriod=true`.
