# Implementation Milestones

Milestones are planned, testable execution contracts. They may change until implementation
starts. A knowledge-model migration is in progress (see `AGENTS.md`): this directory remains the
live plan store and status vocabulary is unchanged (`Not started`, `In progress`, `Complete`).
Completed files are retained for now as delivered contracts; they are **not** the Snapshot.
Current capability lives in module READMEs, accepted ADRs, `docs/conformance.md`, and the root
module registry. Tentative future direction lives in `docs/outlook/`. Do not add Outlook as a
milestone status.

## Planned execution order

Current capability lives in module READMEs, accepted ADRs, `docs/conformance.md`, and the root
module registry — not in completed milestone files. The Complete index rows below remain because
those files still exist; they are not the discovery path for current architecture.

Preferred sequencing for **unfinished** work. It is not a strict dependency graph: later items
may run earlier when their own **Dependencies** headers allow (for example Jackson 2 parity
without waiting on Spring). Each unfinished milestone’s **Dependencies** field remains the
authoritative prerequisite set among live plans only.

1. **Phase 2.15 — Jackson 3 PATCH binding:** presence-aware update commands on current Jackson 3,
   core update validation, and shared fixture catalogs.
2. **Phase 3.1 — query parser:** independent optional artifact.
3. **Phase 2.16 — Jackson 2 document writer:** starts the parity track on current jackson-common
   contracts and shared codec fixtures; **2.17** reader and **2.18** mapping follow 2.16.
4. **Phase 2.19 — Jackson 2 compound serialization** and **Phase 2.21 — flat DTO reader:** after
   their unfinished Jackson 2 dependencies (2.19 needs 2.18; 2.21 needs 2.17 and 2.18).
5. **Phase 2.20 — Jackson 2 sparse fieldsets** and **Phase 2.22 — typed domain envelope:** after
   2.19 and 2.21 respectively.
6. **Phase 2.23 — Jackson 2 PATCH binding:** after 2.15, 2.17, and 2.21.
7. **Phase 3.2 — Spring WebMVC document transport:** after 3.1; then **3.3** DTO binding, **3.4**
   PATCH arguments, and **3.5** WebFlux evaluation.
8. **Phase 4.1 — conformance and hardening** after remaining unfinished product work; **Phase 4.2
   — stable release** after 4.1.

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
- [Phase 0.13 — Milestone Design-Review Workflow](phase-0-13-milestone-design-review-workflow.md) — repository workflow — Complete
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
- [Phase 2.24 — Shared Compound Write Test Fixtures](phase-2-24-shared-compound-write-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `CompoundSerializationSpec` — Complete
- [Phase 2.25 — Shared Sparse-Fieldset Write Test Fixtures](phase-2-25-shared-sparse-fieldset-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `SparseFieldsetSpec` — Complete
- [Phase 2.26 — Shared Typed Envelope Read Test Fixtures](phase-2-26-shared-envelope-read-fixtures.md) — `jsonapi-java-test-fixtures` / jackson3 `DomainDocumentReaderSpec` — Complete
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
execution-contract milestone files in this directory, updates both the dependency order and index, and then
runs `milestone-design-review` orchestration (two fresh-context reviewers, worst-wins combination)
so the design review is not influenced by the planning session. After a design-review Pass, it runs
the `milestone-plan-review` procedure in a fresh-context subagent. Each stage has its own fix loop
capped at two re-reviews. Plan-review edits do not restart design review in the same run. Planning
is complete only after both a design-review Pass and a plan-review Pass for each created or refined
milestone.

An implementable milestone should be the largest coherent unit that can still be implemented and
independently reviewed in one context. Numeric deliverable and acceptance-criteria bounds in the
`milestone-planning` skill are heuristics, not an automatic split. Conceptual decomposition does
not by itself create child implementation plans; split only for a genuine execution/review
boundary (see the `milestone-planning` skill).

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

Milestones are execution contracts retained during this migration; they are not current
architecture after delivery. Three ephemeral review kinds write under
`.agentWork/.session/` and are excluded from version control; each re-review overwrites the prior
artifact for that milestone.

- **Design review:** on-demand reviews of whether a milestone's technical design is sound use the
  project `milestone-design-review` skill. Two isolated reviewers write
  `.agentWork/.session/milestone-design-review-design-<milestone-basename>.md` and
  `.agentWork/.session/milestone-design-review-adversarial-<milestone-basename>.md`. The official
  pointer stub is `.agentWork/.session/milestone-design-review-<milestone-basename>.md`. The
  `milestone-planning` skill runs the same orchestration after create/refine/decompose; manual
  on-demand design reviews remain available.
- **Plan/spec review:** on-demand reviews of a milestone contract against planning rules use the
  project `milestone-plan-review` skill and write
  `.agentWork/.session/milestone-plan-review-<milestone-basename>.md`. The `milestone-planning`
  skill runs the same procedure in a fresh-context subagent after design-review Pass; manual
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
