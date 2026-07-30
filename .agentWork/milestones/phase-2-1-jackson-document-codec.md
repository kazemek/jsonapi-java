# Phase 2.1 — Jackson 3 Document Writer

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 1.1  
> **Status:** Not started

## Goal

Add a Jackson 3 artifact that validates and writes every completed core document-model state with deterministic JSON:API v1.1 wire semantics.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) and [ADR-007](../../docs/adr/007-module-boundaries.md)
  reserve symmetric `jsonapi-java-jackson3` / `jsonapi-java-jackson2` artifacts; this milestone
  implements the first without introducing a combined or runtime-detected Jackson artifact.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) — databind uses `tools.jackson.*`; `ObjectMapper` and `JsonFactory` are immutable and modules are added through builders or `rebuild()`. The writer must not assume Jackson 2 mutation APIs.
- [ADR-002](../../docs/adr/002-document-representation.md) — Java `null` means member absence, sealed data/linkage variants mean wire `null`/single/collection, and flat wrappers must not leak synthetic accessor names.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) and `JsonApiDocumentValidator` — local constructors remain authoritative and aggregate validation with a caller-supplied `ValidationContext` runs before any bytes are emitted.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — document envelopes require explicit codecs; default record serialization is not an acceptable implementation for flat wrappers, sealed data/linkage, links, or additional members.
- [JSON:API v1.1](https://jsonapi.org/format/1.1/) — the textual specification and repository conformance fixtures are authoritative. The unreleased JSON:API 1.1 schema is only a supplemental check in Phase 2.5.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md), [ADR-010](../../docs/adr/010-architectural-tests.md), and the `module-docs` skill — the new public package is `@NullMarked`, module dependencies are guarded, and the public writer flow receives dual-audience documentation.
- [`AGENTS.md`](../../AGENTS.md) — add Jackson through the version catalog and regenerate dependency-verification checksums without weakening verification.

## Deliverables

- Register `jsonapi-java-jackson3`, add current maintained Jackson 3 databind coordinates, and add an ArchUnit rule permitting only JDK, JSpecify, core public packages, and `tools.jackson..`; amend ADR-007, ADR-010, vision, and root module registration for the symmetric major-version artifacts.
- Add a small public writer entry point in `io.github.kazemek.jsonapi.jackson3` that derives a codec-configured mapper from a caller-supplied Jackson 3 JSON mapper/builder, binds a `ValidationContext`, and never mutates or silently replaces caller configuration.
- Implement internal streaming serializers for `JsonApiDocument` and its nested model values, including all `DocumentData`, `RelationshipData`, and `Link` variants, nullable link entries, open JSON values, and flattened attributes, relationships, links, metadata, and additional members.
- Add version-neutral canonical writer fixtures and a manifest under `fixtures/jsonapi-1.1/`, plus Jackson 3 Spock contract tests for every Phase 1.1 wire state, based on official JSON:API examples and local edge cases the examples do not cover.
- Use `module-docs` for the new module and update `docs/conformance.md` so serialization and its canonical policy become **supported** while deserialization remains **deferred** to Phase 2.4.

## Non-goals

- Deserialization, malformed-input diagnostics, or source-location tracking; Phase 2.4 owns reads.
- JSON Schema validation; Phase 2.5 owns the pinned draft-schema cross-check.
- Annotated domain-object mapping, compound traversal, sparse fieldsets, or Jackson 2 support.
- A claim that every historical Jackson 3 minor is supported; the stable-release compatibility matrix remains Phase 4 work.

## Implementation boundaries

- Public types live in `io.github.kazemek.jsonapi.jackson3`; implementation types live in `io.github.kazemek.jsonapi.jackson3.internal`. No code may import `io.github.kazemek.jsonapi.core.internal`.
- A writer is configured with an explicit `ValidationContext`; a documented convenience default may use `ValidationContext.defaults()`. Aggregate validation happens before generator output starts so failure cannot leave a partially written document.
- Standard members use one documented per-object order matching the model accessor order, followed by additional members in insertion order. Arrays and map-backed semantic members retain caller/model order; `hreflang` always emits its canonical array form.
- Presence helpers (`hasDataMember()`, `hasErrorsMember()`, `hasIncludedMember()`, and relationship `hasDataMember()`) govern omission. Sealed null variants emit JSON `null`, present-empty wrappers emit `{}`, empty collections emit `[]`, and null-valued links remain present as JSON `null`.
- `Attributes`, `Relationships`, and `Links` serialize from `flatten()`; `Meta` serializes from `members()`. Additional members write directly into fixed-shape containing objects. Open values are emitted only through their JSON-compatible core shapes; no reflective bean serialization of core model records is used.

## Test strategy

- Build model values in tests, validate/write them, and compare parsed JSON trees with the shared fixture manifest; use exact UTF-8 text assertions only for the documented member order and canonical `hreflang` shape.
- Cover resource and identifier single/collection forms, explicit-null and absent primary/relationship data, empty collections, present-empty attributes/relationships, link-only and meta-only relationships, string/object/null links, errors, JSON:API information, compound data, local identifiers, extensions, profiles, and `@` members.
- Assert that invalid aggregate documents fail before output with the original `ValidationRuleCode` and JSON Pointer-like path.
- Assert runtime/compile dependency boundaries and that ordinary caller serialization remains unchanged outside the explicit JSON:API writer.

## Acceptance criteria

- [ ] The published runtime artifact uses Jackson 3 (`tools.jackson.*`), core remains Jackson-free, dependency verification is updated, and ArchUnit rejects core-internal or Jackson 2 production dependencies.
- [ ] Every Phase 1.1 state emits the required absent/null/single/collection and flat-object wire shape; canonical member order, collection order, nullable links, and array-form `hreflang` are fixture-tested.
- [ ] Invalid aggregate documents fail before output, and using the JSON:API writer does not alter the caller mapper's ordinary serialization behavior.
- [ ] The canonical `module-docs` checklist passes and `docs/conformance.md` marks only the delivered serialization contract **supported**.
- [ ] `./gradlew :jsonapi-java-jackson3:test` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
