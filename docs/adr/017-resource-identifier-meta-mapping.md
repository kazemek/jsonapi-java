# ADR-017: Flat Mapping for Resource Identifier Meta

**Status:** Accepted
**Date:** 2026-08-26

## Context

ADR-015 maps the complete `meta` object of a resource (`ResourceObject.meta`) and of a relationship
(`Relationship.meta`) onto sibling application-owned properties. JSON:API also allows `meta` on
`ResourceIdentifier` objects used as relationship linkage. That member is not relationship meta:
it belongs to an individual linkage identifier.

For to-one linkage there is at most one identifier and therefore at most one identifier-meta
object. For to-many linkage each identifier may carry its own `meta`. Treating that per-element
member as `Relationship.meta` would conflate two JSON:API concepts. Inventing generic list-element
PATCH addresses so that identifier meta could be patched independently would reopen ADR-014's
atomic `List` / `Set` / array / `Map` replacement boundary.

The wire codec already reads and writes `ResourceIdentifier.meta`. Domain mapping previously dropped
it on built-in linkage conversion. This ADR defines the application-owned mapping contract against
the stabilized Jackson 3 / `jackson-common` surface so Jackson 2 can later reproduce the same
semantics.

## Decision

Add a sibling flat mapping for `ResourceIdentifier.meta`, distinct from ADR-015 resource and
relationship meta.

### Public API

`@JsonApiIdentifierMeta(value)` marks one property per mapped relationship as that relationship's
per-linkage identifier meta. `value()` is required and has no default: it is the target
relationship's resolved JSON:API member (wire) name — the same `value()` convention as
`@JsonApiRelationshipMeta`. The target relationship must be declared by `@JsonApiRelationship` on
the same mapping. At most one identifier-meta property may target a given relationship.

The annotation is not independently patchable. Identifier meta participates in presence-aware PATCH
only as `ResourceIdentifier.meta` on whole-linkage replacement.

### Cardinality

- **To-one:** one Bean / `Map` / `Object`, with at most one `Optional` wrapper — the same whole-meta
  object rule as ADR-015.
- **To-many:** a `List` or array of those object targets, index-aligned with the linkage
  identifiers. `Set` and `Map` are rejected: alignment is by linkage order, not unordered identity
  or map-key addressing. At most one `Optional` may wrap the sequence; sequence elements are not
  themselves `Optional`.

Cardinality is taken from the target relationship's declared type, not from an accidental transport
wrapper.

### Read and write

Read binds identifier `meta` members under the mapped property's logical name through configured
Jackson (full `JavaType`, property-scoped deserializers, naming). To-one absent identifier meta
omits the property. To-many binds an aligned list only when at least one identifier carries meta;
elements without meta are Java `null`. Built-in `ResourceIdentifier` linkage conversion preserves
identifier meta (additional members remain dropped).

Write: when `@JsonApiIdentifierMeta` is present and non-null it is an authoritative overlay on the
constructed identifiers. Omitted identifier-meta, or a configured serializer that emits nothing,
leaves any `ResourceIdentifier.meta` already on the relationship value in place (passthrough).
To-one identifier meta with null linkage, to-many length mismatch, and converted non-object members
fail with stable diagnostics. Relationship linkage, resource identity, and ADR-015 relationship
meta are unchanged.

### PATCH

Identifier meta is **not independently patchable**.

- No `PatchChange` variant is added. Low-level `PatchCommand` skips identifier-meta properties as
  separate changes. Identifier meta rides on `ResourceIdentifier` values inside
  `RelationshipChange` when linkage is supplied, including after coercion to array, `Set`, or
  `Optional`.
- Typed `PatchPresence<T>` DTOs reject `@JsonApiIdentifierMeta` at declaration time with
  `INVALID_PATCH_PROPERTY_TYPE`. Applications that need identifier meta on PATCH supply it on the
  `ResourceIdentifier` (or collection of them) that replaces the relationship.

This is whole-linkage replacement, not element-addressed mutation inside ADR-014 atomic containers.
Supplying linkage still means replacing linkage, including any per-identifier meta carried on those
identifiers. The two PATCH paths are not required to use the same representation shape; they must
not contradict each other: neither path offers “update meta on linkage element N”.

### Diagnostics

New `jackson-common` codes:

- `INVALID_IDENTIFIER_META_TARGET` — declaration, cardinality, shape, length, or converted
  non-array to-many sequence
- `UNRESOLVED_IDENTIFIER_META` — `@JsonApiIdentifierMeta` names an unknown relationship

Reuse `DUPLICATE_ROLE` when two identifier-meta properties target the same relationship. Converted
non-object meta members reuse `INVALID_META_TARGET` (same conversion failure as ADR-015).

Locations are resource-relative `MappingLocation` pointers: `/relationships/{name}/data/meta`
(to-one / declaration), `/relationships/{name}/data/{index}/meta` (to-many element),
`/relationships/{name}/data` (to-many list-level).

### Ownership

Semantic catalogs, diagnostic codes, `PatchChange` documentation, and the annotation live at
Jackson-major-neutral boundaries. Jackson 3 owns introspection, property-scoped conversion, and
adapter-local mechanism tests. `jsonapi-java-jackson-common` remains free of Jackson-major imports.

## Consequences

- Applications can read and write per-linkage identifier meta without modeling it as relationship
  meta and without a hidden element-address protocol.
- Presence-aware PATCH can carry identifier meta only by replacing the relationship's linkage.
  That limitation is explicit in the annotation, diagnostics, docs, and tests.
- Built-in `ResourceIdentifier` conversion now preserves `meta`. Identifiers without meta remain
  indistinguishable from the previous mapping.
- Exhaustive `PatchChange` switches do not gain a new variant. Jackson 2 parity must reproduce
  this contract rather than invent a more granular PATCH model.
