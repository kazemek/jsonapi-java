# jsonapi-java-jackson3

Jackson 3 codecs for validating and writing [JSON:API v1.1](https://jsonapi.org/) documents with deterministic wire semantics.

## Packages

| Package                                        | Role                                                         |
|------------------------------------------------|--------------------------------------------------------------|
| `io.github.kazemek.jsonapi.jackson3`           | Public writer factory and validate-then-write entry point    |
| `io.github.kazemek.jsonapi.jackson3.internal`  | Streaming serializers and module registration; not public API |

## Minimal usage

```java
JsonMapper callerMapper = JsonMapper.builder().build();
JsonApiDocumentWriter writer = JsonApiJackson3.writer(callerMapper);

String json = writer.writeValueAsString(document);
```

Builder form (the builder is not given the JSON:API module):

```java
JsonApiDocumentWriter writer = JsonApiJackson3.writer(JsonMapper.builder());
```

`JsonApiJackson3.writer` always derives a **new** mapper via `rebuild()`; the caller's mapper or builder configuration is never mutated in place. Aggregate validation runs before any generator output. Pass an explicit `ValidationContext` when extension/profile policy or create-request usage is required. The codec mapper is not part of the public API—use only `JsonApiDocumentWriter` write methods.

## Non-goals

This module does not yet deserialize documents, map annotated domain types, or provide compound-inclusion / sparse-fieldset write policy. Those land in later Phase 2 milestones. Jackson 2 parity is a separate artifact; see [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md)
- [ADR-002 — Wire states](../docs/adr/002-document-representation.md)
- [ADR-004 — Jackson integration](../docs/adr/004-jackson-integration.md)
- [ADR-007 — Module boundaries](../docs/adr/007-module-boundaries.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Canonical writer fixtures](../fixtures/jsonapi-1.1/)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Validate then write:** `JsonApiDocumentWriter` is the sole public write path; it calls `JsonApiDocumentValidator` before emission. Failures leave no partial output and preserve `ValidationRuleCode` + JSON Pointer-like path. Do not expose the codec mapper publicly.
- **Wire states:** Omit members for Java `null` components; emit JSON `null` for sealed null variants; emit `{}` / `[]` for present-empty wrappers and empty collections. Serialize flat wrappers from `flatten()` / `Meta.members()`.
- **Mapper isolation:** Never mutate the caller-supplied `JsonMapper` or `JsonMapper.Builder`; always `rebuild().addModule(...).build()` on a built mapper.
- **Nullness:** Production packages are `@NullMarked` (JSpecify only). Use `@Nullable` for absence and intentionally null map values. Do not import `core.internal`.
- **Architectural tests:** `Jackson3DependencyRulesSpec` allows JDK, JSpecify, core public packages, annotations, `tools.jackson..`, and this module; bans `core.internal` and Jackson 2 (`com.fasterxml.jackson..`) in production sources (ADR-010).
- **Tests:** Spock specs under `src/test/groovy/`; shared fixtures under `fixtures/jsonapi-1.1/`.
