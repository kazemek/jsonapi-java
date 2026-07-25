# ADR-006: Document-First Deserialization

**Status:** Accepted  
**Date:** 2026-07-26

## Context

Resource linkage may reference a resource not present in `included`. Arbitrary domain graph hydration also requires identity handling, cycle policy, constructor selection, partial-update semantics, and application-specific resolution.

Treating graph hydration as the inverse of serialization would hide these unresolved choices.

## Decision

Initial read support decodes JSON into the document model and validates it. It does not automatically hydrate annotated domain object graphs.

Applications consume resource objects, identifiers, relationships, and errors directly or implement an explicit mapping layer. A future domain-binding feature requires its own ADR and must define unresolved linkage, identity, immutable construction, cycles, and PATCH semantics.

The codec remains capable of reading request and response document shapes; this decision limits the target Java representation, not JSON:API wire coverage.

## Consequences

- Deserialization has a clear, achievable contract.
- Linkage-only documents do not fabricate domain instances.
- Request validation can ship before a domain hydration system.
- Users wanting graph binding must write policy-aware mapping code initially.
- Serialization and deserialization are intentionally asymmetric at the domain-object layer.
