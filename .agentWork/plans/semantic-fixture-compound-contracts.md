# Semantic Fixture Compound Inclusion Contracts

> **Scope:** `jsonapi-java-test-fixtures` and Jackson 3 compound-serialization tests
> **Dependencies:** [Semantic Fixture Catalog Foundation and Codec Contracts](semantic-fixture-catalog-foundation.md), [Semantic Fixture Domain Read and Write Contracts](semantic-fixture-domain-contracts.md)
> **Status:** Not started
> **Work item:** KAZ-18

## Goal

Attach every compound-inclusion traversal, policy, identity, limit, diagnostic, iterable, and
isolation proof to semantic scenarios as typed contracts, while retaining the existing
compound-write API as a lossless derived projection.

## Research and constraints

- The foundation plan owns semantic identity, named wire/core representations, typed projection
  ordering, and reusable codec anchors for the compound author, nested comments/author, and
  shared-author graphs. The domain plan adds the permitted named domain representation variant;
  reuse it and extend those scenarios where values genuinely overlap rather than defining a second
  graph catalogue.
- [`CompoundWriteScenarios`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/compoundwrite/CompoundWriteScenarios.java)
  and its `CompoundWriteScenario`, sealed `CompoundWriteRequest`, sealed
  `CompoundWriteExpectation`, `CompoundWriteSide`, and `IncludedResourceRef` types own 30 cases.
  `CompoundWriteScenariosCatalogSpec` pins request/expectation pairings, suppliers, ids, lookup,
  and constructor guards.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) requires explicit include paths and
  policies, linkage/inclusion separation, bounded traversal, deterministic first discovery,
  identity deduplication, conflict detection, and application-owned loading. Those behavioral
  proofs cannot be reduced to codec model equivalence or scenario flags.
- The domain graph used by `includes nested intermediates for comments.author`,
  `prefix-overlapping paths traverse suffixes`, `converging different-suffix paths still traverse`,
  and `nested policy matches owner resource type` uses articles/1, comments/5 and /12, people/2
  Ezra, and people/9 Dan. It matches the codec `compound-nested-intermediate` identity/attribute
  graph except that the write-domain primary article also carries title/body attributes. Store both
  named representations on one semantic scenario and keep each operation case id as its own
  contract.
- `shared identity is included once` matches the codec `compound-shared-identity` identity example
  (articles/1 and /2 share people/9 Dan) while domain primary attributes differ and the domain graph
  additionally carries a present-empty `comments` linkage that the codec graph lacks. The shared
  semantic identity is the shared people/9 example, not byte-identical graph values; the contract
  owns its distinct named domain/core/wire representations, and the present-empty linkage is
  explicitly disclosed rather than claimed equal.
- Cycles, repeated path segments, depth/count boundaries, one-shot iterables, runtime subtype
  policy, access counting, conflict precedence, and concurrent isolation are operation-specific
  semantic behaviors with no required codec/domain-read representation. They remain separate
  semantic scenarios with the typed representations they actually use. The converging-suffix
  contract is the exception: it consumes the exact shared `article()` graph and therefore belongs
  to the foundation nested-graph semantic scenario, with its traversal behavior retained on its own
  contract value.
- `CompoundSerializationSpec` currently owns adapter mechanics, absolute Jackson 3 getter-read
  counts, round-trip harnesses, mapper construction, and concurrency execution. Shared contracts own
  only adapter-neutral requests, expected inclusion/order/diagnostics, and relative traversal
  guarantees.

## Target contract model

- Add `CompoundWriteContract` as a registered `FixtureContract` root with canonical invocation
  variants for context-free, document, collection, and concurrent requests. These canonical variants
  carry named `DomainRepresentation<?>` references (including collection and both concurrent sides),
  plus invocation shape, include paths/policy/limits, off-path access, expected traversal delta, and
  ordered included identities. They retain the existing success, failure, and concurrent-isolation
  outcome distinctions.
- `CompoundWriteRequest` and `CompoundWriteSide` remain supplier-bearing compatibility values, not
  canonical contract inputs. The canonical-to-legacy projector is the only conversion: it derives
  every legacy supplier from its referenced `DomainRepresentation<?>`, preserves fresh values per
  execution, and cannot accept registrations or graph construction of its own. Every non-null
  executable input among the current 30 cases, including one-shot iterable and concurrent-side
  inputs, has one scenario-owned representation; there is no open-ended behavior-only supplier
  exception.
- `CompoundWriteExpectation` success/failure/concurrent-isolation variants remain the canonical
  outcome vocabulary. The canonical failure outcome carries the typed `MappingDiagnostic` plus the
  nullable `propertyPath` and `resourceClass` payloads matching the legacy `Failure` record
  (`Failure(diagnostic, propertyPath, resourceClass)`), so the projection reconstructs `Failure`
  exactly and never collapses absent vs. null property path. The canonical success outcome preserves
  the absent-`included` (`null`) vs. present-empty (`[]`) distinction as wire-visible states.
- Constructor/catalogue invariants must reject concurrent request/outcome mismatches, missing or
  cross-scenario representations, and null or invalid individual `IncludePath` values already
  rejected today. An empty `List<IncludePath>` remains the valid no-inclusion request that omits
  `included`; zero limits remain valid and negative limits retain their current rejection. Mutable
  graph values are fresh per execution.
- Every compound-only semantic scenario has an explicit globally unique semantic id matching the
  Foundation grammar; operation-case ids remain contract ids and are never reused as semantic ids.

## Deliverables

- `CompoundWriteContract` is a `FixtureContract` root living in the Foundation `semantic` package;
  the resulting `semantic` → `compoundwrite` dependency is already legal under the current
  test-fixtures ArchUnit allowlist (`TestFixturesDependencyRulesSpec`) and stays documented, with no
  allowlist change. Compound family declarations expose their data through public static accessors
  consumed by the root-package coordinator (mirroring the Foundation `codec.cases` pattern).
- Register all 30 compound-write cases exactly once as `CompoundWriteContract` entries. The
  semantic-scenario attachment map is fixed:
  - The 17 exact `article()`-graph consumers (`context-free overloads omit included`, `empty
    include path list omits included`, `includes nested intermediates for comments.author`,
    `prefix-overlapping paths traverse suffixes`, `converging different-suffix paths still traverse`,
    `nested policy matches owner resource type`, `nested policy denies wrong owner type`, `maxDepth
    zero rejects non-empty path`, `path longer than maxDepth fails`, `maxIncluded zero fails on first
    included resource`, `maxIncluded exceeded fails`, `mapper-time unknown relationship fails`,
    `denied relationship fails before traversal`, and the four `multi-failure` cases) attach to the
    foundation nested-graph semantic scenario `article.compound.nested-comments-author` as distinct
    contract values with same-owner references to that scenario's write-domain representation
    instance; their operation ids, policies, limits, and traversal semantics remain separate contract
    values.
  - `shared identity is included once` attaches to the foundation `article.compound.shared-author`
    scenario with a separate named domain representation, disclosing the present-empty `comments`
    linkage difference.
  - `one-shot iterable is materialized once` owns scenario `article.compound.one-shot-iterable` with
    a scenario-owned representation of its iterable graph (article/2 authored by people/2 Ezra,
    expectation `[people/9, people/2]`), which is not the shared-author graph.
  - The remaining cases own distinct scenarios with scenario-local representations: `empty
    resolution emits included empty array` → `article.compound.empty-included`; `self-reference
    primary is not re-emitted in included` → `article.compound.self-reference`; `conflicting
    representations fail` → `article.compound.conflicting-representations`; `off-path relationships
    are not read for inclusion traversal` → `article.compound.off-path-access`; `heterogeneous
    collection fails on later type` → `article.compound.heterogeneous-collection`; `runtime nested
    owner type re-checks include policy` → `article.compound.runtime-subtype-policy`; `empty primary
    collection still enforces maxDepth` → `article.compound.empty-primary-collection`; `cyclic graph
    with repeated segment path terminates` → `node.compound.cyclic`; `multi-primary multi-path
    first-discovery order` → `article.compound.multi-primary`; `deep nested path includes the chain`
    → `node.compound.deep-chain`; `concurrent compound mappings isolate included sets` →
    `article.compound.concurrent-isolation`. These ids satisfy the Foundation semantic-id grammar and
    are globally unique against the pinned anchors.
- The canonical collection invocation variant that carries the one-shot iterable references a
  scenario-owned `DomainRepresentation<Iterable<...>>` whose fresh values are one-shot iterables;
  the canonical-to-legacy projector derives the supplier as
  `() -> representation.freshValue()` and never wraps a reusable collection into an iterable.
- Preserve every specialized typed proof: nested intermediates, shared/primary identity exclusion,
  prefix overlap, converging suffixes, conflict detection, empty/absent included, one-shot iterable,
  owner-type and runtime-subtype policy, off-path non-access, zero/negative and finite limits,
  self/cyclic/repeated-segment traversal, validation precedence, first-discovery order, and
  per-call/concurrent isolation.
- Convert `CompoundWriteScenarios`, `JsonApiFixtures.compoundWrite()`, and the existing
  supplier-bearing scenario value surface into an immutable projection from
  `JsonApiFixtures.compoundWriteContracts()`, the direct typed `FixtureCatalog` accessor derived by
  the foundation registry with area label `compound-write-contract` whose unknown-id diagnostic is
  asserted by tests. The projected `CompoundWriteScenarios.catalog()` keeps the legacy
  `compound-write` area label so its preserved unknown-id diagnostic stays byte-stable. Do not add
  class-token dispatch or a wrapper catalogue. Keep existing ids,
  order, lookup diagnostics, request/expectation values, and static convenience methods; remove the
  independent `SCENARIOS` data list, supplier declaration, and graph construction from the
  compatibility layer. `CompoundWriteScenario`, `CompoundWriteRequest` (all sealed variants),
  `CompoundWriteSide`, `CompoundWriteExpectation` (all sealed variants), and `IncludedResourceRef`
  remain source-compatible public records/types with unchanged canonical constructors and static
  factories and standalone construction behavior, mirroring the Foundation codec-DTO and Domain
  retention pins; only canonical storage moves.
- Migrate Jackson 3 `CompoundSerializationSpec` shared parameterization to the canonical typed
  projection and full-projection coverage. Preserve its relaxed coverage behavior for focused
  `--tests` selection if required by the existing spec, but a normal full spec run must prove exact
  coverage.
- Use `module-docs` to move contributor ownership from a compound scenario silo to semantic
  scenarios plus `CompoundWriteContract`, while retaining the compatibility API for live future
  Jackson 2 plans.

## Non-goals

- Sparse-fieldset migration, flat read/write migration, envelope binding, PATCH, Jackson 2
  implementation, or changing compound serialization production behavior.
- Replacing traversal proofs with codec round trips, equating `IncludedResourceRef` with bound DTO
  identity, or flattening policies/limits/diagnostics into generic metadata.
- Renaming/removing case ids, changing graph values to make unrelated fixtures look equal, or moving
  Jackson-specific exact access counts into shared contracts.
- Reshaping or restricting the public surface of `CompoundWriteScenario`, `CompoundWriteRequest`,
  `CompoundWriteSide`, `CompoundWriteExpectation`, or `IncludedResourceRef`: their public record
  constructors, sealed variants, and static catalogue/factory surfaces remain source-compatible and
  standalone-constructible; only canonical storage moves, mirroring the Foundation codec-DTO and
  Domain retention pins.

## Source-of-truth transition

- Add canonical compound contracts and replace `CompoundWriteScenarios` storage in one atomic unit.
  Afterward, new compound cases extend semantic scenarios and the typed contract projection only;
  the repository-owned compatibility catalogue cannot accept independent registrations or storage.
  Standalone values constructed through any retained public DTO API remain noncanonical and do not
  register catalogue data.
- This plan follows the flat domain contract migration because it uses that plan's permitted domain
  representation variant. It owns compound registrations and graph representations only. The
  sparse-fieldset plan depends on this unit because it reuses the canonical nested graph and compound
  context semantics.

## Test strategy

- Extend semantic-catalogue tests for all compound request/expectation combinations, fresh graph
  factories, contract-id/order uniqueness, representation membership, concurrent-side pairing, and
  the valid empty include-path list versus invalid individual-path boundary.
- Before replacing the old `SCENARIOS` list, add a test-only fixed legacy-inventory baseline for all
  30 entries at
  `jsonapi-java-test-fixtures/src/test/resources/semantic-catalog-baselines/compound-write.tsv`.
  The TSV header is
  `namespace\tid\tposition\trequestVariant\tcontext\texpectation\tfreshInputGraph\tnote\tlookupDiagnostic`
  with one row per case in catalogue order; `N/A` marks a column that does not apply to that row.
  The `namespace` value is exactly `compound-write` (matching the legacy area label), and the
  baseline rejects a wrong header, unknown namespace, blank or duplicate key, and missing or extra
  rows. `note` carries the observable `notes()` value per row, which equals the id for every
  compound entry. `context` serializes the compound context (include paths, include policy, limits) deterministically;
  `freshInputGraph` records a canonical structural description of the fresh supplier graph (for
  cyclic and one-shot graphs the description is the graph shape and expected traversal, not a raw
  Java serialization), and the value-bearing columns serialize the observable values deterministically
  under the current equality rules (fresh input graph values per invocation, mutable/array values
  snapshotted). Record
  each observable id, order, request variant/context, fresh input graph values, expectation, note,
  and unknown-id diagnostic without becoming registration or runtime ownership.
  Retain this characterization proof alongside the canonical-to-legacy bijection so the baseline
  proves migration preservation while the bijection proves derived ownership.
- Rewrite `CompoundWriteScenariosCatalogSpec` as a complete canonical-to-legacy bijection, and
  include `JsonApiFixturesSpec` in the rewrite so the
  `JsonApiFixtures.compoundWrite().is(CompoundWriteScenarios.catalog())` instance identity and the
  byte-stable `compound-write` unknown-id diagnostic hold against the derived projection. Prove the
  projection is the only source of legacy suppliers, retains all constructor rejection and
  fresh-supplier behavior, and preserves the no-inclusion empty-path case.
- Add cross-representation assertions for nested and shared-author graph identities and values,
  including the intentional primary-attribute difference from codec core/wire representations.
- Parameterize every canonical contract through Jackson 3 without id dispatch; assert exact included
   order/identity, diagnostics/path/class, off-path and one-shot guarantees, cycles/limits/policy, and
   concurrent isolation. Compare executed ids with the full canonical projection. Keep the existing
  five-case round-trip assertion as an adapter-local supplemental id selection: it neither dispatches
  canonical execution nor contributes to canonical coverage, and no round-trip marker moves into a
  shared contract.
- Keep adapter-local round-trip and absolute getter-count assertions local and explicitly outside
  shared expected semantics.

## Acceptance criteria

- [ ] All 30 compound-write ids, order, requests, expectations, graph values, notes, and lookup
      diagnostics are present exactly once in the canonical typed projection and derived view.
- [ ] The nested comments/author and shared-author examples reuse foundation semantic scenarios with
       named differing representations; the 17 exact `article()` consumers attach to
       `article.compound.nested-comments-author` as distinct contracts; `shared identity is included
       once` attaches to `article.compound.shared-author` with the disclosed present-empty `comments`
       linkage; `one-shot iterable is materialized once` owns
       `article.compound.one-shot-iterable`; and the remaining distinct-scenario cases follow the
       fixed attachment map above.
- [ ] The one-shot iterable contract references a scenario-owned
       `DomainRepresentation<Iterable<...>>` whose fresh values are one-shot iterables, and the
       projector derives the supplier from `representation.freshValue()`.
- [ ] Graph traversal, identity, first-order, one-shot iterable, access, diagnostic precedence, and
      concurrency/isolation proofs retain their existing typed expressiveness and behavior.
- [ ] `CompoundWriteScenarios` and `JsonApiFixtures.compoundWrite()` contain no independent
       canonical declarations, graph factories, or suppliers and pass an order/value/fresh-supplier
       bijection against `CompoundWriteContract`, including the valid empty include-path list; the
       fixed test-only `semantic-catalog-baselines/compound-write.tsv` baseline independently proves
       all 30 pre-migration entries'
       ids, order, request/context, fresh graph values, expectations, notes, and lookup diagnostic
       are preserved.
- [ ] Jackson 3 consumes the canonical projection, dispatches on typed variants rather than ids, and
       proves full projection coverage while keeping adapter-specific mechanics local; the retained
       adapter-local five-case round-trip assertion may select its supplemental cases by id without
       dispatching the canonical projection or adding shared selection metadata.
- [ ] The `module-docs` checklist passes for the changed fixture ownership and compatibility surface.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` and
       `./gradlew :jsonapi-java-jackson3:test --tests 'io.github.kazemek.jsonapi.jackson3.CompoundSerializationSpec'`
       pass.
- [ ] The `spotless-format` skill runs `./gradlew spotlessApply` then
      `./gradlew spotlessCheck`, followed by `./gradlew clean build`.
- [ ] The `sonar-quality-gate` skill's Quality Gate wait and authenticated Issues API check both
      exit 0 with zero unresolved new-code issues.
