# Phase 1.2 — Domain-Mapping Annotations

> **Module:** `jsonapi-java-annotations`  
> **Package:** `io.github.kazemek.jsonapi.annotation`  
> **Dependencies:** None beyond `java.base`  
> **Status:** Not started

## Goal

Define a minimal annotation vocabulary for opt-in domain-to-resource mapping. Annotations describe JSON:API roles; Jackson remains authoritative for property discovery, names, visibility, values, and custom serialization.

This milestone is independent of Phase 1.1 and may be implemented in parallel after Phase 0.1.

## Default mapping policy

For a type marked `@JsonApiResource`:

- Jackson-visible logical properties are attributes by default.
- The identifier property and relationship properties are removed from attributes.
- Jackson-ignored properties remain ignored.
- Jackson names, naming strategies, and mix-ins apply unless a JSON:API annotation explicitly overrides the JSON:API field name.
- Domain mapping is serialization-only in the initial roadmap.

This default is intentionally close to ordinary Jackson serialization. Applications that require explicit allow-listing use Jackson visibility and ignore annotations rather than a second competing visibility system.

## Annotation contracts

### `@JsonApiResource`

- Runtime annotation targeting types.
- Requires a non-blank resource `type`.
- The type value follows the complete JSON:API member-name grammar: case-sensitive ASCII letters and digits or permitted non-ASCII characters, with hyphen, underscore, and space allowed only internally.
- Mapping rejects annotated interfaces, enums, and annotation types; supported shapes are classes and records.
- The annotation is not inherited implicitly. A subtype must declare its resource type or be handled by an explicit mapping policy.

### `@JsonApiId`

- Runtime annotation targeting fields, record components, methods, and parameters participating in a Jackson logical property.
- Zero or one logical property may be explicitly annotated.
- If no logical property is annotated, the Jackson logical property named `id` is used by convention.
- More than one logical id property, or absence of both an annotation and conventional `id`, is a mapping-definition error.
- Conversion to JSON:API string form is delegated to a documented identifier converter. The default supports strings, integral values, UUIDs, and enums; custom converters are explicit.

### `@JsonApiAttribute`

- Optional runtime annotation targeting fields, record components, methods, and parameters.
- Overrides the JSON:API attribute name; an empty override retains the Jackson logical property name.
- It does not make a Jackson-ignored property visible.
- Applying it to the logical id property or together with `@JsonApiRelationship` is a mapping-definition error.

### `@JsonApiRelationship`

- Runtime annotation with the same property targets as `@JsonApiAttribute`.
- Overrides the JSON:API relationship name; an empty override retains the Jackson logical property name.
- It marks linkage only. It never requests inclusion and carries no fetch, cascade, repository, or ORM options.
- Names share a namespace with attributes and cannot be `type` or `id`.

## Logical-property resolution

Phase 2.2 resolves annotations through Jackson introspection. A record annotation that is propagated to its component, field, accessor, or constructor parameter still describes one logical property.

Conflicting annotations found on members contributing to the same logical property are rejected with a stable mapping error. There is no “fields first, getters second” precedence.

## Relationship cardinality

Cardinality comes from Jackson `JavaType`:

- arrays and collection-like types are to-many;
- `Optional<T>` and ordinary object values are to-one;
- null to-one values create explicit-null linkage;
- empty collection-like values create empty to-many linkage;
- raw, unresolved, map-like, or otherwise ambiguous relationship types are rejected unless a custom relationship mapper is registered.

JPA proxies and persistent collections receive no special behavior. They are handled only to the extent that their exposed Jackson type and serializer behave like supported values.

## Member-name validation

Use one shared implementation of the JSON:API v1.1 grammar. Tests must cover:

- uppercase and lowercase names;
- Unicode names;
- internal hyphen, underscore, and space;
- empty names and illegal leading/trailing separators;
- reserved punctuation;
- extension namespace syntax separately from ordinary member names.

## Test strategy

Annotation-module tests verify retention, targets, defaults, and valid annotation placement on records and POJOs without adding Jackson.

Jackson logical-property behavior, mix-ins, naming strategies, conflict detection, cardinality, and identifier conversion are acceptance tests for Phase 2.2, where Jackson is available.

## Acceptance criteria

- [ ] Four annotations are provided with runtime retention and property targets that include creator parameters.
- [ ] The default-attribute and id-convention policies are documented without contradiction.
- [ ] Annotation types contain no inclusion, persistence, query, or Spring concerns.
- [ ] The exact member-name grammar has shared test vectors ready for core and mapping use.
- [ ] The module has zero third-party runtime dependencies.
- [ ] `./gradlew :jsonapi-java-annotations:test` passes.
- [x] The verified base package namespace is documented (ADR-008 / Phase 0.1).
