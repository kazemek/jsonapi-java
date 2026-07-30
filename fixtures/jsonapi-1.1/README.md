# JSON:API 1.1 writer fixtures

Version-neutral expected wire JSON for document writers. Jackson 3 and (later) Jackson 2 contract
tests share this corpus; do not fork major-specific copies of these files.

## Layout

| Path                       | Role                                                         |
|----------------------------|--------------------------------------------------------------|
| `manifest.json`            | Ordered index of fixture ids, expected JSON paths, and notes |
| `documents/*.json`         | Pretty-printed expected wire documents                       |
| `documents/*.compact.json` | Exact UTF-8 expectations for member-order cases              |

Model builders and validation contexts live in the internal Gradle module
`jsonapi-java-test-fixtures` (`io.github.kazemek.jsonapi.testfixtures.writer`), not in this tree.

## Adding a fixture

1. Add pretty expected JSON under `documents/`.
2. Add a row to `manifest.json` (same order as the catalog list).
3. Add a readable case class under
   `jsonapi-java-test-fixtures/.../writer/cases/` that returns `WriterFixture.of(...)`.
4. Register the case in `WriterFixtures` (explicit list; no classpath scanning).
5. If exact member order matters, add a `.compact.json` sibling and set `assertExactUtf8` /
   `exactUtf8Path` on the fixture.
6. Run `./gradlew :jsonapi-java-test-fixtures:test :jsonapi-java-jackson3:test`.
