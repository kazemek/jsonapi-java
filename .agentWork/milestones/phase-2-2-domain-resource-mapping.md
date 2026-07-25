# Phase 2.2 — Domain-to-Resource Mapping

> **Module:** `jsonapi-java-jackson`  
> **Dependencies:** Phases 1.1, 1.2, and 2.1  
> **Status:** Not started

## Goal

Map annotated POJOs and records to resource objects while retaining ordinary Jackson property behavior.

## API boundary

Provide an explicit JSON:API mapper/writer API built from a caller-supplied `ObjectMapper`. Registering the document codec must not globally change normal JSON serialization of every annotated domain type.

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
- Identifier conversion follows the annotation milestone and is replaceable.

## Test strategy

Use records, mutable POJOs, immutable creator-based POJOs, inheritance, arrays, collection-like values, and `Optional`.

Prove behavior for:

- naming strategies and `@JsonProperty`;
- `@JsonIgnore` and mix-ins;
- custom value serializers;
- conventional and explicit ids;
- duplicate/conflicting propagated annotations;
- null to-one and empty to-many linkage;
- raw collection rejection;
- attribute/relationship namespace collisions;
- normal JSON serialization remaining unchanged outside the explicit JSON:API writer.

## Acceptance criteria

- [ ] No independent field-first/getter-first scanner exists.
- [ ] Mapping is invoked explicitly and does not hijack the caller's normal `ObjectMapper`.
- [ ] Jackson property behavior is covered by integration tests.
- [ ] Relationship mapping creates linkage but never automatic inclusion.
- [ ] Mapping failures carry stable codes and logical property paths.
- [ ] `./gradlew :jsonapi-java-jackson:test` passes.
