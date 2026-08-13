# Phase 2.24 — Shared Compound Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `CompoundSerializationSpec`  
> **Dependencies:** Phases 2.3, 2.11, 2.13, and 2.28  
> **Status:** Complete

## Goal

Extract one version-neutral compound-inclusion scenario catalog from Phase 2.3 domain-graph tests
for cross-major parity.

## Research and constraints

- Phase 2.3 `CompoundSerializationSpec` owns the closed shared semantic inventory (all 30 current
  tests; verified against the spec). Concurrent isolation is shared when it only needs shared
  models (include it). The neutral inclusion-policy contract tests that Phase 2.11 moved to
  jackson-common (`negative limits are rejected`, `factory-time malformed paths fail with raw
  input`, `canonical constructor rejects whitespace and dotted segments`, `equivalent policies
  compare equal`) are contract tests of the common types and stay in `JacksonCommonContractsSpec`;
  they are not part of this domain-graph catalog.
- Closed shared set from `CompoundSerializationSpec.groovy` (initial inventory; the catalog grows
  by addition):
  `context-free overloads omit included`; `empty include path list omits included`; `includes nested
  intermediates for comments.author`; `shared identity is included once`; `empty resolution emits
  included empty array`; `self-reference primary is not re-emitted in included`; `prefix-overlapping
  paths traverse suffixes`; `converging different-suffix paths still traverse`; `conflicting
  representations fail`; `off-path relationships are not read for inclusion traversal`;
  `heterogeneous collection fails on later type`; `one-shot iterable is materialized once`; `nested
  policy matches owner resource type`; `nested policy denies wrong owner type`; `maxDepth zero
  rejects non-empty path`; `path longer than maxDepth fails`; `maxIncluded zero fails on first
  included resource`; `maxIncluded exceeded fails`; `mapper-time unknown relationship fails`; `denied
  relationship fails before traversal`; `multi-failure precedence is depth then mapping then policy
  in path order`; `multi-failure mapping beats policy on the same segment`; `multi-failure
  request-list order prefers first path mapping over later policy`; `multi-failure nested segment
  mapping beats later-segment policy`; `runtime nested owner type re-checks include policy`; `empty
  primary collection still enforces maxDepth`; `cyclic graph with repeated segment path terminates`;
  `multi-primary multi-path first-discovery order`; `deep nested path includes the chain`;
  `concurrent compound mappings isolate included sets`.
- Adapter-local: empty today (no Jackson-API-specific compound cases); retain only mapper isolation
  locally if a future major-specific case appears.
- Phase 2.13's checked criterion that its helper inventory remains local in `jackson3.testmodel`
  is superseded for exactly the four helpers it pinned local — `ConflictArticle`,
  `AccessCountingArticle`, `BaseComment`, `ModeratedComment`: the compound catalog cannot be
  shared across majors without its graph builders in `jsonapi-java-test-fixtures`, so they move
  with this milestone. The other four moved builders (`CyclicNode`, `DeepNode`, `LinkedArticle`,
  `PolymorphicArticle`) were never pinned local by Phase 2.13 (they are compound-catalog-specific
  models). The remaining Phase 2.13 helpers (`ArticleWithArray`, `ArticleWithOptionalRelationship`,
  plus `ArticleWithRenamedAuthor`/`AccessCountingFieldsetArticle`, which Phase 2.25 moves for the
  fieldset catalog) are not part of this move set. A note recording the supersession is added to
  the Phase 2.13 milestone file.
- Absolute getter-read counts decompose into linkage reads plus traversal reads (for example
  `authorReads == 2` is one linkage read on the selected primary plus one traversal read); the
  shared expectation is scoped to a major-neutral, binary assertion shape: the scenario input is
  executed twice — once with the include path and once with an empty include-path baseline — and
  the adapter asserts equal off-path counts across the two runs (`expectedTraversalDelta == 0`
  for the off-path relationship, measured as count-with-include minus count-without-include), so
  traversal adds zero reads. Absolute counts remain adapter-suite assertions in the Jackson 3
  spec, since per ADR-004 each major's mapping is authoritative over its own property-access
  patterns.
- Phase 2.28 owns the `Scenario` / `FixtureCatalog` contract and the `JsonApiFixtures` facade; the
  catalog is `CompoundWriteScenarios` in the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.compoundwrite` under `src/main/java/` (`@NullMarked` per
  ADR-009), exposing the `FixtureCatalog<CompoundWriteScenario>` contract through the Phase 2.28
  pinned static delegation surface and registered as
  `JsonApiFixtures.compoundWrite()`. Adapter suites run the whole catalog and assert full coverage
  (`executedScenarioIds == catalogScenarioIds`) per the Phase 2.13 relaxed contract — no exclusion
  manifest.
- Canonical codec compound documents do not replace domain-graph traversal proofs.

## Deliverables

- Add `CompoundWriteScenarios` (fixed `compoundwrite` package, `@NullMarked`) as an immutable
  compound-inclusion scenario catalog covering the initial inventory above with expected included
  resources, order, and common diagnostics, exposing
  the `FixtureCatalog<CompoundWriteScenario>` contract through the Phase 2.28 pinned static
  delegation surface (`all()`/`byId(String)`/`where(Predicate)` statics plus a `catalog()`
  accessor) and registered as
  `JsonApiFixtures.compoundWrite()` on the Phase 2.28 facade. Expectation members that must
  represent absent `included` distinctly from an empty array are `@Nullable` with accurate
  decoration per ADR-009. Scenario inputs are supplier-based and re-created per execution
  (following the Phase 2.13 `DomainWriteInput` pattern), so the one-shot iterable scenario gets a
  fresh iterable on every run. Each `CompoundWriteScenario` pins its request side: the domain
  input (supplier), the include paths, the common `IncludePolicy`/allowance configuration, and
  the depth/count limits (`maxDepth`, `maxIncluded`), plus the off-path relationship name where
  the traversal-delta assertion applies. The concurrent-isolation scenario is a two-mapping variant carrying
  two inputs (each with its serialization context and expected included list) executed concurrently
  against one mapper with an isolation assertion; the shared expectation carries the
  traversal-delta assertion (`expectedTraversalDelta == 0` for the off-path relationship, measured
  by the adapter as count-with-include minus count-without-include over two runs of the input) for
  scenarios whose models observe access, while absolute getter-read
  counts remain Jackson 3 suite-local assertions.
- Move the Jackson-neutral graph builders the scenarios require into the fixed
  `io.github.kazemek.jsonapi.testfixtures.compoundwrite` package alongside the catalog (mirroring
  the `domainwrite` co-location precedent): `AccessCountingArticle`, `BaseComment`,
  `ConflictArticle`, `CyclicNode`, `DeepNode`, `LinkedArticle`, `ModeratedComment`,
  `PolymorphicArticle` (from
  `jackson3.testmodel`, imports only annotations/domainwrite/JDK — major-neutral). `PolymorphicArticle`
  resolves `BaseComment` in the same package, so the two move together; all remaining jackson3
  references are repointed to the shared package. The moved builders retain accurate `@Nullable`
  on their null-bearing members under the `@NullMarked` package per ADR-009 (`LinkedArticle.related`,
  `DeepNode.child`, `CyclicNode.child`, `BaseComment.id`/`body`/`author` via its no-arg
  constructor, and any further member the inventory constructs with null).
- Refactor Jackson 3 `CompoundSerializationSpec` to consume the catalog with a full-catalog
  coverage assertion (`executedScenarioIds == catalogScenarioIds`); adapter-local remains empty
  unless a Jackson-API-specific case appears.
- Add catalog integrity tests for unique ids, resolvable expectations (every entry carries a
  complete, resolvable expectation; success entries explicitly represent the absent-`included`
  versus present-empty-array state), and the `FixtureCatalog` contract — the catalog enforces no
  completeness enumeration against external lists (no explicit exclusions).
- Update test-fixtures documentation for compound-write scenarios via the `module-docs` skill
  (fixed `compoundwrite` package map, `CompoundWriteScenarios`/`CompoundWriteScenario` entry
  points, `JsonApiFixtures.compoundWrite()` accessor, agent notes, and the root `README.md`
  Project-structure row refresh naming the shared compound-write catalog).

## Non-goals

- Sparse fieldsets; Phase 2.25 owns them.
- Flat mapping extraction (Phase 2.13) or Jackson 2 compound implementation (Phase 2.19).
- Sharing production inclusion engines.
- Closed catalog indexes or adapter-local exclusion manifests; the catalog grows by addition.

## Implementation boundaries

- Scenarios depend on common compound policy types and shared write models only.
- Traversal scenarios preserve access-vs-linkage, snapshot, and visit-state contracts from Phase
  2.3.

## Test strategy

- Parameterize the initial inventory through Jackson 3, compare included resources, order,
  linkage, and diagnostics, and assert full-catalog coverage
  (`executedScenarioIds == catalogScenarioIds`); the Jackson 3 suite keeps its suite-local
  round-trip serialization checks for the five scenarios that currently perform them (`includes
  nested intermediates for comments.author`, `shared identity is included once`,
  `self-reference primary is not re-emitted in included`, `cyclic graph with repeated segment
  path terminates`, `multi-primary multi-path first-discovery order`), and asserts the
  absolute getter-read counts locally.
- Catalog integrity rejects any scenario whose expectation components are incomplete or
  unresolvable.

## Acceptance criteria

- [x] The closed shared `CompoundSerializationSpec` inventory (all 30 named tests above) is present
      as the initial `CompoundWriteScenarios` catalog exposing the
      `FixtureCatalog<CompoundWriteScenario>` contract through the Phase 2.28 pinned static
      delegation surface (`all()`/`byId(String)`/`where(Predicate)` plus `catalog()`;
      `compoundwrite` package, `@NullMarked` with accurate
      `@Nullable` on every null-bearing expectation member — explicitly the absent-`included`
      state distinct from a present-empty array — per ADR-009; `JsonApiFixtures.compoundWrite()`
      registered) without major-specific production imports; the four jackson-common contract
      tests stay in `JacksonCommonContractsSpec`; adapter-local is empty unless a
      Jackson-API-specific case is documented.
- [x] Jackson 3 `CompoundSerializationSpec` consumes the catalog for that set and asserts
      `executedScenarioIds == catalogScenarioIds`.
- [x] Every catalog scenario's expectation encodes the included-resource order (first-discovery),
      the include-policy outcome, and the `MappingDiagnostic` code / `resourceClass` /
      `propertyPath` where the scenario fails; scenarios whose models observe access additionally
      carry the traversal-delta assertion (`expectedTraversalDelta == 0`, measured by the adapter
      over two runs with and without the include path); the concurrent-isolation variant carries
      both mappings and is executed concurrently by the Jackson 3 suite, which asserts the
      absolute access counts and round-trip checks locally.
- [x] The eight graph builders move together into `jsonapi-java-test-fixtures` (`PolymorphicArticle`
      with `BaseComment`), retain accurate `@Nullable` on their null-bearing members under the
      `@NullMarked` package per ADR-009, jackson3 references are repointed, and catalog integrity
      and the canonical `module-docs` checklist cover compound-write scenarios.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
