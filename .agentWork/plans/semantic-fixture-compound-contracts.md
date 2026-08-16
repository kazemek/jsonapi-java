# Semantic Fixture Compound Inclusion Contracts

> **Scope:** `jsonapi-java-test-fixtures` and Jackson 3 compound-serialization tests
> **Dependencies:** [Semantic Fixture Domain Read and Write Contracts](semantic-fixture-domain-contracts.md)
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
- `shared identity is included once` matches the codec `compound-shared-identity` identity graph
  (articles/1 and /2 share people/9 Dan) while domain primary attributes differ. This is one semantic
  identity example with distinct named domain/core/wire representations and distinct expectations,
  not evidence that traversal deduplication and envelope DTO identity are the same contract.
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
- Constructor/catalogue invariants must reject concurrent request/outcome mismatches, missing or
  cross-scenario representations, and null or invalid individual `IncludePath` values already
  rejected today. An empty `List<IncludePath>` remains the valid no-inclusion request that omits
  `included`; zero limits remain valid and negative limits retain their current rejection. Mutable
  graph values are fresh per execution.
- Every compound-only semantic scenario has an explicit globally unique semantic id matching the
  Foundation grammar; operation-case ids remain contract ids and are never reused as semantic ids.

## Deliverables

- Register all 30 compound-write cases exactly once as `CompoundWriteContract` entries. Attach every
  contract consuming the exact nested `article()` graph, including the converging-suffix case, to the
  foundation codec nested-graph semantic scenario; its operation id and traversal semantics remain
  separate contract values. Attach the shared-author contract to its foundation codec scenario with
  separate named domain and core/wire representations. Keep all other cases distinct unless exact
  repository values prove shared semantic identity.
- Preserve every specialized typed proof: nested intermediates, shared/primary identity exclusion,
  prefix overlap, converging suffixes, conflict detection, empty/absent included, one-shot iterable,
  owner-type and runtime-subtype policy, off-path non-access, zero/negative and finite limits,
  self/cyclic/repeated-segment traversal, validation precedence, first-discovery order, and
  per-call/concurrent isolation.
- Convert `CompoundWriteScenarios`, `JsonApiFixtures.compoundWrite()`, and the existing
  supplier-bearing scenario value surface into an immutable projection from
  `JsonApiFixtures.compoundWriteContracts()`, the direct typed `FixtureCatalog` accessor derived by
  the foundation registry. Do not add class-token dispatch or a wrapper catalogue. Keep existing ids,
  order, lookup diagnostics, request/expectation values, and static convenience methods; remove the
  independent `SCENARIOS` data list, supplier declaration, and graph construction from the
  compatibility layer.
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
  30 entries. Record each observable id, order, request variant/context, fresh input graph values,
  expectation, note, and unknown-id diagnostic without becoming registration or runtime ownership.
  Retain this characterization proof alongside the canonical-to-legacy bijection so the baseline
  proves migration preservation while the bijection proves derived ownership.
- Rewrite `CompoundWriteScenariosCatalogSpec` as a complete canonical-to-legacy bijection. Prove the
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
       named differing representations; every exact nested `article()` consumer, including
       converging suffixes, is a distinct contract on the nested-graph scenario; and other
       cycle/policy/limit/conflict cases remain distinct.
- [ ] Graph traversal, identity, first-order, one-shot iterable, access, diagnostic precedence, and
      concurrency/isolation proofs retain their existing typed expressiveness and behavior.
- [ ] `CompoundWriteScenarios` and `JsonApiFixtures.compoundWrite()` contain no independent
       canonical declarations, graph factories, or suppliers and pass an order/value/fresh-supplier
       bijection against `CompoundWriteContract`, including the valid empty include-path list; a
       fixed test-only legacy-inventory baseline independently proves all 30 pre-migration entries'
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
