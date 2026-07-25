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
