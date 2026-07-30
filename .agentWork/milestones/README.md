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
11. **Phase 1.1 — Document model and validation** and **Phase 1.2 — annotations:** independent foundations with no functional third-party runtime dependencies; Phase 1.2 uses the completed namespace, module-docs, and nullness foundations from Phases 0.1, 0.5, and 0.8.
12. **Phase 2.1 — Jackson document codec:** proves the core model against real wire fixtures.
13. **Phase 2.2 — domain mapping** and **Phase 2.4 — document-first reads:** may proceed in parallel after their listed dependencies.
14. **Phase 2.3 — compound serialization:** builds on domain mapping.
15. **Phase 3.1 — query parser:** independent optional artifact after Phase 0.1.
16. **Phase 3.2 — Spring WebMVC:** integrates the completed codec, mapping, and query contracts.
17. **Phase 3.3 — WebFlux evaluation:** begins only after WebMVC behavior is stable.
18. **Phase 4.1 — conformance and hardening.**
19. **Phase 4.2 — stable release.**

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
- [Phase 1.1 — Document Model and Validation](phase-1-1-spec-data-model.md) — `jsonapi-java-core` — Complete
- [Phase 1.2 — Domain-Mapping Annotations](phase-1-2-annotations.md) — `jsonapi-java-annotations` — Not started
- [Phase 2.1 — Jackson Document Codec](phase-2-1-jackson-document-codec.md) — `jsonapi-java-jackson` — Not started
- [Phase 2.2 — Domain-to-Resource Mapping](phase-2-2-domain-resource-mapping.md) — `jsonapi-java-jackson` — Not started
- [Phase 2.3 — Compound Serialization Context](phase-2-3-compound-serialization.md) — `jsonapi-java-jackson` — Not started
- [Phase 2.4 — Document-First Reads](phase-2-4-document-reads.md) — `jsonapi-java-jackson` — Not started
- [Phase 3.1 — Optional Query-Parameter Parser](phase-3-1-query-parameters.md) — `jsonapi-java-query` — Not started
- [Phase 3.2 — Spring WebMVC Adapter](phase-3-2-spring-webmvc.md) — `jsonapi-java-spring-webmvc` — Not started
- [Phase 3.3 — WebFlux Adapter Evaluation](phase-3-3-webflux-evaluation.md) — candidate `jsonapi-java-spring-webflux` — Not started
- [Phase 4.1 — Conformance and Hardening](phase-4-1-conformance-and-hardening.md) — all implemented modules — Not started
- [Phase 4.2 — Stable Release](phase-4-2-stable-release.md) — publication and compatibility — Not started

Every implementation milestone must finish with the relevant module tests and `./gradlew clean build` passing.

## Milestone planning

Use the explicitly invoked project `milestone-planning` skill to create, refine, or decompose
milestones. It performs targeted exploration and relevant authoritative research, writes the
permanent milestone files in this directory, and updates both the dependency order and index.

An implementable milestone must fit one focused coding-agent task and reviewable commit. It
normally contains one principal capability in one primary module or layer, at most five
deliverables, and at most eight acceptance criteria. Independent capabilities, modules,
architectural decisions, or verification surfaces are separate milestones with explicit
dependencies.

A `Not started` milestone may be refined or decomposed. Once implementation starts, the milestone
is a fixed delivery contract; changed or additional scope belongs in a follow-up milestone.

## Milestone reviews

Milestones are permanent delivery contracts. On-demand reviews of an implementation against one milestone are ephemeral working artifacts produced with the project `milestone-review` skill.

Reviews are written to `.agentWork/.session/milestone-review-<milestone-basename>.md`. They are excluded from version control and overwritten when the same milestone is reviewed again.
