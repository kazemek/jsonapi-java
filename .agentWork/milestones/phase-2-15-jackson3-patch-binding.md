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
  DTO envelopes. Core already enforces the update document shape (Phase 1.3); this milestone binds
  only supplied mapped members.
- Phase 1.3 validates single-resource shape, required identity, relationship `data`, and optional
  endpoint/body identity before domain binding. Reader-validation failures stay
  `JsonApiDocumentReadException` with `ValidationRuleCode` (document-relative pointers).
- Phase 2.9 owns reverse Jackson mapping definitions, relationship/identifier conversion, and
  `MappingDiagnostic` codes. Attribute binding today constructs DTOs via whole-map `convertValue`;
  PATCH introduces per-member `JsonMapper.convertValue` to the mapped property `JavaType` inside a
  Jackson 3 patch binder. Do not call `JsonApiResourceBinder` or whole-DTO `convertValue`
  construction. Internal helpers may be shared with `DomainResourceBinder` (cardinality, built-in
  linkage, `RelationshipLinkageMapper`, identifier parse/coerce).
- Phase 2.11 created `io.github.kazemek.jsonapi.jackson` but did not define PATCH command types.
  [ADR-007](../../docs/adr/007-module-boundaries.md) today assigns presence-aware PATCH commands to
  `jsonapi-java-jackson3`; this milestone amends it (Phase 2.11 precedent) so Jackson-import-free
  command types live in `jsonapi-java-jackson-common` and mapper-bound entry points stay in each
  major adapter. [ADR-010](../../docs/adr/010-architectural-tests.md) allowlists are unchanged:
  new common types stay in `io.github.kazemek.jsonapi.jackson`; `JsonApiPatchReader` stays in
  `io.github.kazemek.jsonapi.jackson3`.
- Document reading uses `JsonApiDocumentReader` (Phase 2.4) with a `DocumentReadContext` that
  carries `PrimaryDataKind.RESOURCE` and a `ValidationContext` whose usage is forced to
  `DocumentUsage.UPDATE_REQUEST` (optional `EndpointIdentity` preserved from the caller context).
  That composed context is the sole aggregate-validation policy on the convenience path (one
  validate-on-read, then bind). Do not accept a caller `DocumentReadContext` (it could carry the
  wrong kind). Do not compose `JsonApiDomainDocumentReader` or typed envelopes. Phase 2.10 is not
  a dependency.
- Phase 2.14 supplies shared flat-read DTOs reused by PATCH scenarios (`FlatArticle`,
  `FlatIntIdArticle`, `FlatCountedThing`, `FlatThingWithIgnored`, `FlatPersonArticle`). The
  `domainpatch` package may add PATCH-specific DTOs only when that set cannot express an initial
  inventory entry.
- Phase 2.28 owns `Scenario` / `FixtureCatalog` and `JsonApiFixtures`; shared PATCH entries
  implement `Scenario` in `io.github.kazemek.jsonapi.testfixtures.domainpatch` under
  `src/main/java/` (`@NullMarked` per [ADR-009](../../docs/adr/009-jspecify-nullness.md)), with
  `PatchScenarios` exposing `FixtureCatalog<PatchScenario>` through the Phase 2.28 pinned static
  delegation surface (`all()` / `byId(String)` / `where(Predicate)` / `catalog()`), registered as
  `JsonApiFixtures.patch()`. Construct the catalog with `FixtureCatalog.of("patch", …)` so unknown
  ids fail as `Unknown patch scenario id: <id>`. The catalog grows by addition with stable ids;
  adapter suites run the whole catalog and assert `executedScenarioIds == catalogScenarioIds` per
  the Phase 2.13 relaxed contract — no closed-index manifest.
- Initial shared PATCH scenario inventory (stable ids for Phase 2.23 reuse; Jackson-major-neutral
  JSON documents + expected changes/diagnostics only):
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
  `/relationships/<name>/data` (Phase 2.9 resource-relative binder path; the patch reader does
  not join `/data` onto binder failures);
  `patch-relationship-cardinality-mismatch` → `RELATIONSHIP_CARDINALITY_MISMATCH` at
  `/relationships/<name>/data`.
  Pointer-space convention: Phase 1.3 reader-validation diagnostics are document-relative
  (`/data`, `/data/relationships/...`, `/data/type`); binder-originated diagnostics keep Phase
  2.9 resource-relative pointers (`/id`, `/type`, `/` + logical property, `/relationships/<name>/data`).
- Adapter-local cases by exact name stay in each major's `*PatchBindingSpec` only, documented
  there with major-local harnesses and never enumerated in a shared manifest:
  `custom deserializer applies to attribute change`; `patch-custom-linkage-conversion`.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — per-member `convertValue` is how custom
  deserializers apply to attribute changes. [ADR-009](../../docs/adr/009-jspecify-nullness.md) —
  omitted vs explicit JSON `null` is list membership vs `@Nullable` change value; do not add a
  sealed attribute-null variant (core `Attributes` already uses `@Nullable` map values).
  Relationship `NullLinkage` converts to Java `null` or empty `Optional` as Phase 2.9 would; the
  change is still present in the command.
- Conformance matrix edits (name both rows): Domain mapping “Presence-aware resource-update
  commands” → mark **supported** for Jackson 3 binding with the pinned replacement note
  “Jackson 3 binding supported (Phase 2.15); Jackson 2 binding remains Phase 2.23”. Phase 1.3
  “Command application (PATCH binding)” → retitle to “Command application” and mark **out of
  scope** with the single exact note “Applications apply authorized update commands; Jackson 2
  binding remains Phase 2.23”. The Domain mapping section header is reworded from “PATCH 2.15/2.23
  and Jackson 2 parity — deferred” to “Jackson 2 parity — deferred”, and its supported-phases list
  gains Phase 2.15 (“Phases 2.2–2.3, 2.8–2.10, 2.15 — supported; Jackson 2 parity — deferred”).
  The conformance intro's provenance paragraph gains a Phase 2.15 clause (the Phase 1.3 / 2.9–2.12
  precedent).

## Deliverables

- Add immutable `@NullMarked` public patch-command types in
  `io.github.kazemek.jsonapi.jackson`: `PatchCommand<T>`, sealed `PatchChange` permitting
  `AttributeChange` and `RelationshipChange`. Record the placement with an ADR-007 amendment:
  Jackson-import-free command contracts live in `jsonapi-java-jackson-common`; mapper-bound
  entry points live in each major adapter. Amend the ADR-007 `jsonapi-java-jackson-common` bullet
  to include presence-aware update-command values (Phase 2.15), and change the jackson3 bullet
  from owning “presence-aware PATCH commands” to owning the PATCH reader entry points that
  produce those common commands. Do not add a new `MappingDiagnostic` constant.
- Add Jackson 3 patch reader entry points: `JsonApiJackson3.patchReader` overloads (exact matrix
  in Implementation boundaries) returning `JsonApiPatchReader` with `readValue` / `fromDocument`
  `Class` and `JavaType` overloads. Convenience `readValue` is one `JsonApiDocumentReader`
  validate-on-read then presence-aware bind of only supplied mapped attributes and relationships.
  Reuse Phase 2.9 mapping definitions and relationship/identifier conversion; introduce
  per-member typed attribute conversion. Never call `JsonApiResourceBinder` or whole-DTO
  construction; never synthesize omitted changes; never read `included`; never compose
  `JsonApiDomainDocumentReader`.
- Add `PatchScenario` implementing `Scenario` and `PatchScenarios` exposing
  `FixtureCatalog<PatchScenario>` through the Phase 2.28 pinned static delegation surface (fixed
  `domainpatch` package, `@NullMarked`) for the initial shared inventory, registered as
  `JsonApiFixtures.patch()`. Keep the named adapter-local cases out of the shared catalog.
  Payload shape is pinned in Implementation boundaries.
- Use `module-docs` for the changed `jsonapi-java-jackson3`, `jsonapi-java-jackson-common`, and
  `jsonapi-java-test-fixtures` surfaces; apply the named conformance matrix edits above; update
  `docs/vision.md`'s jackson3 module line (presence-aware PATCH binding is available as of Phase
  2.15) and its jackson-common module line (contract categories gain presence-aware update
  commands).

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
- A new `MappingDiagnostic` value, a `DocumentReadContext` factory argument, or a
  `JsonApiPatchReader.withExpectedEndpointIdentity` wither (endpoint identity is supplied on the
  factory `ValidationContext`).
- Changing ADR-010 allowlists or adding a `jackson.patch` subpackage.

## Implementation boundaries

- The milestone lands as two sequential reviewable commits (the Phase 2.14 precedent), verified
  as one unit by the milestone review with the commit-1 checkpoint (green build) checked
  explicitly: commit 1 = the neutral command contracts, the ADR-007 amendment, and the Jackson 3
  patch-reader entry points with a green build (compilation plus existing module suites; commit 1
  ships the new public API before its behavioral suites exist); commit 2 = the `PatchScenarios`
  catalog, the Jackson 3 `*PatchBindingSpec` suites, and the docs/conformance edits. The two-commit
  sequence is a deliberate, recorded size-gate decision: one coherent outcome, commit 1
  independently buildable, the pair reviewed as one unit.
- **Factory matrix** (mirrors `resourceBinder` plus the writer's `ValidationContext` argument;
  each `JsonMapper` overload has a `JsonMapper.Builder` twin that `build()`s then delegates;
  builders are not given the JSON:API module):
  `patchReader(JsonMapper)`;
  `patchReader(JsonMapper, ValidationContext)`;
  `patchReader(JsonMapper, ValidationContext, IdentifierConverter)`;
  `patchReader(JsonMapper, ValidationContext, IdentifierConverter, Map<Class<?>, RelationshipLinkageMapper>)`.
  The one-argument overload uses `ValidationContext.defaults()`. Every overload then **forces**
  usage with `validationContext.withDocumentUsage(DocumentUsage.UPDATE_REQUEST)` (other fields,
  including `expectedEndpointIdentity`, are preserved) and composes
  `DocumentReadContext.of(forced, PrimaryDataKind.RESOURCE)` as the sole aggregate-validation
  policy. Omitted converter/mappers default to `IdentifierConverter.defaults()` and `Map.of()`.
  Derive the binder mapper via `JsonMapper.rebuild()`; never mutate the caller mapper. Construct
  `JsonApiPatchReader` only from this factory (package-private constructor); it is safe for
  concurrent use once created.
- **`JsonApiPatchReader` methods:** `readValue(String|byte[]|InputStream|JsonParser, Class<T>)`
  returns `PatchCommand<T>`; `readValue(…, JavaType)` returns `PatchCommand<?>` whose
  `resourceType()` is the JavaType's raw class. Close/ownership rules match
  `JsonApiDocumentReader` (convenience overloads close parsers they create; caller-owned streams
  and parsers stay open). `fromDocument(JsonApiDocument, Class<T>|JavaType)` binds without
  re-validation. `fromDocument` requires non-null document whose `data()` is
  `DocumentData.SingleResource`; any other primary-data state (including absent/`NullData`/
  collections/identifiers) throws `IllegalArgumentException` (caller skipped Phase 1.3). Callers
  of `fromDocument` carry the Phase 1.3 update-usage validation responsibility.
- **Command types** (Jackson-import-free records in `io.github.kazemek.jsonapi.jackson`):
  `PatchCommand<T>(Class<T> resourceType, Object identity, List<PatchChange> changes)` —
  `T` is the annotated DTO type; `identity` is the converted DTO identifier property value
  (never `@Nullable`; fail instead of emitting a command with missing identity); `changes()` is
  an unmodifiable list in attribute-then-relationship encounter order. Compact constructor
  rejects null `resourceType`, `identity`, `changes`, or change elements.
  `PatchChange` is `sealed` and permits only `AttributeChange` and `RelationshipChange`.
  Each change carries `jsonapiName()` (the change key, including `@JsonApiAttribute` /
  `@JsonApiRelationship` renames) and `logicalName()` (Jackson logical property name).
  `AttributeChange` / `RelationshipChange` hold `@Nullable Object value()`: the per-member
  converted property value (Optional wrapping and List/Set/array shapes included). Omitted
  members never appear. Explicit attribute JSON `null` is `value == null` on a present
  `AttributeChange`. `NullLinkage` on to-one is `value == null` or empty `Optional` as Phase 2.9
  would bind. Empty to-many is an empty collection of the mapped property type.
- **Bind pipeline (fail-fast; no partial command):** resolve `ResourceMapping` via the Phase 2.9
  cache; `RESOURCE_TYPE_MISMATCH` at `/type` before identity or changes; convert identity; then
  attribute changes in `Attributes.attributes()` iteration order; then relationship changes in
  `Relationships.relationships()` iteration order. Identity conversion follows Phase 2.9:
  `@JsonApiId` or logical name `id`; `resource.hasId()` → parse + `convertValue` at `/id`; else
  lid if present at `/lid`; if neither, `IDENTIFIER_CONVERSION_FAILED` at `/id`. Identity is
  never emitted as a change. Unmapped or `@JsonIgnore` supplied names are omitted from the
  change set (Phase 2.9 ignore-unmapped policy). Duplicate/colliding mapping definitions still
  fail before a command escapes. Per-member attribute conversion is
  `mapper.convertValue(rawValue, propertyJavaType)` including JSON `null` (so primitive-null
  fails); failure emits `UNSUPPORTED_ATTRIBUTE_VALUE` at `/` + Jackson logical property name.
  Relationship conversion reuses Phase 2.9 cardinality / built-in `ResourceIdentifier` /
  registered `RelationshipLinkageMapper` rules, then `convertValue`s the intermediate value to
  the property `JavaType` so empty to-many matches List vs Set vs array. Relationship
  conversion/cardinality/unsupported-target diagnostics keep Phase 2.9 paths
  (`/relationships/<jsonapiName>/data`). Bind failures throw `JsonApiMappingException` with
  resource-relative pointers and are never wrapped into `JsonApiDocumentReadException` or
  prefixed with `/data`. Codec/aggregate failures on `readValue` stay
  `JsonApiDocumentReadException`.
- **`PatchScenario` payload:** stable `id()`; one JSON document string; target DTO `Class`;
  `@Nullable EndpointIdentity expectedEndpointIdentity`; sealed `PatchExpectation` permitting
  `Success(Object identity, List<PatchChange> changes)`,
  `ReaderFailure(ValidationRuleCode code, String jsonPointer)`, and
  `BinderFailure(MappingDiagnostic diagnostic, String propertyPath)`. The adapter suite maps
  optional expected endpoint identity onto
  `ValidationContext.withExpectedEndpointIdentity` — never dispatching on the scenario id.
  Success expectations use the production `PatchChange` types. Shared scenarios use the default
  identifier converter (identifier failure is `FlatIntIdArticle` coercion, not a custom
  `parse`).
- Jackson 3 `*PatchBindingSpec` must also cover the named adapter-local cases
  (`custom deserializer applies to attribute change`, `patch-custom-linkage-conversion`) with
  major-local harnesses, plus `Builder` / `JavaType` / named `IdentifierConverter` /
  linkage-mapper factory overloads. Those cases are not shared fixtures.
- Encounter order is total and deterministic as specified above. The application chooses whether
  and how to apply, authorize, or reorder changes.

## Test strategy

- Add a `PatchScenariosCatalogSpec` in `jsonapi-java-test-fixtures`: unique stable ids, `byId`
  round-trip per entry, resolvable target DTO in `domainread` / `domainwrite` / `domainpatch`,
  exactly one JSON document, exactly one discriminated `PatchExpectation`, and unknown-id
  message `Unknown patch scenario id: …` (mirroring `DomainReadScenariosCatalogSpec`). Additive
  pickup is enforced by the adapter suite's full-catalog coverage assertion, not by the integrity
  spec.
- Parameterize the initial shared PATCH scenario inventory through Jackson 3, collect executed
  scenario ids, and assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`);
  also cover the named adapter-local cases in Jackson 3 `*PatchBindingSpec` only, exercising the
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
  and unsupported target (`UNSUPPORTED_RELATIONSHIP_TARGET` at `/relationships/<name>/data`)
  diagnostics before any command escapes; duplicate/colliding mapping definitions fail before a
  command escapes (a targeted mapper-definition case in the Jackson 3 suite). Entry-point
  coverage is pinned per scenario kind: the reader-validation diagnostic scenarios
  (`patch-wrong-primary-shape`, `patch-missing-relationship-data`,
  `patch-endpoint-identity-mismatch`) run through `readValue`; at least one success and one
  binder-diagnostic scenario run through `fromDocument` to prove the no-revalidation guarantee;
  all remaining scenarios run through `readValue`. A facade-spec assertion covers
  `JsonApiFixtures.patch()` view identity and the `where` shim, per the Phase 2.28 facade-spec
  pattern. Assert attribute-then-relationship encounter order, JSON:API-name change keys,
  accompanying logical names, and that typed identity is never listed among changes.

## Acceptance criteria

- [ ] Patch commands (`PatchCommand<T>` with sealed `AttributeChange` / `RelationshipChange`)
      contain exactly the supplied mapped attribute and relationship changes keyed by final
      JSON:API name (and carrying Jackson `logicalName`), preserving explicit null, null
      linkage, empty collections of the mapped property type, and attribute-then-relationship
      encounter order; typed identity is the DTO identifier property (Phase 2.9: `@JsonApiId`
      or logical `id`, then `IdentifierConverter.parse` and coercion), exposed separately and
      never as a change. The neutral command contracts live in
      `io.github.kazemek.jsonapi.jackson` per the ADR-007 amendment recorded in deliverable 1.
- [ ] Omitted DTO properties never invoke constructors/deserializers, acquire fabricated defaults,
      or appear as changes; public patch APIs satisfy ADR-009 `@NullMarked` / `@Nullable` rules.
- [ ] Pipeline is one `JsonApiDocumentReader` validate-on-read via a factory-composed
      `DocumentReadContext` (`PrimaryDataKind.RESOURCE` + factory-accepted `ValidationContext`
      forced to `UPDATE_REQUEST`, optional `EndpointIdentity`) exposed through the named
      `JsonApiJackson3.patchReader(...)` / `JsonApiPatchReader` entry points, then presence-aware
      binding via Phase 2.9 mapping definitions, relationship/identifier diagnostics, and newly
      introduced per-member attribute conversion (never a second defaults validate, never
      `JsonApiResourceBinder` / whole-DTO construction, never typed envelopes /
      `JsonApiDomainDocumentReader`, never `included`, never application mutation).
- [ ] The initial shared PATCH scenario inventory is cataloged as `PatchScenarios` exposing the
      `FixtureCatalog<PatchScenario>` contract through the Phase 2.28 pinned static delegation
      surface (fixed `domainpatch` package, `@NullMarked`) with `JsonApiFixtures.patch()`
      registered and a passing `PatchScenariosCatalogSpec` (unique ids, `byId` round-trip,
      resolvable expectations, additive posture), and is consumed with full-catalog coverage by
      Jackson 3 `*PatchBindingSpec`; that Spec also covers the named adapter-local cases
      (`custom deserializer applies to attribute change`, `patch-custom-linkage-conversion`)
      for Phase 2.23 parity, with no shared exclusion manifest.
- [ ] The canonical `module-docs` checklist passes for jackson3, jackson-common, and
      test-fixtures; Domain mapping “Presence-aware resource-update commands” is **supported**
      for Jackson 3 with the pinned note “Jackson 3 binding supported (Phase 2.15); Jackson 2
      binding remains Phase 2.23”; Phase 1.3 “Command application (PATCH binding)” is retitled
      to “Command application”, marked **out of scope**, and carries the single exact note
      “Applications apply authorized update commands; Jackson 2 binding remains Phase 2.23”;
      the Domain mapping section header is reworded to “Jackson 2 parity — deferred” with Phase
      2.15 added to its supported-phases list; the conformance intro gains a Phase 2.15
      provenance clause; and the `docs/vision.md` jackson3 and jackson-common module lines
      reflect that presence-aware PATCH binding is available as of Phase 2.15 and that the
      jackson-common contract categories include presence-aware update commands.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*PatchBindingSpec'` and `./gradlew clean
      build` pass.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that
      CI must still pass the gate.
