# jsonapi-java-jackson3

Jackson 3 codecs for validating, writing, and reading [JSON:API v1.1](https://jsonapi.org/) documents with deterministic wire semantics.

## Packages

| Package                                        | Role                                                                  |
|------------------------------------------------|-----------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.jackson3`           | Public writer/reader factories and validate-then-codec entry points   |
| `io.github.kazemek.jsonapi.jackson3.internal`  | Streaming serializers/decoders and module registration; not public API |

## Minimal usage

```java
JsonMapper callerMapper = JsonMapper.builder().build();

JsonApiDocumentWriter writer = JsonApiJackson3.writer(callerMapper);
String json = writer.writeValueAsString(document);

JsonApiDocumentReader reader =
    JsonApiJackson3.reader(callerMapper, DocumentReadContext.resourceDefaults());
JsonApiDocument roundTrip = reader.readValue(json);
```

Builder form (the builder is not given the JSON:API module):

```java
JsonApiDocumentWriter writer = JsonApiJackson3.writer(JsonMapper.builder());
JsonApiDocumentReader reader =
    JsonApiJackson3.reader(JsonMapper.builder(), DocumentReadContext.resourceDefaults());
```

`JsonApiJackson3.writer` / `reader` always derive a **new** mapper via `rebuild()`; the caller's mapper or builder configuration is never mutated in place. Writers validate before emission. Readers require an explicit `DocumentReadContext` (primary-data kind plus validation policy), decode through public core constructors, then run aggregate validation before returning. Pass a non-default `ValidationContext` (writer) or `DocumentReadContext` (reader) when extension/profile policy, create-request usage, or resource-identifier primary data is required. The codec mapper is not part of the public API—use only `JsonApiDocumentWriter` / `JsonApiDocumentReader` methods.

## Non-goals

This module does not yet map annotated domain types or provide compound-inclusion / sparse-fieldset write policy. Those land in later Phase 2 milestones. Jackson 2 parity is a separate artifact; see [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md)
- [ADR-002 — Wire states](../docs/adr/002-document-representation.md)
- [ADR-004 — Jackson integration](../docs/adr/004-jackson-integration.md)
- [ADR-006 — Document-first reads](../docs/adr/006-read-boundary.md)
- [ADR-007 — Module boundaries](../docs/adr/007-module-boundaries.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Canonical fixtures](../fixtures/jsonapi-1.1/README.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Validate then write / read then validate:** `JsonApiDocumentWriter` and `JsonApiDocumentReader` are the sole public codec paths. Failures preserve stable diagnostics (`ValidationRuleCode` + JSON Pointer-like path; reads also carry `CodecFailureCategory` and safe source location). Do not expose the codec mapper publicly.
- **Primary-data kind:** Ambiguous `{"type","id"}` and `[]` require explicit `PrimaryDataKind` on `DocumentReadContext`; never guess from object members.
- **Wire states:** Omit members for Java `null` components; emit/decode JSON `null` for sealed null variants; emit/decode `{}` / `[]` for present-empty wrappers and empty collections. Serialize flat wrappers from `flatten()` / `Meta.members()`.
- **Mapper isolation:** Never mutate the caller-supplied `JsonMapper` or `JsonMapper.Builder`; always `rebuild().addModule(...).build()` on a built mapper. Close only parsers created by convenience overloads; leave caller-owned streams/parsers open.
- **Nullness:** Production packages are `@NullMarked` (JSpecify only). Use `@Nullable` for absence and intentionally null map values. Do not import `core.internal`.
- **Architectural tests:** `Jackson3DependencyRulesSpec` allows JDK, JSpecify, core public packages, annotations, `tools.jackson..`, and this module; bans `core.internal` and Jackson 2 (`com.fasterxml.jackson..`) in production sources (ADR-010).
- **Tests:** Spock specs under `src/test/groovy/`; shared fixtures under `fixtures/jsonapi-1.1/`.
