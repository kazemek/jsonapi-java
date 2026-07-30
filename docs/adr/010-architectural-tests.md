# ADR-010: Architectural Tests for Module Boundaries

**Status:** Accepted  
**Date:** 2026-07-29  
**Amended:** 2026-07-30

## Context

ADR-007 and the vision require dependency-free foundation modules and controlled coupling for optional integrations. Gradle dependency declarations guard the published classpath, but they do not stop production sources from referring to types that appear on the compile or test classpath by mistake, or from sibling modules reaching into non-public packages such as `core.internal`.

JSpecify (`org.jspecify.annotations`) is an intentional compile-only exception (ADR-009) and must remain allowed in production sources that use `@NullMarked` / `@Nullable`.

## Decision

- Enforce package and type dependency rules with [ArchUnit](https://www.archunit.org/) as a **`testImplementation`** dependency on each library module that owns production Java sources. ArchUnit must never appear on the published runtime classpath.
- ArchUnit is the project-wide architectural test tool—not core-only. New modules add ArchUnit rules alongside their production packages; do not reinvent coupling checks with classpath or source-import scanners.
- Current allowlists:
  - `io.github.kazemek.jsonapi.core..` → `java..`, `org.jspecify.annotations..`, and other `io.github.kazemek.jsonapi.core..` types.
  - `io.github.kazemek.jsonapi.annotation..` → `java..`, `org.jspecify.annotations..`, and other `io.github.kazemek.jsonapi.annotation..` types.
- Gradle continues to own artifact selection and publication; ArchUnit owns package/type coupling that Gradle cannot express.
- Changing an allowlist requires updating this ADR.
- Future sibling modules (jackson, query, Spring adapters) must not depend on `io.github.kazemek.jsonapi.core.internal..`. That rule is intentional policy here and will be implemented when those modules exist.

## Consequences

- `./gradlew clean build` fails when a guarded module's production code gains an illegal type dependency.
- Agents and contributors treat ArchUnit failures as boundary violations, not tests to delete or weaken without an ADR change.
- Additional architectural rules (cross-module `internal` bans, layer DAGs) land in follow-up milestones alongside the modules they protect.
