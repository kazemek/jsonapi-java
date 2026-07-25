# Vision and Architectural Strategy: `jsonapi-java`

> Make JSON:API v1.1 documents straightforward to read and write in Java without taking ownership of an application's persistence, endpoints, or business architecture.

## Product boundary

`jsonapi-java` is a lightweight, Java 21+ document codec with optional domain-mapping, query-parameter, and web-framework adapters.

The library owns:

- an immutable, dependency-free representation of JSON:API documents;
- strict validation of document structure and cross-document invariants;
- Jackson encoding and decoding of that document model;
- opt-in mapping from ordinary Jackson-visible POJOs and records to resource objects;
- optional parsing of JSON:API query parameters;
- optional Spring integration for the JSON:API media type.

Applications continue to own:

- controller and endpoint design;
- persistence, repositories, transactions, and authorization;
- filtering, sorting, pagination, and query execution;
- decisions about supported include paths, fields, profiles, and extensions;
- HTTP operation semantics beyond behavior explicitly implemented by an adapter.

This boundary is deliberate. The project is not an API engine, ORM bridge, repository abstraction, or endpoint generator.

## What “lightweight” means

- `jsonapi-java-core` has no third-party runtime dependencies.
- Domain mapping is opt-in and requires no inheritance or framework interfaces.
- Jackson, query parsing, Spring WebMVC, and future WebFlux support are separate artifacts.
- A relationship creates linkage; it does not automatically traverse and include an object graph.
- The library does not execute filters, sorts, or pagination strategies.
- Framework adapters remain thin and do not introduce application architecture.

“Lightweight” does not mean omitting required JSON:API semantics. Presence versus explicit `null`, compound-document linkage, link forms, extension members, and media-type behavior must remain representable and testable.

## Design principles

### Wire semantics before Java convenience

The document model preserves distinctions visible on the wire, including an absent member versus a member whose value is `null`. Public types are designed from official JSON:API examples and negative cases, not from a desired type count.

### Strict documents, explicit application policy

Local invariants are enforced when values are constructed. Invariants involving a whole document, such as included-resource uniqueness and full linkage, are enforced by document validation. Inclusion, sparse fieldsets, traversal limits, and supported query features are explicit policies rather than hidden defaults.

### Jackson is authoritative for Java properties

Domain mapping uses Jackson's logical property model. It preserves Jackson visibility, naming, mix-ins, ignored properties, custom serializers, and creator rules instead of independently reflecting over fields and getters.

### Read documents before hydrating graphs

Initial deserialization produces a validated JSON:API document model. Automatic reconstruction of arbitrary domain object graphs is not an initial goal because unresolved linkage, cycles, identity, immutable constructors, and partial updates require application policy.

### Extensible without interpreting extensions

The base model preserves valid extension members and `@` members. It does not implement extension-specific semantics unless a separate feature explicitly declares support.

## Modules

- `jsonapi-java-core` — dependency-free document model and validation.
- `jsonapi-java-annotations` — dependency-free, opt-in domain-mapping annotations.
- `jsonapi-java-jackson` — JSON document codec and Jackson-based domain-to-resource mapping.
- `jsonapi-java-query` — optional framework-neutral query-parameter parsing.
- `jsonapi-java-spring-webmvc` — optional Spring Boot WebMVC integration.
- `jsonapi-java-spring-webflux` — possible later adapter, independently scoped.

Maven group is `io.github.kazemek`; Java packages live under `io.github.kazemek.jsonapi` (see `docs/adr/008-public-namespace.md`).

## Compliance contract

Compliance is tracked by feature and layer instead of claimed globally.

### Guaranteed by the core and codec when implemented

- JSON:API v1.1 base document structures and legal wire forms.
- Required local and aggregate document invariants.
- Correct distinction between absent, explicit `null`, single, and collection data.
- Parsing and emission of standard links, errors, metadata, resource linkage, and compound documents.
- Preservation of valid extension and `@` members without interpreting their semantics.

### Guaranteed only by optional adapters

- Query-family parsing is guaranteed by `jsonapi-java-query`; support and execution remain application choices.
- JSON:API `Content-Type` and `Accept` handling is guaranteed only by the applicable web adapter.
- Domain mapping is guaranteed only for documented Jackson property shapes and mapping policies.

### Application responsibilities

- Endpoint availability and operation semantics.
- HTTP status selection outside adapter-defined behavior.
- Persistence and relationship mutation.
- Authorization and visibility of fields and relationships.
- Query execution and limits.
- Extension and profile semantics.

Each milestone updates a conformance checklist with one of: supported, pass-through, delegated, deferred, or out of scope.

## Roadmap

### Phase 0 — Publication identity

- Verified Maven group `io.github.kazemek` and Java base package `io.github.kazemek.jsonapi` before public source types are implemented.

### Phase 1 — Document foundation

- Define a semantically complete, immutable document model.
- Preserve absent/null/value states and legal link/member forms.
- Enforce local invariants and validate aggregate document rules.

### Phase 2 — Jackson codec and mapping

1. Encode and decode the document model using official wire fixtures.
2. Map Jackson-visible domain properties to resource objects.
3. Add explicitly requested compound inclusion and sparse fieldsets with bounded traversal.
4. Read JSON into validated document models; defer automatic domain graph hydration.

### Phase 3 — Optional adapters

1. Parse JSON:API query parameters without executing them.
2. Integrate the media type, codec, query arguments, and error documents with Spring WebMVC.
3. Evaluate WebFlux as a separate artifact after WebMVC behavior is stable.

### Phase 4 — Production readiness

- Complete the conformance and malformed-input suites.
- Define size, depth, traversal, and resource limits.
- Establish performance baselines and compatibility policy.
- Decide JPMS support and publish under a verified namespace.

## Initial non-goals

- Generated controllers or repositories.
- ORM-specific behavior or automatic lazy association traversal.
- Automatic domain graph hydration during deserialization.
- A filtering or pagination DSL.
- Relationship endpoint implementation.
- Extension-specific processing beyond explicitly supported extensions.
- A guarantee that an application using the library is globally JSON:API compliant.

## Project conventions

- Java 21 toolchain.
- Gradle Kotlin DSL and version catalog.
- Spock specifications under `src/test/groovy`.
- Apache License 2.0.