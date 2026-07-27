# Phase 0.4 — Spotless Formatting

> **Scope:** Build tooling and agent completion  
> **Dependencies:** Phase 0.3  
> **Status:** Complete

## Goal

Enforce consistent formatting for Java, Groovy/Spock, Kotlin, and Gradle Kotlin DSL via Spotless, and teach agents to format before declaring work complete.

## Deliverables

- Add a root-only `jsonapi-java-spotless` convention plugin in `build-logic` that applies Spotless
  (version from the catalog, currently 8.5.1 — capped below 8.6 because greclipse breaks cold CI;
  see diffplug/spotless#2950) with:
  - Java: Spotless-default `googleJavaFormat()`, unused-import removal, trailing whitespace, final newline
  - Groovy/Spock: greclipse with `config/spotless/greclipse.properties`, trailing whitespace, final newline
    (`excludeJava()` omitted: Spotless rejects it when a custom `target("**/*.groovy")` is set; the glob already excludes Java)
  - Kotlin sources and Gradle Kotlin DSL: Spotless-default `ktlint()`, trailing whitespace, final newline
- Keep the Spotless plugin version in `gradle/libs.versions.toml` so Renovate can update it; formatter
  tool versions follow Spotless defaults.
- Apply the convention plugin from the root `build.gradle.kts`.
- Add `config/spotless/greclipse.properties` (2-space indent, same-line braces for Spock labels).
- Add a project `spotless-format` skill and require it in the agent completion lifecycle in `AGENTS.md`.
- Refresh `gradle/verification-metadata.xml` for Spotless and formatter dependencies, including IDE convenience trusts for sources/javadoc jars and Gradle src zips.
- Run `spotlessCheck` in GitHub Actions before build/Sonar.
- Format the repository so `spotlessCheck` passes.

## Non-goals

- Explicitly wiring `check` → `spotlessCheck` beyond Spotless plugin defaults.
- Disabling dependency verification or trusting all artifacts.
- IDE editorconfig or style rules beyond the specified formatters.
- Reopening completed Phase 0.3.

## Acceptance criteria

- [x] `jsonapi-java-spotless` exists under `build-logic` and is applied at the root only.
- [x] `config/spotless/greclipse.properties` exists with the agreed greclipse settings.
- [x] `.cursor/skills/spotless-format/SKILL.md` exists; `AGENTS.md` requires it after `clean build` and before Sonar.
- [x] CI runs `spotlessCheck` (see `.github/workflows/build.yml`).
- [x] `./gradlew spotlessApply` succeeds.
- [x] `./gradlew spotlessCheck` succeeds.
- [x] `./gradlew clean build` succeeds without `SONAR_TOKEN`.
- [x] Dependency verification covers Spotless-related artifacts.
