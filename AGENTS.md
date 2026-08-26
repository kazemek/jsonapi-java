# Build And Verification

- Install JDK 21 locally; the Gradle toolchain requires it, and no resolver is configured to
  download it. Use the committed wrapper.
- Primary token-free verification: `./gradlew clean build`. It compiles, runs Spock/JUnit and
  ArchUnit tests, produces JaCoCo reports, enforces per-module JaCoCo instruction/branch floors
  through `check` (numbers only in `build-logic/.../jsonapi-java-library.gradle.kts`; policy in the
  root README Build section), and runs `spotlessCheck` through `check`.
- Run one spec with `./gradlew :<module>:test --tests '<spec FQCN>'`, for example
  `./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.validation.UpdateRequestValidationSpec'`.
  Ordinary incremental and scoped test execution is the default once task inputs are correct;
  do not use `--rerun-tasks` as routine workflow.
- `clean` alone does not prove tests re-executed rather than being restored from the build
  cache. `--no-build-cache` is an exceptional troubleshooting or final-verification path: it
  disables build-cache reuse only and does not disable ordinary up-to-date checking. For a
  deliberately cache-free full verification, use `./gradlew clean build --no-build-cache`.
- Dependency verification is checksum-enforced by `gradle/verification-metadata.xml`. After a
  dependency change or verification failure, run
  `./gradlew --refresh-dependencies --write-verification-metadata sha256 clean build`; never disable
  verification globally.

# Repository Shape

- `settings.gradle.kts` is the only source of truth for present modules; the root README also lists
  planned modules, which have no usable entry points. Read the affected module README before code.
- Sources are under `<module>/src/main/`; Spock specs are under `src/test/groovy/`, with some Java
  test fixtures under `src/test/java/`.
- `build-logic/` owns shared Java/test/nullness/coverage and Spotless configuration. Dependency
  versions are in `gradle/libs.versions.toml`; there is no code-generation step.
- `jsonapi-java-test-support` is an internal, unpublished module. The version-neutral wire corpus
  is on its classpath under `src/main/resources/jsonapi/corpus/1.1/`; pinned draft schemas are
  vendored under `src/main/resources/jsonapi/schema/vendor/1.1-pr1603/`.
- Before changing shared fixtures, catalogs, or corpora, read `jsonapi-java-test-support/README.md`
  and the affected corpus/schema resource READMEs; those files own fixture-specific invariants.

# Task Routing

Choose the narrowest affected module or root subsystem first. Expand only for callers,
dependencies, public API effects, or repository-wide configuration.

Skills live at `.agents/skills/<name>/SKILL.md`. Lifecycle workflow entrypoints are entered only
through explicit maintainer request or authorization; workflow-support skills execute as part of an
already-authorized workflow per that workflow's documented contract. The routing tables below are
descriptive, not an automatic task-matching router:

**Lifecycle workflow entrypoints** (entering one changes or establishes lifecycle/authorization context):

| User intent | Lifecycle workflow entrypoint |
|-------------|-------------------------------|
| Plan one coherent implementation increment | `implementation-planning` |
| Request an independent design challenge | `implementation-design-review` |
| Request an independent plan review | `implementation-plan-review` |
| Implement requested work | `implement-work` |

**Workflow-support skills** (may execute within an authorized workflow; may also be requested directly):

| Trigger / purpose | Workflow-support skill |
|-------------------|------------------------|
| Review implementation against the requested outcome | `implementation-review` |
| Produce a fresh-session review handoff when required | `implementation-handoff` |
| Add a module or change public packages, entry points, validate/read flows, or non-goals | `module-docs` |
| Format Spotless-covered files | `spotless-format` |
| Complete production/test source work | `sonar-quality-gate` |

**Planning uses the capabilities available in the current session.** Establish facts from repository
evidence and primary sources. When executable tools are available, bounded planning spikes may be
used when they materially reduce uncertainty. If an important planning action cannot be performed
with the available capabilities, surface the limitation to the maintainer. The repository does not
depend on, or prescribe, any particular harness mode.

Informal planning and exploration may happen in any session using the capabilities available there.
The `implementation-planning` workflow is a defined engineering workflow: it requires the
capabilities necessary to execute reliably and must not silently degrade into a partial version when
those capabilities are unavailable.

- A lifecycle workflow entrypoint must not be entered merely because the current task appears to
  match it.
- Top-level lifecycle workflow entrypoints require explicit maintainer request or authorization,
  which the harness may realize semantically rather than via literal slash syntax. An authorized
  workflow may execute, delegate to, or load workflow-support instructions, or execute an explicitly
  authorized nested lifecycle workflow, according to its documented contract and the capabilities
  available in the current session. Using a workflow-support skill never grants authority to enter a
  new lifecycle workflow or to transition from planning to implementation.
- If requested implementation work is not one coherent increment against current repository reality,
  stop with **Needs decomposition** and return control to the user/coordinating layer. Do not invent
  a repository multi-plan DAG.
- A direct user/maintainer request or authorization to enter a lifecycle workflow is sufficient
  authority for that workflow. No external tracker is required. `implement-work` remains the single
  implementation authorization boundary.
- Planning is never automatic: planning ends ready for implementation, and implementation begins
  only when the maintainer explicitly requests or authorizes the `implement-work` lifecycle workflow.
  There is no separate mandatory `Build now?` confirmation; explicit maintainer intent is the
  authorization.
- Design Review and Plan Review are optional, advisory, and maintainer-controlled. Neither grants
  implementation permission. An authorized workflow may execute an approved review directly rather
  than requiring another explicit instruction.
- Read `settings.gradle.kts`, the module README, changed-package `package-info.java`, exact sources
  and mirrored tests, then only directly relevant ADRs or conformance sections. Read
  `docs/architecture.md` when the work is cross-module.
- Read `docs/vision.md` only for new modules, product-boundary changes, or stable direction.

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
| Current cross-module architecture mental model | `docs/architecture.md` |
| Human-readable module inventory | root `README.md` |
| Actual build membership | `settings.gradle.kts` |
| Package responsibility and invariants | `package-info.java` |
| Public API contract and semantics | Javadoc |
| Behavioral proof | tests |
| Cross-cutting architecture rationale | accepted ADR under `docs/adr/` |
| JSON:API compliance state | `docs/conformance.md` |
| Workflow and agent routing | this file and `.agents/skills/` |
| Planning/review finding severity and stage ownership | `.agents/skills/review-findings.md` |
| Stable product direction | `docs/vision.md` |
| Requested work intent / acceptance intent | Explicit user/maintainer request, or external coordinating layer when present |
| Backlog, prioritization, decomposition, blockers | External coordinating layer when present |
| Temporary engineer/agent planning memory | gitignored `.agentWork/plans/*.md` and `.agentWork/.session/spikes/` (not engineering truth) |
| Temporary review/session context | gitignored `.agentWork/.session/` |
| Forensic history | Git |

- Snapshot (current repository evidence) and Vision are separate authorities. Surface conflicts;
  never change implementation merely to make it match Vision.
- External coordinating-layer metadata is optional coordination and traceability, not engineering
  truth or a correctness gate. When a coordinator is unavailable, never invent backlog order from
  local plans, source layout, or Git history; report coordination as unsynchronized if asked to
  sync.
- Local working plans are optional, gitignored, and disposable. They are not backlog entries,
  roadmaps, dependency graphs, or permanent architecture docs. Delete them when no longer useful.
  Do not create a plans index or archive.
- Implementation Review evaluates requested outcome / acceptance intent + actual diff + repository
  contracts/docs/tests + applicable gates. It must not reconstruct the requested work from a local
  plan, branch names, a tracker, Git history, or inferred code alone.
- Committed workflow skills are tracker-agnostic. Tracker-specific conventions belong only in
  maintainer-local coordinating skills outside the public workflow.

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
