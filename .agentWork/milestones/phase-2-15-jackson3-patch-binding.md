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
  ADR-009), with `PatchScenarios` exposing the `FixtureCatalog<PatchScenario>` contract through
  the Phase 2.28 pinned static delegation surface, registered as
  `JsonApiFixtures.patch()` (see the Phase 2.28 milestone for the pinned surface and the Phase
  2.13 milestone for the relaxed additive catalog contract). The catalog grows by addition with
  stable ids; adapter suites run the
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
  `patch-endpoint-identity-mismatch` → `ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or `/data/id`;
  `patch-unsupported-relationship-target` → `UNSUPPORTED_RELATIONSHIP_TARGET` at
  `/relationships/<name>/data` (the Phase 2.9 resource-relative binder path; the patch reader
  does not join the `/data` document pointer for binder failures);
  `patch-relationship-cardinality-mismatch` → `RELATIONSHIP_CARDINALITY_MISMATCH` at
  `/relationships/<name>/data` (resource-relative, the Phase 2.9 binder path).
  Pointer-space convention: Phase 1.3 reader-validation diagnostics are document-relative
  (`/data`, `/data/relationships/...`, `/data/type`), binder-originated diagnostics keep Phase
  2.9 resource-relative pointers (`/id`, `/type`, `/` + property, `/relationships/<name>/data`).
- Adapter-local cases by exact name stay in each major's `*PatchBindingSpec` only, documented
  there with major-local harnesses and never enumerated in a shared manifest:
  `custom deserializer applies to attribute change`; `patch-custom-linkage-conversion`.
- [ADR-004](../../docs/adr/004-jackson-integration.md) and [ADR-009](../../docs/adr/009-jspecify-nullness.md)
  constrain typed change values and null-bearing public APIs.
- Conformance matrix edits (name both rows): Domain mapping “Presence-aware resource-update
  commands” → mark **supported** for Jackson 3 binding with the pinned replacement note
  “Jackson 3 binding supported (Phase 2.15); Jackson 2 binding remains Phase 2.23”. Phase 1.3
  “Command application (PATCH binding)” → retitle to “Command application” and mark **out of
  scope** with the single exact note “Applications apply authorized update commands; Jackson 2
  binding remains Phase 2.23” (the Jackson 2 deferral signal already lives in the Domain mapping
  section header), so no row can be read as unfinished binding. The Domain mapping
  section header's deferral note is reworded from “PATCH 2.15/2.23 and Jackson 2 parity —
  deferred” to “Jackson 2 parity — deferred”, and its supported-phases list gains Phase 2.15
  (“Phases 2.2–2.3, 2.8–2.10, 2.15 — supported; Jackson 2 parity — deferred”): only the resolved
  PATCH part is dropped; the
  section-level Jackson 2 deferral signal stays because Phases 2.16–2.23 are all not started.
  The conformance intro's provenance paragraph gains a Phase 2.15 clause (the Phase 1.3 / 2.9–2.12
  precedent).

## Deliverables

- Add immutable `@NullMarked` public patch-command, property-change, and relationship-linkage
  contracts in `io.github.kazemek.jsonapi.jackson`: `PatchCommand<T>` (typed update identity plus
  ordered changes, presence-aware), `AttributeChange` / `RelationshipChange` (keyed by final
  JSON:API name with explicit null/presence states), with explicit presence APIs and nullable
  values only where JSON null is legal; keep Jackson 3 `JavaType`/mapper-bound entry points under
  `io.github.kazemek.jsonapi.jackson3`. This placement is recorded by an ADR-007 amendment (the
  Phase 2.11 precedent), since ADR-007 today assigns presence-aware PATCH commands to
  `jsonapi-java-jackson3`; the amendment records that the Jackson-import-free command contracts
  live in `jsonapi-java-jackson-common`, with mapper-bound entry points in each major adapter.
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
  `PatchScenarios` exposing the `FixtureCatalog<PatchScenario>` contract through the Phase 2.28
  pinned static delegation surface (both in the fixed package
  `io.github.kazemek.jsonapi.testfixtures.domainpatch`, `@NullMarked`) to
  `jsonapi-java-test-fixtures` for the initial shared inventory above
  (attribute/relationship presence, identity mismatches, and stable diagnostics) for Phase 2.23
  reuse, with `all()`/`byId(String)`/`where(Predicate)` statics plus a `catalog()` accessor and
  registered
  as `JsonApiFixtures.patch()` on the Phase 2.28 facade; keep the named adapter-local cases
  (`custom deserializer applies to attribute change`, `patch-custom-linkage-conversion`) out of
  the shared catalog. Each `PatchScenario` carries: one stable id, one neutral wire document (or a
  supplier of it), the target DTO `Class` (shared from `domainwrite`/`domainread`; the
  `domainpatch` package may add PATCH-specific DTO entries when that set is insufficient for the
  closed inventory), an optional `@Nullable` expected endpoint identity, and a
  discriminated expectation — either the expected ordered attribute-then-relationship changes
  (typed values from shared DTO property types with explicit null/presence states, keyed by final
  JSON:API name) or an expected diagnostic code with its pointer in the pinned space —
  document-relative for Phase 1.3 reader-validation diagnostics, resource-relative for
  binder-originated diagnostics. The adapter
  suite maps the optional expected endpoint identity onto
  `withExpectedEndpointIdentity` — never dispatching on the scenario id — so
  `patch-endpoint-identity-mismatch` is self-describing for Jackson 3 and Phase 2.23. Expected
  diagnostics use the pinned pointer-space convention: Phase 1.3 reader-validation diagnostics
  are document-relative, binder-originated diagnostics keep Phase 2.9 resource-relative
  pointers. The `fromDocument(...)` overloads bind a caller-supplied document without
  re-validation (the `JsonApiDomainDocumentReader.fromDocument` precedent); the validate-on-read
  guarantee applies to the `readValue(...)` path, and callers of `fromDocument` carry the Phase
  1.3 update-usage validation responsibility.
- Use `module-docs` for the changed `jsonapi-java-jackson3`, `jsonapi-java-jackson-common`, and
  `jsonapi-java-test-fixtures` surfaces, apply the named conformance matrix edits above (the
  targeted `docs/conformance.md` rows are the Phase 1.3 "Command application" row, the Domain
  mapping section header, and the "Presence-aware resource-update commands" row), and
  update `docs/vision.md`'s jackson3 module line (presence-aware PATCH binding is available as of
  Phase 2.15, not later Phase 2 work) and its jackson-common module line (the shared-artifact
  contract categories gain presence-aware update commands). Per the planned ADR-007 amendment
  (see deliverable 1), the
  neutral command types live in `jsonapi-java-jackson-common` (the Jackson-import-free shared
  artifact), with mapper-bound entry points confined to `jsonapi-java-jackson3`.

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

- The milestone lands as two sequential reviewable commits (the Phase 2.14 precedent), verified
  as one unit by the milestone review with the commit-1 checkpoint (green build) checked
  explicitly: commit 1 = the neutral command contracts, the ADR-007
  amendment, and the Jackson 3 patch-reader entry points with a green build — the checkpoint
  verifies compilation plus the existing module suites, since commit 1 ships the new public API
  before its behavioral suites exist; commit 2 = the
  `PatchScenarios` catalog, the creation of the Jackson 3 `*PatchBindingSpec` suites (which prove
  the binding semantics), and the
  docs/conformance edits. The milestone sits at the top edge of the size gate; the two-commit
  sequence (commit 1 independently buildable, the pair reviewed as one unit with the commit-1
  checkpoint checked explicitly) is a deliberate, recorded size-gate decision, mirroring Phase
  2.14.
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
  pointer `/` + Jackson logical property name — the same convention as Phase 2.9 binder attribute
  failures, but here produced deterministically by the patch binder's own per-member conversion
  (not the bulk `convertValue` path, which the patch binder never calls);
  `patch-attribute-conversion-failure` asserts that diagnostic
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
  `PatchScenarios` catalog: unique stable ids, `byId(String)` round-trip per entry, and
  resolvable expected changes/diagnostics (mirroring `DomainWriteScenarios` / Phase 2.14 catalog
  invariants); additive pickup is enforced by the adapter suite's full-catalog coverage
  assertion, not by the integrity spec.
- Parameterize the initial shared PATCH scenario inventory through Jackson 3, collect executed
  scenario ids, and assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`);
  also cover the named adapter-local cases (`custom deserializer applies to attribute change`,
  `patch-custom-linkage-conversion`) in Jackson 3 `*PatchBindingSpec` only, exercising the
  `patchReader` factory's named `IdentifierConverter`, linkage-mapper, `Builder`, and `JavaType`
  overloads.
- Cover null/single/empty/non-empty relationship replacements and compound requests proving
  `included` never affects a change.
- Verify wrong primary shape (`UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`), missing relationship
  data (`RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`), endpoint mismatch
  (`ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or `/data/id` via caller-supplied
  `EndpointIdentity`), resource-type mismatch (`RESOURCE_TYPE_MISMATCH` at `/type`), identifier
  conversion failure (`IDENTIFIER_CONVERSION_FAILED` at `/id`), attribute conversion failure
  (`UNSUPPORTED_ATTRIBUTE_VALUE` at `/` + Jackson logical property name), cardinality mismatch,
  and unsupported target (`UNSUPPORTED_RELATIONSHIP_TARGET` at
  `/relationships/<name>/data`, resource-relative per Phase 2.9) diagnostics before any command
  escapes; duplicate/colliding
  mapping definitions fail before a command escapes (a targeted mapper-definition case in the
  Jackson 3 suite). Entry-point coverage is pinned per scenario kind: the reader-validation
  diagnostic scenarios (`patch-wrong-primary-shape`, `patch-missing-relationship-data`,
  `patch-endpoint-identity-mismatch`) run through `readValue`; at least one success and one
  binder-diagnostic scenario run through `fromDocument` to prove the no-revalidation guarantee;
  all remaining scenarios run through `readValue`. A facade-spec assertion covers
  `JsonApiFixtures.patch()` view identity and the `where` shim, per the Phase 2.28 facade-spec
  pattern. Assert attribute-then-relationship
  encounter order, JSON:API-name change keys for attributes and relationships, and that typed
  identity is never listed among changes.

## Acceptance criteria

- [ ] Patch commands (`PatchCommand<T>` with `AttributeChange` / `RelationshipChange`) contain
      exactly the supplied mapped attribute and relationship changes keyed
      by final JSON:API name, preserving explicit null, null linkage, empty collections, and
      attribute-then-relationship encounter order; typed identity is the DTO identifier property
      (Phase 2.9: `@JsonApiId` or logical `id`, then `IdentifierConverter.parse` and coercion),
      exposed separately and never as a change. The neutral command contracts live in
      `io.github.kazemek.jsonapi.jackson` per the ADR-007 amendment recorded in deliverable 1.
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
- [ ] The initial shared PATCH scenario inventory is cataloged as `PatchScenarios` exposing the
      `FixtureCatalog<PatchScenario>` contract through the Phase 2.28 pinned static delegation
      surface (fixed `domainpatch` package, `@NullMarked`) with
      `JsonApiFixtures.patch()` registered and a passing `PatchScenariosCatalogSpec` (unique ids,
      `byId` round-trip, resolvable expectations, additive posture), and is consumed with
      full-catalog coverage by Jackson 3 `*PatchBindingSpec`; that Spec also covers the named
      adapter-local cases (`custom deserializer applies to attribute change`,
      `patch-custom-linkage-conversion`) for Phase 2.23 parity, with no shared exclusion manifest.
- [ ] The canonical `module-docs` checklist passes for jackson3, jackson-common, and test-fixtures;
      Domain mapping “Presence-aware resource-update commands” is **supported** for Jackson 3
      with the pinned note “Jackson 3 binding supported (Phase 2.15); Jackson 2 binding remains
      Phase 2.23”; Phase
      1.3 “Command application (PATCH binding)” is retitled to “Command application”, marked
      **out of scope**, and carries the single exact note “Applications apply authorized update
      commands; Jackson 2 binding remains Phase 2.23”; the Domain mapping section header is
      reworded to “Jackson 2 parity — deferred” with Phase 2.15 added to its supported-phases
      list; the conformance intro gains a Phase 2.15 provenance clause; and the
      `docs/vision.md` jackson3 and jackson-common module lines
      reflect that presence-aware PATCH binding is available as of Phase 2.15 and that the
      jackson-common contract categories include presence-aware update commands.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*PatchBindingSpec'` and `./gradlew clean build`
      pass.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
