# Phase 3.3 — Spring WebMVC Flat DTO Binding

> **Module:** `jsonapi-java-spring-webmvc`  
> **Dependencies:** Phases 2.8, 2.9, 2.10, 2.11, and 3.2<br>
> **Status:** Not started

## Goal

Let opted-in Spring WebMVC controllers use annotated flat DTOs, typed domain envelopes, and
presence-aware PATCH commands without directly handling core documents.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — DTO binding remains document-first,
  included resources bind independently, and relationships are never graph-hydrated.
- [ADR-012](../../docs/adr/012-resource-patch-binding.md) — PATCH controller arguments are
  presence-aware commands; applications authorize and apply them.
- Phase 3.2 owns converter selection, media-type negotiation, core transport, query arguments, and
  safe error rendering. This milestone extends that adapter rather than registering a competing
  converter.
- Phases 2.8, 2.10, and 2.15 own sparse-fieldset/inclusion context, typed envelopes, and PATCH
  semantics. Spring only transports explicit caller policy into those APIs.
- Spring MVC return-value and argument resolution must remain conditional and must not alter
  ordinary `application/json` controller behavior.

## Deliverables

- Add opt-in controller argument and return-value handling for annotated single/collection DTOs
  and typed domain envelopes over `application/vnd.api+json`.
- Add immutable return wrappers/builders carrying domain data plus explicit links, metadata,
  JSON:API information, include policy, traversal limits, and sparse fieldsets into the Phase 2
  writer contracts.
- Add request binding for typed domain envelopes, including independently registered/bound
  `included` DTOs and stable codec/mapping error rendering.
- Add presence-aware PATCH command arguments with an explicit application/adapter hook for expected
  endpoint identity; never infer identity unsafely from arbitrary route-variable names.
- Add MockMvc fixtures, safe error mappings, auto-configuration metadata, module docs, and
  conformance examples for DTO-oriented endpoints.

## Non-goals

- Generated controllers, repositories, persistence operations, authorization, or command
  application.
- Automatic relationship graph hydration or injection of included DTOs.
- Implicit include paths, field authorization, extension/profile semantics, or query execution.
- Changing ordinary Spring/Jackson handling for `application/json`.
- WebFlux support; Phase 3.4 evaluates the stable WebMVC contract separately.

## Implementation boundaries

- DTO behavior activates only for explicit JSON:API wrapper/argument types or documented opt-in
  annotations and the JSON:API media type. Plain POJO handling remains Spring/Jackson-owned.
- Input always follows Phase 3.2 decode/validation before Phase 2.10/2.11 binding. Mapping failures
  use the registered safe JSON:API error policy without exposing application values or internals.
- Response mapping uses caller-supplied immutable inclusion, fieldset, profile, extension, and
  traversal policy; defaults request no inclusion and do not authorize fields.
- PATCH endpoint identity must be supplied through an explicit resolver/argument contract.
  Mismatch is rejected before the controller receives a command.
- Framework wrappers depend on Jackson 3 contracts only. Core, annotation, Jackson, and query
  modules acquire no Spring dependency.

## Test strategy

- Use MockMvc controllers for record/POJO single and collection responses, typed request envelopes,
  heterogeneous included DTOs, links/meta, explicit-null data, and ordinary JSON coexistence.
- Cover include paths, sparse fieldsets, traversal failures, mapping diagnostics, malformed input,
  unsupported media types, and safe error rendering.
- Exercise valid PATCH commands, omitted versus explicit-null attributes, relationship replacement,
  endpoint identity mismatch, and proof that controller code—not the adapter—applies changes.

## Acceptance criteria

- [ ] Opted-in JSON:API controllers can use annotated flat DTOs and typed envelopes for single and
      collection requests/responses without `JsonApiDocument` in routine method signatures.
- [ ] Included DTOs remain independently accessible, relationship fields remain linkage-only, and
      include/fieldset/traversal policy is explicit on every response path.
- [ ] PATCH arguments preserve requested changes and validate explicit endpoint identity before
      controller invocation without mutating application state.
- [ ] Ordinary JSON handling remains unchanged; the canonical `module-docs` checklist and
      conformance examples cover only the delivered WebMVC DTO contract.
- [ ] `./gradlew :jsonapi-java-spring-webmvc:test --tests '*DomainBindingMvcSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, local Sonar
      validation is explicitly blocked rather than counted as passed, and CI must still run and
      pass the gate.
