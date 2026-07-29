# Phase 0.8 — JSpecify Nullness

> **Scope:** Build tooling, `jsonapi-java-core`, and agent guidance  
> **Dependencies:** Phase 0.5, Phase 0.7  
> **Status:** Complete

## Goal

Make Java nullability explicit and enforceable in `jsonapi-java-core` with JSpecify and NullAway, while preserving ADR-002 wire states and a dependency-free published runtime for core.

## Research and constraints

- [docs/vision.md](../../docs/vision.md) — core has no third-party runtime dependencies; clarify that a compile-only JSpecify jar is allowed.
- [docs/adr/002-document-representation.md](../../docs/adr/002-document-representation.md) — Java `null` means member absence; sealed types mean explicit JSON `null`.
- [docs/adr/007-module-boundaries.md](../../docs/adr/007-module-boundaries.md) — core remains usable with no third-party runtime dependency.
- [https://jspecify.dev/docs/user-guide/](https://jspecify.dev/docs/user-guide/) — `@NullMarked` packages; `@Nullable` for nullable type usages; avoid redundant `@NonNull` under `@NullMarked`.
- NullAway JSpecify mode via Error Prone — fail `./gradlew clean build` on nullness violations in `main` sources only.

## Deliverables

- ADR-009 (JSpecify nullness policy) and a vision note that compile-only JSpecify does not violate the zero-runtime-deps promise.
- Shared `jsonapi-java-library` wiring: JSpecify `compileOnly`, Error Prone + NullAway on Java `main` sources (`io.github.kazemek.jsonapi`), plus refreshed dependency verification metadata.
- `@NullMarked` on every production package-info and accurate `@Nullable` decoration of absence-null and null-preserving APIs in `jsonapi-java-core`; build green under NullAway.
- Agent discovery updates: `AGENTS.md` Conventions bullet, `jsonapi-java-core` README agent notes, and checklist hooks in `module-docs`, `milestone-review`, and `milestone-planning` skills.
- Index this milestone in `.agentWork/milestones/README.md`.

## Non-goals

- Cursor rules or a dedicated jspecify skill.
- Annotating Groovy/Spock tests; Checker Framework; changing the sealed wire-state model.
- Publishing JSpecify as `api` / `compileOnlyApi` (would soften zero runtime deps).
- Applying annotations to modules that do not exist yet (jackson, annotations, query, Spring).

## Implementation boundaries

- Affected packages: `io.github.kazemek.jsonapi.core.model`, `.validation`, `.internal`.
- Nullness annotations: `org.jspecify.annotations` only.
- Runtime `LocalValidation.requireNonNull` remains the construction contract; annotations document it.
- Enforcement applies to Java compilation of library modules; tests stay unannotated.

## Test strategy

- Compile-time: NullAway must fail on a deliberate `@NullMarked` violation during implementation, then pass on the annotated tree.
- Regression: existing Spock specs continue to pass under `./gradlew clean build` (behavior unchanged).
- No new Spock specs required solely for annotation presence.

## Acceptance criteria

- [x] ADR-009 is accepted and linked from `jsonapi-java-core/README.md` further reading.
- [x] All production packages are `@NullMarked`; public absence-null APIs use JSpecify `@Nullable`; no JetBrains/JSR-305/Checker nullness annotations in main sources.
- [x] NullAway is enabled for `io.github.kazemek.jsonapi` main sources and the annotated tree compiles cleanly.
- [x] Agent path documents the policy (`AGENTS.md`, core README, skill checklists) without Cursor rules.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
- [x] `.agentWork/milestones/README.md` lists Phase 0.8 in dependency order and the milestone index.
