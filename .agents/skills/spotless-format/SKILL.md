---
name: spotless-format
description: Applies and verifies Spotless formatting for Java, Groovy/Spock, Kotlin, and Gradle Kotlin DSL. Use when the change touches Spotless-covered files (`.java`, `.groovy`, `.kt`, `.gradle.kts`) or the formatter configuration, or when the user asks to format code, run Spotless, spotlessApply, or spotlessCheck. Skip for docs-only and pure workflow changes.
disable-model-invocation: true
---

# Spotless Format

Format the repository and verify compliance before declaring implementation work complete. Run this before `./gradlew clean build` (whose `check` already executes `spotlessCheck`) and before the `sonar-quality-gate` skill, so the build passes on the first run instead of failing on formatting and requiring a re-run.

## Applicability

Run this skill when:

- the change touches Spotless-covered files (`.java`, `.groovy`, `.kt`, `.gradle.kts`) or the
  formatter configuration; or
- the user explicitly asks to format code or run Spotless (`spotlessApply` / `spotlessCheck`), even
  if no covered files changed.

Docs-only and pure workflow changes have no formatting surface by default; skip this gate for them
unless the user makes an explicit formatting request.

## Run formatting

1. Apply formatting:

```bash
./gradlew spotlessApply
```

2. Verify compliance:

```bash
./gradlew spotlessCheck
```

3. Interpret the result:
   - **Exit 0 on both:** Formatting is clean. Continue to any remaining completion gates (e.g. Sonar).
   - **Non-zero exit:** Task is incomplete. Inspect Spotless output, fix remaining issues (or re-run `spotlessApply` after manual edits), then run `spotlessCheck` again.

## Notes

- Do not attach `spotlessCheck` to `build`/`check` unless the user asks.
- Spotless is configured by the root-only `jsonapi-java-spotless` convention plugin.
- Greclipse settings for Groovy/Spock live in `config/spotless/greclipse.properties`.
