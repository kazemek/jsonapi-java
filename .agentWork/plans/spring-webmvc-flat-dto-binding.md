# Spring WebMVC Flat DTO Binding

> **Module:** `jsonapi-java-spring-webmvc`  
> **Dependencies:** [Spring WebMVC Adapter](spring-webmvc-adapter.md)  
> **Status:** Not started
> **Work item:** KAZ-37

## Goal

Let opted-in Spring WebMVC controllers bind `JsonApiDomainDocument` request arguments and return
annotated domain DTOs (or Spring response wrappers) without directly handling core documents.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — DTO binding remains document-first,
  included resources bind independently, and relationships are never graph-hydrated.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — new public wrapper/argument types are
  `@NullMarked` with accurate `@Nullable` on absence-nullable members (links, meta, jsonapi).
- [Spring WebMVC Adapter](spring-webmvc-adapter.md) owns converter selection, media-type negotiation, core transport, query arguments, and
  safe error rendering. This plan extends that adapter rather than registering a competing
  converter.
- `JsonApiResourceBinder` owns flat DTO binding, `JsonApiDomainDocument` /
  `ResourceTypeRegistry` own typed domain documents, `CompoundSerializationContext` /
  `IncludePolicy` own inclusion context, `FieldPolicy` / `MappedDocument` own sparse-fieldset
  write coordination (`jsonapi-java-jackson3` README), and `jsonapi-java-jackson-common` supplies
  the contracts Spring consumes. Spring only transports explicit caller policy into those APIs.
- Activation types: request arguments are `JsonApiDomainDocument` only; return values are (a) a
  bare type or collection element type carrying `@JsonApiResource`, or (b) an explicit library
  Spring response wrapper that carries domain data plus `DocumentEnvelope` members and
  `CompoundSerializationContext` policy. Never activate for arbitrary POJOs; always only with the
  JSON:API media type. Plain POJO `application/json` handling stays Spring/Jackson-owned.
- Included-type registry: applications provide a `ResourceTypeRegistry` Spring bean; auto-config
  does not invent a default registry of domain types.
- Response writes: non-empty fieldsets use `toMappedDocument` / `toMappedResourceCollection`.
  `mapped.applyTo(baseValidationContext)` produces the `ValidationContext` that carries the
  sparse-fieldset full-linkage exception when needed; pass that context to
  `JsonApiJackson3.writer(...)`. Serialize `mapped.document()` with the writer's `writeValue*`
  methods. Inclusion with an empty
  fieldset map uses three-argument `toDocument` / `toResourceCollection`. Unrestricted responses
  without that context may use the non-mapped path.
- Request arguments bind via `JsonApiDomainDocumentReader.fromDocument` on the already-validated
  `JsonApiDocument` from [Spring WebMVC Adapter](spring-webmvc-adapter.md) transport (registry bean configures `domainDocumentReader`).
  Do not re-parse the body with `readValue` after the document converter.
- Presence-aware PATCH controller arguments are [Spring WebMVC Presence-Aware PATCH Binding](spring-webmvc-patch-binding.md).
- Spring MVC return-value and argument resolution must remain conditional and must not alter
  ordinary `application/json` controller behavior.

## Deliverables

- Add opt-in controller argument handling for `JsonApiDomainDocument` and return-value handling for
  bare annotated DTOs/collections and Spring response wrappers over `application/vnd.api+json`.
- Add immutable `@NullMarked` Spring response wrappers/builders carrying domain data plus explicit
  links, metadata, JSON:API information, include policy, traversal limits, and sparse fieldsets into
  the jackson3 writer contracts (`@Nullable` only where absence is legal).
- Add request binding that uses `JsonApiDomainDocumentReader.fromDocument` on the
  [Spring WebMVC Adapter](spring-webmvc-adapter.md)-validated document with an
  application-provided `ResourceTypeRegistry` bean, including
  independently registered/bound `included` DTOs and stable codec/mapping error rendering.
- Add MockMvc fixtures and safe error mappings for DTO/envelope endpoints.
- Use `module-docs` for the changed Spring WebMVC surface and update the HTTP/endpoints conformance
  row for Spring-annotated DTO and typed-envelope binding to **supported**.

## Non-goals

- Presence-aware PATCH command arguments ([Spring WebMVC Presence-Aware PATCH Binding](spring-webmvc-patch-binding.md)).
- Generated controllers, repositories, persistence operations, or authorization.
- Automatic relationship graph hydration or injection of included DTOs.
- Implicit include paths, field authorization, extension/profile write semantics, or query
  execution.
- Changing ordinary Spring/Jackson handling for `application/json`.
- WebFlux support; [WebFlux Adapter Evaluation](spring-webflux-adapter-evaluation.md) evaluates the stable WebMVC contract separately.

## Implementation boundaries

- DTO behavior activates only for `JsonApiDomainDocument` request arguments, returns whose
  (element) type carries `@JsonApiResource`, or an explicit library response wrapper—and only with
  the JSON:API media type. Arbitrary POJO returns never activate JSON:API handling. Plain POJO
  `application/json` handling remains Spring/Jackson-owned.
- Input always follows [Spring WebMVC Adapter](spring-webmvc-adapter.md) decode/validation before `JsonApiDomainDocumentReader` binding.
  Mapping failures use the registered safe JSON:API error policy without exposing application
  values or internals.
- Response mapping uses caller-supplied immutable inclusion, fieldset, and traversal policy on
  `CompoundSerializationContext` plus optional `DocumentEnvelope` links/meta/jsonapi; defaults
  request no inclusion and do not authorize fields. Profile/extension Content-Type behavior stays
  [Spring WebMVC Adapter](spring-webmvc-adapter.md).
- When non-empty fieldsets are applied, Spring writes via `MappedDocument` overloads and `applyTo`
  validation. Inclusion without fieldsets uses three-argument `toDocument` /
  `toResourceCollection`. Unrestricted writes without that context may use the non-mapped path.
- Request resolution calls `fromDocument` on the [Spring WebMVC Adapter](spring-webmvc-adapter.md)-validated `JsonApiDocument`; it does not
  re-parse or re-validate the body.
- Framework wrappers depend on Jackson 3 / common contracts only. Core, annotation, Jackson, and
  query modules acquire no Spring dependency.

## Test strategy

- Use MockMvc controllers for record/POJO single and collection responses (bare DTO and wrapper),
  `JsonApiDomainDocument` requests, heterogeneous included DTOs via an application registry bean,
  links/meta, explicit-null data, and ordinary JSON coexistence.
- Cover include paths (three-argument write), sparse fieldsets (`MappedDocument.applyTo`
  coordination), traversal failures, mapping diagnostics, malformed input, unsupported media
  types, and safe error rendering.

## Acceptance criteria

- [ ] Opted-in JSON:API controllers accept `JsonApiDomainDocument` requests and return bare
      annotated DTOs/collections or Spring response wrappers without `JsonApiDocument` in routine
      method signatures.
- [ ] Included DTOs remain independently accessible via the application `ResourceTypeRegistry`
      bean and relationship fields remain linkage-only; when include/fieldset/traversal context is
      supplied, that policy is honored (non-empty fieldsets via `mapped.applyTo(...)` for
      validation context and `mapped.document()` for serialization; inclusion
      without fieldsets via three-argument `toDocument` / `toResourceCollection`); unrestricted
      non-mapped responses remain allowed when no such context is supplied.
- [ ] New public WebMVC packages/types are `@NullMarked` with accurate `@Nullable` on
      absence-nullable members (ADR-009).
- [ ] Ordinary JSON handling remains unchanged; the canonical `module-docs` checklist passes; the
      HTTP/endpoints Spring DTO/envelope conformance row is **supported**.
- [ ] `./gradlew :jsonapi-java-spring-webmvc:test --tests '*DomainBindingMvcSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, local Sonar
      validation is explicitly blocked rather than counted as passed, and CI must still run and
      pass the gate.
