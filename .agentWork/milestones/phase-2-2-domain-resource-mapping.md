# Phase 2.2 — Jackson 3 Domain-to-Resource Mapping

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phases 1.1, 1.2, and 2.1  
> **Status:** Not started

## Goal

Map annotated POJOs and records to resource objects while retaining ordinary Jackson property behavior.

## API boundary

Provide an explicit JSON:API mapper/writer API derived from a caller-supplied Jackson 3
`JsonMapper`/builder. Jackson 3 mappers are immutable, so codec and mapping modules are added while
building or rebuilding the JSON:API mapper; normal serialization through the caller's original
mapper remains unchanged.

This milestone owns write-side mapping definitions and identifier conversion. Phase 2.9 reuses
those immutable definitions for flat DTO reads rather than changing this milestone into a
bidirectional implementation.

The API maps:

- one domain value to single-resource primary data;
- a declared collection to resource-collection primary data;
- annotated relationship values to resource linkage;
- caller-supplied links, metadata, and JSON:API information into the document envelope.

## Metadata discovery

- Use Jackson `BeanDescription`, `JavaType`, and logical properties.
- Respect visibility, `@JsonProperty`, `@JsonIgnore`, mix-ins, naming strategies, custom serializers, and configured modules.
- Resolve annotations propagated across a record component, field, accessor, and creator parameter as one property.
- Cache immutable mapping definitions by Jackson type and relevant mapper configuration identity.
- Report duplicate roles, name collisions, invalid types, missing identifiers, and ambiguous relationships with stable mapping codes.

## Mapping behavior

- Jackson-visible properties are attributes by default.
- Explicit id and relationship roles remove those properties from attributes.
- Nested attribute values are serialized through the configured Jackson provider rather than reflected recursively by the library.
- Relationship values produce explicit-null, single, or collection linkage.
- Relationship mapping does not populate `included`.
- Raw or unresolved collection relationships require a registered custom mapper.
- Phase 1.2 supplies identifier and role metadata only; this milestone owns default and replaceable identifier conversion.

## Test strategy

Use records, mutable POJOs, immutable creator-based POJOs, inheritance, arrays, collection-like values, and `Optional`.

Prove behavior for:

- naming strategies and `@JsonProperty`;
- `@JsonIgnore` and mix-ins;
- custom value serializers;
- conventional and explicit ids, including default identifier conversion, replaceable conversion, and conversion failures;
- invalid `@JsonApiResource` `type` and non-empty attribute/relationship `name()` overrides that violate the JSON:API member-name grammar;
- duplicate/conflicting propagated annotations;
- null to-one and empty to-many linkage;
- raw collection rejection;
- attribute/relationship namespace collisions;
- normal JSON serialization remaining unchanged outside the explicit JSON:API writer.

## Acceptance criteria

- [ ] Mapping uses Jackson's logical property model without an independent field/getter scanner, and integration tests cover records/POJOs, naming, visibility, ignores, mix-ins, creators, inheritance, and custom value serializers.
- [ ] Mapping is invoked explicitly through a mapper derived from caller configuration and does not change ordinary serialization through the caller's original Jackson 3 mapper.
- [ ] Identifier/attribute/relationship role resolution, replaceable identifier conversion, null/collection linkage, collision rejection, and stable logical-property diagnostics match the documented mapping policy; relationships never populate `included`.
- [ ] The canonical `module-docs` checklist passes and `docs/conformance.md` marks only the delivered Jackson 3 mapping shapes **supported**.
- [ ] `./gradlew :jsonapi-java-jackson3:test` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
