# Phase 2.14 — Shared Domain Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceBinderSpec`  
> **Dependencies:** Phases 2.9, 2.11, and 2.13
> **Status:** Not started

## Goal

Provide one version-neutral flat resource-to-DTO binding scenario catalog that proves binder parity
while preserving graph-free linkage-only semantics.

## Research and constraints

- Phase 2.9 defines validated resource-to-DTO binding independently of JSON parsing and included
  resources; shared cases must not collapse binder and document-reader responsibilities.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) requires linkage-only relationship binding
  and never reads `included` for DTO relationships.
- Phase 2.13 is the authoritative owner of
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` and its shared `BlogWithJsonProperty`,
  `Comment`, and `Person` models. Phase 2.13 performs the import-only migration of
  `ResourceBinderSpec`; this milestone performs the later binder-catalog extraction after that
  prerequisite and must reuse those types rather than move, redefine, or duplicate them.
- Phase 2.14 owns the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.domainread` under
  `jsonapi-java-test-fixtures/src/main/java/`. `DomainReadScenarios.all()` is the complete closed
  `ResourceBinderSpec` index, including both `SHARED` entries with reusable DTO fixtures and
  `ADAPTER_LOCAL` entries represented only by stable ids and exclusion metadata;
  `DomainReadScenarios.shared()` selects the reusable cases and `byId(String)` resolves either kind.
- The adapter-local `Jackson3DomainReadExclusionManifest` contains the ten exact adapter-only
  scenario ids currently listed below, with non-blank reasons. A reusable neutral
  `DomainReadCoverage.assertExact(catalogIds, executedIds, excludedIds)` requires
  `executedIds == catalogIds - excludedIds`, rejects unknown/duplicate ids, and is the coverage
  source of truth for Jackson 3 and later Jackson 2 binder suites. The manifest is adapter-local;
  the shared package owns only `DomainReadScenario`, `DomainReadScenarios`, and the coverage helper.
- Closed shared `ResourceBinderSpec` test names:
  `binds record with id, attributes, and built-in ResourceIdentifier relationships`;
  `binds mutable POJO`; `binds immutable creator-based POJO`; `binds inherited properties`;
  `binds @JsonProperty named attribute`; `@JsonIgnore property is not bound`;
  `default identifier conversion binds non-String id via convertValue`;
  `custom IdentifierConverter parse inverts the wire form`;
  `lid-only resource binds into identifier property`;
  `resource without id or lid omits the identifier property`;
  `explicit-null attribute binds null and omitted attribute keeps its default`;
  `unmapped resource attributes are ignored`;
  `fromResources binds homogeneous collection in order`;
  `fromResources validates every element type`;
  `omitted to-one relationship key is not bound`;
  `links-or-meta-only to-one relationship is not bound`;
  `NullLinkage on to-one binds null`;
  `collection linkage on to-one is a cardinality mismatch`;
  `empty collection linkage on to-many binds empty collection`;
  `empty collection linkage on to-many binds empty Set`;
  `empty collection linkage on to-many binds empty array`;
  `non-empty collection linkage on to-many binds List`;
  `non-empty collection linkage on to-many binds Set`;
  `non-empty collection linkage on to-many binds array`;
  `NullLinkage on to-many is a cardinality mismatch`;
  `single linkage on to-many is a cardinality mismatch`;
  `empty collection linkage on to-one is a cardinality mismatch`;
  `NullLinkage on Optional to-one binds empty Optional`;
  `SingleLinkage on Optional to-one binds present Optional`;
  `resource type mismatch is RESOURCE_TYPE_MISMATCH at /type`;
  `unregistered to-one relationship target is UNSUPPORTED_RELATIONSHIP_TARGET`;
  `unregistered to-many relationship target is UNSUPPORTED_RELATIONSHIP_TARGET`;
  `identifier parse exception is IDENTIFIER_CONVERSION_FAILED at /id`;
  `identifier parse returning null is IDENTIFIER_CONVERSION_FAILED`;
  `identifier coercion failure is IDENTIFIER_CONVERSION_FAILED`;
  `absent required creator property is MISSING_CREATOR_INPUT`;
  `creator throwing during instantiation is MISSING_CREATOR_INPUT`;
  `attribute value that cannot coerce is UNSUPPORTED_ATTRIBUTE_VALUE`;
  `explicit-null attribute into primitive property is UNSUPPORTED_ATTRIBUTE_VALUE`;
  `binder never sees document included resources`.
- Adapter-local exclusions by exact name: `custom deserializer applies to attribute value`;
  `naming strategy renames bound attribute keys`; `mix-in attribute name is honored`;
  `JavaType entry points bind resource and collection`;
  `registered linkage mapper binds to-one single linkage and to-many collection`;
  `mapper receives Optional-unwrapped to-one type and collection to-many type`;
  `NullLinkage and empty linkage short-circuit without invoking the mapper`;
  `cardinality is enforced before the mapper is invoked`;
  `mapper exception is reported as LINKAGE_MAPPING_FAILED`;
  `mapper returning null binds null property`.
- Capability-tagged codec documents from Phase 2.12 are optional inputs after that milestone lands;
  they are not a hard prerequisite. This milestone does not force every wire fixture into DTO
  binding. Typed-envelope catalogs remain Phase 2.26.

## Deliverables

- Move only the Jackson-neutral flat DTOs and reusable expected values specific to the closed shared
  binder test names that are not owned by Phase 2.13 into `jsonapi-java-test-fixtures`, with
  `@NullMarked` package-info, accurate `@Nullable` on null-bearing members per ADR-009, and Gradle
  dependencies on `jsonapi-java-core`, annotations, Phase 2.11 common contracts, and shared
  `jackson-annotations` only (no major-specific databind/core APIs). Reuse
  `BlogWithJsonProperty`, `Comment`, and `Person` from
  `io.github.kazemek.jsonapi.testfixtures.domainwrite`; do not create duplicate records or take
  ownership of their package/import migration.
- Add `DomainReadScenarios` in the fixed `io.github.kazemek.jsonapi.testfixtures.domainread`
  package. Its complete `all()` index contains every closed shared and adapter-local scenario id;
  shared entries carry expected DTO values and stable common diagnostics, while adapter-local
  entries carry their capability and exclusion metadata without major-specific fixture types.
  Expose `shared()`, `all()`, and `byId(String)` plus the neutral `DomainReadCoverage` helper.
- Refactor Jackson 3 `ResourceBinderSpec` to consume the catalog after Phase 2.13's import-only
  migration, while retaining the named adapter-local cases and preserving the Phase 2.13-owned
  shared model imports. Record executed ids and require exact coverage using
  `DomainReadCoverage.assertExact(catalogIds, executedIds, excludedIds)` against the adapter-local
  `Jackson3DomainReadExclusionManifest`.
- Document capability selection, the ten-entry Jackson 3 exclusion manifest, and the exact
  `catalogIds - excludedIds` coverage rule so later Jackson 2 binder suites can define their own
  adapter-local manifest and run every applicable shared scenario.
- Use `module-docs` for the fixed domain-read package map, `DomainReadScenarios`/`DomainReadCoverage`
  entry points, and agent notes.

## Non-goals

- Typed domain envelope catalogs; Phase 2.26 owns them.
- Making every codec fixture DTO-bindable.
- Graph hydration, relationship injection, persistence lookup, or PATCH fixtures.
- Sharing `JavaType`, mapper, or custom-deserializer implementations across Jackson majors.
- Redefining or moving the Phase 2.13-owned `domainwrite` models, catalog, or shared exclusion
  value types; this milestone consumes those entry points and owns only the domain-read DTO
  fixtures, coverage helper, and binder catalog. Adapter-local write manifests remain owned by
  their respective adapters.

## Implementation boundaries

- Shared DTOs and expectations depend on annotations, core, and common Jackson contracts but import
  no major-specific databind/core API.
- `io.github.kazemek.jsonapi.testfixtures.domainwrite` is a Phase 2.13-owned prerequisite;
  `BlogWithJsonProperty`, `Comment`, and `Person` are consumed from that package, while this
  milestone's DTO-specific models live in its separate domain-read package and must not duplicate
  the write models.
- `io.github.kazemek.jsonapi.testfixtures.domainread` is the fixed owner of
  `DomainReadScenario`, `DomainReadScenarios`, and `DomainReadCoverage`; adapter-specific exclusion
  manifests remain in adapter test source sets and are not shared main-source data.
- Binder expectations remain resource-relative; `included` is never read for relationship fields.
- Identifier primary data is out of binder scope unless listed above; do not invent new
  dual-interpretation binder fixtures.

## Test strategy

- Run each shared flat-binding scenario through Jackson 3 and compare complete values, null/presence
  states, and diagnostics; collect executed scenario ids from the parameterized suite and call
  `DomainReadCoverage.assertExact` with the Jackson 3 local exclusions.
- Verify changes to `included` never alter primary DTO relationship fields.
- Add catalog tests for unique ids, declared binder capabilities, exact full-index/shared-subset
  classification, the ten-entry exclusion manifest, and exact executed-id coverage.

## Acceptance criteria

- [ ] Exactly the closed shared `ResourceBinderSpec` test names are present in the shared catalog
      without major-specific production imports; `BlogWithJsonProperty`, `Comment`, and `Person`
      are imported from the Phase 2.13-owned `io.github.kazemek.jsonapi.testfixtures.domainwrite`
      package rather than duplicated; `DomainReadScenarios.all()` contains the complete closed
      index, `shared()` contains exactly the reusable cases, and the ten adapter-local ids remain
      local in `Jackson3DomainReadExclusionManifest`.
- [ ] Jackson 3 `ResourceBinderSpec` consumes the catalog for those shared names and retains only
      the named adapter-local cases locally; its Phase 2.13-repointed shared-model imports remain
      intact, Phase 2.13's write catalog is not edited by this refactor, and executed ids satisfy
      `catalogIds - Jackson3DomainReadExclusionManifest.all()*.scenarioId` exactly.
- [ ] Shared expectations preserve missing/null/linkage cardinality and never read `included`; new
      Java fixture packages are `@NullMarked` with accurate `@Nullable` per ADR-009.
- [ ] Catalog integrity validates the full index, shared/adapter-local capability classification,
      the ten-entry exclusion manifest, and `DomainReadCoverage.assertExact`; an adapter cannot
      omit an applicable shared case. The canonical `module-docs` checklist passes for the fixed
      `io.github.kazemek.jsonapi.testfixtures.domainread` package and its
      `DomainReadScenarios`/`DomainReadCoverage` entry points.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
