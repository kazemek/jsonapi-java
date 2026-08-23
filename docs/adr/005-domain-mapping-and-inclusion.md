# ADR-005: Separate Linkage from Inclusion

**Status:** Accepted  
**Date:** 2026-07-26

## Context

Serializing a relationship's resource linkage and placing the related resource in top-level `included` are separate JSON:API decisions. Automatically traversing every annotated relationship can disclose data, trigger lazy loading, create cycles, and violate requested include paths.

Sparse fieldsets also affect full linkage and must share the same serialization context.

## Decision

`@JsonApiRelationship` identifies a relationship and its name only. It carries no fetch, cascade, inclusion, repository, or ORM behavior.

Relationship values produce linkage. Related resources enter `included` only when selected by an explicit serialization context or application policy.

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
