# Phase 2.10 — Jackson 3 Typed Domain Envelope

> **Module:** `jsonapi-java-jackson3`
> **Dependencies:** Phase 2.9
> **Status:** Not started

## Goal

Expose a validated JSON:API document as an immutable domain-facing envelope with flat primary
DTOs and independently bound included DTOs, without requiring `JsonApiDocument` in routine reader
signatures.

## Research and constraints

- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — decode/validate the document first;
  routine Jackson/Spring signatures use a typed envelope; relationship properties stay linkage-only;
  `included` binds only through an explicit type registry and is never injected into relationships.
- [ADR-006](../../docs/adr/006-read-boundary.md) — Phase 2.4 (`JsonApiDocumentReader`) remains the
  sole JSON→document path; this milestone composes it, then binds.
- [ADR-002](../../docs/adr/002-document-representation.md) — preserve absent vs `data: null` vs
  single vs collection primary data; do not collapse present-empty `included: []` with absent
  `included`.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — inclusion and linkage are separate;
  the read envelope likewise exposes included DTOs without relationship graph wiring.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — attribute/relationship DTO construction
  stays on Phase 2.9 / Jackson logical properties; do not invent a second bean model for resources.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — public packages stay `@NullMarked`; only
  absence-nullable envelope members use `@Nullable`.
- Write-side `DocumentEnvelope` already carries mapper links/meta/jsonapi into
  `JsonApiResourceMapper#toDocument`. The read envelope must use a distinct type name
  (`JsonApiDomainDocument`) so the two contracts cannot be confused.
- Phase 2.9 — reuse `JsonApiResourceBinder` / `DomainResourceBinder` for every resource→DTO
  conversion (primary and included). Do not reimplement synthetic-map binding. Heterogeneous
  collections and `included` are in scope here; homogeneous `fromResources` stays on the binder.
- Phase 2.9 non-goal — `ResourceIdentifier` primary data is never treated as a resource DTO; the
  envelope preserves identifier primary-data states as core `ResourceIdentifier` values.
- JSON:API permits heterogeneous primary and included collections; Java targets come only from the
  registry, never from object-shape guessing.

## Deliverables

- Add public immutable types in `io.github.kazemek.jsonapi.jackson3`:
  - `JsonApiDomainDocument` — `@Nullable DomainData data`, `@Nullable List<ErrorObject> errors`,
    `@Nullable Meta meta`, `@Nullable JsonApiObject jsonapi`, `@Nullable Links links`,
    `@Nullable IncludedResources included`, `Map<String, @Nullable Object> additionalMembers`
    (same absence/`null` member rules as `JsonApiDocument`; no `JsonApiDocument` /
    `ResourceObject` / `DocumentData` fields on the public envelope). Constructors defensively
    copy `errors` and `additionalMembers` (`List.copyOf` / ordered map copy); accessors return
    unmodifiable views so mutating a caller-supplied source or a returned collection cannot change
    the envelope.
  - sealed `DomainData` permitting `NullData`, `SingleResource(Object resource)`,
    `ResourceCollection(List<Object> resources)`, `SingleIdentifier(ResourceIdentifier)`,
    `IdentifierCollection(List<ResourceIdentifier>)`. Resource DTO payloads are `Object` because
    primary/included collections may be heterogeneous; callers cast using their registrations.
    `ResourceCollection` and `IdentifierCollection` defensively copy their lists and expose
    unmodifiable accessors.
  - `IncludedResources` — immutable wire-order `List<Object> resources()` plus identity lookup
    `Optional<Object> find(ResourceIdentity)`; when a bound resource has both `id` and `lid`,
    index under both `ResourceIdentity.ofId` and `ofLid` keys to the same DTO instance.
    Defensively copy the wire-order list and identity index at construction; mutating a returned
    `resources()` list or a construction-time source must not change wire order or `find` results.
  - `ResourceTypeRegistry` built via `ResourceTypeRegistry.builder().register(Class|JavaType)...`
    `.build()`. Registration only records target `Class`/`JavaType` and keys by reading
    `@JsonApiResource.type()` from the raw class (annotation lookup only — no
    `MappingDefinitionCache` / BeanDescription introspection and no `JsonMapper` on the registry).
    Missing `@JsonApiResource` or empty/invalid type names fail at `register` with the existing
    `MISSING_RESOURCE_ANNOTATION` / `INVALID_RESOURCE_TYPE` codes on `JsonApiMappingException`.
    Duplicate JSON:API type names fail at `build()` with `JsonApiMappingException`
    (`CONFLICTING_TYPE_REGISTRATION`, `propertyPath` = the conflicting JSON:API type name string
    such as `articles`, `resourceClass` = the later registrant's raw class). Full Phase 2.2
    `ResourceMapping` resolution stays on the Phase 2.9 binder/`MappingDefinitionCache` at bind
    time using the domain reader's `rebuild()`-derived mapper. Empty registry is legal
    (identifier/error/meta-only documents, or resource documents that fail at bind time if
    resource types appear).
- Add `JsonApiDomainDocumentReader` and factory methods
  `JsonApiJackson3.domainDocumentReader(JsonMapper|Builder, DocumentReadContext, ResourceTypeRegistry)`
  / `domainDocumentReader(..., IdentifierConverter)` /
  `domainDocumentReader(..., IdentifierConverter, Map<Class<?>, RelationshipLinkageMapper>)`,
  deriving the binder mapper via `rebuild()` exactly as `resourceBinder` does. Entry points:
  `readValue(String|byte[]|InputStream|JsonParser)` (same close/ownership rules as
  `JsonApiDocumentReader`) and `fromDocument(JsonApiDocument)` (bind only; do not re-parse or
  re-validate). Codec/validation failures from `readValue` remain `JsonApiDocumentReadException`
  with existing category/path/location/rule codes.
- Bind primary resource data and every present `included` element through the Phase 2.9 binder
  after looking up `ResourceObject.type()` in the registry. Unregistered resource types throw
  `JsonApiMappingException` with `UNREGISTERED_RESOURCE_TYPE`, document pointer
  `propertyPath` (`/data`, `/data/<index>`, `/included/<index>`), and `resourceClass` null before
  any envelope escapes. Identifier primary data and error documents never attempt DTO binding.
  Present-empty `included: []` yields a non-null empty `IncludedResources`; absent `included`
  stays `@Nullable` null. When rethrowing binder `JsonApiMappingException`s, set `propertyPath` to
  document pointer + binder path with a single joining `/` (binder paths already start with `/`):
  e.g. `/included/1` + `/title` → `/included/1/title`, `/data` + `/type` → `/data/type`,
  `/data/0` + `/relationships/author/data` → `/data/0/relationships/author/data`.
- Add `MappingDiagnostic` values `UNREGISTERED_RESOURCE_TYPE` and
  `CONFLICTING_TYPE_REGISTRATION` (both carried only by `JsonApiMappingException`). Expose document
  members as the already-decoded core `Links` / `Meta` / `JsonApiObject` / `ErrorObject` /
  additional-member map values (no reflective serialization of those core records). Add
  `@Nullable metaAs(Class|JavaType)` on `JsonApiDomainDocument` that `convertValue`s
  `Meta.members()` using the same domain-reader `rebuild()`-derived binder mapper that bound the
  document (retained via package-private envelope construction — not a public document component
  and not a fresh default mapper). Both overloads return `@Nullable`; absent `meta` → return null;
  present meta conversion failure → `JsonApiMappingException` + `UNSUPPORTED_ATTRIBUTE_VALUE` at
  `/meta`. Refresh module docs/Javadoc via `module-docs` and mark typed domain envelopes plus
  independent included binding **supported** in `docs/conformance.md` without claiming graph
  hydration or PATCH commands.

## Non-goals

- Resolving relationship fields from `included`, graph assembly, persistence lookup, identity-map
  mutation, or cycle policy beyond what aggregate validation already enforced.
- Exposing unbound resource-level links/meta/additional members through the envelope (callers that
  need them keep using `JsonApiDocumentReader`).
- Applying extension/profile semantics beyond preserving their document additional members.
- Presence-aware PATCH commands; Phase 2.11 owns update binding (and may compose this reader).
- Replacing or deprecating `JsonApiDocumentReader` / `JsonApiResourceBinder` for advanced callers.
- Changing write-side `DocumentEnvelope` or inventing a second resource binder.
- Jackson 2 support; Phase 2.16 ports this contract.

## Implementation boundaries

- Public types in `io.github.kazemek.jsonapi.jackson3`; implementation in
  `io.github.kazemek.jsonapi.jackson3.internal`. Production code imports no `core.internal` and no
  sibling module internals.
- Resource type selects the DTO target only through the supplied registry. Registration does not
  accept a caller type-name override; `@JsonApiResource.type()` on the registered raw class is the
  sole type key. Binder `RESOURCE_TYPE_MISMATCH` still applies if a resource's wire `type` disagrees
  with the mapping resolved at bind time (defense in depth after registry lookup).
- Error documents (`errors` present, `data` absent) never bind primary DTOs; `included` cannot be
  present (core local rule). Meta-only and extension-only documents remain representable with
  `data == null`.
- `PrimaryDataKind` on `DocumentReadContext` continues to disambiguate resource vs identifier wire
  forms for Phase 2.4; the domain reader does not guess cardinality or kind from the Java registry.
- Relationship properties on primary and included DTOs remain linkage-only under Phase 2.9 rules;
  custom `RelationshipLinkageMapper` registrations on the domain reader apply to both.
- `metaAs` must not construct a new default `JsonMapper`; it reuses the domain reader's derived
  binder mapper retained at envelope construction.
- Envelope collection members match core document mutability: defensive copies at construction and
  unmodifiable accessors for `errors`, `additionalMembers`, `DomainData` collection variants, and
  `IncludedResources` (including the id/lid index).
- Public nullness follows ADR-009.

## Test strategy

- Spec class: `DomainDocumentReaderSpec` (plus focused companion specs only if the primary file
  would become unreadable). Reuse Phase 2.9 flat testmodel types (`FlatArticle`, people/comments
  identifier shapes, etc.) and add registry/heterogeneous fixtures under
  `src/test/java/.../testmodel/` only when existing types are insufficient.
- Positive: single-resource, homogeneous collection, heterogeneous collection (two registered
  types), `data: null`, absent data (meta-only / errors), identifier primary data (pass-through),
  compound fixtures (`compound-document`, `compound-shared-identity`, `extension-and-at-members`),
  nullable links, additional/`@` members preserved. Assert absent `included` (no member) →
  `included == null` and `empty-included` → non-null empty `IncludedResources` separately.
  `metaAs`: both overloads return null when meta is absent; successful conversion uses a
  caller-mapper module or feature that a fresh default mapper cannot apply, exercised on envelopes
  from both `readValue` and `fromDocument`. Immutability: mutate construction-time source lists
  and returned `resources()` / `errors` / `additionalMembers` collections and assert wire order and
  id/lid `find` results are unchanged.
- Included: wire order preserved; `find` by id and by lid when both present; shared identity
  yields one DTO instance reachable from both keys; cyclic/shared linkage fixtures prove
  relationship fields remain `ResourceIdentifier` (or registered linkage-mapper values) while
  included DTOs are independently listed/indexed.
- Negative: unregistered primary type → `JsonApiMappingException` /
  `UNREGISTERED_RESOURCE_TYPE` at `/data` or `/data/n` (`resourceClass` null); unregistered
  included type → same at `/included/n`; duplicate registry type → `JsonApiMappingException` /
  `CONFLICTING_TYPE_REGISTRATION` at `build()` with `propertyPath` equal to the conflicting type
  name and `resourceClass` the later registrant; Phase 2.9 binder failures surface as
  `JsonApiMappingException` with joined document+binder paths covering `/data/type`,
  `/data/0/type`, `/included/1/title`, and `/data/0/relationships/author/data` (single joining
  `/`); incompatible `metaAs` target → `JsonApiMappingException` with
  `UNSUPPORTED_ATTRIBUTE_VALUE` at `/meta` (not `JsonApiDocumentReadException`); codec/validation
  failures from `readValue` remain `JsonApiDocumentReadException` (assert category unchanged vs
  Phase 2.4).
- Ownership: on success and failure, caller-owned `InputStream` and `JsonParser` remain open;
  `String` / `byte[]` / `InputStream` convenience overloads close only the `JsonParser` instances
  they create (no reader-created streams).
- Isolation: mutate or swap `included` in a fixture after constructing a binder-only baseline and
  assert domain-envelope relationship fields still match linkage-only binding (no injection).

## Acceptance criteria

- [ ] `readValue` / `fromDocument` preserve absent, `NullData`, single-resource, resource-collection,
      single-identifier, and identifier-collection primary states plus document-level
      links/meta/jsonapi/errors/additional members without requiring `JsonApiDocument` in routine
      `readValue` signatures; identifier primary data is never DTO-bound; absent `included` stays
      null while present-empty `included: []` is a non-null empty `IncludedResources`; envelope
      collections and the id/lid index are mutation-safe; `@Nullable metaAs(Class|JavaType)` uses
      the reader's derived binder mapper on both entry paths (absent meta → null for both
      overloads; conversion failure → `JsonApiMappingException` /
      `UNSUPPORTED_ATTRIBUTE_VALUE` at `/meta`).
- [ ] Explicit `ResourceTypeRegistry` registration deterministically binds heterogeneous
      primary/included resources, preserves included wire order and dual id/lid identity lookup,
      and throws `JsonApiMappingException` for `UNREGISTERED_RESOURCE_TYPE` (document-pointer
      `propertyPath`, null `resourceClass`) and `CONFLICTING_TYPE_REGISTRATION` (`propertyPath` =
      conflicting type name, `resourceClass` = later registrant) before a partial envelope escapes.
- [ ] Included resources are never injected into primary or included DTO relationship properties,
      including cyclic and shared-identity fixtures; binding reuses Phase 2.9 binder contracts.
- [ ] Public envelope APIs satisfy ADR-009 nullness; the canonical `module-docs` checklist passes;
      `docs/conformance.md` marks typed domain envelopes and independent included binding
      **supported** without claiming graph hydration or PATCH commands.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*DomainDocumentReaderSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
