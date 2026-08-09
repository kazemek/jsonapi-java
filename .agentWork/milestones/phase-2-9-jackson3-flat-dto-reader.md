# Phase 2.9 — Jackson 3 Flat DTO Reader

> **Module:** `jsonapi-java-jackson3`
> **Dependencies:** Phases 2.2 and 2.4
> **Status:** Complete

## Goal

Bind a validated `ResourceObject` to an annotated flat record or POJO through Jackson logical
properties, without reading `included` or assembling a domain graph.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — binding runs only after document
  validation; relationship properties receive linkage only; `included` is never injected.
- [ADR-006](../../docs/adr/006-read-boundary.md) — Phase 2.4 remains the sole JSON→document path;
  this milestone starts from core `ResourceObject` values, not raw JSON.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — Jackson visibility, naming, ignores,
  mix-ins, creators, converters, custom deserializers, and configured modules remain authoritative
  for attribute and bean construction.
- [ADR-002](../../docs/adr/002-document-representation.md) — omit Jackson input for absent resource
  members; pass a present null token for explicit JSON null; do not collapse empty to-many linkage
  with null or absent.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — public packages stay `@NullMarked`; binder
  APIs annotate absence-nullable parameters and results with `@Nullable`.
- Phase 2.2 — reuse `MappingDefinitionResolver` / `ResourceMapping` / `MappingDefinitionCache` for
  `@JsonApiResource` type, `@JsonApiId` / conventional `id`, attribute and relationship roles, and
  JSON:API name overrides. Do not add a second annotation scanner.
- Phase 2.2 write asymmetry — write may extract linkage from related `@JsonApiResource` domain
  values (`Person`, `Comment`). Read does **not** fabricate those domain types from linkage. The
  documented bidirectional relationship shapes are `ResourceIdentifier` (and Optional / List / Set /
  array variants). Other relationship Java types require an explicit `RelationshipLinkageMapper`.
- Phase 2.2 `IdentifierConverter` — today write-only (`Object`→`String`). Extend it with a default
  `parse(String)` that returns the wire string unchanged; the binder then applies
  `JsonMapper.convertValue` to the identifier property’s `JavaType`. Custom write converters that
  alter the wire form must override `parse` to invert that form. Keep a single abstract `convert`
  method so existing lambdas/`@FunctionalInterface` call sites remain valid.
- Phase 2.4 — codec/validation failures stay on `JsonApiDocumentReadException`. Binding failures
  throw `JsonApiMappingException` with `MappingDiagnostic` and a resource-relative JSON
  Pointer-like `propertyPath` (`/type`, `/id`, `/lid`, `/attributes/...`,
  `/relationships/<name>/data`…).

## Deliverables

- Add public `JsonApiResourceBinder` and factory methods
  `JsonApiJackson3.resourceBinder(JsonMapper|Builder)` /
  `resourceBinder(..., IdentifierConverter)` /
  `resourceBinder(..., IdentifierConverter, Map<Class<?>, RelationshipLinkageMapper>)`, deriving a
  mapper via `rebuild()` exactly as `resourceMapper` does. Entry points:
  `fromResource(ResourceObject, Class<T>|JavaType)` and
  `fromResources(List<ResourceObject>, Class<T>|JavaType)` for declared homogeneous collections.
- Validate `ResourceObject.type()` against `@JsonApiResource.type()` (`RESOURCE_TYPE_MISMATCH` on
  mismatch). Place only mapped identifier (`id` if present, else `lid` if present, else omit),
  attribute, and relationship values into a synthetic Jackson property map keyed by **Jackson
  logical names**, then construct the bean with one `JsonMapper.convertValue` so
  creators/deserializers apply. Extend `IdentifierConverter` with default `parse` as constrained
  above.
- Bind relationships from linkage only with built-in targets
  `ResourceIdentifier` / `Optional<ResourceIdentifier>` / List·Set·array of `ResourceIdentifier`;
  register `RelationshipLinkageMapper` for any other target `Class`. Never read document `included`.
- Add `MappingDiagnostic` values used only on the read path:
  `RESOURCE_TYPE_MISMATCH`, `IDENTIFIER_CONVERSION_FAILED`, `RELATIONSHIP_CARDINALITY_MISMATCH`,
  `UNSUPPORTED_RELATIONSHIP_TARGET`, `LINKAGE_MAPPING_FAILED`, and `MISSING_CREATOR_INPUT`. Map
  bulk-`convertValue` failures by Jackson cause: missing creator / instantiation input →
  `MISSING_CREATOR_INPUT`; any other coercion, type, or property failure → existing
  `UNSUPPORTED_ATTRIBUTE_VALUE`. Use the best available resource-relative path (property name when
  Jackson exposes it, otherwise `/`). Do not add a separate per-attribute conversion diagnostic.
  Refresh module docs/Javadoc (via `module-docs`) and mark flat resource-to-DTO binding
  **supported** in `docs/conformance.md`.

## Non-goals

- Document-level members, heterogeneous primary data, or `included` binding (Phase 2.10).
- Parsing JSON or calling `JsonApiDocumentReader` inside the binder (callers validate first).
- Auto-stubbing related `@JsonApiResource` domain types from linkage.
- Graph hydration, persistence lookup, identity maps, cycles, or PATCH commands (Phase 2.15).
- Treating `ResourceIdentifier` primary data as a full resource DTO.
- Jackson 2 port (Phase 2.21).

## Implementation boundaries

- Public types in `io.github.kazemek.jsonapi.jackson3`; implementation in
  `io.github.kazemek.jsonapi.jackson3.internal`. Production code imports no `core.internal` and no
  sibling module internals.
- Binder input is already-validated `ResourceObject`. `fromResources` requires every element’s
  `type` to match the single target `@JsonApiResource.type()`; heterogeneous lists are out of
  scope.
- Member presence rules:
  - missing `attributes` object / missing attribute key → omit that logical property;
  - attribute key present with JSON null → put Java `null` in the synthetic map;
  - missing relationship key → omit;
  - relationship present with `data == null` (links/meta-only) → omit the relationship property;
  - `NullLinkage` → Java `null` on to-one / Optional empty; illegal on to-many targets
    (`RELATIONSHIP_CARDINALITY_MISMATCH`);
  - empty `IdentifierCollectionLinkage` → empty collection on to-many; illegal on to-one;
  - `SingleLinkage` / non-empty collection linkage must match to-one vs to-many property shape.
- Unmapped resource attribute or relationship names (not in `ResourceMapping`) are ignored. Only
  mapped logical properties are placed into the synthetic map; caller
  `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` therefore does not see unmapped JSON:API
  fields.
- Shared write diagnostics (`MISSING_RESOURCE_ANNOTATION`, `DUPLICATE_ROLE`, …) still apply when
  resolving the mapping definition. Do not invent a parallel registry.
- Relationship target dispatch (after the presence rules above):
  1. Resolve the target `Class` (unwrap `Optional`; use collection content type for to-many).
  2. **Built-in** `ResourceIdentifier` → apply the cardinality rules above; never call a custom
     mapper.
  3. **Registered** `RelationshipLinkageMapper` for that `Class`:
     - missing relationship key or `data == null` → omit (do not call mapper);
     - enforce cardinality **before** mapper (same illegal shapes as built-in: `NullLinkage` on
       to-many, collection linkage on to-one → `RELATIONSHIP_CARDINALITY_MISMATCH`);
     - to-one `NullLinkage` → Java `null` / empty `Optional` **without** calling mapper;
     - to-many empty `IdentifierCollectionLinkage` → empty collection **without** calling mapper;
     - to-one `SingleLinkage` or to-many non-empty collection → invoke mapper with that
       `RelationshipData` and target `JavaType`; place the returned value in the synthetic map
       (`null` return → null property); mapper exceptions → `LINKAGE_MAPPING_FAILED`.
  4. Else → `UNSUPPORTED_RELATIONSHIP_TARGET`.

## Test strategy

- Spec class: `ResourceBinderSpec` (plus focused companion specs only if the primary file would
  become unreadable). Reuse or add testmodel types under
  `src/test/java/.../testmodel/`; for bidirectional relationship coverage prefer flat DTO shapes
  with `ResourceIdentifier` relationship fields rather than `Article`/`Person` write models.
- Positive: records, mutable POJOs, `@JsonCreator` / immutable creators, inheritance, naming
  strategies, `@JsonProperty`, `@JsonIgnore`, mix-ins, custom deserializers, default and custom
  `IdentifierConverter` (including non-`String` id types via `convertValue`), `id`-only, `lid`-only,
  explicit-null attributes, omitted attributes, homogeneous `fromResources`.
- Relationship matrix:
  - to-one `ResourceIdentifier`: omitted key; links/meta-only `data == null`; `NullLinkage` → null;
    `SingleLinkage` success; collection linkage → `RELATIONSHIP_CARDINALITY_MISMATCH`;
  - to-many `List`/`Set`/array of `ResourceIdentifier`: empty collection success; non-empty
    success; `NullLinkage` → `RELATIONSHIP_CARDINALITY_MISMATCH`; single linkage →
    `RELATIONSHIP_CARDINALITY_MISMATCH`;
  - Optional to-one: `NullLinkage` → empty `Optional`;
  - custom mapper target: success on `SingleLinkage` and non-empty collection; `NullLinkage`/empty
    short-circuit without invoking the mapper; cardinality fails before mapper; mapper throw →
    `LINKAGE_MAPPING_FAILED`.
- Negative: `RESOURCE_TYPE_MISMATCH`; to-one vs to-many cardinality mismatches; unregistered
  `Person`/`Comment`-typed relationship properties → `UNSUPPORTED_RELATIONSHIP_TARGET`; identifier
  parse/`convertValue` failures → `IDENTIFIER_CONVERSION_FAILED`; creator-required property absent
  → `MISSING_CREATOR_INPUT`; attribute value that cannot coerce to the property type (e.g. object
  where a number is required) → `UNSUPPORTED_ATTRIBUTE_VALUE`; custom linkage mapper failures →
  `LINKAGE_MAPPING_FAILED`.
- Compound isolation: bind a primary `ResourceObject` taken from a document that also has
  `included`; mutate or swap `included` content in the fixture and assert the bound DTO and its
  relationship fields are unchanged (binder never receives `included`).

## Acceptance criteria

- [x] `fromResource` / `fromResources` validate `ResourceObject.type()` against
      `@JsonApiResource.type()` (`RESOURCE_TYPE_MISMATCH` on mismatch) and bind `id`/`lid`,
      attributes, and built-in `ResourceIdentifier` relationship shapes as the documented inverse
      of Phase 2.2 for those flat shapes; unregistered non-identifier relationship targets fail
      without reading `included`.
- [x] Mapping definitions come from the Phase 2.2 resolver/cache; caller mapper configuration is
      preserved via `rebuild()`; production code imports neither `core.internal` nor another
      integration module’s internals; public binder APIs satisfy ADR-009 nullness.
- [x] Read-path `MappingDiagnostic` codes and resource-relative paths are asserted for the negative
      cases listed in Test strategy.
- [x] The canonical `module-docs` checklist passes and `docs/conformance.md` marks flat
      resource-to-DTO binding **supported** without claiming typed envelopes or graph hydration.
- [x] `./gradlew :jsonapi-java-jackson3:test --tests '*ResourceBinderSpec'` passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes (passed: gate green and 0 new-code issues via the Issues API,
      confirmed by local analysis and CI PR analysis).
