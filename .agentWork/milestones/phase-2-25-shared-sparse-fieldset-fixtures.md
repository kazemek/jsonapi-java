# Phase 2.25 — Shared Sparse-Fieldset Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `SparseFieldsetSpec`  
> **Dependencies:** Phases 2.8, 2.11, 2.13, and 2.24  
> **Status:** Not started

## Goal

Extract one version-neutral sparse-fieldset scenario catalog from Phase 2.8 tests for cross-major
parity.

## Research and constraints

- Phase 2.8 `SparseFieldsetSpec` owns the closed shared semantic inventory (all current tests).
  Concurrent isolation stays shared.
- Closed shared set from `SparseFieldsetSpec.groovy`:
  `unrestricted MappedDocument matches Phase 2.2 attributes and relationships`; `three-argument
  toDocument with empty fieldset map remains Phase 2.3 equivalent`; `present empty list emits
  identity-only primary`; `present empty list emits identity-only included when that type appears`;
  `present empty list with denyAll succeeds without DENIED_FIELDSET_FIELD`; `three-argument
  toDocument rejects non-empty fieldsets`; `three-argument toResourceCollection rejects non-empty
  fieldsets`; `attribute-only fieldset via toMappedDocument`; `relationship-only fieldset via
  toMappedResourceCollection`; `renamed JsonProperty fieldset names use final JSON:API names`;
  `renamed JsonApiAttribute fieldset uses body-text`; `renamed JsonApiRelationship fieldset uses
  written-by`; `unknown JsonApiRelationship rename fails against Java property name`; `per-type
  fieldsets do not strip unrelated included types`; `include author with fields articles title omits
  linkage and sets exception`; `nested include comments.author with fields comments body`;
  `attribute-only omission with fully linked includes keeps exception false`; `access counting
  proves linkage vs traversal split`; `unknown fieldset field fails with INVALID_FIELDSET_FIELD`;
  `denyAll rejects first present fieldset name`; `missing FieldAllowance denies with
  DENIED_FIELDSET_FIELD`; `unmapped name wins over policy denial`; `unused fieldset type keys are
  ignored`; `defensive copy isolates fieldset map and duplicate names collapse`; `FieldAllowance set
  is defensively copied`; `identity preserved under every fieldset shape`; `surviving fields keep
  mapping definition order`; `concurrent fieldset mappings isolate documents and exception flags`;
  `applyTo leaves base unchanged when exception flag is false`.
- Adapter-local: empty today unless a future concurrent isolation case needs a major-specific
  mapper; keep concurrent as shared.
- Phase 2.24 supplies compound-write scenarios and shared graph builders needed when fieldsets
  interact with inclusion.
- HTTP `fields[...]` parsing remains application/adapter-owned.

## Deliverables

- Add an immutable sparse-fieldset scenario catalog covering exactly the closed shared set above
  with expected mapped documents, access counts, and common diagnostics.
- Refactor Jackson 3 `SparseFieldsetSpec` to consume the catalog; adapter-local remains empty unless
  a major-mapper-only case appears.
- Add catalog integrity tests for unique ids, capabilities, and explicit exclusions.
- Update test-fixtures documentation for fieldset-write scenarios.

## Non-goals

- Compound catalog extraction (Phase 2.24) or Jackson 2 fieldset implementation (Phase 2.20).
- Query-parameter parsing or authorization policy.

## Implementation boundaries

- Fieldsets use final mapped JSON:API names and common `FieldPolicy` types.
- Full-linkage exception expectations are asserted only for actual relationship omission.

## Test strategy

- Parameterize the closed shared set through Jackson 3 and compare output, access counts, and
  diagnostics.
- Catalog integrity rejects omitted applicable cases.

## Acceptance criteria

- [ ] The closed shared `SparseFieldsetSpec` inventory (all named tests above) is present as shared
      scenarios without major-specific production imports; adapter-local is empty unless a
      major-mapper-only case is documented.
- [ ] Jackson 3 `SparseFieldsetSpec` consumes the catalog for that closed set.
- [ ] Shared expectations prove pre-access filtering and actual-omission full-linkage behavior.
- [ ] Catalog integrity and test-fixtures docs cover fieldset-write scenarios.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
