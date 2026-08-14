# Phase 2.17 — Jackson 2 Document Reader

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phase 2.16  
> **Status:** Not started
> **Work item:** KAZ-28

## Goal

Port the explicit, validated document-read contract to Jackson 2 with semantic and diagnostic
parity with Jackson 3, driven by the shared codec fixture catalog.

## Research and constraints

- `JsonApiDocumentReader` (`jsonapi-java-jackson3` README / [ADR-006](../../docs/adr/006-read-boundary.md))
  defines the stable read contract: explicit resource versus resource-identifier
  primary-data interpretation, public core construction, aggregate validation, and safe
  path/location diagnostics.
- Phase 2.16 establishes the Jackson 2 artifact, supported `2.22.x` / BOM `2.22.1` baseline, and
  `:jsonapi-java-jackson2:jackson2CompatibilityTest`; reads stay within its
  `com.fasterxml.jackson.*` API and common-contract dependency boundary.
- `JsonApiFixtures.codec()`, `negativeCodec()`, and `ambiguousPrimaryData()` own capability-tagged
  positive and read-only negative fixtures with stable category, pointer, and rule-code expectations.
- [ADR-002](../../docs/adr/002-document-representation.md) and
  [ADR-003](../../docs/adr/003-validation-and-immutability.md) — Jackson major differences may not
  change wire-state preservation or bypass core validation.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
  — adapt parser/databind calls explicitly; do not introduce reflection or a lowest-common-denominator
  Jackson abstraction.

## Deliverables

- Add Jackson 2 reader entry points under `io.github.kazemek.jsonapi.jackson2` that consume common
  read contexts/diagnostics and preserve jackson3 `JsonApiDocumentReader` ownership and
  primary-data-kind rules.
- Port the token-driven decoder for strings, UTF-8 bytes, streams, and caller-owned parsers using
  Jackson 2 APIs and public core constructors only.
- Port the read exception contract with stable category, JSON Pointer-like path, safe Jackson 2
  source location, and originating core `ValidationRuleCode`.
- Run the `JsonApiFixtures` positive, ambiguous, extension/profile, malformed, local-validation, and
  aggregate-validation capability matrix and document any justified parser-location difference.
- Refresh Jackson 2 module docs/Javadoc and conformance notes for read support without changing the
  Jackson 3 contract.

## Non-goals

- Automatic domain hydration, included-resource wiring, persistence, or PATCH execution; Phases
  2.21–2.23 compose this validated reader into narrower DTO/envelope/command contracts.
- Reflection-based runtime selection between Jackson major versions.
- Input/resource limits, which remain Phase 4.1 work.
- Jackson 2 domain mapping, compound inclusion, or sparse fieldsets; Phases 2.18–2.20 own them.
- Duplicating common contexts/diagnostics or forking the `JsonApiFixtures.codec()` fixture corpus.

## Implementation boundaries

- Resource versus resource-identifier interpretation remains caller-selected for both object and
  array primary data; Jackson 2 must not add shape heuristics absent from Jackson 3.
- Unknown/additional member classification, open-value conversion, constructor use, and aggregate
  validation follow jackson3 `JsonApiDocumentReader` exactly.
- Caller-owned parser and stream lifecycles remain unchanged. Convenience overloads close only
  resources they create.
- Production code imports no `tools.jackson.*`, `jsonapi-java-jackson3`, or `core.internal` types;
  shared behavior is proven by `JsonApiFixtures` codec fixtures rather than cross-major runtime
  delegation.

## Test strategy

- Parameterize `JsonApiFixtures` read/round-trip fixtures and assert equal `JsonApiDocument` values from
  Jackson 2 and Jackson 3 readers.
- Compare failure category, core rule code, and JSON Pointer exactly; compare source locations only
  where the fixture capability requires it, documenting parser-boundary differences otherwise.
- Run dependency/ArchUnit allowlist checks for Jackson 2 reader production code. Phase 2.16 owns
  `:jsonapi-java-jackson2:jackson2CompatibilityTest` against Jackson `2.22.x` / BOM `2.22.1`.

## Acceptance criteria

- [ ] All applicable `JsonApiFixtures` read fixtures produce model values equal to Jackson 3 results,
      including ambiguous empty/single primary data under the explicit primary-data kind; caller-owned
      parsers and streams remain open after successful convenience and caller-owned reads.
- [ ] Shared malformed and validation fixtures produce equal stable categories, pointers, and core
      rule codes; every source-location difference is narrowly documented and tested; caller-owned
      resources remain open after failed reads.
- [ ] Jackson 2 reader code depends on common contracts for neutral types and has no
      production/runtime dependency on Jackson 3 or `core.internal` (ArchUnit allowlist).
- [ ] The canonical `module-docs` checklist and conformance notes cover the completed Jackson 2
      read flow without claiming domain hydration or Phase 4 limits.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*DocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
