# Phase 2.1 — Jackson Document Codec

> **Module:** `jsonapi-java-jackson`  
> **Dependencies:** Phase 1.1, Jackson databind  
> **Status:** Not started

## Goal

Encode and decode the core document model with exact JSON:API v1.1 wire semantics before domain mapping is added.

## Deliverables

- A Jackson module for core document types.
- Custom handling for flat links/meta, sealed data/linkage values, explicit null, additional members, and string/object links.
- Strict reads that construct validated core values and run aggregate validation.
- A documented canonical output policy: stable member ordering for fixtures, array-form `hreflang`, and preservation of collection order.
- A validation context for allowed extensions and profiles; extension semantics remain outside the codec.
- Structured read failures containing rule code, path, and safe source location.

## Fixture suite

Create golden request and response fixtures based on official specification examples:

- single resource, resource collection, and empty collection;
- single identifier and identifier collection;
- explicit-null primary and relationship data;
- absent relationship data, link-only relationships, and meta-only relationships;
- links as strings, objects, and null, including pagination;
- metadata, JSON:API information, and errors;
- compound documents and local identifiers;
- extension and `@` members;
- malformed documents for each enforced MUST/MUST NOT rule.

Round trips need semantic equality. Exact byte equality is required only where the canonical writer contract declares ordering or shape.

## Non-goals

- Annotated domain-object mapping.
- Automatic object-graph hydration.
- Query or HTTP handling.
- Lenient acceptance of invalid JSON:API.

## Acceptance criteria

- [ ] Every Phase 1.1 state has a valid wire representation and read path.
- [ ] `{}` and `{"data":null}` do not decode to the same value.
- [ ] Links and meta serialize as flat objects without a synthetic `members` key.
- [ ] Official golden fixtures pass in both directions where applicable.
- [ ] Negative fixtures return stable rule codes and paths.
- [ ] Jackson is not added to the core module.
- [ ] `./gradlew :jsonapi-java-jackson:test` passes.
