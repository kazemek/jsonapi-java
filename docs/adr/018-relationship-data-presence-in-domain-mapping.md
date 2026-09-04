# ADR-018: Ordinary Domain Relationships Remain Linkage-Oriented

**Status:** Accepted
**Date:** 2026-09-03

## Context

The core document model represents relationship data presence exactly. `Relationship.data` is a
nullable `RelationshipData`, and the sealed `RelationshipData` variants distinguish three present
linkage states:

| Wire state | Core representation |
|------------|---------------------|
| `data` absent (link-only or meta-only relationship) | Java `null` on `Relationship.data` |
| `"data": null` (explicit null to-one linkage) | `RelationshipData.NullLinkage` |
| `"data": { ... }` (single linkage) | `RelationshipData.SingleLinkage` |
| `"data": [ ... ]` (collection linkage; `[]` is present-empty, not absent) | `RelationshipData.IdentifierCollectionLinkage` |

`Relationship.links` and `Relationship.meta` are independent members. Core construction and
validation accept links-only relationships that carry a non-pagination link and meta-only
relationships, and the document codec preserves all four data states plus links, meta, and
additional members in both directions. The core model is complete; nothing in this area is missing.

Ordinary domain mapping is narrower. On writes, a selected `@JsonApiRelationship` property always
materializes a `data` member:

- null to-one property (or `Optional.empty()`) → `NullLinkage` → `"data": null`;
- non-null to-one property → `SingleLinkage` (domain target, `ResourceIdentifier`, `RelationshipData`,
  or `RelationshipLinkage<T, M>` with identifier-meta overlay);
- null/empty to-many property → `IdentifierCollectionLinkage.empty()` → `"data": []`;
- non-empty to-many property → collection linkage;
- custom `RelationshipLinkageMapper` results → linkage as returned.

No supported ordinary write path produces a relationship object without `data`. On reads, flat DTO
binding is linkage-oriented by contract ([ADR-011](011-flat-dto-read-binding.md)): it binds explicit
null, single, or collection linkage only. A relationship that is present on the wire but carries no
`data` binds no linkage — the mapped relationship property is left unbound under configured Jackson
missing-property semantics — while that relationship's `meta` still binds through
`@JsonApiRelationshipMeta`. Presence-aware PATCH treats a supplied data-less relationship as
Omitted, never as a null replacement ([ADR-012](012-resource-patch-binding.md),
[ADR-013](013-direct-typed-patch-dto-binding.md)).

The gap is therefore representational, not structural: a valid JSON:API relationship whose `data`
member is absent cannot be produced through ordinary domain mapping. The motivating server case is
exposing a relationship `related` link without loading or emitting linkage — for example
`relationships.comments.links.related = "/articles/1/comments"` with no `data` member. That is a
legitimate, useful representation, and it is available today only through explicit core `Relationship`
/ document construction or by reading a full document model rather than an ordinary domain DTO.

This decision is the last relationship-state checkpoint before the major-neutral Level-1 application
contract freezes the ordinary-vs-advanced boundary, so it must state an unambiguous 1.0 contract.

## Problem

The core wire model can express more relationship states than ordinary domain mapping. The library
must decide whether the missing state — `data` absence — requires a new semantic abstraction in the
domain-mapping API before 1.0, or whether it intentionally remains an advanced, document-level
concern.

The decision must not weaken three established boundaries:

- ordinary domain DTOs stay simple (`@JsonApiRelationship Author author;` — no wrapper by default);
- decoration stays additive: decoration enriches already-mapped output and never creates, deletes,
  or replaces linkage;
- relationship links, relationship meta, inclusion policy, sparse fieldsets, PATCH semantics,
  persistence/loading state, and HTTP operation policy each remain separate concerns that a
  relationship *data-presence* value must not absorb.

## Considered alternatives

### Option A — keep `data` absence outside ordinary domain mapping for 1.0 (chosen)

Ordinary mapped relationship properties are linkage-oriented: when a selected ordinary domain
relationship is emitted, the resulting relationship contains a `data` member. Links-only and
meta-only relationships remain first-class in the core document model and in document-level APIs,
and are not reachable through ordinary domain mapping in either direction.

Benefits:

- Ordinary DTOs stay plain Java: no state wrapper, no four-state semantics leaking through every
  relationship path, no interaction analysis with `Optional`, collections, `RelationshipLinkage`,
  and `PatchPresence` (which would become a fourth wrapper type around relationship values).
- The smallest possible public API surface before 1.0. The write contract above is already
  implemented, tested, and internally consistent; formalizing it changes no behavior.
- The advanced escape hatch already exists and is complete: core `Relationship.linkOnly` /
  `metaOnly` construction, the core document model, and the document codec preserve data-absence
  precisely, including through reads. No information is lost to the library; ordinary linkage
  properties simply carry no data-presence marker.
- The Level-1 application contract stays linkage-oriented without a facade-level relationship
  envelope; advanced document APIs cover the uncommon forms.

Costs, evaluated rather than waved away:

- Producing a links-only relationship requires core/document construction rather than a DTO
  annotation. Acceptable for 1.0: the case is real but uncommon, the escape hatch is small
  (`Relationship.linkOnly` plus a document), and a future additive capability can revisit it without
  breaking the ordinary contract.
- Ordinary linkage properties carry no data-presence marker on reads. Whether a bound bean can
  distinguish absent `data` from explicit `"data": null` depends on the DTO shape and configured
  Jackson: defaulted shapes keep their initializers for absent data but bind explicit null, while
  shapes without per-property defaults bind both alike. Applications that need the distinction
  regardless of shape read the relationship object, not just the linkage.
- A decorator cannot fabricate a links-only relationship from nothing. This is intentional:
  decoration's authority is enrichment of already-mapped members, and letting it synthesize
  relationships would bypass mapping, fieldset validation, and inclusion bookkeeping.

### Option B — introduce a minimal opt-in relationship-data state abstraction

A conceptual `RelationshipDataState<T>` with four states (absent, null, single, collection) was
evaluated as the smallest defensible abstraction. Rejected for 1.0:

- The fourth state is the only one ordinary Java cannot already express, and expressing it requires
  a new wrapper on every relationship that might want it. `null`, `Optional`, and collections
  already express null, single, and collection safely and are tested, documented contracts.
- The wrapper cannot stay small in practice: it must define write serialization mechanics
  (suppressing `data` while still emitting the relationship), read binding for a property that
  "represents absence", cardinality interactions, and its position relative to `RelationshipLinkage`
  (identifier meta), `PatchPresence` (PATCH presence), and `@JsonApiRelationshipMeta` (meta).
  Each answer adds surface; avoiding a mega-wrapper requires reinventing the ordinary pipeline
  beside it.
- It burdens the common case to serve the uncommon case: almost every relationship mapping path
  (writes, reads, PATCH, inclusion, fieldsets, diagnostics, Jackson 2 parity) gains a fifth shape to
  define and test before 1.0, for a representation that core APIs already cover completely.
- The same capability can be added later, after 1.0, without breaking the linkage-oriented ordinary
  contract, because Option A changes no existing behavior.

### Option C — let decoration create links-only relationships

Extending `RelationshipDecoration` so a decorator could contribute a relationship object that
normal mapping did not produce (a data-less relationship with links) was considered as a
"no-new-type" route. Rejected: it changes decoration's contract from additive enrichment to
fabrication, makes decoration a second mapping authority that competes with mapping and fieldsets,
and conflates response shaping with representation capability. Decoration semantics are not
modified by this ADR.

## Decision

**Ordinary domain mapping remains linkage-oriented. For 1.0, a selected ordinary
`@JsonApiRelationship` property always represents a relationship whose `data` member is present —
explicit null, single, or collection linkage. Relationship `data` absence (links-only and
meta-only relationships) intentionally remains an advanced, document-level concern.**

The 1.0 contract, explicitly:

- **Write:** every selected mapped relationship emits `data` (never omitted). A null/`Optional.empty()`
  to-one emits `"data": null`; an empty to-many emits `"data": []`. Absence of the whole
  relationship from the resource is achieved by fieldset exclusion, not by a Java value.
- **Read:** a wire relationship without `data` binds no linkage. The mapped relationship property
  is left unbound, and the resulting Java value follows configured Jackson missing-property
  semantics: field initializers, creator defaults, and null-handling customizations remain in
  effect. Explicit `"data": null` is separately an explicit null binding through the same
  configured-Jackson construction, so defaulted DTO shapes can distinguish the two states; shapes
  without per-property defaults (for example plain records) happen to bind both alike.
  `"data": null` on a to-many property is invalid linkage cardinality and fails with
  `RELATIONSHIP_CARDINALITY_MISMATCH`, while `"data": []` binds an empty collection. None of this
  is a rejection or a wire-state collapse: the document model retains every distinction; the
  ordinary linkage property simply carries no data-presence marker.
- **Relationship meta:** a data-less relationship's `meta` still binds through
  `@JsonApiRelationshipMeta` on reads and is still emitted on writes. Meta ownership is unchanged;
  the data-presence contract neither absorbs nor constrains it.
- **Links-only and meta-only relationships** remain fully supported through core `Relationship`
  construction, the core document model, and the document codec (both directions). They are
  out of scope for ordinary domain properties, for decorators, and for the Level-1 relationship
  facet.
- **Direction neutrality:** the contract is symmetric for client and server code. It encodes no
  HTTP-method or operation semantics; a data-less relationship is a valid representation the
  general layer neither produces through ordinary mapping nor rejects on read.

## Rationale

- **Correct semantics with zero new surface.** The core model already distinguishes all four data
  states plus links and meta, and preserves them end to end. Option A formalizes existing, tested
  behavior instead of adding an abstraction whose only new expressiveness is the one state the
  document layer already owns.
- **Simple ordinary DTOs.** The default relationship property stays a bare target or collection.
  Four-state semantics would otherwise touch every relationship property declaration, every mapping
  diagnostic, and every adapter's tests, before 1.0, to serve representations most applications
  emit rarely.
- **Clean ownership.** Linkage presence stays owned by mapping (ordinary) and document construction
  (advanced). Links stay owned by core members and decoration. Meta stays owned by
  `@JsonApiRelationshipMeta`. Inclusion stays owned by representation selection and policy. PATCH
  stays owned by the presence-aware update contracts. No component gains a second authority.
- **Jackson 2 parity.** There is nothing new to port: the contract is the current Jackson 3
  behavior, and the absence of an abstraction is itself major-neutral.
- **Coherent Level-1 API.** The planned major-neutral application contract keeps its relationship
  facet linkage-oriented and its document facet advanced. Links-only support in the facade would
  require either the rejected state abstraction or a facade-level relationship envelope; both are
  explicitly avoided for 1.0.

## Consequences

Positive:

- No new public mapping type; ordinary relationship ergonomics and existing tests are unchanged.
- Write-side absence remains unambiguous: fieldset omission omits the whole relationship; Java null
  and empty collection have fixed linkage meanings.
- Read-side behavior for data-less relationships is deterministic and documented instead of
  incidental.
- Decoration, meta, inclusion, and PATCH boundaries all stay intact with no interaction analysis.

Negative / explicit losses:

- A server that wants `related`-link-only relationships must construct the relationship or document
  through core APIs rather than annotating a DTO. This is the accepted 1.0 cost.
- Ordinary linkage properties carry no data-presence marker. Absent `data` and explicit `"data":
  null` bind through different construction inputs (missing member vs explicit null), and whether
  the bound value differs depends on the DTO shape and configured Jackson; defaulted shapes
  distinguish them, shapes without per-property defaults bind both alike. The distinction is always
  recoverable at the document layer.
- If demand justifies it later, an opt-in data-presence capability can be added incrementally
  without breaking this contract; nothing in the 1.0 surface precludes it.

## Interactions

- **Ordinary relationship properties** (`T`, `Optional<T>`, `List`/`Set`/array of `T`,
  `ResourceIdentifier`, `RelationshipLinkage<T, M>`): all linkage-oriented; all emit `data`; all
  unchanged.
- **Flat reads:** data-less wire relationships leave the relationship property unbound and bind
  relationship meta normally. The unbound property follows configured Jackson missing-property
  semantics; explicit `"data": null` is a separate explicit null binding, and `"data": null` on a
  to-many property fails as invalid linkage cardinality. No diagnostic is raised for a data-less
  to-one relationship; the shape is supported, just not expressible as a data-presence marker in
  an ordinary linkage property.
- **Relationship links:** owned by core `Relationship.links` and, on writes, by additive decoration.
  A data-presence value — had one existed — would not have known about links; with Option A the
  question does not arise.
- **Relationship meta** (`@JsonApiRelationshipMeta`): unchanged and orthogonal; it follows the
  relationship's mapped wire name and binds/emits independently of linkage presence.
- **`RelationshipLinkage<T, M>`:** unchanged role — one linkage occurrence plus *identifier* meta
  (`ResourceIdentifier.meta`). It is not stretched into a general relationship-state wrapper.
- **Custom linkage mappers** (`RelationshipLinkageMapper`): invoked only for present linkage
  occurrences; a data-less relationship never reaches them, so no mapper can observe or produce
  data absence. There is no competing authority between mapper-selected linkage contents and
  data state.
- **Decoration (additive):** decorates only already-mapped relationships and never creates one;
  therefore it cannot materialize a links-only relationship from nothing. That limitation is
  intentional for 1.0.
- **Sparse fieldsets:** omitting a relationship from the fieldset removes the entire relationship
  object from the resource. A relationship present in the representation with an absent `data`
  member is a different state, reachable only through advanced construction. The two are never
  conflated.
- **Compound inclusion:** inclusion traversal consumes mapped relationship *target values*;
  inclusion requires resources to place in `included`, which a data-less relationship by
  definition does not carry. A data-less relationship therefore contributes no included resources
  and participates in traversal only in the trivial (empty) sense. Inclusion policy remains a
  representation-selection concern, not relationship state.
- **Jackson 2 parity:** no abstraction exists to port; future Jackson 2 work reproduces the current
  linkage-oriented read/write contract as is.
- **Spring / server operation policy:** separated by design. The general representation layer
  neither requires nor forbids `data` beyond JSON:API core rules; a future Spring layer may require
  supplied relationship objects in create/PATCH requests to carry `data` without changing this
  mapping contract. The Jackson/domain API never becomes HTTP-method-aware.
- **Level-1 application contract:** the ordinary resource/relationship surface stays
  linkage-oriented; links-only and meta-only relationships stay on the advanced document path. No
  relationship-envelope abstraction is introduced into the facade for this case.

## Non-goals

- Implementing any relationship data-presence type or wrapper (none is required by this decision).
- Changing `ResourceDecorator` / `ResourceDecoration` / `RelationshipDecoration` semantics.
- Adding links-only or meta-only emission to ordinary domain mapping, decorators, or the Level-1
  relationship facet.
- Persistence loading, lazy fetching, or ORM lifecycle state in jsonapi-java.
- Spring integration, endpoint generation, or create/PATCH operation validation.
- Jackson 2 implementation.
- Redesigning `RelationshipLinkage<T, M>` or `@JsonApiRelationshipMeta`.
