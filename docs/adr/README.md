# Architecture Decision Records

ADRs record consequential, hard-to-reverse “why” decisions. Vision and roadmap live in
[`docs/vision.md`](../vision.md); concrete delivery plans live under
[`.agentWork/milestones/`](../../.agentWork/milestones/README.md).

1. [ADR-001: Document Codec as the Product Boundary](001-product-boundary.md)
2. [ADR-002: Preserve JSON:API Wire States](002-document-representation.md)
3. [ADR-003: Strict Construction and Aggregate Validation](003-validation-and-immutability.md)
4. [ADR-004: Jackson Introspection Is Authoritative](004-jackson-integration.md)
5. [ADR-005: Separate Linkage from Inclusion](005-domain-mapping-and-inclusion.md)
6. [ADR-006: Document-First Deserialization](006-read-boundary.md)
7. [ADR-007: Optional Adapter Modules](007-module-boundaries.md)
8. [ADR-008: Public Namespace and Maven Group](008-public-namespace.md)
9. [ADR-009: JSpecify Nullness](009-jspecify-nullness.md)
10. [ADR-010: Architectural Tests for Module Boundaries](010-architectural-tests.md)
11. [ADR-011: Flat DTO Reads Remain Document-First](011-flat-dto-read-binding.md)
12. [ADR-012: Resource PATCH Produces Presence-Aware Commands](012-resource-patch-binding.md)
