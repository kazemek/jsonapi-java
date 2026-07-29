# Build & Test

./gradlew clean build

Requires JDK 21 (enforced via Gradle toolchain).

Dependency verification is enabled via `gradle/verification-metadata.xml`. After adding or
changing dependencies (or when CI fails verification), regenerate checksums with
`./gradlew --refresh-dependencies --write-verification-metadata sha256 clean build`
(Renovate PRs do this as well).

IDE sync may download extra artifacts (sources jars, Gradle src zips, and Groovy 4.x
`.module`/`.pom` metadata from the Gradle distribution's "Gradle Libs" repo). Those are trusted
by pattern in `verification-metadata.xml`; do **not** disable verification globally.
Build dependencies (including Groovy 5.x used by Spock) remain checksum-verified.

# Project structure

Multi-module Gradle build (Kotlin DSL, Gradle 9.6.1). Submodules are listed in `settings.gradle.kts`.
Currently: `jsonapi-java-core`.

Source lives under `<module>/src/`. Tests use Groovy + Spock (`<module>/src/test/groovy/`).

## Targeted discovery

When implementing or reviewing work in a submodule, gather knowledge in this order. Do **not** scan the whole repository first.

1. Read `settings.gradle.kts` to identify the affected submodule(s).
2. Read `<module>/README.md` for packages, minimal usage, non-goals, and agent notes.
3. Read `package-info.java` under the packages you will touch.
4. Follow only the ADRs and conformance links listed in that module README.
5. Then open the narrow code and tests under that module.

Root planning docs (`docs/vision.md`, `.agentWork/milestones/`, `docs/adr/`) orient the overall change. Module READMEs orient implementation inside a submodule. Planned modules (`jsonapi-java-annotations`, `jsonapi-java-jackson`, `jsonapi-java-query`, `jsonapi-java-spring-webmvc`, and a later WebFlux evaluation) follow the same rule once their README exists: read that module’s README first.

When adding a submodule or changing a module’s public surface, use the project `module-docs` skill to create or refresh dual-audience documentation.

# Build logic

Shared build configuration lives in `build-logic/` as precompiled script plugins.

- `jsonapi-java-library` (applied via `id("jsonapi-java-library")`) provides: `java-library` +
  `groovy` + `jacoco` plugins, JDK 21 toolchain, JSpecify `compileOnly`, Error Prone + NullAway
  on Java `main` sources, Spock/Groovy/ByteBuddy test dependencies, JUnit Platform configuration,
  and JaCoCo XML/HTML reports after tests.
- `jsonapi-java-spotless` (applied at the root via `id("jsonapi-java-spotless")`) configures
  Spotless for Java, Groovy/Spock, Kotlin, and Gradle Kotlin DSL. Greclipse settings live in
  `config/spotless/greclipse.properties`.

New submodules only need:

```kotlin
plugins {
    id("jsonapi-java-library")
}
```

# CI

GitHub Actions runs `./gradlew clean spotlessCheck build jacocoTestReport sonar` on push to
`main` and on PRs (requires repository secret `SONAR_TOKEN`). `sonar.qualitygate.wait` is
enabled, so a red Quality Gate fails the job. The project Quality Gate is named `jsonapi-java`
(copy of Sonar way plus **any new issue fails**: `new_violations` > 0). Associate it in
SonarCloud under Project Settings → Quality Gate if the project still shows Sonar way.
Local `./gradlew clean build` does not need Sonar; formatting is enforced via Spotless
(`spotlessCheck`).

CI uploads a `gradle-reports` artifact (dependency-verification, test HTML, JaCoCo, and test-results)
and publishes a Unit tests check from JUnit XML when present. Download the artifact from the workflow
run to inspect HTML reports after failures.

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

After implementation, before declaring the work complete, a coding agent MUST:

6. If public module surface changed (packages, entry points, validate/read flows, non-goals, or
   agent-relevant invariants), use the project `module-docs` skill to update that module’s
   documentation. Skip when only internals or tests changed with no surface impact.
7. Ensure `./gradlew clean build` passes.
8. Use the project `spotless-format` skill to run `./gradlew spotlessApply` then
   `./gradlew spotlessCheck`.
9. Use the project `sonar-quality-gate` skill to run SonarCloud analysis with Quality Gate wait.
   - Local `./gradlew clean build` remains token-free; Sonar is a separate completion gate.
   - Without `SONAR_TOKEN`, do not claim completion: report that Sonar is blocked and CI must still pass the Quality Gate.
   - Completion requires a green gate under the project policy of **zero new issues**.

## Milestone Planning

When a user explicitly requests milestone creation, refinement, or breakdown, use the project
`milestone-planning` skill. It performs targeted exploration and research, writes the actual files
under `.agentWork/milestones/`, and updates the milestone dependency order and index.

An implementable milestone must fit one focused coding-agent task and reviewable commit. It should
normally cover one principal capability in one primary module or layer, with at most five
deliverables and eight acceptance criteria. Split independent capabilities, modules, architectural
decisions, or verification surfaces into ordered milestones rather than allowing implementation
context to grow without bound.

Milestones may be refined while their status is `Not started`. Once implementation has started,
preserve the milestone as a historical delivery contract and capture changed or additional scope in
a follow-up milestone.

## Agent-Driven Code Reviews

Milestone reviews are performed on demand. When a user requests a milestone review, use the project `milestone-review` skill and review the implementation against exactly one corresponding file under `.agentWork/milestones/`.

Write the result to `.agentWork/.session/milestone-review-<milestone-basename>.md`. Session reviews are ephemeral, non-canonical working artifacts: they do not replace milestones, the vision, or ADRs, and a later review of the same milestone overwrites the previous artifact.

Milestone planning and refinement use the project `milestone-planning` skill (see Milestone Planning).
Module documentation updates use the project `module-docs` skill when public surface changed (see Agent Workflow step 6).
Formatting checks for task completion use the project `spotless-format` skill (see Agent Workflow step 8).
Sonar Quality Gate checks for task completion use the project `sonar-quality-gate` skill (see Agent Workflow step 9).

# Conventions

* **Verified namespace:** Maven group `io.github.kazemek`; Java base package `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).
* **Package suffixes:** `core.model`, `core.validation`, `annotation`, `jackson`, `query`, and adapter-specific Spring packages under the verified base.
* **Module orientation:** See [`jsonapi-java-core/README.md`](jsonapi-java-core/README.md) for package map, usage, non-goals, and agent notes. Additional modules follow the same `<module>/README.md` pattern once present.
* **Nullness:** JSpecify `@NullMarked` packages and `@Nullable` for absence/null-preserving values (see [`docs/adr/009-jspecify-nullness.md`](docs/adr/009-jspecify-nullness.md) and module agent notes). NullAway enforces this on Java `main` sources.
* **Architectural tests:** ArchUnit enforces that `jsonapi-java-core` production types depend only on the JDK, JSpecify annotations, and other core types (see [`docs/adr/010-architectural-tests.md`](docs/adr/010-architectural-tests.md)). Do not weaken allowlists without updating the ADR; extend rules when adding modules.
* **Java 21 features:** records, sealed interfaces, pattern matching, text blocks
* **Testing:** Spock specs under `src/test/groovy/` mirroring the main package structure
