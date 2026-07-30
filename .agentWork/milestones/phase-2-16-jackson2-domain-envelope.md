# Phase 2.16 — Jackson 2 Typed Domain Envelope

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.10 and 2.15  
> **Status:** Not started

## Goal

Port typed domain document envelopes and independent included-resource binding to Jackson 2.

## Research and constraints

- Phase 2.10 defines envelope primary states, document-member views, type registration, included
  identity/indexing, and graph-free behavior.
- Phase 2.15 supplies Jackson 2 resource binding; Phase 2.7 remains the validated parser.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — routine DTO signatures avoid core
  documents, unregistered included types fail, and included resources are never injected.
- Envelope concepts remain recognizable across majors while public `JavaType`/mapper signatures
  stay major-specific.

## Deliverables

- Add Jackson 2 domain envelope and primary-data entry points with the same conceptual states and
  nullness as Phase 2.10.
- Port document-level links/meta/JSON:API/errors/additional-member views and typed metadata hooks.
- Add Jackson 2 resource-type registration and independent ordered/identity-indexed included DTO
  binding.
- Compare complete shared document/envelope values and diagnostics across both Jackson majors.
- Refresh Jackson 2 module docs/Javadoc and conformance examples for typed envelopes.

## Non-goals

- Graph assembly, relationship injection, persistence lookup, or extension/profile interpretation.
- Presence-aware PATCH binding; Phase 2.17 owns it.
- Runtime cross-major delegation or one artifact accepting both mapper APIs.

## Implementation boundaries

- Convenience readers validate through Phase 2.7 before binding and preserve codec failure
  categories/paths/locations.
- Resource targets come only from explicit registration; unregistered/conflicting registrations
  fail before an envelope escapes.
- Public/production signatures use Jackson 2 only and import no Jackson 3 or sibling internal.

## Test strategy

- Reuse Phase 2.10 data/error/meta-only/compound, heterogeneous included, explicit-null/absent, and
  registration fixtures.
- Compare ordered included DTOs, identity lookup, document members, and stable errors across majors.

## Acceptance criteria

- [ ] Shared fixtures produce equivalent typed envelope states and document-member views through
      both Jackson majors.
- [ ] Included registration, order, identity lookup, unknown/conflict diagnostics, and no-injection
      behavior match Phase 2.10.
- [ ] Public nullness and caller ownership contracts hold with no Jackson 3/runtime dependency.
- [ ] The canonical `module-docs` checklist passes and conformance docs cover Jackson 2 envelopes.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*DomainDocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
