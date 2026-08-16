# Semantic Fixture Sparse Fieldset Contracts

> **Scope:** `jsonapi-java-test-fixtures` and Jackson 3 sparse-fieldset tests
> **Dependencies:** [Semantic Fixture Compound Inclusion Contracts](semantic-fixture-compound-contracts.md)
> **Status:** Not started
> **Work item:** KAZ-18

## Goal

Represent every mapped/unmapped sparse-fieldset operation, policy, projection, identity, access,
diagnostic, and isolation proof as a typed contract on semantic scenarios, reusing the canonical
compound graph without collapsing fieldset behavior into generic flags.

## Research and constraints

- The foundation and compound plans own the semantic model and the canonical articles/1,
  comments/5 and /12, people/2 and /9 domain graph. Sparse contracts may reference that fresh domain
  representation and add fieldset-specific expected representations; they must not rebuild a second
  canonical graph.
- [`SparseFieldsetScenarios`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/sparsefieldset/SparseFieldsetScenarios.java)
  and its `SparseFieldsetScenario`, `SparseFieldsetOperation`, sealed `SparseFieldsetRequest`, sealed
  `SparseFieldsetExpectation`, `FieldsetResourceState`, `ZeroReadGuarantee`, and
  `SparseFieldsetSide` types own 30 cases. `SparseFieldsetScenariosCatalogSpec` pins operation,
  request, expectation, cross-field, freshness, target-state, constructor-rejection, and lookup
  invariants, but does not independently pin the current 30-case id/order inventory.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) keeps field policy distinct from
  include policy while sharing `CompoundSerializationContext`. Preserve final JSON:API member-name
  resolution, application allow-list policy, pre-access filtering, identity retention, and the
  scoped full-linkage exception.
- `nested include comments.author with fields comments body` uses the exact compound article graph
  and included identity order, but its `FieldsetResourceState` values prove filtered attributes.
  Attach it to the existing graph semantic scenario as a separate sparse contract and expected
  projection. Remove the duplicate sparse `article()` factory when its request representation is
  projected from the compound-owned graph factory.
- `renamed JsonProperty fieldset names use final JSON:API names` uses
  `BlogWithJsonProperty("b1", "Hello")`, not the domain read/write `"My Blog"` value. Keep it a
  distinct semantic scenario or named domain representation; do not silently alter values to force
  a merge.
- Mapped versus unmapped operations are a required discriminator: the three-argument unmapped
  calls reject non-empty fieldsets while mapped calls succeed. Preserve
  `SparseFieldsetOperation`; do not infer the entry point from request shape or id.
- Identity preservation across four shapes, renamed members, field policy, zero-read guarantees,
  access-vs-linkage/traversal behavior, collection mapping, exact diagnostics, and concurrent
  document/exception isolation are specialized proofs, not generic scenario metadata. Duplicate
  caller-member collapse remains a Jackson 3 mutable-input harness assertion; canonical contexts
  retain only their already-normalized field-name values.
- Jackson 3 owns mutable-map harnesses, exact getter counts, mapper construction, message details
  that were deliberately adapter-local, and thread execution. Shared contracts retain portable
  state/diagnostic/zero-read expectations only.

## Target contract model

- Add `SparseFieldsetContract` as a registered `FixtureContract` root. Define one sealed canonical
  `SparseFieldsetInvocation` hierarchy with single, collection, concurrent-side, and
  identity-preservation variants. Every executable invocation references one or more
  scenario-owned `DomainRepresentation<?>` factories; no canonical invocation carries a supplier.
  The variants retain the operation-specific context, policy, include/field names, and four
  identity-preservation shapes. Preserve mapped success, unmapped success, failure,
  concurrent-isolation, and identity-preservation expectation variants.
- Contracts retain typed expected fieldset/core state, `ZeroReadGuarantee`, sparse-full-linkage
  exception state, and expected diagnostics. `SparseFieldsetRequest` and `SparseFieldsetSide` are
  supplier-bearing compatibility values only: the canonical-to-legacy projection derives their fresh
  suppliers from `SparseFieldsetInvocation` representation factories, and neither type can register
  or own canonical fixture state.
- Extend the foundation-owned package-private semantic registry assembly/freeze path in this atomic
  migration; do not add a sparse registry or mutable post-freeze extension API. Its existing
  pre-freeze declaration phase registers sparse scenarios, representations, and contracts after the
  prerequisite family declarations and before owner-index/catalogue freezing. Sparse registration
  attaches the nested fieldset contract to the same in-construction compound semantic scenario and
  representation instances. The frozen registry then derives `sparseFieldsetContracts()` and the
  legacy projection from those registered instances.
- Retain and test the existing cross-field guards: concurrent pairings, identity operation/shape,
  collection operation/input, and mapped/unmapped operation/expectation consistency. Contract
  references must belong to the owning semantic scenario and mutable inputs must be fresh.

## Deliverables

- Register all 30 sparse-fieldset cases exactly once as canonical `SparseFieldsetContract` entries.
  Reuse the compound nested graph for the proven nested include/fieldset case; create separate
  semantic scenarios for access counting, renamed-value, policy, identity-shape, collection,
   diagnostics, and concurrency cases unless exact values prove a shared representation.
- Extend the foundation registry's internal pre-freeze family assembly to register the sparse
  declarations and derive `JsonApiFixtures.sparseFieldsetContracts()` from the same frozen graph;
  retain its exactly-one-owner index and immutable post-freeze boundary.
- Preserve typed operation and behavior distinctions for attribute-only, relationship-only, empty,
  omitted-linkage/full-linkage exception, primary/included filtering, final mapped names,
  unknown/disallowed fields, access counting, include/fieldset interaction, identity preservation,
  collection shapes, unmapped rejection, and concurrent isolation.
- Convert `SparseFieldsetScenarios`, `JsonApiFixtures.sparseFieldset()`, and existing scenario values
  into an immutable projection from
  `JsonApiFixtures.sparseFieldsetContracts()`, the direct typed `FixtureCatalog` accessor derived by
  the foundation registry. Do not add class-token dispatch or a wrapper catalogue. Keep all current
   ids, order, request/expectation values, constructor guards, and lookup diagnostics; remove
   independent built-in scenario/model registration and storage from the compatibility catalogue.
   Preserve the signatures and behavior of `SparseFieldsetScenarios.catalog()`, `all()`,
   `byId(String)`, and `where(Predicate)`, `JsonApiFixtures.sparseFieldset()`, and all public
   `SparseFieldsetRequest` and `SparseFieldsetExpectation` static factories; each delegates to or
   constructs values from the derived projection without registering canonical state.
   Retain the public standalone constructors and validation behavior of `SparseFieldsetScenario`, all
   `SparseFieldsetRequest` variants, `SparseFieldsetSide`, `SparseFieldsetExpectation` variants,
   `FieldsetResourceState`, and `ZeroReadGuarantee`. Directly constructed compatibility values remain
   usable outside canonical registration and never own or register fixture state.
- Before replacing the old `SCENARIOS` list, add a test-only fixed legacy-inventory baseline for all
  30 entries. Record each observable id, order, operation/request variant, fresh input graph values,
  context, expectation, note, and lookup diagnostic without becoming registration or runtime
  ownership. Retain it alongside the canonical-to-legacy projection bijection so it proves migration
  preservation independently of derived ownership.
- Migrate Jackson 3 `SparseFieldsetSpec` shared parameterization to the canonical typed projection,
  typed operation/request/expectation dispatch, and exact full-projection coverage. Keep
  Jackson-specific map-mutation, exact read-count, message-composition, `applyTo`, and writer
  validation cases local.
- Use `module-docs` to document semantic ownership, compound-graph reuse, typed sparse contracts,
  and the retained compatibility projection for live future Jackson 2 work.
- Apply ADR-009 to the new canonical sparse contract/invocation and retained compatibility API:
  every added production package has `@NullMarked` package metadata, every public null-bearing
  member, parameter, and type use has accurate `@Nullable` metadata, and applicable explicit
  JSON:API null/absence states remain represented by the existing sealed wire variants rather than
  accidental bare Java null.

## Non-goals

- Query-parameter parsing, authorization, changing field/include production semantics, envelope
  binding, PATCH, Jackson 2 implementation, or flat domain migration.
- Flattening mapped/unmapped operations, identity shapes, zero-read/access proofs, field policy, or
  diagnostics into booleans on `SemanticScenario`.
- Renaming/removing ids, changing fixture values to create artificial overlap, or promoting
  Jackson-major-only exact counts/messages into shared expected behavior.

## Source-of-truth transition

- Add canonical sparse contracts and replace `SparseFieldsetScenarios` storage atomically. New
  sparse cases thereafter extend semantic scenarios and the typed projection only; compatibility
  catalogues cannot register or store independent built-in cases.
- Compound contracts remain canonical and supply the shared graph/context semantics. The final
  envelope/convergence unit depends on this plan so it can prove every pre-PATCH family has left no
  independent storage.

## Test strategy

- Extend semantic-catalogue tests for all operation/request/expectation guards, representation
  membership, graph factory freshness, contract-id/order uniqueness, identity shape count, and
  concurrent-side pairing. Prove sparse registration runs in the foundation registry's pre-freeze
  assembly, the nested fieldset contract shares the compound scenario/representation instances, and
  the frozen owner index/catalogues contain no parallel sparse registry or post-freeze mutation path.
- Establish the fixed 30-entry test-only legacy-inventory baseline before replacing `SCENARIOS`, then
  rewrite `SparseFieldsetScenariosCatalogSpec` as a complete canonical-to-legacy bijection while
  retaining constructor rejection, target-state consistency, normalized context values, and
  fresh-input assertions. Add source-compatibility and constructor-guard coverage for the retained
  standalone sparse DTO APIs and public static catalogue/factory surface, including proof that direct
  construction or static factory use does not register or mutate canonical catalogue state.
- Add a cross-contract test proving the nested fieldset contract references the canonical compound
  graph and preserves comments/5, comments/12, people/2, people/9 order while using its own filtered
  expected states. Prove the `BlogWithJsonProperty("Hello")` representation remains distinct from
  the value-equal domain read/write scenario.
- Execute every canonical sparse contract through Jackson 3 without id dispatch; assert fieldsets,
  identity, included state, full-linkage exception, zero reads, policies, diagnostics, collections,
  and concurrent isolation, then compare executed ids with the full projection.
- Keep mutable harness, duplicate caller-member collapse, exact count, message, and writer
  integration assertions adapter-local and preserve absent versus present-empty
  attributes/relationships/included values.
- Verify the ADR-009 package annotations and nullable type uses for every added canonical and
  compatibility production API, including absent attributes/relationships/`included` and nullable
  diagnostic members; verify that applicable explicit JSON:API null states use existing sealed wire
  variants.

## Acceptance criteria

- [ ] All 30 sparse-fieldset ids, order, operations, requests, expectations, policies, values,
      notes, and lookup diagnostics exist exactly once in the canonical projection and derived view.
- [ ] The nested include/fieldset case reuses the canonical compound graph with a distinct filtered
      expectation; renamed-value and other merely similar examples are not artificially merged.
- [ ] Mapped/unmapped, identity, collection, field-policy, zero-read, full-linkage, diagnostic, and
       concurrent-isolation distinctions remain typed and constructor-validated; canonical
       invocations are representation-backed while supplier-bearing request/side values are derived
       compatibility projections only.
- [ ] Sparse declarations register through the foundation-owned pre-freeze registry assembly; the
       nested fieldset contract shares Compound's frozen semantic scenario and representations, and
       `sparseFieldsetContracts()` plus its legacy projection derive from that one immutable registry
       without a parallel sparse registry or post-freeze mutation API.
- [ ] `SparseFieldsetScenarios` and `JsonApiFixtures.sparseFieldset()` contain no independent
       canonical declarations and pass exact order/value projection-bijection tests; a fixed test-only
       legacy-inventory baseline independently proves all 30 pre-migration ids, order, operation,
       request/context, fresh input graph values, expectations, notes, and lookup diagnostics.
- [ ] Retained standalone sparse DTO constructors and validation guards remain source-compatible;
       directly constructed values remain outside canonical registration and cannot alter canonical
       fixture state.
- [ ] `SparseFieldsetScenarios.catalog()`, `all()`, `byId(String)`, and `where(Predicate)`,
       `JsonApiFixtures.sparseFieldset()`, and all public `SparseFieldsetRequest` /
       `SparseFieldsetExpectation` static factories retain their signatures and behavior; tests
       compile and exercise them against the derived catalogue without registering canonical state.
- [ ] Jackson 3 consumes the canonical typed projection, dispatches without ids, proves full
      projection coverage, and leaves Jackson-specific harness expectations local.
- [ ] The `module-docs` checklist passes for semantic ownership, graph reuse, and compatibility.
- [ ] ADR-009 nullness obligations pass: every added production package is `@NullMarked`, every
       public null-bearing canonical or compatibility member/parameter/type use has accurate
       `@Nullable` metadata, and applicable explicit JSON:API null/absence states remain typed sealed
       wire variants rather than bare Java null.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` and
       `./gradlew :jsonapi-java-jackson3:test --tests 'io.github.kazemek.jsonapi.jackson3.SparseFieldsetSpec'`
       pass.
- [ ] The `spotless-format` skill runs `./gradlew spotlessApply` then
      `./gradlew spotlessCheck`, followed by `./gradlew clean build`.
- [ ] The `sonar-quality-gate` skill's Quality Gate wait and authenticated Issues API check both
      exit 0 with zero unresolved new-code issues.
