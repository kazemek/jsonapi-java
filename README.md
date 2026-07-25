# jsonapi-java

> Read and write JSON:API v1.1 documents in Java without surrendering control of persistence, endpoints, or application architecture.

A lightweight [JSON:API v1.1](https://jsonapi.org/) document codec for **Java 21+**, with opt-in Jackson domain mapping, query parsing, and Spring adapters. Compliance is tracked by feature and layer; the library does not claim that an application's endpoint behavior is automatically JSON:API compliant.

## Status

**Pre-alpha.** The Gradle build scaffold, CI pipeline, architecture decisions, and implementation milestones are in place. Library implementation has not started.

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
| `jsonapi-java-core/` | Current scaffold; planned zero-dependency document model and validation |
| `build-logic/` | Shared Gradle convention plugin (`jsonapi-java-library`) |
| `docs/` | Vision statement and architecture decision records |
| `.agentWork/milestones/` | Concrete, testable implementation increments |

Planned optional artifacts are `jsonapi-java-annotations`, `jsonapi-java-jackson`, `jsonapi-java-query`, and `jsonapi-java-spring-webmvc`. WebFlux will be evaluated separately.

## Documentation

- [Vision & roadmap](docs/vision.md)
- [Architecture decision records](docs/adr/)
- [Implementation milestones](.agentWork/milestones/)
- [Agent workflow](AGENTS.md)

## License

Apache License 2.0 — see [LICENSE](LICENSE).
