# jsonapi-java-annotations

Runtime-visible, dependency-free annotations that declare JSON:API resource, identifier, attribute-name, and relationship-name roles on domain classes and records for later Jackson mapping.

## Packages

| Package                                | Role                                                                    |
|----------------------------------------|-------------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.annotation` | Public annotation API (`@JsonApiResource`, id, attribute, relationship) |

## Minimal usage

```java
@JsonApiResource(type = "articles")
public record Article(
    @JsonApiId String id,
    @JsonApiAttribute(name = "headline") String title,
    @JsonApiRelationship String author) {}
```

These annotations store metadata only. Jackson mapping, member-name validation, identifier conversion, and inclusion policy belong in later modules (Phase 2.2+).

## Non-goals

This module does not provide Jackson codecs, document model types, inclusion/fetch/cascade policy, query parsing, or framework adapters. See [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md) — Phase 1.2 annotation metadata
- [ADR-004 — Jackson introspection](../docs/adr/004-jackson-integration.md)
- [ADR-005 — Linkage vs inclusion](../docs/adr/005-domain-mapping-and-inclusion.md)
- [ADR-007 — Module boundaries](../docs/adr/007-module-boundaries.md)
- [ADR-008 — Public namespace](../docs/adr/008-public-namespace.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Metadata only:** Annotations override JSON:API role or optional field name. They do not invent a second property model, request inclusion, or carry converters/persistence/query elements.
- **Rename sentinel:** Empty `name()` means “keep Jackson's logical property name”; non-empty overrides are validated in Phase 2.2.
- **Nullness:** The production package is `@NullMarked` (JSpecify compile-only). Annotation `String` elements are non-null; the empty string is the rename sentinel. Groovy tests are not annotated.
- **Architectural tests:** `AnnotationDependencyRulesSpec` (ArchUnit) enforces JDK + JSpecify + self type dependencies for production sources (ADR-010). Do not weaken the allowlist without updating the ADR.
- **Tests:** Spock specs under `src/test/groovy/` mirror the main package; Java fixtures under `src/test/java/` cover records and POJOs without Jackson.
