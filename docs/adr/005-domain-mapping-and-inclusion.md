# ADR-005: Separate Linkage from Inclusion

**Status:** Accepted  
**Date:** 2026-07-26  
**Amendment:** 2026-08-31 — relationship annotation is role-only; Jackson owns the member name; representation selection is separated from application policy  
**Amendment:** 2026-09-03 — `@JsonApiLocalId` adds an independent local-identifier domain role; `id` and `lid` never fall back to each other

## Context

Serializing a relationship's resource linkage and placing the related resource in top-level `included` are separate JSON:API decisions. Automatically traversing every annotated relationship can disclose data, trigger lazy loading, create cycles, and violate requested include paths.

Sparse fieldsets also affect full linkage. Requested include paths and fieldsets are operation scoped,
while include/field permissions and traversal limits are application scoped; those lifetimes must not
share one public context value.

## Decision

`@JsonApiRelationship` identifies a relationship role only. Configured Jackson owns the external relationship member name. The annotation carries no fetch, cascade, inclusion, repository, or ORM behavior.

Relationship values produce linkage. Related resources enter `included` only when an explicit
`RepresentationSelection` requests the path and `RepresentationPolicy` permits traversal.

`RepresentationSelection` contains only requested include paths and sparse fieldsets.
`RepresentationPolicy` contains include/field permissions and traversal/resource limits. Jackson
adapters compose them into one internal effective representation; neither policy nor selection
replaces `MappedDocument` sparse-fieldset provenance at the writer boundary.

Compound serialization:

- validates requested relationship paths;
- includes intermediate resources needed for full linkage;
- applies sparse fieldsets by resource type;
- deduplicates by resource identity;
- has configurable depth and resource-count limits;
- detects cycles without treating repeated identity as an error;
- emits deterministic first-encounter order.

JPA and other persistence technologies receive no implicit integration.

## Consequences

- Ordinary relationship serialization does not walk an unbounded graph.
- Include behavior is testable and request-aware.
- Applications explicitly decide what relationships may be exposed.
- Inclusion requires context beyond a plain value serializer.
- ORM-specific convenience can be considered later without affecting core semantics.

## Amendment (2026-08): Sparse-fieldset provenance at the writer boundary

Mapping provenance for sparse fieldsets is owned by the writer, not by callers. Mapping overloads
that apply fieldsets return a provenance-bearing result (`MappedDocument`) carrying the identities
of included resources whose inbound linkage was removed by an applied fieldset while inclusion
still traversed the linking relationship. Document writers compose that provenance with their bound
validation context before validation; callers never translate mapping provenance into validation
policy themselves. Provenance stays out of the core document model — it is a mapping result, not a
document member.

Full-linkage relaxation granularity is per-resource, not document-wide. Exempted included
resources count as reachable roots, so their own relationships still extend reachability to their
subtrees; every other included resource still requires full linkage. A blanket document-wide flag
was rejected because one legitimate fieldset omission would silently disable detection of
unrelated genuine full-linkage defects elsewhere in the same document. The exemption concept is a
core validation-policy value (`ValidationContext`) keyed by core resource identity, so adapter
 integrations share it without Jackson-major-specific choreography.

## Amendment (2026-08): Declared type fidelity for generic domain writes

The write mapper has two root-type entry routes:

- convenience methods infer a type from a concrete runtime class;
- overloads accepting a complete Jackson `JavaType` preserve caller-declared parameterization.

A directly parameterized runtime value such as `Container<Thing>` has only `Container.class` at
runtime, so the convenience route cannot recover `Thing`. When a mapped member depends on that
binding, an unparameterized root fails at its JSON:API member location rather than guessing from a
null, empty collection, first element, proxy, or polymorphic runtime value. A concrete subtype that
binds its generic superclass, such as `ThingContainer extends Container<Thing>`, remains eligible for
the convenience route when Jackson resolves that binding.

The declared `JavaType` is carried through the serialization-oriented `ResourceMapping`, property
serialization, relationship linkage, and recursive include traversal. Mapping cache entries remain
distinct for distinct parameterizations. This keeps generic scalar properties and `T`,
`Optional<T>`, and `List<T>` relationships on the same configured Jackson authority as ordinary
writes; the relationship/inclusion separation above is unchanged.

## Amendment (2026-09): First-class local identifier domain mapping

JSON:API `id` and `lid` are independent identity members, and domain mapping now represents them
that way. `@JsonApiId` is the role for the resource `id` member; the new `@JsonApiLocalId` is a
role-only annotation for the `lid` member. Neither role falls back to the other: write mapping
emits the id role to `ResourceObject.id` and the local-id role to `ResourceObject.lid`, flat reads
bind wire `id` only to the id role and wire `lid` only to the local-id role, and relationship
linkage preserves id-only, lid-only, and id+lid identity without promoting a local identifier to
`id`. Mapping metadata tracks the two roles as separate optional properties; at most one property
per role, never both roles on one property, and at least one identity role per mapped type.
Violations fail deterministically with the existing mapping diagnostics (`DUPLICATE_ROLE`,
`MISSING_IDENTIFIER`).

The local identifier is a JSON:API protocol concept for resources identified only within their
document, not an application persistence or transient-entity heuristic, and mapping never infers
role from value shape, nullness, or database state. A mapped lid-only resource represents a state
that is legal for create/local-identifier usage; core document validation remains the authority
for whether that state is legal in a given document usage. Configured Jackson remains the sole
authority for the underlying Java property, and identifier conversion stays shared between the two
roles through the existing `IdentifierConverter`.

Compound-inclusion identity bookkeeping is alias-aware to match core validation's id↔lid partner
binding: a resource carrying both members is registered under both its id and lid keys, a primary
is recognized under any of its aliases, and included occurrences of one resource deduplicate no
matter which alias the reaching occurrence carries. Unequal representations that share an identity
alias still fail with `CONFLICTING_INCLUDED_REPRESENTATION` at mapping time; core duplicate policy
is unchanged.
