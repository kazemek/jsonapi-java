# Vision and Architectural Strategy: `jsonapi-java`

> Make JSON:API v1.1 documents straightforward to read and write in Java without taking ownership of an application's persistence, endpoints, or business architecture.

## Product boundary

`jsonapi-java` is a lightweight, Java 21+ document codec with optional bidirectional
domain-mapping, query-parameter, and web-framework adapters.

The library owns:

- an immutable, dependency-free representation of JSON:API documents;
- strict validation of document structure and cross-document invariants;
- Jackson encoding and decoding of that document model;
- opt-in mapping between ordinary Jackson-visible POJOs/records and flat resource objects through
  domain-facing document envelopes;
- presence-aware resource-update commands that preserve omitted values versus explicit JSON
  `null` without mutating application objects;
- optional parsing of JSON:API query parameters;
- optional Spring integration for the JSON:API media type.

Applications continue to own:

- controller and endpoint design;
- persistence, repositories, transactions, and authorization;
- filtering, sorting, pagination, and query execution;
- decisions about supported include paths, fields, profiles, and extensions;
- authorization and application of resource-update commands to existing domain state;
- HTTP operation semantics beyond behavior explicitly implemented by an adapter.

This boundary is deliberate. The project is not an API engine, ORM bridge, repository abstraction, or endpoint generator.

## What “lightweight” means

- `jsonapi-java-core` has no third-party runtime dependencies. A compile-only JSpecify
  annotation jar may be used for nullness metadata (see ADR-009); it is not a functional
  runtime dependency and must not appear on the published runtime classpath.
- Domain mapping is opt-in, bidirectional for documented flat DTO shapes, and requires no
  inheritance or framework interfaces.
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

### Document-first correctness, DTO-first adapters

Deserialization first produces and validates the JSON:API document model. Jackson and framework
adapters may then bind primary resources to annotated flat DTOs and expose document-level members
through a typed domain envelope, so routine application code need not manipulate the core model.

Relationship properties bind resource linkage only. Included resources are bound independently
through explicit resource-type registration and are never injected into relationship properties.
Automatic reconstruction of arbitrary domain graphs remains outside the product boundary because
unresolved linkage, cycles, identity, and persistence resolution require application policy.

Resource PATCH binding produces a presence-aware update command. Omitted attributes and
relationships remain distinguishable from explicit attribute `null` and explicit null, single, or
collection linkage. Applications authorize and apply that command; the library does not mutate an
existing DTO or persistence object.

### Extensible without interpreting extensions

The base model preserves valid extension members and `@` members. It does not implement extension-specific semantics unless a separate feature explicitly declares support.

## Modules

- `jsonapi-java-core` — dependency-free document model and validation.
- `jsonapi-java-annotations` — dependency-free, opt-in domain-mapping annotations.
- `jsonapi-java-jackson3` — Jackson 3 document writer and reader are available, as is flat
  resource-to-DTO binding; typed domain envelopes and PATCH binding remain later Phase 2 work.
- `jsonapi-java-jackson2` — later Jackson 2 artifact with the same stable conceptual contracts.
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
- Domain mapping, typed envelopes, and PATCH commands are guaranteed only for documented Jackson
  property shapes and mapping policies.

### Application responsibilities

- Endpoint availability and operation semantics.
- HTTP status selection outside adapter-defined behavior.
- Persistence and relationship mutation.
- Authorization and application of presence-aware update commands.
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

1. Encode the document model with official wire fixtures (Phase 2.1 writer complete); decode is
   available in Phase 2.4.
2. Map Jackson-visible domain properties to and from flat resource objects.
3. Add explicitly requested compound inclusion and sparse fieldsets with bounded traversal.
4. Read JSON into validated document models before optional DTO binding.
5. Expose typed domain envelopes with independently bound included resources.
6. Bind JSON:API resource updates to presence-aware commands without applying them.
7. Stabilize these contracts on Jackson 3, then port them to an isolated Jackson 2 artifact.

### Phase 3 — Optional adapters

1. Parse JSON:API query parameters without executing them.
2. Integrate the media type, codec, query arguments, error documents, annotated DTOs, typed
   envelopes, and presence-aware update commands with Spring WebMVC.
3. Evaluate WebFlux as a separate artifact after WebMVC behavior is stable.

### Phase 4 — Production readiness

- Complete the conformance and malformed-input suites.
- Define size, depth, traversal, and resource limits.
- Establish performance baselines and compatibility policy.
- Decide JPMS support and publish under a verified namespace.

## Initial non-goals

- Generated controllers or repositories.
- ORM-specific behavior or automatic lazy association traversal.
- Automatic domain graph hydration or injection of `included` resources into relationships.
- Automatic application of PATCH commands to domain or persistence objects.
- A filtering or pagination DSL.
- Relationship endpoint implementation.
- Extension-specific processing beyond explicitly supported extensions.
- A guarantee that an application using the library is globally JSON:API compliant.

## Project conventions

- Java 21 toolchain.
- Gradle Kotlin DSL and version catalog.
- Spock specifications under `src/test/groovy`.
- Apache License 2.0.