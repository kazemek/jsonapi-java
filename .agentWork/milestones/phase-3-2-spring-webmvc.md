# Phase 3.2 — Spring WebMVC Adapter

> **Module:** `jsonapi-java-spring-webmvc`  
> **Dependencies:** Phases 2.1, 2.4, 2.5, and 3.1; a Jackson 3-based Spring Boot WebMVC line  
> **Status:** Not started

## Goal

Integrate validated core-document transport, JSON:API media-type negotiation, query parsing, and
safe errors with Spring Boot WebMVC.

## Deliverables

- Conditional Spring Boot auto-configuration.
- An HTTP message converter for validated `JsonApiDocument` reads/writes limited to
  `application/vnd.api+json`.
- Controller argument resolvers for opted-in query model types.
- An exception-to-error registry with safe defaults; arbitrary exceptions are not exposed automatically.
- JSON:API error rendering for codec, validation, query, and media-type failures plus focused
  module documentation and MockMvc fixtures.

## Media-type behavior

Implement and test JSON:API v1.1 rules for:

- `Content-Type` parameters, including permitted `ext` and `profile`;
- unsupported parameters and extensions returning `415`;
- `Accept` alternatives, quality values, unsupported parameters/extensions, and `406`;
- profile handling as application policy;
- `Vary: Accept` where required;
- avoiding interference with non-JSON:API converters.

## Query behavior

Argument resolvers preserve repeated parameters and encoded/unencoded bracket names before invoking `jsonapi-java-query`. They parse values only. Controllers and services interpret and execute query semantics.

Unsupported optional features follow JSON:API response requirements rather than being silently ignored.

## Test strategy

Use Spring context and MockMvc integration tests for:

- converter selection and ordinary JSON coexistence;
- explicit document requests and responses;
- request document decoding;
- all relevant `406`, `415`, and `400` paths;
- extension/profile negotiation;
- repeated and bracketed query parameters;
- error source pointers/parameters/headers;
- safe treatment of unregistered application exceptions;
- conditional auto-configuration.

## Non-goals

- Annotated DTO or typed domain-envelope controller binding; Phase 3.3 owns that integration.
  Presence-aware PATCH arguments are Phase 3.4.
- Presence-aware PATCH command arguments, domain mapping, compound traversal, or sparse fieldsets.
- Controller generation, endpoint semantics, persistence, authorization, or query execution.
- WebFlux or reactive types; Phase 3.5 evaluates that adapter after WebMVC DTO/PATCH behavior
  stabilizes.

## Acceptance criteria

- [ ] The converter handles only `application/vnd.api+json`, returns validated core documents, and
      does not replace ordinary Jackson handling for other media types.
- [ ] Required `Content-Type`, `Accept`, extension/profile, `Vary`, and error-rendering cases pass
      without leaking unregistered exception details.
- [ ] Query arguments preserve the query module input contract, and no repository, transaction,
      ORM, authorization, endpoint, or query-execution abstraction is introduced.
- [ ] The canonical `module-docs` checklist passes and conformance notes distinguish document
      transport from the deferred Phase 3.3 DTO integration.
- [ ] `./gradlew :jsonapi-java-spring-webmvc:test` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, local Sonar
      validation is explicitly blocked rather than counted as passed, and CI must still run and
      pass the gate.
