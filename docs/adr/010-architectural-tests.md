# ADR-010: Architectural Tests for Module Boundaries

**Status:** Accepted  
**Date:** 2026-07-29

## Context

ADR-007 and the vision require `jsonapi-java-core` to stay free of third-party runtime dependencies. Gradle dependency declarations guard the published classpath, but they do not stop production sources from referring to types that appear on the compile or test classpath by mistake, or (later) from sibling modules reaching into non-public packages such as `core.internal`.

JSpecify (`org.jspecify.annotations`) is an intentional compile-only exception (ADR-009) and must remain allowed in core sources.

## Decision

- Enforce package and type dependency rules with [ArchUnit](https://www.archunit.org/) as a **`testImplementation`** dependency. ArchUnit must never appear on the published runtime classpath.
- First rule (Phase 0.9): production classes under `io.github.kazemek.jsonapi.core..` may depend only on `java..`, `org.jspecify.annotations..`, and other `io.github.kazemek.jsonapi.core..` types.
- Gradle continues to own artifact selection and publication; ArchUnit owns package/type coupling that Gradle cannot express.
- Changing the allowlist requires updating this ADR.
- Future sibling modules (jackson, query, Spring adapters) must not depend on `io.github.kazemek.jsonapi.core.internal..`. That rule is intentional policy here and will be implemented when those modules exist.

## Consequences

- `./gradlew clean build` fails when core production code gains an illegal type dependency.
- Agents and contributors treat ArchUnit failures as boundary violations, not tests to delete or weaken without an ADR change.
- Additional architectural rules (cross-module `internal` bans, layer DAGs) land in follow-up milestones alongside the modules they protect.
