# Phase 2.8 — Jackson 3 Sparse Fieldsets

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 2.3  
> **Status:** Not started

## Goal

Apply caller-supplied sparse fieldsets to primary and included resources without weakening identity, linkage, or bounded-traversal guarantees.

## Research and constraints

- [JSON:API sparse fieldsets](https://jsonapi.org/format/1.1/#fetching-sparse-fieldsets) — fieldsets are selected per resource type and can affect the full-linkage requirement in compound documents.
- [`docs/vision.md`](../../docs/vision.md) — supported fields and field visibility remain explicit application policy; the library applies requested policy but does not authorize fields.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — sparse fieldsets share the explicit serialization context with inclusion even though they land as a separate reviewable milestone.
- Phase 2.2 mapping definitions provide Jackson logical field names, and Phase 2.3 provides inclusion traversal; fieldsets must reuse those definitions rather than rescan domain types.
- `ValidationContext.withSparseFieldsetException` is the core mechanism for the JSON:API full-linkage exception and must be enabled only when omission actually results from an applied fieldset.

## Deliverables

- Extend the immutable Jackson 3 serialization context with validated field-name sets keyed by JSON:API resource type and an explicit application allow-list/policy.
- Filter mapped attributes and relationships by their final JSON:API names for both primary and included resources while always retaining `type` and resource identity.
- Apply relationship fieldsets before property access and Phase 2.3 traversal so excluded relationships are neither read nor included.
- Coordinate omitted linkage with aggregate validation's sparse-fieldset exception without disabling duplicate-identity, local-identifier, extension/profile, or other validation rules.
- Add focused fieldset fixtures and refresh module docs/Javadoc and conformance notes for field selection and policy ownership.

## Non-goals

- Parsing HTTP `fields[type]` parameters; the query and web modules own transport syntax.
- Choosing which fields a caller is authorized to expose.
- Implicit default fieldsets, persistence fetch plans, or ORM lazy-loading behavior.
- Jackson 2 sparse-fieldset support; plan it only after Jackson 2 document-codec and mapping scope is established.

## Implementation boundaries

- Field names resolve through Phase 2.2 mapping metadata after annotation/Jackson renames. Unknown resource types or field names fail with stable mapping codes and logical paths.
- An absent fieldset entry means unrestricted mapped fields for that type; a present empty set means emit no attributes or relationships while preserving identity.
- Excluded relationship properties are not accessed. Included traversal can follow only relationships that survive the applied fieldset and Phase 2.3 inclusion policy.
- The sparse-fieldset full-linkage exception is scoped to the generated document validation call; it is not a global or caller-mapper setting.

## Test strategy

- Cover unrestricted, empty, attribute-only, relationship-only, renamed, per-type, primary, and included-resource fieldsets.
- Use access-counting/lazy-failure fixtures to prove excluded relationship properties are not read.
- Cover unknown type/field requests, identity preservation, inclusion interaction, full-linkage exception scoping, and deterministic output.

## Acceptance criteria

- [ ] Fieldsets use final JSON:API names and apply consistently to primary and included resources while always preserving `type` and `id`/`lid`.
- [ ] Excluded relationships are not accessed or traversed; inclusion and fieldset policies compose deterministically for nested paths.
- [ ] The full-linkage exception is enabled only for omissions caused by applied fieldsets, and all other aggregate validation remains active.
- [ ] The canonical `module-docs` checklist passes and conformance docs state that field authorization and query parsing remain application/adapter responsibilities.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*SparseFieldsetSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
