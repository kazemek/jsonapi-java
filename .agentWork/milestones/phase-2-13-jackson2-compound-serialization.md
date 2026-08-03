# Phase 2.13 — Jackson 2 Compound Serialization

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.3 and 2.12  
> **Status:** Not started

## Goal

Port explicit, bounded compound-document inclusion to Jackson 2 with Jackson 3 semantic parity.

## Research and constraints

- Phase 2.3 defines requested include paths, allow-list policy (owner JSON:API
  `resourceType` matching), limits (including zero/negative semantics), traversal,
  output deduplication seeded with primary identities, `(ResourceIdentity,
  request-list index, segment index)` traversal visit state,
  `ResourceObject.equals` conflict detection, deterministic order, validation
  precedence (including heterogeneous primary types), once-materialized primary
  `Iterable` snapshot for `toResourceCollection`, factory- vs mapper-time
  `IncludePath` exception contract, per-call traversal state, and the mapping-only
  compound context (no `ValidationContext` on the serialization context;
  validation stays on the document writer).
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — linkage never implies inclusion,
  and persistence/lazy-loading policy remains application-owned.
- Phase 2.12 supplies Jackson 2 mapping definitions; traversal reuses them and must not rescan
  domain types.
- Shared compound **codec** fixtures (not runtime delegation) establish wire/schema parity
  between Jackson majors; domain graphs in `CompoundSerializationSpec` still prove mapping
  traversal parity.
- Mapper compound overloads follow Phase 2.3's three-argument shape only
  (`resource|iterable`, `@Nullable DocumentEnvelope`, `CompoundSerializationContext`) to avoid
  ambiguity with nullable envelope two-argument methods.

## Deliverables

- Add the Jackson 2 immutable mapping-only serialization context with API parity to Phase 2.3:
  `IncludePath.of(String)` (same dot-separated syntax, rejection rules, and
  `JsonApiMappingException` / `INVALID_INCLUDE_PATH` factory contract),
  `IncludePolicy.allowAll()` / `denyAll()` / `allowing(Set<RelationshipAllowance>)`,
  `RelationshipAllowance(resourceType, relationshipName)` where `resourceType` is the
  owner JSON:API type matched at every nested segment, `withX()` copy methods with the same
  defensive-copy contract, reject-negative / zero-limit semantics, and the same finite defaults
  (depth 10, count 100). The context has no `ValidationContext`; document validation remains on
  the Jackson 2 document writer.
- Extend the Jackson 2 resource mapper with Phase 2.3's three-argument overloads
  (`toDocument(Object, @Nullable DocumentEnvelope, CompoundSerializationContext)` and
  `toResourceCollection(Iterable<?>, @Nullable DocumentEnvelope, CompoundSerializationContext)`;
  no two-argument context overloads). Port path validation and relationship traversal using
  Phase 2.12 definitions with Phase 2.3 semantic parity: once-materialized primary `Iterable`
  snapshot for collections; access-vs-linkage (full linkage via `toResource` on selected
  resources; never-read / no-I/O wording apply to inclusion traversal only); output dedup vs
  `(ResourceIdentity, request-list index, segment index)` visit separation;
  `ResourceObject.equals` conflict checks; heterogeneous primary-type validation; per-call
  traversal state; validation precedence; intermediates, cycles, and first-encounter order; and
  the five `MappingDiagnostic` codes (`INVALID_INCLUDE_PATH`, `DENIED_RELATIONSHIP_INCLUDE`,
  `CONFLICTING_INCLUDED_REPRESENTATION`, `INCLUDE_DEPTH_EXCEEDED`, `INCLUDE_COUNT_EXCEEDED`)
  with dotted JSON:API `propertyPath` semantics.
- Run Phase 2.3's shared compound codec fixture/diagnostic matrix through Jackson 2 artifacts
  (reuse fixture bytes and catalog entries; no major-specific copies).
- Refresh Jackson 2 module docs/Javadoc and conformance notes for opt-in inclusion.
- Spock `CompoundSerializationSpec` covering domain-graph parity with Phase 2.3 (including the
  one-shot primary `Iterable` case and traversal-scoped access counting).

## Non-goals

- Sparse fieldsets; Phase 2.14 owns them.
- Implicit inclusion, ORM/lazy-loading integration, repositories, or authorization.
- Jackson 2 flat DTO reads, envelopes, or PATCH binding.
- Two-argument context-only mapper overloads (same ambiguity constraint as Phase 2.3).

## Implementation boundaries

- Defaults request no included resources and retain finite safety limits (including Phase 2.3
  zero/negative limit semantics).
- Off-path relationships are not accessed for inclusion traversal; include policy is checked before
  traversal property access (owner JSON:API type). Selected resources still emit full linkage
  (Phase 2.3 access-vs-linkage / traversal-scoped no-I/O contract). Primary identities are never
  re-emitted in `included`; traversal visit state remains separate from output deduplication and
  is allocated per mapping invocation.
- `toResourceCollection` materializes the primary `Iterable<?>` once into an ordered snapshot and
  reuses it for type validation, primary mapping, and inclusion traversal (Phase 2.3 contract).
- Production code imports no Jackson 3 or sibling internal types, and shared fixtures are not
  copied into major-specific variants.

## Test strategy

- Reuse Phase 2.3 cyclic, shared, nested, empty, conflict, limit (including zero), invalid-path
  (factory- and mapper-time), mixed-type (including one-shot primary `Iterable`), nested-policy,
  self-reference, overlapping- and different-suffix converging-path, concurrent-isolation, and
  multi-failure domain graphs.
- Reuse Phase 2.3's traversal-scoped access-counting fixture to prove off-path relationships are not
  accessed for inclusion traversal (parity with Phase 2.3's access-vs-linkage contract).
- Compare included resources, order, linkage, and stable diagnostics across Jackson 2 and 3.
- Shared writer fixtures prove codec/wire and draft-schema parity; they do not replace domain-graph
  mapping traversal tests.

## Acceptance criteria

- [ ] The Jackson 2 context surface (`IncludePath.of` syntax, `IncludePolicy` factories,
      `RelationshipAllowance`, `withX()` defensive copies, reject-negative/zero limits, defaults
      10/100), three-argument mapper overloads (including once-materialized primary `Iterable`
      snapshot), and the five `MappingDiagnostic` codes with dotted `propertyPath` semantics match
      Phase 2.3; explicit include requests produce the same included resources, intermediates,
      linkage, and first-encounter order as Phase 2.3.
- [ ] Policy rejection, conflicts (`ResourceObject.equals`), cycles, depth/count limits,
      overlapping-path traversal, primary identity exclusion from `included`, and invalid paths
      have parity diagnostics, and off-path relationships are not accessed for inclusion traversal.
- [ ] Jackson 2 compound code has no Jackson 3/runtime or sibling-internal dependency.
- [ ] The canonical `module-docs` checklist passes and conformance documentation records exact
      opt-in Jackson 2 inclusion behavior.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*CompoundSerializationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
