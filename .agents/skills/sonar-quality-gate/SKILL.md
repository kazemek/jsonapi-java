---
name: sonar-quality-gate
description: Runs SonarCloud analysis with Quality Gate wait and verifies zero unresolved new-code issues via the Issues API. Use before declaring module production/test source (`jsonapi-java-*/src/**`) work complete, or when the user asks to run Sonar, check the Quality Gate, or verify SonarCloud status. Skip for docs-only, workflow-only, and build-config-only changes.
disable-model-invocation: true
---

# Sonar Quality Gate

Verify SonarCloud analysis before declaring source-scope implementation work complete. Ordinary
`./gradlew clean build` stays token-free; this skill only covers the Sonar completion gate.

## Project policy

**Completion requires zero unresolved issues in the new-code period**, regardless of Quality Gate
status. On SonarCloud free tier, custom Quality Gate conditions (including fail-on
`new_violations`) are not available; the built-in gate can stay green while new smells exist.
Agents must therefore treat a green gate as necessary but **not sufficient**.

Do **not** claim completion from `sonar.qualitygate.wait` alone. Always confirm with the Issues API
script below. Changing SonarCloud Quality Gate conditions needs an explicit user request.

## Prerequisites

1. Confirm `./gradlew clean build` already passes for the change under review.
2. For automatic completion gating, confirm the change touches module production or test sources
   (`jsonapi-java-*/src/**`). Skip docs-only, workflow-only, and build-config-only changes, including
   `build-logic/src/**`, and do not report Sonar as their completion blocker. An explicit user
   request to run or inspect Sonar still invokes this skill.
3. Confirm `SONAR_TOKEN` is available in the environment (or can be provided by the user).
   - If the token is missing: do **not** claim the task is complete. Report that Sonar is blocked,
     that CI must still pass Sonar analysis, and ask for a token if a local check is required.
4. When running in CI-like contexts, also provide `GITHUB_TOKEN` when PR decoration is relevant.
5. The Issues API script needs `jq`, and `curl` 7.76+ (for `--fail-with-body`). If either is
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

3. Separately confirm zero new-code issues via the Issues API script (required):

```bash
.agents/skills/sonar-quality-gate/scripts/check-new-code-issues.sh
```

Scope the query with environment variables (do not put the token on argv):
   - **Pull request analysis** (CI on a PR): set `PULL_REQUEST=<PR number>`.
   - **Non-default branch analysis**: set `BRANCH=<branch name>`.
   - **Default branch (main) analysis**: omit both; without an identity the API reports the
     project's default-branch new-code period.

The script owns API authentication, pagination/query mechanics, and fail-closed validation for HTTP
or network errors, malformed JSON, missing or non-numeric `total`, and missing
`SONAR_TOKEN`/`jq`/`curl`. It reads the token without placing it on the argument list and prints a
validated numeric `total` on success.

Interpret the result:
   - **Non-zero exit:** Do **not** declare completion and do **not** treat the result as zero
     issues. Report the failure (API/credential/network error) and retry or escalate.
   - **Exit 0:** `total` is a valid number. If `total == 0`, the task may be declared complete
     for Sonar. If `total > 0`, re-run with `--list` to print issue keys/messages, fix every one
     (or get an explicit user waiver), re-run steps 1–3, and do not claim completion while any
     remain.

Project key / organization: `kazemek_jsonapi-java` / `kazemek` (see root `build.gradle.kts`).

## Notes

- Do not attach `sonar` to `build`/`check`. Developers who only build and test locally do not need
  `SONAR_TOKEN`.
- Current CI runs the scanner and waits for the Quality Gate, but does not run the Issues API
  script. CI success alone therefore does not satisfy the separate authenticated zero-issue check.
- Prefer fixing smells over suppressions. Use suppressions only when the user explicitly agrees.
- Missing blame / SCM warnings can affect new-code detection; still fix any issues the API returns
  for `inNewCodePeriod=true`.
