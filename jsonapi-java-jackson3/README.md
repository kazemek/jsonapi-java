# jsonapi-java-jackson3

Jackson 3 codecs for validating, writing, and reading [JSON:API v1.1](https://jsonapi.org/) documents,
and for mapping annotated domain types to resource objects.

## Packages

| Package                                        | Role                                                                  |
|------------------------------------------------|-----------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.jackson3`           | Public writer/reader/mapper/binder/PATCH factories and validate-then-codec entry points |
| `io.github.kazemek.jsonapi.jackson3.internal`  | Streaming serializers/decoders, mapping engine, module registration; not public API |
| `io.github.kazemek.jsonapi.jackson`            | Jackson-major-neutral policy/context/diagnostic/envelope/PATCH command contracts (in `jsonapi-java-jackson-common`) |

Codec and mapping policy, contexts, diagnostics, domain envelope values, and presence-aware update
commands (`DocumentReadContext`, `CompoundSerializationContext`, `IncludePath`, `IncludePolicy`,
`FieldPolicy`, `MappedDocument`, `IdentifierConverter`, `DomainData`, `IncludedResources`,
`PatchCommand`, `PatchChange`, and the failure types) live in the Jackson-major-neutral package
`io.github.kazemek.jsonapi.jackson` and are imported from `jsonapi-java-jackson-common`; this module
holds only Jackson 3-bound factories, readers, writers, and binders.

## Minimal usage

Document codec:

```java
JsonMapper callerMapper = JsonMapper.builder().build();

JsonApiDocumentWriter writer = JsonApiJackson3.writer(callerMapper);
String json = writer.writeValueAsString(document);

JsonApiDocumentReader reader =
    JsonApiJackson3.reader(callerMapper, DocumentReadContext.resourceDefaults());
JsonApiDocument roundTrip = reader.readValue(json);
```

Domain-to-resource mapping (map → write):

```java
JsonMapper callerMapper = JsonMapper.builder().build();
JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(callerMapper);

JsonApiDocument doc = mapper.toDocument(someAnnotatedPojo);
String json = JsonApiJackson3.writer(callerMapper).writeValueAsString(doc);
```

Flat resource-to-DTO binding (validated document model → DTO; bind after `JsonApiDocumentReader`):

```java
JsonApiResourceBinder binder = JsonApiJackson3.resourceBinder(callerMapper);

JsonApiDocument document = JsonApiJackson3.reader(callerMapper, DocumentReadContext.resourceDefaults())
    .readValue(json);
ResourceObject resource = ((DocumentData.SingleResource) document.data()).resource();

FlatArticleDto dto = binder.fromResource(resource, FlatArticleDto.class);

JsonApiDocument collectionDocument = JsonApiJackson3.reader(callerMapper, DocumentReadContext.resourceDefaults())
    .readValue(collectionJson);
List<ResourceObject> resources = ((DocumentData.ResourceCollection) collectionDocument.data()).resources();

List<FlatArticleDto> dtos = binder.fromResources(resources, FlatArticleDto.class);
```

Compound inclusion (explicit context only; relationship mapping alone never includes):

```java
CompoundSerializationContext context =
    CompoundSerializationContext.defaults()
        .withIncludePaths(List.of(IncludePath.of("comments.author")))
        .withIncludePolicy(IncludePolicy.allowAll());

JsonApiDocument compound = mapper.toDocument(article, null, context);
```

Sparse fieldsets (same context; only via `MappedDocument` overloads):

```java
CompoundSerializationContext fieldsets =
    CompoundSerializationContext.defaults()
        .withIncludePaths(List.of(IncludePath.of("author")))
        .withIncludePolicy(IncludePolicy.allowAll())
        .withFieldsets(Map.of("articles", List.of("title")))
        .withFieldPolicy(FieldPolicy.allowAll());

MappedDocument mapped = mapper.toMappedDocument(article, null, fieldsets);
String json =
    JsonApiJackson3.writer(callerMapper, mapped.applyTo(ValidationContext.defaults()))
        .writeValueAsString(mapped.document());
```

Bare resource (inspect or compose a document yourself; not a top-level wire payload):

```java
ResourceObject resource = mapper.toResource(someAnnotatedPojo);
```

Collection primary data (also a `JsonApiDocument`; feed it to the same writer):

```java
JsonApiDocument collDoc = mapper.toResourceCollection(allPojos);
```

Typed domain envelope (validated JSON:API JSON → flat DTOs; no `JsonApiDocument` in routine
signatures):

```java
ResourceTypeRegistry registry =
    ResourceTypeRegistry.builder()
        .register(FlatArticleDto.class)
        .register(AuthorDto.class)
        .build();

JsonApiDomainDocumentReader domainReader =
    JsonApiJackson3.domainDocumentReader(
        callerMapper, DocumentReadContext.resourceDefaults(), registry);

JsonApiDomainDocument envelope = domainReader.readValue(json);
FlatArticleDto article = (FlatArticleDto) ((DomainData.SingleResource) envelope.data()).resource();
Optional<Object> includedAuthor =
    envelope.included() == null
        ? Optional.empty()
        : envelope.included().find(ResourceIdentity.ofId("people", "9"));
```

Presence-aware PATCH (validated update document → immutable command of supplied changes only):

```java
JsonApiPatchReader patchReader = JsonApiJackson3.patchReader(callerMapper);

PatchCommand<FlatArticleDto> command =
    patchReader.readValue(updateJson, FlatArticleDto.class);
Object identity = command.identity();
List<PatchChange> changes = command.changes();
```

Direct typed PATCH DTO binding (validated update document → annotated PATCH DTO with
`PatchPresence<T>` members; no projector, no separate normal DTO):

```java
@JsonApiResource(type = "articles")
public record ArticlePatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author) {}

JsonApiPatchDtoReader patchDtoReader = JsonApiJackson3.patchDtoReader(callerMapper);

ArticlePatch patch = patchDtoReader.readValue(updateJson, ArticlePatch.class);
if (!patch.title().isOmitted()) {
  // patch.title() is Present("new title") or Present(null) for explicit JSON null
}
```

`included` resources bind independently through the registry, stay wire-ordered, and are never
injected into relationship properties; identifier primary data passes through as core
`ResourceIdentifier` values. `domainDocumentReader` derives the binder mapper exactly like
`resourceBinder`; `fromDocument(JsonApiDocument)` binds an already-validated document without
re-parsing. The envelope is a public low-level domain-binding result, not a required application
controller/service abstraction: a Spring adapter may unwrap its primary payload
into the application's declared DTO type, so applications can consume typed DTOs without depending
on `JsonApiDomainDocument` (the envelope stays available for document metadata, `included`, or
explicit representation-state access).

`patchReader` forces `DocumentUsage.UPDATE_REQUEST` and `PrimaryDataKind.RESOURCE` for validate-on-read,
binds only supplied mapped attributes and relationships into a `PatchCommand` (never a complete
DTO), never reads `included`, and keeps binder failures as resource-relative `JsonApiMappingException`
pointers. Pass optional `EndpointIdentity` on the factory `ValidationContext`. Applications own
authorization and command application.

`patchDtoReader` uses the same validate-on-read contract and binds the update **directly** into an
application-owned annotated PATCH DTO: every attribute and relationship member must be declared
exactly as `PatchPresence<T>` (wrapper-level `@JsonDeserialize`/`@JsonSerialize` customization —
`using`, `converter`, key/content/null customizers, typing, type refinement, and mix-ins — on such
a member is rejected with `INVALID_PATCH_PROPERTY_TYPE`; inner-`T` customization stays supported),
omitted members become `PatchPresence.omitted()`, supplied members become `PatchPresence.present(...)`
(explicit JSON `null` / null linkage becomes `present(null)`, or `present(Optional.empty())` for an
`Optional` inner type), and supplied members unknown to the PATCH DTO fail with
`UNKNOWN_PATCH_MEMBER` (the low-level path silently ignores them — direct binding rejects).
Parameterized `JavaType` targets (e.g. `GenericPatch<String>`) keep their type bindings through
mapping, conversion, and construction. The caller mapper is never mutated; the binder mapper is
derived via `rebuild()` plus an internal `PatchPresence` module whose internal marker always
serializes with the exact `present`/`value` member names, so the tri-state is invariant to caller
`JsonInclude` inclusion config **and** caller property naming strategies. See
[ADR-013](../docs/adr/013-direct-typed-patch-dto-binding.md).

By default, `@JsonApiId` values become JSON:API `"id"` strings via `Object.toString()`. Pass an
`IdentifierConverter` to `resourceMapper`, `resourceBinder`, `patchReader`, or `patchDtoReader` only
when you need a different wire form; read binding inverts it through `IdentifierConverter.parse(String)`.

`JsonApiJackson3.writer` / `reader` / `resourceMapper` / `resourceBinder` / `patchReader` /
`patchDtoReader` always derive a **new** mapper via `rebuild()`; the caller's mapper or builder is
never mutated. Writers validate before emission. Readers decode through public core constructors,
then run aggregate validation. Mappers and binders introspect types for resource metadata but do
not register a Jackson module (the PATCH DTO reader additionally derives a binder mapper with an
internal `PatchPresence` module; the caller's mapper is still never mutated).

## Non-goals

HTTP `fields[TYPE]` parsing and field authorization beyond the explicit `FieldPolicy` allow-list
remain application/adapter responsibilities. Domain graph hydration and
persistence lookup remain out of scope. Command application (mutating domain or persistence
objects from a `PatchCommand`) remains application-owned. Jackson 2 parity is a separate
artifact; both majors share the neutral contracts of
[jsonapi-java-jackson-common](../jsonapi-java-jackson-common/README.md) per [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md)
- [ADR-002 — Wire states](../docs/adr/002-document-representation.md)
- [ADR-004 — Jackson integration](../docs/adr/004-jackson-integration.md)
- [ADR-005 — Domain mapping and inclusion](../docs/adr/005-domain-mapping-and-inclusion.md)
- [ADR-006 — Document-first reads](../docs/adr/006-read-boundary.md)
- [ADR-007 — Module boundaries](../docs/adr/007-module-boundaries.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [ADR-011 — Flat DTO reads](../docs/adr/011-flat-dto-read-binding.md)
- [ADR-012 — Resource PATCH binding](../docs/adr/012-resource-patch-binding.md)
- [ADR-013 — Direct typed PATCH DTO binding](../docs/adr/013-direct-typed-patch-dto-binding.md)
- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
- [Jackson common contracts module](../jsonapi-java-jackson-common/README.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Validate then write / read then validate:** `JsonApiDocumentWriter` and `JsonApiDocumentReader`
  are the sole public codec paths. Failures preserve stable diagnostics (`ValidationRuleCode` +
  JSON Pointer-like path; reads also carry `CodecFailureCategory` and safe source location). Do not
  expose the codec mapper publicly.
- **Map then write:** `JsonApiResourceMapper` produces core model objects; feed them to a writer
  for serialization. Mapping uses Jackson's logical property model and caches `ResourceMapping`
  by type and mapper config identity. Mapping diagnostics use `MappingDiagnostic` + domain class
  rather than core validation codes.
- **Validate then bind:** `JsonApiResourceBinder` binds already-validated `ResourceObject` values
  to flat DTOs; it never parses JSON, never reads document `included`, and assembles no domain
  graph. `fromResource`/`fromResources` validate `type` against `@JsonApiResource.type()` and
  report `MappingDiagnostic` + a resource-relative pointer (`/type`, `/id`, `/lid`,
  `/relationships/<name>/data`, and the Jackson property name, e.g. `/count`, for bulk
  construction failures). Missing members are omitted; explicit JSON
  `null` binds null; relationship linkage binds `ResourceIdentifier` (plus Optional/List/Set/array
  shapes) directly, and any other target class needs a registered `RelationshipLinkageMapper`.
  Bind failures throw `JsonApiMappingException`, never `JsonApiDocumentReadException`.
- **Typed domain envelope:** `JsonApiDomainDocumentReader` composes the document reader with the
  flat DTO binder. Primary and included resources bind only through the `ResourceTypeRegistry`
  (keyed by `@JsonApiResource.type()` on the registered raw class; annotation lookup only);
  unregistered types fail with `UNREGISTERED_RESOURCE_TYPE` at the document pointer
  (`/data`, `/data/n`, `/included/n`), duplicate type registrations fail at `build()` with
  `CONFLICTING_TYPE_REGISTRATION`. Identifier primary data never binds; absent `included` stays
  null while `included: []` is a non-null empty `IncludedResources` with dual id/lid identity
  lookup. Binder failures are rethrown with document pointer + binder path joined by a single
  `/`. Envelope collections are defensively copied at construction and unmodifiable; `metaAs`
  reuses the reader-derived binder mapper (never a fresh default mapper). No relationship
  injection: `included` DTOs are independently listed/indexed only.
- **Identifier round-trip:** read binding calls `IdentifierConverter.parse(String)` on the wire
  identifier and coerces the result to the identifier property type via `convertValue`; custom
  write converters must override `parse` to invert their wire form.
- **Opt-in inclusion:** Compound `included` resources require a `CompoundSerializationContext` on
  the three-argument mapper overloads (`resource`/`collection`, nullable `DocumentEnvelope`,
  context). `IncludePolicy` gates inclusion traversal only; linkage on selected resources remains
  full when fieldsets are empty. Empty include paths omit `included`; a non-empty request that
  resolves to nothing emits `included: []`. Defaults are deny-all with finite depth/count limits.
- **Sparse fieldsets:** `fieldsets` + `FieldPolicy` on the same context select attributes and
  relationships by final JSON:API names (absent type key = unrestricted; present empty list =
  identity-only). Applied only by `toMappedDocument` / `toMappedResourceCollection`; three-argument
  `toDocument` / `toResourceCollection` reject a non-empty fieldset map with
  `FIELDSETS_REQUIRE_MAPPED_DOCUMENT`. Inclusion traversal may still follow fieldset-excluded
  relationships on validated include paths; `MappedDocument.sparseFieldsetException` is true only
  after an actual relationship omission, and `mapped.applyTo(ValidationContext)` enables the core
  full-linkage exception for that write.
- **Primary-data kind:** Ambiguous `{"type","id"}` and `[]` require explicit `PrimaryDataKind` on
  `DocumentReadContext`; never guess from object members.
- **Wire states:** Omit members for Java `null` components; emit/decode JSON `null` for sealed
  null variants; emit/decode `{}` / `[]` for present-empty wrappers and empty collections.
  Serialize flat wrappers from `flatten()` / `Meta.members()`.
- **Mapper isolation:** Never mutate the caller-supplied `JsonMapper` or `JsonMapper.Builder`;
  always derive via `rebuild()`. Close only parsers created by convenience overloads; leave
  caller-owned streams/parsers open.
- **Nullness:** Production packages are `@NullMarked` (JSpecify only). Use `@Nullable` for
  absence and intentionally null map values. Do not import `core.internal`.
- **Mapping grammar:** JSON:API member-name validation delegates to
  `core.validation.MemberNames`. Do not import `core.internal`.
- **Presence-aware PATCH:** `JsonApiPatchReader` validates with forced `UPDATE_REQUEST` usage, then
  binds only supplied mapped attributes and relationships into a common `PatchCommand`. Never call
  `JsonApiResourceBinder` / whole-DTO construction, never read `included`, never prefix binder
  pointers with `/data`. Explicit attribute JSON `null` stores `value == null` (including Optional
  properties). Identity comes from resource `id` only (no `lid` fallback) and is never a change.
- **Direct typed PATCH DTO:** `JsonApiPatchDtoReader` shares the same validate-on-read contract and
  binds the update into an annotated PATCH DTO whose patchable members are exactly
  `PatchPresence<T>`. Both paths share `PatchMemberConverter` (per-member conversion against an
  explicit target `JavaType`); the DTO path converts through the unwrapped inner type and wraps in
  `Present`/`Omitted`. Declaration violations (`INVALID_PATCH_PROPERTY_TYPE`) and unknown supplied
  members (`UNKNOWN_PATCH_MEMBER`) fail at bind time; the declaration check covers implicit-role
  members too and rejects every wrapper-level Jackson customization path (custom `using`
  serializers/deserializers, converters, key/content/null customizers, typing, type refinement,
  mix-ins). Construction uses the synthetic-map + `convertValue` strategy (ADR-004) with an
  internal `PresenceMarker` + `PatchPresenceModule`; the marker's serializer always emits the exact
  `present`/`value` member names (invariant to caller naming strategies and inclusion config) and
  the deserializer fails loudly on any other marker shape. Never register a `PatchPresence`
  deserializer on the caller's mapper.
- **Architectural tests:** `Jackson3DependencyRulesSpec` allows JDK, JSpecify, core public
  packages, annotations, the common contracts package, `tools.jackson..`, and this module; bans
  `core.internal` and Jackson 2 (`com.fasterxml.jackson..`) in production sources, and asserts no
  moved common-contract type is re-declared here (ADR-010).
- **Tests:** Spock specs under `src/test/groovy/`; remaining adapter-local test domain types under
  `src/test/java/io/github/kazemek/jsonapi/jackson3/testmodel/`. Flat binder contract cases come
  from `DomainReadScenarios`; `ResourceBinderSpec` asserts full-catalog coverage and keeps
  Jackson-API-specific cases local. Compound-inclusion contract cases come from
  `CompoundWriteScenarios`; `CompoundSerializationSpec` asserts full-catalog coverage.
  Sparse-fieldset contract cases come from `SparseFieldsetScenarios`; `SparseFieldsetSpec`
  asserts full-catalog coverage and keeps harness-level assertions (mutation isolation,
  duplicate collapse, exact access counts, `applyTo`/writer validation) local. Typed
  envelope contract cases come from `EnvelopeReadScenarios`; `DomainDocumentReaderSpec` asserts
  full-catalog coverage and keeps Jackson-API-specific cases local (`metaAs`, `JavaType`
  registrations, builder-based reader factories, custom linkage mappers, caller-owned streams,
  malformed input, validation failures).   Presence-aware PATCH contract cases come from
  `PatchScenarios`; `PatchBindingSpec` asserts full-catalog coverage and keeps adapter-local
  cases local (custom deserializer, custom linkage conversion, Optional attribute null,
  `fromDocument` missing id, factory overloads, ownership, illegal primary-data matrices).
  Direct typed PATCH DTO contract cases come from
  `PatchDtoScenarios`; `PatchDtoBindingSpec` asserts full-catalog coverage and keeps adapter-local
  cases local (generics/`JavaType`, wrapper-level `@JsonDeserialize`/`@JsonSerialize` rejection,
  inner-type customization, custom linkage mappers, naming strategy, `fromDocument`, construction
  robustness under `NON_ABSENT`/`NON_EMPTY`, ownership).
