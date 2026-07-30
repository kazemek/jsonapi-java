# Phase 2.9 — Jackson 3 Flat DTO Reader

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phases 2.2 and 2.4  
> **Status:** Not started

## Goal

Bind validated JSON:API resource objects to annotated flat records and POJOs without resolving
included resources into a domain graph.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — binding runs after document validation,
  uses flat DTOs, and never injects `included` resources into relationships.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — Jackson logical properties, creators,
  naming, ignores, mix-ins, converters, and configured modules remain authoritative.
- Phase 2.2 owns immutable mapping definitions and identifier conversion; read binding reuses them
  in reverse rather than introducing a second annotation/reflection scanner.
- Phase 2.4 owns JSON parsing, primary resource-versus-identifier interpretation, source locations,
  and core validation. Mapping failures add domain diagnostics without replacing codec failures.
- [ADR-002](../../docs/adr/002-document-representation.md) — explicit null, empty collections, and
  member absence must remain distinguishable wherever the target DTO shape can represent them.

## Deliverables

- Add a public Jackson 3 resource binder configured from the same caller mapper and immutable
  mapping registry used by Phase 2.2, with `Class`, `JavaType`, and type-safe convenience entry
  points for one resource and declared homogeneous resource collections.
- Bind resource `type`, `id`/`lid`, and attributes to Jackson logical properties, using inverse
  identifier conversion and normal Jackson creator/deserializer behavior.
- Bind annotated relationship properties from linkage only: scalar and collection identifier
  targets use registered identifier conversion, richer reference targets require an explicit
  linkage mapper, and no value is read from `included`.
- Add stable mapping categories and logical JSON Pointer-like paths for type mismatch, unsupported
  target/cardinality, identifier conversion, missing creator input, unknown mapped member, and
  custom linkage failures.
- Refresh module docs/Javadoc and conformance rows for the flat DTO read flow.

## Non-goals

- Binding document-level members or `included`; Phase 2.10 owns the typed envelope.
- Automatic graph hydration, persistence lookup, identity maps, or cycle resolution.
- Presence-aware partial updates; Phase 2.11 owns PATCH binding.
- Resource-identifier documents as if they were full resource DTOs.
- Jackson 2 support; Phase 2.15 ports this contract after the Jackson 3/Spring path is stable.

## Implementation boundaries

- Public APIs live in `io.github.kazemek.jsonapi.jackson3`; implementation remains in
  `io.github.kazemek.jsonapi.jackson3.internal` and imports no `core.internal` types.
- A resource type must match `@JsonApiResource(type)`. Heterogeneous primary collections require
  explicit per-type registration and are deferred to the Phase 2.10 envelope registry.
- Attribute values pass through the caller's Jackson deserializers. Unknown and ignored logical
  properties obey caller mapper configuration after JSON:API role/name resolution.
- Omitted resource members are omitted from Jackson input; explicit JSON null remains a present
  null token. Jackson creator/null policy determines whether the target DTO accepts that shape.
- Relationship absence, absent relationship `data`, explicit null linkage, and empty to-many
  linkage are not collapsed. Unsupported target shapes fail instead of fabricating related DTOs.

## Test strategy

- Cover records, mutable and immutable POJOs, creators, inheritance, naming strategies,
  `@JsonProperty`, `@JsonIgnore`, mix-ins, custom deserializers, and identifier converters.
- Exercise single and homogeneous collection resources, explicit-null attributes, omitted
  properties, null/single/empty/to-many linkage, and stable negative diagnostics.
- Use compound documents to prove that changing an `included` representation cannot affect the
  bound primary DTO or relationship fields.

## Acceptance criteria

- [ ] Resource type, identifier, attribute, creator, naming, ignore, mix-in, and custom
      deserializer behavior is the tested inverse of the documented Phase 2.2 mapping contract.
- [ ] Relationships bind only from linkage with stable cardinality/conversion failures, and no
      `included` resource is read or injected.
- [ ] Mapping definitions are shared with Phase 2.2, caller mapper behavior is preserved, and
      production code imports neither `core.internal` nor another integration module's internals.
- [ ] The canonical `module-docs` checklist passes and conformance documentation marks only the
      delivered flat DTO read shapes **supported**.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*ResourceBinderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
