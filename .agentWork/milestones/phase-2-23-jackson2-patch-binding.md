# Phase 2.23 — Jackson 2 Presence-Aware PATCH Binding

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.15, 2.17, and 2.21  
> **Status:** Not started
> **Work item:** KAZ-34

## Goal

Port presence-aware annotated DTO PATCH commands to Jackson 2 with semantic and diagnostic parity,
reusing common command contracts and the shared scenarios established by Phase 2.15.

## Research and constraints

- [ADR-012](../../docs/adr/012-resource-patch-binding.md) and `jsonapi-java-core`
  `JsonApiDocumentValidator` (`DocumentUsage.UPDATE_REQUEST`) define update shape,
  identity, omitted/null semantics, and the application-owned mutation boundary.
- Phase 2.15 defines the stable typed command/property/linkage contract in common packages and the
  shared PATCH fixture catalog; its pipeline is one `JsonApiDocumentReader` validate-on-read via
  `DocumentReadContext` (`PrimaryDataKind.RESOURCE` + `ValidationContext` forced to
  `UPDATE_REQUEST`, optional `EndpointIdentity`) then presence-aware binding—not typed envelopes.
- Phase 2.17 supplies the Jackson 2 document reader; Phase 2.21 supplies Jackson 2 flat DTO /
  mapping-definition parity used by the patch binder. Phase 2.22 envelopes are not a dependency.
- Phase 2.15 adapter-local cases for cross-major parity (major-local harnesses only):
  `custom deserializer applies to attribute change`; `patch-custom-linkage-conversion`.
- Jackson major differences may alter implementation APIs but not requested-change presence,
  encounter order, conversion, or diagnostics.
- Conformance: Domain mapping “Presence-aware resource-update commands” → mark **supported** for
  Jackson 2 binding (Jackson 3 already Phase 2.15); keep command application out of scope.

## Deliverables

- Add Jackson 2 patch reader/command entry points using `com.fasterxml.jackson.databind.JavaType`
  and the Phase 2.15 common command contracts, mirroring the Phase 2.15 validate-on-read then
  bind pipeline (no typed envelopes).
- Port supplied-only per-member attribute binding with explicit nullable changes and no fabricated
  omitted values; never call a whole-DTO binder/`convertValue` construction path.
- Port null/single/collection relationship linkage changes, typed identity (exposed separately,
  never as a change), and endpoint identity validation.
- Consume every shared Phase 2.15 PATCH scenario plus the named adapter-local cases to compare
  command values, ordering, mapping failures, and update-validation failures across both majors.
- Use `module-docs` to refresh Jackson 2 module docs/Javadoc and apply the named conformance matrix
  edit above.

## Non-goals

- Constructing/mutating complete DTOs, authorization, persistence, or command application.
- Graph hydration, included resolution, or patching links/meta/extensions.
- JSON Merge Patch, JSON Patch, bulk updates, or atomic operations.
- Redefining common patch-command types under the `jackson2` package.
- Composing typed domain envelopes into PATCH commands.

## Implementation boundaries

- Convenience path: one Jackson 2 `JsonApiDocumentReader` validate-on-read with
  `DocumentReadContext` (`PrimaryDataKind.RESOURCE` + `ValidationContext` forced to
  `UPDATE_REQUEST`, optional `EndpointIdentity`) then presence-aware bind—no second defaults
  validate; no partial command escapes.
- Binding reuses Phase 2.21 mapping definitions and relationship/identifier diagnostics with
  per-member typed attribute conversion—not whole-DTO construction. Typed identity is never listed
  among changes.
- Public/production APIs use Jackson 2 and common contracts only and import no Jackson 3 or sibling
  internal.
- Jackson 2 `*PatchBindingSpec` covers the shared Phase 2.15 inventory and the named adapter-local
  cases with major-local harnesses.

## Test strategy

- Parameterize every shared Phase 2.15 scenario through Jackson 2; also cover
  `custom deserializer applies to attribute change` and `patch-custom-linkage-conversion` locally.
- Assert exact change presence/order, typed identity outside the change set, and stable
  categories/paths; permit source-location differences only where Phase 2.17 already documents
  parser differences.

## Acceptance criteria

- [ ] Jackson 2 and Jackson 3 commands contain the same supplied changes, nullable values,
      relationship linkage, identity (separate from changes), and encounter order for every shared
      Phase 2.15 fixture; Jackson 2 `*PatchBindingSpec` also covers the named adapter-local cases.
- [ ] Pipeline mirrors Phase 2.15 (one validate-on-read `DocumentReadContext` + presence-aware
      bind; never whole-DTO construction, typed envelopes, `included`, or application mutation).
- [ ] Omitted properties never invoke constructors/deserializers or appear as changes; public patch
      APIs satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] The canonical `module-docs` checklist passes; Domain mapping “Presence-aware resource-update
      commands” is **supported** for Jackson 2; no application-mutation claim is introduced.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*PatchBindingSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
