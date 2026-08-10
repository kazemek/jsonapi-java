# JSON:API 1.1 document fixtures

Version-neutral expected wire JSON for document codecs. Jackson 3 and (later) Jackson 2 contract
tests share this corpus; do not fork major-specific copies of these files. Every applicable adapter
runs the same capability-selected catalog: writer-only, reader-only, schema-only, and asymmetric
cases (exact bytes, usage context, parser location) are tagged rather than forced into false
symmetry.

## Layout

| Path                           | Role                                                                                          |
|--------------------------------|-----------------------------------------------------------------------------------------------|
| `manifest.json`                | Ordered index of valid fixture ids, expected JSON paths, and notes                            |
| `ambiguous-manifest.json`      | Ordered index of shared dual-success ambiguous primary-data cases                             |
| `negative-manifest.json`       | Closed read-only negative corpus: inputs that must fail, with expected diagnostics            |
| `documents/*.json`             | Pretty-printed expected wire documents                                                        |
| `documents/*.compact.json`     | Exact UTF-8 expectations for member-order cases                                               |
| `negative/*.json`              | Read-only inputs for the negative corpus (malformed or context-invalid documents)             |

Model builders, capability metadata, and validation contexts live in the internal Gradle module
`jsonapi-java-test-fixtures` (`io.github.kazemek.jsonapi.testfixtures.codec`), not in this tree.
`CodecFixture` carries the capability metadata (write, read, schema kind, primary-data kind,
exact-byte policy, canonical `hreflang`, and known draft-schema disagreement), `CodecFixtures`
exposes capability selections, `AmbiguousPrimaryDataCases` holds the dual-success models, and
`NegativeCodecCases` loads the manifest-backed negative corpus.

## Capabilities

Every valid fixture declares the codec capabilities it may exercise; adapter tests select by
capability instead of maintaining independent hard-coded id lists:

- **write** (`writable`) — participates in writer suites.
- **read** (`readable`) — participates in reader, input-source, and round-trip suites.
- **schema kind** (`schemaKind`) — which pinned draft schema validates the written document;
  null means the fixture is not schema-checked.
- **primary-data kind** (`primaryDataKind`) — explicit `RESOURCE` / `RESOURCE_IDENTIFIER` read
  interpretation; null when the document has no primary data (or only kind-neutral data such as an
  explicit `null`).
- **exact bytes** (`assertExactUtf8` + `exactUtf8Path`) — a `.compact.json` sibling pins the
  exact UTF-8 member order.
- **canonical hreflang** (`assertHreflangArray`) — the writer always emits the array form.
- **known draft disagreement** (`schemaDisagreement`) — the fixture intentionally fails the draft
  schema for a recorded reason and must keep failing.

## Adding a fixture

1. Add pretty expected JSON under `documents/`.
2. Add a row to `manifest.json` (same order as the catalog list).
3. Add a readable case class under
   `jsonapi-java-test-fixtures/.../codec/cases/` that returns `CodecFixture.of(...)` with the
   applicable capabilities.
4. Register the case in `CodecFixtures` (explicit list; no classpath scanning).
5. If exact member order matters, add a `.compact.json` sibling and set `assertExactUtf8` /
   `exactUtf8Path` on the fixture.
6. If the document intentionally fails the pinned draft schema, record the disagreement in
   `schemaDisagreement` and keep the fixture failing.
7. Run `./gradlew :jsonapi-java-test-fixtures:test :jsonapi-java-jackson3:test`.

## Negative corpus

`negative-manifest.json` is the source of truth for the closed list of inputs that must fail to
read. Each case records the version-neutral expected failure category, JSON Pointer, and core
`ValidationRuleCode` — each only when present (codec-only failures such as `MALFORMED_JSON` have
null pointer or rule code) — plus a `sourceLocation` flag for assertions that are parser-specific
and must be tagged, not copied. Adapter tests map the category/rule-code strings onto their own
enums and keep any exact source-location details adapter-local. Never add or remove a negative case
id without updating the closed inventory in `NegativeCodecCasesCatalogSpec`.

## Ambiguous primary data

`ambiguous-manifest.json` lists the shared dual-success cases whose decoded model depends on the
explicit primary-data kind (object and empty-array forms). These are valid documents, not failure
fixtures: each adapter must prove both `PrimaryDataKind` readings succeed with the expected models.
