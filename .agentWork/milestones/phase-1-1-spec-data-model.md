# Phase 1.1 — Document Model and Validation

> **Module:** `jsonapi-java-core`  
> **Packages:** `io.github.kazemek.jsonapi.core.model` and `io.github.kazemek.jsonapi.core.validation`  
> **Dependencies:** None beyond `java.base`  
> **Status:** Not started

## Goal

Define a dependency-free Java representation of JSON:API v1.1 documents that preserves wire-visible states and cannot silently become invalid after construction.

The milestone is driven by semantic coverage, not a fixed type count or a claim that every Java type maps 1:1 to a named specification object.

## Required representation

### Primary data

`DocumentData` is sealed and has five non-null variants:

- explicit JSON `null`;
- one `ResourceObject`;
- a collection of resource objects;
- one `ResourceIdentifier`;
- a collection of resource identifiers.

On `JsonApiDocument`, a Java `null` `data` component means that the member is absent. The explicit-null variant means that the document contains `"data": null`. Empty collections remain collections and never collapse to null or absence.

### Relationships and linkage

`Relationship` is one object, not a to-one/to-many hierarchy. It contains optional `data`, `links`, and `meta` members plus permitted additional members.

`RelationshipData` is sealed and represents:

- explicit JSON `null` for empty to-one linkage;
- one resource identifier;
- a resource-identifier collection, including an empty collection.

A Java `null` relationship-data component means that the `data` member is absent. This permits valid link-only and meta-only relationships without inventing cardinality during deserialization.

### Resources

`ResourceIdentifier` represents `type`, optional `id`, optional `lid`, optional `meta`, and permitted additional members. It requires `type` and requires `id` unless the identifier represents a new resource, in which case `lid` is required.

`ResourceObject` represents `type`, optional `id`/`lid`, optional attributes, relationships, links, and meta, plus permitted additional members. Construction always requires `type` but permits both identifiers to be absent because a client-originated create resource may omit them. A document-usage validator permits that state only in the applicable create context; other resource representations require `id`. It also validates the shared namespace of attributes, relationships, `type`, and `id`.

`Attributes` and `Relationships` are flat object wrappers rather than raw maps. They keep ordinary attribute/relationship members separate from pass-through extension and `@` members, because an `@` member inside either object must not be interpreted as an attribute or relationship. All groups flatten into one JSON object at the codec boundary and reject name collisions.

The model must preserve the difference between an absent `attributes` or `relationships` member and an explicitly present empty object.

### Links and metadata

`Link` remains a sealed value with string and object forms. A link object requires `href` and supports `rel`, `describedby`, `title`, `type`, `hreflang`, and `meta`.

The codec must accept both legal `hreflang` forms. The model uses a documented canonical list representation and the codec emits an array, including for a single language.

`Links` is a flat object wrapper with an insertion-ordered map from relation name to nullable link plus separate pass-through additional members. It supports standard, pagination, and policy-permitted relations without interpreting `@` members as links. It must retain the distinction between an omitted relation and a relation whose value is JSON `null`; all groups flatten into one JSON object and reject collisions.

`Meta` is a flat, insertion-ordered JSON-compatible member map. It must not serialize through a synthetic `members` property.

The broad `Links` representation is validated in the context where it appears: top-level, resource, relationship, and error links have different standard members. Relationship pagination that cannot be proven valid from a link-only document requires a cardinality hint from validation context rather than guessed cardinality.

### Errors and JSON:API information

Model `ErrorObject`, `ErrorSource`, and `JsonApiObject` without context-specific link containers. `Links` is reused so legal and extension-defined links remain representable. An error object must contain at least one standard error member; ignored `@` members do not satisfy that requirement.

### Additional members

Fixed-shape specification objects carry an immutable additional-members map. The map can preserve:

- namespaced extension members;
- `@` members;
- profile-defined members accepted by an explicitly configured profile policy in the codec.

Core validates member-name shape but does not implement extension or profile semantics. Unknown unnamespaced members are not silently accepted as valid base-spec members.

### Top-level document

`JsonApiDocument` represents optional `data`, `errors`, `meta`, `jsonapi`, `links`, `included`, and additional members.

It enforces local top-level rules:

- at least one of `data`, `errors`, `meta`, or an applied extension member is present;
- `data` and `errors` do not coexist;
- `included` is absent when `data` is absent;
- an explicitly present `errors` member is an array;
- an explicitly present `included` member is an array.

## Immutability and open JSON values

- Every collection is copied while preserving insertion order.
- Copies used for `Links` must preserve null values; `Map.copyOf` is not suitable there.
- Nested lists/maps in attributes, metadata, and additional members are recursively copied into unmodifiable containers.
- Open values are limited to documented JSON-compatible shapes: null, string, boolean, finite JSON number, list, and string-keyed map.
- Records are described as structurally immutable; no claim is made that arbitrary caller objects become immutable.

## Validation model

Compact constructors or the only public construction path enforce local invariants. There is no public “raw but possibly invalid” constructor intended for Jackson.

Local validation includes the exact JSON:API member-name grammar and applicable URI-reference, link-relation, language-tag, media-type, extension/profile URI, identifier, and finite-number syntax. Rules requiring external request or application knowledge remain contextual rather than guessed.

`JsonApiDocumentValidator` enforces aggregate rules that need document context:

- no duplicate included resource identity by `type`+`id`, or `type`+`lid` where applicable;
- every included resource has full linkage from primary data, except the sparse-fieldset exception;
- local identifiers refer consistently to the same resource;
- resource identity requirements match the declared document usage, such as create request versus response;
- extension/profile member acceptance matches the supplied validation context.

Validation failures use a library exception carrying a stable rule code and JSON Pointer-like path. They do not use generic `IllegalArgumentException` as the public diagnostics contract.

## Test strategy

Create Spock specifications mirroring the core packages. Cover:

- absent data versus explicit-null data;
- single and collection resource and identifier primary data;
- absent, null, single, and collection relationship linkage;
- link-only and meta-only relationships;
- empty to-many linkage;
- nullable pagination links and relationship pagination links;
- both input forms and the canonical output model for `hreflang`;
- valid and invalid URI references, relation types, language tags, media types, and extension/profile URIs;
- context-specific standard link members and relationship pagination hints;
- attributes/relationships present-empty versus absent;
- extension and `@` members inside attribute, relationship, and link containers without misclassifying them;
- resource identity and field-name collisions;
- error and top-level minimum-member rules;
- defensive copies, including null-valued links and nested open values;
- extension, profile-policy, and `@` member handling;
- duplicate included resources and full-linkage validation;
- stable validation rule codes and paths.

Wire-format assertions belong to Phase 2.1; Phase 1 tests assert model semantics and validation without a Jackson dependency.

## Acceptance criteria

- [ ] Every JSON:API v1.1 base document shape needed by the codec is representable without conflating absence and explicit null.
- [ ] Local invariants cannot be bypassed through a public raw constructor.
- [ ] Aggregate validation covers included identity, full linkage, local identifiers, and configured additional members.
- [ ] Collections and nested open JSON containers are defensively copied.
- [ ] The module has zero third-party runtime dependencies.
- [ ] Tests cover every enforced MUST/MUST NOT rule with stable diagnostic codes.
- [ ] `./gradlew :jsonapi-java-core:test` passes.
- [x] The verified base package namespace is documented (ADR-008 / Phase 0.1).
