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

Construct model types first (local invariants run in constructors). Call `JsonApiDocumentValidator` with a `ValidationContext` for rules that need the whole document (identity uniqueness, full linkage, extension/profile policy, and similar). For update requests use `DocumentUsage.UPDATE_REQUEST`; a `withExpectedEndpointIdentity(EndpointIdentity)` context makes the validator compare the primary resource `type`+`id` against a caller-derived expected endpoint identity. HTTP/route derivation and mutation remain application-owned.

## Non-goals

This module does not provide Jackson codecs, HTTP adapters, query-parameter parsing, or extension-specific semantics. Those belong in later artifacts; see [ADR-007](../docs/adr/007-module-boundaries.md).

## Further reading

- [Architecture overview](../docs/architecture.md)
- [Conformance checklist](../docs/conformance.md)
- [ADR-002 — Wire states](../docs/adr/002-document-representation.md)
- [ADR-003 — Validation and immutability](../docs/adr/003-validation-and-immutability.md)
- [ADR-009 — JSpecify nullness](../docs/adr/009-jspecify-nullness.md)
- [ADR-010 — Architectural tests](../docs/adr/010-architectural-tests.md)
- [Root agent workflow](../AGENTS.md)

## For contributors / agents

- **Local vs aggregate:** Compact constructors enforce single-value invariants (including RFC 6901
  syntax for `ErrorSource.pointer`, and context-standard link names reserved out of
  `Links.additionalMembers`). Cross-document rules live only in `JsonApiDocumentValidator`.
  Pointer validation is syntax-only and does not resolve against a document; see [conformance](../docs/conformance.md).
- **Identity uniqueness:** Duplicate detection is representation-strict (`ResourceObject.equals`) and alias-aware for identifier collections after id↔lid binding.
- **Wire vocabulary:** `JsonApiMembers` holds shared JSON:API member-name constants for codecs and reserved-name sets; it is not an application-facing entry point.
- **Links channels:** Typed `links()` holds `Link` values (including extension relation keys).
  `additionalMembers` is for `@` pass-through (and other non-reserved open JSON); standard names
  (`self`, `related`, `describedby`, pagination, `about`, `type`) are rejected there with
  `RESERVED_FIELD_NAME`.
- **Diagnostics:** Failures use `JsonApiValidationException` with a stable `ValidationRuleCode` and a JSON Pointer-like path—not bare `IllegalArgumentException`. The three diagnostic families (core validation, codec/read, mapping) are summarized in [architecture](../docs/architecture.md).
- **Nullness:** Production packages are `@NullMarked` (JSpecify only). Use `@Nullable` for member absence and intentionally null map/list values. Explicit JSON `null` stays a sealed variant (`DocumentData.NullData`, etc.), not a bare nullable reference. Keep `LocalValidation.requireNonNull` for construction; do not use JetBrains/JSR-305/Checker nullness annotations. Groovy tests are not annotated.
- **Architectural tests:** `CoreDependencyRulesSpec` (ArchUnit) enforces JDK + JSpecify + self type dependencies for production sources (ADR-010). Do not weaken the allowlist without updating the ADR; add ArchUnit rules when adding modules.
- **Tests:** Spock specs under `src/test/groovy/` mirror the main package layout.
- **Extensions:** Preserve valid extension and `@` members; do not interpret extension semantics in core.
