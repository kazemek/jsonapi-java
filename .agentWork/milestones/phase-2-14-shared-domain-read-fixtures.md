# Phase 2.14 — Shared Domain Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceBinderSpec`  
> **Dependencies:** Phases 2.9, 2.11, 2.13, 2.27, and 2.28  
> **Status:** Complete

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
  exclusion-manifest design is dropped.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade;
  `DomainReadScenario` implements `Scenario`, and `DomainReadScenarios` exposes the
  `FixtureCatalog<DomainReadScenario>` contract through the Phase 2.28 pinned static delegation
  surface, registered as
  `JsonApiFixtures.domainRead()` (see the Phase 2.28 milestone for the pinned surface). Phases
  2.27
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
  repoints, then the catalog + spec refactor). This two-commit sequence is a deliberate,
  recorded size-gate decision (the Phase 2.13 precedent also landed multiple commits): the
  milestone remains one coherent outcome, commit 1 is independently buildable, and the milestone
  review treats the pair as one reviewable unit, verifying the commit-1 checkpoint explicitly.

## Deliverables

- Move the Jackson-neutral flat DTOs and reusable expected values specific to the initial
  shared binder inventory above that are not owned by Phase 2.13 into `jsonapi-java-test-fixtures`:
  **Move:** `FlatArticle`, `FlatMutableArticle`, `FlatCreatorArticle`,
  `FlatInheritedBlog`/`FlatBlogBase`, `FlatIntIdArticle`, `FlatLidArticle`, `FlatDefaultedArticle`,
  `FlatThingWithIgnored`, `FlatArticleWithSet`, `FlatArticleWithArray`, `FlatArticleWithOptional`,
  `FlatRequiredThing`, `FlatThrowingCreatorThing`, `FlatCountedThing`, `FlatPersonArticle`,
  `FlatCommentArticle`.
  **Stay local (jackson3 adapter test sources, inline in `ResourceBinderSpec` or its `testmodel`
  package):** `FlatLoudThing`, `FlatWords`, `FlatNamedThing`/`FlatMixInDef`,
  `FlatMappedArticle`, `FlatMappedOptionalArticle`, `FlatAuthor`, the envelope-only targets
  `FlatStrictArticle` / `FlatThrowingArticle` (stay local for this milestone; Phase 2.26 moves
  them into shared fixtures for the envelope-read catalog, superseding this stay-local
  statement), and the inline `UppercaseDeserializer` helper, which backs the adapter-local
  `FlatLoudThing` "custom deserializer applies to attribute value" case inside
  `ResourceBinderSpec`.
  The moved fixtures ship
  with `@NullMarked` package-info, accurate `@Nullable` on null-bearing members per ADR-009
  (expected members include `FlatArticle.{title, body, author, comments}`,
  `FlatLidArticle.id`, `FlatDefaultedArticle.{id, title}`, `FlatPersonArticle.{id, author}`,
  `FlatCommentArticle.{id, comments}`, `FlatArticleWithSet.title`, `FlatArticleWithArray.title`,
  `FlatArticleWithOptional.{title, author}`, `FlatThingWithIgnored.{id, name, confidential}`
  (`confidential` null on every bound value), `FlatBlogBase.{id, name}`,
  `FlatInheritedBlog.description`, and
  `FlatMutableArticle.{id, title, author}` — per
  the null-constructed and attributes-omitted usages in `ResourceBinderSpec` /
  `DomainDocumentReaderSpec`, and for the mutable-field POJOs (`FlatMutableArticle`,
  `FlatPersonArticle`, `FlatCommentArticle`, `FlatCountedThing`, `FlatDefaultedArticle`,
  `FlatThingWithIgnored`, `FlatBlogBase`, `FlatInheritedBlog`) per their public no-arg
  constructor and mutable-field
  shape (the Phase 2.13 `SamplePojo` precedent); `FlatArticleWithOptional.author` (an `Optional`
  component) is marked `@Nullable` as inferred from the moved shape — the definitive decoration is
  re-derived from the moved type shapes at implementation, with AC 3 and NullAway as the
  enforcing gates), and
  Gradle dependencies on `jsonapi-java-core`, annotations, Phase 2.11 common contracts, and shared
  `jackson-annotations` only (no major-specific databind/core APIs). Moved POJO fixtures gain
  value-based `equals`/`hashCode` (records get them for free), and the adapter suite compares
  expected versus bound values element-wise per property (null/presence-aware), using value-based
  `equals` where the DTO defines it — array components always compare element-wise (records cannot
  override `equals`, so `FlatArticleWithArray` never uses record equality). Reuse
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
  enforcing the size-independent invariants (unique ids, `byId(String)` round-trips, and
  resolvable expectations — every entry references a target DTO class present in the shared
  packages, carries exactly one input variant, exactly one converter-behavior discriminator, and
  either a complete bound-value expectation or a known diagnostic, with a
  `@Nullable` path member where the shared expectation carries one), mirroring the Phase 2.13
  catalog-integrity pattern; additive growth is enforced
  by the adapter suite's full-catalog coverage assertion, not by the integrity spec.
- Add `DomainReadScenario` implementing `Scenario` (stable `id()`, default `notes()`), pinning the
  payload shape: a discriminated input (either one input resource or a resource collection,
  mirroring the binder's `fromResource`/`fromResources` entry points, or the dual-document
  included-isolation variant below),   the target DTO `Class`, a
  version-neutral binder-configuration discriminator — a converter-behavior descriptor covering
  the default `convertValue` path, custom `IdentifierConverter.parse` inversion, parse
  throwing, and parse returning null, which the adapter suite maps onto its converter
  registration (the `identifier coercion failure is IDENTIFIER_CONVERSION_FAILED` scenario
  reuses the default-`convertValue` descriptor value with a failure expectation) — and a discriminated expectation — either one expected bound value (with explicit
  null/presence states per property, `@Nullable` where absent/null is expected) or an expected
  diagnostic with its resource-relative property path for failure cases. The shared failure
  expectations carry paths exhaustively, preserving every assertion `ResourceBinderSpec` makes
  today (none is dropped), per this classification of the shared failure scenarios:
  binder-level deterministic paths are shared — `/relationships/<name>/data` for the four
  cardinality-mismatch cases (`collection linkage on to-one is a cardinality mismatch`,
  `NullLinkage on to-many is a cardinality mismatch`, plus stable paths added for `single linkage
  on to-many is a cardinality mismatch` and `empty collection linkage on to-one is a cardinality
  mismatch`), `/type` for `resource type mismatch is RESOURCE_TYPE_MISMATCH at /type` and
  `fromResources validates every element type`, `/id` for the three `IDENTIFIER_CONVERSION_FAILED`
  cases (`identifier parse exception`, `identifier parse returning null`, `identifier coercion
  failure`), and `/relationships/<name>/data` for the two `UNSUPPORTED_RELATIONSHIP_TARGET`
  cases. Jackson-derived property-name paths — `/required` on `absent required creator property
  is MISSING_CREATOR_INPUT`, `/count` on `attribute value that cannot coerce is
  UNSUPPORTED_ATTRIBUTE_VALUE`, and the paths of `explicit-null attribute into primitive property
  is UNSUPPORTED_ATTRIBUTE_VALUE` and `creator throwing during instantiation is
  MISSING_CREATOR_INPUT` (observed Jackson 3 binder behavior — `propertyPath(failure)`, the last
  property name of the databind exception, provenance recorded at implementation) — are asserted
  only as adapter-local supplementary assertions, exactly like the cause-type assertions, until
  the Jackson 2 binder suite proves them portable, at which point they move into the shared
  expectations. The two added cardinality paths carry no portability hedge: they are produced by
  the binder's deterministic `relationshipPath` (JSON:API pointers, not databind-derived paths).
  `resourceClass` is shared where the spec asserts it (the `RESOURCE_TYPE_MISMATCH`
  case; a shared common API value, not a major-specific cause type). Shared expectations carry
  only the diagnostic and property path; major-specific cause-type assertions (for example
  `ValueInstantiationException` after `creator throwing during instantiation`) remain
  adapter-local supplementary assertions in the adapter spec. The included-isolation scenario uses
  a dual-document input variant carrying both wire documents (identical primary data, differing
  `included`) with the expected stable bound value; the adapter suite parses both through its own
  reader (a `DocumentReadContext.resourceDefaults()`-style reader context — the binder never reads
  `included` anyway) and dispatches on the input-variant kind — never on the scenario id — and
  asserts both bind to the expected value. Register the catalog as `JsonApiFixtures.domainRead()`
  on the Phase 2.28 facade.
- Refactor Jackson 3 `ResourceBinderSpec` to consume the catalog after Phase 2.13's import-only
  migration, retaining the named adapter-local cases and preserving the Phase 2.13-owned shared
  model imports; additionally perform the import-only repoint of `DomainDocumentReaderSpec` onto
  the moved `FlatArticle`/`FlatLidArticle` shared types, keeping its behavior unchanged (the Phase
  2.13 repoint pattern). Record executed ids and require exact full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`), mirroring the Phase 2.13/2.18 write-suite rule.
  After the repoints land, the moved `testmodel` files and the nested classes belonging to the
  moved DTOs are deleted from the jackson3 test sources (the Phase 2.13 deletion precedent); the
  adapter-local fixtures and helpers listed above (including `UppercaseDeserializer`) are
  retained.
  The reviewable-unit contract is the explicit two-commit sequence: commit 1 = DTO move +
  repoints with a green build (import-only, behavior unchanged); commit 2 = the catalog plus
  `ResourceBinderSpec` refactor. The milestone review verifies the pair as one unit, checking the
  commit-1 checkpoint (green build, import-only repoints) explicitly (the milestone is at the top
  edge of the size gate).
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
  static delegation surface.) A facade-spec assertion covers `JsonApiFixtures.domainRead()`
  view identity and the `where` shim, per the Phase 2.28 facade-spec pattern.

## Acceptance criteria

- [x] The initial `DomainReadScenarios` catalog contains exactly the closed shared
      `ResourceBinderSpec` names above without major-specific production imports;
      `BlogWithJsonProperty`, `Comment`, and `Person` are imported from the Phase 2.13-owned
      `io.github.kazemek.jsonapi.testfixtures.domainwrite` package rather than duplicated; the ten
      adapter-local binder cases above remain in `ResourceBinderSpec` and appear in no shared
      catalog or manifest.
- [x] Jackson 3 `ResourceBinderSpec` consumes the catalog for those shared names and retains only
      the named adapter-local cases locally; its Phase 2.13-repointed shared-model imports remain
      intact, `DomainDocumentReaderSpec` is import-only repointed onto the moved
      `FlatArticle`/`FlatLidArticle` with no `io.github.kazemek.jsonapi.jackson3.testmodel` import
      remaining for moved flat DTOs, the adapter spec documents its named adapter-local cases and
      the full-catalog coverage rule, Phase 2.13's write catalog is not edited by this refactor,
      and executed ids equal `DomainReadScenarios.all()*.id` exactly.
- [x] Shared expectations preserve missing/null/linkage cardinality and never read `included`;
      every `propertyPath`/`resourceClass` assertion `ResourceBinderSpec` makes today is
      preserved either in the shared expectations or as adapter-local supplementary assertions
      (none is dropped); new
      Java fixture packages are `@NullMarked` with accurate `@Nullable` per ADR-009.
- [x] `DomainReadScenario` implements `Scenario` and `DomainReadScenarios` follows the Phase 2.28
      pinned delegation surface (public static `all()`/`byId(String)`/`where(Predicate)` plus the
      `catalog()` accessor); the five converter scenarios carry the version-neutral
      converter-behavior discriminator that the adapter suite maps onto its converter
      registration; `JsonApiFixtures.domainRead()`
      is registered on the Phase 2.28 facade; catalog integrity validates unique ids and resolvable
      expectations. The canonical `module-docs` checklist passes for the fixed
      `io.github.kazemek.jsonapi.testfixtures.domainread` package and its entry points.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
