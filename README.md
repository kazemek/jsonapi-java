# jsonapi-java

> Read and write JSON:API v1.1 documents in Java without surrendering control of persistence, endpoints, or application architecture.

A lightweight [JSON:API v1.1](https://jsonapi.org/) document model and validation library for
**Java 21+**. Opt-in bidirectional flat DTO mapping, typed envelopes, and Jackson 3 presence-aware
PATCH binding (low-level commands and direct typed PATCH DTOs) are available; query parsing and
Spring adapters are planned. Compliance is tracked by feature and layer; the library does not claim
that an application's endpoint behavior is automatically JSON:API compliant.

## Status

**Pre-alpha.** The Gradle build, CI pipeline, architecture decisions, `jsonapi-java-core` document model and validation, `jsonapi-java-annotations`, Jackson 3 document codec and domain mapping (compound inclusion, sparse fieldsets, flat DTO binding, typed envelopes, presence-aware PATCH binding and direct typed PATCH DTO binding), and Jackson-major-neutral contracts in `jsonapi-java-jackson-api` are in place. Query parsing and Spring adapters are not started.

Maven group: `io.github.kazemek`. Java packages: `io.github.kazemek.jsonapi.*`.

## Requirements

- JDK 21 (enforced via Gradle toolchain)

## Build

```bash
./gradlew clean build
```

`check` (and therefore `build`) enforces per-module JaCoCo instruction and branch floors for
library modules. Numeric floors live only in
[`build-logic/src/main/kotlin/jsonapi-java-library.gradle.kts`](build-logic/src/main/kotlin/jsonapi-java-library.gradle.kts).
Re-measure with `./gradlew jacocoTestReport`, then set each floor to `floor(measuredPercent)`.
Intentional coverage drops must update that map in the same change.
`jsonapi-java-annotations` is skip-listed there because it is annotation-only (no instruction/branch
counters).

## Project structure

| Path                           | Purpose                                                                                          |
|--------------------------------|--------------------------------------------------------------------------------------------------|
| `jsonapi-java-core/`           | Zero-dependency JSON:API document model and validation                                           |
| `jsonapi-java-annotations/`    | Dependency-free domain-mapping annotations                                                       |
| `jsonapi-java-jackson3/`       | Jackson 3 document codec, domain-to-resource mapping, flat DTO reads, typed domain envelopes, and presence-aware PATCH |
| `jsonapi-java-jackson-api/`    | Public Jackson-major-neutral API surface: document, mapping, PATCH, representation, and diagnostic contracts |
| [`jsonapi-java-test-support/`](jsonapi-java-test-support/README.md) | Internal shared test-support: scenario catalogs, classpath JSON:API corpus, and pinned schema resources (not a published module) |
| `build-logic/`                 | Shared Gradle convention plugins                                                                 |
| `docs/`                        | Vision, architecture overview, conformance, and architecture decision records |

## Module registry

| Module                                                           | Status                                                                                                                                                                                  | Purpose                                                                    |
|------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| [`jsonapi-java-core`](jsonapi-java-core/README.md)               | Available | Dependency-free document model and validation                              |
| [`jsonapi-java-annotations`](jsonapi-java-annotations/README.md) | Available | Dependency-free domain-mapping role annotations                            |
| [`jsonapi-java-jackson3`](jsonapi-java-jackson3/README.md)       | Available | Jackson 3 document codec, annotated domain mapping, and presence-aware PATCH binding (commands and direct typed PATCH DTOs) |
| [`jsonapi-java-jackson-api`](jsonapi-java-jackson-api/README.md)       | Available | Public Jackson-major-neutral API surface shared by Jackson 2, Jackson 3, and future framework integrations |
| `jsonapi-java-jackson2`                                          | Planned   | Separately compiled Jackson 2 parity artifact                              |
| `jsonapi-java-query`                                             | Planned   | Optional query-parameter parsing                                           |
| `jsonapi-java-spring-webmvc`                                     | Planned   | Jackson 3-based Spring WebMVC transport and DTO binding                    |
| `jsonapi-java-spring-webflux`                                    | Future evaluation | Separately scoped reactive adapter candidate                               |

Planned and future-evaluation modules have no usable entry point yet. Use each available module
README for its package map, minimal usage, non-goals, and contributor/agent notes; the registry does
not duplicate those module-specific contracts.

## Documentation

- [Core module](jsonapi-java-core/README.md)
- [Annotations module](jsonapi-java-annotations/README.md)
- [Jackson 3 module](jsonapi-java-jackson3/README.md)
- [Jackson API module](jsonapi-java-jackson-api/README.md)
- [Vision](docs/vision.md) — stable product direction and principles
- [Architecture](docs/architecture.md) — current cross-module mental model and flows
- [Conformance checklist](docs/conformance.md) — current JSON:API 1.1 feature status
- [Architecture decision records](docs/adr/README.md)
- [Agent workflow](AGENTS.md) — knowledge ownership, routing, and completion gates

## License

Apache License 2.0 — see [LICENSE](LICENSE).
