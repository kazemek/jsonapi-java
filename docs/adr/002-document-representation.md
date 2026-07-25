# ADR-002: Preserve JSON:API Wire States

**Status:** Accepted  
**Date:** 2026-07-26

## Context

JSON:API gives different meanings to an absent member, explicit JSON `null`, an empty array, a single object, and an object array. Java `null` alone cannot preserve these states.

A relationship can also contain only links or metadata. Such a document does not reveal to-one versus to-many cardinality.

## Decision

Use sealed data-value types for explicit null, single, and collection states. A nullable component on its containing object represents member absence only.

Represent a relationship as one object with optional linkage, links, and metadata. Its linkage uses explicit null/single/collection variants; the relationship itself is not a to-one/to-many hierarchy.

Use flat object wrappers for attributes, relationships, links, and metadata. Links preserve null values and insertion order. Wrappers separate semantic members from pass-through members so `@` members are not misinterpreted as attributes, relationships, or links. The codec flattens both groups and rejects collisions. Fixed-shape objects retain permitted extension, profile-policy, and `@` members in an additional-members map.

The model is semantically complete rather than constrained to a predetermined number of Java types.

## Consequences

- Valid `"data": null` is distinguishable from a missing `data` member.
- Link-only relationships deserialize without guessed cardinality.
- Empty to-many linkage remains distinct from empty to-one linkage.
- Custom codec handling is required for flat maps and sealed data values.
- New model types are acceptable when they preserve a meaningful wire distinction.
