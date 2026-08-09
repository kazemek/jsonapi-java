# Phase 2.23 — Jackson 2 Presence-Aware PATCH Binding

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 1.3, 2.11, 2.15, and 2.22  
> **Status:** Not started

## Goal

Port presence-aware annotated DTO PATCH commands to Jackson 2 with semantic and diagnostic parity,
reusing common command contracts and the shared scenarios established by Phase 2.15.

## Research and constraints

- [ADR-012](../../docs/adr/012-resource-patch-binding.md) and Phase 1.3 define update shape,
  identity, omitted/null semantics, and the application-owned mutation boundary.
- Phase 2.15 defines the stable typed command/property/linkage contract in common packages and the
  shared PATCH fixture catalog.
- Phase 2.22 supplies Jackson 2 domain envelopes and Phase 2.21 supplies property/linkage binding.
- Jackson major differences may alter implementation APIs but not requested-change presence,
  encounter order, conversion, or diagnostics.

## Deliverables

- Add Jackson 2 patch reader/command entry points using `com.fasterxml.jackson.databind.JavaType`
  and the Phase 2.15/2.11 common command contracts.
- Port supplied-only attribute binding with explicit nullable changes and no fabricated omitted
  values.
- Port null/single/collection relationship linkage changes and endpoint identity validation.
- Consume the shared Phase 2.15 PATCH scenarios to compare command values, ordering, mapping
  failures, and update-validation failures across both Jackson majors.
- Use `module-docs` to refresh Jackson 2 module docs/Javadoc and conformance examples for PATCH
  binding.

## Non-goals

- Constructing/mutating complete DTOs, authorization, persistence, or command application.
- Graph hydration, included resolution, or patching links/meta/extensions.
- JSON Merge Patch, JSON Patch, bulk updates, or atomic operations.
- Redefining common patch-command types under the `jackson2` package.

## Implementation boundaries

- Decode and Phase 1.3 validation complete before binding; no partial command escapes.
- DTO target type supplies metadata only and no constructor runs for omitted properties.
- Public/production APIs use Jackson 2 and common contracts only and import no Jackson 3 or sibling
  internal.

## Test strategy

- Parameterize Phase 2.15 attribute, explicit-null, relationship, custom conversion, identity, and
  negative fixtures across both majors.
- Assert exact change presence/order and stable categories/paths; permit source-location
  differences only where Phase 2.17 already documents parser differences.

## Acceptance criteria

- [ ] Jackson 2 and Jackson 3 commands contain the same supplied changes, nullable values,
      relationship linkage, identity, and encounter order for every shared Phase 2.15 fixture.
- [ ] Omitted properties never invoke constructors/deserializers or appear as changes through
      either major.
- [ ] Validation/mapping diagnostics retain stable parity and no application mutation, included
      binding, Jackson 3/runtime dependency, or duplicated common command types is introduced.
- [ ] The canonical `module-docs` checklist passes and conformance docs cover Jackson 2 PATCH
      commands without claiming application semantics; public patch APIs satisfy ADR-009
      `@NullMarked` / `@Nullable` rules.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*PatchBindingSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
