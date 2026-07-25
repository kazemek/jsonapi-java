# Phase 4.2 — Stable Release

> **Scope:** Publication and compatibility  
> **Dependencies:** Phase 4.1 and a verified namespace  
> **Status:** Not started

## Goal

Publish a reviewable stable release with explicit compatibility and support policies.

## Deliverables

- Maven Central publication under the verified namespace.
- Source, Javadoc, checksums, signatures, license, SCM, and developer metadata.
- Semantic versioning and public API compatibility policy.
- A decision on JPMS module descriptors and automatic module names.
- Supported Java, Jackson, and Spring version ranges.
- Upgrade and deprecation policy.
- Minimal examples for document-only, Jackson mapping, query parsing, and WebMVC use.
- Release notes tied to the conformance checklist.

## Acceptance criteria

- [ ] Artifacts resolve independently with only their declared dependencies.
- [ ] Publication coordinates match the verified namespace.
- [ ] Public API compatibility checks run in CI.
- [ ] Documentation does not imply unsupported graph hydration, ORM behavior, query execution, or endpoint compliance.
- [ ] A clean consumer project can run each supported example.
- [ ] The full build and publication validation pass from a clean checkout.
