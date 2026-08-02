# jsonapi-java

> Read and write JSON:API v1.1 documents in Java without surrendering control of persistence, endpoints, or application architecture.

A lightweight [JSON:API v1.1](https://jsonapi.org/) document model and validation library for
**Java 21+**. Opt-in bidirectional flat DTO mapping, typed envelopes, presence-aware PATCH commands,
query parsing, and Spring adapters are planned. Compliance is tracked by feature and layer; the
library does not claim that an application's endpoint behavior is automatically JSON:API
compliant.

## Status

**Pre-alpha.** The Gradle build, CI pipeline, architecture decisions, Phase 1.1 document model/validation in `jsonapi-java-core`, Phase 1.2 domain-mapping annotations in `jsonapi-java-annotations`, Phase 2.1/2.4 Jackson 3 document writer/reader, and Phase 2.2 domain-to-resource mapping in `jsonapi-java-jackson3` are in place. Later adapters and query parsing are not started.

Maven group: `io.github.kazemek`. Java packages: `io.github.kazemek.jsonapi.*`.

## Requirements

- JDK 21 (enforced via Gradle toolchain)

## Build

```bash
./gradlew clean build
```

## Project structure

| Path | Purpose |
|------|---------|
| `jsonapi-java-core/` | Zero-dependency JSON:API document model and validation |
| `jsonapi-java-annotations/` | Dependency-free domain-mapping annotations |
| `jsonapi-java-jackson3/` | Jackson 3 document codec and domain-to-resource mapping |
| `jsonapi-java-test-fixtures/` | Internal shared writer fixture builders (not a published module) |
| `fixtures/jsonapi-1.1/` | Version-neutral canonical JSON:API writer fixtures |
| `build-logic/` | Shared Gradle convention plugins |
| `docs/` | Vision statement and architecture decision records |
| `.agentWork/milestones/` | Concrete, testable implementation increments |

## Module registry

| Module | Status | Purpose |
|--------|--------|---------|
| [`jsonapi-java-core`](jsonapi-java-core/README.md) | Available — Phase 1.1 complete | Dependency-free document model and validation |
| [`jsonapi-java-annotations`](jsonapi-java-annotations/README.md) | Available — Phase 1.2 complete | Dependency-free domain-mapping role annotations |
| [`jsonapi-java-jackson3`](jsonapi-java-jackson3/README.md) | Available — Phases 2.1 (writer), 2.4 (reader), 2.2 (resource mapping) complete | Jackson 3 document codec and annotated domain mapping; later DTO reads, PATCH, sparse fieldsets |
| `jsonapi-java-jackson2` | Planned — parity track not started | Separately compiled Jackson 2 parity artifact |
| `jsonapi-java-query` | Planned — Phase 3.1 not started | Optional query-parameter parsing |
| `jsonapi-java-spring-webmvc` | Planned — Phases 3.2–3.3 not started | Jackson 3-based Spring WebMVC transport and DTO binding |
| `jsonapi-java-spring-webflux` | Future evaluation — Phase 3.4 not started | Separately scoped reactive adapter candidate |

Planned and future-evaluation modules have no usable entry point yet. Use each available module
README for its package map, minimal usage, non-goals, and contributor/agent notes; the registry does
not duplicate those module-specific contracts.

## Documentation

- [Core module](jsonapi-java-core/README.md)
- [Annotations module](jsonapi-java-annotations/README.md)
- [Jackson 3 module](jsonapi-java-jackson3/README.md)
- [Vision & roadmap](docs/vision.md)
- [Conformance checklist](docs/conformance.md)
- [Architecture decision records](docs/adr/README.md)
- [Implementation milestones](.agentWork/milestones/README.md)
- [Agent workflow](AGENTS.md)

## License

Apache License 2.0 — see [LICENSE](LICENSE).
