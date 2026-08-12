# Phase 2.25 — Shared Sparse-Fieldset Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `SparseFieldsetSpec`  
> **Dependencies:** Phases 2.8, 2.11, 2.13, and 2.28  
> **Status:** Not started

## Goal

Extract one version-neutral sparse-fieldset scenario catalog from Phase 2.8 tests for cross-major
parity.

## Research and constraints

- Phase 2.8 `SparseFieldsetSpec` owns the closed shared semantic inventory (all current tests).
  Concurrent isolation stays shared.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade; the
  catalog is `SparseFieldsetScenarios` in the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.sparsefieldset` under `src/main/java/` (`@NullMarked` per
  ADR-009), exposing the `FixtureCatalog<SparseFieldsetScenario>` contract through the Phase 2.28
  pinned static delegation surface and registered as
  `JsonApiFixtures.sparseFieldset()`. Adapter suites run the whole catalog and assert full coverage
  (`executedScenarioIds == catalogScenarioIds`) per the Phase 2.13 relaxed contract — no exclusion
  manifest.
- Closed shared set from `SparseFieldsetSpec.groovy` (initial inventory; the catalog grows by
  addition):
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
- Three scenarios depend on jackson3-local test models that must move into
  `jsonapi-java-test-fixtures` with this catalog: `renamed JsonApiRelationship fieldset uses
  written-by` and `unknown JsonApiRelationship rename fails against Java property name` use
  `ArticleWithRenamedAuthor`, and `access counting proves linkage vs traversal split` uses
  `AccessCountingFieldsetArticle` — both Jackson-neutral (only annotations, domainwrite models,
  and JDK imports) and referenced from no other spec. This supersedes Phase 2.13's checked
  "helpers remain local" criterion for these two models; a dated supersession note is added to
  the Phase 2.13 milestone file (mirroring the Phase 2.24 note).
- HTTP `fields[...]` parsing remains application/adapter-owned.

## Deliverables

- Add `SparseFieldsetScenarios` (fixed `sparsefieldset` package, `@NullMarked`) as an immutable
  sparse-fieldset scenario catalog covering the initial inventory above, exposing
  the `FixtureCatalog<SparseFieldsetScenario>` contract through the Phase 2.28 pinned static
  delegation surface (`all()`/`byId(String)` statics plus a `catalog()` accessor) and registered as
  `JsonApiFixtures.sparseFieldset()` on the Phase 2.28 facade. Each `SparseFieldsetScenario`
  implements the Phase 2.28 `Scenario` contract (stable `id()`, default `notes()`), and the
  catalog fulfills the full `FixtureCatalog` surface including `where(Predicate)`; unknown
  `byId` ids fail with the pinned message label `Unknown sparse-fieldset scenario id: <id>`.
  Scenario inputs are supplier-based and re-created per execution (the Phase 2.13
  `DomainWriteInput` pattern), so the stateful access-counting model gets a fresh instance on
  every run. Each
  `SparseFieldsetScenario` pins
  its payload: an explicit entry-point/operation discriminator (`TO_DOCUMENT`,
  `TO_RESOURCE_COLLECTION`, `TO_MAPPED_DOCUMENT`, `TO_MAPPED_RESOURCE_COLLECTION`, mirroring the
  `DomainWriteOperation` dispatch convention — the two `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`
  rejections are distinguishable from each other only through the discriminator; success versus
  rejection is carried by the discriminated outcome), the
  domain input value, the serialization context (include paths, the common `IncludePolicy` —
  defaults are `denyAll`, include scenarios carry `allowAll` —, fieldsets, and common
  `FieldPolicy`), and a discriminated expected outcome — expected mapped document/resource states
  with the full-linkage exception flag, or a failure diagnostic carrying the `MappingDiagnostic`
  code together with `resourceClass`/`propertyPath` (both `@Nullable` for
  `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`, where both are null in the pinned Jackson 3 behavior — the
  Phase 2.8 contract allows, but does not require, null there) — restricted to
  version-neutral core/common values; the plain
  three-argument success outcome without an exception flag (empty fieldset map, Phase 2.3
  equivalent) is a covered outcome state. The
  concurrent-isolation scenario is a two-context variant carrying both serialization contexts and
  both expected outcomes, executed concurrently by the adapter suite on a shared mapper. The
  identity-preservation scenario is a multi-context variant carrying the four fieldset shapes
  (empty map, present-empty list, attribute-only, relationship-only) with one identity-only
  expected state. For the defensive-copy scenarios, the mutation-isolation half (mutating the
  caller's map after context construction) remains a Jackson 3 suite-local assertion, since
  catalog-constructed contexts cannot be mutated by consumers; the shared scenarios reproduce the
  outcome of collapsed duplicates (their fieldsets carry the collapsed lists), while the collapse
  behavior itself is asserted by the Jackson 3 suite. The FieldAllowance entry follows
  the same split: the catalog scenario carries the `FieldPolicy` value with its allowance set
  (part of the serialization context), with two semantic outcomes — an allowance-satisfied
  success and a `DENIED_FIELDSET_FIELD` denial with `propertyPath` — while the caller-set
  mutation-isolation half stays a Jackson 3 suite-local assertion (the defensive-copy property
  belongs to the common `FieldPolicy` type, not the catalog).
  Null-bearing expectation members (absent attributes/relationships, absent `included`, and the
  rejection variant's `resourceClass`/`propertyPath`) are `@Nullable` per ADR-009. Shared
  expectations carry the zero-read access guarantees Phase 2.8 commits to (excluded attribute
  getters unread, off-path relationship getters unread); exact single-read counts are asserted in
  the Jackson 3 suite only, since per ADR-004 each major's mapping is authoritative over its own
  property-access patterns.
- Move the Jackson-neutral test models `ArticleWithRenamedAuthor` and
  `AccessCountingFieldsetArticle` from `jsonapi-java-jackson3.testmodel` into the fixed
  `sparsefieldset` package of `jsonapi-java-test-fixtures` (Jackson-neutral imports, `@Nullable`
  review of moved members, jackson3 test-source references repointed).
- Refactor Jackson 3 `SparseFieldsetSpec` to consume the catalog with a full-catalog coverage
  assertion (`executedScenarioIds == catalogScenarioIds`); adapter-local scenario content remains
  empty unless a major-mapper-only case appears, while the Jackson 3 suite retains harness-level
  assertions over catalog scenarios (the fieldset-map and FieldAllowance mutation-isolation
  halves, the duplicate-name collapse behavior, the `types: [...]`
  `FIELDSETS_REQUIRE_MAPPED_DOCUMENT` rejection-message composition, the exact single-read access
  counts, and the `applyTo`/writer-validation steps).
- Add catalog integrity tests for unique ids, resolvable expectations, and the `FixtureCatalog`
  contract (no explicit exclusions).
- Update test-fixtures documentation for fieldset-write scenarios via the `module-docs` skill
  (fixed `sparsefieldset` package map, `SparseFieldsetScenarios`/`SparseFieldsetScenario` entry
  points, `JsonApiFixtures.sparseFieldset()` accessor, agent notes, and the root `README.md`
  Project-structure row refresh naming the shared sparse-fieldset catalog).

## Non-goals

- Compound catalog extraction (Phase 2.24) or Jackson 2 fieldset implementation (Phase 2.20).
- Query-parameter parsing or authorization policy.
- Closed catalog indexes or adapter-local exclusion manifests; the catalog grows by addition.

## Implementation boundaries

- Fieldsets use final mapped JSON:API names and common `FieldPolicy` types.
- Full-linkage exception expectations are asserted only for actual relationship omission.

## Test strategy

- Parameterize the initial inventory through Jackson 3, compare output and diagnostics, assert
  the shared zero-read access guarantees through the access-counting scenario, and assert
  full-catalog coverage (`executedScenarioIds == catalogScenarioIds`); the exact single-read
  counts remain Jackson 3 suite-local assertions.
- Catalog integrity enforces unique ids, resolvable expectations, and the `FixtureCatalog`
  contract; full inventory coverage is asserted by the adapter suite's
  `executedScenarioIds == catalogScenarioIds`.

## Acceptance criteria

- [ ] The closed shared `SparseFieldsetSpec` inventory (all named tests above) is covered by the
      initial `SparseFieldsetScenarios` catalog — each named test either becomes a scenario or is
      explicitly split into a catalog scenario plus the pinned Jackson 3 suite-local assertion —
      exposing
      the `FixtureCatalog<SparseFieldsetScenario>` contract through the Phase 2.28 pinned static
      delegation surface (`sparsefieldset` package, `@NullMarked` with
      accurate `@Nullable` per ADR-009; `JsonApiFixtures.sparseFieldset()` registered) without
      major-specific production imports; `ArticleWithRenamedAuthor` and
      `AccessCountingFieldsetArticle` moved into `jsonapi-java-test-fixtures` with jackson3
      references repointed; adapter-local scenario content is empty unless a major-mapper-only
      case is documented (harness-level suite-local assertions remain).
- [ ] Jackson 3 `SparseFieldsetSpec` consumes the catalog for that set and asserts
      `executedScenarioIds == catalogScenarioIds`.
- [ ] Shared expectations prove pre-access filtering and actual-omission full-linkage behavior, and
      failure outcomes carry the `MappingDiagnostic` code with `resourceClass`/`propertyPath`.
- [ ] Catalog integrity covers the `FixtureCatalog` contract and the canonical `module-docs`
      checklist passes for the `sparsefieldset` package and entry points.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
