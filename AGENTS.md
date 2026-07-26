# Build & Test

./gradlew clean build

Requires JDK 21 (enforced via Gradle toolchain).

# Project structure

Multi-module Gradle build (Kotlin DSL, Gradle 9.6.1). Submodules are listed in `settings.gradle.kts`.
Currently: `jsonapi-java-core`.

Source lives under `<module>/src/`. Tests use Groovy + Spock (`<module>/src/test/groovy/`).

# Build logic

Shared build configuration lives in `build-logic/` as a precompiled script plugin.
The convention plugin is `jsonapi-java-library` (applied via `id("jsonapi-java-library")`).

It provides: `java-library` + `groovy` plugins, JDK 21 toolchain, Spock/Groovy/ByteBuddy
test dependencies, and JUnit Platform configuration.

New submodules only need:

```kotlin
plugins {
    id("jsonapi-java-library")
}
```

# CI

GitHub Actions runs `./gradlew clean build` on push to `main` and on PRs.

# Planning

* **Vision:** `docs/vision.md` — architectural strategy and long-term roadmap. Evolves as the project matures.
* **Milestones:** `.agentWork/milestones/` — concrete implementation plans. Fixed once implemented; kept as project documentation.
* **ADRs:** `docs/adr/` — architecture decision records (the "why" behind consequential, hard-to-reverse choices).

All feature implementation proceeds through iterative milestones. Each planned change must be captured in a milestone before coding begins. A milestone produces a committed, tested increment with a passing build.

## Agent Workflow

Before implementing any work, a coding agent MUST:

1. **Read `docs/vision.md`** to understand the project direction.
2. **Check for an existing milestone** in `.agentWork/milestones/` that covers the requested work.
3. If no milestone exists, **propose one** (scope, deliverables, acceptance criteria) before writing code.
4. **Verify alignment with the vision.** If the requested work diverges from the vision, flag it. If the divergence is reasonable, propose an update to `docs/vision.md` before or alongside implementation.
5. **Create or update ADRs** in `docs/adr/` when a significant architectural decision is made that isn't already documented.

## Agent-Driven Code Reviews

Milestone reviews are performed on demand. When a user requests a milestone review, use the project `milestone-review` skill and review the implementation against exactly one corresponding file under `.agentWork/milestones/`.

Write the result to `.agentWork/.session/milestone-review-<milestone-basename>.md`. Session reviews are ephemeral, non-canonical working artifacts: they do not replace milestones, the vision, or ADRs, and a later review of the same milestone overwrites the previous artifact.

# Conventions

* **Verified namespace:** Maven group `io.github.kazemek`; Java base package `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).
* **Package suffixes:** `core.model`, `core.validation`, `annotation`, `jackson`, `query`, and adapter-specific Spring packages under the verified base.
* **Java 21 features:** records, sealed interfaces, pattern matching, text blocks
* **Testing:** Spock specs under `src/test/groovy/` mirroring the main package structure
