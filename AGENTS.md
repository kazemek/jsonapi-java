# Build & Test

Requires JDK 21 (enforced via Gradle toolchain).

`./gradlew clean build` is the token-free primary local verification (compile, tests, ArchUnit).
It is not a discovery step. Before declaring implementation complete, follow **Completion gates**
(`clean build` → `spotless-format` → `sonar-quality-gate`).

Dependency verification is enabled via `gradle/verification-metadata.xml`. After adding or
changing dependencies (or when CI fails verification), regenerate checksums with
`./gradlew --refresh-dependencies --write-verification-metadata sha256 clean build`.
Renovate’s built-in refresh (`dependencies` without `--refresh-dependencies`) can miss
checksums; on `renovate/**` PRs, CI regenerates with the command above and amends the
Renovate tip when needed so the PR stays a single commit.

IDE sync may download extra artifacts (sources jars, Gradle src zips, and Groovy 4.x
`.module`/`.pom` metadata from the Gradle distribution's "Gradle Libs" repo). Those are trusted
by pattern in `verification-metadata.xml`; do **not** disable verification globally.
Build dependencies (including Groovy 5.x used by Spock) remain checksum-verified.

# Project structure

This is a multi-module Gradle build (Kotlin DSL). `settings.gradle.kts` is the source of truth for
current submodules.

Source lives under `<module>/src/`. Tests use Groovy + Spock (`<module>/src/test/groovy/`).

## Task-scoped discovery

Choose the narrowest applicable route. Do **not** scan the whole repository first.

Project skills live at `.cursor/skills/<name>/SKILL.md`. When this file names a skill, read that
path and follow it (skills use explicit invocation only).

### Plan, refine, or decompose a milestone

When the user explicitly requests milestone planning, use the project `milestone-planning` skill.
Start with `docs/vision.md` and `.agentWork/milestones/README.md`; then read only the target
milestone, directly relevant dependencies or adjacent work, affected module documentation, and
narrow feasibility evidence.

### Implement in an existing module

1. Use the milestone index to select and read the governing milestone. If none covers the work,
   stop before coding and propose a focused milestone; create its permanent file only when the user
   explicitly requests milestone planning.
2. Read `settings.gradle.kts` and the affected `<module>/README.md`.
3. Read `package-info.java` for packages that will change.
4. Open the exact production files and mirrored tests needed for the requested behavior.
5. Open linked ADRs or conformance sections only when the change touches their contract. Follow
   additional documentation only when the task or directly implicated code requires it.

Read the full vision before implementation when adding a module, crossing module or public product
boundaries, changing project direction, or resolving a suspected vision conflict. A narrow change
already governed by a milestone and module documentation does not require rereading the full vision.

### Review an implementation

When reviewing against a milestone (including when the user requests a milestone review), read
exactly one governing milestone and establish the diff or path boundary; use the project
`milestone-review` skill for on-demand milestone reviews. For module-scoped changes, follow the
affected-module route above. When no affected module exists (repository-wide build, CI, or
workflow work), or when no milestone governs the change, follow the repository-wide route below
instead of reading irrelevant module documentation. Milestone reviews verify the `module-docs`
checklist when public module surface changed.

### Repository-wide build, CI, or workflow work

Read the root configuration, workflow, or guidance files directly implicated by the request. Read
the vision when the change can affect product direction or module boundaries; read module
documentation only for modules whose behavior or public surface can change.

### Scope expansion rule

Search inside the affected module or root subsystem first. Broaden only when direct callers,
dependencies, public API impact, or repository-wide configuration require it. Enumerate all source
files, ADRs, or milestones only when the task is inherently repository-wide.

When adding a submodule or changing a module’s public surface, use the project `module-docs` skill
to create or refresh dual-audience documentation.

## Stable project boundaries

- The library represents and validates JSON:API documents; applications retain persistence,
  endpoint, authorization, and query-execution policy.
- `jsonapi-java-core` has no functional third-party runtime dependencies; a compile-only JSpecify
  annotation jar is allowed (see ADR-009). Optional integrations belong in separate modules.
- Preserve wire-visible states such as absent versus explicit JSON `null`.
- Keep application policy explicit rather than hiding it in traversal, mapping, or adapter defaults.

# Build logic

Shared build configuration lives in `build-logic/` as precompiled script plugins.
`jsonapi-java-library` owns library, test, coverage, toolchain, and static-analysis defaults;
`jsonapi-java-spotless` owns repository formatting.

New submodules need `include("...")` in `settings.gradle.kts`, the library plugin below, and the
`module-docs` skill for dual-audience documentation and root registry updates:

```kotlin
plugins {
    id("jsonapi-java-library")
}
```

# CI

GitHub Actions runs `./gradlew clean spotlessCheck build jacocoTestReport sonar` on push to
`main` and on PRs. The project SonarCloud Quality Gate (`jsonapi-java`) fails completion on a red
gate or any new Sonar issue. Local `./gradlew clean build` remains token-free; use the
`sonar-quality-gate` skill for detailed policy and local analysis.

CI uploads a `gradle-reports` artifact (dependency-verification, test HTML, JaCoCo, and test-results)
for failure diagnosis, and publishes a Unit tests check from JUnit XML.

# Planning

* **Vision:** `docs/vision.md` — architectural strategy and long-term roadmap. Evolves as the project matures.
* **Milestones:** `.agentWork/milestones/` — concrete implementation plans. Fixed once implementation starts; retained as project documentation.
* **ADRs:** `docs/adr/` — architecture decision records (the "why" behind consequential, hard-to-reverse choices).

All feature implementation proceeds through iterative milestones. Each feature, public-surface, or
new-module change must be captured in a milestone before coding begins and produce a reviewable,
tested increment. Non-feature work (docs-only, CI/workflow, chores, or fixes already covered by an
existing Complete milestone contract) may proceed without a new milestone. Create or update an ADR
when the work makes a consequential, hard-to-reverse decision not already recorded.

An implementable milestone must fit one focused coding-agent task and reviewable commit. It normally
covers one principal capability in one primary module or layer, with at most five deliverables and
eight acceptance criteria. The `milestone-planning` skill owns detailed creation, research,
decomposition, and index synchronization.

Milestones may be refined while their status is `Not started`. Once implementation has started,
preserve the milestone as a historical delivery contract and capture changed or additional scope in
a follow-up milestone.

## Completion gates

Before declaring implementation complete:

1. If public module surface changed (packages, entry points, validate/read flows, non-goals, or
   agent-relevant invariants), use the `module-docs` skill. Skip it for internal-only or test-only
   changes.
2. Ensure `./gradlew clean build` passes.
3. Use `spotless-format` to run `./gradlew spotlessApply` and `./gradlew spotlessCheck`.
4. Use `sonar-quality-gate`; without `SONAR_TOKEN`, report completion blocked until CI passes the
   zero-new-issues Quality Gate.

# Conventions

* **Verified namespace:** Maven group `io.github.kazemek`; Java base package `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).
* **Module orientation:** Every present module documents its package map, usage (code sample or
  explicit no-entry-point note), non-goals, and agent notes in `<module>/README.md`; the root README
  is the module registry.
* **Nullness:** JSpecify `@NullMarked` packages and `@Nullable` for absence/null-preserving values (see [`docs/adr/009-jspecify-nullness.md`](docs/adr/009-jspecify-nullness.md) and module agent notes). NullAway enforces this on Java `main` sources.
* **Architectural tests:** ArchUnit enforces that `jsonapi-java-core` production types depend only on the JDK, JSpecify annotations, and other core types (see [`docs/adr/010-architectural-tests.md`](docs/adr/010-architectural-tests.md)). Do not weaken allowlists without updating the ADR; extend rules when adding modules.
* **Java 21 features:** records, sealed interfaces, pattern matching, text blocks
* **Testing:** Spock specs under `src/test/groovy/` mirroring the main package structure
