# Phase 2.12 — Canonical Codec Fixture Contract

> **Scope:** fixtures / `jsonapi-java-test-fixtures` / jackson3 codec tests  
> **Dependencies:** Phases 2.4, 2.5, and 2.11  
> **Status:** Complete

## Goal

Make one capability-tagged fixture catalog the executable positive and negative document-codec
contract for every Jackson-major adapter.

## Research and constraints

- [`fixtures/jsonapi-1.1/README.md`](../../fixtures/jsonapi-1.1/README.md) already forbids
  major-specific copies and names capability selection, but `WriterFixture` still lacks capability
  metadata and Phase 2.5 schema-kind / known-disagreement maps still live in
  `JsonApiDraftSchemaSpec`.
- Phase 2.4 keeps malformed and validation reader cases inline in `DocumentReaderSpec`; Jackson 2
  parity needs the same inputs and stable expectations without copying a Spock specification.
- Ambiguous primary-data cases (`ambiguous object primary data obeys PrimaryDataKind`,
  `ambiguous empty array primary data obeys PrimaryDataKind`) are dual-success scenarios under both
  `RESOURCE` and `RESOURCE_IDENTIFIER` contexts; they must not be forced into a failure-diagnostic
  corpus.
- Closed negative corpus from Phase 2.4: `malformed-json-without-payload`,
  `truncated-document-enclosing-path`, `empty-input`, `trailing-content-after-document`,
  `unexpected-token-path-location`, `duplicate-members`, `local-validation-top-level`,
  `included-missing-type`, `collection-missing-type`, `relationship-identifier-missing-type`,
  `reserved-attribute`, `missing-link-href`, `invalid-dynamic-link-relation`,
  `invalid-dynamic-attribute-name`, `aggregate-uri-link-relation`,
  `aggregate-validation-resource-location`, `extension-members-require-context`.
- Each negative case records failure category, JSON Pointer, and core `ValidationRuleCode` only
  when present; those fields may be null for `MALFORMED_JSON` and other codec-only failures that
  never produce a validation rule.
- The pinned draft schemas remain supplemental evidence, never the conformance oracle; known gaps
  must stay explicit and intentionally failing.
- Exact UTF-8 ordering, canonical `hreflang`, usage-specific validation, and parser source location
  are not universally symmetric capabilities and must be selected explicitly.

## Deliverables

- Add immutable capability metadata on the document fixture catalog covering write, read, schema
  kind, validation context, primary-data kind, exact-byte policy, and known schema disagreement;
  retain stable fixture ids and expected JSON paths (rename writer-only types only if needed).
- Keep every existing valid fixture in one manifest-backed catalog and run all applicable cases
  through Jackson 3 write, read, round-trip, input-source, and supplemental schema checks.
- Add a manifest-backed read-only negative corpus for exactly the closed Phase 2.4 case ids above,
  with version-neutral expected failure category, JSON Pointer, and core `ValidationRuleCode` only
  when present (null allowed for `MALFORMED_JSON` / codec-only failures); tag source-location
  assertions separately where parser contracts cannot be identical.
- Add shared dual-success ambiguous primary-data cases (object and empty-array) with expected models
  under both `PrimaryDataKind` values, separate from the negative corpus.
- Centralize fixture/schema directory wiring and catalog integrity checks; update fixture guidance
  and conformance traceability for capability selection and intentional asymmetry.

## Non-goals

- Domain mapping, compound traversal, sparse-fieldset, DTO, envelope, or PATCH fixture models;
  Phases 2.13–2.15 and 2.24–2.26 own those layers.
- Treating the draft schemas as normative or changing behavior to satisfy a known draft gap.
- Requiring a read-only malformed input to have a writer representation.
- Implementing Jackson 2 or Phase 4.1 fuzzing, configurable limits, and exhaustive security cases.

## Implementation boundaries

- Fixture files and expected semantic diagnostics remain Jackson-major-neutral; adapter tests own
  only mapper construction and parser-specific location adaptation.
- Existing ids and canonical JSON meanings remain stable. Renaming internal Groovy types must not
  fork or rewrite expected wire documents solely for new terminology.
- Every fixture declares applicable capabilities; tests select by capability instead of maintaining
  independent hard-coded id lists.
- Source text and payload fragments must not leak through safe diagnostic assertions.

## Test strategy

- Verify manifest/catalog bijection, unique ids, valid paths, required capability metadata, and
  exact-byte sibling presence.
- Parameterize Jackson 3 writer, reader, every supported reader input source, round-trip, and schema
  suites from the shared catalog.
- Run the closed negative corpus and dual-success ambiguous cases through Jackson 3 and assert the
  documented expectations.

## Acceptance criteria

- [x] All 24 existing valid documents are cataloged for both write and read, and each applicable
      writer, reader/input-source, round-trip, and schema test derives its cases from capabilities.
- [x] Schema kind, validation/read context, primary-data kind, exact-byte policy, canonical
      `hreflang`, and known draft disagreements have one version-neutral metadata source.
- [x] The closed negative corpus lists every Phase 2.4 case id above; each case records category,
      pointer, and `ValidationRuleCode` only when present (null allowed for `MALFORMED_JSON` /
      codec-only failures); source-location checks are capability-scoped.
- [x] Shared ambiguous object and empty-array cases assert dual-success models under both
      `PrimaryDataKind` values and are not classified as failure fixtures.
- [x] No Jackson-major-specific expected JSON or diagnostic corpus exists; fixture/catalog integrity
      tests reject missing, duplicate, unclassified, or inconsistent entries; fixture guidance and
      conformance traceability describe capability selection and intentional asymmetry.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
