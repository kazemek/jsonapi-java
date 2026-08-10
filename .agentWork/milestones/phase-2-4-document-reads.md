# Phase 2.4 — Jackson 3 Document Reader

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phases 1.1 and 2.1  
> **Status:** Complete

## Goal

Read JSON:API JSON into validated core documents with explicit primary-data interpretation and stable path/location diagnostics, without hydrating domain graphs.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) and [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — every read first terminates at a validated document model; Phase 2.9 may subsequently bind flat DTOs, but unresolved linkage and included resources are never injected into application objects.
- [ADR-002](../../docs/adr/002-document-representation.md) — missing, explicit `null`, empty collection, and empty object states must remain distinct after parsing.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — parsing must call public core constructors and aggregate validation; it may not introduce a raw or bypass construction path.
- `DocumentData` permits both resource and resource-identifier objects, whose minimal JSON forms can be identical — the read API must require an explicit resource-document versus relationship-document primary-data interpretation instead of guessing from object members or an empty array.
- `ValidationContext` and `JsonApiDocumentValidator` — caller policy controls document usage, extension/profile members, pagination hints, and aggregate rules after local construction succeeds.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) — use `tools.jackson.*` streaming/databind APIs and preserve ownership of caller-supplied parsers and streams.

## Deliverables

- Add an immutable public read context that combines `ValidationContext` with an explicit primary-data kind (`RESOURCE` or `RESOURCE_IDENTIFIER`), and a `JsonApiDocumentReader` configured from a caller-supplied Jackson 3 JSON mapper.
- Implement one internal streaming decoder used by string, UTF-8 byte-array, `InputStream`, and `JsonParser` entry points; convenience methods close only resources they create, while caller-owned streams/parsers remain open.
- Construct every core model value through its public API, classify `@`/namespaced members into additional-member maps, preserve profile candidates for aggregate policy, and run `JsonApiDocumentValidator` before returning.
- Add one public read exception contract carrying a stable codec failure category, JSON Pointer-like path, safe Jackson source location, and the originating core `ValidationRuleCode` when validation produced the failure.
- Add positive and malformed Spock fixtures, refresh module docs/Javadoc for the read flow, and update `docs/conformance.md` for the delivered deserialization and diagnostic coverage.

## Non-goals

- Automatic domain-object construction in this reader, included-resource wiring, identity maps, persistence lookup, or PATCH application. Phases 2.9–2.11 compose validated reads into those narrower DTO/envelope/update contracts.
- Guessing whether an ambiguous object/array is resource data or resource-identifier linkage.
- Lenient parsing that returns an invalid document.
- Input size, nesting, and collection limits; Phase 4.1 owns the coordinated hardening policy and deterministic limit failures.
- Jackson 2 reads; Phase 2.17 ports this contract after it is stable.

## Implementation boundaries

- Parsing is token-driven so duplicate object members, token kinds, JSON Pointer state, and source locations remain observable; converting the complete document to an unlocated generic map/tree is not the read implementation.
- The Jackson 3 internal package owns its pointer accumulator because `core.internal.JsonPointers` is off-limits; member segments escape `~` as `~0` and `/` as `~1`.
- Unknown fixed-shape members are retained only in the model's additional-member channel. `@` and namespaced members are classified structurally; unnamespaced candidates survive construction only when the supplied profile policy allows them during aggregate validation.
- The primary-data kind controls both single-object and array decoding, including empty arrays. Relationship object `data` always uses `RelationshipData` and does not depend on the top-level kind.
- Open values are recursively converted to the core-supported null/string/boolean/finite-number/list/string-keyed-map shapes without exposing Jackson tree nodes in the model; parser configurations that admit non-finite numbers still fail through the core validity contract.
- For constructor or aggregate failures, retain token locations by pointer and report the exact or nearest enclosing captured location. Messages must not include source payloads or arbitrary application values.

## Test strategy

- Decode official request/response examples and the Phase 2.1 canonical fixtures under the correct explicit read context, then assert semantic equality with constructed core documents.
- Cover all absent/null/single/collection states, empty wrappers/arrays, all link forms, compound documents, local identifiers, errors, metadata, extensions, profiles, and `@` members.
- Use paired ambiguous fixtures (`{"type":"articles","id":"1"}` and `[]`) to prove the selected primary-data kind determines `DocumentData` variants without heuristics.
- Cover malformed JSON, wrong token types, duplicate/reserved/unknown members, constructor failures, and aggregate failures with stable category, pointer, location, and core rule-code assertions.

## Acceptance criteria

- [x] Every Phase 1.1 wire state decodes through public core construction and aggregate validation without conflating absence, explicit null, empty, single, or collection forms.
- [x] Ambiguous object and empty-array primary data require and obey the explicit resource versus resource-identifier read context; no shape-guessing heuristic exists.
- [x] Malformed, local-validation, and aggregate-validation failures expose stable categories and paths plus safe source locations, and no invalid document is returned.
- [x] The canonical `module-docs` checklist passes and conformance documentation marks the delivered read/diagnostic behavior **supported** without claiming flat DTO binding or domain hydration.
- [x] `./gradlew :jsonapi-java-jackson3:test --tests '*DocumentReaderSpec'` passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
