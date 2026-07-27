# Implementation Milestones

Milestones are planned, testable increments. They may change until implementation starts; after completion they are retained as documentation of the delivered contract.

## Dependency order

1. **Phase 0.1 — Public namespace:** gates every public source package.
2. **Phase 0.2 — milestone review workflow:** defines the repository's on-demand review process.
3. **Phase 0.3 — CI quality and supply chain:** coverage, Sonar Quality Gate, verification, Action digests, and CI report artifacts.
4. **Phase 0.4 — Spotless formatting:** repo-wide Spotless via a root convention plugin, greclipse for Spock, and an agent format completion skill.
5. **Phase 1.1 — Document model and validation** and **Phase 1.2 — annotations:** independent zero-dependency foundations that may proceed in parallel after Phase 0.1.
6. **Phase 2.1 — Jackson document codec:** proves the core model against real wire fixtures.
7. **Phase 2.2 — domain mapping** and **Phase 2.4 — document-first reads:** may proceed in parallel after their listed dependencies.
8. **Phase 2.3 — compound serialization:** builds on domain mapping.
9. **Phase 3.1 — query parser:** independent optional artifact after Phase 0.1.
10. **Phase 3.2 — Spring WebMVC:** integrates the completed codec, mapping, and query contracts.
11. **Phase 3.3 — WebFlux evaluation:** begins only after WebMVC behavior is stable.
12. **Phase 4.1 — conformance and hardening.**
13. **Phase 4.2 — stable release.**

## Milestone index

- [Phase 0.1 — Public Namespace Decision](phase-0-1-public-namespace.md)
- [Phase 0.2 — Milestone Review Workflow](phase-0-2-milestone-review-workflow.md)
- [Phase 0.3 — CI Quality and Supply Chain](phase-0-3-ci-quality-and-supply-chain.md)
- [Phase 0.4 — Spotless Formatting](phase-0-4-spotless-formatting.md)
- [Phase 1.1 — Document Model and Validation](phase-1-1-spec-data-model.md)
- [Phase 1.2 — Domain-Mapping Annotations](phase-1-2-annotations.md)
- [Phase 2.1 — Jackson Document Codec](phase-2-1-jackson-document-codec.md)
- [Phase 2.2 — Domain-to-Resource Mapping](phase-2-2-domain-resource-mapping.md)
- [Phase 2.3 — Compound Serialization Context](phase-2-3-compound-serialization.md)
- [Phase 2.4 — Document-First Reads](phase-2-4-document-reads.md)
- [Phase 3.1 — Optional Query-Parameter Parser](phase-3-1-query-parameters.md)
- [Phase 3.2 — Spring WebMVC Adapter](phase-3-2-spring-webmvc.md)
- [Phase 3.3 — WebFlux Adapter Evaluation](phase-3-3-webflux-evaluation.md)
- [Phase 4.1 — Conformance and Hardening](phase-4-1-conformance-and-hardening.md)
- [Phase 4.2 — Stable Release](phase-4-2-stable-release.md)

Every implementation milestone must finish with the relevant module tests and `./gradlew clean build` passing.

## Milestone reviews

Milestones are permanent delivery contracts. On-demand reviews of an implementation against one milestone are ephemeral working artifacts produced with the project `milestone-review` skill.

Reviews are written to `.agentWork/.session/milestone-review-<milestone-basename>.md`. They are excluded from version control and overwritten when the same milestone is reviewed again.
