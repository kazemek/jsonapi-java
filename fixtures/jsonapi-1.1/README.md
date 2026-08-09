# JSON:API 1.1 document fixtures

Version-neutral expected wire JSON for document codecs. Jackson 3 and (later) Jackson 2 contract
tests share this corpus; do not fork major-specific copies of these files. Every applicable adapter
runs the same capability-selected catalog: writer-only, reader-only, schema-only, and asymmetric
cases (exact bytes, usage context, parser location) are tagged rather than forced into false
symmetry.

## Layout

| Path                       | Role                                                         |
|----------------------------|--------------------------------------------------------------|
| `manifest.json`            | Ordered index of fixture ids, expected JSON paths, and notes |
| `documents/*.json`         | Pretty-printed expected wire documents                       |
| `documents/*.compact.json` | Exact UTF-8 expectations for member-order cases              |

Model builders, capability metadata, and validation contexts live in the internal Gradle module
`jsonapi-java-test-fixtures` (`io.github.kazemek.jsonapi.testfixtures.writer`), not in this tree.
Phase 2.12 generalizes that catalog for write/read/schema selection and shared read-only negatives.

## Adding a fixture

1. Add pretty expected JSON under `documents/`.
2. Add a row to `manifest.json` (same order as the catalog list).
3. Add a readable case class under
   `jsonapi-java-test-fixtures/.../writer/cases/` that returns `WriterFixture.of(...)` (or the
   Phase 2.12 successor type) with the applicable capabilities.
4. Register the case in `WriterFixtures` (explicit list; no classpath scanning).
5. If exact member order matters, add a `.compact.json` sibling and set `assertExactUtf8` /
   `exactUtf8Path` on the fixture.
6. Run `./gradlew :jsonapi-java-test-fixtures:test :jsonapi-java-jackson3:test`.
