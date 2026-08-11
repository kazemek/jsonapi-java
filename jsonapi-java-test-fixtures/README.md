# jsonapi-java-test-fixtures

Internal Groovy and Java module holding the shared fixture builders, capability metadata, catalog
loaders, and version-neutral [JSON:API 1.1 document corpus](../fixtures/jsonapi-1.1/README.md).
Not a published module; Jackson 3 (and later Jackson 2) contract tests consume it.

## Packages

| Package                                                        | Role                                                               |
|----------------------------------------------------------------|--------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.testfixtures.codec`                 | `CodecFixture` capability metadata, `CodecFixtures` catalog, `AmbiguousPrimaryDataCases`, manifest-backed `NegativeCodecCases`, `SchemaKind` / `SchemaDisagreement` |
| `io.github.kazemek.jsonapi.testfixtures.codec.cases`           | One fixture case class per corpus entry (explicit list; no classpath scanning) |
| `io.github.kazemek.jsonapi.testfixtures.domainwrite`           | Shared flat domain-to-resource write fixtures: annotated domain models plus the `DomainWriteScenarios` catalog and the `DomainWriteOperation` / `DomainWriteInput` / `DomainWriteOutcome` / `DomainWriteComparisonPolicy` value types |

## Minimal usage

This module is not published and its types are not a supported production/library API, but
`CodecFixtures`, `NegativeCodecCases`, `AmbiguousPrimaryDataCases`, and `DomainWriteScenarios`
are public in-repo entry points for adapter tests:

```groovy
fixture << CodecFixtures.readable()            // read / input-source / round-trip suites
fixture << CodecFixtures.writable()            // writer suites
fixture << CodecFixtures.schemaChecked()       // draft-schema cross-check suites
fixture << NegativeCodecCases.all()            // closed read-only negative corpus
fixture << AmbiguousPrimaryDataCases.all()     // dual-success ambiguous primary-data cases

DomainWriteScenarios.all()                     // flat write-mapping scenarios, catalog order
DomainWriteScenarios.byId("maps mutable POJO") // stable-id lookup
```

Test JVMs must have `jsonapi.fixtures.dir` pointing at `fixtures/jsonapi-1.1`; the
`jsonapi-java-library` convention plugin wires it (together with `jsonapi.schema.fixtures.dir`)
for every module.

## Non-goals

This module does not add wire expectations, diagnostics, or corpora per Jackson major — those
must stay version-neutral (see [ADR-007](../docs/adr/007-module-boundaries.md)). Flat domain
read, compound, sparse-fieldset, typed-envelope, and PATCH fixture catalogs belong to later
fixture phases (2.14–2.15, 2.24–2.26); the flat write catalog is complete as of Phase 2.13.

## Further reading

- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
- [Conformance checklist](../docs/conformance.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Stable ids and paths:** `CodecFixture` ids and expected JSON paths are stable across Jackson
  majors; never fork or rewrite expected wire documents for new terminology. `manifest.json`
  remains the ordered index and `CodecFixturesCatalogSpec` enforces the bijection.
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
- **Capability selection:** Tests select by capability (`writable`, `readable`, `schemaKind`,
  `primaryDataKind`, `assertExactUtf8`, `assertHreflangArray`, `schemaDisagreement`) instead of
  maintaining independent hard-coded id lists. Adapter write suites dispatch on the
  `DomainWriteOperation`/`DomainWriteInput` descriptor, never on scenario ids.
- **Negative corpus:** `NegativeCodecCases` loads `negative-manifest.json`; the closed case set is
  enforced by `NegativeCodecCasesCatalogSpec`. Category and rule-code values are manifest strings;
  adapters map them onto their own enums.
- **Ambiguous primary data:** `AmbiguousPrimaryDataCases` holds both expected models per case;
  these are valid dual-success documents, never failure fixtures.
- **Major-neutral boundary:** production types under `io.github.kazemek.jsonapi.testfixtures..`
  never depend on `tools.jackson..`, `com.fasterxml.jackson.databind..`, a major-specific adapter
  package, or `core.internal..`; `TestFixturesDependencyRulesSpec` (ArchUnit, per ADR-010)
  enforces this on main bytecode — do not replace it with a source-import scan.
- **Null-bearing write models:** `Article.author`, `Comment.author`, `Person.name`,
  `Comment.body`, and `SamplePojo.{id, name, comments}` are `@Nullable` under the `@NullMarked`
  `domainwrite` package (ADR-009); expected outcomes still hold non-null core values unless the
  scenario exercises an explicit null state.
- **Extension workflow (Jackson 2):** a new adapter suite runs every scenario of the shared
  domain-write catalog through its own resource mapper and asserts full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`) exactly like the Jackson 3 suite (mandatory per
  Phase 2.18); Jackson-API-specific behavior (mix-ins, serializers, naming strategies, converter
  wiring) stays in adapter-local specs, documented there.
- **Nullness:** Groovy sources are not annotated (ADR-009 applies to Java production packages).
