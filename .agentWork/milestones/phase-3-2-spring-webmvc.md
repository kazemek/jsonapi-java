# Phase 3.2 — Spring WebMVC Adapter

> **Module:** `jsonapi-java-spring-webmvc`  
> **Dependencies:** Phases 2.1–2.4 and 3.1, Spring Boot WebMVC  
> **Status:** Not started

## Goal

Integrate the codec and optional mapping/query features with Spring Boot WebMVC without owning controller, persistence, or query execution.

## Deliverables

- Conditional Spring Boot auto-configuration.
- An HTTP message converter limited to `application/vnd.api+json`.
- Controller argument resolvers for opted-in query model types.
- Explicit helpers or return wrappers for domain mapping context, links, metadata, inclusion, and sparse fieldsets.
- An exception-to-error registry with safe defaults; arbitrary exceptions are not exposed automatically.
- JSON:API error rendering for codec, validation, mapping, query, and media-type failures.

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
- POJO/record and explicit document responses;
- request document decoding;
- all relevant `406`, `415`, and `400` paths;
- extension/profile negotiation;
- repeated and bracketed query parameters;
- error source pointers/parameters/headers;
- safe treatment of unregistered application exceptions;
- conditional auto-configuration.

## Acceptance criteria

- [ ] The adapter does not replace normal Jackson handling for other media types.
- [ ] Required media-type negotiation cases pass integration tests.
- [ ] Query arguments preserve the query module's input contract.
- [ ] Error mapping is explicit and does not leak exception details by default.
- [ ] No repository, transaction, ORM, or query-execution abstraction is introduced.
- [ ] `./gradlew :jsonapi-java-spring-webmvc:test` passes.
