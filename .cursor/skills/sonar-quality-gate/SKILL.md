---
name: sonar-quality-gate
description: Runs SonarCloud analysis with Quality Gate wait and treats a red gate as incomplete work. Use before declaring an implementation task complete, or when the user asks to run Sonar, check the Quality Gate, or verify SonarCloud status.
disable-model-invocation: true
---

# Sonar Quality Gate

Verify that SonarCloud's Quality Gate passes before declaring implementation work complete. Ordinary `./gradlew clean build` stays token-free; this skill only covers the Sonar completion gate.

## Project policy

The SonarCloud Quality Gate named **`jsonapi-java`** (not built-in Sonar way) fails when
**any new issue** is introduced (`new_violations` > 0), in addition to Sonar way ratings,
coverage, duplication, and security-hotspot conditions. The project must use that gate
(Project Settings → Quality Gate in SonarCloud).

Zero new issues is required for task completion. Do **not** loosen that gate; changing
Quality Gate conditions needs an explicit user request.

## Prerequisites

1. Confirm `./gradlew clean build` already passes for the change under review.
2. Confirm `SONAR_TOKEN` is available in the environment (or can be provided by the user).
   - If the token is missing: do **not** claim the task is complete. Report that Sonar is blocked, that CI must still pass the Quality Gate, and ask for a token if a local gate check is required.
3. When running in CI-like contexts, also provide `GITHUB_TOKEN` when PR decoration is relevant.

## Run the gate

1. Run:

```bash
./gradlew clean build jacocoTestReport sonar
```

with `SONAR_TOKEN` set. `sonar.qualitygate.wait=true` is configured in the root build, so the `sonar` task blocks until SonarCloud returns the gate result and fails on a red gate.

2. Interpret the result:
   - **Exit 0:** Quality Gate passed. Optionally confirm unresolved new-code issues are empty via SonarCloud issues search when the dashboard or scanner output looks inconsistent with a green gate; if unexpected open issues remain on new code, treat the task as incomplete.
   - **Non-zero exit / Quality Gate failure:** Task is incomplete. Summarize failing conditions from the scanner output, then fix the issues or escalate to the user.
3. Only after a green gate (and no unexpected new-code issues), or an explicit user waiver, may the agent declare the work complete.

## Notes

- Do not attach `sonar` to `build`/`check`. Developers who only build and test locally do not need `SONAR_TOKEN`.
- Prefer the Gradle scanner wait for gate status. Issues search is allowed as a defensive check for the zero-new-issues policy when something looks wrong after a green gate.
- Do not loosen the project Quality Gate. Creating or tightening conditions is allowed only when the user explicitly requests it.
