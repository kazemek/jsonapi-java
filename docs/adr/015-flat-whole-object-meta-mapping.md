# ADR-015: Flat Whole-Object Mapping for Resource-Side Meta

**Status:** Accepted
**Date:** 2026-08-19
**Amendment:** 2026-08-22

## Context

The domain mapping layer flattens JSON:API transport structure into application-owned DTOs. Today
that flattening covers resource identity, attributes, and relationship linkage, but not JSON:API
resource-side `meta` (`ResourceObject.meta` and `Relationship.meta`). Document-level meta is already
document-owned through `DocumentEnvelope` on write and `JsonApiDomainDocument.meta()`/`metaAs(...)`
on read. Resource-side meta is different: applications may treat it as application-relevant
information, persist it, and need to update it.

ADR-014 delivered recursive structured-value PATCH semantics through a location-agnostic
engine (`StructuredValueBinder`) and a neutral `StructuredPatch` payload, and explicitly reserved
the reuse boundary for a follow-up flat meta mapping: "a later structured JSON:API meta mapping can
reuse the same machinery at its own location with a stricter outer-state policy." This ADR delivers
that follow-up for Jackson 3.

## Decision

Add first-class whole-object flat domain mapping for resource-side `meta` on the Jackson 3 adapter:
domain read, domain write, and presence-aware PATCH on both the typed `PatchPresence<T>` DTO path
and the low-level `PatchCommand<T>` path. The primary DX maps the complete `meta` object at a
location to one application-owned property:

```java
record Article(
    @JsonApiId ArticleId id,
    @JsonApiAttribute String title,
    @JsonApiMeta ArticleMeta meta,
    @JsonApiRelationship(name = "author") AuthorId author,
    @JsonApiRelationshipMeta("author") AuthorRelationshipMeta authorMeta) {}
```

`ResourceIdentifier.meta` is deliberately out of scope; it is a separate per-linkage-element
cardinality/ownership problem outside this ADR's scope.

### Public API

- `@JsonApiMeta` — marks one property per resource as the complete resource-side `meta` object.
  Parameterless; at most one per mapping.
- `@JsonApiRelationshipMeta(value)` — marks one property per relationship as the complete `meta`
  object of that relationship. The required `value()` references the target relationship's resolved
  JSON:API member (wire) name. This is an intentional deviation from the optional `name()` convention
  of `@JsonApiAttribute`/`@JsonApiRelationship`: the relationship target is mandatory and no implicit
  name derivation exists. A renamed relationship therefore requires the wire name in `value()`.
- `PatchChange` gains sealed variants `ResourceMetaChange` and `RelationshipMetaChange` so the
  low-level `PatchCommand` exposes explicit resource-meta and relationship-meta requested changes
  without collapsing them into attribute changes.

### Mapping ownership model

`MappingDefinitionCache` and `MappingDefinitionResolver` stay **kind-agnostic**: there is no
read/write-vs-PATCH discriminator in the mapping cache. The resolver resolves common mapping
structure only: annotation role, `MappingProperty` metadata, `JavaType`, relationship-meta target
association, duplicate/conflict checks, and common mapping invariants. Entry-point-specific
wrapper-chain validation belongs to the consuming binder/writer:

- **Normal domain read/write and low-level `PatchCommand` domain mappings:** valid whole-meta
  declarations are Bean / `Map` / `Object` with at most one `Optional<T>` wrapper;
  `PatchPresence<?>` is invalid in these models and nested `Optional<Optional<...>>` is invalid.
- **Typed PATCH DTO mappings:** the meta property must be declared exactly `PatchPresence<T>`
  (consistent with the ADR-013 declaration contract); unwrap exactly one `PatchPresence`, then at
   most one `Optional`, then the effective target must be Bean / `Map` / `Object`. All ADR-014
  wrapper-customization restrictions apply unchanged.

### Whole-meta ownership and uniqueness

At most one `@JsonApiMeta` property is allowed per mapping, and at most one
`@JsonApiRelationshipMeta` property may target a given mapped relationship. Each meta mapping owns
the complete meta object of its location; there is no merge or last-wins semantics. Duplicates are
rejected at mapping resolution with a stable diagnostic (`DUPLICATE_ROLE` or an additive dedicated
code).

### Read and write

- Write converts the whole-meta property value through the mapped property's fully contextualized
  Jackson property serializer, then requires the result to be a `Map` before constructing core
  `Meta`. Direct property serializers, mix-ins, contextual/content serializers, type serializers,
  and mapper modules remain configured-Jackson authority. When no mapped property writer can be
  resolved, the existing `convertValue(value, Object.class)` type/module fallback applies.
  Scalar/array/non-object runtime results fail with a stable `JsonApiMappingException` + meta
  diagnostic. Invalid meta member-name or non-open-value failures from `Meta.of(...)` are
  translated to the same stable meta diagnostic, never leaked as raw core validation exceptions.
- Read places `resource.meta().members()` (or a relationship's meta members) under the mapped
  meta property's logical name in the synthetic property map so the single `convertValue`
  construction binds the application-owned type.
- Absent meta omits the property (constructor default / null); a supplied empty object binds an
  empty value (for a bean with defaults or a `Map`).
- **Write-side Jackson authority:** Mapped attributes and whole-meta properties
  use the same location-neutral property-scoped serialization authority. The adapter supplies the
  already-read property value, so mapped accessors are not read twice; `Optional` omission and
  unwrapping remain adapter policy, and whole-meta object-shape validation remains mandatory after
  serialization. The conversion buffer retains `convertValue`'s root-unwrapped semantics. This
  supersedes this ADR's original type-level-only write statement and aligns whole-meta writes with
  the mapped-attribute write semantics on the Jackson 3 adapter.

### PATCH semantics

Whole-meta outer presence is location-constrained: JSON:API `meta` is object-valued, so outer
`meta: null` is wire-invalid and is already rejected at the token-driven reader (START_OBJECT
requirement); it can never become an outer `Present(null)`. Outer presence is therefore binary:
omitted or a supplied object.

- **Typed path (strict):** `@JsonApiMeta PatchPresence<MetaPatchType>` and
  `@JsonApiRelationshipMeta("author") PatchPresence<AuthorMetaPatch>` bind through the recursive
  structured-value engine defined by ADR-014. Recursive presence-aware nested shapes preserve nested omitted / explicit-null /
  supplied-value state. `{}` on a recursively patchable bean target binds a present shape with every
  member omitted; `{}` on an atomic map-like target is an atomic empty-map replacement. Supplied
  meta without a matching meta member is rejected with an `UNKNOWN_PATCH_MEMBER`-style diagnostic
  (the typed path is strict and never silently drops supplied meta).
- **Low-level path (permissive):** supplied resource/relationship meta binds to
  `ResourceMetaChange`/`RelationshipMetaChange`. A traversable-bean target with an object wire value
  binds a `StructuredPatch` of supplied-only nested changes; a `Map`/`Object` target stays an atomic
  converted value. Supplied meta without a mapped meta property is skipped, preserving the existing
  lossless change-list philosophy. Relationship meta participates only when the relationship
  carries `data` (on both `readValue` and `fromDocument`); there is no hidden meta-only relationship
  PATCH wire form, and the supplied `data` retains its normal linkage replacement semantics.
- **Diagnostic pointers:** atomic meta conversion failures report `/meta` and
  `/relationships/<name>/meta`, never the attribute-oriented `/<logicalName>` pointer.
- **Change ordering:** `PatchCommand.changes()` is deterministic: resource meta first, then
  attributes (encounter order), then relationships with their linkage change and, when present,
  the relationship's meta change immediately after its `RelationshipChange`.

### Amendment of ADR-014's "no PatchChange sealed-hierarchy change"

ADR-014 stated "There is no `PatchChange` sealed-hierarchy change; the new public payload types are
additive," and rejected a structured-value-specific variant because it would couple the hierarchy to
one representation. This ADR amends/supersedes that statement: the new variants are
**location-specific**, not representation-specific — resource meta and relationship meta are
distinct JSON:API change locations, and `jsonapiName = "meta"` alone cannot discriminate resource
meta from a legal attribute named `meta` (an attribute member named `meta` is wire-legal under
`MemberNames`). The variants carry a `StructuredPatch` only where ADR-014 recursion applies on the
low-level path; the structured-value payload itself is unchanged. Sealed additions have
source/binary compatibility implications for consumers switching exhaustively over `PatchChange`;
this is documented as part of the public API change.

### Sparse fieldsets

`fields[TYPE]` selects attributes and relationships only. Meta is never a field name and never
participates in fieldset validation. Resource meta is emitted unconditionally, even for an empty
(attribute/relationship-less) fieldset selection; relationship meta rides its owning relationship
and is absent when that relationship is fieldset-excluded. Existing wording that calls an empty
fieldset "identity-only" is clarified: it means no attributes/relationships are
emitted; non-field resource members such as mapped resource meta remain independent.

### Relationship asymmetry

Normal domain read may bind meta from a valid meta-only `Relationship` representation (core
supports `Relationship.metaOnly`). UPDATE/PATCH relationship meta participates only when `data` is
present; the validator requires `data` for primary relationships in update requests, and the binder
never synthesizes a meta-only relationship change. This asymmetry is intentional and is covered by
tests.

## Consequences

- Applications can read, write, and patch resource/relationship meta through one application-owned
  property per location, without envelope wrappers and without a second recursion engine.
- The Jackson 3 adapter honors the same Jackson authority, diagnostics, nullness, generic `JavaType`
  preservation, and container boundaries as existing mapping; the neutral contracts in
  `jsonapi-java-jackson-common` stay Jackson-import-free so Jackson 2 parity can follow the same
  semantics.
- `PatchChange` gains two sealed variants; exhaustive consumer switches must add cases (or a default
  branch). Documented compatibility consideration.
- Document-level meta remains document-owned; no resource annotation ambiguously means document
  meta.
- `ResourceIdentifier.meta` remains unmapped; that separate problem stays out of scope for this
  ADR and its successors.
