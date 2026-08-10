# Phase 2.24 — Shared Compound Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `CompoundSerializationSpec`  
> **Dependencies:** Phases 2.3, 2.11, and 2.13  
> **Status:** Not started

## Goal

Extract one version-neutral compound-inclusion scenario catalog from Phase 2.3 domain-graph tests
for cross-major parity.

## Research and constraints

- Phase 2.3 `CompoundSerializationSpec` owns the closed shared semantic inventory (all 34 current
  tests). Concurrent isolation is shared when it only needs shared models (include it).
- Closed shared set from `CompoundSerializationSpec.groovy`:
  `context-free overloads omit included`; `empty include path list omits included`; `includes nested
  intermediates for comments.author`; `shared identity is included once`; `empty resolution emits
  included empty array`; `self-reference primary is not re-emitted in included`; `prefix-overlapping
  paths traverse suffixes`; `converging different-suffix paths still traverse`; `conflicting
  representations fail`; `off-path relationships are not read for inclusion traversal`;
  `heterogeneous collection fails on later type`; `one-shot iterable is materialized once`; `nested
  policy matches owner resource type`; `nested policy denies wrong owner type`; `maxDepth zero
  rejects non-empty path`; `path longer than maxDepth fails`; `maxIncluded zero fails on first
  included resource`; `maxIncluded exceeded fails`; `negative limits are rejected`; `factory-time
  malformed paths fail with raw input`; `canonical constructor rejects whitespace and dotted
  segments`; `equivalent contexts compare equal`; `mapper-time unknown relationship fails`; `denied
  relationship fails before traversal`; `multi-failure precedence is depth then mapping then policy
  in path order`; `multi-failure mapping beats policy on the same segment`; `multi-failure
  request-list order prefers first path mapping over later policy`; `multi-failure nested segment
  mapping beats later-segment policy`; `runtime nested owner type re-checks include policy`; `empty
  primary collection still enforces maxDepth`; `cyclic graph with repeated segment path terminates`;
  `multi-primary multi-path first-discovery order`; `deep nested path includes the chain`;
  `concurrent compound mappings isolate included sets`.
- Adapter-local: empty today (no Jackson-API-specific compound cases); retain only mapper isolation
  locally if a future major-specific case appears.
- Phase 2.13 supplies shared write models and the major-neutral test-fixtures boundary.
- Canonical codec compound documents do not replace domain-graph traversal proofs.

## Deliverables

- Add an immutable compound-inclusion scenario catalog covering exactly the closed shared set above
  with expected included resources, order, access counts, and common diagnostics.
- Move any additional Jackson-neutral graph builders required by those scenarios into
  `jsonapi-java-test-fixtures`.
- Refactor Jackson 3 `CompoundSerializationSpec` to consume the catalog; adapter-local remains empty
  unless a Jackson-API-specific case appears.
- Add catalog integrity tests for unique ids, capabilities, and explicit exclusions.
- Update test-fixtures documentation for compound-write scenarios.

## Non-goals

- Sparse fieldsets; Phase 2.25 owns them.
- Flat mapping extraction (Phase 2.13) or Jackson 2 compound implementation (Phase 2.19).
- Sharing production inclusion engines.

## Implementation boundaries

- Scenarios depend on common compound policy types and shared write models only.
- Traversal scenarios preserve access-vs-linkage, snapshot, and visit-state contracts from Phase
  2.3.

## Test strategy

- Parameterize the closed shared set through Jackson 3 and compare included resources, order,
  linkage, access counts, and diagnostics.
- Catalog integrity rejects omitted applicable cases.

## Acceptance criteria

- [ ] The closed shared `CompoundSerializationSpec` inventory (all 34 named tests above) is present
      as shared scenarios without major-specific production imports; adapter-local is empty unless a
      Jackson-API-specific case is documented.
- [ ] Jackson 3 `CompoundSerializationSpec` consumes the catalog for that closed set.
- [ ] Shared expectations prove order, access-count, policy, and diagnostic parity.
- [ ] Catalog integrity and test-fixtures docs cover compound-write scenarios.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
