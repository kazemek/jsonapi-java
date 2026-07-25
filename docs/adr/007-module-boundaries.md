# ADR-007: Optional Adapter Modules

**Status:** Accepted  
**Date:** 2026-07-26

## Context

Document consumers should not acquire Jackson or Spring transitively, and domain classes should not depend on web-framework types. Query parsing is useful outside Spring but is not part of a JSON:API document.

## Decision

Use these module boundaries:

- `jsonapi-java-core`: dependency-free document model and validation;
- `jsonapi-java-annotations`: dependency-free domain-mapping annotations;
- `jsonapi-java-jackson`: document codec and Jackson domain mapping;
- `jsonapi-java-query`: optional query-parameter parser;
- `jsonapi-java-spring-webmvc`: optional Spring Boot WebMVC integration;
- `jsonapi-java-spring-webflux`: separately evaluated future integration.

Spring modules depend on the lower layers they adapt. Lower layers never depend on Spring.

Package and Maven coordinates use the verified namespace in ADR-008 (`io.github.kazemek` / `io.github.kazemek.jsonapi`).

## Consequences

- Core remains usable with no third-party runtime dependency.
- Consumers select only the adapters they need.
- WebMVC can stabilize without coupling its release to WebFlux.
- More artifacts and Gradle subprojects must be maintained.
- Build complexity is accepted in exchange for dependency and responsibility boundaries.
