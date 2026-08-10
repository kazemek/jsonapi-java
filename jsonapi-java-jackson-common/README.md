# jsonapi-java-jackson-common

Jackson-major-neutral public contracts for codec and domain-mapping policy, diagnostics, contexts,
and domain envelope values shared by the Jackson 2 and Jackson 3 adapters.

## Packages

| Package                                    | Role                                                                                 |
|--------------------------------------------|--------------------------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.jackson`        | Jackson-import-free policy, diagnostic, context, and domain envelope contracts       |

## Minimal usage

This module has no standalone entry points. Consumers use it through a Jackson adapter:

```java
// Jackson 3 (or, later, Jackson 2) consumes the same neutral contracts:
DocumentReadContext context = DocumentReadContext.resourceDefaults();
CompoundSerializationContext inclusion =
    CompoundSerializationContext.defaults()
        .withIncludePaths(List.of(IncludePath.of("comments.author")))
        .withIncludePolicy(IncludePolicy.allowAll());
```

Types here are values only: policies (`IncludePolicy`, `FieldPolicy`, allowance keys), read/write
contexts (`DocumentReadContext`, `CompoundSerializationContext`, `DocumentEnvelope`,
`MappedDocument`), diagnostics (`MappingDiagnostic`, `CodecFailureCategory`,
`JsonApiMappingException`, `JsonApiDocumentReadException`, `SourceLocation`), identifier
conversion (`IdentifierConverter`), and domain envelope values (`DomainData`, `IncludedResources`).
No type in this package imports or exposes `tools.jackson.*` or `com.fasterxml.jackson.*`;
Jackson-bound factories, readers, writers, binders, and mapping introspection stay in the
major-specific adapter packages.

## Non-goals

This module does not share Jackson-bound readers, writers, mapping introspection, serializers,
binders, module registration, or mapper factories; there is no runtime major detection and no
lowest-common-denominator Jackson abstraction. Jackson 2 and Jackson 3 remain separately compiled
artifacts; see [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md)
- [ADR-004 — Jackson integration](../docs/adr/004-jackson-integration.md)
- [ADR-007 — Module boundaries](../docs/adr/007-module-boundaries.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Jackson-free boundary:** Production code must not import `tools.jackson.*`,
  `com.fasterxml.jackson.*`, or any major-specific adapter package (`jackson2..`, `jackson3..`).
  ArchUnit enforces this via `JacksonCommonDependencyRulesSpec` (ADR-010). Moved-type Javadocs
  must not `{@link}` Jackson-major-specific types; keep wording neutral.
- **IncludedResources invariant:** Assemble with `IncludedResources.of(resources, identityIndex)`
  where index values are 0-based positions into the resource list. Positions make
  identity-index/resource-list inconsistency unrepresentable; out-of-range positions are rejected.
  Do not re-introduce a raw two-collection constructor.
- **Move policy:** Neutral contracts live here, not in the adapters. When a type can be expressed
  without Jackson imports, move it here rather than duplicating it per major; when it exposes
  Jackson APIs it must stay in the adapter.
- **Nullness:** The package is `@NullMarked` (JSpecify only). Use `@Nullable` for member absence
  and intentionally null values; explicit wire `null` stays a sealed variant (`DomainData.NullData`,
  etc.). NullAway enforces this on Java `main` sources (ADR-009).
- **Diagnostics:** `JsonApiMappingException` carries a stable `MappingDiagnostic`; read failures
  use `JsonApiDocumentReadException` with `CodecFailureCategory`, a JSON Pointer-like path, and a
  safe `SourceLocation`. Do not introduce new failure types without a milestone.
- **Tests:** Spock specs under `src/test/groovy/` mirror the main package layout; unit/contract
  tests of moved types live here, while Jackson-bound integration suites stay in the adapters.
