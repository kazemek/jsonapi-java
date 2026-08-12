# jsonapi-java-test-fixtures

Internal Java module holding the shared scenario catalogs, fixture builders, and version-neutral
[JSON:API 1.1 document corpus](../fixtures/jsonapi-1.1/README.md). Not a published module; Jackson 3
(and later Jackson 2) contract tests consume it.

## Packages

| Package                                                        | Role                                                               |
|----------------------------------------------------------------|--------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.testfixtures`                       | `Scenario` / `FixtureCatalog` contract, `JsonApiFixtures` facade, and `FixtureDirectory` |
| `io.github.kazemek.jsonapi.testfixtures.codec`                 | `CodecScenario` capability metadata, `CodecScenarios` catalog, `AmbiguousPrimaryDataScenarios`, JSON-P-backed `NegativeCodecScenarios`, `SchemaKind` / `SchemaDisagreement` |
| `io.github.kazemek.jsonapi.testfixtures.codec.cases`           | One scenario builder class per corpus entry (explicit list; no classpath scanning) |
| `io.github.kazemek.jsonapi.testfixtures.domainwrite`           | Shared flat domain-to-resource write fixtures: annotated domain models plus the `DomainWriteScenarios` catalog and the `DomainWriteOperation` / `DomainWriteInput` / `DomainWriteOutcome` / `DomainWriteComparisonPolicy` value types |

## Minimal usage

This module is not published and its types are not a supported production/library API, but
`JsonApiFixtures` is the canonical in-repo retrieval entry point (`FixtureCatalog` views; capability
selection is `where`). Concrete `*Scenarios` statics remain as compatibility shims:

```groovy
JsonApiFixtures.codec().where(CodecScenario::readable)
JsonApiFixtures.codec().where(CodecScenario::writable)
JsonApiFixtures.codec().where { it.schemaKind() != null }
JsonApiFixtures.negativeCodec().all()
JsonApiFixtures.ambiguousPrimaryData().all()
JsonApiFixtures.domainWrite().all()
JsonApiFixtures.domainWrite().byId("maps mutable POJO")
```

Test JVMs must have `jsonapi.fixtures.dir` pointing at `fixtures/jsonapi-1.1`; resolve both
directory properties through `FixtureDirectory`. The `jsonapi-java-library` convention plugin wires
them (together with `jsonapi.schema.fixtures.dir`) for every module.

## Non-goals

This module does not add wire expectations, diagnostics, or corpora per Jackson major — those
must stay version-neutral (see [ADR-007](../docs/adr/007-module-boundaries.md)). Flat domain
read, compound, sparse-fieldset, typed-envelope, and PATCH fixture catalogs belong to later
fixture phases (2.14–2.15, 2.24–2.26); the flat write catalog is complete as of Phase 2.13.

## Further reading

- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
- [Conformance checklist](../docs/conformance.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Retrieval:** `JsonApiFixtures` plus the `FixtureCatalog` instances it exposes is the canonical
  API. Future catalogs (2.14, 2.15, 2.24–2.26) register a facade accessor and the same public
  static `all()` / `byId(String)` / `where(Predicate)` / `catalog()` delegation surface; they do
  not invent retrieval types. Existing suites may keep calling the `*Scenarios` shims.
- **Stable ids and paths:** `CodecScenario` ids and expected JSON paths are stable across Jackson
  majors; never fork or rewrite expected wire documents for new terminology. `manifest.json`
  remains the ordered index and `CodecScenariosCatalogSpec` enforces the bijection.
  `DomainWriteScenarios` ids are stable and looked up via `byId(String)`; the catalog grows by
  addition.
- **Domain-write catalog:** `DomainWriteScenariosCatalogSpec` enforces the local invariants that
  hold for every entry regardless of catalog size: unique stable ids, exactly one
  operation/typed input/envelope state/discriminated outcome/comparison policy, complete expected
  outcomes, and valid policies (entries reference existing relationships; unordered comparison
  only for to-many linkage). Adapter suites run the whole catalog through their own mapper and
  assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`), so adding a
  scenario is a one-step action: add it to the catalog and the adapter suites pick it up
  automatically. Adapter-specific behavior (Jackson API surface, mapper-factory wiring) is
  documented in the adapter-local specs themselves, not enumerated in a manifest.
- **Capability selection:** Tests select by `FixtureCatalog.where` (and the retained
  `CodecScenarios` conveniences `writable`, `readable`, `schemaChecked`, `exactUtf8`,
  `hreflangArray`) instead of maintaining independent hard-coded id lists. Adapter write suites
  dispatch on the `DomainWriteOperation`/`DomainWriteInput` descriptor, never on scenario ids.
- **Directories:** Read `jsonapi.fixtures.dir` and `jsonapi.schema.fixtures.dir` only through
  `FixtureDirectory`.
- **Negative corpus:** `NegativeCodecScenarios` loads `negative-manifest.json` with JSON-P (Jakarta
  JSON Processing + Parsson); the closed case set is enforced by `NegativeCodecScenariosCatalogSpec`.
  Category and rule-code values are manifest strings; adapters map them onto their own enums.
  Catalog specs still cross-check the manifest independently via `JsonSlurper`.
- **Ambiguous primary data:** `AmbiguousPrimaryDataScenarios` holds both expected models per
  scenario; these are valid dual-success documents, never failure fixtures.
- **Major-neutral boundary:** production types under `io.github.kazemek.jsonapi.testfixtures..`
  never depend on `tools.jackson..`, `com.fasterxml.jackson.databind..`, a major-specific adapter
  package, `core.internal..`, or Groovy; `TestFixturesDependencyRulesSpec` (ArchUnit, per ADR-010)
  enforces this on main bytecode — do not replace it with a source-import scan. JSON-P
  (`jakarta.json..` / `org.eclipse.parsson..`) is allowed for the negative-manifest loader.
- **Null-bearing write models:** `Article.author`, `Comment.author`, `Person.name`,
  `Comment.body`, and `SamplePojo.{id, name, comments}` are `@Nullable` under the `@NullMarked`
  `domainwrite` package (ADR-009); expected outcomes still hold non-null core values unless the
  scenario exercises an explicit null state.
- **Nullness:** Production packages are `@NullMarked` (JSpecify). Use `@Nullable` for catalog
  members that are absent (`CodecScenario.primaryDataKind`, `schemaKind`, `schemaDisagreement`,
  `exactUtf8Path`; `NegativeCodecScenario.pointer`, `ruleCode`). Groovy tests are not annotated.
- **Extension workflow (Jackson 2):** a new adapter suite runs every scenario of the shared
  domain-write catalog through its own resource mapper and asserts full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`) exactly like the Jackson 3 suite (mandatory per
  Phase 2.18); Jackson-API-specific behavior (mix-ins, serializers, naming strategies, converter
  wiring) stays in adapter-local specs, documented there.
