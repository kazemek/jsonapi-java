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

# Build logic

Shared build configuration lives in `build-logic/` as precompiled script plugins.

- `jsonapi-java-library` (applied via `id("jsonapi-java-library")`) provides: `java-library` +
  `groovy` + `jacoco` plugins, JDK 21 toolchain, Spock/Groovy/ByteBuddy test dependencies,
  JUnit Platform configuration, and JaCoCo XML/HTML reports after tests.
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
enabled, so a red Quality Gate fails the job. Local `./gradlew clean build` does not need
Sonar; formatting is enforced via Spotless (`spotlessCheck`).

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

6. Ensure `./gradlew clean build` passes.
7. Use the project `spotless-format` skill to run `./gradlew spotlessApply` then
   `./gradlew spotlessCheck`.
8. Use the project `sonar-quality-gate` skill to run SonarCloud analysis with Quality Gate wait.
   - Local `./gradlew clean build` remains token-free; Sonar is a separate completion gate.
   - Without `SONAR_TOKEN`, do not claim completion: report that Sonar is blocked and CI must still pass the Quality Gate.

## Agent-Driven Code Reviews

Milestone reviews are performed on demand. When a user requests a milestone review, use the project `milestone-review` skill and review the implementation against exactly one corresponding file under `.agentWork/milestones/`.

Write the result to `.agentWork/.session/milestone-review-<milestone-basename>.md`. Session reviews are ephemeral, non-canonical working artifacts: they do not replace milestones, the vision, or ADRs, and a later review of the same milestone overwrites the previous artifact.

Formatting checks for task completion use the project `spotless-format` skill (see Agent Workflow step 7).
Sonar Quality Gate checks for task completion use the project `sonar-quality-gate` skill (see Agent Workflow step 8).

# Conventions

* **Verified namespace:** Maven group `io.github.kazemek`; Java base package `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).
* **Package suffixes:** `core.model`, `core.validation`, `annotation`, `jackson`, `query`, and adapter-specific Spring packages under the verified base.
* **Core orientation:** See `jsonapi-java-core/README.md` for package map, validate flow, and local vs aggregate validation.
* **Java 21 features:** records, sealed interfaces, pattern matching, text blocks
* **Testing:** Spock specs under `src/test/groovy/` mirroring the main package structure
