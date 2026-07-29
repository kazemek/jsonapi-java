# Phase 0.9 — ArchUnit Core Dependency Guard

> **Scope:** `jsonapi-java-core` architectural tests and agent guidance  
> **Dependencies:** Phase 0.8  
> **Status:** Complete

## Goal

Enforce that production types in `jsonapi-java-core` depend only on the JDK, JSpecify annotations, and other core types, so the zero-runtime-deps promise cannot drift via accidental type coupling.

## Research and constraints

- [docs/vision.md](../../docs/vision.md) — `jsonapi-java-core` has no third-party runtime dependencies; compile-only JSpecify is allowed.
- [docs/adr/007-module-boundaries.md](../../docs/adr/007-module-boundaries.md) — core remains usable with no third-party runtime dependency.
- [docs/adr/009-jspecify-nullness.md](../../docs/adr/009-jspecify-nullness.md) — `org.jspecify.annotations` is the intentional non-JDK exception on the compile classpath.
- [ArchUnit user guide](https://www.archunit.org/userguide/html/000_Index.html) — free Fluent API and `ClassFileImporter` work with any test framework (Spock).

## Deliverables

- ADR-010 (architectural tests for package/type dependencies) linked from `jsonapi-java-core/README.md`.
- ArchUnit `1.4.2` as `testImplementation` on `jsonapi-java-core` only (version catalog + verification metadata).
- Spock `CoreDependencyRulesSpec` asserting core production classes may depend only on `java..`, `org.jspecify.annotations..`, and `io.github.kazemek.jsonapi.core..`.
- Agent notes: `AGENTS.md` Conventions bullet and core README agents subsection.
- Index this milestone in `.agentWork/milestones/README.md`.

## Non-goals

- Cursor rules or a dedicated ArchUnit skill; skill-checklist churn.
- Cross-module bans on `core.internal` (document intent in ADR only; implement when sibling modules exist).
- Layer DAGs, annotation-family rules beyond the dependency allowlist, or ArchUnit in the shared library plugin.
- Changing production Java sources unless a real ArchUnit violation appears.

## Implementation boundaries

- Primary module: `jsonapi-java-core`.
- ArchUnit is `testImplementation` only; never a production dependency.
- Gradle continues to own artifact classpath; ArchUnit owns package/type dependency rules.
- Spec lives under `src/test/groovy/.../architecture/`.

## Test strategy

- ArchUnit Spock spec imports `main` classes under `io.github.kazemek.jsonapi.core` and fails the build on illegal type dependencies.
- Existing Spock specs continue to pass under `./gradlew clean build`.

## Acceptance criteria

- [x] ADR-010 is accepted and linked from `jsonapi-java-core/README.md` further reading.
- [x] ArchUnit rule passes: core production types depend only on JDK, JSpecify annotations, and other core types.
- [x] Agent path documents the policy (`AGENTS.md`, core README) without Cursor rules.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
- [x] `.agentWork/milestones/README.md` lists Phase 0.9 in dependency order and the milestone index.
