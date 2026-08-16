# Semantic Fixture Envelope Contracts and Catalog Convergence

> **Scope:** `jsonapi-java-test-fixtures`, Jackson 3 envelope tests, and final fixture-catalog documentation
> **Dependencies:** [Semantic Fixture Catalog Foundation and Codec Contracts](semantic-fixture-catalog-foundation.md), [Semantic Fixture Domain Read and Write Contracts](semantic-fixture-domain-contracts.md), [Semantic Fixture Compound Inclusion Contracts](semantic-fixture-compound-contracts.md), [Semantic Fixture Sparse Fieldset Contracts](semantic-fixture-sparse-fieldset-contracts.md)
> **Status:** Not started
> **Work item:** KAZ-18

## Goal

Move typed-envelope binding into semantic contracts and complete the migration so every shared
fixture family has one canonical semantic owner, every legacy API is a derived view, and catalogue-
wide coverage prevents loss, duplication, or drift before future PATCH and adapter work begins.

## Research and constraints

- The prerequisite plans make codec, flat domain, compound, and sparse-fieldset contract roots
  canonical. This unit is the convergence gate: after it lands, no existing shared operation family
  may retain an independently editable scenario list.
- [`EnvelopeReadScenarios`](../../jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/enveloperead/EnvelopeReadScenarios.java)
  and its `EnvelopeReadScenario`, sealed `EnvelopeReadVariant`, `EnvelopeReadInput`,
  `EnvelopeReadExpectation`, `EnvelopeReadCase`, entry-point/reader-context enums,
  `IncludedExpectation`, and `EnvelopeBindingDocument` own 22 closed-inventory cases.
  `EnvelopeReadScenariosCatalogSpec` and `EnvelopeReadFixtureModelsSpec` pin the exact inventory,
  input/context/entry-point guards, target packages, core/wire resolvability, identity probes,
  validation-invalid documents, and constructor rejection.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) requires independent included binding,
  explicit registration, ordered and identity-indexed DTOs, and no relationship injection. Envelope
  contracts must not be conflated with flat binder included-isolation or compound traversal.
- Existing cross-catalogue evidence must become direct semantic references:
  - envelope codec inputs already reuse codec case ids for null data, meta-only, errors, included,
    compound, identifier, and other document forms;
  - `envelope-binding/independent-envelopes-matching.json` is byte-identical to
    `documents/compound-document.json`;
  - `envelope-binding/unregistered-primary-single.json` and
    `unregistered-primary-collection.json` are byte-identical to `documents/single-identifier.json`
    and `documents/identifier-collection.json`;
  - compound shared-author binding uses the codec shared-author graph but proves one bound DTO and
    identity lookup, not traversal deduplication.
  Preserve every existing path as a named wire representation even where bytes are equal, and add
  equality invariants rather than deleting/renaming files.
- Explicit null data versus absent data, absent versus present-empty `included`, id/lid dual-key
  identity, unregistered/conflicting types, joined document pointers, mutation safety, cyclic
  linkage, duplicate included identities, and registry construction are independent typed
  behaviors. Similar values do not justify merging their contracts.
- Jackson 3 `DomainDocumentReaderSpec` owns mapper/reader construction, stream/parser lifecycle,
  codec-text loading, context derivation, registry execution, and exception mechanics. Shared
  contracts own adapter-neutral inputs, target registrations, complete envelope values,
  identities, diagnostics, and mutation-safety expectations.
- Existing compatibility surfaces are deliberately retained because current Not-started Jackson 2
  plans name them. They may remain indefinitely as derived internal views, but documentation must
  prohibit adding cases there. New capabilities such as PATCH use canonical contracts directly and
  do not receive a compatibility silo merely for symmetry.
- Planning authority is supplied by the accepted live Foundation, Domain, Compound, and Sparse plan
  contracts linked above. Their `Not started` status records that implementation has not begun; it
  does not make their accepted design contracts unavailable to this downstream planning review.
- ADR-013 is a planning obligation defined by the accepted Foundation plan, not a planning-time file
  prerequisite. The Foundation implementation must materialize and index the accepted ADR-013
  before Convergence implementation begins; this plan must link that resulting ADR and final
  verification must inspect the materialized file and index rather than treating plan prose as proof.
- Existing capability applicability evidence is split by the pre-migration repository model: positive
  `CodecScenario` declarations expose `writable`, `readable`, and `schemaKind != null`; the explicit
  `AmbiguousPrimaryDataScenarios` catalog is reader-only; and the JSON-P-backed
  `NegativeCodecScenarios` catalog is read-only. The persisted manifests own IDs, paths, notes, and
  negative diagnostics but contain no capability flags. Capture these existing observable memberships
  in a fixed test-only baseline before canonical replacement; it is a migration oracle, not runtime
  ownership or a second catalogue.

## Target contract model

- Add `EnvelopeReadContract` as a registered `FixtureContract` root whose canonical entries are
  typed executable leaves: one leaf for each current `EnvelopeReadCase` and each
  `EnvelopeReadVariant.RegistryAttempt` (34 leaves across the current 22 stable scenario groups).
  Each leaf has an explicitly declared stable `leafId` exposed as `EnvelopeReadContract.id()`, a
  separate legacy scenario id, an ordered group position, document-binding or registry-attempt
  variant, and the existing typed input/entry-point/context/expectation state. The direct
  `FixtureCatalog<EnvelopeReadContract>` is ordered by leaf registration and keyed only by `leafId`;
  `EnvelopeReadScenarios.byId` remains keyed only by the separate legacy scenario id. The 22
  `EnvelopeReadScenario` values remain an ordered grouping projection over those leaves, not
  canonical aggregate contracts. `EnvelopeReadContract.notes()` is the unchanged legacy group id for
  compatibility diagnostics; leaf ids never replace that notes value. The grouped
  `EnvelopeReadScenario` derives both `id()` and `notes()` from the same fixed legacy group row.
- Replace codec-id/path indirection in canonical declarations with direct named
  `WireRepresentation` / `CoreRepresentation` references on the owning semantic scenario. For a
  codec-derived input, also retain a typed `CodecReadOutcomeReference` to the exact canonical codec
  read contract/outcome, including stable codec case id, validation context, primary-data kind, and
  projection provenance. The leaf projector uses that reference to reconstruct
  `EnvelopeReadInput.CodecFixture` and its reader context without path/ID searches or duplicated
  context metadata. `EnvelopeBindingDocument` and `CoreDocument` values remain directly named
  representations for their owning leaves.
- Each document-binding leaf stores its reader-context strategy explicitly. `CodecReadOutcomeReference`
  supplies context and primary-data kind only for `CODEC_DERIVED` leaves; `IDENTIFIER_DEFAULTS` and
  `RESOURCE_DEFAULTS` remain explicit canonical values, including the no-primary-data fallback to
  the existing resource interpretation.
- `CodecReadOutcomeReference` selects exactly one canonical codec read contract and outcome key,
  matches that outcome's named wire/core representations and complete validation context, carries a
  closed `CodecReadProjectionProvenance` value (`POSITIVE_MANIFEST`, `AMBIGUOUS_MANIFEST`, or
  `NEGATIVE_MANIFEST`), and is permitted only for `CODEC_DERIVED` leaves. It stores the effective
  non-null reader primary-data kind, including `RESOURCE` for the existing no-primary-data fallback;
  nullable raw `CodecScenario.primaryDataKind` remains compatibility metadata only. Default-context
  leaves reject an outcome reference. The selected outcome key must match the effective kind and
  expected core representation before registration, and provenance is diagnostic metadata only, never
  a dispatch key.
- `CodecWireSourceReference` is a separate source-only value for default-context codec leaves. It
  carries the exact canonical codec wire representation and its stable legacy codec case id, supplies
  no reader context, primary-data kind, or outcome selection, and is permitted only for
  `IDENTIFIER_DEFAULTS`/`RESOURCE_DEFAULTS` leaves. Its wire representation and legacy id must match
  before registration, and the compatibility projector uses the stored id directly without a reverse
  path lookup.
- A typed `EnvelopeBindingSourceReference` owns each `EnvelopeBindingDocument` enum identity and its
  named wire representation; a `CoreDocument` source pairs that binding identity with its named core
  representation. The enum remains the persisted corpus index and compatibility projection source;
  no independent semantic path table is allowed.
- Core representations may hold locally constructible but aggregate-invalid documents, including the
  duplicate-included-identities document. Registration must not perform aggregate validation for those
  leaves; only `CoreDocument` inputs on `FROM_DOCUMENT` negative leaves may reference them. They may
  not back codec outcomes or ordinary wire-read leaves. Adapter execution retains the expected
  validation-invalid diagnostic and pointer.
- Reference separate named read DTO/domain representations for primary and included values. Identity
  probes and expected shared instances remain envelope contract state; do not place them on generic
  semantic scenarios or reuse compound `IncludedResourceRef` as an envelope identity type.
- Preserve constructor/catalogue guards for entry point, reader context, non-empty cases/attempts,
  target registration, core-only `FROM_DOCUMENT`, validation-invalid paths, and representation
  ownership.
- Validate the grouping projection before materialization: all leaves in one legacy group must share
  variant kind; document-binding leaves must share target-class order and entry point; registry groups
  must contain only registry attempts; and group positions must be unique and contiguous. Project
  cases/attempts strictly by group position, preserving valid zero-target document bindings.
- Canonical envelope expectations reference separate named primary and included read-domain
  representations. A fresh materializer derives retained `BoundEnvelope` values, aliases only the
  representations explicitly covered by a shared-instance probe, and preserves absent `included`
  versus present-empty `IncludedExpectation`.
- Attach each leaf to the semantic scenario that owns its direct representations; exact codec/domain/
  compound semantic overlaps reuse existing scenario and representation instances. Byte equality
  alone does not establish semantic ownership: behaviorally distinct envelope leaves own distinct
  named representations and are compared through the explicit cross-owner equality map below.
  Envelope-only leaves own distinct envelope semantic scenarios. The grouping projection may combine
  leaves from different semantic scenarios under one stable legacy id but never transfers ownership
  or duplicates a representation.

### Canonical Envelope Leaf Inventory

The following fixed mapping is the independent migration identity/order source. `leafId` is the
canonical `EnvelopeReadContract.id()` and direct-catalogue key; `legacyScenarioId` is the unchanged
22-group compatibility id; `position` is one-based within that group and also fixes group projection
order. The rows are in direct canonical catalogue order.

| # | leafId | legacyScenarioId | position | kind |
|---:|---|---|---:|---|
| 1 | `envelope.single-resource.case-01` | `binds a single-resource document into a flat DTO envelope` | 1 | document |
| 2 | `envelope.homogeneous-collection.case-01` | `binds a homogeneous resource collection in wire order` | 1 | document |
| 3 | `envelope.heterogeneous-collection.case-01` | `binds a heterogeneous collection through the registry` | 1 | document |
| 4 | `envelope.null-data.case-01` | `preserves explicit null data as NullData` | 1 | document |
| 5 | `envelope.meta-only.case-01` | `preserves absent data on a meta-only document` | 1 | document |
| 6 | `envelope.identifier-pass-through.case-01` | `passes through identifier primary data without DTO binding` | 1 | document |
| 7 | `envelope.identifier-pass-through.case-02` | `passes through identifier primary data without DTO binding` | 2 | document |
| 8 | `envelope.errors-document.case-01` | `preserves errors without binding anything` | 1 | document |
| 9 | `envelope.jsonapi-links-members.case-01` | `preserves jsonapi object, nullable links, and additional members` | 1 | document |
| 10 | `envelope.jsonapi-links-members.case-02` | `preserves jsonapi object, nullable links, and additional members` | 2 | document |
| 11 | `envelope.jsonapi-links-members.case-03` | `preserves jsonapi object, nullable links, and additional members` | 3 | document |
| 12 | `envelope.jsonapi-links-members.case-04` | `preserves jsonapi object, nullable links, and additional members` | 4 | document |
| 13 | `envelope.absent-empty-included.case-01` | `absent included stays null while present-empty included is a non-null empty IncludedResources` | 1 | document |
| 14 | `envelope.absent-empty-included.case-02` | `absent included stays null while present-empty included is a non-null empty IncludedResources` | 2 | document |
| 15 | `envelope.included-wire-order.case-01` | `binds included resources preserving wire order with identity lookup` | 1 | document |
| 16 | `envelope.compound-shared-identity.case-01` | `compound shared identity binds one included DTO reachable from both primary resources` | 1 | document |
| 17 | `envelope.shared-id-lid-instance.case-01` | `shared identity yields one DTO instance reachable from both id and lid keys` | 1 | document |
| 18 | `envelope.duplicate-included-identities.case-01` | `fromDocument fails fast on duplicate included identities` | 1 | document |
| 19 | `envelope.unregistered-primary.case-01` | `unregistered resource-shaped primary fails at the document pointer with null resourceClass` | 1 | document |
| 20 | `envelope.unregistered-primary.case-02` | `unregistered resource-shaped primary fails at the document pointer with null resourceClass` | 2 | document |
| 21 | `envelope.unregistered-included.case-01` | `unregistered included type fails at the included index` | 1 | document |
| 22 | `envelope.duplicate-registry-types.case-01` | `duplicate registry type names fail at build with the later registrant` | 1 | registry |
| 23 | `envelope.registration-rejects-annotations.case-01` | `registration rejects missing, empty, and invalid resource annotations` | 1 | registry |
| 24 | `envelope.registration-rejects-annotations.case-02` | `registration rejects missing, empty, and invalid resource annotations` | 2 | registry |
| 25 | `envelope.registration-rejects-annotations.case-03` | `registration rejects missing, empty, and invalid resource annotations` | 3 | registry |
| 26 | `envelope.binder-failures.case-01` | `binder failures surface with the document pointer joined to the binder path` | 1 | document |
| 27 | `envelope.binder-failures.case-02` | `binder failures surface with the document pointer joined to the binder path` | 2 | document |
| 28 | `envelope.binder-failures.case-03` | `binder failures surface with the document pointer joined to the binder path` | 3 | document |
| 29 | `envelope.root-level-binder-failure.case-01` | `root-level binder failures join to the document pointer without a trailing slash` | 1 | document |
| 30 | `envelope.cyclic-linkage.case-01` | `cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate` | 1 | document |
| 31 | `envelope.independent-envelopes.case-01` | `independent envelopes sharing linkage never inject included DTOs` | 1 | document |
| 32 | `envelope.independent-envelopes.case-02` | `independent envelopes sharing linkage never inject included DTOs` | 2 | document |
| 33 | `envelope.mutation-safe-collections.case-01` | `reader-derived envelope collections are mutation-safe` | 1 | document |
| 34 | `envelope.mutation-safe-collections.case-02` | `reader-derived envelope collections are mutation-safe` | 2 | document |

The fixed baseline must validate that every group position is contiguous, every listed legacy id is
present exactly once in the 22-group inventory, and every leaf id is unique and stable. It is a
test-only characterization source, not canonical runtime storage.

### Leaf Ownership Matrix

Each leaf has one explicit semantic owner and source handle. The owner id is the semantic scenario
that owns the referenced representation instances; repeated owner ids are intentional reuse, and
`alias` marks an intentional named wire alias rather than a duplicate representation instance.

| leafId range | semantic owner id | source handle / alias |
|---|---|---|
| `envelope.single-resource.case-01`, `envelope.absent-empty-included.case-01` | `envelope.single-resource` | `binding:single-resource` |
| `envelope.homogeneous-collection.case-01` | `document.resource.collection` | `codec:resource-collection` |
| `envelope.heterogeneous-collection.case-01` | `envelope.heterogeneous-collection` | `binding:heterogeneous-collection` |
| `envelope.null-data.case-01` | `document.data.null` | `codec:null-data` |
| `envelope.meta-only.case-01` | `document.meta-only` | `codec:meta-only` |
| `envelope.identifier-pass-through.case-01` | `document.identifier.single` | `codec-wire:single-identifier` |
| `envelope.identifier-pass-through.case-02` | `document.identifier.collection` | `codec-wire:identifier-collection` |
| `envelope.errors-document.case-01` | `document.errors` | `codec:errors-document` |
| `envelope.jsonapi-links-members.case-01` | `document.jsonapi.object` | `codec:jsonapi-object` |
| `envelope.jsonapi-links-members.case-02` | `document.links.string-and-object` | `codec:string-and-object-links` |
| `envelope.jsonapi-links-members.case-03` | `document.extension-and-at-members` | `codec:extension-and-at-members` |
| `envelope.jsonapi-links-members.case-04` | `envelope.at-member-document` | `binding:at-member-document` |
| `envelope.absent-empty-included.case-02` | `document.included.empty` | `codec:empty-included` |
| `envelope.included-wire-order.case-01` | `document.compound` | `codec:compound-document` |
| `envelope.compound-shared-identity.case-01` | `article.compound.shared-author` | `codec:compound-shared-identity` |
| `envelope.shared-id-lid-instance.case-01` | `envelope.shared-identity.id-lid` | `core:shared-identity-id-and-lid` |
| `envelope.duplicate-included-identities.case-01` | `envelope.duplicate-included-identities` | `core:duplicate-included-identities` |
| `envelope.unregistered-primary.case-01`, `envelope.unregistered-primary.case-02` | `envelope.unregistered-primary` | `binding:unregistered-primary-*` |
| `envelope.unregistered-included.case-01` | `document.compound` | `codec:compound-document` |
| `envelope.duplicate-registry-types.case-01` | `envelope.registry.duplicate-types` | `registry-attempt` |
| `envelope.registration-rejects-annotations.case-01`, `case-02`, `case-03` | `envelope.registry.annotations` | `registry-attempt` |
| `envelope.binder-failures.case-01`, `case-02`, `case-03` | `envelope.binder.failures` | `binding:binder-failure-*` |
| `envelope.root-level-binder-failure.case-01` | `envelope.binder.root-failure` | `binding:root-level-failure` |
| `envelope.cyclic-linkage.case-01` | `envelope.cyclic-linkage` | `binding:cyclic-linkage` |
| `envelope.independent-envelopes.case-01`, `case-02` | `envelope.independent-envelopes` | `core:independent-envelopes-*` |
| `envelope.mutation-safe-collections.case-01` | `envelope.mutation.compound` | `wire-alias:compound-document` |
| `envelope.mutation-safe-collections.case-02` | `envelope.mutation.errors` | `wire-alias:errors-document` |

The matrix expands range rows by the fixed leaf table above; implementation tests must compare every
leaf individually, validate owner/reference membership at freeze time, and permit only the aliases
named here.

Owner ids in this matrix are stable `SemanticOwnerHandle` references resolved from the prerequisite
registry handoffs, not implementation-chosen strings. `article.compound.shared-author` is the pinned
Foundation/Compound anchor; every other handle must be published by its owning prerequisite
migration or explicitly created as an envelope-only owner after the handoff proves no exact owner
exists. Convergence must reject an unknown, colliding, or representation-incompatible handle.

For every expanded leaf row, the durable baseline also records `primaryReadRepresentation`,
`includedReadRepresentation` (or `NONE`), `freshness` (`FRESH_PER_MATERIALIZATION` or an explicitly
named `SHARED_INSTANCE_ALIAS`), and the owner handle for each representation. Primary and included
read-domain values are separate named representations by default; only the shared-instance probe
leaves may use the explicitly named alias, and independent-envelope/mutation leaves must not reuse
that alias accidentally.

### Cross-owner Wire Equality Map

These pairs retain separate named wire representations under their matrix-selected semantic owners;
the equality assertions compare persisted bytes only and do not create cross-owner representation
aliases or weaken the owner index.

| Codec source | Envelope source | Equality |
|---|---|---|
| `documents/compound-document.json` | `envelope-binding/independent-envelopes-matching.json` | exact byte equality |
| `documents/single-identifier.json` | `envelope-binding/unregistered-primary-single.json` | exact byte equality |
| `documents/identifier-collection.json` | `envelope-binding/unregistered-primary-collection.json` | exact byte equality |

## Deliverables

- Register all 34 executable envelope leaves exactly once as canonical `EnvelopeReadContract` entries
  and derive the exact 22 stable legacy scenario groups in their existing order. Attach leaves to
  existing codec/domain/compound semantic scenarios where they consume the same wire/core/domain
  representations, and create distinct scenarios for envelope-only registry, failure, cyclic,
  mutation, id/lid, and binding documents. Add those leaves during the Foundation registry's single
  pre-freeze family assembly after prerequisite declarations and before owner-index/catalogue freeze;
  no post-freeze envelope registry or extension path is permitted.
- Extend the test-only migration baseline with the complete 34-row mapping above, including exact
  leaf id, legacy group id, group position, variant kind, source reference, reader context, target
  registration, expectation, and registry diagnostic. Keep the existing 22-group inventory as a
  separate compatibility baseline.
- Represent each byte-identical codec/envelope path pair as two stable named wire sources on their
  respective matrix-selected semantic owners and assert the explicit cross-owner byte equality map.
  Do not remove either path. Preserve all 14 `envelope-binding/` files, validation-invalid markers,
  and the exact 22 legacy scenario ids/order.
- Convert `EnvelopeReadScenarios`, `JsonApiFixtures.envelopeRead()`, and existing envelope scenario
  values into an immutable projection from
  `JsonApiFixtures.envelopeReadContracts()`, the direct typed `FixtureCatalog` accessor derived by the
  foundation registry. Do not add class-token dispatch or a wrapper catalogue. Preserve all current
  input forms, order, target types, contexts, outcomes, and lookup diagnostics while removing
  independent scenario/core/wire construction from the compatibility layer. Preserve the signatures
  and behavior of `EnvelopeReadScenarios.catalog()`, `all()`, `byId(String)`, and `where(Predicate)`,
  `JsonApiFixtures.envelopeRead()`, public constructors of the retained envelope DTO/variant
  records, and public `EnvelopeReadInput`, `EnvelopeReadExpectation`, and `IncludedExpectation`
  factories. These standalone compatibility values remain usable without registering or owning
  canonical fixture state.
- Retain every current public envelope construction surface and validation behavior: constructors for
  `EnvelopeReadScenario`, `EnvelopeReadCase`, `EnvelopeReadVariant.DocumentBinding`,
  `EnvelopeReadVariant.Registry`, `EnvelopeReadVariant.RegistryAttempt`, all public input and
  expectation records, `IncludedExpectation` and `IdentityProbe`, plus their static factories and
  `EnvelopeBindingDocument` enum methods. Direct construction remains standalone, noncanonical, and
  unable to mutate or register frozen state.
- Migrate Jackson 3 `DomainDocumentReaderSpec` shared parameterization to the canonical projection,
  direct representation access, typed variant dispatch, and exact full-projection coverage. Keep
  adapter execution mechanics and adapter-only cases local.
- Add one catalogue-wide convergence spec that inventories every expected contract id for codec
  read/write/schema, domain read/write, compound, sparse-fieldset, and envelope namespaces from the
  canonical catalogue and proves: semantic-id uniqueness; namespace-local case-id uniqueness;
  representation ownership; family-specific projection laws; no legacy view drops/adds/duplicates a
  case or leaf within its declared law; and no compatibility `*Scenarios` class contains independent
  canonical storage. Direct one-contract families use order-preserving bijections, envelope uses its
  ordered 34-leaf-to-22-group projection, and codec uses a manifest-ordered stable-id keyed join
  proving each applicable read/write/schema contract exactly once. Its expected-id inputs are
  independent: persisted manifest rows plus the pre-migration capability-applicability baseline for
  codec, fixed migration baselines from the Domain,
  Compound, and Sparse plans for those families, and the fixed 34-leaf envelope baseline plus the
  22-group inventory for envelope; the canonical catalogue under test is never its own expected-id
  source.
- Add a fixed test-only codec capability-applicability baseline recording each stable codec id's
  read/write/schema membership before canonical migration. For positive manifest IDs, record the
  existing `CodecScenario` boolean/`schemaKind` selections in manifest order; record every ambiguous
  manifest ID as read-only; and record every negative-manifest ID as read-only. Keep positive,
  ambiguous, and negative IDs in their existing namespace-local inventories, join by stable ID plus
  namespace, and reject missing, extra, duplicate, or conflicting membership. Manifests remain the
  persisted owners of IDs, paths, notes, and order; the capability baseline is declaration
  characterization only and is not a second runtime catalogue.
- Require durable, non-runtime baseline handoffs before deleting prerequisite plans. The exact files
  are `jsonapi-java-test-fixtures/src/test/resources/semantic-catalog-baselines/codec-capabilities.tsv`
  (Foundation), `domain-write.tsv` and `domain-read.tsv` (Domain), `compound-write.tsv` (Compound),
  and `sparse-fieldset.tsv` (Sparse); Convergence adds `envelope-groups.tsv` and
  `envelope-leaves.tsv`. Each file is a fixed characterization oracle, preserves independent
  IDs/order/values required by its owning migration, and is consumed by the convergence spec without
  entering runtime catalogue registration.
- Use `module-docs` to finalize `jsonapi-java-test-fixtures/README.md`, affected package docs, and
  `JsonApiFixtures` Javadoc as the canonical contributor/adapter guide. Link the unified-catalogue
  ADR for rationale, the wire README for corpus storage, package Javadocs for contract invariants,
  and adapter module docs for executor mechanics. Remove superseded operation-catalog ownership or
  declaration-layout guidance rather than duplicating it. Apply ADR-009 to every added semantic
  envelope type and retained compatibility projection: added production packages are `@NullMarked`,
  nullable public type uses carry accurate `@Nullable` metadata, and explicit JSON:API null/absence
  remains represented by typed wire states where applicable.
- Keep implementation ordering explicit: Convergence implementation is blocked until the actual
  Foundation, Domain, Compound, and Sparse implementations and accepted/indexed ADR-013 are
  materialized. This planning contract may be reviewed against the accepted prerequisite plans
  before those implementation outputs exist.
- Extend the single registry freeze validation with a private identity-keyed representation-owner
  index (or equivalent identity check) covering wire, core, and domain representations. Reject
  accidental duplicate instances across semantic scenarios while allowing intentional named wire
  aliases, and validate every contract reference against the frozen owner index.

## Non-goals

- PATCH implementation, Jackson 2/Gson modules, removing stable compatibility APIs, deleting
  byte-identical fixture paths, graph hydration, relationship injection, or changing envelope
  production semantics.
- Treating contract ids as globally unique, renaming existing ids, forcing one read/write domain
  representation, or flattening envelope identity/registry/mutation behavior.
- Adapter capability conditionals such as different expected semantic outcomes by Jackson major.

## Final source-of-truth rule

- After this unit, `JsonApiFixtures.scenarios()` and its explicit semantic declarations are the sole
  source of shared scenario/representation/contract truth. All existing facade accessors and
  `*Scenarios` classes are projections and must be tested/documented as such.
- Source files may remain family-oriented, manifest-backed, helper-based, or per-case where that
  improves readability. The architectural invariant is one registration/ownership path, not visual
  declaration uniformity.
- Future adapters execute the same typed contract projections. They may use compatibility views
  while live plans still name them, but may not fork expected values, ids, diagnostics, or corpora.
  Adapter-specific API/source-location/harness limitations stay explicit in adapter tests and never
  redefine normal shared semantics.
- Future PATCH registers `PatchContract` values and any new named representations directly with the
  semantic catalogue. It must not create an independent canonical `PatchScenarios` list; no legacy
  patch view is needed because no such API has shipped.

## Test strategy

- Rewrite envelope catalogue specs as canonical-leaf and ordered-group projection/invariant tests,
  preserving the exact 22-id closed inventory, 34 executable leaves, and all target/input/core/wire
  validation.
- Preserve the existing fixed 22-entry inventory characterization and add a separate pre-migration
  fixed 34-leaf baseline keyed by stable leaf id/group position, recording variant, input source,
  reader context, target registration, expectation, and registry diagnostics. Add source-compatibility
  tests for the envelope catalogue/facade shims, public input/expectation factories, and standalone
  DTO/variant construction without canonical registration.
- Add direct semantic-reference tests for null/meta/error/empty-included codec documents, compound
  and shared identity, identifier pass-through/failure, and all byte-identical path aliases. Prove
  absent/null/present-empty and traversal/binding/flat-read distinctions remain separate.
- Verify default-context codec leaves use `CodecWireSourceReference` with the exact legacy codec id
  and canonical wire representation, while `CODEC_DERIVED` leaves use only
  `CodecReadOutcomeReference`; verify no source reference supplies implicit context and no reverse
  path lookup is used.
- Parameterize every canonical envelope leaf through Jackson 3 without id dispatch; assert full
  envelope values, codec-outcome context/provenance, registration failures, joined paths,
  identity/index behavior, shared instances, mutation safety, ordered grouping, and full leaf
  projection coverage.
- Test grouping guards and materialization: shared variant kind, target-class order, entry point,
  contiguous group positions, registry-only attempts, valid zero-target bindings, fresh primary and
  included representations, explicit shared-instance aliasing only where expected, and absent versus
  present-empty `included`.
- Verify all 34 leaf-to-owner matrix rows, source handles, intentional aliases, representation
  identity ownership, and pre-freeze membership independently of the canonical catalogue's own ids.
- Run the catalogue-wide convergence spec over every contract root and every retained compatibility
  view. Mechanically search production fixture sources/tests to ensure old `SCENARIOS` data lists or
  loaders are not alternate owners; use persisted manifests, the pre-migration codec capability
  baseline, and prerequisite migration baselines as expected-id inputs rather than deriving expected
  ids from the canonical catalogue under test.
  Static projection caches are allowed only when built from the canonical catalogue.
- Preserve ArchUnit's test-fixtures allowlist and add dependency coverage for any new semantic
  package; no Jackson-major import may enter the shared catalogue. Verify ADR-009 package and
  nullable-use annotations for added envelope contracts and compatibility projections, including
  absent envelope members and explicit JSON null states.

## Acceptance criteria

- [ ] All 34 executable envelope leaves expose unique stable `EnvelopeReadContract.id()` leaf ids,
      and their 22 stable legacy scenario groups preserve separate ids, order, variants, cases,
      contexts, registrations, expectations, values, notes, paths, and lookup diagnostics exactly
      once under the declared leaf/group projection law.
- [ ] Shared codec/domain/compound representations are referenced directly where exact; envelope-
       only behavior remains distinct, and the three byte-identical codec/envelope pairs in the
       explicit cross-owner equality map remain separate named wire representations with exact-byte
       assertions rather than same-scenario ownership aliases.
- [ ] Every codec-derived envelope leaf retains an exact typed codec-read outcome reference carrying
       stable codec id, complete validation context, effective non-null primary-data kind, and closed
       projection provenance; nullable raw codec metadata remains a separate compatibility value; legacy input
      and `CODEC_DERIVED` reader-context values derive from that reference without path/ID searches or
      duplicated context metadata, while `IDENTIFIER_DEFAULTS`, `RESOURCE_DEFAULTS`, and the
       no-primary-data fallback remain explicit and unchanged.
- [ ] Default-context codec leaves use a separate `CodecWireSourceReference` carrying the exact
       canonical wire representation and legacy codec id without supplying reader context or outcome
       metadata; `CODEC_DERIVED` leaves reject that source-only form and use the exact outcome
       reference instead.
- [ ] The 22-entry envelope inventory and separate 34-leaf inventory have independent fixed
      characterization baselines, and `EnvelopeReadScenarios.catalog()`, `all()`, `byId(String)`,
      `where(Predicate)`,
      `JsonApiFixtures.envelopeRead()`, public input/expectation factories, and retained DTO/variant
       constructors remain source-compatible, lossless, and noncanonical when directly constructed.
- [ ] Durable baseline handoffs exist at the named `semantic-catalog-baselines` test-resource paths
       before prerequisite plans are deleted; Convergence consumes those files as independent oracles
       and they do not enter runtime catalogue registration.
- [ ] The fixed codec capability-applicability baseline independently preserves each stable codec id's
       read/write/schema membership from the pre-migration `CodecScenario`,
       `AmbiguousPrimaryDataScenarios`, and `NegativeCodecScenarios` surfaces: positive IDs use their
       existing `writable`/`readable`/`schemaKind != null` values, ambiguous IDs are read-only, and
       negative IDs are read-only. The convergence oracle joins this baseline to manifest IDs/order by
       namespace and rejects missing, extra, duplicate, or conflicting membership without deriving
       expected capability sets from the canonical catalogue.
- [ ] Every current public envelope constructor/factory and validation surface, including
       `EnvelopeReadScenario`, `EnvelopeReadCase`, `DocumentBinding`, `Registry`, `RegistryAttempt`,
       all input/expectation records, `IncludedExpectation`, `IdentityProbe`, and
       `EnvelopeBindingDocument` methods, remains standalone and noncanonical.
- [ ] Envelope entry-point, identity, registry, joined-diagnostic, mutation-safety, validation, and
      absent/null/empty distinctions retain typed constructor and behavioral proofs; all leaves in a
      group share valid variant metadata, registry groups contain only attempts, zero-target bindings
      remain valid, and expectation materialization preserves explicit shared-instance and
      absent-versus-present-empty semantics.
- [ ] Envelope declarations enter the Foundation-owned pre-freeze registry assembly and share its
       immutable contract and representation owner indexes/catalogues; no parallel envelope registry
       or post-freeze mutation path exists. Named wire aliases are the only permitted duplicate
       representation references.
- [ ] Every leaf matches the explicit semantic-owner/source matrix, and the freeze-time identity
       owner check rejects unlisted duplicate representation instances while allowing only the named
       intentional wire aliases.
- [ ] Each leaf has explicit primary/included read-domain representation handles, freshness mode, and
       allowed shared-instance alias; primary and included binding remain independent except for the
       named shared-instance probes.
- [ ] Binding and core source references preserve the exact `EnvelopeBindingDocument` identity and
       named wire/core pairing; locally constructible aggregate-invalid core documents remain
       registerable and reach adapter validation with their expected diagnostic, pointer, and invalid
       marker.
- [ ] Aggregate-invalid core representations are accepted only for `FROM_DOCUMENT` negative leaves;
       codec outcomes and ordinary wire-read leaves cannot select them.
- [ ] Every legacy accessor and `*Scenarios` class across all migrated families follows its declared
      projection law with no independent canonical storage: direct families are order-preserving
      bijections, envelope is the lossless ordered 34-leaf-to-22-group projection, and codec is a
      manifest-ordered stable-id keyed join covering each applicable read/write/schema contract once.
- [ ] Jackson 3 envelope execution uses canonical typed contracts with full projection coverage and
      no id dispatch or redefined shared expectations.
- [ ] The `module-docs` checklist passes; README/package/Javadoc/ADR links assign one canonical owner
       to architecture, contributor flow, corpus storage, package invariants, and adapter mechanics.
- [ ] ADR-009 nullness obligations pass for every added envelope contract package and retained
       compatibility projection: packages are `@NullMarked`, nullable public type uses are accurately
       `@Nullable`, and applicable explicit JSON:API null states remain typed wire variants rather
       than bare Java null.
- [ ] Before Convergence implementation and final verification, the actual Foundation, Domain,
       Compound, and Sparse implementation outputs and accepted/indexed ADR-013 exist; final
       convergence checks inspect those materialized repository artifacts and do not treat accepted
       plan prose as implementation proof.
- [ ] Documentation states the future PATCH extension rule: a later PATCH plan adds `PatchContract`
      directly to `scenarios()` with no independent `PatchScenarios` source; this criterion does not
      add or implement `PatchContract` in Convergence, and future adapters are required to reuse
      shared projections.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` and
       `./gradlew :jsonapi-java-jackson3:test --tests 'io.github.kazemek.jsonapi.jackson3.DomainDocumentReaderSpec'`
       pass.
- [ ] The `spotless-format` skill runs `./gradlew spotlessApply` then
      `./gradlew spotlessCheck`, followed by `./gradlew clean build`.
- [ ] The `sonar-quality-gate` skill's Quality Gate wait and authenticated Issues API check both
      exit 0 with zero unresolved new-code issues.
