# Phase 2.8 — Jackson 3 Sparse Fieldsets

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 2.3  
> **Status:** Complete

## Goal

Apply caller-supplied sparse fieldsets on Jackson 3 write mapping so primary and included
resources emit only selected attributes and relationships, while identity, bounded inclusion, and
scoped full-linkage validation stay intact.

## Research and constraints

- [JSON:API sparse fieldsets](https://jsonapi.org/format/1.1/#fetching-sparse-fieldsets) — field
  selection is per resource type; a present empty `fields[TYPE]` means no fields; an absent type
  entry lets the server choose (this library: unrestricted mapped fields). Field names are
  attribute and relationship member names, never `type` / `id` / `lid`.
- [JSON:API compound documents](https://jsonapi.org/format/1.1/#document-compound-documents) — the
  only full-linkage exception is when relationship fields that would otherwise carry linkage are
  excluded by sparse fieldsets. Core already implements that switch as
  `ValidationContext.withSparseFieldsetException(boolean)` in
  `JsonApiDocumentValidator.validateCompoundDocument`.
- [`docs/vision.md`](../../docs/vision.md) — supported fields remain explicit application policy;
  the library applies requested fieldsets and allow-lists but does not authorize callers or parse
  HTTP `fields[TYPE]`.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — sparse fieldsets share the
  explicit serialization context with inclusion (extend `CompoundSerializationContext`; do not add
  a second parallel context type).
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) / [ADR-010](../../docs/adr/010-architectural-tests.md)
  — new public types are `@NullMarked` with accurate `@Nullable`; no new production package; the
  existing Jackson 3 ArchUnit allowlist stays unchanged (no `core.internal`, no Jackson 2).
- Phase 2.2 — `ResourceMapping` / `MappingProperty.jsonapiName()` remain the only field-name
  authority; fieldsets must not rescan domain types or invent alternate names.
- Phase 2.3 — `IncludePolicy` continues to gate inclusion traversal only; `DomainResourceWriter.toResource`
  today always emits full mapped attributes/relationships. Fieldsets introduce selective emission
  on that path. Include-path pre-validation (depth → mapping → policy) is unchanged and is **not**
  failed merely because a path segment is fieldset-excluded.
- Phase 2.20 ports this contract to Jackson 2; keep conceptual parity (fieldsets on the compound
  context, selective emission, traversal vs linkage split, `MappedDocument`-equivalent validation
  coordination).
- Phase 3.1 owns `fields[TYPE]` query grammar; Phase 3.3 consumes the immutable context and
  `MappedDocument` validation hint when writing responses.

## Serialization context

Extend the existing immutable `CompoundSerializationContext` (do not replace it) with:

- `Map<String, List<String>> fieldsets` — keys are JSON:API resource type strings; values are
  ordered lists of allowed field names (attribute and relationship JSON:API names). Never null:
  use an empty map for “no fieldset request” (unrestricted emission, matching empty include paths
  for “no inclusion request”). List values preserve encounter order and collapse duplicate names
  to the first occurrence (same retention rule Phase 3.1 will use for `fields[TYPE]`).
- `FieldPolicy fieldPolicy` — application allow-list over `(resourceType, fieldName)` pairs, with
  the same mode shape as `IncludePolicy`: `allowAll()` / `denyAll()` / `allowing(Set<FieldAllowance>)`.
  Default is `FieldPolicy.allowAll()`. `FieldAllowance` is a public record of
  `resourceType` + `fieldName` (JSON:API names, not Java names).

Growing the public record with `fieldsets` and `fieldPolicy` changes the canonical constructor;
that API break is intentional for the pre-release (`0.1.0-SNAPSHOT`) line. Do **not** add a
legacy four-argument compatibility overload.

Constructor and every `withX()` method defensively copy the fieldset map: reject null map, null
keys, null value lists, and null field-name elements; copy each value through a null-rejecting
`LinkedHashSet` then `List.copyOf` so duplicates collapse while encounter order is preserved
(do **not** rely on bare `Set.copyOf` iteration order for diagnostics); store the outer map as
`Map.copyOf(...)` so `fieldsets()` cannot expose mutable state. `FieldPolicy.allowing` rejects a
null set and null elements and retains an unmodifiable defensive copy via `Set.copyOf`, matching
`IncludePolicy.allowing`. Every `withX()` reconstructs from those immutable copies.
`withFieldsets` / `withFieldPolicy` copy methods required.

**Per-type semantics when emitting a resource of JSON:API type `T`:**

| Fieldset map entry for `T` | Emission |
|---|---|
| Key absent | All mapped attributes and relationships (Phase 2.2 behavior) |
| Key present, empty list | No attributes, no relationships; keep `type` and `id`/`lid` |
| Key present, non-empty list | Emit only mapped members whose `jsonapiName()` is in the list |

`type`, `id`, and `lid` are never fieldset-selectable and are never omitted by a fieldset.

**Unknown names:** when emitting a resource whose type has a present fieldset entry, every name in
that list must equal a mapped attribute or relationship `jsonapiName()` on that resource’s
`ResourceMapping`. Unknown → `MappingDiagnostic.INVALID_FIELDSET_FIELD` with `resourceClass` equal
to the domain resource class being emitted (same shape as `INVALID_INCLUDE_PATH` /
`DENIED_RELATIONSHIP_INCLUDE`) and `propertyPath` equal to the offending JSON:API field name.

**FieldPolicy:** for every field name in a present fieldset entry,
`fieldPolicy.allows(resourceType, fieldName)` must be true; otherwise
`MappingDiagnostic.DENIED_FIELDSET_FIELD` with the same `resourceClass` (domain class) /
`propertyPath` (JSON:API field name) shape.
Under `allowAll()`, policy never denies. Under `denyAll()`, any present fieldset entry that
contains at least one field name fails. Under `allowing(...)`, each requested field name must
appear in the allowance set for that owner resource type. A present empty list still means
identity-only emission and does not consult per-field allows.

**Per-name validation order (before any selective property read for that resource):** iterate the
fieldset names in the stored list’s encounter order. For each name: (1) mapping membership →
`INVALID_FIELDSET_FIELD` if the name is not a mapped attribute or relationship `jsonapiName()`;
(2) then `fieldPolicy.allows` → `DENIED_FIELDSET_FIELD` if denied. First failing check wins; stop
before selective reads. Under `denyAll()`, step (2) fails on the first name that survived step (1),
with `propertyPath` equal to that JSON:API field name (same shape as a missing `FieldAllowance`).
Add a multi-failure test that asserts the winning diagnostic when a name is both unmapped and
would be policy-denied (`INVALID_FIELDSET_FIELD` wins).

**Unused fieldset type keys:** entries whose resource type is never materialized as primary or
included data in this mapping are ignored (not an error). Write mapping has no type registry;
strict query-type validation belongs to Phase 3.1 / application schema checks.

## Fieldset vs inclusion behavior

- **Linkage emission (selective `toResource`):** skip attribute and relationship properties whose
  names are excluded by the applied fieldset. Do not read excluded attribute getters. Do not read
  excluded relationship getters **for linkage construction**.
- **Inclusion traversal:** Phase 2.3 path membership + `IncludePolicy` still decide whether a
  relationship is followed to populate `included`. A relationship that is fieldset-excluded on the
  owner resource may still be read for traversal when it is a segment on a validated, policy-allowed
  include path. This is the JSON:API case that requires the full-linkage exception (for example
  `include=author` with `fields[articles]=title`).
- **Composition:** include-path pre-validation does not consult fieldsets. Fieldsets never enlarge
  include paths. Off-path relationships remain unread for traversal regardless of fieldsets.
- **Access-counting fixtures:** assert (1) excluded attributes are not read; (2) excluded
  relationships are not read for linkage; (3) fieldset-excluded relationships **are** read when
  required for inclusion traversal; (4) off-path relationships are never read for traversal.

## Full-linkage exception coordination

`CompoundSerializationContext` remains mapping-only (no embedded `ValidationContext`), matching
Phase 2.3. Aggregate validation stays on `JsonApiDocumentWriter`.

During selective emission, each selective write reports whether any relationship member was omitted
from that `ResourceObject` because a present fieldset for that resource’s type did not include that
relationship name. Fold those bits with OR across the mapping call (primary selective writes and
included selective writes). Expose the result via a new public record:

```java
public record MappedDocument(
    JsonApiDocument document, boolean sparseFieldsetException) {
  public ValidationContext applyTo(ValidationContext base) { /* ... */ }
}
```

`applyTo` returns `base.withSparseFieldsetException(true)` only when `sparseFieldsetException` is
true; otherwise returns `base` unchanged. The flag is true only when at least one relationship was
actually omitted by an applied fieldset during that mapping call (not merely because the fieldset
map is non-empty, and not for attribute-only omissions).

**Entry-point split (avoid discarding the hint):**

- Fieldsets are applied only by the `MappedDocument` overloads:
  - `toMappedDocument(Object, @Nullable DocumentEnvelope, CompoundSerializationContext)`
  - `toMappedResourceCollection(Iterable<?>, @Nullable DocumentEnvelope, CompoundSerializationContext)`
- Existing three-argument `toDocument` / `toResourceCollection` overloads keep Phase 2.3 inclusion
  behavior and **reject** a context whose fieldset map is non-empty with
  `MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT` (stable code; `resourceClass` / `propertyPath`
  may be null). Callers that need fieldsets must use the `MappedDocument` overloads and write with
  `mapped.applyTo(...)` passed into the existing `JsonApiJackson3.writer(JsonMapper, ValidationContext)`
  factory. Do **not** add a `writer(..., MappedDocument)` convenience that binds a document-specific
  `sparseFieldsetException` onto a reusable writer (that writer accepts any `JsonApiDocument`).

Context-free overloads and bare `toResource(Object)` remain full emission (no fieldset). Fieldset-only
use (empty include-path list) goes through the `MappedDocument` overloads.

Duplicate-identity, local-identifier, extension/profile, and all other aggregate rules stay active
when the exception is enabled; only the full-linkage walk is skipped (existing core behavior).

## Deliverables

- Extend `CompoundSerializationContext` with defensively copied `fieldsets` and `FieldPolicy`; add
  public `FieldPolicy`, `FieldAllowance`, and `MappedDocument` (`document` +
  `sparseFieldsetException` + `applyTo(ValidationContext)`).
- Implement selective attribute/relationship emission in `DomainResourceWriter` (fieldset-aware
  selective write used by primary mapping and `CompoundInclusionEngine`); apply fieldsets only on
  `toMappedDocument` / `toMappedResourceCollection`; reject non-empty fieldsets on existing
  three-argument `toDocument` / `toResourceCollection` with `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`;
  keep inclusion traversal able to follow fieldset-excluded relationships on validated include
  paths; fold selective omission bits with OR into `MappedDocument.sparseFieldsetException`.
- Add `MappingDiagnostic.INVALID_FIELDSET_FIELD`, `DENIED_FIELDSET_FIELD`, and
  `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`; validate present fieldset entries against `ResourceMapping`
  JSON:API names and `FieldPolicy` before selective attribute/relationship reads.
- Add Spock `SparseFieldsetSpec` (and access-counting test models as needed) covering the cases in
  Test strategy; refresh `jsonapi-java-jackson3/README.md`, public `package-info.java`, entry-point
  Javadoc, and `docs/conformance.md` (sparse fieldsets on write → **supported** with the exact
  policy below).
- Run the canonical `module-docs` checklist for the public surface changes (context members,
  `MappedDocument`, fieldset/inclusion composition, validation coordination).

## Non-goals

- Parsing HTTP `fields[TYPE]` parameters (Phase 3.1) or authorizing fields beyond the explicit
  `FieldPolicy` allow-list.
- Implicit default fieldsets, persistence fetch plans, JPA/Hibernate initialization, or changing
  Phase 2.3 include-path / depth / count / dedup rules.
- Fieldset filtering of bare `toResource(Object)`, context-free mapper overloads, resource `links` /
  `meta`, or read-side DTO / envelope / PATCH APIs.
- Jackson 2 sparse fieldsets (Phase 2.20); canonical `fixtures/jsonapi-1.1/` codec fixtures (fieldsets
  are a mapping policy, not a document-codec wire form).
- Two-argument context overloads that would reintroduce `DocumentEnvelope` ambiguity.

## Implementation boundaries

- Public types stay in `io.github.kazemek.jsonapi.jackson3`; selective emission helpers stay in
  `io.github.kazemek.jsonapi.jackson3.internal`. No new production package; ArchUnit allowlist
  unchanged.
- Prefer a **public** selective write helper on `DomainResourceWriter` (same visibility pattern as
  existing `toResource(Object)`) that returns both the `ResourceObject` and whether any relationship
  was omitted by the applied fieldset—for example an internal
  `record SelectiveResource(ResourceObject resource, boolean relationshipOmittedByFieldset)` with
  `toResource(Object, @Nullable List<String> fields)` (null = unrestricted; empty = identity only;
  non-empty = allow-list by `jsonapiName`) plus a helper that resolves the list from
  `CompoundSerializationContext` for the resource’s mapped type. Do not use a `ResourceObject`-only
  return that drops the omission bit. Package-private visibility is insufficient:
  `JsonApiResourceMapper` lives in the parent package and must invoke the helper for primary
  selective emission.
- Fold omission with OR via return values only: primary selective writes contribute their bits;
  `CompoundInclusionEngine` returns included resources plus an aggregated omission bit from the
  same selective helper (for example an internal
  `record IncludedResourcesResult(@Nullable List<ResourceObject> included, boolean relationshipOmittedByFieldset)`).
  Do **not** pass a mutable shared accumulator into the engine or store omission state on the mapper
  or engine instance (same concurrency rule as Phase 2.3 traversal state).
- `CompoundInclusionEngine` must build included `ResourceObject`s through that selective path, not
  the unrestricted `toResource(Object)`.
- Primary mapping in `MappedDocument` overloads must use selective emission for primary resources
  (today three-argument paths call unrestricted `toResource` before `collectIncluded`). Existing
  three-argument `toDocument` / `toResourceCollection` remain unrestricted emission and reject
  non-empty fieldset maps before any mapping work.
- Fieldset name checks run against the resolved `ResourceMapping` before any selective attribute or
  relationship getter for that resource; identifier extraction remains unrestricted and is not a
  fieldset field. Do not reflect independently of `MappingDefinitionCache`.
- Files to add: `FieldPolicy.java`, `FieldAllowance.java`, `MappedDocument.java`,
  `SparseFieldsetSpec.groovy`, plus any small access-counting test models under
  `src/test/java/.../testmodel/`. Files to edit: `CompoundSerializationContext.java`,
  `JsonApiResourceMapper.java`, `DomainResourceWriter.java`, `CompoundInclusionEngine.java`,
  `MappingDiagnostic.java`, `jsonapi-java-jackson3/README.md`, `package-info.java`, entry-point
  Javadoc, `docs/conformance.md`.

## Test strategy

- Unrestricted (empty fieldset map): identical attributes/relationships to Phase 2.2/2.3 for the
  same inputs via `MappedDocument` overloads; three-argument `toDocument` /
  `toResourceCollection` with an empty fieldset map remain Phase 2.3-equivalent.
- Present empty list for a type: identity-only primary (and included, when that type appears).
- Present empty list with `FieldPolicy.denyAll()`: identity-only emission succeeds and does not raise
  `DENIED_FIELDSET_FIELD` (empty lists skip per-field policy checks).
- **Entry-point split:** three-argument `toDocument` / `toResourceCollection` with a non-empty
  fieldset map fail with `FIELDSETS_REQUIRE_MAPPED_DOCUMENT` before property access; at least one
  positive fieldset case (identity-only or attribute-only, empty include-path list) is asserted
  through `toMappedDocument` / `toMappedResourceCollection`.
- Attribute-only and relationship-only fieldsets on primary resources; renamed Jackson /
  `@JsonApiAttribute` / `@JsonApiRelationship` names use final JSON:API names.
- Per-type fieldsets: restricting `articles` must not strip fields from included `people` unless
  `people` also has an entry.
- Inclusion + fieldset: `include=author` with `fields[articles]=title` omits `author` linkage on
  the article, still includes the author resource, and yields
  `MappedDocument.sparseFieldsetException == true`; writing with `mapped.applyTo(defaults)` passes
  aggregate validation; writing with defaults and `false` fails `FULL_LINKAGE_VIOLATION`.
- Nested case: `include=comments.author` with `fields[comments]=body` omits `author` on included
  comments, still includes authors, sets the exception flag.
- Attribute-only omission with includes that remain fully linked: flag stays false.
- Access-counting: excluded attributes unread; excluded relationships unread for linkage;
  fieldset-excluded include-path relationships read for traversal only; off-path unread.
- Unknown field name → `INVALID_FIELDSET_FIELD`; `FieldPolicy.denyAll()` / missing allowance →
  `DENIED_FIELDSET_FIELD` with `resourceClass` equal to the domain resource class and
  `propertyPath` equal to the first failing JSON:API field name in stored list encounter order;
  multi-failure case where an unmapped name would also be policy-denied asserts
  `INVALID_FIELDSET_FIELD` wins.
- Concurrent isolation: two concurrent fieldset mappings on a shared mapper (at least one omitting
  a relationship) produce isolated documents and independent
  `MappedDocument.sparseFieldsetException` values.
- Defensive-copy isolation: mutating a caller-supplied fieldset map/list or `FieldAllowance` set
  after context construction (and mutating collections returned by accessors, if any mutable view
  were exposed) must not change an existing context; duplicate field names collapse to first
  occurrence while preserved encounter order remains stable.
- Identity preservation (`type` + `id`/`lid`) under every fieldset shape; deterministic member
  iteration order matches mapping definition order among surviving fields.
- Existing `CompoundSerializationSpec` scenarios remain green when the fieldset map is empty.

## Acceptance criteria

- [x] Fieldsets use final JSON:API names on primary and included resources, always preserve `type`
      and `id`/`lid`, treat absent type keys as unrestricted and present empty lists as identity-only,
      and are applied only by the `MappedDocument` overloads; three-argument `toDocument` /
      `toResourceCollection` reject non-empty fieldset maps with
      `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`; defensive-copy isolation holds for fieldset/`FieldAllowance`
      inputs; concurrent mappings on a shared mapper yield isolated documents and independent
      `MappedDocument.sparseFieldsetException` values.
- [x] Inclusion and fieldsets compose as specified: linkage omits excluded relationships; inclusion
      traversal may still follow fieldset-excluded relationships on validated include paths;
      access-counting fixtures prove the read split; `MappedDocument.sparseFieldsetException` is true
      only after an actual relationship omission by fieldset; `mapped.applyTo` skips only full
      linkage while other aggregate validation remains active.
- [x] Unknown or disallowed fieldset names fail with `INVALID_FIELDSET_FIELD` or
      `DENIED_FIELDSET_FIELD` using mapping-then-policy precedence, with `resourceClass` equal to the
      domain resource class and `propertyPath` equal to the first failing JSON:API field name in
      stored list encounter order.
- [x] The canonical `module-docs` checklist passes; `docs/conformance.md` marks sparse fieldsets on
      write **supported** with query parsing and field authorization remaining application/adapter
      responsibilities; new public types are `@NullMarked` with accurate `@Nullable` (ADR-009); the
      ArchUnit allowlist is unchanged.
- [x] `./gradlew :jsonapi-java-jackson3:test --tests '*SparseFieldsetSpec'` passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
