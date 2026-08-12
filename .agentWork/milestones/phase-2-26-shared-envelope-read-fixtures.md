# Phase 2.26 — Shared Typed Envelope Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `DomainDocumentReaderSpec`  
> **Dependencies:** Phases 2.10, 2.11, 2.12, 2.13, 2.14, 2.27, and 2.28  
> **Status:** Not started

## Goal

Extract one version-neutral typed-envelope scenario catalog from Phase 2.10 tests for cross-major
parity without forcing every codec fixture into DTO binding.

## Research and constraints

- Phase 2.10 `DomainDocumentReaderSpec` uses a capability-relevant subset of canonical documents
  plus named binding variants. Applicability is owned entirely by this catalog's explicit
  reference list of codec scenario ids / named binding variants; it does not require a Phase 2.12
  `DOMAIN_BIND` capability flag. Codec scenarios are available transitively via Phase 2.28 (which
  depends on Phases 2.12 and 2.27); no Phase 2.12 capability flag is required.
- The catalog consumes shared DTOs from the Phase 2.13-owned
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` package (notably `Person`, bound by eight
  of the shared scenarios), available transitively via Phase 2.28.
- Phase 2.14 owns flat-binding DTOs; this milestone composes them into envelope scenarios.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade; the
  catalog is `EnvelopeReadScenarios` in the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.enveloperead` under `src/main/java/` (`@NullMarked` per
  ADR-009), exposing the `FixtureCatalog<EnvelopeReadScenario>` contract through the Phase 2.28
  pinned static delegation surface and registered as
  `JsonApiFixtures.envelopeRead()`. The catalog grows by addition with stable ids; adapter suites
  run the whole catalog and assert full coverage (`executedScenarioIds == catalogScenarioIds`) per
  the Phase 2.13 relaxed contract — no exclusion manifest. The closed shared test names below are
  the initial inventory; adapter-local cases stay in `DomainDocumentReaderSpec` and later adapter
  specs, documented there.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — included resources bind independently
  and are never injected into relationships.
- Closed shared `DomainDocumentReaderSpec` test names (initial inventory):
  `binds a single-resource document into a flat DTO envelope`;
  `binds a homogeneous resource collection in wire order`;
  `binds a heterogeneous collection through the registry`;
  `preserves explicit null data as NullData`;
  `preserves absent data on a meta-only document`;
  `passes through identifier primary data without DTO binding`;
  `preserves errors without binding anything`;
  `preserves jsonapi object, nullable links, and additional members`;
  `absent included stays null while present-empty included is a non-null empty IncludedResources`;
  `binds included resources preserving wire order with identity lookup`;
  `compound shared identity binds one included DTO reachable from both primary resources`;
  `shared identity yields one DTO instance reachable from both id and lid keys`;
  `fromDocument fails fast on duplicate included identities`;
  `unregistered resource-shaped primary fails at the document pointer with null resourceClass`;
  `unregistered included type fails at the included index`;
  `duplicate registry type names fail at build with the later registrant`;
  `registration rejects missing, empty, and invalid resource annotations`;
  `binder failures surface with the document pointer joined to the binder path`;
  `root-level binder failures join to the document pointer without a trailing slash`;
  `cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate`;
  `independent envelopes sharing linkage never inject included DTOs`;
  `reader-derived envelope collections are mutation-safe`.
- Adapter-local cases by exact name stay in the adapter spec (`DomainDocumentReaderSpec` today),
  documented there with major-local harnesses and never enumerated in a shared manifest:
  `metaAs returns null for both overloads when meta is absent`;
  `metaAs converts via the caller-mapper module on both entry paths and both overloads`;
  `incompatible metaAs target is UNSUPPORTED_ATTRIBUTE_VALUE at /meta`;
  `JavaType registrations bind through the same registry gate`;
  `builder-based domainDocumentReader overloads derive readers that bind identically`;
  `custom linkage mappers apply to primary and included resources`;
  `caller-owned stream and parser remain open on success and failure`;
  `malformed input stays JsonApiDocumentReadException with category and location`;
  `validation failures keep the originating rule code`.

## Deliverables

- Add `EnvelopeReadScenarios` and `EnvelopeReadScenario` (both in the fixed `enveloperead`
  package, `@NullMarked` with accurate `@Nullable` per ADR-009; `EnvelopeReadScenario` implements
  `Scenario`; `EnvelopeReadScenarios` follows the Phase 2.28 pinned delegation surface — public
  static `all()`, `byId(String)`, and `where(Predicate)` plus a `catalog()` accessor —
  registered as `JsonApiFixtures.envelopeRead()`) covering the initial inventory above, referencing listed Phase 2.12 codec scenarios
  (renamed `CodecScenarios` in Phase 2.28) or named binding variants explicitly inside the
  catalog, with expected envelope values, registration outcomes, included order, dual id/lid
  lookup, and no-injection proofs. Each `EnvelopeReadScenario` pins its descriptor shape: one stable id; an entry-point
  (transport) discriminator — wire-read via `readValue` or raw-document bind via `fromDocument`,
  required because `fromDocument fails fast on duplicate included identities` is not expressible
  through `readValue` (wire validation fails first) and the `fromDocument`/multi-input tests need
  it; zero or more input documents (a referenced codec scenario id, or a named binding-variant
  document from the inventory below), with a per-input discriminated expectation for multi-input
  scenarios (each input's expected diagnostic or bound values are pinned individually, as the
  three-input binder-failure scenario requires); the target DTO `Class`(es); a reader-context
  discriminator; and a discriminated expectation — expected envelope values
  (primary data, included order and identity, errors, jsonapi object, links, meta, with
  absent versus null states `@Nullable`), registration outcomes, or a failure diagnostic joined to
  the document pointer — all version-neutral core/common values. A mutation-safety expectation
  variant asserts the envelope's collections reject mutation (`UnsupportedOperationException`),
  carrying the `reader-derived envelope collections are mutation-safe` scenario. Registry-level
  scenarios
  (`duplicate registry type names fail at build with the later registrant`, `registration rejects
  missing, empty, and invalid resource annotations`) use a registry variant carrying the target
  classes and per-group expected registration outcomes — for the three-case registration
  scenario: `MISSING_RESOURCE_ANNOTATION` for the missing-annotation target and
  `INVALID_RESOURCE_TYPE` for each of the two invalid-annotation targets, each with its
  `resourceClass` — with no input documents and no reader-context discriminator. Multi-input
  scenarios carry all
  their input documents in one scenario (one executed id per scenario, not per input). The
  reader-context discriminator is pinned for Phase 2.22 parity: codec-fixture-referencing inputs
  derive the context from the referenced codec scenario's own validation context and primary-data
  kind with the `PrimaryDataKind.RESOURCE` fallback for null-primary-kind fixtures
  (`DocumentReadContext.of(codecScenario.context(), codecScenario.primaryDataKind() != null
  ? codecScenario.primaryDataKind() : PrimaryDataKind.RESOURCE)` — `extension-and-at-members` and
  `jsonapi-object` therefore read with their `extContext()`-backed contexts, while `null-data`,
  `meta-only`, and `errors-document` fall back to `RESOURCE`), except the identifier pass-through
  scenarios (`single-identifier`, `identifier-collection`), which pin
  `DocumentReadContext.identifierDefaults()` since their codec fixtures are `RESOURCE_IDENTIFIER`
  kind; binding-variant inputs resolve to `DocumentReadContext.resourceDefaults()` by default
  (the `SINGLE_RESOURCE`, `HETEROGENEOUS_COLLECTION`, `AT_MEMBER_DOCUMENT`, binder-failure,
  root-level, and cyclic inputs all read this way per the spec evidence), with
  `DocumentReadContext.identifierDefaults()` only for the identifier pass-through scenario;
  `fromDocument`-entry inputs pin `DocumentReadContext.resourceDefaults()` (the spec's `newReader`
  path). The rule resolves per input: mixed-input scenarios apply it to each input individually —
  the `preserves jsonapi object, nullable links, and additional members` scenario derives the
  codec-fixture context for its three codec inputs and `resourceDefaults()` for its
  `AT_MEMBER_DOCUMENT` variant. The
  binding-variant documents are exhaustively enumerated with stable names — `SINGLE_RESOURCE`,
  `HETEROGENEOUS_COLLECTION`, `AT_MEMBER_DOCUMENT`, the unregistered-primary single and
  collection inputs, the three binder-failure inputs, the root-level-failure input, the
  cyclic-linkage input, and wire forms of the four programmatic `fromDocument` inputs (every
  non-codec wire input in the closed inventory receives a named variant) — and reside as
  version-neutral documents under the exact path `fixtures/jsonapi-1.1/envelope-binding/`
  (envelope-binding directory), covered
  by the same id-stability rule as the codec corpus, so catalog integrity has a concrete
  resolvability target and Phase 2.22 consumes the same document set. `fromDocument`-entry
  scenarios carry the version-neutral core `JsonApiDocument` value directly (the
  `CodecFixture.document` precedent; test-fixtures already depends on core), with their wire
  forms serving as resolvability and documentation targets: only the duplicate-identity wire form
  is validation-invalid (it cannot pass the validated public read path), while the other wire
  forms are valid and route through `fromDocument` for uniformity of the entry-point contract.
  The duplicate-identity wire form is deliberately not registered in the Phase 2.12 negative
  corpus — it serves the `fromDocument` entry-point contract here, not the codec failure
  corpus.
- Move the envelope-only binding targets the shared scenarios need — `FlatNode`, `FlatStrictArticle`,
  `FlatThrowingArticle`, `EmptyResourceType`, `InvalidResourceType` — from
  `jsonapi-java-jackson3.testmodel` into `jsonapi-java-test-fixtures` with the `enveloperead`
  package (Jackson-neutral, `@Nullable` review of moved members, jackson3 references repointed);
  Phase 2.14's DTO move does not cover them. This move supersedes Phase 2.14's stay-local
  statement for `FlatStrictArticle` / `FlatThrowingArticle` (recorded there).
  The named adapter-local cases stay in the adapter
  spec. The registration-diagnostics scenario additionally needs a missing-annotation target: add
  a dedicated plain record without `@JsonApiResource` (for example `UnannotatedBindingTarget`) in
  the `enveloperead` package, because the current missing-annotation target `FlatAuthor` stays
  adapter-local per Phase 2.14.
- Refactor Jackson 3 `DomainDocumentReaderSpec` to consume the catalog with a full-catalog coverage
  assertion (`executedScenarioIds == catalogScenarioIds`) while retaining adapter-local
  configuration cases.
- Add catalog integrity tests for unique ids, resolvable documents/variants, and the
  `FixtureCatalog` contract.
- Use `module-docs` for the `jsonapi-java-test-fixtures` envelope-read package map and agent notes,
  and update the `fixtures/jsonapi-1.1/README.md` layout for the new
  `fixtures/jsonapi-1.1/envelope-binding/` directory. The milestone lands as two sequential
  reviewable commits (the Phase 2.14 precedent), verified as one unit by the milestone review:
  commit 1 = the envelope-binding corpus documents, the DTO moves plus repoints, and the
  `UnannotatedBindingTarget` with a green build; commit 2 = the `EnvelopeReadScenarios` catalog,
  the `DomainDocumentReaderSpec` refactor, and the docs/module-docs edits.

## Non-goals

- Flat binder extraction (Phase 2.14) or Jackson 2 envelope implementation (Phase 2.22).
- Graph hydration, relationship injection, or PATCH fixtures.
- Extending Phase 2.12 capability metadata with a domain-bind flag.
- Re-cataloging malformed/validation codec failures owned by Phase 2.12.
- Closed catalog indexes or adapter-local exclusion manifests; the catalog grows by addition.

## Implementation boundaries

- Envelope scenarios reuse common envelope/domain-data contracts, Phase 2.14 DTOs, and the five
  envelope-only binding targets this milestone moves into the `enveloperead` package.
- Absent versus empty `included` remains distinct; identifier primary data stays core values;
  errors never bind.
- Applicability is this catalog's reference list of codec scenario ids / binding variants, not a
  codec-fixture capability tag.

## Test strategy

- Parameterize the initial inventory through Jackson 3 and compare complete values and
  diagnostics; assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`).
- Catalog integrity rejects unresolvable expectations; the initial inventory is pinned to the
  closed shared test names above.

## Acceptance criteria

- [ ] The initial `EnvelopeReadScenarios` catalog exposes the
      `FixtureCatalog<EnvelopeReadScenario>` contract through the Phase 2.28 pinned static
      delegation surface
      (`enveloperead` package, `@NullMarked` with accurate `@Nullable` per ADR-009,
      `JsonApiFixtures.envelopeRead()` registered), covers the closed shared
      `DomainDocumentReaderSpec` test names, and references applicable codec scenario ids / named
      binding variants explicitly; the five envelope-only binding targets plus the dedicated
      missing-annotation target move into `jsonapi-java-test-fixtures` with jackson3 references
      repointed, and the named adapter-local cases remain in the adapter spec with no shared
      manifest.
- [ ] Jackson 3 `DomainDocumentReaderSpec` consumes the catalog for those shared names and asserts
      `executedScenarioIds == catalogScenarioIds`.
- [ ] Shared expectations preserve registration, included order/identity, pointer composition, and
      the no-injection boundary; the initial inventory is pinned to the closed shared test names
      above and catalog integrity rejects unresolvable expectations.
- [ ] The canonical `module-docs` checklist passes for `jsonapi-java-test-fixtures` envelope-read
      docs, and the `fixtures/jsonapi-1.1/README.md` layout documents the new
      `fixtures/jsonapi-1.1/envelope-binding/` directory with its stable binding-variant names.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
