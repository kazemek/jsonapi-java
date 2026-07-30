# Phase 2.15 — Jackson 2 Flat DTO Reader

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.7, 2.9, and 2.12  
> **Status:** Not started

## Goal

Port validated flat resource-to-DTO binding to Jackson 2 with mapping and diagnostic parity.

## Research and constraints

- Phase 2.9 defines flat binding, linkage-only relationships, target/cardinality rules, and mapping
  diagnostics.
- Phase 2.7 supplies validated Jackson 2 document reads; Phase 2.12 supplies Jackson 2 mapping
  definitions and identifier conversion.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — included resources never populate DTO
  relationships and document validation always precedes binding.
- Jackson 2 creators/deserializers may differ in source API but must honor equivalent caller
  configuration and shared semantic fixtures.

## Deliverables

- Add Jackson 2 resource binder entry points for one resource and declared homogeneous
  collections using `com.fasterxml.jackson.databind.JavaType`.
- Port type/id/lid, attribute, creator, and custom deserializer binding through Phase 2.12
  definitions.
- Port scalar/collection relationship identifier conversion and explicit custom linkage mappers
  without included-resource resolution.
- Compare shared result and stable diagnostic fixtures across Jackson majors.
- Refresh Jackson 2 module docs/Javadoc and conformance entries for flat DTO reads.

## Non-goals

- Typed envelopes/included binding; Phase 2.16 owns them.
- Graph hydration, persistence lookup, identity maps, or PATCH commands.
- Runtime delegation to Jackson 3 or resource-shape guessing.

## Implementation boundaries

- Public APIs expose Jackson 2 types only; production code imports no Jackson 3 or sibling internal.
- Unknown/ignored properties and creator/null behavior follow caller Jackson 2 configuration after
  JSON:API role/name resolution.
- Relationship absence and null/single/collection linkage are not collapsed or populated from
  included resources.

## Test strategy

- Parameterize Phase 2.9 records/POJOs, creators, naming, mix-ins, custom deserializers,
  identifiers, linkage, and negative diagnostics across both majors.
- Prove changing included resources does not change primary DTOs or relationship fields.

## Acceptance criteria

- [ ] Every shared supported resource shape binds to semantically equivalent DTO values through
      Jackson 2 and Jackson 3.
- [ ] Relationship linkage and mapping failures retain parity categories/paths, and `included` is
      never read.
- [ ] Caller mapper behavior is preserved and no Jackson 3/runtime or sibling-internal dependency
      exists.
- [ ] The canonical `module-docs` checklist passes and conformance docs distinguish major-specific
      flat read support.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*ResourceBinderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
