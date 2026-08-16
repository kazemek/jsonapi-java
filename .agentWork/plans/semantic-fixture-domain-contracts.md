# Semantic Fixture Domain Read and Write Contracts

> **Scope:** `jsonapi-java-test-fixtures` and Jackson 3 flat domain mapping tests
> **Dependencies:** [Semantic Fixture Catalog Foundation and Codec Contracts](semantic-fixture-catalog-foundation.md)
> **Status:** Not started
> **Work item:** KAZ-18

## Goal

Make every flat domain-to-core and core-to-domain case a typed contract on the semantic fixture
catalogue, with separate named write-object and read-DTO representations where required and lossless
derived views for the existing domain-write and domain-read APIs.

## Research and constraints

- The foundation plan owns `SemanticScenario`, typed representations/contracts,
  direct typed `FixtureCatalog` facade accessors, semantic identity, explicit registration, and codec
  anchors. Extend that model; do not add another catalogue, builder, identity scheme, class-token
  dispatch API, or generic legacy wrapper.
- [`DomainWriteScenarios`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/domainwrite/DomainWriteScenarios.java),
  its typed `DomainWriteOperation`, `DomainWriteInput`, `DomainWriteOutcome`, and
  `DomainWriteComparisonPolicy`, and `DomainWriteScenariosCatalogSpec` own 14 current flat write
  cases. Preserve operation dispatch, fresh suppliers, envelope passthrough, exactly-one
  resource/document success, failure type, and unordered identifier-pair comparison for Set-based
  linkage.
- [`DomainReadScenarios`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/domainread/DomainReadScenarios.java),
  its sealed `DomainReadInput`, `ConverterBehavior`, and `DomainReadExpectation`, plus
  `DomainReadScenariosCatalogSpec` and `DomainReadFixtureModelsSpec`, own 40 flat read cases. Preserve
  single/collection/included-isolation inputs, converter behavior, target classes, resource-relative
  diagnostic paths, creator/default/null behavior, and the proof that flat binding never reads
  `included`.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) deliberately separates flat read DTO
  binding from write-side graph mapping. A semantic scenario may own several domain
  representations; it must not force `FlatArticle` and `Article`, or other read/write types, into one
  Java object.
- The strongest proven overlaps are:
   - domain-write `maps @JsonProperty naming` and domain-read
     `binds @JsonProperty named attribute` use the same
     `BlogWithJsonProperty("b1", "My Blog")` and core `blogs/b1` representation;
   - codec `relationship-null-linkage` and domain-read `NullLinkage on to-one binds null` share the
     same explicit-null author core representation;
   - codec `relationship-empty-to-many` and domain-read
     `empty collection linkage on to-many binds empty collection` share the same present-empty
     comments core representation; its List and array targets remain separate contracts and named
     DTO representations. The Set target instead uses a distinct `tags` core representation.
   - the two domain-write linkage cases produce one value-identical full article resource with title,
     body, explicit-null author, and present-empty comments. Retain both case ids as separate
     contracts on a distinct semantic scenario; they do not reuse either single-relationship codec
     core representation.
- Similar outcomes are not proof of one representation. In particular, links/meta-only relationship
  data is absent while `NullLinkage` is explicit null; envelope DTO reads have different document
  inputs; included-isolation proves non-consumption rather than graph inclusion; and Set write/read
  examples use different models, ids, and comparison semantics.
- Existing case ids and catalogue order are coverage/accounting contracts. Even the two write cases
  whose current suppliers/outcomes are value-identical retain both ids as separate contracts on the
  appropriate semantic scenario; do not delete or alias an id as cleanup.
- Test-fixtures production dependencies remain within ADR-010's current allowlist. No Jackson-major
  imports or adapter-specific property-path expectations move into shared contracts.

## Target contract model

- Add `DomainWriteContract` and `DomainReadContract` as `FixtureContract` roots registered with
  the foundation catalogue. This plan adds the permitted `DomainRepresentation<?>` variant with its
  real consumers, as the Foundation defers its concrete factory shape to this migration. Reuse the
  existing sealed operation/input/request/expectation policy types where they already encode valid
  combinations; change them only to reference named `DomainRepresentation<?>` and
  `CoreRepresentation` values instead of independently constructing duplicate semantic values.
- `CoreRepresentation` remains document-valued as required by the Foundation. For each existing
  bare `ResourceObject` input or `TO_RESOURCE` outcome, store a named `JsonApiDocument` whose data
  is that single resource; for each resource-collection input, store a document whose data is the
  matching `DocumentData.ResourceCollection`. Typed contract variants validate/extract the required
  `SingleResource` or `ResourceCollection` shape for mapper/binder calls, while whole-document
  operations retain the document and compatibility projections expose their unchanged bare resource
  or resource-list values.
- A domain-write contract identifies its existing case id, operation, typed single/collection/null
  input source, optional envelope only for the envelope operation, success/failure outcome, and
  comparison policy. Successful expected core resources/documents are scenario-owned core
  representations. Null input is a typed behavior input with no valid domain representation rather
  than a nullable representation slot.
- A domain-read contract identifies its existing case id, one or more scenario-owned core inputs,
  target domain representation/type, converter behavior, and success/failure expectation.
  `IncludedIsolation` references its two scenario-owned inline `WireRepresentation` values and one
  expected DTO representation; the compatibility projection returns the exact source strings and
  each adapter continues to parse both values before binding. Paired core representations may support
  semantic assertions, but never replace those wire inputs. Success and failure remain sealed
  alternatives.
- Domain representations are named by role (`write-model`, `read-dto`, collection-shape variants,
  or similarly explicit names), expose fresh values where mutation is possible, and retain their
  concrete Java type. Contracts dispatch on typed variants and operation enums, never semantic or
  case-id strings.
- Factories/constructors reject invalid cross-field combinations before registration: a
  `TO_RESOURCE` contract requires a single-input and a single-resource success when successful;
  document operations retain document successes and only the envelope operation carries an envelope;
  `TO_RESOURCE_COLLECTION` requires collection input and a resource-collection document result;
  read single/collection/included-isolation variants require their corresponding document or wire
  representation shapes and compatible target/expectation values. Catalogue tests prove these
  rejections but do not define validity.

## Deliverables

- Register all 14 domain-write and all 40 domain-read cases as canonical typed contracts. Reuse the
  foundation's codec semantic scenarios only for exact read-representation overlap, including null
  author linkage and empty comments linkage. Attach the two value-identical full-resource
  domain-write linkage contracts to one distinct `article.relationship.author-null-and-comments-empty`
  semantic scenario, and create explicit domain semantic scenarios for all other behaviors rather
  than joining examples by vague structural similarity.
- Add one `blog.json-property-name` semantic scenario (or the foundation ADR's exact naming form)
  with the proven shared `BlogWithJsonProperty("b1", "My Blog")` read/write domain representation,
  core resource, domain-write contract, and domain-read contract. This is the explicit exception:
  read/write models that differ remain separate named domain representations unless an exact semantic
  identity is proven. Keep the sparse-fieldset `"Hello"` case separate until its own plan because its
  value and behavioral expectation differ.
- Put the List and array empty-comments read contracts on the shared empty-linkage semantic scenario
  as separate named target representations/contracts; retain the Set `tags` contract on its distinct
  core representation. Preserve links/meta-only, null, absent, and present-empty inputs as distinct
  core representations even when their bound DTO values compare equal.
- Convert `DomainWriteScenarios`, `DomainReadScenarios`, `JsonApiFixtures.domainWrite()`, and
  `JsonApiFixtures.domainRead()` into immutable projections from
  `JsonApiFixtures.domainWriteContracts()` and `JsonApiFixtures.domainReadContracts()`. Each method
  returns the direct typed `FixtureCatalog` derived by the foundation registry; do not add class-token
  dispatch or a wrapper catalogue. Existing scenario DTOs/static methods may remain, but must
  materialize repository-owned built-in entries from canonical contracts and contain no
  independently editable built-in case list, registration source, or model storage. Standalone value
  construction through any retained public DTO API does not register canonical data.
- Migrate Jackson 3 `ResourceMapperSpec` and `ResourceBinderSpec` shared parameterization to the
  canonical contract projections. Keep mapper/binder construction, `JsonMapper` behavior, custom
  conversion harnesses, absolute Jackson-observed property paths, and adapter-only cases local.
- Use `module-docs` to update the test-fixtures package table, representation/contract examples,
  compatibility status, and adapter-consumption guidance. Link ADR-011 and the foundation ADR; do
  not duplicate their rationale.

## Non-goals

- Compound graph traversal, sparse fieldsets, typed envelopes, codec changes, PATCH, or a Jackson 2
  implementation.
- Merging read DTO and write object types, hydrating relationships from `included`, or introducing
  persistence/lookup behavior.
- Renaming/removing case ids, changing target DTO behavior, moving adapter-observed path details into
  shared expectations, or removing compatibility APIs needed by current live Jackson 2 plans.
- Removing operation, converter, comparison, or failure variants in favor of booleans or nullable
  fields.

## Source-of-truth transition

- Switch each domain family atomically: add its canonical contracts and replace its old list with a
  projection in the same change. Afterward, new domain read/write cases are added only by extending a
  semantic scenario and attaching the typed contract; compatibility catalogues are derived views,
  while standalone DTO values remain outside canonical registration.
- The codec family remains canonical in the unified catalogue. Compound, sparse-fieldset, and
  envelope families remain their documented owners until their separate plans land; do not import
  them as untyped placeholders.
- Every new domain semantic scenario has an explicit globally unique semantic id matching the
  Foundation grammar; existing operation-case ids remain contract ids and are never reused as
  semantic ids.

## Test strategy

- Extend semantic-catalogue invariants for both contract roots, representation membership, exact
  contract-id/order uniqueness, semantic-id grammar, target Java types, fresh mutable domain
  instances, operation/input / outcome consistency, and success/failure exclusivity. Verify every
  document-valued resource representation extracts only a single resource for the retained mapper
  and binder values.
- Rewrite both catalogue specs as projection-bijection tests: every current id occurs once in the
  canonical typed projection and once in the derived view, in the same order, with equal operations,
  inputs, contexts, outcomes, policies, notes, and unknown-id diagnostics.
- Before replacing the old built-in lists, add a test-only fixed legacy-inventory baseline for all 14
  write and 40 read entries. It records each observable id, order, operation/variant, notes,
  input/output/expectation values, policy, and lookup diagnostic without becoming registration or
  runtime ownership. Compare fresh supplier values per invocation and snapshot mutable/array values
  with the current observable equality rules. Retain this characterization proof alongside the
  canonical-to-derived bijection so the latter proves ownership while the baseline proves migration
  preservation.
- Add focused cross-contract assertions for `BlogWithJsonProperty`, explicit null author linkage,
  and empty comments linkage. Prove `BlogWithJsonProperty("b1", "My Blog")` is the explicit shared
  read/write domain-representation exception, and require distinct named representations for
  List/Set/array DTOs and all read/write models that differ.
- Preserve fresh suppliers, one-shot collection behavior where applicable, included-isolation,
  converter exceptions/null returns, null input rejection, envelope members, comparison policy,
  diagnostics, and absent/null/empty distinctions.
- Prove `IncludedIsolation` preserves its two exact inline wire sources through the derived legacy
  input and that Jackson 3 parses each before binding; do not reconstruct either source from core
  documents.
- Jackson 3 executors collect contract ids from the full `DomainWriteContract` and
  `DomainReadContract` projections and assert exact full-projection coverage without dispatching on
  ids.

## Acceptance criteria

- [ ] All 14 domain-write and 40 domain-read ids, order, values, variants, notes, and lookup
      diagnostics are preserved as canonical typed contracts and lossless legacy projections.
- [ ] Read DTOs and write objects whose models differ are separate named domain representations;
        `BlogWithJsonProperty("b1", "My Blog")` is the proven shared read/write representation
        exception; and null input and failure cases do not require fabricated valid representations.
- [ ] Contract factories reject every mismatched write operation/input/envelope/outcome and read
       input/representation/target/expectation combination before registration; document-valued
       core representations extract only the single-resource or resource-collection shape required by
       the typed adapter operation, while document operations retain their complete document values.
- [ ] The blog naming, explicit-null author, and empty-comments read examples share only proven
       semantic representations; the value-identical full-resource domain-write linkage cases retain
       their separate contract ids on their own semantic scenario; and List/array comments, Set tags,
       absent-vs-null, included-isolation, and adapter-local differences remain typed distinct
       contracts.
- [ ] `DomainWriteScenarios` and `DomainReadScenarios` contain no independent canonical storage, and
       their `JsonApiFixtures` accessors are derived from the direct typed
       `domainWriteContracts()` / `domainReadContracts()` catalogues with bijection/order tests.
- [ ] Jackson 3 mapper and binder suites consume the canonical projections, retain all specialized
       behavior, and prove full projection coverage with no id-based dispatch.
- [ ] Included-isolation retains both exact wire source strings as named wire representations and
       proves separate adapter-reader parsing before equivalent binding; no compatibility value is
       reserialized from a core representation.
- [ ] The `module-docs` checklist passes and documents the canonical/compatibility ownership without
       duplicating ADR-011 or the unified-catalogue ADR.
- [ ] Every added production Java package is `@NullMarked`, public null-bearing domain contract and
       representation type uses have accurate `@Nullable` metadata, and explicit wire null remains a
       typed JSON:API state rather than a bare Java-null representation, as required by ADR-009.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` and
       `./gradlew :jsonapi-java-jackson3:test --tests 'io.github.kazemek.jsonapi.jackson3.ResourceMapperSpec' --tests 'io.github.kazemek.jsonapi.jackson3.ResourceBinderSpec'`
       pass.
- [ ] The `spotless-format` skill runs `./gradlew spotlessApply` then
      `./gradlew spotlessCheck`, followed by `./gradlew clean build`.
- [ ] The `sonar-quality-gate` skill's Quality Gate wait and authenticated Issues API check both
      exit 0 with zero unresolved new-code issues.
