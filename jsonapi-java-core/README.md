# jsonapi-java-core

Zero-dependency Java representation of [JSON:API v1.1](https://jsonapi.org/) documents, with local construction invariants and aggregate document validation.

## Packages

| Package                                     | Role                                                               |
|---------------------------------------------|--------------------------------------------------------------------|
| `io.github.kazemek.jsonapi.core.model`      | Immutable document model (resources, relationships, links, errors) |
| `io.github.kazemek.jsonapi.core.validation` | Aggregate validator, validation context, stable rule codes         |
| `io.github.kazemek.jsonapi.core.internal`   | Shared helpers; not a public API surface                           |

## Minimal usage

```java
ResourceObject resource = ResourceObject.of("articles", "1");
JsonApiDocument document = JsonApiDocument.withData(
    new DocumentData.SingleResource(resource));

new JsonApiDocumentValidator().validate(document, ValidationContext.defaults());
```

Construct model types first (local invariants run in constructors). Call `JsonApiDocumentValidator` with a `ValidationContext` for rules that need the whole document (identity uniqueness, full linkage, extension/profile policy, and similar).

## Non-goals

This module does not provide Jackson codecs, HTTP adapters, query-parameter parsing, or extension-specific semantics. Those belong in later artifacts; see [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Conformance checklist](../docs/conformance.md)
- [ADR-002 — Wire states](../docs/adr/002-document-representation.md)
- [ADR-003 — Validation and immutability](../docs/adr/003-validation-and-immutability.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Local vs aggregate:** Compact constructors enforce single-value invariants. Cross-document rules live only in `JsonApiDocumentValidator`.
- **Diagnostics:** Failures use `JsonApiValidationException` with a stable `ValidationRuleCode` and a JSON Pointer-like path—not bare `IllegalArgumentException`.
- **Nullness:** Production packages are `@NullMarked` (JSpecify only). Use `@Nullable` for member absence and intentionally null map/list values. Explicit JSON `null` stays a sealed variant (`DocumentData.NullData`, etc.), not a bare nullable reference. Keep `LocalValidation.requireNonNull` for construction; do not use JetBrains/JSR-305/Checker nullness annotations. Groovy tests are not annotated.
- **Architectural tests:** `CoreDependencyRulesSpec` (ArchUnit) enforces JDK + JSpecify + self type dependencies for production sources (ADR-010). Do not weaken the allowlist without updating the ADR; add ArchUnit rules when adding modules.
- **Tests:** Spock specs under `src/test/groovy/` mirror the main package layout.
- **Extensions:** Preserve valid extension and `@` members; do not interpret extension semantics in core.
