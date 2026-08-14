# Phase 2.21 — Jackson 2 Flat DTO Reader

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.17 and 2.18  
> **Status:** Not started
> **Work item:** KAZ-33

## Goal

Port validated flat resource-to-DTO binding to Jackson 2 with mapping and diagnostic parity using
common contracts and shared domain-read fixtures.

## Research and constraints

- `JsonApiResourceBinder` (`jsonapi-java-jackson3` README / [ADR-011](../../docs/adr/011-flat-dto-read-binding.md))
  defines flat binding, linkage-only relationships, target/cardinality rules, and mapping
  diagnostics.
- Phase 2.17 supplies validated Jackson 2 document reads; Phase 2.18 supplies Jackson 2 mapping
  definitions and identifier conversion.
- `jsonapi-java-jackson-common` supplies common diagnostics and identifier conversion;
  `JsonApiFixtures.domainRead()` / `DomainReadScenarios` is the shared flat-binding scenario catalog.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — included resources never populate DTO
  relationships and document validation always precedes binding.
- Jackson 2 creators/deserializers may differ in source API but must honor equivalent caller
  configuration and shared semantic fixtures.

## Deliverables

- Add Jackson 2 resource binder entry points for one resource and declared homogeneous collections
  using `com.fasterxml.jackson.databind.JavaType` and common diagnostics.
- Port type/id/lid, attribute, creator, and custom deserializer binding through Phase 2.18
  definitions.
- Port scalar/collection relationship identifier conversion and explicit custom linkage mappers
  without included-resource resolution.
- Consume the `DomainReadScenarios` catalog; keep major-specific deserializer cases adapter-local.
- Use `module-docs` to refresh Jackson 2 module docs/Javadoc and conformance entries for flat DTO
  reads.

## Non-goals

- Typed envelopes/included binding; Phase 2.22 owns them.
- Graph hydration, persistence lookup, identity maps, or PATCH commands.
- Duplicating common diagnostic/identifier types under the `jackson2` package.
- Runtime delegation to Jackson 3 or resource-shape guessing.

## Implementation boundaries

- Public APIs expose Jackson 2 types and common contracts only; production code imports no Jackson 3
  or sibling internal.
- Unknown/ignored properties and creator/null behavior follow caller Jackson 2 configuration after
  JSON:API role/name resolution.
- Relationship absence and null/single/collection linkage are not collapsed or populated from
  included resources.

## Test strategy

- Parameterize `DomainReadScenarios` (records/POJOs, creators, naming, identifiers,
  linkage, and negative diagnostics) across both majors.
- Retain adapter-local Jackson 2 cases for major-specific deserializers and mapper isolation.
- Prove changing included resources does not change primary DTOs or relationship fields.

## Acceptance criteria

- [ ] Every applicable `DomainReadScenarios` supported resource shape binds to semantically equivalent DTO
      values through Jackson 2 and Jackson 3.
- [ ] Relationship linkage and mapping failures retain parity categories/paths, and `included` is
      never read.
- [ ] Caller mapper behavior is preserved and no Jackson 3/runtime or sibling-internal dependency
      exists.
- [ ] The canonical `module-docs` checklist passes and conformance docs distinguish major-specific
      flat read support; public binder APIs satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*ResourceBinderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
