# Phase 2.12 — Jackson 2 Domain-to-Resource Mapping

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.2 and 2.6  
> **Status:** Not started

## Goal

Port the stable annotated domain-to-resource mapping contract to the isolated Jackson 2 artifact.

## Research and constraints

- Phase 2.2 is the semantic contract for logical-property roles, identifiers, linkage, diagnostics,
  and caller mapper isolation; this milestone changes Jackson APIs, not mapping policy.
- Phase 2.6 establishes the maintained Jackson 2 baseline and `com.fasterxml.jackson.*` boundary.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — use Jackson 2 `BeanDescription`,
  `JavaType`, creators, names, ignores, mix-ins, serializers, and configured modules.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — production code may depend on core public
  packages, annotations, JSpecify, and Jackson 2, never Jackson 3 or sibling internals.

## Deliverables

- Add Jackson 2 immutable mapping definitions/cache with the same role/name/conflict policy as
  Phase 2.2.
- Add explicit mapper/writer entry points under `io.github.kazemek.jsonapi.jackson2`, derived from
  caller configuration without mutating the caller mapper.
- Port default/replaceable identifier conversion, attribute serialization, and null/single/
  collection relationship linkage.
- Reuse the Jackson 3 domain fixture manifest to compare produced core resources and stable mapping
  categories across majors.
- Refresh Jackson 2 module docs/Javadoc, root registry, and conformance notes for write mapping.

## Non-goals

- Compound inclusion or sparse fieldsets; Phases 2.13 and 2.14 own them.
- Flat DTO reads or typed envelopes; Phases 2.15 and 2.16 own them.
- Runtime delegation to Jackson 3 or reflection-based major detection.

## Implementation boundaries

- Public signatures use only `com.fasterxml.jackson.*`; no `tools.jackson.*`,
  `jsonapi-java-jackson3`, or `core.internal` production dependency is permitted.
- Public concepts and diagnostics mirror Jackson 3, while unavoidable Jackson-major source API
  differences remain internal.
- Shared expected resources/JSON are consumed directly; no divergent Jackson 2 fixture copies.

## Test strategy

- Parameterize Phase 2.2 records/POJOs, naming, ignores, mix-ins, creators, inheritance, custom
  serializers, identifiers, linkage, and negative mapping fixtures across both majors.
- Prove ordinary caller mapper behavior remains unchanged and both major artifacts can coexist.

## Acceptance criteria

- [ ] Jackson 2 mapping produces core resources and stable diagnostic categories equivalent to
      Phase 2.2 for every shared fixture.
- [ ] Identifier conversion, attributes, and relationship linkage retain the stable Jackson 3
      contract while respecting Jackson 2 caller configuration.
- [ ] Production/runtime dependencies contain no Jackson 3 artifact, package, or sibling internal.
- [ ] The canonical `module-docs` checklist passes and conformance docs distinguish Jackson 2 and
      Jackson 3 mapping support.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*DomainResourceMapperSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
