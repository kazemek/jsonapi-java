# Implementation Milestones

Milestones are planned, testable increments. They may change until implementation starts; after completion they are retained as documentation of the delivered contract.

## Dependency order

1. **Phase 0.1 — Public namespace:** gates every public source package.
2. **Phase 1.1 — Document model and validation** and **Phase 1.2 — annotations:** independent zero-dependency foundations that may proceed in parallel after Phase 0.1.
3. **Phase 2.1 — Jackson document codec:** proves the core model against real wire fixtures.
4. **Phase 2.2 — domain mapping** and **Phase 2.4 — document-first reads:** may proceed in parallel after their listed dependencies.
5. **Phase 2.3 — compound serialization:** builds on domain mapping.
6. **Phase 3.1 — query parser:** independent optional artifact after Phase 0.1.
7. **Phase 3.2 — Spring WebMVC:** integrates the completed codec, mapping, and query contracts.
8. **Phase 3.3 — WebFlux evaluation:** begins only after WebMVC behavior is stable.
9. **Phase 4.1 — conformance and hardening.**
10. **Phase 4.2 — stable release.**

## Milestone index

- [Phase 0.1 — Public Namespace Decision](phase-0-1-public-namespace.md)
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
