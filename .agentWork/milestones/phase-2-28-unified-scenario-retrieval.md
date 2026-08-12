# Phase 2.28 — Unified Scenario Retrieval

> **Scope:** `jsonapi-java-test-fixtures` main sources and all fixture consumers (jackson3 specs, test-fixtures catalog specs)  
> **Dependencies:** Phases 2.12, 2.13, and 2.27  
> **Status:** Not started

## Goal

Give every adapter suite one vocabulary, one retrieval surface, and one centralized
fixtures-directory resolution for all shared fixtures: a common `Scenario` contract, a generic
`FixtureCatalog<T>`, uniformly named `<Area>Scenarios` catalogs, a single `JsonApiFixtures`
facade, and the shared `FixtureDirectory`. The directory centralization is atomic with the
vocabulary work: its eight property-read sites live in exactly the files this milestone touches
(the renamed catalog specs, the migrated jackson3 specs, and `NegativeCodecCases`).

## Research and constraints

- Four catalogs expose the same retrieval shape (`all()`/`byId(String)`) under four different
  vocabularies: `CodecFixtures` (capability flags), `NegativeCodecCases`, `AmbiguousPrimaryDataCases`,
  and the already-Java `DomainWriteScenarios` (operation/input/outcome descriptors). Phase 2.27
  converts the codec branch to Java with names unchanged; this milestone performs the unification
  design on top.
- Unified term is `Scenario` (domain-write vocabulary; "case"/"fixture" are dropped). Entry types
  become `CodecScenario`, `NegativeCodecScenario`, `AmbiguousPrimaryDataScenario`; catalogs become
  `CodecScenarios`, `NegativeCodecScenarios`, `AmbiguousPrimaryDataScenarios`; the 26 codec case
  builder classes become `…Scenario` (`SingleResourceScenario`, …). `SchemaKind`,
  `SchemaDisagreement`, and `Models` keep their names (enums/helpers are not entry types).
  `DomainWriteScenario(s)` implement the contract unchanged.
- Phase 2.12's stability rule applies to ids and expected wire paths only; type names are free to
  change. Adapter consumer specs change in imports, entry-type declarations, and accessors only,
  never in scenario semantics; the renamed test-fixtures catalog specs additionally gain the new
  assertion coverage listed in the test strategy (unknown-id messages, `where` predicates,
  notes-vs-manifest).
- Capability selection becomes generic filtering: the codec catalog keeps its five capability
  conveniences (`writable()`, `readable()`, `exactUtf8()`, `hreflangArray()`, and the derived
  `schemaChecked()`) as static delegations over `where`, so migrated call sites and
  `CodecFixturesCatalogSpec`'s capability non-emptiness assertion change names only; the `where`
  predicate itself is
  exercised by the renamed catalog specs (`where(CodecScenario::writable)` for flags,
  `where(s -> s.schemaKind() != null)` for schema-checked since schema kind is a component, not a
  boolean flag). `DomainWriteOperation`-based dispatch in `ResourceMapperSpec` is scenario content
  and stays untouched.
- Catalog accessor shape is pinned: each catalog class implements its own small delegation
  surface — public static `all()`, `byId(String)`, and `where(Predicate)` entry points (plus the
  capability conveniences below) delegating to a private `FixtureCatalog<T>` instance — and
  exposes that instance through a public static `catalog()` accessor so the facade (which lives
  in the root package and cannot reach package-private members of the `codec`/`domainwrite`
  packages) returns the same instance. The public statics are delegation shims kept for the
  untouched domain-write consumers (`ResourceMapperSpec`, `DomainWriteScenariosCatalogSpec`);
  the facade plus the shared contract is the canonical retrieval surface for migrated and future
  consumers. Migrated consumers (the 5 jackson3 specs and the 3 codec catalog specs, renamed with
  the vocabulary: `CodecScenariosCatalogSpec`, `NegativeCodecScenariosCatalogSpec`,
  `AmbiguousPrimaryDataScenariosCatalogSpec`) repoint to the renamed static catalogs — accessor
  swaps only; the facade is exercised by the facade spec. Future catalogs may either delegate
  through static shims (the shape chosen for the four existing catalogs, required to keep the
  untouched static consumers working) or implement `FixtureCatalog` directly (the shape the
  dependent fixture milestones 2.14/2.15/2.24–2.26 already plan); the facade registers either as
  the `FixtureCatalog` view.
- `jsonapi.fixtures.dir` and `jsonapi.schema.fixtures.dir` are wired by the
  `jsonapi-java-library` plugin but currently read inline at 8 sites:
  `NegativeCodecCases.groovy` (main), `CodecFixturesCatalogSpec`,
  `NegativeCodecCasesCatalogSpec`, `AmbiguousPrimaryDataCasesCatalogSpec`,
  `JsonApiDraftSchemaSpec` (schema dir), and `DocumentWriterContractSpec`,
  `DocumentReaderSpec`, `DomainDocumentReaderSpec`; a shared `FixtureDirectory` centralizes
  resolution of all 8 with clear errors.
- Future fixture milestones (2.14, 2.15, 2.24–2.26) add their catalogs to this contract and
  register a facade accessor instead of inventing retrieval surfaces.
- The unified message format changes the existing `DomainWriteScenarios.byId` diagnostic (today
  `Unknown domain-write scenario: <id>`) to `Unknown domain-write scenario id: <id>`; the
  untouched `DomainWriteScenariosCatalogSpec` asserts only the exception type, so this is a safe,
  intended behavior change covered by the facade spec's verbatim assertion.
- The `module-docs` skill applies: public in-repo entry points change.

## Deliverables

- Add the common contract in `io.github.kazemek.jsonapi.testfixtures`: `Scenario` (`id()`, and a
  `default notes()` returning the id, so `DomainWriteScenario` — which has no notes component —
  implements the contract unchanged) and `FixtureCatalog<T extends Scenario>` (`all()`,
  `byId(String)`, `where(Predicate<? super T>)` returning an immutable `List<T>`, so Spock data
  pipes and Groovy collection chains on filtered results migrate as accessor swaps). Unknown
  `byId` ids throw `IllegalArgumentException` with the single shared message format
  `Unknown <area> scenario id: <id>`, with the four area labels pinned as
  `Unknown codec scenario id:`, `Unknown negative-codec scenario id:`,
  `Unknown ambiguous-primary-data scenario id:`, and `Unknown domain-write scenario id:`
  (kebab-case catalog name minus the `Scenarios` suffix); the renamed catalog specs and the
  facade spec assert these verbatim.
- Rename the codec entry types and catalogs to the `Scenario` vocabulary listed above, rename the
  26 `…Case` builder factory methods from `fixture()` to `scenario()` (no `fixture()` method
  survives; the corpus README's `CodecFixture.of(...)` workflow text is rewritten to the
  surviving factory shape), and make
  `DomainWriteScenario` implement `Scenario`; keep fixture ids, expected paths, notes, and
  capabilities identical, and keep `toString()` returning the scenario id on the renamed record
  types (Spock data-pipe iteration names use it).
- Add the `JsonApiFixtures` facade exposing `codec()`, `negativeCodec()`,
  `ambiguousPrimaryData()`, and `domainWrite()` as typed `FixtureCatalog` accessors; capability
  selection works through `where`.
- Add `FixtureDirectory` resolving `jsonapi.fixtures.dir` / `jsonapi.schema.fixtures.dir` with
  clear errors, and migrate main-source and spec property reads onto it. A missing
  `jsonapi.fixtures.dir` fails with `IllegalStateException` and the pinned message
  `System property jsonapi.fixtures.dir must point at fixtures/jsonapi-1.1` (preserving the
  Phase 2.27 loader message it supersedes), and a missing `jsonapi.schema.fixtures.dir` fails
  with the analogous message naming `fixtures/jsonapi-schema/1.1-pr1603`.
- Migrate all consumers — the 5 jackson3 specs (`DocumentWriterContractSpec`,
  `JsonApiDraftSchemaSpec`, `DocumentReaderSpec`, `DocumentWriterSinkSpec`,
  `DomainDocumentReaderSpec`) and the 3 renamed codec catalog specs — onto the renamed
  static catalogs; update `jsonapi-java-test-fixtures/README.md` package map and agent notes via
  the `module-docs` skill, including the `docs/conformance.md`, `fixtures/jsonapi-1.1/README.md`,
  and `package-info.java` doc-sweep (the new root-package `package-info.java` and the codec
  package-infos created by Phase 2.27) so no `CodecFixture`/`…Cases` vocabulary remains in those
  documentation paths.

## Non-goals

- Changing scenario content, fixture ids, wire documents, or manifests.
- Touching `DomainWriteOperation`/`DomainWriteInput` dispatch or the full-catalog coverage
  assertions in adapter suites.
- Converting Spock test sources to Java.
- Designing the future catalogs themselves (2.14, 2.15, 2.24–2.26 own them); this milestone only
  establishes the contract they implement.

## Implementation boundaries

- The contract types (`Scenario`, `FixtureCatalog`), `FixtureDirectory`, and `JsonApiFixtures`
  live in the version-neutral `io.github.kazemek.jsonapi.testfixtures` root package; the renamed
  entry types stay in their current packages (`codec`, `codec.cases`, `domainwrite`). The ArchUnit
  allowlist already covers `io.github.kazemek.jsonapi.testfixtures..`.
- `FixtureDirectory` supersedes the loader-internal directory resolution introduced by Phase 2.27
  and is the single owner of the missing-property error; spec property reads migrate onto it.
- Facade accessors return `FixtureCatalog<T>` views; catalogs remain the immutable data owners.
- Main sources stay Java-only `@NullMarked` (Phase 2.27 rule); no `groovy..` /
  `org.codehaus.groovy..` allowlist entries are reintroduced.

## Test strategy

- The migrated catalog specs remain the contract: bijection with manifests, unique ids, `byId`
  identity, `CodecFixturesCatalogSpec`'s capability non-emptiness assertion (through the retained
  static conveniences), the
  capability-predicate equivalences (`CodecScenarios.where(CodecScenario::writable) ==
  CodecScenarios.writable()` and `where(s -> s.schemaKind() != null) ==
  CodecScenarios.schemaChecked()`), invariant checks,
  and rejection of unknown `byId` ids with the unified message pass under the new names. The three
  renamed catalog specs — `CodecScenariosCatalogSpec`, `NegativeCodecScenariosCatalogSpec`, and
  `AmbiguousPrimaryDataScenariosCatalogSpec` — each gain the unknown-id message assertion and a
  `where`-shim test (no codec catalog spec tests unknown ids today; the untouched
  `DomainWriteScenariosCatalogSpec` asserts only the exception type).
- A small facade spec asserts the four typed accessors return the catalog views
  (`JsonApiFixtures.domainWrite().all() == DomainWriteScenarios.all()`, and the same for the
  codec family), that each accessor returns the same instance as the catalog's `catalog()`
  accessor (`JsonApiFixtures.codec().is(CodecScenarios.catalog())` per family), that every catalog's `where` shim works
  (`DomainWriteScenarios.where({ true })*.id == DomainWriteScenarios.all()*.id` and the
  equivalent for the codec catalogs), that every `DomainWriteScenarios.all()` entry satisfies the
  `Scenario.notes()` default contract (`notes() == id()`), and that the domain-write
  unknown-`byId` failure carries the unified message verbatim — covering the `domainWrite()`
  accessor and the domain-write `where` static that the untouched domain-write consumers do not
  exercise.
- The jackson3 suites pass unchanged in behavior; `ResourceMapperSpec` keeps asserting
  `executedScenarioIds == catalogScenarioIds`.
- `FixtureDirectory` failure paths are covered (missing property produces a clear error).

## Acceptance criteria

- [ ] `Scenario` and `FixtureCatalog<T>` exist in `io.github.kazemek.jsonapi.testfixtures`; all
      four catalogs expose the contract (public static `all()`, `byId(String)`, and
      `where(Predicate)` delegating to a private `FixtureCatalog` instance reachable via a public
      static `catalog()` accessor) and every entry has an `id()`; unknown `byId` ids fail with
      `IllegalArgumentException` carrying the shared message format, asserted verbatim by all
      three renamed catalog specs and by the facade spec for domain-write.
- [ ] All renames are complete: no references to `CodecFixture`, `CodecFixtures`,
      `NegativeCodecCase(s)`, `AmbiguousPrimaryDataCase(s)`, or the `…Case` builders remain in
      main sources, test sources, or the deliverable-5 documentation paths (test-fixtures module
      README, `docs/conformance.md`, `fixtures/jsonapi-1.1/README.md`, and the changed
      `package-info.java` files); historical milestone files under `.agentWork/` are exempt
      (completed milestones are permanent and legitimately document the old vocabulary); the
      three catalog spec classes are renamed to the new vocabulary; fixture ids, expected paths,
      notes, and capability semantics are unchanged (bijection and capability specs pass, and a
      catalog-spec assertion iterates every codec entry's notes against `manifest.json` as an
      independent cross-check — the negative corpus loads its notes from the manifest, so that
      side is identity, not a cross-check — while the ambiguous catalog's notes are preserved
      as-is, legitimately diverging from `ambiguous-manifest.json`, which serves the spec's
      independent cross-check only).
- [ ] `JsonApiFixtures` exposes the four typed catalog accessors and capability filtering works
      via `where`.
- [ ] `FixtureDirectory` centralizes both directory properties; main sources and specs read
      `jsonapi.fixtures.dir` / `jsonapi.schema.fixtures.dir` only through `FixtureDirectory` — no
      other direct `System.getProperty` reads of those two properties remain; missing-property
      errors are tested.
- [ ] All consumers are migrated (5 jackson3 specs, 3 catalog specs); `ResourceMapperSpec`'s
      operation dispatch and full-catalog coverage assertion are intact.
- [ ] Main sources remain Java-only `@NullMarked`; ArchUnit and NullAway pass with no `groovy..`
      or `org.codehaus.groovy..` allowlist entries.
- [ ] The canonical `module-docs` checklist passes for the changed test-fixtures package map,
      entry points, and the new root-package contract types.
- [ ] `./gradlew clean build` passes; Spotless passes (`./gradlew spotlessApply` then
      `./gradlew spotlessCheck`); Sonar Quality Gate passes — if `SONAR_TOKEN` is unavailable,
      report Sonar blocked and that CI must still pass the gate.
