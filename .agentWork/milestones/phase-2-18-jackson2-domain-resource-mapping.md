# Phase 2.18 — Jackson 2 Domain-to-Resource Mapping

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.2, 2.11, 2.13, and 2.16  
> **Status:** Not started

## Goal

Port the stable annotated domain-to-resource mapping contract to the isolated Jackson 2 artifact
using common diagnostics and shared domain-write fixtures.

## Research and constraints

- Phase 2.2 is the semantic contract for logical-property roles, identifiers, linkage, diagnostics,
  and caller mapper isolation; this milestone changes Jackson APIs, not mapping policy.
- Phase 2.11 supplies common `IdentifierConverter`, `MappingDiagnostic`, and related Jackson-free
  contracts; Phase 2.16 establishes the Jackson 2 baseline.
- Phase 2.13 owns the shared domain-write scenario catalog for flat mapping parity.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — use Jackson 2 `BeanDescription`,
  `JavaType`, creators, names, ignores, mix-ins, serializers, and configured modules.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — public packages are `@NullMarked`; nullable
  envelope members (for example `@Nullable DocumentEnvelope`) use explicit `@Nullable`.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — production code may depend on core public
  packages, annotations, common contracts, JSpecify, and Jackson 2, never Jackson 3 or sibling
  internals.

## Deliverables

- Add Jackson 2 immutable mapping definitions/cache with the same role/name/conflict policy as
  Phase 2.2.
- Add explicit mapper (and document-assembly) entry points under
  `io.github.kazemek.jsonapi.jackson2`, derived from caller configuration without mutating the
  caller mapper and consuming common diagnostics; wire emission uses the Phase 2.16 writer.
- Port default/replaceable identifier conversion, attribute serialization, and null/single/
  collection relationship linkage through common contracts where applicable.
- Consume the Phase 2.13 flat-mapping scenario catalog to compare produced core resources and stable
  mapping categories across majors; keep major-specific serializer/mix-in cases adapter-local.
- Refresh Jackson 2 module docs/Javadoc, root registry, and conformance notes for write mapping.

## Non-goals

- Compound inclusion or sparse fieldsets; Phases 2.19 and 2.20 own them.
- Flat DTO reads or typed envelopes; Phases 2.21 and 2.22 own them.
- Duplicating common diagnostic/identifier types under the `jackson2` package.
- Runtime delegation to Jackson 3 or reflection-based major detection.

## Implementation boundaries

- Public signatures use only `com.fasterxml.jackson.*` and common contracts; no `tools.jackson.*`,
  `jsonapi-java-jackson3`, or `core.internal` production dependency is permitted.
- Public concepts and diagnostics mirror Jackson 3 via the common package, while unavoidable
  Jackson-major source API differences remain internal.
- Shared Phase 2.13 scenarios and expected resources are consumed directly; no divergent Jackson 2
  fixture copies.

## Test strategy

- Parameterize Phase 2.13 flat-mapping scenarios (records/POJOs, naming, ignores, creators,
  identifiers, linkage, and negative diagnostics) across both majors.
- Retain adapter-local Jackson 2 cases for major-specific serializers, mix-ins, and mapper isolation.
- Prove ordinary caller mapper behavior remains unchanged and both major artifacts can coexist.

## Acceptance criteria

- [ ] Jackson 2 mapping produces core resources and stable diagnostic categories equivalent to
      Phase 2.2 for every applicable Phase 2.13 shared scenario.
- [ ] Mapping is invoked explicitly through a mapper derived from caller configuration and does
      not change ordinary serialization through the caller's original Jackson 2 mapper;
      relationship mapping never populates `included`.
- [ ] Production/runtime dependencies contain no Jackson 3 artifact, package, or sibling internal.
- [ ] The canonical `module-docs` checklist passes, including ADR-009 `@NullMarked` /
      `@Nullable DocumentEnvelope` on the public surface, and conformance docs distinguish Jackson 2
      and Jackson 3 mapping support.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*ResourceMapperSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
