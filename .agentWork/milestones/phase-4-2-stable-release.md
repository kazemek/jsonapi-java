# Phase 4.2 — Stable Release

> **Scope:** Publication and compatibility  
> **Dependencies:** Phase 4.1 and a verified namespace  
> **Status:** Not started

## Goal

Publish a reviewable stable release with explicit compatibility and support policies.

## Deliverables

- Maven Central publication under the verified namespace with source, Javadoc, checksums,
  signatures, license, SCM, and developer metadata.
- Semantic versioning, public API compatibility, upgrade, and deprecation policies.
- A JPMS/automatic-module-name decision plus supported Java, Jackson 2, Jackson 3, and Spring
  version ranges.
- Clean-consumer examples for document-only use, both Jackson majors, flat DTO/envelope
  read/write, independent included binding, PATCH commands, query parsing, and DTO-oriented WebMVC.
- Release notes tied to the conformance checklist and explicit application-owned graph,
  authorization, mutation, persistence, and endpoint boundaries.

## Acceptance criteria

- [ ] Artifacts resolve independently with only their declared dependencies.
- [ ] Publication coordinates match the verified namespace.
- [ ] Public API compatibility checks run in CI.
- [ ] Documentation does not imply unsupported graph hydration, automatic PATCH mutation, ORM
      behavior, query execution, or endpoint compliance.
- [ ] A clean consumer project can run each supported example.
- [ ] The full build and publication validation pass from a clean checkout.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
