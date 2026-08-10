# Phase 3.4 — Spring WebMVC Presence-Aware PATCH Binding

> **Module:** `jsonapi-java-spring-webmvc`  
> **Dependencies:** Phases 2.11, 2.15, 3.2, and 3.3  
> **Status:** Not started

## Goal

Let opted-in Spring WebMVC controllers accept presence-aware PATCH commands without constructing
full DTOs or applying mutations in the adapter.

## Research and constraints

- [ADR-012](../../docs/adr/012-resource-patch-binding.md) — PATCH controller arguments are
  presence-aware commands; applications authorize and apply them.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — public argument contracts and the
  `EndpointIdentityResolver` SPI are `@NullMarked` with accurate `@Nullable` where absence or JSON
  null is legal.
- Phase 2.11 owns the Jackson-import-free common package/module. Phase 2.15 owns PATCH command
  semantics, public command contracts (in that common package), and the factory-accepted
  `ValidationContext` / optional `EndpointIdentity` validate-on-read bind shape. Phase 3.2 owns
  media-type, transport, and safe-error infrastructure this path uses—not a separate
  identity-bearing validate before bind. Phase 3.3 owns DTO/envelope WebMVC binding this milestone
  extends.
- Expected endpoint identity is supplied only through the documented optional application-owned
  Spring bean `EndpointIdentityResolver` with signature
  `@Nullable EndpointIdentity resolve(NativeWebRequest request)`. Absent bean or null return means
  comparison off; auto-config invents no default identity. HTTP/route derivation stays
  application-owned inside that bean—never undocumented path-variable name heuristics in the
  adapter.
- The PATCH argument path consults the resolver, builds `ValidationContext` forced to
  `DocumentUsage.UPDATE_REQUEST` with `withExpectedEndpointIdentity(...)`, and invokes Phase 2.15
  entry points so that context is the sole aggregate UPDATE_REQUEST validation before bind (one
  validate-on-read, then bind).
- Activation is type-based for documented PATCH command argument types and the JSON:API media type
  only; ordinary `application/json` handling stays unchanged.

## Deliverables

- Add presence-aware PATCH command controller arguments over `application/vnd.api+json` using
  Phase 2.15 entry points and Phase 2.15 common command contracts.
- Add optional application-owned `EndpointIdentityResolver` bean support
  (`@Nullable EndpointIdentity resolve(NativeWebRequest)`); thread the result into Phase 2.15’s
  `ValidationContext` before the sole UPDATE_REQUEST validate-on-read; reject mismatch before the
  controller receives a command.
- Add MockMvc fixtures covering omitted versus explicit-null attributes, relationship replacement,
  endpoint identity mismatch, identity comparison-off when unresolved/absent, and ordinary JSON
  coexistence.
- Use `module-docs` for the changed Spring WebMVC surface and update the HTTP/endpoints conformance
  notes so Spring PATCH command binding is **supported** (application of commands remains out of
  scope).

## Non-goals

- Flat DTO / typed-envelope WebMVC binding (Phase 3.3).
- Constructing complete DTOs, mutating domain objects, authorization, or persistence.
- Inferring endpoint identity from undocumented path-variable heuristics.
- A separate Phase 3.2 identity validate before Phase 2.15 bind.
- WebFlux support (Phase 3.5).

## Implementation boundaries

- PATCH path: resolve optional identity → Phase 2.15 factory with `UPDATE_REQUEST` +
  `withExpectedEndpointIdentity` → one validate-on-read then presence-aware bind. Phase 3.2
  supplies media-type/transport/error infrastructure only.
- Absent `EndpointIdentityResolver` bean or null resolve result ⇒ comparison off; mismatch uses
  Phase 1.3 `ENDPOINT_IDENTITY_MISMATCH` before controller invocation.
- Framework wrappers depend on Jackson 3 / common contracts only; no Spring dependency leaks into
  core or Jackson modules.

## Test strategy

- Exercise valid PATCH commands, omitted versus explicit-null attributes, relationship replacement,
  endpoint identity mismatch, comparison-off when identity is absent, and proof that controller
  code—not the adapter—applies changes.
- Assert ordinary JSON coexistence remains unchanged.

## Acceptance criteria

- [ ] Controller PATCH arguments expose presence-aware changes for omitted vs explicit-null
      attributes and relationship replacement without constructing a full DTO or mutating
      application state in the adapter.
- [ ] When `EndpointIdentityResolver` supplies expected identity, mismatch fails before controller
      invocation via Phase 2.15’s sole UPDATE_REQUEST validate-on-read; when the bean is absent or
      returns null, identity comparison is off.
- [ ] Public PATCH argument APIs and `EndpointIdentityResolver` satisfy ADR-009 `@NullMarked` /
      `@Nullable` rules.
- [ ] The canonical `module-docs` checklist passes; HTTP/endpoints notes mark Spring PATCH command
      binding **supported** without claiming application semantics; ordinary JSON coexistence
      remains unchanged.
- [ ] `./gradlew :jsonapi-java-spring-webmvc:test --tests '*PatchBindingMvcSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, local Sonar
      validation is explicitly blocked rather than counted as passed, and CI must still run and
      pass the gate.
