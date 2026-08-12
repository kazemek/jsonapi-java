# Phase 2.14 — Shared Domain Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceBinderSpec`  
> **Dependencies:** Phases 2.9, 2.11, 2.13, 2.27, and 2.28  
> **Status:** Not started

## Goal

Provide one version-neutral flat resource-to-DTO binding scenario catalog that proves binder parity
while preserving graph-free linkage-only semantics, on the unified Java `Scenario` retrieval
surface.

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
- Phase 2.13's relaxed catalog contract governs this milestone: catalogs grow by addition with
  stable ids, adapter suites run the whole catalog and assert full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`), and adapter-specific behavior is documented in
  adapter-local specs rather than enumerated in shared manifests. The earlier closed-index plus
  `Jackson3DomainReadExclusionManifest` / `DomainReadCoverage.assertExact` design is dropped.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade;
  `DomainReadScenario` implements `Scenario` and `DomainReadScenarios` implements
  `FixtureCatalog<DomainReadScenario>`, registered as `JsonApiFixtures.domainRead()`. Phases 2.27
  and 2.28 land first so main sources are Java-only and the unified surface exists before this
  catalog is born.
- Phase 2.14 owns the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.domainread` under
  `jsonapi-java-test-fixtures/src/main/java/`, `@NullMarked` per ADR-009. The initial catalog
  inventory is the closed shared `ResourceBinderSpec` test names below; it grows by addition and
  every entry is shared — there is no adapter-local classification inside the catalog.
- The ten Jackson-API-specific binder cases stay in the adapter test spec, documented there with
  their major-local harnesses (custom deserializer, naming strategy, mix-in, `JavaType` entry
  points, linkage mapper, Optional unwrapping, short-circuit, cardinality, `LINKAGE_MAPPING_FAILED`,
  and mapper-returning-null cases listed below). A later Jackson 2 binder suite runs every shared
  scenario with the same full-catalog coverage assertion and keeps its own adapter-local cases
  locally.
- Closed shared `ResourceBinderSpec` test names (initial inventory):
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
- Adapter-local `ResourceBinderSpec` cases by exact name (stay local, no manifest):
  `custom deserializer applies to attribute value`;
  `naming strategy renames bound attribute keys`; `mix-in attribute name is honored`;
  `JavaType entry points bind resource and collection`;
  `registered linkage mapper binds to-one single linkage and to-many collection`;
  `mapper receives Optional-unwrapped to-one type and collection to-many type`;
  `NullLinkage and empty linkage short-circuit without invoking the mapper`;
  `cardinality is enforced before the mapper is invoked`;
  `mapper exception is reported as LINKAGE_MAPPING_FAILED`;
  `mapper returning null binds null property`.
- Capability-tagged codec documents from Phase 2.12 (Complete) are optional inputs; they are not a
  hard prerequisite. This milestone does not force every wire fixture into DTO binding.
  Typed-envelope catalogs remain Phase 2.26.
- This is the largest fixture catalog to date (40 scenarios with expected values and diagnostics
  plus 16 DTO moves); it stays within the size gate but sits at the top edge of one reviewable
  commit — the natural fallback is two sequential commits under the same milestone (DTO move +
  repoints, then the catalog + spec refactor).

## Deliverables

- Move only the Jackson-neutral flat DTOs and reusable expected values specific to the initial
  shared binder inventory above that are not owned by Phase 2.13 into `jsonapi-java-test-fixtures`:
  `FlatArticle`, `FlatMutableArticle`, `FlatCreatorArticle`, `FlatInheritedBlog`/`FlatBlogBase`,
  `FlatIntIdArticle`, `FlatLidArticle`, `FlatDefaultedArticle`, `FlatThingWithIgnored`,
  `FlatArticleWithSet`, `FlatArticleWithArray`, `FlatArticleWithOptional`, `FlatRequiredThing`,
  `FlatThrowingCreatorThing`, `FlatCountedThing`, `FlatPersonArticle`, `FlatCommentArticle` (the
  adapter-local DTOs `FlatLoudThing`, `FlatWords`, `FlatNamedThing`/`FlatMixInDef`,
  `FlatMappedArticle`, `FlatMappedOptionalArticle`, `FlatAuthor`, and the envelope-only targets
  `FlatStrictArticle` / `FlatThrowingArticle` (plus the inline `UppercaseDeserializer` helper,
  which backs the adapter-local `FlatLoudThing` "custom deserializer applies to attribute value"
  case inside `ResourceBinderSpec`) stay in the jackson3 adapter test
  sources — inline in `ResourceBinderSpec` or its `testmodel` package),
  with `@NullMarked` package-info, accurate `@Nullable` on null-bearing members per ADR-009
  (expected members include `FlatArticle.{title, body, author, comments}`,
  `FlatLidArticle.id`, `FlatDefaultedArticle.title`, `FlatPersonArticle.author`,
  `FlatCommentArticle.comments`, `FlatArticleWithSet.title`, `FlatArticleWithArray.title`,
  `FlatArticleWithOptional.{title, author}`, `FlatThingWithIgnored.confidential` (null on every
  bound value), and `FlatMutableArticle.{id, title, author}` — per
  the null-constructed and attributes-omitted usages in `ResourceBinderSpec` /
  `DomainDocumentReaderSpec`, for `FlatMutableArticle` per its public no-arg constructor and
  mutable-field shape (the Phase 2.13 `SamplePojo` precedent), and for `FlatPersonArticle.author`
  / `FlatCommentArticle.comments` / `FlatArticleWithOptional.author` per the relationship-key
  absence leaving the property null), and
  Gradle dependencies on `jsonapi-java-core`, annotations, Phase 2.11 common contracts, and shared
  `jackson-annotations` only (no major-specific databind/core APIs). Moved POJO fixtures carry
  value-based `equals`/`hashCode` (records get them for free), so the "one expected bound value"
  comparison target is well-defined; array- or collection-typed members compare element-wise
  (per-property or content comparison, never record-equality identity for array components, as
  `FlatArticleWithArray` requires). Reuse
  `BlogWithJsonProperty`, `Comment`, and `Person` from
  `io.github.kazemek.jsonapi.testfixtures.domainwrite`; do not create duplicate records or take
  ownership of their package/import migration.
- Add `DomainReadScenarios` in the fixed `io.github.kazemek.jsonapi.testfixtures.domainread`
  package, following the Phase 2.28 pinned delegation surface (public static `all()`,
  `byId(String)`, and `where(Predicate)` delegating to a private `FixtureCatalog` instance
  reachable via a public static `catalog()` accessor): an additive `all()` catalog whose
  initial inventory is the closed shared binder names above (every entry carries expected DTO
  values and stable common diagnostics), plus `byId(String)`; no adapter-local classification
  lives inside the catalog. Include a `DomainReadScenariosCatalogSpec`-style integrity spec
  enforcing the size-independent invariants (unique ids, `byId(String)` round-trips, resolvable
  expectations), mirroring the Phase 2.13 catalog-integrity pattern; additive growth is enforced
  by the adapter suite's full-catalog coverage assertion, not by the integrity spec.
- Add `DomainReadScenario` implementing `Scenario` (stable `id()`, default `notes()`), pinning the
  payload shape: a discriminated input (either one input resource or a resource collection,
  mirroring the binder's `fromResource`/`fromResources` entry points, or the dual-document
  included-isolation variant below),   the target DTO `Class`, a
  version-neutral binder-configuration discriminator — a converter-behavior descriptor covering
  the default `convertValue` path, custom `IdentifierConverter.parse` inversion, parse
  throwing, and parse returning null, which the adapter suite maps onto its converter
  registration — and a discriminated expectation — either one expected bound value (with explicit
  null/presence states per property, `@Nullable` where absent/null is expected) or an expected
  diagnostic with its resource-relative property path for failure cases; every failure expectation
  carries the path, with stable paths added for the four shared cases that do not assert one
  today (`single linkage on to-many is a cardinality mismatch`, `empty collection linkage on
  to-one is a cardinality mismatch`, `explicit-null attribute into primitive property is
  UNSUPPORTED_ATTRIBUTE_VALUE`, and `creator throwing during instantiation is
  MISSING_CREATOR_INPUT`). The two relationship-cardinality paths are deterministic JSON:API
  pointers; the two attribute/creator paths are pinned from observed Jackson 3 binder behavior
  (`propertyPath(failure)`, the last property name of the databind exception) with that
  provenance recorded at implementation — if a path is not reproducible from version-neutral
  primitives, it stays adapter-local exactly like the cause-type assertions, rather than being
  forced into the additive catalog. Failure expectations may additionally carry the
  `resourceClass` where the spec asserts it (a shared common API value, not a major-specific
  cause type). Shared expectations carry
  only the diagnostic and property path; major-specific cause-type assertions (for example
  `ValueInstantiationException` after `creator throwing during instantiation`) remain
  adapter-local supplementary assertions in the adapter spec. The included-isolation scenario uses
  a dual-document input variant carrying both wire documents (identical primary data, differing
  `included`) with the expected stable bound value; the adapter suite parses both through its own
  reader and dispatches on the input-variant kind — never on the scenario id — and asserts both
  bind to the expected value. Register the catalog as `JsonApiFixtures.domainRead()` on the
  Phase 2.28 facade.
- Refactor Jackson 3 `ResourceBinderSpec` to consume the catalog after Phase 2.13's import-only
  migration, retaining the named adapter-local cases and preserving the Phase 2.13-owned shared
  model imports; additionally perform the import-only repoint of `DomainDocumentReaderSpec` onto
  the moved `FlatArticle`/`FlatLidArticle` shared types, keeping its behavior unchanged (the Phase
  2.13 repoint pattern). Record executed ids and require exact full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`), mirroring the Phase 2.13/2.18 write-suite rule.
  The DTO move plus repoints and the catalog plus `ResourceBinderSpec` refactor may land as two
  sequential commits under this milestone (the milestone is at the top edge of the size gate).
- Document the adapter-local case list and the full-catalog coverage rule in the adapter spec (so
  later Jackson 2 binder suites run every applicable shared scenario with their own adapter-local
  cases), and update the fixed domain-read package map, `DomainReadScenarios` / `DomainReadScenario`
  entry points, the `JsonApiFixtures` accessor, and agent notes via the `module-docs` skill.

## Non-goals

- Typed domain envelope catalogs; Phase 2.26 owns them.
- Making every codec fixture DTO-bindable.
- Graph hydration, relationship injection, persistence lookup, or PATCH fixtures.
- Sharing `JavaType`, mapper, or custom-deserializer implementations across Jackson majors.
- Closed catalog indexes, exclusion manifests, or coverage helpers that enumerate adapter-local
  ids; adapter-local cases live in adapter test specs only.
- Redefining or moving the Phase 2.13-owned `domainwrite` models and catalog; this milestone
  consumes those entry points and owns only the domain-read DTO fixtures and binder catalog.

## Implementation boundaries

- Shared DTOs and expectations depend on annotations, core, and common Jackson contracts but import
  no major-specific databind/core API.
- `io.github.kazemek.jsonapi.testfixtures.domainwrite` is a Phase 2.13-owned prerequisite;
  `BlogWithJsonProperty`, `Comment`, and `Person` are consumed from that package, while this
  milestone's DTO-specific models live in its separate domain-read package and must not duplicate
  the write models.
- `io.github.kazemek.jsonapi.testfixtures.domainread` is the fixed owner of `DomainReadScenario`
  and `DomainReadScenarios`; adapter-local binder cases are test-source code in the adapter spec,
  never shared main-source data.
- Binder expectations remain resource-relative; `included` is never read for relationship fields.
- Identifier primary data is out of binder scope unless listed above; do not invent new
  dual-interpretation binder fixtures.

## Test strategy

- Run every shared flat-binding scenario through Jackson 3 and compare complete values, null/presence
  states, and diagnostics; collect executed scenario ids from the parameterized suite and assert
  `executedScenarioIds == catalogScenarioIds`.
- Verify changes to `included` never alter primary DTO relationship fields.
- Add catalog integrity tests for unique ids, `byId(String)` round-trips, and resolvable
  expectations (size-independent invariants); additive growth pickup is proven by the adapter
  suite's `executedScenarioIds == catalogScenarioIds` assertion.
  (`DomainReadScenarios` exposes the Phase 2.28 `FixtureCatalog` contract through the pinned
  static delegation surface).

## Acceptance criteria

- [ ] The initial `DomainReadScenarios` catalog contains exactly the closed shared
      `ResourceBinderSpec` names above without major-specific production imports;
      `BlogWithJsonProperty`, `Comment`, and `Person` are imported from the Phase 2.13-owned
      `io.github.kazemek.jsonapi.testfixtures.domainwrite` package rather than duplicated; the ten
      adapter-local binder cases above remain in `ResourceBinderSpec` and appear in no shared
      catalog or manifest.
- [ ] Jackson 3 `ResourceBinderSpec` consumes the catalog for those shared names and retains only
      the named adapter-local cases locally; its Phase 2.13-repointed shared-model imports remain
      intact, `DomainDocumentReaderSpec` is import-only repointed onto the moved
      `FlatArticle`/`FlatLidArticle` with no `io.github.kazemek.jsonapi.jackson3.testmodel` import
      remaining for moved flat DTOs, the adapter spec documents its named adapter-local cases and
      the full-catalog coverage rule, Phase 2.13's write catalog is not edited by this refactor,
      and executed ids equal `DomainReadScenarios.all()*.id` exactly.
- [ ] Shared expectations preserve missing/null/linkage cardinality and never read `included`; new
      Java fixture packages are `@NullMarked` with accurate `@Nullable` per ADR-009.
- [ ] `DomainReadScenario` implements `Scenario` and `DomainReadScenarios` follows the Phase 2.28
      pinned delegation surface (public static `all()`/`byId(String)`/`where(Predicate)` plus the
      `catalog()` accessor); the four converter scenarios carry the version-neutral
      converter-behavior discriminator that the adapter suite maps onto its converter
      registration; `JsonApiFixtures.domainRead()`
      is registered on the Phase 2.28 facade; catalog integrity validates unique ids and resolvable
      expectations. The canonical `module-docs` checklist passes for the fixed
      `io.github.kazemek.jsonapi.testfixtures.domainread` package and its entry points.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
