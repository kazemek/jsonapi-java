# Build & Test

Requires JDK 21 (enforced via Gradle toolchain).

`./gradlew clean build` is the token-free primary local verification (compile, tests, ArchUnit).
It is not a discovery step. Before declaring implementation complete, classify the change scope
from the diff and apply only the matching **Completion gates** (see below); docs-only and
workflow-only changes do not require a build.

Run a single Spock spec with `./gradlew :<module>:test --tests '<spec FQCN>'`, for example
`./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.validation.UpdateRequestValidationSpec'`.

Dependency verification is enabled via `gradle/verification-metadata.xml`. After adding or
changing dependencies (or when CI fails verification), regenerate checksums with
`./gradlew --refresh-dependencies --write-verification-metadata sha256 clean build`
(Renovate PRs do this as well). Never disable verification globally; trusted-by-pattern entries
already cover IDE/Gradle metadata while build dependencies stay checksum-verified.

# Project structure

Multi-module Gradle build (Kotlin DSL); `settings.gradle.kts` is the source of truth for current
submodules. Production sources live under `<module>/src/main/`; tests are Groovy + Spock under
`<module>/src/test/groovy/` mirroring the main package structure. `fixtures/jsonapi-1.1/` holds
canonical version-neutral writer fixtures that the Jackson 3 tests cross-check against.

# Task-scoped discovery

Choose the narrowest applicable route. Do **not** scan the whole repository first.

Project skills live at `.agents/skills/<name>/SKILL.md`. When this file names a skill, read that
path and follow it (skills use explicit invocation only).

- **Plan/refine/decompose a milestone:** use the `milestone-planning` skill; it verifies each
  created or refined milestone with the `milestone-plan-review` procedure in a fresh-context
  subagent.
- **Implement a milestone:** use the `implement-milestone` skill; it runs the applicable
  completion gates and verifies with the `milestone-review` procedure in a fresh-context subagent.
- **Implement in an existing module:** select the governing milestone from the index (stop and
  propose a focused milestone if none covers the work), read `settings.gradle.kts` and the
  affected `<module>/README.md`, read `package-info.java` for changed packages, open the exact
  production files and mirrored tests, and follow linked ADRs/conformance only when the change
  touches their contract.
- **Review a milestone plan/spec:** use the `milestone-plan-review` skill for on-demand reviews;
  the `milestone-planning` skill runs the same procedure in a fresh subagent.
- **Review an implementation:** use the `milestone-review` skill for on-demand reviews; the
  `implement-milestone` skill runs the same procedure in a fresh subagent.
- **Review-isolation handoff:** use `milestone-handoff` only when a write-capable fresh subagent
  cannot be spawned for plan or implementation review; it is not a primary task route.
- **Repository-wide build, CI, or workflow work:** read only the root configuration, workflow,
  or guidance files directly implicated; completion follows the gate tiers below.
- **Scope expansion:** search inside the affected module or root subsystem first; broaden only
  for direct callers, dependencies, public API impact, or repository-wide configuration.
- **New submodule or changed public surface:** use the `module-docs` skill.
- Read the full vision only when adding a module, crossing module or public product boundaries,
  or changing project direction; otherwise module documentation suffices.

# Stable project boundaries

- The library represents and validates JSON:API documents; applications retain persistence,
  endpoint, authorization, and query-execution policy.
- `jsonapi-java-core` has no functional third-party runtime dependencies; a compile-only JSpecify
  annotation jar is allowed (see ADR-009). Optional integrations belong in separate modules.
- Preserve wire-visible states such as absent versus explicit JSON `null`.
- Keep application policy explicit rather than hiding it in traversal, mapping, or adapter defaults.

# Build logic

Shared build configuration lives in `build-logic/` as precompiled script plugins:
`jsonapi-java-library` owns library, test, coverage, toolchain, and static-analysis defaults;
`jsonapi-java-spotless` owns repository formatting. Dependencies and versions are declared in the
Version Catalog `gradle/libs.versions.toml`.

New submodules need `include("...")` in `settings.gradle.kts`, the `jsonapi-java-library` plugin,
and the `module-docs` skill for dual-audience documentation and root registry updates.

# CI

GitHub Actions runs `./gradlew clean spotlessCheck build jacocoTestReport sonar` on push to
`main` and on PRs. SonarCloud Quality Gate wait is enabled, but free-tier gates do not enforce
zero new issues; completion of source-scope changes still requires the `sonar-quality-gate`
skill's Issues API check (`resolved=false` + `inNewCodePeriod=true` → `total == 0`). Local
`./gradlew clean build` remains token-free.

CI uploads a `gradle-reports` artifact (dependency-verification, test HTML, JaCoCo, and test-results)
for failure diagnosis, and publishes a Unit tests check from JUnit XML.

# Planning

- **Vision:** `docs/vision.md` — architectural strategy and long-term roadmap.
- **Milestones:** `.agentWork/milestones/` — implementation plans; the README there is the index
  and dependency order.
- **ADRs:** `docs/adr/` — the "why" behind consequential, hard-to-reverse choices.
- **Conformance:** `docs/conformance.md` — feature-by-feature JSON:API 1.1 compliance status.

Feature and public-surface work proceeds through milestones; non-feature work (docs-only, CI,
chores, or fixes already covered by a Complete milestone) may proceed without a new milestone.
A `Not started` milestone may be refined; once implementation starts it is a fixed delivery
contract and new scope goes into a follow-up milestone. `implement-milestone` moves `Status` to
`In progress` on implementation start and `Complete` only after a fresh-context review passes;
the status stays in sync between the milestone file and the index. An implementable milestone
fits one focused coding-agent task and reviewable commit (at most five deliverables and eight
acceptance criteria); the `milestone-planning` skill owns the detailed rules.

## Completion gates

Before declaring implementation complete, classify the change scope from the diff and apply the
highest applicable tier. Tiers combine: a change touching files from several tiers requires the
union of their gates.

| Change scope (touched files)                                                        | Required gates                                                                                        |
|-------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| Docs/planning only (`**/*.md`, `docs/**`, `.agentWork/**`, READMEs)                 | None — review the docs themselves (links, consistency, section order)                                 |
| Workflow only (`.github/**`, `.editorconfig`, `.gitattributes`, `.gitignore`)       | None locally — CI validates the workflow itself                                                       |
| Build configuration (`**/*.gradle.kts`, `gradle/**`, `build-logic/**`, `config/**`) | `./gradlew clean build`; Spotless when a Spotless-covered file or the formatter configuration changed |
| Production/test sources (`**/src/**`)                                               | Full: `clean build` → `spotless-format` → `sonar-quality-gate`                                        |
| Other/unclassified paths (e.g. `fixtures/**`, `LICENSE`, `opencode.jsonc`)          | Classify explicitly; otherwise apply the full source tier                                             |

Gate details:

1. If public module surface changed (packages, entry points, validate/read flows, non-goals, or
   agent-relevant invariants), use the `module-docs` skill. Skip it for internal-only or test-only
   changes.
2. Ensure `./gradlew clean build` passes when the change touches production/test sources or build
   configuration. Skip it for docs-only or workflow-only changes.
3. Use `spotless-format` (`./gradlew spotlessApply` then `./gradlew spotlessCheck`) only when the
   change touches Spotless-covered files (`.java`, `.groovy`, `.kt`, `.gradle.kts`) or the
   formatter configuration.
4. Use `sonar-quality-gate` only when the change touches production/test sources; Sonar analyzes
   new code, so it adds nothing for docs, workflow, or build-config-only changes. Without
   `SONAR_TOKEN`, report Sonar blocked for source-scope changes and keep them uncompleted until CI
   Sonar analysis succeeds and new-code issues are confirmed empty via the Issues API.

# Conventions

* **Verified namespace:** Maven group `io.github.kazemek`; Java base package `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).
* **Module orientation:** Every present module documents its package map, usage (code sample or
  explicit no-entry-point note), non-goals, and agent notes in `<module>/README.md`; the root README
  is the module registry.
* **Nullness:** JSpecify `@NullMarked` packages and `@Nullable` for absence/null-preserving values (see [`docs/adr/009-jspecify-nullness.md`](docs/adr/009-jspecify-nullness.md) and module agent notes). NullAway enforces this on Java `main` sources.
* **Architectural tests:** ArchUnit enforces production type-dependency allowlists per library module (see [`docs/adr/010-architectural-tests.md`](docs/adr/010-architectural-tests.md)). Do not weaken allowlists without updating the ADR; add rules when adding modules.
* **Java 21 features:** records, sealed interfaces, pattern matching, text blocks
* **Testing:** Spock specs under `src/test/groovy/` mirroring the main package structure
