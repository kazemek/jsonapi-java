# Phase 2.13 — Jackson 2 Compound Serialization

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.3 and 2.12  
> **Status:** Not started

## Goal

Port explicit, bounded compound-document inclusion to Jackson 2 with Jackson 3 semantic parity.

## Research and constraints

- Phase 2.3 defines requested include paths, allow-list policy, limits, traversal, deduplication,
  cycle handling, conflict detection, deterministic order, and the mapping-only compound context
  (no `ValidationContext` on the serialization context; validation stays on the document writer).
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — linkage never implies inclusion,
  and persistence/lazy-loading policy remains application-owned.
- Phase 2.12 supplies Jackson 2 mapping definitions; traversal reuses them and must not rescan
  domain types.
- Shared compound fixtures, not runtime delegation, establish parity between Jackson majors.

## Deliverables

- Add the Jackson 2 immutable mapping-only serialization context with API parity to Phase 2.3:
  `IncludePath.of(String)` (same dot-separated syntax and rejection rules),
  `IncludePolicy.allowAll()` / `denyAll()` / `allowing(Set<RelationshipAllowance>)`,
  `RelationshipAllowance(resourceType, relationshipName)`, `withX()` copy methods with the same
  defensive-copy contract, and the same finite defaults (depth 10, count 100). The context has no
  `ValidationContext`; document validation remains on the Jackson 2 document writer.
- Port requested-path validation and relationship traversal using Phase 2.12 definitions, including
  Phase 2.3's access-vs-linkage contract (full linkage via `toResource` on selected resources;
  never-read applies to inclusion traversal only), and the five `MappingDiagnostic` codes
  (`INVALID_INCLUDE_PATH`, `DENIED_RELATIONSHIP_INCLUDE`, `CONFLICTING_INCLUDED_REPRESENTATION`,
  `INCLUDE_DEPTH_EXCEEDED`, `INCLUDE_COUNT_EXCEEDED`) with dotted JSON:API `propertyPath` semantics.
- Port identity deduplication, required intermediate inclusion, cycle handling, conflicting
  representation failure, and deterministic first-encounter order.
- Run the shared compound fixture/diagnostic matrix through both major-specific artifacts.
- Refresh Jackson 2 module docs/Javadoc and conformance notes for opt-in inclusion.

## Non-goals

- Sparse fieldsets; Phase 2.14 owns them.
- Implicit inclusion, ORM/lazy-loading integration, repositories, or authorization.
- Jackson 2 flat DTO reads, envelopes, or PATCH binding.

## Implementation boundaries

- Defaults request no included resources and retain finite safety limits.
- Off-path relationships are not accessed for inclusion traversal; include policy is checked before
  traversal property access. Selected resources still emit full linkage (Phase 2.3 access-vs-linkage
  contract).
- Production code imports no Jackson 3 or sibling internal types, and shared fixtures are not
  copied into major-specific variants.

## Test strategy

- Reuse Phase 2.3 cyclic, shared, nested, empty, conflict, limit, and invalid-path domain graphs.
- Reuse Phase 2.3's traversal-scoped access-counting fixture to prove off-path relationships are not accessed for inclusion traversal (parity with Phase 2.3's access-vs-linkage contract).
- Compare included resources, order, linkage, and stable diagnostics across Jackson 2 and 3.

## Acceptance criteria

- [ ] The Jackson 2 context surface (`IncludePath.of` syntax, `IncludePolicy` factories,
      `RelationshipAllowance`, `withX()` defensive copies, defaults 10/100) and the five
      `MappingDiagnostic` codes with dotted `propertyPath` semantics match Phase 2.3; explicit
      include requests produce the same included resources, intermediates, linkage, and
      first-encounter order as Phase 2.3.
- [ ] Policy rejection, conflicts, cycles, depth/count limits, and invalid paths have parity
      diagnostics, and off-path relationships are not accessed for inclusion traversal.
- [ ] Jackson 2 compound code has no Jackson 3/runtime or sibling-internal dependency.
- [ ] The canonical `module-docs` checklist passes and conformance documentation records exact
      opt-in Jackson 2 inclusion behavior.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*CompoundSerializationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
