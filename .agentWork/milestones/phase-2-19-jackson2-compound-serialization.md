# Phase 2.19 — Jackson 2 Compound Serialization

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phase 2.18  
> **Status:** Not started
> **Work item:** KAZ-35

## Goal

Port explicit, bounded compound-document inclusion to Jackson 2 with Jackson 3 semantic parity,
using common policy types and shared compound-write fixtures.

## Research and constraints

- jackson3 compound inclusion (`jsonapi-java-jackson3` README /
  [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md)) defines requested include paths,
  allow-list policy (owner JSON:API `resourceType`
  matching), limits (including zero/negative semantics), traversal, output deduplication seeded
  with primary identities, visit-state separation, `ResourceObject.equals` conflict detection,
  deterministic order, validation precedence, once-materialized primary `Iterable` snapshot,
  factory- vs mapper-time `IncludePath` exceptions, per-call traversal state, and the mapping-only
  compound context.
- `jsonapi-java-jackson-common` places `CompoundSerializationContext`, `IncludePath`, `IncludePolicy`,
  `RelationshipAllowance`, and related diagnostics in the common package; Jackson 2 must consume
  those types rather than redefining them.
- `JsonApiFixtures.compoundWrite()` / `CompoundWriteScenarios` is the shared compound-inclusion
  scenario catalog; Phase 2.18 supplies Jackson 2 mapping definitions that traversal must reuse
  without rescanning domain types.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — linkage never implies inclusion,
  and persistence/lazy-loading policy remains application-owned.
- Mapper compound overloads follow jackson3's three-argument shape only
  (`resource|iterable`, `@Nullable DocumentEnvelope`, `CompoundSerializationContext`).

## Deliverables

- Wire Jackson 2 resource-mapper three-argument overloads to the common compound context and
  Phase 2.18 mapping definitions with jackson3 semantic parity (snapshot, access-vs-linkage,
  output dedup vs visit-state separation, conflict checks, heterogeneous primary validation,
  deterministic order, and the five `MappingDiagnostic` codes with dotted `propertyPath`).
- Port relationship traversal and path validation without rescanning domain types or depending on
  Jackson 3.
- Consume the `CompoundWriteScenarios` catalog (and applicable codec fixtures for wire/schema
  parity) without major-specific copies.
- Use `module-docs` to refresh Jackson 2 module docs/Javadoc and conformance notes for opt-in
  inclusion.
- Spock `CompoundSerializationSpec` covering domain-graph parity with jackson3 compound inclusion, including the
  one-shot primary `Iterable` case and traversal-scoped access counting.

## Non-goals

- Sparse fieldsets; Phase 2.20 owns them.
- Implicit inclusion, ORM/lazy-loading integration, repositories, or authorization.
- Jackson 2 flat DTO reads, envelopes, or PATCH binding.
- Redefining common compound policy types under the `jackson2` package.
- Two-argument context-only mapper overloads (same ambiguity constraint as jackson3).

## Implementation boundaries

- Defaults request no included resources and retain finite safety limits (including jackson3
  zero/negative limit semantics).
- Off-path relationships are not accessed for inclusion traversal; include policy is checked before
  traversal property access. Selected resources still emit full linkage.
- `toResourceCollection` materializes the primary `Iterable<?>` once into an ordered snapshot.
- Production code imports no Jackson 3 or sibling internal types, and shared fixtures are not
  copied into major-specific variants.

## Test strategy

- Execute every scenario from the live `CompoundWriteScenarios` catalog through Jackson 2,
  collect executed scenario IDs, and assert `executedScenarioIds == catalogScenarioIds` so newly
  added scenarios cannot silently escape the suite.
- Compare included resources, order, linkage, and stable common diagnostics across Jackson 2 and 3.
- Shared codec fixtures prove wire/schema parity; they do not replace domain-graph traversal tests.
- Preserve adapter-local major-specific test cases separately from the shared catalog.

## Acceptance criteria

- [ ] Jackson 2 three-argument mapper overloads consume common compound contracts, execute every
      live `CompoundWriteScenarios` scenario, and assert `executedScenarioIds == catalogScenarioIds`;
      they match jackson3 included resources, intermediates, linkage, and first-encounter order.
- [ ] Policy rejection, conflicts, cycles, depth/count limits, overlapping-path traversal, primary
      identity exclusion from `included`, and invalid paths have parity diagnostics, and off-path
      relationships are not accessed for inclusion traversal.
- [ ] Jackson 2 compound code has no Jackson 3/runtime or sibling-internal dependency and does not
      redefine common policy types.
- [ ] The canonical `module-docs` checklist passes and conformance documentation records exact
      opt-in Jackson 2 inclusion behavior; public `@Nullable DocumentEnvelope` compound overloads
      satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*CompoundSerializationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
