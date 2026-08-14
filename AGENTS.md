# Build And Verification

- Install JDK 21 locally; the Gradle toolchain requires it, and no resolver is configured to
  download it. Use the committed wrapper.
- Primary token-free verification: `./gradlew clean build`. It compiles, runs Spock/JUnit and
  ArchUnit tests, produces JaCoCo reports, and runs `spotlessCheck` through `check`.
- Run one spec with `./gradlew :<module>:test --tests '<spec FQCN>'`, for example
  `./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.validation.UpdateRequestValidationSpec'`.
- Dependency verification is checksum-enforced by `gradle/verification-metadata.xml`. After a
  dependency change or verification failure, run
  `./gradlew --refresh-dependencies --write-verification-metadata sha256 clean build`; never disable
  verification globally.
- Spotless 8.5.1 is intentionally capped in `gradle/libs.versions.toml` and `renovate.json` because
  8.6+ breaks Greclipse input fingerprinting on cold CI.

# Repository Shape

- `settings.gradle.kts` is the only source of truth for present modules; the root README also lists
  planned modules, which have no usable entry points. Read the affected module README before code.
- Sources are under `<module>/src/main/`; Spock specs are under `src/test/groovy/`, with some Java
  test fixtures under `src/test/java/`.
- `build-logic/` owns shared Java/test/nullness/coverage and Spotless configuration. Dependency
  versions are in `gradle/libs.versions.toml`; there is no code-generation step.
- `jsonapi-java-test-fixtures` is an internal, unpublished module. The version-neutral wire corpus
  is under `fixtures/jsonapi-1.1/`; pinned draft schemas are under
  `fixtures/jsonapi-schema/1.1-pr1603/`.
- Before changing shared fixtures, catalogs, or corpora, read `jsonapi-java-test-fixtures/README.md`
  and the affected documentation under `fixtures/`; those files own fixture-specific invariants.

# Task Routing

Choose the narrowest affected module or root subsystem first. Expand only for callers,
dependencies, public API effects, or repository-wide configuration.

Project skills live at `.agents/skills/<name>/SKILL.md` and require explicit invocation. Use:

| Task | Skill |
|------|-------|
| Create, refine, or decompose a plan | `implementation-planning` |
| Implement a plan under `.agentWork/plans/` | `implement-plan` |
| Review design, plan/spec, or implementation | `implementation-design-review`, `implementation-plan-review`, or `implementation-review` |
| Produce a review handoff when a fresh write-capable reviewer cannot run | `implementation-handoff` |
| Add a module or change public packages, entry points, validate/read flows, or non-goals | `module-docs` |
| Format Spotless-covered files | `spotless-format` |
| Complete production/test source work | `sonar-quality-gate` |

- For work in an existing module, use its governing live plan when one exists. If no plan covers
  requested implementation work, stop and propose a focused plan. Read `settings.gradle.kts`, the
  module README, changed-package `package-info.java`, exact sources and mirrored tests, then only
  directly relevant ADRs or conformance sections.
- Read `docs/vision.md` only for new modules, product-boundary changes, or stable direction. Read
  `docs/outlook/` only for tentative future work; Outlook is not current truth or a dependency.

# Architecture Constraints

- This library represents and validates JSON:API documents. Persistence, endpoints, authorization,
  and query execution remain application policy; do not hide policy in mapping or adapter defaults.
- `jsonapi-java-core` has no functional third-party runtime dependencies; compile-only JSpecify is
  allowed. Optional integrations belong in separate modules.
- `jsonapi-java-jackson-common` must remain free of Jackson-major imports
  (`tools.jackson.*` and `com.fasterxml.jackson.*`) despite its name.
- Preserve wire-visible distinctions: absent, explicit JSON `null`, and present-empty are different
  states. Explicit null data/linkage uses sealed model variants, not bare Java null.
- Production Java packages are JSpecify `@NullMarked`; NullAway checks `compileJava` only. ArchUnit
  enforces module dependency allowlists. Do not weaken those rules without updating
  `docs/adr/010-architectural-tests.md`.

# Knowledge And Plans

Every durable fact has one canonical owner; other documents should link or provide only navigation.

| Fact | Canonical owner |
|------|-----------------|
| Current module capability and usage | `<module>/README.md` |
| Human-readable module inventory | root `README.md` |
| Actual build membership | `settings.gradle.kts` |
| Package responsibility and invariants | `package-info.java` |
| Public API contract and semantics | Javadoc |
| Behavioral proof | tests |
| Cross-cutting architecture and rationale | accepted ADR under `docs/adr/` |
| JSON:API compliance state | `docs/conformance.md` |
| Workflow and agent routing | this file and `.agents/skills/` |
| Stable product direction | `docs/vision.md` |
| Tentative future direction | `docs/outlook/` |
| Temporary execution contract | unfinished plans under `.agentWork/plans/` |
| Backlog, prioritization, and coordination | Linear |
| Forensic history | Git |

- Snapshot (current repository evidence) and Vision are separate authorities. Surface conflicts;
  never change implementation merely to make it match Vision. Outlook overrides neither authority
  nor accepted ADRs.
- Linear is coordination and optional traceability, not engineering truth or a correctness gate.
  Never use Linear IDs or Outlook as plan dependencies. When Linear is unavailable, never infer the
  next task from live plans, Outlook, source layout, or a reconstructed backlog.
- Plans exist only while unfinished; do not create a plan index or archive. Status is only
  `Not started` or `In progress`. Dependencies are `None` or relative Markdown links to unfinished
  plan files. Once work starts, freeze its scope; after gates, synchronization, and review Pass,
  reconcile references and delete the completed plan. Session reviews belong in the gitignored
  `.agentWork/.session/`.

# Completion Gates

Classify the final diff; tiers combine when multiple scopes are touched.

| Changed paths | Required gates |
|---------------|----------------|
| Docs/planning only (`**/*.md`, `docs/**`, `.agentWork/**`) | Review links, consistency, and section order; no build |
| Workflow only (`.github/**`, `.editorconfig`, `.gitattributes`, `.gitignore`) | No local gate; CI validates workflow behavior |
| Build configuration (`**/*.gradle.kts`, `gradle/**`, `build-logic/**`, `config/**`) | `./gradlew clean build`; also Spotless if a covered file/config changed |
| Module production/test sources (`jsonapi-java-*/src/**`) | `spotless-format` -> `./gradlew clean build` -> `sonar-quality-gate` |
| Other/unclassified paths, including `fixtures/**` | Classify explicitly; otherwise use the full source tier |

- For covered `.java`, `.groovy`, `.kt`, or `.gradle.kts` changes, run `./gradlew spotlessApply`
  then `./gradlew spotlessCheck` before the build.
- Source changes are incomplete without the Sonar skill's Quality Gate wait and Issues API script
  exiting 0 (zero unresolved new-code issues). The script fails closed by default; do not treat a
  green Quality Gate or a printed non-zero `total` as success. If `SONAR_TOKEN` is unavailable,
  report the blocker; work remains incomplete until CI passes including that Issues API check.
- CI runs `./gradlew clean spotlessCheck build jacocoTestReport sonar` on `main` pushes and PRs,
  then `.agents/skills/sonar-quality-gate/scripts/check-new-code-issues.sh --list` so neither
  agents nor CI can complete on Quality Gate alone. Failed-run details are in the
  `gradle-reports` artifact and the Unit tests check.
