# Phase 2.11 — Jackson Common Contracts

> **Scope:** `jsonapi-java-jackson-common` and `jsonapi-java-jackson3` public contracts  
> **Dependencies:** Phases 1.1, 1.2, and 2.10  
> **Status:** Not started

## Goal

Establish one Jackson-major-neutral production contract for codec and domain-mapping policy while
leaving Jackson-bound implementation and entry points in separately compiled major adapters.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) makes Jackson authoritative for Java properties while
  keeping optional integrations outside core; shared contracts may describe policy and results but
  must not replace Jackson introspection.
- [ADR-007](../../docs/adr/007-module-boundaries.md) requires separate Jackson 2 and Jackson 3
  artifacts with no runtime major detection. Amend it to add a common contract dependency without
  combining either major's implementation.
- [ADR-010](../../docs/adr/010-architectural-tests.md) requires exact production dependency
  allowlists for every source module.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) applies to every moved null-bearing contract;
  absence and explicit wire null must remain distinguishable, and `@Nullable` decorations must move
  with the types.
- Closed move inventory from current `io.github.kazemek.jsonapi.jackson3` public types (Jackson-
  import-free): `CodecFailureCategory`, `CompoundSerializationContext`, `DocumentEnvelope`,
  `DocumentReadContext`, `DomainData` (and its sealed variants), `FieldAllowance`, `FieldPolicy`,
  `IdentifierConverter`, `IncludePath`, `IncludePolicy`, `IncludedResources`,
  `JsonApiDocumentReadException`, `JsonApiMappingException`, `MappedDocument`, `MappingDiagnostic`,
  `PrimaryDataKind`, `RelationshipAllowance`, `SourceLocation`. Exclude every type whose signature
  exposes `JsonMapper`, `JavaType`, parser/generator, introspection, serializer, or deserializer
  APIs (`JsonApiJackson3`, readers/writers, binders, `JsonApiDomainDocument`,
  `ResourceTypeRegistry`, `RelationshipLinkageMapper`, `JsonApiResourceMapper`).
- `IncludedResources` today uses a package-private constructor from `JsonApiDomainDocumentReader`
  that takes a wire-order resource list and an identity index that must agree. After the move it
  must expose a public common-package construction API so major-specific readers can assemble it
  without package coupling. Prefer the simplest API that preserves the `resources()` ↔ identity-index
  invariant by making inconsistent states unrepresentable or by reliably rejecting them; do not
  simply publish the existing two-collection constructor without that guarantee, and do not
  prescribe a particular factory/constructor shape or add unnecessary validation complexity.
- Moved-type Javadocs must not `{@link}` Jackson-major-specific types excluded from the inventory
  (for example `JsonApiDomainDocument`, `JsonApiDomainDocumentReader`, `ResourceTypeRegistry`).
  Adapt `DomainData`, `IncludedResources`, and any other moved Javadoc that currently cites those
  types to neutral wording before or as part of the move.

## Deliverables

- Register and publish `jsonapi-java-jackson-common` with public package
  `io.github.kazemek.jsonapi.jackson`, depending only on public core/annotation contracts and
  compile-only JSpecify; add ArchUnit coverage for that exact boundary.
- Move exactly the closed inventory above into the common package, including a public
  `IncludedResources` construction API that preserves the wire-order/`find` identity-index
  invariant (unrepresentable or reliably rejected inconsistency), and with moved-type Javadocs
  free of Jackson-major-specific `{@link}` targets.
- Migrate Jackson 3 production code, tests, and public signatures to consume the common types and
  remove the former `io.github.kazemek.jsonapi.jackson3` duplicates without compatibility wrappers
  or semantic changes (visibility of `IncludedResources` construction may widen as specified).
- Update ADR-007, ADR-010, vision/conformance references, dependency verification, and publication
  metadata to record the common-contract dependency and unchanged major isolation.
- Use `module-docs` for the new module and changed Jackson 3 public surface, including package
  documentation, focused entry-point Javadoc, and the root module registry.

## Non-goals

- Sharing Jackson-bound readers, writers, mapping introspection, serializers, binders, module
  registration, or mapper factories.
- Reflection, source generation, runtime major detection, or a lowest-common-denominator Jackson
  abstraction.
- Implementing Jackson 2 or changing codec, mapping, inclusion, fieldset, binding, envelope, or
  PATCH semantics; presence-aware PATCH command types remain Phase 2.15.
- Retaining deprecated Jackson 3 aliases for the moved pre-alpha contracts.

## Implementation boundaries

- The common module imports no `tools.jackson.*` or `com.fasterxml.jackson.*` production types and
  has no runtime dependency on either Jackson major or adapter artifact.
- Major-specific factories and APIs remain under `io.github.kazemek.jsonapi.jackson2` and
  `io.github.kazemek.jsonapi.jackson3`; only neutral values cross through
  `io.github.kazemek.jsonapi.jackson`.
- Existing equality, defensive-copy, diagnostic-code, nullness, presence, ordering, policy-default,
  and validation-context behavior moves unchanged except for the documented `IncludedResources`
  construction visibility/API change and moved-Javadoc neutralization.
- Public `IncludedResources` construction keeps `resources()` wire order and `find` identity-index
  results consistent; construction API design is otherwise open to the implementer.
- `jsonapi-java-core` and `jsonapi-java-annotations` must not depend on the common module.

## Test strategy

- Move neutral unit/contract tests with their types and run the existing Jackson 3 integration
  suites against the new package to prove behavior is unchanged.
- Add ArchUnit and Gradle dependency assertions for the common module's JDK/JSpecify/core/annotation
  allowlist and for the absence of either Jackson major.
- Verify Jackson 3 exposes no duplicate moved type and that its major-specific signatures continue
  to expose only Jackson 3 APIs.

## Acceptance criteria

- [ ] `jsonapi-java-jackson-common` is a published, `@NullMarked` module whose production/runtime
      graph contains no Jackson major or adapter, whose canonical package is
      `io.github.kazemek.jsonapi.jackson`, and whose moved null-bearing members retain accurate
      `@Nullable` decorations per ADR-009.
- [ ] Exactly the closed inventory types are moved, each retains its Phase 2.1–2.10 semantics, and
      none remain as public duplicates under `io.github.kazemek.jsonapi.jackson3`.
- [ ] `IncludedResources` is assemblable from major-specific readers via a public common-package
      construction API that preserves the `resources()` ↔ identity-index invariant (inconsistent
      states unrepresentable or reliably rejected) without package-private coupling.
- [ ] Jackson 3 codec, mapping, inclusion, fieldset, binder, and envelope entry points consume the
      common contracts while all Jackson-bound signatures and implementation remain major-specific;
      moved-type Javadocs do not `{@link}` excluded Jackson-major-specific types.
- [ ] ADR-007/ADR-010, vision/conformance, publication metadata, dependency verification, and the
      canonical `module-docs` checklist pass for the new common module and changed Jackson 3 surface.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
