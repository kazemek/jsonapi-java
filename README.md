# jsonapi-java

> Read and write JSON:API v1.1 documents in Java without surrendering control of persistence, endpoints, or application architecture.

A lightweight [JSON:API v1.1](https://jsonapi.org/) document model and validation library for
**Java 21+**. Opt-in bidirectional flat DTO mapping and typed envelopes are available; presence-aware
PATCH commands, query parsing, and Spring adapters are planned. Compliance is tracked by feature and
layer; the library does not claim that an application's endpoint behavior is automatically JSON:API
compliant.

## Status

**Pre-alpha.** The Gradle build, CI pipeline, architecture decisions, `jsonapi-java-core` document model and validation, `jsonapi-java-annotations`, Jackson 3 document codec and domain mapping (compound inclusion, sparse fieldsets, flat DTO binding, typed envelopes), and Jackson-major-neutral contracts in `jsonapi-java-jackson-common` are in place. Query parsing and Spring adapters are not started.

Maven group: `io.github.kazemek`. Java packages: `io.github.kazemek.jsonapi.*`.

## Requirements

- JDK 21 (enforced via Gradle toolchain)

## Build

```bash
./gradlew clean build
```

## Project structure

| Path                           | Purpose                                                                                          |
|--------------------------------|--------------------------------------------------------------------------------------------------|
| `jsonapi-java-core/`           | Zero-dependency JSON:API document model and validation                                           |
| `jsonapi-java-annotations/`    | Dependency-free domain-mapping annotations                                                       |
| `jsonapi-java-jackson3/`       | Jackson 3 document codec, domain-to-resource mapping, flat DTO reads, and typed domain envelopes |
| `jsonapi-java-jackson-common/` | Jackson-major-neutral policy, diagnostic, context, and envelope contracts                       |
| [`jsonapi-java-test-fixtures/`](jsonapi-java-test-fixtures/README.md) | Internal shared scenario catalogs and fixture builders, including the shared domain-write, domain-read, compound-write, and sparse-fieldset catalogs (not a published module) |
| `fixtures/jsonapi-1.1/`        | Version-neutral canonical JSON:API document fixtures for codec parity                            |
| `build-logic/`                 | Shared Gradle convention plugins                                                                 |
| `docs/`                        | Vision, Outlook, conformance, and architecture decision records                                  |
| `.agentWork/milestones/`       | Live execution plans (knowledge-model migration in progress)                                     |

## Module registry

| Module                                                           | Status                                                                                                                                                                                  | Purpose                                                                    |
|------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| [`jsonapi-java-core`](jsonapi-java-core/README.md)               | Available | Dependency-free document model and validation                              |
| [`jsonapi-java-annotations`](jsonapi-java-annotations/README.md) | Available | Dependency-free domain-mapping role annotations                            |
| [`jsonapi-java-jackson3`](jsonapi-java-jackson3/README.md)       | Available | Jackson 3 document codec and annotated domain mapping; PATCH binding not started |
| [`jsonapi-java-jackson-common`](jsonapi-java-jackson-common/README.md) | Available | Jackson-major-neutral public contracts shared by both Jackson adapters     |
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
- [Jackson common contracts module](jsonapi-java-jackson-common/README.md)
- [Vision](docs/vision.md) — stable product direction and principles
- [Outlook](docs/outlook/README.md) — tentative, revisable future direction
- [Conformance checklist](docs/conformance.md) — current JSON:API 1.1 feature status
- [Architecture decision records](docs/adr/README.md)
- [Implementation milestones](.agentWork/milestones/README.md) — live execution plans (migration in progress)
- [Agent workflow](AGENTS.md) — knowledge model, discovery, and completion gates

## License

Apache License 2.0 — see [LICENSE](LICENSE).
