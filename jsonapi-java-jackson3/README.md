# jsonapi-java-jackson3

Jackson 3 codecs for validating, writing, and reading [JSON:API v1.1](https://jsonapi.org/) documents,
and for mapping annotated domain types to resource objects.

## Packages

| Package                                        | Role                                                                  |
|------------------------------------------------|-----------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.jackson3`           | Public writer/reader/mapper factories and validate-then-codec entry points |
| `io.github.kazemek.jsonapi.jackson3.internal`  | Streaming serializers/decoders, mapping engine, module registration; not public API |

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

Bare resource (inspect or compose a document yourself; not a top-level wire payload):

```java
ResourceObject resource = mapper.toResource(someAnnotatedPojo);
```

Collection primary data (also a `JsonApiDocument`; feed it to the same writer):

```java
JsonApiDocument collDoc = mapper.toResourceCollection(allPojos);
```

By default, `@JsonApiId` values become JSON:API `"id"` strings via `Object.toString()`. Pass an
`IdentifierConverter` to `resourceMapper` or `resourceBinder` only when you need a different wire
form; read binding inverts it through `IdentifierConverter.parse(String)`.

`JsonApiJackson3.writer` / `reader` / `resourceMapper` / `resourceBinder` always derive a **new**
mapper via `rebuild()`; the caller's mapper or builder is never mutated. Writers validate before
emission. Readers decode through public core constructors, then run aggregate validation. Mappers
and binders introspect types for resource metadata but do not register a Jackson module.

## Non-goals

Sparse-fieldset write policy and typed domain envelopes (including independent binding of
`included` resources) are planned for later Phase 2 milestones. Jackson 2 parity is a separate
artifact; see [ADR-007](../docs/adr/007-module-boundaries.md).

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
- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
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
- **Identifier round-trip:** read binding calls `IdentifierConverter.parse(String)` on the wire
  identifier and coerces the result to the identifier property type via `convertValue`; custom
  write converters must override `parse` to invert their wire form.
- **Opt-in inclusion:** Compound `included` resources require a `CompoundSerializationContext` on
  the three-argument mapper overloads (`resource`/`collection`, nullable `DocumentEnvelope`,
  context). `IncludePolicy` gates inclusion traversal only; linkage on selected resources remains
  full. Empty include paths omit `included`; a non-empty request that resolves to nothing emits
  `included: []`. Defaults are deny-all with finite depth/count limits.
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
- **Architectural tests:** `Jackson3DependencyRulesSpec` allows JDK, JSpecify, core public
  packages, annotations, `tools.jackson..`, and this module; bans `core.internal` and Jackson 2
  (`com.fasterxml.jackson..`) in production sources (ADR-010).
- **Tests:** Spock specs under `src/test/groovy/`; test domain types under
  `src/test/java/io/github/kazemek/jsonapi/jackson3/testmodel/`.
