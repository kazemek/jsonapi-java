# Phase 2.11 — Jackson 3 Presence-Aware PATCH Binding

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phases 1.3, 2.9, and 2.10  
> **Status:** Not started

## Goal

Bind a validated JSON:API resource update into an immutable typed command that preserves exactly
which annotated DTO properties the client requested to change.

## Research and constraints

- [JSON:API 1.1 updating resources](https://jsonapi.org/format/1.1/#crud-updating) and
  [ADR-012](../../docs/adr/012-resource-patch-binding.md) — omitted members retain current values;
  supplied relationships replace linkage; the result is not JSON Merge Patch.
- Phase 1.3 validates single-resource shape, required identity, relationship `data`, and optional
  endpoint/body identity before domain binding.
- Phase 2.9 owns reverse Jackson mapping, logical property definitions, identifier conversion,
  linkage mappers, and stable mapping diagnostics; PATCH must reuse those contracts.
- Phase 2.10 owns the domain-facing envelope and access to non-patchable document/resource members.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — configured Jackson property types,
  names, ignores, mix-ins, and deserializers determine typed change values.

## Deliverables

- Add immutable `@NullMarked` public patch-command, property-change, and relationship-linkage
  abstractions parameterized by the annotated DTO `JavaType`, with explicit presence APIs and
  nullable values only where JSON null is legal.
- Add Jackson 3 patch reader entry points that compose Phase 2.10 decoding with Phase 1.3 update
  validation and expose the update resource identity plus ordered requested changes.
- Bind only supplied attribute entries through each mapped property's Jackson deserializer;
  preserve a present JSON null as a present nullable change and never synthesize omitted changes.
- Bind only supplied relationships from their required `data` member into explicit null, single,
  or collection linkage changes using Phase 2.9 identifier/linkage conversion.
- Add stable diagnostics, Javadoc, module examples, and conformance coverage for update binding
  without application mutation.

## Non-goals

- Constructing a complete DTO from a partial update or mutating an existing DTO/domain object.
- Authorization, business validation, persistence, transactions, or relationship endpoint logic.
- Resolving relationship changes from `included` or assembling a graph.
- Treating links, meta, extension/profile members, or included resources as patchable DTO fields.
- JSON Merge Patch, JSON Patch, bulk updates, or atomic operations.

## Implementation boundaries

- Patch binding accepts only documents validated with Phase 1.3 update usage. Convenience input
  methods perform decode, update validation, and binding as one all-or-nothing operation.
- The generic DTO type supplies mapping metadata and typed property values; no DTO constructor is
  invoked and no defaults are fabricated for omitted properties.
- Attribute changes are keyed by final JSON:API name and expose their Jackson logical property
  descriptor. Duplicate/colliding, unknown, ignored, or non-writable supplied fields fail according
  to the documented mapping/caller-Jackson policy.
- Relationship changes remain linkage-only and retain null versus empty collection cardinality.
  A target unable to represent the supplied linkage fails with the Phase 2.9 diagnostic contract.
- The command preserves request encounter order for diagnostics and deterministic application, but
  the application chooses whether and how to apply, authorize, or reorder changes.

## Test strategy

- Bind record, mutable POJO, and immutable POJO mapping definitions with one/many omitted and
  supplied attributes, explicit null, custom deserializers, renames, ignores, and unknown fields.
- Cover null/single/empty/non-empty relationship replacements, custom linkage conversion, and
  compound requests proving `included` never affects a change.
- Verify wrong primary shape, missing relationship data, endpoint mismatch, conversion failure,
  and unsupported target diagnostics before any command escapes.

## Acceptance criteria

- [ ] Patch commands contain exactly the supplied mapped attribute and relationship changes,
      preserving explicit null, null linkage, empty collections, encounter order, and identity.
- [ ] Omitted DTO properties never invoke constructors/deserializers, acquire fabricated defaults,
      or appear as changes.
- [ ] Binding reuses Phases 1.3/2.9/2.10 validation, mapping, and diagnostics and never reads
      `included` or mutates an application object.
- [ ] Public patch APIs satisfy ADR-009 nullness, the canonical `module-docs` checklist passes, and
      conformance documentation marks only presence-aware command binding **supported**.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*PatchBindingSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
