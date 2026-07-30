# ADR-007: Optional Adapter Modules

**Status:** Accepted  
**Date:** 2026-07-26  
**Amended:** 2026-07-30

## Context

Document consumers should not acquire Jackson or Spring transitively, and domain classes should not depend on web-framework types. Query parsing is useful outside Spring but is not part of a JSON:API document.

## Decision

Use these module boundaries:

- `jsonapi-java-core`: dependency-free document model and validation;
- `jsonapi-java-annotations`: dependency-free domain-mapping annotations;
- `jsonapi-java-jackson3`: Jackson 3 document codec, flat DTO mapping, typed envelopes, and
  presence-aware PATCH commands;
- `jsonapi-java-jackson2`: separately compiled Jackson 2 artifact with parity contracts;
- `jsonapi-java-query`: optional query-parameter parser;
- `jsonapi-java-spring-webmvc`: optional Spring Boot WebMVC integration;
- `jsonapi-java-spring-webflux`: separately evaluated future integration.

Spring modules depend on the lower layers they adapt. The first WebMVC adapter targets Jackson 3;
Jackson 2 remains usable without Spring integration. Lower layers never depend on Spring.

Package and Maven coordinates use the verified namespace in ADR-008 (`io.github.kazemek` / `io.github.kazemek.jsonapi`).

## Consequences

- Core remains usable with no third-party runtime dependency.
- Consumers select only the adapters they need.
- Jackson 2 and Jackson 3 public APIs never share one runtime artifact or use runtime major
  detection.
- WebMVC can stabilize without coupling its release to WebFlux.
- More artifacts and Gradle subprojects must be maintained.
- Build complexity is accepted in exchange for dependency and responsibility boundaries.
