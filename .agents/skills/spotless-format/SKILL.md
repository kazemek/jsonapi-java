---
name: spotless-format
description: Applies and verifies Spotless formatting for Java, Groovy/Spock, Kotlin, and Gradle Kotlin DSL. Use before declaring an implementation task complete, or when the user asks to format code, run Spotless, spotlessApply, or spotlessCheck.
disable-model-invocation: true
---

# Spotless Format

Format the repository and verify compliance before declaring implementation work complete. Run this after `./gradlew clean build` passes and before the `sonar-quality-gate` skill.

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
