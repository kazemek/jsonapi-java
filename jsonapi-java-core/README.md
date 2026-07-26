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
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Local vs aggregate:** Compact constructors enforce single-value invariants. Cross-document rules live only in `JsonApiDocumentValidator`.
- **Diagnostics:** Failures use `JsonApiValidationException` with a stable `ValidationRuleCode` and a JSON Pointer-like path—not bare `IllegalArgumentException`.
- **Tests:** Spock specs under `src/test/groovy/` mirror the main package layout.
- **Extensions:** Preserve valid extension and `@` members; do not interpret extension semantics in core.
