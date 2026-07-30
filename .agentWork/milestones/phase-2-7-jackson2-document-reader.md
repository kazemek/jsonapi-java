# Phase 2.7 — Jackson 2 Document Reader

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.4 and 2.6  
> **Status:** Not started

## Goal

Port the explicit, validated document-read contract to Jackson 2 with semantic and diagnostic parity with Jackson 3.

## Research and constraints

- Phase 2.4 defines the stable read contract: explicit resource versus resource-identifier primary-data interpretation, public core construction, aggregate validation, and safe path/location diagnostics.
- Phase 2.6 establishes the independent Jackson 2 artifact, supported `2.22.x` compatibility line,
  BOM `2.22.1` baseline, and `:jsonapi-java-jackson2:jackson2CompatibilityTest` task; reads must
  stay within its `com.fasterxml.jackson.*` API and dependency boundary.
- [ADR-002](../../docs/adr/002-document-representation.md) and [ADR-003](../../docs/adr/003-validation-and-immutability.md) — Jackson major differences may not change wire-state preservation or bypass core validation.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) catalogs renamed parser/databind APIs — adapt implementation calls explicitly instead of introducing reflection or a lowest-common-denominator public abstraction.
- Shared fixtures must establish model and diagnostic parity; source offsets may differ only where the two Jackson parsers document different locations for the same malformed token.

## Deliverables

- Add Jackson 2 reader/context entry points under `io.github.kazemek.jsonapi.jackson2` with the same conceptual API, ownership rules, and primary-data-kind requirement as Phase 2.4.
- Port the token-driven decoder for strings, UTF-8 bytes, streams, and caller-owned parsers using Jackson 2 APIs and public core constructors only.
- Port the read exception contract with stable category, JSON Pointer-like path, safe Jackson 2 source location, and originating core `ValidationRuleCode`.
- Run the shared positive, ambiguous, extension/profile, malformed, local-validation, and aggregate-validation fixture matrix across both Jackson majors and document any justified parser-location difference.
- Refresh Jackson 2 module docs/Javadoc and conformance notes for read support without changing the Jackson 3 contract.

## Non-goals

- Automatic domain hydration, included-resource wiring, persistence, or PATCH execution; Phases
  2.15–2.17 compose this validated reader into narrower DTO/envelope/command contracts.
- Reflection-based runtime selection between Jackson major versions.
- Input/resource limits, which remain Phase 4.1 work.
- Jackson 2 domain mapping, compound inclusion, or sparse fieldsets; Phases 2.12–2.14 own them.

## Implementation boundaries

- Resource versus resource-identifier interpretation remains caller-selected for both object and array primary data; Jackson 2 must not add shape heuristics absent from Jackson 3.
- Unknown/additional member classification, open-value conversion, constructor use, and aggregate validation follow Phase 2.4 exactly.
- Caller-owned parser and stream lifecycles remain unchanged. Convenience overloads close only resources they create.
- Production code imports no `tools.jackson.*`, `jsonapi-java-jackson3`, or `core.internal` types;
  shared behavior is proven by fixtures rather than cross-major runtime delegation.

## Test strategy

- Parameterize the same semantic fixture manifest used by Phase 2.4 and assert equal `JsonApiDocument` values from Jackson 2 and Jackson 3 readers.
- Compare failure category, core rule code, and JSON Pointer exactly; compare source locations exactly where parser contracts align and by documented token boundary otherwise.
- Run dependency/ArchUnit checks and `:jsonapi-java-jackson2:jackson2CompatibilityTest` against the
  Jackson `2.22.x` line using BOM `2.22.1`, as established in Phase 2.6.

## Acceptance criteria

- [ ] All shared read fixtures produce model values equal to Jackson 3 results, including ambiguous empty/single primary data under the explicit primary-data kind.
- [ ] Shared malformed and validation fixtures produce equal stable categories, pointers, and core rule codes; every source-location difference is narrowly documented and tested.
- [ ] Jackson 2 reader code has no production/runtime dependency on Jackson 3, the Jackson 3 artifact, or `core.internal`, and caller-owned resources remain open.
- [ ] The canonical `module-docs` checklist and conformance notes cover the completed Jackson 2 read flow without claiming domain hydration or Phase 4 limits.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*DocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
