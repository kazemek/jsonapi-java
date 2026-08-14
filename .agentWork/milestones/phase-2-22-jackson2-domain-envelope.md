# Phase 2.22 — Jackson 2 Typed Domain Envelope

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phase 2.21  
> **Status:** Not started
> **Work item:** KAZ-36

## Goal

Port typed domain document envelopes and independent included-resource binding to Jackson 2 using
common envelope values and shared envelope-read fixtures.

## Research and constraints

- `JsonApiDomainDocumentReader` (`jsonapi-java-jackson3` README / [ADR-011](../../docs/adr/011-flat-dto-read-binding.md))
  defines envelope primary states, document-member views, type registration, included
  identity/indexing, and graph-free behavior.
- `jsonapi-java-jackson-common` places Jackson-import-free envelope/domain-data contracts in the
  common package; Phase 2.21 supplies Jackson 2 resource binding; Phase 2.17 remains the validated
  parser.
- `JsonApiFixtures.envelopeRead()` / `EnvelopeReadScenarios` is the shared typed-envelope scenario
  catalog that selects applicable codec documents or named binding variants.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — routine DTO signatures avoid core
  documents, unregistered included types fail, and included resources are never injected.
- Public `JavaType`/mapper signatures stay major-specific while envelope values remain common.

## Deliverables

- Add Jackson 2 domain-document reader entry points that assemble common envelope/primary-data
  values with the same conceptual states and nullness as jackson3 `JsonApiDomainDocument`.
- Port document-level links/meta/JSON:API/errors/additional-member views and typed metadata hooks.
- Add Jackson 2 resource-type registration and independent ordered/identity-indexed included DTO
  binding.
- Consume the `EnvelopeReadScenarios` catalog and compare complete shared values and
  diagnostics across both Jackson majors.
- Use `module-docs` to refresh Jackson 2 module docs/Javadoc and conformance examples for typed
  envelopes.

## Non-goals

- Graph assembly, relationship injection, persistence lookup, or extension/profile interpretation.
- Presence-aware PATCH binding; Phase 2.23 owns it.
- Redefining common envelope/domain-data types under the `jackson2` package.
- Runtime cross-major delegation or one artifact accepting both mapper APIs.

## Implementation boundaries

- Convenience readers validate through Phase 2.17 before binding and preserve codec failure
  categories/paths/locations.
- Resource targets come only from explicit registration; unregistered/conflicting registrations
  fail before an envelope escapes.
- Public/production signatures use Jackson 2 and common contracts only and import no Jackson 3 or
  sibling internal.

## Test strategy

- Parameterize `EnvelopeReadScenarios` data/error/meta-only/compound, heterogeneous included, explicit-null/
  absent, and registration scenarios.
- Compare ordered included DTOs, identity lookup, document members, and stable errors across majors.

## Acceptance criteria

- [ ] Applicable shared fixtures produce equivalent typed envelope states and document-member views
      through both Jackson majors via common envelope contracts.
- [ ] Included registration, order, identity lookup, unknown/conflict diagnostics, and no-injection
      behavior match jackson3 `JsonApiDomainDocumentReader`.
- [ ] Caller ownership contracts hold with no Jackson 3/runtime dependency and no duplicated
      common envelope types.
- [ ] The canonical `module-docs` checklist passes and conformance docs cover Jackson 2 envelopes;
      public null-bearing envelope APIs satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*DomainDocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
