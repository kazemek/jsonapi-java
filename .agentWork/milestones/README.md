# Implementation Milestones

Milestones are planned, testable increments. They may change until implementation starts; after completion they are retained as documentation of the delivered contract.

## Dependency order

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
15. **Phase 2.1 — Jackson 3 document writer:** creates the first major-specific codec artifact and
    proves deterministic model-to-wire behavior.
16. **Phase 2.2 — Jackson 3 write mapping**, **Phase 2.4 — document reader**, and **Phase 2.5 —
    draft-schema cross-check:** completed; **Phase 2.3 — compound serialization** and **Phase 2.9 —
    flat DTO reader:** may proceed in parallel after their listed dependencies.
17. **Phase 2.3 — Jackson 3 compound serialization** and **Phase 2.9 — flat DTO reader:** add
    explicit inclusion and validated resource-to-DTO binding independently.
18. **Phase 2.8 — Jackson 3 sparse fieldsets** and **Phase 2.10 — typed domain envelope:** build on
    their respective compound and flat-read foundations.
19. **Phase 2.11 — Jackson 3 PATCH binding:** composes update validation and typed DTO envelopes
    into presence-aware commands.
20. **Phase 3.1 — query parser:** remains an independent optional artifact.
21. **Phase 3.2 — Spring WebMVC document transport:** integrates media negotiation, validated
    documents, query arguments, and safe errors.
22. **Phase 3.3 — Spring WebMVC flat DTO binding:** adds the primary Jackson 3/Spring DTO,
    envelope, inclusion/fieldset, and PATCH experience.
23. **Phase 3.4 — WebFlux evaluation:** begins after document and DTO-oriented WebMVC behavior is
    stable.
24. **Phase 2.6 — Jackson 2 document writer:** starts the later parity track after the Jackson
    3/Spring path, without adding an artificial Spring dependency.
25. **Phase 2.7 — Jackson 2 document reader** and **Phase 2.12 — domain mapping:** may proceed after
    the Jackson 2 writer and their respective Jackson 3 contracts.
26. **Phase 2.13 — Jackson 2 compound serialization** and **Phase 2.15 — flat DTO reader:** build
    independently on stable mapping/read contracts.
27. **Phase 2.14 — Jackson 2 sparse fieldsets** and **Phase 2.16 — typed domain envelope:** finish
    write-policy and read-envelope parity independently.
28. **Phase 2.17 — Jackson 2 PATCH binding:** completes presence-aware DTO parity.
29. **Phase 4.1 — conformance and hardening.**
30. **Phase 4.2 — stable release.**

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
- [Phase 2.1 — Jackson 3 Document Writer](phase-2-1-jackson-document-codec.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.2 — Jackson 3 Domain-to-Resource Mapping](phase-2-2-domain-resource-mapping.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.3 — Jackson 3 Compound Serialization Context](phase-2-3-compound-serialization.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.4 — Jackson 3 Document Reader](phase-2-4-document-reads.md) — `jsonapi-java-jackson3` — Complete
- [Phase 2.5 — JSON:API 1.1 Draft-Schema Cross-Check](phase-2-5-json-schema-cross-check.md) — `jsonapi-java-jackson3` test suite — Complete
- [Phase 2.6 — Jackson 2 Document Writer](phase-2-6-jackson2-document-writer.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.7 — Jackson 2 Document Reader](phase-2-7-jackson2-document-reader.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.8 — Jackson 3 Sparse Fieldsets](phase-2-8-sparse-fieldsets.md) — `jsonapi-java-jackson3` — Not started
- [Phase 2.9 — Jackson 3 Flat DTO Reader](phase-2-9-jackson3-flat-dto-reader.md) — `jsonapi-java-jackson3` — Not started
- [Phase 2.10 — Jackson 3 Typed Domain Envelope](phase-2-10-jackson3-domain-envelope.md) — `jsonapi-java-jackson3` — Not started
- [Phase 2.11 — Jackson 3 Presence-Aware PATCH Binding](phase-2-11-jackson3-patch-binding.md) — `jsonapi-java-jackson3` — Not started
- [Phase 2.12 — Jackson 2 Domain-to-Resource Mapping](phase-2-12-jackson2-domain-resource-mapping.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.13 — Jackson 2 Compound Serialization](phase-2-13-jackson2-compound-serialization.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.14 — Jackson 2 Sparse Fieldsets](phase-2-14-jackson2-sparse-fieldsets.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.15 — Jackson 2 Flat DTO Reader](phase-2-15-jackson2-flat-dto-reader.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.16 — Jackson 2 Typed Domain Envelope](phase-2-16-jackson2-domain-envelope.md) — `jsonapi-java-jackson2` — Not started
- [Phase 2.17 — Jackson 2 Presence-Aware PATCH Binding](phase-2-17-jackson2-patch-binding.md) — `jsonapi-java-jackson2` — Not started
- [Phase 3.1 — Optional Query-Parameter Parser](phase-3-1-query-parameters.md) — `jsonapi-java-query` — Not started
- [Phase 3.2 — Spring WebMVC Adapter](phase-3-2-spring-webmvc.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.3 — Spring WebMVC Flat DTO Binding](phase-3-3-spring-webmvc-dto-binding.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.4 — WebFlux Adapter Evaluation](phase-3-4-webflux-evaluation.md) — candidate `jsonapi-java-spring-webflux` — Not started
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
