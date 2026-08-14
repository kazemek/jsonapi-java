# Jackson 2 Domain-to-Resource Mapping

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** [Jackson 2 Document Writer](jackson2-document-writer.md)  
> **Status:** Not started
> **Work item:** KAZ-25

## Goal

Port the stable annotated domain-to-resource mapping contract to the isolated Jackson 2 artifact
using common diagnostics and shared domain-write fixtures.

## Research and constraints

- `JsonApiResourceMapper` (`jsonapi-java-jackson3` README / [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md))
  is the semantic contract for logical-property roles, identifiers, linkage, diagnostics,
  and caller mapper isolation; this plan changes Jackson APIs, not mapping policy.
- `jsonapi-java-jackson-common` supplies `IdentifierConverter`, `MappingDiagnostic`, and related
  Jackson-free contracts; [Jackson 2 Document Writer](jackson2-document-writer.md) establishes the Jackson 2 baseline.
- `JsonApiFixtures.domainWrite()` / `DomainWriteScenarios` is the shared domain-write scenario
  catalog for flat mapping parity.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — use Jackson 2 `BeanDescription`,
  `JavaType`, creators, names, ignores, mix-ins, serializers, and configured modules.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — public packages are `@NullMarked`; nullable
  envelope members (for example `@Nullable DocumentEnvelope`) use explicit `@Nullable`.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — production code may depend on core public
  packages, annotations, common contracts, JSpecify, and Jackson 2, never Jackson 3 or sibling
  internals.

## Deliverables

- Add Jackson 2 immutable mapping definitions/cache with the same role/name/conflict policy as
  jackson3 `JsonApiResourceMapper`.
- Add explicit mapper (and document-assembly) entry points under
  `io.github.kazemek.jsonapi.jackson2`, derived from caller configuration without mutating the
  caller mapper and consuming common diagnostics; wire emission uses the [Jackson 2 Document Writer](jackson2-document-writer.md).
- Port default/replaceable identifier conversion, attribute serialization, and null/single/
  collection relationship linkage through common contracts where applicable, and mirror the
  Jackson 3 negative-diagnostic surface (missing/unsupported identifiers, duplicate roles,
  member-name collisions, invalid resource types, unsupported relationship collection values,
  converter failures) through the common `MappingDiagnostic` categories in adapter-local tests.
- Run every scenario of the shared `DomainWriteScenarios` catalog through the Jackson 2
  mapper and assert full-catalog coverage (`executedScenarioIds == catalogScenarioIds`), mirroring
  Jackson 3's `ResourceMapperSpec`, to compare produced core resources and stable mapping
  categories across majors; keep major-specific serializer/mix-in cases adapter-local, documented
  in the adapter specs.
- Refresh Jackson 2 module docs/Javadoc, root registry, and conformance notes for write mapping.

## Non-goals

- Compound inclusion or sparse fieldsets; [Jackson 2 Compound Serialization](jackson2-compound-serialization.md) and [Jackson 2 Sparse Fieldsets](jackson2-sparse-fieldsets.md) own them.
- Flat DTO reads or typed envelopes; [Jackson 2 Flat DTO Reader](jackson2-flat-dto-reader.md) and [Jackson 2 Typed Domain Envelope](jackson2-typed-domain-envelope.md) own them.
- Duplicating common diagnostic/identifier types under the `jackson2` package.
- Runtime delegation to Jackson 3 or reflection-based major detection.

## Implementation boundaries

- Public signatures use only `com.fasterxml.jackson.*` and common contracts; no `tools.jackson.*`,
  `jsonapi-java-jackson3`, or `core.internal` production dependency is permitted.
- Public concepts and diagnostics mirror Jackson 3 via the common package, while unavoidable
  Jackson-major source API differences remain internal.
- Shared `DomainWriteScenarios` and expected resources are consumed directly; no divergent Jackson 2
  fixture copies.

## Test strategy

- Parameterize the `DomainWriteScenarios` flat-mapping scenarios (records/POJOs, naming, identifiers,
  linkage, document/envelope wrapping, and the null-input rejection) across both majors, and
  assert full-catalog coverage: the Jackson 2 write suite records executed scenario ids and
  requires `executedScenarioIds == catalogScenarioIds` against the live catalog. Ignores,
  creator/Jackson-API-specific cases, and negative diagnostics are covered by adapter-local
  Jackson 2 cases, not by the shared catalog.
- Retain adapter-local Jackson 2 cases for major-specific serializers, mix-ins, and mapper
  isolation, and for the negative mapping-diagnostic contract mirrored from Jackson 3.
- Prove ordinary caller mapper behavior remains unchanged and both major artifacts can coexist;
  the Jackson 2 mapping suite also runs under the [Jackson 2 Document Writer](jackson2-document-writer.md) `jackson2CompatibilityTest` gate
  against the supported Jackson 2.22.x baseline.

## Acceptance criteria

- [ ] The Jackson 2 write suite runs every scenario of the shared `DomainWriteScenarios` catalog
      through its own mapper, and its coverage assertion requires `executedScenarioIds ==
      catalogScenarioIds`; Jackson 2 mapping produces core resources equivalent to jackson3
      `JsonApiResourceMapper` for
      each shared scenario, and its adapter-local negative cases raise the same common
      `MappingDiagnostic` categories as Jackson 3 (duplicate roles, member-name collisions,
      invalid resource types, missing/unsupported identifiers, unsupported relationship
      collection values, converter failures).
- [ ] Mapping is invoked explicitly through a mapper derived from caller configuration and does
      not change ordinary serialization through the caller's original Jackson 2 mapper;
      relationship mapping never populates `included`.
- [ ] Production/runtime dependencies contain no Jackson 3 artifact, package, or sibling internal.
- [ ] The canonical `module-docs` checklist passes, including ADR-009 `@NullMarked` /
      `@Nullable DocumentEnvelope` on the public surface, and conformance docs distinguish Jackson 2
      and Jackson 3 mapping support.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*ResourceMapperSpec'` passes, and the mapping
      suite also passes under `:jsonapi-java-jackson2:jackson2CompatibilityTest` (Jackson 2.22.x
      baseline, per [Jackson 2 Document Writer](jackson2-document-writer.md)).
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
