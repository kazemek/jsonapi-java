# Phase 2.15 — Jackson 3 Presence-Aware PATCH Binding

> **Scope:** `jsonapi-java-jackson3`, `jsonapi-java-jackson-common`, and `jsonapi-java-test-fixtures`  
> **Dependencies:** Phases 1.3, 2.4, 2.9, 2.11, 2.13, 2.14, and 2.28  
> **Status:** Not started

## Goal

Bind a validated JSON:API resource update into an immutable typed command that preserves exactly
which annotated DTO properties the client requested to change.

## Research and constraints

- [JSON:API 1.1 updating resources](https://jsonapi.org/format/1.1/#crud-updating) and
  [ADR-012](../../docs/adr/012-resource-patch-binding.md) — omitted members retain current values;
  supplied relationships replace linkage; the result is not JSON Merge Patch; commands are not full
  DTO envelopes.
- Phase 1.3 validates single-resource shape, required identity, relationship `data`, and optional
  endpoint/body identity before domain binding.
- Phase 2.9 owns reverse Jackson mapping definitions, relationship/identifier conversion, and
  diagnostics. Attribute binding today constructs DTOs via whole-map `convertValue`; PATCH must
  introduce typed conversion for each supplied attribute member (for example per-member
  `convertValue` to the mapped property type) inside the Jackson 3 patch binder. Phase 2.11 creates
  the common package for Jackson-import-free contracts but does not define PATCH command types.
  PATCH must not call `JsonApiResourceBinder` or whole-DTO `convertValue` construction.
- Document reading uses `JsonApiDocumentReader` (Phase 2.4) with a `DocumentReadContext` that
  carries `PrimaryDataKind.RESOURCE` and a `ValidationContext` forced to
  `DocumentUsage.UPDATE_REQUEST` (optional `EndpointIdentity`); that context is the sole
  aggregate-validation policy on the convenience path (one validate-on-read, then bind). Do not
  compose `JsonApiDomainDocumentReader` or typed envelope decoding into PATCH commands. Phase 2.10
  envelope work is not a dependency.
- Phase 2.14 supplies shared flat-read DTOs reused by PATCH scenarios.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade;
  shared PATCH entries implement `Scenario` in a fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.domainpatch` under `src/main/java/` (`@NullMarked` per
  ADR-009), with `PatchScenarios` implementing `FixtureCatalog<PatchScenario>` registered as
  `JsonApiFixtures.patch()`. The catalog grows by addition with stable ids; adapter suites run the
  whole catalog and assert full coverage (`executedScenarioIds == catalogScenarioIds`) per the
  Phase 2.13 relaxed contract — no closed-index manifest.
- Initial shared PATCH scenario inventory (stable ids for Phase 2.23 reuse; Jackson-major-neutral
  wire documents + expected changes/diagnostics only):
  `patch-omitted-and-supplied-attributes`; `patch-explicit-null-attribute`;
  `patch-attribute-rename`; `patch-ignored-unmapped-omitted-from-changes`;
  `patch-relationship-null-linkage`; `patch-relationship-single-linkage`;
  `patch-relationship-empty-collection`; `patch-relationship-non-empty-collection`;
  `patch-relationship-cardinality-mismatch`; `patch-compound-included-ignored`;
  `patch-endpoint-identity-mismatch`; `patch-missing-relationship-data`;
  `patch-wrong-primary-shape`; `patch-resource-type-mismatch`;
  `patch-identifier-conversion-failure`; `patch-attribute-conversion-failure`;
  `patch-unsupported-relationship-target`.
  Shared validation diagnostics (Phase 1.3): `patch-wrong-primary-shape` →
  `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`; `patch-missing-relationship-data` →
  `RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`;
  `patch-endpoint-identity-mismatch` → `ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or `/data/id`.
- Adapter-local cases by exact name stay in each major's `*PatchBindingSpec` only, documented
  there with major-local harnesses and never enumerated in a shared manifest:
  `custom deserializer applies to attribute change`; `patch-custom-linkage-conversion`.
- [ADR-004](../../docs/adr/004-jackson-integration.md) and [ADR-009](../../docs/adr/009-jspecify-nullness.md)
  constrain typed change values and null-bearing public APIs.
- Conformance matrix edits (name both rows): Domain mapping “Presence-aware resource-update
  commands” → mark **supported** for Jackson 3 binding (Jackson 2 remains Phase 2.23). Phase 1.3
  “Command application (PATCH binding)” → retitle to “Command application” and mark **out of
  scope** with the note “Applications apply authorized update commands (Jackson 2 binding remains
  deferred per Phase 2.23)”, so no row can be read as unfinished binding. The Domain mapping
  section header's deferral note is reworded from “PATCH 2.15/2.23 and Jackson 2 parity —
  deferred” to “Jackson 2 parity — deferred”: only the resolved PATCH part is dropped; the
  section-level Jackson 2 deferral signal stays because Phases 2.16–2.23 are all not started.

## Deliverables

- Add immutable `@NullMarked` public patch-command, property-change, and relationship-linkage
  contracts in `io.github.kazemek.jsonapi.jackson`: `PatchCommand<T>` (typed update identity plus
  ordered changes, presence-aware), `AttributeChange` / `RelationshipChange` (keyed by final
  JSON:API name with explicit null/presence states), with explicit presence APIs and nullable
  values only where JSON null is legal; keep Jackson 3 `JavaType`/mapper-bound entry points under
  `io.github.kazemek.jsonapi.jackson3`.
- Add Jackson 3 patch reader entry points: a `JsonApiJackson3.patchReader(JsonMapper, ...)`
  factory (mirroring the existing reader/binder factory pattern) returning a named
  `JsonApiPatchReader` with `readValue(...)`/`fromDocument(...)` overloads whose convenience
  pipeline is one `JsonApiDocumentReader` validate-on-read (Phase 2.4) via `DocumentReadContext`
  with `PrimaryDataKind.RESOURCE` and a factory-accepted `ValidationContext` forced to
  `DocumentUsage.UPDATE_REQUEST` (optional `EndpointIdentity` via `withExpectedEndpointIdentity`),
  then presence-aware binding of only supplied attributes and relationships. That read context is
  the sole aggregate-validation policy before bind—no second validate with defaults. Reuse Phase
  2.9 mapping definitions and relationship/identifier conversion diagnostics; introduce per-member
  typed attribute conversion in the patch binder (do not call `JsonApiResourceBinder` or whole-DTO
  construction; never synthesize omitted changes; never read `included`). Expose typed update
  resource identity (DTO identifier property via Phase 2.9: `@JsonApiId` or logical name `id`, then
  `IdentifierConverter.parse` and typed coercion) separately from ordered requested changes;
  identity is never emitted as an attribute or relationship change. Do not compose
  `JsonApiDomainDocumentReader` / typed envelope decoding into PATCH commands (ADR-012: commands
  are not full DTO envelopes).
- Add `PatchScenario` implementing `Scenario` (stable `id()`, default `notes()`) and
  `PatchScenarios` implementing `FixtureCatalog<PatchScenario>` (both in the fixed package
  `io.github.kazemek.jsonapi.testfixtures.domainpatch`, `@NullMarked`) to
  `jsonapi-java-test-fixtures` for the initial shared inventory above
  (attribute/relationship presence, identity mismatches, and stable diagnostics) for Phase 2.23
  reuse, with `all()`/`byId(String)` and registered
  as `JsonApiFixtures.patch()` on the Phase 2.28 facade; keep the named adapter-local cases
  (`custom deserializer applies to attribute change`, `patch-custom-linkage-conversion`) out of
  the shared catalog. Each `PatchScenario` carries: one stable id, one neutral wire document (or a
  supplier of it), the target DTO `Class` (shared from `domainwrite`/`domainread`), and a
  discriminated expectation — either the expected ordered attribute-then-relationship changes
  (typed values from shared DTO property types with explicit null/presence states, keyed by final
  JSON:API name) or an expected diagnostic code with its resource-relative pointer.
- Use `module-docs` for the changed `jsonapi-java-jackson3`, `jsonapi-java-jackson-common`, and
  `jsonapi-java-test-fixtures` surfaces and apply the named conformance matrix edits above.

## Non-goals

- Constructing a complete DTO from a partial update, calling `JsonApiResourceBinder` / whole-DTO
  `convertValue`, or mutating an existing DTO/domain object.
- Composing typed domain envelopes into PATCH commands.
- Authorization, business validation, persistence, transactions, or relationship endpoint logic.
- Resolving relationship changes from `included` or assembling a graph.
- Treating links, meta, extension/profile members, or included resources as patchable DTO fields.
- JSON Merge Patch, JSON Patch, bulk updates, or atomic operations.
- Command application / mutation of domain or persistence objects.
- Closed catalog indexes or adapter-local exclusion manifests; `PatchScenarios` grows by addition
  and adapter-local cases live in adapter test specs only.

## Implementation boundaries

- Patch binding accepts only documents validated with Phase 1.3 update usage. Convenience input
  methods perform one `JsonApiDocumentReader` validate-on-read then presence-aware binding as an
  all-or-nothing operation. Patch reader factories accept a `ValidationContext` forced to
  `DocumentUsage.UPDATE_REQUEST` (optional `EndpointIdentity` via `withExpectedEndpointIdentity`;
  comparison off when null) and compose it into `DocumentReadContext` with
  `PrimaryDataKind.RESOURCE` as the sole aggregate-validation policy for that reader—no second
  validate with defaults.
  `patch-endpoint-identity-mismatch` asserts `ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or
  `/data/id` when a non-matching expected identity is supplied.
  `patch-wrong-primary-shape` asserts `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`;
  `patch-missing-relationship-data` asserts `RELATIONSHIP_DATA_REQUIRED` at
  `/data/relationships/<name>/data`.
- Neutral command types live in the common package; no DTO constructor runs and no defaults are
  fabricated for omitted properties. Binding reuses Phase 2.9 mapping definitions and
  relationship/identifier diagnostics, and introduces per-member typed attribute conversion—not
  `JsonApiResourceBinder` / whole-DTO construction.
- Jackson 3 `*PatchBindingSpec` must also cover the named adapter-local cases
  (`custom deserializer applies to attribute change`, `patch-custom-linkage-conversion`) with
  major-local harnesses; those cases are not shared fixtures.
- Command identity is the DTO identifier property resolved exactly as Phase 2.9 (`@JsonApiId` or
  logical name `id`, then `IdentifierConverter.parse` and typed coercion). It is exposed on the
  command separately from the change set and is never emitted as an attribute or relationship
  change. `patch-identifier-conversion-failure` proves `IDENTIFIER_CONVERSION_FAILED` at `/id` on
  that path; `patch-resource-type-mismatch` proves `RESOURCE_TYPE_MISMATCH` at `/type` when wire
  `type` ≠ `@JsonApiResource.type()`.
- Attribute and relationship changes are keyed by final JSON:API name (including
  `@JsonApiAttribute` / `@JsonApiRelationship` renames). Unmapped or `@JsonIgnore` supplied names
  are omitted from the change set (Phase 2.9 ignore-unmapped policy). Duplicate/colliding mapping
  definitions, conversion failures, cardinality mismatches, unsupported targets, resource-type
  mismatch, identifier conversion failure, and other named Phase 2.9 diagnostics still fail before
  a command escapes.
- Per-member attribute conversion failures emit `UNSUPPORTED_ATTRIBUTE_VALUE` with resource-relative
  pointer `/` + Jackson logical property name (same convention as Phase 2.9 binder attribute
  failures from bulk `convertValue`); `patch-attribute-conversion-failure` asserts that diagnostic
  and pointer. Relationship conversion/cardinality/unsupported-target diagnostics keep Phase 2.9
  paths (`/relationships/<jsonapiName>/data`).
- Relationship changes remain linkage-only and retain null versus empty collection cardinality.
  Typed identity remains outside the change set.
- Encounter order is total and deterministic: attribute changes in `attributes` map iteration
  order, then relationship changes in `relationships` map iteration order; typed identity is
  exposed separately. The application chooses whether and how to apply, authorize, or reorder
  changes.

## Test strategy

- Add a `PatchScenariosCatalogSpec`-style integrity check in `jsonapi-java-test-fixtures` for the
  `PatchScenarios` catalog: unique stable ids, `byId(String)` round-trip per entry, resolvable
  expected changes/diagnostics, and the additive-growth posture (mirroring `DomainWriteScenarios`
  / Phase 2.14 catalog invariants).
- Parameterize the initial shared PATCH scenario inventory through Jackson 3, collect executed
  scenario ids, and assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`);
  also cover the named adapter-local cases (`custom deserializer applies to attribute change`,
  `patch-custom-linkage-conversion`) in Jackson 3 `*PatchBindingSpec` only.
- Cover null/single/empty/non-empty relationship replacements and compound requests proving
  `included` never affects a change.
- Verify wrong primary shape (`UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`), missing relationship
  data (`RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`), endpoint mismatch
  (`ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or `/data/id` via caller-supplied
  `EndpointIdentity`), resource-type mismatch (`RESOURCE_TYPE_MISMATCH` at `/type`), identifier
  conversion failure (`IDENTIFIER_CONVERSION_FAILED` at `/id`), attribute conversion failure
  (`UNSUPPORTED_ATTRIBUTE_VALUE` at `/` + Jackson logical property name), cardinality mismatch,
  and unsupported target diagnostics before any command escapes. Assert attribute-then-relationship
  encounter order, JSON:API-name change keys for attributes and relationships, and that typed
  identity is never listed among changes.

## Acceptance criteria

- [ ] Patch commands (`PatchCommand<T>` with `AttributeChange` / `RelationshipChange`) contain
      exactly the supplied mapped attribute and relationship changes keyed
      by final JSON:API name, preserving explicit null, null linkage, empty collections, and
      attribute-then-relationship encounter order; typed identity is the DTO identifier property
      (Phase 2.9: `@JsonApiId` or logical `id`, then `IdentifierConverter.parse` and coercion),
      exposed separately and never as a change.
- [ ] Omitted DTO properties never invoke constructors/deserializers, acquire fabricated defaults,
      or appear as changes; public patch APIs satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] Pipeline is one `JsonApiDocumentReader` validate-on-read via `DocumentReadContext`
      (`PrimaryDataKind.RESOURCE` + factory-accepted `ValidationContext` forced to
      `UPDATE_REQUEST`, optional `EndpointIdentity`) exposed through the named
      `JsonApiJackson3.patchReader(...)` / `JsonApiPatchReader` entry points, then presence-aware
      binding via Phase 2.9
      mapping definitions, relationship/identifier diagnostics, and newly introduced per-member
      attribute conversion (never a second defaults validate, never `JsonApiResourceBinder` /
      whole-DTO construction, never typed envelopes / `JsonApiDomainDocumentReader`, never
      `included`, never application mutation).
- [ ] The initial shared PATCH scenario inventory is cataloged as `PatchScenarios` implementing
      `FixtureCatalog<PatchScenario>` (fixed `domainpatch` package, `@NullMarked`) with
      `JsonApiFixtures.patch()` registered and a passing `PatchScenariosCatalogSpec` (unique ids,
      `byId` round-trip, resolvable expectations, additive posture), and is consumed with
      full-catalog coverage by Jackson 3 `*PatchBindingSpec`; that Spec also covers the named
      adapter-local cases (`custom deserializer applies to attribute change`,
      `patch-custom-linkage-conversion`) for Phase 2.23 parity, with no shared exclusion manifest.
- [ ] The canonical `module-docs` checklist passes for jackson3, jackson-common, and test-fixtures;
      Domain mapping “Presence-aware resource-update commands” is **supported** for Jackson 3; Phase
      1.3 “Command application (PATCH binding)” is retitled/clarified as application and marked
      **out of scope** with Phase 2.15 removed from that row’s notes.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*PatchBindingSpec'` and `./gradlew clean build`
      pass.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
