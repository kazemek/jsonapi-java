# jsonapi-java-test-fixtures

Internal Groovy module holding the shared fixture builders, capability metadata, and catalog
loaders for the version-neutral [JSON:API 1.1 document corpus](../fixtures/jsonapi-1.1/README.md).
Not a published module; Jackson 3 (and later Jackson 2) contract tests consume it.

## Packages

| Package                                                        | Role                                                               |
|----------------------------------------------------------------|--------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.testfixtures.codec`                 | `CodecFixture` capability metadata, `CodecFixtures` catalog, `AmbiguousPrimaryDataCases`, manifest-backed `NegativeCodecCases`, `SchemaKind` / `SchemaDisagreement` |
| `io.github.kazemek.jsonapi.testfixtures.codec.cases`           | One fixture case class per corpus entry (explicit list; no classpath scanning) |

## Minimal usage

This module is not published and its types are not a supported production/library API, but
`CodecFixtures`, `NegativeCodecCases`, and `AmbiguousPrimaryDataCases` are public in-repo entry
points for adapter tests, which select cases by capability:

```groovy
fixture << CodecFixtures.readable()            // read / input-source / round-trip suites
fixture << CodecFixtures.writable()            // writer suites
fixture << CodecFixtures.schemaChecked()       // draft-schema cross-check suites
fixture << NegativeCodecCases.all()            // closed read-only negative corpus
fixture << AmbiguousPrimaryDataCases.all()     // dual-success ambiguous primary-data cases
```

Test JVMs must have `jsonapi.fixtures.dir` pointing at `fixtures/jsonapi-1.1`; the
`jsonapi-java-library` convention plugin wires it (together with `jsonapi.schema.fixtures.dir`)
for every module.

## Non-goals

This module does not add wire expectations, diagnostics, or corpora per Jackson major — those
must stay version-neutral (see [ADR-007](../docs/adr/007-module-boundaries.md)). Domain mapping,
compound, sparse-fieldset, DTO, envelope, and PATCH fixture models belong to later fixture phases
(2.13–2.15, 2.24–2.26).

## Further reading

- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
- [Conformance checklist](../docs/conformance.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Stable ids and paths:** `CodecFixture` ids and expected JSON paths are stable across Jackson
  majors; never fork or rewrite expected wire documents for new terminology. `manifest.json`
  remains the ordered index and `CodecFixturesCatalogSpec` enforces the bijection.
- **Capability selection:** Tests select by capability (`writable`, `readable`, `schemaKind`,
  `primaryDataKind`, `assertExactUtf8`, `assertHreflangArray`, `schemaDisagreement`) instead of
  maintaining independent hard-coded id lists.
- **Negative corpus:** `NegativeCodecCases` loads `negative-manifest.json`; the closed case set is
  enforced by `NegativeCodecCasesCatalogSpec`. Category and rule-code values are manifest strings;
  adapters map them onto their own enums.
- **Ambiguous primary data:** `AmbiguousPrimaryDataCases` holds both expected models per case;
  these are valid dual-success documents, never failure fixtures.
- **Nullness:** Groovy sources are not annotated (ADR-009 applies to Java production packages).
