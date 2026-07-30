# ADR-010: Architectural Tests for Module Boundaries

**Status:** Accepted  
**Date:** 2026-07-29  
**Amended:** 2026-07-30 (Phase 2.1 implements jackson3 allowlist and `core.internal` ban)

## Context

ADR-007 and the vision require dependency-free foundation modules and controlled coupling for optional integrations. Gradle dependency declarations guard the published classpath, but they do not stop production sources from referring to types that appear on the compile or test classpath by mistake, or from sibling modules reaching into non-public packages such as `core.internal`.

JSpecify (`org.jspecify.annotations`) is an intentional compile-only exception (ADR-009) and must remain allowed in production sources that use `@NullMarked` / `@Nullable`.

## Decision

- Enforce package and type dependency rules with [ArchUnit](https://www.archunit.org/) as a **`testImplementation`** dependency on each library module that owns production Java sources. ArchUnit must never appear on the published runtime classpath.
- ArchUnit is the project-wide architectural test tool—not core-only. New modules add ArchUnit rules alongside their production packages; do not reinvent coupling checks with classpath or source-import scanners.
- Current allowlists:
  - `io.github.kazemek.jsonapi.core..` → `java..`, `org.jspecify.annotations..`, and other `io.github.kazemek.jsonapi.core..` types.
  - `io.github.kazemek.jsonapi.annotation..` → `java..`, `org.jspecify.annotations..`, and other `io.github.kazemek.jsonapi.annotation..` types.
  - `io.github.kazemek.jsonapi.jackson3..` → `java..`, `org.jspecify.annotations..`,
    `io.github.kazemek.jsonapi.core.model..`, `io.github.kazemek.jsonapi.core.validation..`,
    `io.github.kazemek.jsonapi.annotation..`, `io.github.kazemek.jsonapi.jackson3..`, and
    `tools.jackson..`. Production sources must not depend on
    `io.github.kazemek.jsonapi.core.internal..` or `com.fasterxml.jackson..`.
- Major-specific Jackson 2 allowlist (when registered):
  - `io.github.kazemek.jsonapi.jackson2..` → JDK, JSpecify, core public packages, annotations,
    module-owned types, and `com.fasterxml.jackson..`; never Jackson 3 or another module's
    internals. Must not depend on `core.internal`.
- Query and Spring milestones record their exact framework package allowlists when those modules
  are registered. Spring may use public core, annotation, Jackson 3, and query contracts; no lower
  layer may acquire Spring types.
- Gradle continues to own artifact selection and publication; ArchUnit owns package/type coupling that Gradle cannot express.
- Changing an allowlist requires updating this ADR.
- Sibling modules must not depend on `io.github.kazemek.jsonapi.core.internal..`. That ban is
  enforced for `jsonapi-java-jackson3` and must be added when further sibling modules register.

## Consequences

- `./gradlew clean build` fails when a guarded module's production code gains an illegal type dependency.
- Agents and contributors treat ArchUnit failures as boundary violations, not tests to delete or weaken without an ADR change.
- Additional architectural rules (cross-module `internal` bans, layer DAGs) land in follow-up milestones alongside the modules they protect.
