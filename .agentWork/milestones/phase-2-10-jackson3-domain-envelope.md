# Phase 2.10 — Jackson 3 Typed Domain Envelope

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 2.9  
> **Status:** Not started

## Goal

Expose complete validated JSON:API documents through a domain-facing typed envelope with flat
primary DTOs and independently bound included DTOs.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — common Jackson/Spring DTO flows do not
  require `JsonApiDocument`, but binding remains document-first and graph-free.
- [ADR-002](../../docs/adr/002-document-representation.md) — the envelope must preserve absent,
  explicit-null, single, and collection primary data plus nullable links and additional members.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — inclusion and relationship
  linkage are separate; read envelopes likewise expose included resources without relationship
  injection.
- Phase 2.4 remains the only JSON parser and validator. Phase 2.9 remains the only resource-to-DTO
  binder; this milestone composes those contracts instead of duplicating them.
- JSON:API permits heterogeneous primary and included collections. Java target types therefore
  come from an explicit resource-type registry, never from object-shape guessing.

## Deliverables

- Add immutable domain-facing envelope and primary-data types preserving absent, explicit-null,
  single, and collection states without exposing core document types in routine reader signatures.
- Add envelope views/binding hooks for links, meta, JSON:API information, errors, and additional
  members, with JSON-compatible immutable defaults and caller-selected Jackson `JavaType` targets
  for open metadata where applicable.
- Add an immutable resource-type registry mapping JSON:API type names to target `JavaType` and
  optional linkage/identifier converters for primary and included binding.
- Bind `included` in wire order into an immutable typed collection indexed by resource identity;
  require explicit registration, reject conflicting target registrations, and fail unregistered
  included types with stable diagnostics without injecting them into primary DTOs.
- Add a domain reader facade over Phase 2.4 plus module documentation, Javadoc, examples, and
  conformance updates for resource, error, meta-only, and compound documents.

## Non-goals

- Resolving relationship fields from `included`, graph assembly, persistence lookup, or cycles.
- Applying extension/profile semantics beyond preserving their members.
- Presence-aware PATCH commands; Phase 2.11 owns update binding.
- Replacing the public document-model reader for advanced callers.
- Jackson 2 support; Phase 2.16 ports the stable envelope contract.

## Implementation boundaries

- Convenience readers accept the Phase 2.4 input forms and read context, validate first, and then
  bind. Codec and validation failures retain their existing category, path, location, and rule
  code; envelope/binding failures add a distinct stable mapping category.
- Resource type selects the target only through the supplied registry. Duplicate registrations,
  target/type mismatches, and unregistered types fail before a partially bound envelope escapes.
- Error documents never attempt primary DTO binding. Meta-only documents and absent data remain
  representable, while `data: null` remains distinct from both.
- Included DTOs preserve first wire encounter order and core-validated identity uniqueness. Their
  relationship properties remain linkage-only under Phase 2.9.
- Public nullness follows ADR-009: packages are `@NullMarked`, and only states that semantically
  permit Java null use `@Nullable`.

## Test strategy

- Decode official and canonical data, error, meta-only, extension/profile, and compound fixtures
  directly into typed envelopes and compare their document semantics with Phase 2.4 results.
- Cover homogeneous and heterogeneous primary/included resources, id/lid identities, registration
  errors, explicit null/absent/empty primary data, nullable links, and typed metadata.
- Prove no graph wiring by binding cyclic/shared included fixtures and asserting relationships
  remain linkage values while included DTOs are independently indexed.

## Acceptance criteria

- [ ] Domain reader entry points preserve every primary-data state and document-level member
      covered by Phase 2.4 without requiring `JsonApiDocument` in routine caller signatures.
- [ ] Explicit registration deterministically binds heterogeneous primary/included resources,
      preserves included order and identity lookup, and reports stable unknown/conflict failures.
- [ ] Included resources are never injected into primary or included DTO relationship properties,
      including cyclic and shared-identity fixtures.
- [ ] Public envelope APIs satisfy ADR-009 nullness and the canonical `module-docs` checklist;
      conformance documentation marks only delivered envelope shapes **supported**.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*DomainDocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
