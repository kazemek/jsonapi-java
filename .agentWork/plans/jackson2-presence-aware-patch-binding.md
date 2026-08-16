# Jackson 2 Presence-Aware PATCH Binding

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** [Jackson 2 Document Reader](jackson2-document-reader.md), [Jackson 2 Flat DTO Reader](jackson2-flat-dto-reader.md)  
> **Status:** Not started
> **Work item:** KAZ-34

## Goal

Port presence-aware annotated DTO PATCH commands to Jackson 2 with semantic and diagnostic parity,
reusing common command contracts (`PatchCommand` / `PatchChange` in `jsonapi-java-jackson-common`) and the shared `PatchScenarios` catalog (`jsonapi-java-test-fixtures` README).

## Research and constraints

- [ADR-012](../../docs/adr/012-resource-patch-binding.md) and `jsonapi-java-core`
  `JsonApiDocumentValidator` (`DocumentUsage.UPDATE_REQUEST`) define update shape,
  identity, omitted/null semantics, and the application-owned mutation boundary.
- `PatchCommand` / `PatchChange` (`jsonapi-java-jackson-common` README) and `JsonApiPatchReader`
  (`jsonapi-java-jackson3` README / [ADR-012](../../docs/adr/012-resource-patch-binding.md)) define
  the stable typed command/property/linkage contract and the shared `PatchScenarios` catalog; the
  pipeline is one `JsonApiDocumentReader` validate-on-read via `DocumentReadContext`
  (`PrimaryDataKind.RESOURCE` + `ValidationContext` forced to `UPDATE_REQUEST`, optional
  `EndpointIdentity`) then presence-aware binding—not typed envelopes.
- [Jackson 2 Document Reader](jackson2-document-reader.md) supplies the Jackson 2 document reader; [Jackson 2 Flat DTO Reader](jackson2-flat-dto-reader.md) supplies Jackson 2 flat DTO /
  mapping-definition parity used by the patch binder. [Jackson 2 Typed Domain Envelope](jackson2-typed-domain-envelope.md) is not a dependency.
- Adapter-local cases from Jackson 3 `PatchBindingSpec` for cross-major parity (major-local harnesses only):
  `custom deserializer applies to attribute change`; `patch-custom-linkage-conversion`.
- Jackson major differences may alter implementation APIs but not requested-change presence,
  encounter order, conversion, or diagnostics.
- Conformance: Domain mapping “Presence-aware resource-update commands” → mark **supported** for
  Jackson 2 binding (Jackson 3 already supported); keep command application out of scope.

## Deliverables

- Add Jackson 2 patch reader/command entry points using `com.fasterxml.jackson.databind.JavaType`
  and the common `PatchCommand` / `PatchChange` contracts, mirroring Jackson 3 `JsonApiPatchReader`
  validate-on-read then bind pipeline (no typed envelopes).
- Port supplied-only per-member attribute binding with explicit nullable changes and no fabricated
  omitted values; never call a whole-DTO binder/`convertValue` construction path.
- Port null/single/collection relationship linkage changes, typed identity (exposed separately,
  never as a change), and endpoint identity validation.
- Consume every shared `PatchScenarios` inventory plus the named adapter-local cases to compare
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
- Binding reuses [Jackson 2 Flat DTO Reader](jackson2-flat-dto-reader.md) mapping definitions and relationship/identifier diagnostics with
  per-member typed attribute conversion—not whole-DTO construction. Typed identity is never listed
  among changes.
- Public/production APIs use Jackson 2 and common contracts only and import no Jackson 3 or sibling
  internal.
- Jackson 2 `*PatchBindingSpec` covers the shared `PatchScenarios` inventory and the named adapter-local
  cases with major-local harnesses.

## Test strategy

- Parameterize every shared `PatchScenarios` inventory through Jackson 2; also cover
  `custom deserializer applies to attribute change` and `patch-custom-linkage-conversion` locally.
- Assert exact change presence/order, typed identity outside the change set, and stable
  categories/paths; permit source-location differences only where [Jackson 2 Document Reader](jackson2-document-reader.md) already documents
  parser differences.

## Acceptance criteria

- [ ] Jackson 2 and Jackson 3 commands contain the same supplied changes, nullable values,
      relationship linkage, identity (separate from changes), and encounter order for every shared
      `PatchScenarios` fixture; Jackson 2 `*PatchBindingSpec` also covers the named adapter-local cases.
- [ ] Pipeline mirrors Jackson 3 `JsonApiPatchReader` (one validate-on-read `DocumentReadContext` + presence-aware
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
