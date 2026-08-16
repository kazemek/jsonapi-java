# Semantic Fixture Catalog Foundation and Codec Contracts

> **Scope:** `jsonapi-java-test-fixtures`, `jsonapi-java-jackson3` tests, `fixtures/jsonapi-1.1/ambiguous-manifest.json`, `fixtures/jsonapi-1.1/negative-manifest.json`, and fixture architecture documentation
> **Dependencies:** None
> **Status:** Not started
> **Work item:** KAZ-18

## Goal

Establish the Jackson-major-neutral semantic scenario, named representation, typed contract, and
canonical retrieval model, without precluding broader future adapter reuse, then prove it by making
all positive, ambiguous-primary-data, negative, writer, and schema codec expectations originate from
that model without changing existing case ids or wire paths.

## Research and constraints

- [`jsonapi-java-test-fixtures/README.md`](../../jsonapi-java-test-fixtures/README.md) and its root
  [`package-info.java`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/package-info.java)
  own current fixture retrieval, stable case-id, adapter-dispatch, and contributor rules. Use the
  `module-docs` skill to replace operation-catalog ownership with the semantic model; do not create a
  second durable explanation in this plan.
- [`Scenario`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/Scenario.java),
  [`FixtureCatalog`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/FixtureCatalog.java),
  [`ImmutableFixtureCatalog`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/ImmutableFixtureCatalog.java),
  and [`JsonApiFixtures`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/JsonApiFixtures.java)
  supply the current immutable `all()` / `byId()` / `where()` pattern. Reuse that public catalogue
  abstraction for both semantic scenarios and typed contracts. Existing operation-case ids and
  new semantic ids occupy different generic catalogue entry types; neither replaces the other.
- [`manifest.json`](../../fixtures/jsonapi-1.1/manifest.json),
  [`ambiguous-manifest.json`](../../fixtures/jsonapi-1.1/ambiguous-manifest.json),
  [`negative-manifest.json`](../../fixtures/jsonapi-1.1/negative-manifest.json), and the
  [wire-corpus README](../../fixtures/jsonapi-1.1/README.md) own corpus membership, order, paths,
  negative diagnostic metadata, and storage rules. Preserve their paths, ids, order, and the JSON-P
  negative loader; storage uniformity is not a goal. They remain the persisted owners of the fields
  they contain. Runtime contracts materialize those fields from loaded rows rather than redeclaring
  them in Java semantic declarations.
- Extend `negative-manifest.json` minimally with optional string field `messagePolicy`. Set
  `messagePolicy: "EXCLUDES_INPUT"` only on `malformed-json-without-payload`; absence means no shared
  message-content policy. An explicit JSON `null`, non-string value, or unknown string is invalid;
  loaders must distinguish field absence from those malformed forms and reject them rather than infer
  policy from notes or case ids. The manifest remains the persisted owner, and loaders must preserve
  every negative id, path, row order, category, pointer, rule code, source-location flag, and note.
- `CodecScenariosCatalogSpec`, `AmbiguousPrimaryDataScenariosCatalogSpec`,
  `NegativeCodecScenariosCatalogSpec`, `CodecScenarioConversionEquivalenceSpec`, and the Jackson 3
  `DocumentReaderSpec`, `DocumentWriterContractSpec`, and `JsonApiDraftSchemaSpec` pin model values,
  contexts, capabilities, exact UTF-8, hreflang arrays, schema disagreements, safe diagnostics,
  and manifest bijections. Replace duplicate ownership assertions with canonical-contract and
  compatibility-projection proofs without weakening those behaviors.
- Load `manifest.json`, `ambiguous-manifest.json`, and `negative-manifest.json` through one
  Jackson-major-neutral JSON-P row-loader strategy. Give each root its required-field and field-type
  validation, reject malformed or missing rows before declaration joins, and preserve root-specific
  persisted ownership without introducing a second parser or adapter dependency.
- The currently unmigrated envelope fixture family resolves codec ids through `CodecScenarios` and
  consumes `CodecScenario` values. This unit therefore must retain `CodecScenario`,
  `AmbiguousPrimaryDataScenario`, `NegativeCodecScenario`, all three existing `*Scenarios` static
  surfaces, their corresponding current `JsonApiFixtures` accessor signatures, lookup behavior and
  diagnostics, and capability methods as source-compatible lossless derived projections. Their
  built-in catalogue entries move to the semantic graph and typed contracts; their signatures and
  observable behavior do not change in this unit. Preserve the public canonical record constructors
  of all three DTOs and `CodecScenario.of(...)`, including their ability to construct standalone
  values. A directly constructed DTO is not registered with or owned by the canonical graph.
- Before canonical codec compatibility projections replace the current codec catalogue ownership,
  materialize a durable test-only capability baseline at
  `jsonapi-java-test-fixtures/src/test/resources/semantic-catalog-baselines/codec-capabilities.tsv`.
  Its exact TSV header is `namespace\tid\treadable\twritable\tschemaApplicable`; namespace values
  are exactly `codec`, `ambiguous-primary-data`, and `negative-codec`; and it has one row per
  namespace/id pair with literal `true` or `false` membership values. Positive rows use the existing
  `CodecScenario.readable`, `writable`, and `schemaKind != null` selections, while ambiguous and
  negative rows are read-only. The stable ids are repeated only as the join key needed for
  independent membership; manifests remain the sole owners of paths, order, notes, and diagnostic
  metadata. Reject a wrong header, unknown namespace, blank/duplicate key, malformed boolean, or
  missing/extra namespace/id row. This baseline is a migration oracle, not runtime catalogue
  storage, and Convergence consumes this exact Foundation-owned oracle rather than deriving expected
  capabilities from the catalogue under test.
- An ambiguous object is parse-equal to `single-identifier`, and an ambiguous empty array is
  parse-equal to `empty-identifier-collection`, but each ambiguous case proves two context-selected
  core outcomes. `extension-members-require-context` reuses the exact
  `documents/extension-and-at-members.json` wire path and proves failure without the extension
  validation context. These are required demonstrations of multiple named representations and
  multiple contracts on one semantic scenario, not separate canonical catalogues.
- The two ambiguous Java compatibility notes currently differ from their manifest notes. Reconcile
  that pre-existing drift once by updating `ambiguous-manifest.json` to the currently exposed texts:
  `Object primary data decoding to either a resource or an identifier model` and
  `Empty-array primary data decoding to either a resource or an identifier model`. Preserve both
  ids, paths, and row order; after this migration, manifest rows are the sole persisted note owner.
- [ADR-007](../../docs/adr/007-module-boundaries.md),
  [ADR-009](../../docs/adr/009-jspecify-nullness.md), and
  [ADR-010](../../docs/adr/010-architectural-tests.md) require `@NullMarked` Java packages and keep
  test fixtures free of `tools.jackson.*`, `com.fasterxml.jackson.databind.*`, adapter packages,
  `core.internal..`, and Groovy production dependencies. Annotation-only
  `com.fasterxml.jackson.annotation.*`, core, jackson-common, JSpecify, and JSON-P remain allowed.
- `jsonapi-java-jackson-common` is Jackson-major-neutral shared contract ownership for Jackson 2/3,
  not an established generic adapter-common module. Shared fixture contracts may depend on its
  Jackson-import-free semantic values where applicable, but ADR-013 and module docs must not claim
  that this dependency alone proves full Gson/other-adapter neutrality. The catalogue architecture
  and fixture representations must avoid concrete Jackson-major imports and must not preclude a
  future adapter from executing applicable contracts or motivate a new generic common module now.
- This is a consequential multi-adapter test-architecture decision not owned by ADR-007 or ADR-010.
  Add an accepted ADR rather than leaving the rationale only in a module README. The closed PR #83
  declaration-layout direction is not a dependency or target; source-file layout follows semantic
  readability and explicit registration, not a universal inline/per-case rule.

## Target type and ownership model

- Add an `@NullMarked` semantic fixture package with these responsibilities (exact record/class
  spelling may change only where Java compilation requires it; preserve the boundaries):
  - `SemanticScenario` implements `Scenario`, owns a nonblank Java-owned semantic `description`,
    named `ScenarioRepresentation<?>` values, and attached `FixtureContract` values, and delegates
    `id()` to `semanticId()`. The description is semantic prose, never a compatibility note or
    diagnostic source. Generic catalogue types and the explicit `semanticId()` accessor distinguish
    semantic identity from operation-case identity.
  - sealed `ScenarioRepresentation<T>` initially permits typed `WireRepresentation` and
    `CoreRepresentation`. Wire values use a sealed `WireSource` with fixture-path and inline-text
    variants (inline text may be invalid JSON, including empty/whitespace negative inputs) and an
    explicit source role. `EXPECTED_OUTPUT` wire sources must be valid expected JSON and may be used
    by writer/exact-byte structural expectations. `READ_INPUT` wire sources are adapter-read inputs:
    valid-JSON sources may back successful read contracts (for example included-isolation wire
    strings), while invalid, empty, or whitespace inline sources are `READ_INPUT` values permitted
    only on failure contracts. A single fixture path may back distinct named representations
    with different roles. Core values expose `JsonApiDocument`. A scenario may have zero
    or more of each kind, and names distinguish pretty/compact/empty/whitespace wires and
    context-specific core outcomes. ADR-013
    records domain representation as a required layer without freezing its Java factory shape; the
    dependent domain plan adds and proves that permitted variant with real consumers.
  - `FixtureContract` extends legacy `Scenario`, so `id()` and `notes()` remain the stable
    operation-case identity presented in Spock names and diagnostics. For codec contracts,
    `notes()` is loaded from the owning manifest row; it is never the semantic scenario description.
    Each typed contract directly references only the representation variants it can consume;
    behavior-specific request, policy, expectation, and diagnostic variants remain on that contract
    rather than on `SemanticScenario`.
  - A package-private semantic registry/builder owns graph construction, cross-reference validation,
    typed indexes, and contract ownership for one assembly invocation. The builder/registry and every
    family-registration step live in `io.github.kazemek.jsonapi.testfixtures` (the same package as
    `JsonApiFixtures` and the assembly bridge); the `semantic` package contains only public
    model/contract types. Family declarations (for example under `codec.cases`) expose their
    declaration data through public static accessors consumed by the coordinator; they never receive
    builder access. The lifecycle is explicit:
    create one internal builder, register every family selected for semantic migration in the current
    repository revision through package-private family-registration steps, invoke one final freeze, then derive
    `FixtureCatalog<SemanticScenario>` in first semantic-registration encounter order and the
    private immutable identity-keyed contract-to-semantic-owner index from the complete graph. The
    "present" family set means families in that revision's semantic graph, not every legacy
    catalogue that happens to exist in the repository. The Foundation revision is independently
    implementable with codec-only assembly: traverse
    `manifest.json`, then `ambiguous-manifest.json`, then `negative-manifest.json`, register those
    roots, and freeze once after the codec steps available in that revision. Later Domain, Compound,
    Sparse, and Convergence migrations extend that same pre-freeze assembly sequence by adding their
    registrations before the existing final freeze boundary; they do not create a second registry or
    wait for later families before Foundation can be implemented. There is no runtime post-freeze
    extension, public registration API, or mutable builder surviving across assembly invocations or
    repository revisions. The registry maintains an identity-keyed owner/reference map incrementally
    during pre-freeze registration and exposes a package-private lookup to family-registration steps of
    later revisions, so later families resolve the owners of earlier-registered representations and
    contracts before the final freeze; owner resolution is always completed before freeze. Build the
    sealed immutable owner index from that validated map during the freeze traversal, reject an identical instance attached to zero or multiple scenarios, discard mutable builder
    state, and use the frozen index only to prove direct typed-catalog ownership and compatibility
    joins resolve to the same semantic scenario. Domain-write, Domain-read, Compound-write,
    Sparse-fieldset, and Envelope-read legacy catalogues remain outside the Foundation graph until
    their own migrations add their typed registration steps.
  - Public semantic model types live under
    `io.github.kazemek.jsonapi.testfixtures.semantic`. Package-private
    `SemanticFixtureAssembly` and `SemanticFixtureBundle` live in the existing
    `io.github.kazemek.jsonapi.testfixtures` package so `JsonApiFixtures` can call the sole
    package-private `SemanticFixtureAssembly.bundle()` entry point. That entry point uses one lazy,
    cached immutable bundle per classloader; every semantic, typed-contract, compatibility, and
    facade accessor reads that bundle. Package-private family-registration methods are invoked only
    by this coordinator before freeze; sibling legacy catalog packages never receive builder access,
    and no public registration or mutation API exists.
  - Public retrieval continues to use only `FixtureCatalog` and compile-time facade methods:
    `JsonApiFixtures.scenarios()` returns `FixtureCatalog<SemanticScenario>`, while
    `codecReadContracts()`, `codecWriteContracts()`, and `schemaContracts()` return
    `FixtureCatalog<CodecReadContract>`, `FixtureCatalog<CodecWriteContract>`, and
    `FixtureCatalog<SchemaContract>` directly. The typed accessor area labels are `semantic`,
    `codec-read`, `codec-write`, and `schema` and their unknown-id diagnostics are asserted by tests.
    Later plans add equally explicit typed methods for
    their finite contract roots; do not expose wrappers or runtime class-token dispatch.
- Semantic-family declaration classes may be split where readability or manifest loading warrants
  it, but all register into the one internal registry. Do not use classpath scanning or import an old
  `*Scenarios` list as canonical data. Java codec declarations are keyed by stable case id and own
  only fields absent from manifests: semantic id, core values, complete contexts, capabilities,
  schema expectations, inline wire variants, and supplemental writer assertions. They do not repeat
  manifest-owned ids, notes, fixture paths, order, or negative diagnostics. The compact exact-UTF-8
  source (`member-order.compact.json`) is a Java-declared named `EXPECTED_OUTPUT` wire representation
  referenced by the `ExactUtf8` assertion; it is not added to the manifest.
- The Foundation-owned declaration coverage matrix has one manifest-keyed row for every positive,
  ambiguous, and negative manifest entry. It records only the fields absent from the manifests:
  semantic owner id, named representation/source handles, typed contract namespace and id, expected
  core values, complete contexts, and capability roots. The matrix must be assembled from the
  existing case declarations as a test-only typed table; it is not stored as a second manifest-style
  file. It must reject missing, extra, duplicate, or multiply consumed rows while leaving manifest
  ids, paths, order, notes, and diagnostic metadata in their existing persisted owners. The explicit
  semantic merges and independent negative scenarios above are the required mapping exceptions.
- Codec typed and compatibility order is materialized directly during manifest traversal, with no
  separate order field or post-hoc sort. `codecReadContracts()` has one total order: applicable
  positive contracts in `manifest.json` order, then ambiguous contracts in
  `ambiguous-manifest.json` order, then failure contracts in `negative-manifest.json` order.
  `codecWriteContracts()` and `schemaContracts()` follow applicable `manifest.json` order.
- Enforce at construction/catalogue-test time: globally unique nonblank semantic ids; nonblank and
  unique representation names within a scenario; at least one contract per scenario; every
  contract representation reference belongs to its owning scenario; every registered representation
  instance has exactly one semantic-scenario owner; one registration of each contract object; exactly
  one derived semantic owner for each contract; and unique contract ids within each typed contract-root
  namespace. References from multiple contracts in the same owner are valid; cross-owner sharing and
  unlisted aliases are rejected. The same legacy id may appear in different namespaces such as codec
  read and codec write. Preserve manifest-derived codec projection order and immutable defensive
  copies.
- Semantic ids are new, explicit, globally unique dot-separated identities named for the JSON:API
  example or behavior, not for a consuming adapter operation. Enforce
  `[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)+` at construction. They are not dispatch keys and do not
  replace or alias existing case ids. Pin these reusable anchors so later plans do not reinvent
  names: `document.extension-and-at-members`, `document.identifier.single`,
  `document.identifier.empty-collection`, `article.relationship.author-null`,
  `article.relationship.comments-empty`, `article.compound.author`,
  `article.compound.nested-comments-author`, and `article.compound.shared-author`. Other codec-only
  examples receive equally explicit semantic ids; visually similar but semantically different
  examples remain separate.

## Typed codec contracts

- Add a sealed `CodecReadContract` with typed success and failure variants. Success references one
  wire representation and exposes a non-empty insertion-ordered map from complete
  `DocumentReadContext` to expected core representation. Its factory/builder accepts ordered outcome
  entries, rejects duplicate complete contexts before map construction, then freezes the validated
  map; normal reads have one entry and ambiguous-primary-data reads have resource and
  resource-identifier entries with distinct complete contexts. Retain a separate nullable
  `legacyPrimaryDataKind` compatibility value for each built-in codec projection: it preserves the
  existing `CodecScenario.primaryDataKind` null for kind-neutral/no-primary-data rows and preserves
  explicit `RESOURCE` or `RESOURCE_IDENTIFIER` values. A canonical effective `RESOURCE` context
  fallback for no-primary-data execution must never overwrite that legacy null. Failure references a non-empty
  ordered collection of named wire representations, one complete `DocumentReadContext`, and typed
  category, pointer, rule-code, source-location, and optional message-policy expectations; it has no
  core representation. Manifest `category` is mandatory and is parsed eagerly to
  `CodecFailureCategory`; unknown values fail graph construction, the failure contract stores the
  enum, and executors never redispatch from arbitrary strings. Represent pointer expectation as an
  explicit unspecified/exact value: manifest `pointer: null` creates unspecified and therefore no
  shared exact-pointer assertion, while every non-null value creates exact and is validated as a
  JSON Pointer. Exact `""` is the real root pointer and cannot collapse into unspecified; executors
  compare pointer equality only for exact expectations and never invent a value for unspecified
  ones. Represent rule-code expectation separately as an optional typed `ValidationRuleCode`:
  manifest `ruleCode: null` means exact runtime absence, a non-null value is parsed eagerly, unknown
  values fail graph construction, and executors always compare exact value or exact absence.
  `LOCAL_VALIDATION` and `AGGREGATE_VALIDATION` require a rule code;
  `MALFORMED_JSON`, `UNEXPECTED_TOKEN`, and `DUPLICATE_MEMBER` forbid one. Validate these
  combinations when constructing the failure contract. Add typed enum
  `CodecFailureMessagePolicy` with `EXCLUDES_INPUT`; failure contracts expose the optional
  manifest-derived policy without nullable or prose-based dispatch. Missing `messagePolicy` means no
  policy; explicit JSON `null`, non-string values, and unknown strings are rejected before contract
  construction. Every shared adapter executor
  applies it to every named wire representation on that failure contract. This permits the stable
  `empty-input` contract to own its empty-file, inline-empty, and inline-whitespace inputs. Default
  negative contexts are registered explicitly rather than inferred by an adapter.
- Exact exception message text and cause shape are not shared fixture semantics in this unit. Keep
  Jackson 3's exact empty-input message `Expected a JSON:API document object` and
  `JsonApiDocumentReadException.cause == null` as explicit adapter-local executor invariants. Do not
  add exact-message or cause fields to shared contracts. Jackson 2/future adapter parity covers
  category, pointer, rule code, manifest message policy, and capability-qualified source location.
- Separately retain Jackson 3's existing broad adapter-local redaction safety invariant: for every
  applicable non-empty negative corpus source, its exception message excludes the complete raw
  source text whether or not the shared contract declares a message policy. Keep that corpus-wide
  assertion independent from execution of manifest-owned `EXCLUDES_INPUT`; the former does not
  become a future Jackson 2/Gson contract field, and the latter remains portable shared semantics
  only where explicitly declared.
- Add `CodecWriteContract` from one core representation and complete `ValidationContext` to one
  mandatory structural expected-wire representation plus an immutable list of sealed supplemental
  assertions (`ExactUtf8` and `HreflangArray`). Contract presence is the capability: a semantic
  scenario without a write contract is not writable, and supplemental checks never replace the
  structural comparison. Its factory accepts only `EXPECTED_OUTPUT` wire representations with valid
  JSON content; `READ_INPUT` invalid/empty/whitespace sources are rejected for writer expectations.
- Add `SchemaContract` from a selected core representation plus writer `ValidationContext` to
  `SchemaKind`, with a sealed conforms/known-disagreement expectation. Jackson schema execution must
  validate JSON generated by the adapter writer from those inputs; a stored wire representation is
  only the separate codec-write expectation and never substitutes for generated schema input.
  Preserve every `SchemaDisagreement` keyword/path/reason.
- Put `single-identifier` and `ambiguous-object-primary-data` contracts on one semantic scenario
  with separate pretty/compact wire representations and both valid core representations. Do the
  analogous merge for `empty-identifier-collection` and
  `ambiguous-empty-array-primary-data`. Put the positive codec, known schema disagreement, and
  negative no-extension-context contracts for `extension-and-at-members` on one semantic scenario.
  Keep all case ids and both wire paths where paths differ.
- Register every other positive or negative codec case exactly once. Of the current negative rows,
  only `extension-members-require-context` attaches to an existing positive semantic scenario; each
  other negative row owns one independent wire-only semantic scenario unless a future reviewed plan
  identifies another exact semantic merge. `NegativeCodecScenarios` becomes a projection and not a
  second loaded catalogue. Keep the JSON-P loader and manifest-backed declaration mechanism.
- Build positive, ambiguous, and negative typed/legacy projections during the manifest-first graph
  construction above. Manifest rows supply their owned runtime fields; declarations supply only
  absent semantic/model/context data. For each positive row, validate one declaration, one owning
  semantic scenario, compatible wire/core/context references, and the declared read/write/schema
  capability combination before materializing one `CodecScenario` from the exact canonical contract
  instances. Ambiguous and negative traversals likewise require exactly one declaration per row and
  no unconsumed declarations; negative execution coverage includes every named wire representation
  on its failure contract.

## Deliverables

- Add and index `docs/adr/013-unified-semantic-fixture-catalogue.md`, recording the scenario /
  representation / contract separation, one canonical explicit-registration source, typed
  projection and identity rules, existing `FixtureCatalog` plus dedicated typed facade accessors,
  direct contract catalogues plus the derived private owner index, manifest-owned codec order,
  derived compatibility views, Jackson-major-neutral semantic boundary, adapter-local execution
  boundary, source-layout non-decision, and additive extension point used by future adapters and
  PATCH. It states that the architecture does not preclude broader future adapter reuse while the
  current `jsonapi-java-jackson-common` dependency does not itself establish fully generic adapter
  neutrality. It records the three representation roles while leaving the concrete domain factory
  variant to its first consuming migration. Link ADR-007, ADR-009, and ADR-010 rather than
  duplicating their rules.
- Update `docs/conformance.md` to identify the frozen semantic graph and direct typed codec catalogues
  as the canonical fixture contract owner, while describing the retained `CodecScenario`,
  `AmbiguousPrimaryDataScenario`, and `NegativeCodecScenario` views as compatibility projections.
  Preserve all existing conformance statuses, capability rows, and wire-behavior claims.
- Implement and document the target semantic types, invariants, explicit builder, and
  `JsonApiFixtures.scenarios()` / `codecReadContracts()` / `codecWriteContracts()` /
  `schemaContracts()` surfaces. Use `module-docs` for the changed package, facade, README,
  contributor flow, and entry-point Javadoc.
- Migrate the full positive, ambiguous, negative, writer, and schema codec inventories atomically
  into typed semantic contracts. Retain or reorganize `codec.cases` only when the resulting semantic
  declarations are clearer; do not move sources merely to reproduce or negate PR #83.
- Reconcile the two ambiguous manifest notes to the exact currently exposed compatibility text named
  in Research and constraints, then remove their Java note declarations so canonical and legacy
  values derive from the corrected manifest rows. No other manifest field changes.
- Add `messagePolicy: "EXCLUDES_INPUT"` to the `malformed-json-without-payload` negative manifest
  row, load it into the typed failure contract, and update the wire-corpus README to distinguish
  shared category/pointer/rule-code/source-location/message-policy metadata from adapter-local exact
  message text, cause shape, broad Jackson 3 no-complete-source-echo safety, parser mechanics, and
  exact location details.
- Retain `CodecScenario`, `AmbiguousPrimaryDataScenario`, and `NegativeCodecScenario` as
  source-compatible immutable DTOs. Preserve each public canonical record constructor and
  `CodecScenario.of(...)`, including standalone construction behavior. Retain the complete existing
  static surfaces of `CodecScenarios`, `AmbiguousPrimaryDataScenarios`, and
  `NegativeCodecScenarios`, the corresponding current `JsonApiFixtures` accessor signatures, lookup
  behavior/diagnostics, and codec capability methods. Materialize repository-owned built-in entries
  losslessly from the dedicated typed contract catalogues through manifest-ordered validated joins;
  projection code may invoke the preserved constructors/factory. No built-in compatibility
  declaration list, static catalogue registration, or facade storage may independently declare or
  register expected paths, models, contexts, capabilities, or diagnostics. Caller-constructed DTO
  values remain ordinary noncanonical standalone values and do not register into the semantic graph.
  The `NegativeCodecScenario` projection converts typed category and rule code back to their stable
  enum names and converts unspecified/exact pointer expectation back to null/the exact string so its
  existing string/null public shape remains unchanged.
- Migrate Jackson 3 codec/schema parameterization to canonical typed contract projections and typed
  variant dispatch. Jackson-specific input-source, parser/sink lifecycle, `JsonMapper`, wire-equality
  fallback, schema-engine, and exception mechanics remain adapter-local.
- Materialize the fixed test-only codec capability baseline before replacing the built-in codec
  compatibility ownership, preserve its independent durable contents across the migration, and
  expose the named path as the handoff consumed by Convergence. Do not add equivalent
  capability-baseline storage for Domain, Compound, or Sparse; those families own their independent
  pre-migration characterization evidence in the durable forms their own plans establish.

## Non-goals

- Migrating domain-write, domain-read, compound-write, sparse-fieldset, or envelope-read declarations;
  they remain their sole canonical owners until their dependent plans switch them atomically.
- Renaming current contract ids, moving or rewriting canonical wire files, changing manifest order,
  or deleting duplicate paths whose stability is currently tested.
- Jackson 2, Gson, PATCH behavior, adapter capability matrices, runtime adapter selection, or
  adapter-specific expected semantics.
- Promoting Jackson 3's exact empty-input message or null-cause assertion into shared fixture
  metadata, or weakening the shared `EXCLUDES_INPUT` policy into notes/id special casing.
- Removing, renaming, reshaping, restricting, or simplifying the existing codec compatibility DTOs,
  their public canonical record constructors, `CodecScenario.of(...)`, static catalogue surfaces,
  `JsonApiFixtures` accessor signatures, lookup diagnostics, or capability methods. Their eventual
  deletion or broader simplification requires a later reviewed convergence decision after current
  consumers migrate.
- A nullable/boolean `Scenario` god object, one mandatory representation of each layer, a public
  scenario-contract wrapper, or flattening schema, exact-byte, context, and diagnostic distinctions.
- A universal inline or per-case source-file convention.

## Source-of-truth transition

- After this unit, the frozen semantic graph is the sole runtime owner of executable codec contracts
  and every repository-owned built-in codec legacy catalogue entry is derived. The three manifests
  remain the persisted owners of their existing row fields; Java declarations own only the
  semantic/model/context fields absent from those manifests. This includes optional negative
  `messagePolicy`; no Java declaration or executor may infer or independently declare that policy.
  All three `*Scenarios` static surfaces, current facade accessors, lookup behavior, and capability
  methods obtain built-in values only through canonical projection; there is no independently
  editable compatibility registration list or second built-in storage source. The retained DTO
  constructors/factory remain public and source-compatible, may be used by projection code, and may
  construct arbitrary standalone values for callers; direct construction neither registers a value
  nor makes it canonical catalogue-owned data. The five not-yet-migrated operation families remain
  their own documented owners; do not import them into the semantic catalogue through generic
  legacy wrappers.
- Each following migration adds typed contracts directly to the same pre-freeze assembly sequence
  and replaces that family's storage in the same change. A repository revision registers every
  family present in that revision and freezes once; Foundation's codec-only revision is valid, while
  later revisions add their family-registration steps before that same final freeze. At no point may
  an old and new list both accept independent additions for the same contract namespace, and no
  family may extend the frozen graph.

## Test strategy

- Add semantic-registry and `FixtureCatalog` tests for every construction invariant, semantic-id
  grammar, deterministic dedicated typed projections, namespace-local case-id uniqueness, unique
  read-success context keys, non-empty failure wire inputs, direct representation membership,
  exactly-one representation and derived contract ownership, immutable catalogues/indexes, and unknown
  semantic/contract ids without relying on adapter classes. Prove direct typed catalogues contain the
  same contract instances attached to semantic scenarios and that no caller can mutate or separately
  populate the private owner index.
- Exercise the codec-only Foundation assembly and assert one internal builder, one final freeze, and
  immutable derived catalogues/indexes. The test and handoff must make later family-registration
  steps explicit pre-freeze additions to that same assembly path, with no post-freeze extension,
  second registry, public registration API, or builder retained across revisions.
- Assert that repeated calls through `SemanticFixtureAssembly.bundle()`, `JsonApiFixtures`, typed
  contract accessors, and retained compatibility projections use the same cached immutable bundle and
  contract/representation instances; only the mutable builder is discarded after freeze.
- Add failure-contract and loader tests proving missing/null category rejection and mandatory typed
  category parsing; rejection of unknown categories and rule codes; valid exact pointer syntax;
  distinct unspecified pointer and exact root `""` states; required rule codes for local/aggregate
  validation; forbidden rule codes for malformed JSON, unexpected token, and duplicate member; and
  exact runtime rule-code absence when the manifest declares null. Do not change any existing
  category, pointer, or rule-code value.
- Add representation-role tests proving valid expected-output wire sources are accepted by structural
  writer contracts, while malformed, empty, and whitespace inline sources are rejected there and
  remain valid only as named failure-contract read inputs.
- Rewrite codec catalogue specs to prove manifest/order/path/notes bijections against the derived
  views, the explicit positive/ambiguous/negative total read order, and that every manifest row plus
  declaration materializes exactly one applicable canonical contract set. Prove every legacy entry
  derives from those same instances. Replace the one-time conversion oracle with live projection
  equivalence after it confirms the one-time ambiguous-note reconciliation; do not retain hard-coded
  manifest metadata as a parallel source.
- Capture the codec capability baseline before the compatibility projection switch, then have the
  canonical codec projections join against its stable namespace/id membership and reject missing,
  extra, duplicate, or conflicting read/write/schema selections. Do not derive expected capability
  membership from the canonical catalogue under test.
- Add source-compatibility and projection tests for all three retained codec DTO types, their full
  `*Scenarios` static APIs, corresponding current `JsonApiFixtures` accessor signatures, lookup
  diagnostics, and capability methods. Prove ids, reconciled notes, manifest order, paths,
  model/context/diagnostic values, and capability selections are equal to the current observable
  surface and all built-in entries derive only from canonical contracts/manifests. Pin the public
  canonical constructor signatures for all three records and the `CodecScenario.of(...)` signature
  and behavior; prove standalone values can still be constructed without changing either canonical
  or compatibility catalogue contents, and every exposed built-in entry traces to a canonical
  registration.
- Prove positive, ambiguous, and negative inventories are complete; the two ambiguous scenarios
  execute both context outcomes; extension members succeed with the extension context and fail with
  the existing diagnostic without it; `empty-input` covers file/empty/whitespace named inputs; and
  explicit null, absent, and present-empty values stay distinct.
- Prove the negative loader accepts absent `messagePolicy`, maps only `EXCLUDES_INPUT` to the typed
  policy, rejects explicit null, non-string, and unknown values, and places the policy only on the canonical
  `malformed-json-without-payload` failure contract. Jackson 3 canonical-contract execution must
  assert each applicable named source text is absent from the exception message because of that
  policy, without dispatching on case id or notes.
- Preserve exact-byte `member-order`, hreflang-array, schema hash/disagreement, source-location,
  caller-resource ownership, and codec reader/writer isolation assertions. Separately retain a
  Jackson 3 adapter-local negative-corpus test that checks every applicable non-empty source is not
  echoed in full, regardless of shared message-policy presence; do not let the targeted shared-policy
  test replace or narrow this corpus-wide safety proof. Also retain Jackson 3 adapter-local tests for
  exact empty-input message text and null exception cause. Future adapters do not inherit these
  three mechanics from shared contracts.
- In Jackson 3, record executed contract ids for each applicable codec-read, codec-write, and schema
  projection and compare them with the full canonical typed projection so adding a contract cannot
  silently escape its executor.

## Acceptance criteria

- [ ] ADR-013 is accepted and indexed, and the `module-docs` checklist passes for the semantic
      package, dedicated `JsonApiFixtures` semantic/contract accessors, compatibility facade, README,
      and contributor guidance.
- [ ] `docs/conformance.md` names the frozen semantic graph and direct typed codec catalogues as
      canonical and the retained codec DTO/catalogue views as derived compatibility projections,
      without changing conformance status or wire claims.
- [ ] `SemanticScenario`, typed named representations, typed contracts, the internal registry,
      and existing `FixtureCatalog` enforce the identity/reference/immutability/order rules above
      and expose Java- and Spock-friendly direct typed catalogues without `ContractCase`, runtime
      class-token dispatch, a nullable/boolean god object, or a second public catalogue abstraction;
      the same canonical registration traversal derives a private immutable exactly-one-owner index;
      a codec-only revision freezes once after its available registrations, and later family
      registrations occur before that same final freeze with no post-freeze extension, second
      registry, public registration API, or cross-revision mutable builder.
- [ ] The package-private `SemanticFixtureAssembly.bundle()` entry point in the root fixture package
      is the sole assembly bridge used by `JsonApiFixtures` and family registration steps; all
      accessors read one lazy cached immutable bundle, and no sibling legacy package or public API can
      register or mutate canonical state.
- [ ] Every registered wire/core/domain representation instance has exactly one semantic-scenario
      owner; same-owner contract references are allowed, while cross-owner sharing and unlisted
      aliases are rejected. Structural writer expectations accept only valid `EXPECTED_OUTPUT` wire
      sources; invalid/empty/whitespace sources remain failure-only read inputs.
- [ ] Every positive, ambiguous, negative, write, exact-byte/hreflang, and schema codec case is
      represented exactly once in the canonical model; all existing case ids, paths, manifest order,
      and diagnostic metadata are unchanged. The two ambiguous manifest notes are reconciled to the
      exact previously exposed Java compatibility texts, all other notes are unchanged, and all
      notes thereafter materialize from their owning manifest rows rather than repeated Java
      declarations. The combined read catalogue follows the specified
      positive-then-ambiguous-then-negative total order.
- [ ] `negative-manifest.json` optionally owns `messagePolicy`; only
      `malformed-json-without-payload` declares `EXCLUDES_INPUT`, it materializes as typed
      `CodecFailureMessagePolicy.EXCLUDES_INPUT`, absent differs from explicit null/non-string,
      unknown values fail loading, and every applicable named wire representation is checked for
      source-text exclusion without id/note dispatch.
- [ ] All three manifest roots are loaded through the one neutral JSON-P strategy with root-specific
      field/type validation and eager rejection of malformed or missing rows; no adapter-major parser
      or second manifest loader is introduced.
- [ ] `SemanticScenario.description` is nonblank Java-owned semantic prose, while every codec
      `FixtureContract.notes()` value is the exact owning manifest-row note and is not used as a
      semantic description or diagnostic substitute.
- [ ] Every failure contract stores a mandatory typed `CodecFailureCategory`; unknown category or
      rule-code strings fail canonical loading; pointer expectation distinguishes unspecified
      manifest null from exact root `""`, validates every exact pointer, and causes exact pointer
      comparison only when specified; runtime rule-code comparison includes exact absence; and
      construction requires rule codes for local/aggregate validation while forbidding them for
      malformed JSON, unexpected token, and duplicate member.
- [ ] The ambiguous object/array and extension-context examples use multiple representations or
      contracts on shared semantic scenarios, while negative cases with no valid core value remain
      wire-plus-failure contracts.
- [ ] `CodecScenario`, `AmbiguousPrimaryDataScenario`, `NegativeCodecScenario`, the complete existing
      static surfaces of `CodecScenarios`, `AmbiguousPrimaryDataScenarios`, and
      `NegativeCodecScenarios`, the corresponding current `JsonApiFixtures` accessor signatures,
      lookup behavior/diagnostics, and capability methods remain source-compatible lossless derived
      projections. They preserve ids, reconciled notes, manifest-owned order, wire paths, applicable
      model/context/diagnostic values, and capability behavior; manifest-first construction rejects
      missing/extra declarations and cross-root scenario/wire/core/context inconsistencies without
      repeating manifest fields, storing another order, or allowing independently editable
      built-in compatibility storage. Their existing public canonical record constructors and
      `CodecScenario.of(...)` retain signatures and standalone behavior; direct DTO construction does
      not register catalogue data, while every repository-owned built-in compatibility entry derives
      from canonical registration.
- [ ] Before canonical codec replacement, the Foundation-owned
      `semantic-catalog-baselines/codec-capabilities.tsv` baseline independently preserves every
      stable codec id's pre-migration read/write/schema membership, with ambiguous and negative ids
      read-only. Its exact header, namespace vocabulary, one-row-per-namespace/id rule, boolean
      encoding, and missing/extra/duplicate-row validation are enforced; the Convergence oracle joins
      it by namespace/id and never derives expected capability sets from the canonical catalogue.
- [ ] The manifest-keyed declaration coverage matrix represents every current manifest row exactly
      once with semantic owner, named representation/source, typed contract namespace/id, core/context,
      and capability fields, without duplicating manifest-owned paths, order, notes, or diagnostics.
- [ ] The canonical-to-legacy projection preserves nullable `CodecScenario.primaryDataKind` exactly:
      kind-neutral/no-primary-data rows remain null even when canonical execution uses an effective
      `RESOURCE` context fallback; explicit `RESOURCE` and `RESOURCE_IDENTIFIER` values remain
      explicit.
- [ ] Jackson 3 codec/schema executors consume canonical typed projections with full applicable
      projection coverage; read failures and writes use their complete shared contexts, schema
      checks validate writer-generated output from the selected core/context, and no shared expected
      behavior depends on a concrete Jackson-major package. Documentation describes the contracts as
      Jackson-major-neutral today and broader-adapter-compatible in architecture, not already proven
      fully generic through `jsonapi-java-jackson-common`. Jackson 3 separately retains its exact
      empty-input message and null-cause tests plus the independent broad no-complete-source-echo
      check for every applicable non-empty negative corpus input as adapter-local invariants; those
      mechanics are absent from shared contracts and future-adapter parity requirements. The broad
      Jackson 3 check remains separate from portable manifest-owned `EXCLUDES_INPUT` execution.
- [ ] Every added production Java package is `@NullMarked`; every public type use that still permits
      Java null, including retained compatibility DTO members, has accurate `@Nullable` metadata;
      and explicit wire-null states remain typed sealed variants rather than bare Java null, as
      required by ADR-009.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` and focused Jackson 3 codec/schema specs pass.
- [ ] The `spotless-format` skill runs `./gradlew spotlessApply` then
      `./gradlew spotlessCheck`, followed by `./gradlew clean build`.
- [ ] The `sonar-quality-gate` skill's Quality Gate wait and authenticated Issues API check both
      exit 0 with zero unresolved new-code issues.
