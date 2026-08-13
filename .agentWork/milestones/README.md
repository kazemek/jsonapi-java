# Implementation Milestones

Milestones are planned, testable increments. They may change until implementation starts; after completion they are retained as documentation of the delivered contract.

## Planned execution order

Preferred sequencing for agents and humans. It is not a strict dependency graph: milestones later
in this list may run earlier when their own **Dependencies** headers allow (for example Jackson 2
parity after common contracts/fixtures without waiting on Spring). Each milestone’s **Dependencies**
field remains the authoritative prerequisite set.

1. **Phase 0.1 — Public namespace:** gates every public source package.
2. **Phase 0.2 — milestone review workflow:** defines the repository's on-demand review process.
3. **Phase 0.3 — CI quality and supply chain:** coverage, Sonar Quality Gate, verification, Action digests, and CI report artifacts.
4. **Phase 0.4 — Spotless formatting:** repo-wide Spotless via a root convention plugin, greclipse for Spock, and an agent format completion skill.
5. **Phase 0.5 — Module docs workflow:** targeted module discovery in `AGENTS.md` and a `module-docs` skill for dual-audience module documentation.
6. **Phase 0.6 — Line ending enforcement:** LF via `.gitattributes` / `.editorconfig`, CRLF only for Windows batch scripts.
7. **Phase 0.7 — milestone planning workflow:** creates, refines, and decomposes context-bounded implementation milestones.
8. **Phase 0.8 — JSpecify nullness:** JSpecify `@NullMarked` packages, NullAway enforcement, and portable agent guidance for `jsonapi-java-core`.
9. **Phase 0.9 — ArchUnit core dependency guard:** ArchUnit enforces JDK + JSpecify + self type deps for `jsonapi-java-core`.
10. **Phase 0.10 — task-scoped discovery and documentation pattern:** unifies agent routing and documentation checklists after Phases 0.2, 0.5, and 0.7.
11. **Phase 0.11 — implement-milestone workflow:** implements one milestone end-to-end and verifies it with a fresh-context milestone review and a bounded fix loop.
12. **Phase 0.12 — milestone plan-review workflow:** verifies create/refine/decompose with a
    fresh-context plan review and a bounded fix loop.
13. **Phase 1.1 — document model/validation** and **Phase 1.2 — annotations:** completed,
    independent foundations with no functional third-party runtime dependencies.
14. **Phase 1.3 — resource update request validation:** completed; depends on Phase 1.1 and
    feeds later PATCH binding.
15. **Phase 1.4 — core identity/linkage hardening:** follows Phase 1.1/1.3; closes alias-aware
    identifier-collection uniqueness and related core regressions without Phase 4.1 scope.
16. **Phase 1.5 — error source pointer conformance:** follows Phase 1.1/1.4; RFC 6901 syntax for
   `ErrorSource.pointer` without document resolution or Phase 4.1 scope.
17. **Phase 1.6 — links additional member conformance:** follows Phase 1.1/1.5; reserves
   context-standard link names out of `Links.additionalMembers` without Phase 4.1 scope.
18. **Phase 2.1 — Jackson 3 document writer:** creates the first major-specific codec artifact and
   proves deterministic model-to-wire behavior.
19. **Phase 2.2 — Jackson 3 write mapping**, **Phase 2.4 — document reader**, and **Phase 2.5 —
   draft-schema cross-check:** completed; **Phase 2.3 — compound serialization** and **Phase 2.9 —
   flat DTO reader:** may proceed in parallel after their listed dependencies.
20. **Phase 2.3 — Jackson 3 compound serialization** and **Phase 2.9 — flat DTO reader:** add
   explicit inclusion and validated resource-to-DTO binding independently.
21. **Phase 2.8 — Jackson 3 sparse fieldsets** and **Phase 2.10 — typed domain envelope:** build on
   their respective compound and flat-read foundations.
22. **Phase 2.11 — Jackson common contracts:** extracts Jackson-import-free public policy,
   diagnostic, context, and envelope types into `jsonapi-java-jackson-common` and migrates
   Jackson 3 onto them before any Jackson 2 work.
23. **Phase 2.12 — canonical codec fixtures:** capability-tags the shared document corpus for
   write/read/schema parity and adds a shared read-only negative corpus.
24. **Phase 2.13 — shared domain-write fixtures:** owns the shared Java domain-write models,
    operation catalog, and the Jackson 3 write-suite migration; adapter suites must run the whole
    catalog (mandatory for Jackson 2 per Phase 2.18).
25. **Phase 2.27 — Java codec fixture contract** and **Phase 2.28 — unified scenario retrieval:**
    the very next milestones after Phase 2.13. They convert the codec fixture branch to pure Java and
    establish the `Scenario` / `FixtureCatalog` contract, the `JsonApiFixtures` facade, and the
    centralized fixtures-directory resolution. Every later fixture milestone and the Jackson 2
    parity track build on this unified Java surface, so nothing is built on the superseded
    dual-language fixture design and changed later.
26. **Phase 2.14 — shared domain-read fixtures:** follows Phase 2.13 and reuses its shared models
    while extracting the flat DTO-binding catalog from `ResourceBinderSpec` onto the Phase 2.28
    `Scenario` surface (relaxed additive contract, no exclusion manifests).
27. **Phase 2.24 — shared compound write fixtures**, **Phase 2.25 — shared sparse-fieldset fixtures**,
    and **Phase 2.26 — shared envelope read fixtures:** extract the remaining domain-fixture catalogs
    on the Phase 2.28 surface, before any feature or parity work builds on them.
28. **Phase 2.15 — Jackson 3 PATCH binding:** the first non-fixture feature milestone after the
    fixture surface; composes document reading, Phase 1.3 update validation, and presence-aware
    binding into commands and the shared `PatchScenarios` catalog (not typed envelopes).
29. **Phase 3.1 — query parser:** remains an independent optional artifact.
30. **Phase 3.2 — Spring WebMVC document transport:** integrates media negotiation, validated
    documents, query arguments, and safe errors.
31. **Phase 3.3 — Spring WebMVC flat DTO binding:** adds the primary Jackson 3/Spring DTO and
    typed-envelope experience.
32. **Phase 3.4 — Spring WebMVC PATCH binding:** adds presence-aware PATCH command arguments on
    top of Phase 3.3.
33. **Phase 3.5 — WebFlux evaluation:** begins after document and DTO-oriented WebMVC behavior is
    stable.
34. **Phase 2.16 — Jackson 2 document writer:** starts the parity track after the unified Java
    scenario surface (Phases 2.27–2.28 and 2.14–2.15, 2.24–2.26), without an artificial Spring
    dependency.
35. **Phase 2.17 — Jackson 2 document reader** and **Phase 2.18 — domain mapping:** may proceed in
    parallel after the Jackson 2 writer and their respective Jackson 3 / fixture contracts.
36. **Phase 2.19 — Jackson 2 compound serialization** and **Phase 2.21 — flat DTO reader:** build
    independently on stable mapping/read contracts and shared domain fixtures (2.19 needs 2.24;
    2.21 needs 2.14).
37. **Phase 2.20 — Jackson 2 sparse fieldsets** and **Phase 2.22 — typed domain envelope:** finish
    write-policy and read-envelope parity independently (2.20 needs 2.25; 2.22 needs 2.26).
38. **Phase 2.23 — Jackson 2 PATCH binding:** completes presence-aware DTO parity after the
    Jackson 2 document reader (2.17) and flat DTO reader (2.21); does not depend on envelopes (2.22).
39. **Phase 4.1 — conformance and hardening.**
40. **Phase 4.2 — stable release.**

## Milestone index

Each entry is `milestone — module/scope — status`; use it to select a candidate before opening a
milestone file.

- [Phase 0.1 — Public Namespace Decision](phase-0-1-public-namespace.md) — all modules — Complete
- [Phase 0.2 — Milestone Review Workflow](phase-0-2-milestone-review-workflow.md) — repository workflow — Complete
- [Phase 0.3 — CI Quality and Supply Chain](phase-0-3-ci-quality-and-supply-chain.md) — build, CI, and agent completion — Complete
- [Phase 0.4 — Spotless Formatting](phase-0-4-spotless-formatting.md) — build and agent completion — Complete
- [Phase 0.5 — Module Docs Discovery and Maintenance](phase-0-5-module-docs-workflow.md) — repository workflow — Complete
- [Phase 0.6 — Line Ending Enforcement](phase-0-6-line-endings.md) — repository hygiene — Complete
- [Phase 0.7 — Milestone Planning Workflow](phase-0-7-milestone-planning-workflow.md) — repository workflow — Complete
- [Phase 0.8 — JSpecify Nullness](phase-0-8-jspecify-nullness.md) — build, core, and agent guidance — Complete
- [Phase 0.9 — ArchUnit Core Dependency Guard](phase-0-9-archunit-core-deps.md) — core architecture and agent guidance — Complete
- [Phase 0.10 — Task-Scoped Discovery and Documentation Pattern](phase-0-10-task-scoped-discovery-and-doc-pattern.md) — repository workflow and agent guidance — Complete
- [Phase 0.11 — Implement-Milestone Workflow](phase-0-11-implement-milestone-workflow.md) — repository workflow — Complete
- [Phase 0.12 — Milestone Plan-Review Workflow](phase-0-12-milestone-plan-review-workflow.md) — repository workflow — Complete
- [Phase 1.1 — Document Model and Validation](phase-1-1-spec-data-model.md) — `jsonapi-java-core` — Complete
- [Phase 1.2 — Domain-Mapping Annotations](phase-1-2-annotations.md) — `jsonapi-java-annotations` — Complete
- [Phase 1.3 — Resource Update Request Validation](phase-1-3-update-request-validation.md) — `jsonapi-java-core` — Complete
- [Phase 1.4 — Core Identity and Linkage Hardening](phase-1-4-core-identity-linkage-hardening.md) — `jsonapi-java-core` — Complete
- [Phase 1.5 — Error Source Pointer Conformance](phase-1-5-error-source-pointer-conformance.md) — `jsonapi-java-core` — Complete
- [Phase 1.6 — Links Additional Member Conformance](phase-1-6-links-additional-member-conformance.md) — `jsonapi-java-core` — Complete
- [Phase 2.1 — Jackson 3 Document Writer](phase-2-1-jackson-document-codec.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.2 — Jackson 3 Domain-to-Resource Mapping](phase-2-2-domain-resource-mapping.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.3 — Jackson 3 Compound Serialization Context](phase-2-3-compound-serialization.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.4 — Jackson 3 Document Reader](phase-2-4-document-reads.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.5 — JSON:API 1.1 Draft-Schema Cross-Check](phase-2-5-json-schema-cross-check.md) — `jsonapi-java-jackson3` test suite — Complete
- [Phase 2.8 — Jackson 3 Sparse Fieldsets](phase-2-8-sparse-fieldsets.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.9 — Jackson 3 Flat DTO Reader](phase-2-9-jackson3-flat-dto-reader.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.10 — Jackson 3 Typed Domain Envelope](phase-2-10-jackson3-domain-envelope.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.11 — Jackson Common Contracts](phase-2-11-jackson-common-contracts.md) — `jsonapi-java-jackson-common` / `jsonapi-java-jackson3` — Complete
- [Phase 2.12 — Canonical Codec Fixture Contract](phase-2-12-canonical-codec-fixtures.md) — fixtures / `jsonapi-java-test-fixtures` / jackson3 codec tests — Complete
- [Phase 2.13 — Shared Domain Write Test Fixtures](phase-2-13-shared-domain-write-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `ResourceMapperSpec` — Complete
- [Phase 2.14 — Shared Domain Read Test Fixtures](phase-2-14-shared-domain-read-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `ResourceBinderSpec` — Complete
- [Phase 2.15 — Jackson 3 Presence-Aware PATCH Binding](phase-2-15-jackson3-patch-binding.md) — `jsonapi-java-jackson3` / `jsonapi-java-jackson-common` / `jsonapi-java-test-fixtures` — Not started
- [Phase 2.16 — Jackson 2 Document Writer](phase-2-16-jackson2-document-writer.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.17 — Jackson 2 Document Reader](phase-2-17-jackson2-document-reader.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.18 — Jackson 2 Domain-to-Resource Mapping](phase-2-18-jackson2-domain-resource-mapping.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.19 — Jackson 2 Compound Serialization](phase-2-19-jackson2-compound-serialization.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.20 — Jackson 2 Sparse Fieldsets](phase-2-20-jackson2-sparse-fieldsets.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.21 — Jackson 2 Flat DTO Reader](phase-2-21-jackson2-flat-dto-reader.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.22 — Jackson 2 Typed Domain Envelope](phase-2-22-jackson2-domain-envelope.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.23 — Jackson 2 Presence-Aware PATCH Binding](phase-2-23-jackson2-patch-binding.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.24 — Shared Compound Write Test Fixtures](phase-2-24-shared-compound-write-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `CompoundSerializationSpec` — Not started
- [Phase 2.25 — Shared Sparse-Fieldset Write Test Fixtures](phase-2-25-shared-sparse-fieldset-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `SparseFieldsetSpec` — Not started
- [Phase 2.26 — Shared Typed Envelope Read Test Fixtures](phase-2-26-shared-envelope-read-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `DomainDocumentReaderSpec` — Not started
- [Phase 2.27 — Java Codec Fixture Contract](phase-2-27-java-codec-fixture-contract.md) — fixtures / `jsonapi-java-test-fixtures` / ADR-010 — Complete
- [Phase 2.28 — Unified Scenario Retrieval](phase-2-28-unified-scenario-retrieval.md) — fixtures / `jsonapi-java-test-fixtures` / jackson3 specs — Complete
- [Phase 3.1 — Optional Query-Parameter Parser](phase-3-1-query-parameters.md) — `jsonapi-java-query` — Not started
- [Phase 3.2 — Spring WebMVC Adapter](phase-3-2-spring-webmvc.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.3 — Spring WebMVC Flat DTO Binding](phase-3-3-spring-webmvc-dto-binding.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.4 — Spring WebMVC Presence-Aware PATCH Binding](phase-3-4-spring-webmvc-patch-binding.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.5 — WebFlux Adapter Evaluation](phase-3-5-webflux-evaluation.md) — candidate `jsonapi-java-spring-webflux` — Not started
- [Phase 4.1 — Conformance and Hardening](phase-4-1-conformance-and-hardening.md) — all implemented modules — Not started
- [Phase 4.2 — Stable Release](phase-4-2-stable-release.md) — publication and compatibility — Not started

Every implementation milestone must finish with the completion gates applicable to its change
scope (see the change-scope gate tiers in `AGENTS.md`); code milestones end with the relevant
module tests and `./gradlew clean build` passing.

## Milestone planning

Use the explicitly invoked project `milestone-planning` skill to create, refine, or decompose
milestones. It performs targeted exploration and relevant authoritative research, writes the
permanent milestone files in this directory, updates both the dependency order and index, and then
runs the `milestone-plan-review` procedure in a fresh-context subagent so the review is not
influenced by the planning session. Findings are fixed and re-reviewed with a new subagent, capped
at two re-reviews. Planning is complete only after a plan-review Pass for each created or refined
milestone.

An implementable milestone must fit one focused coding-agent task and reviewable commit. It
normally contains one principal capability in one primary module or layer, at most five
deliverables, and at most eight acceptance criteria. Independent capabilities, modules,
architectural decisions, or verification surfaces are separate milestones with explicit
dependencies.

A `Not started` milestone may be refined or decomposed. Once implementation starts, the milestone
is a fixed delivery contract; changed or additional scope belongs in a follow-up milestone.

## Milestone implementation

Implement a milestone with the explicitly invoked project `implement-milestone` skill. It resolves
one milestone, reads its contract and affected module documentation, implements within the
milestone boundaries, runs the completion gates, and then runs the `milestone-review` procedure in
a fresh-context subagent so the review is not influenced by the implementing session. Findings are
fixed and re-reviewed with a new subagent, capped at two re-reviews. Completion gates are re-run
after every fix batch and before the next review, so `Complete` is based on the post-fix results.

The milestone `Status` moves `Not started` → `In progress` when implementation starts and
`Complete` only after a review `Pass`. The status stays in sync between the milestone file and the
index entry. Acceptance criteria are marked `[x]` by the implementer as
evidence; the review verifies them but never edits them.

## Milestone reviews

Milestones are permanent delivery contracts. Two ephemeral review kinds write under
`.agentWork/.session/` and are excluded from version control; each re-review overwrites the prior
artifact for that milestone.

- **Plan/spec review:** on-demand reviews of a milestone contract against planning rules use the
  project `milestone-plan-review` skill and write
  `.agentWork/.session/milestone-plan-review-<milestone-basename>.md`. The `milestone-planning`
  skill runs the same procedure in a fresh-context subagent after create/refine/decompose; manual
  on-demand plan reviews remain available.
- **Implementation review:** on-demand reviews of an implementation against one milestone use the
  project `milestone-review` skill and write
  `.agentWork/.session/milestone-review-<milestone-basename>.md`. The `implement-milestone` skill
  runs the same procedure in a fresh-context subagent after implementation; manual on-demand
  implementation reviews remain available.
- **Review-isolation handoff:** when a write-capable fresh subagent cannot be spawned, the
  `milestone-handoff` skill writes a contract-only kickoff file at
  `.agentWork/.session/milestone-handoff-<review-kind>-<milestone-basename>.md` (not a review
  verdict). Re-handoff overwrites the prior file for that milestone and kind.
