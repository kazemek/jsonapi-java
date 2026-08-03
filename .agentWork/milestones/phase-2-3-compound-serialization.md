# Phase 2.3 — Jackson 3 Compound Serialization Context

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 2.2  
> **Status:** Not started

## Goal

Add explicit, bounded compound-document inclusion to Jackson 3 write mapping without turning relationship mapping into automatic graph traversal.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) — a relationship creates linkage but never requests automatic inclusion; include paths and traversal limits remain explicit application policy.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — compound serialization validates requested paths, includes intermediates for full linkage, deduplicates by identity, applies finite depth/count limits, treats repeated identity as deduplication not recursion, and emits deterministic first-encounter order. No JPA/Hibernate implicit integration.
- [JSON:API compound documents](https://jsonapi.org/format/1.1/#document-compound-documents) — included resources require full linkage and unique resource identity; `included` appears only when `data` is present.
- `JsonApiDocumentValidator` (core) — already enforces identity uniqueness, full linkage, and local-identifier consistency; the engine's output must pass this validator unchanged.
- Phase 2.2 — `ResourceMapping` / `MappingProperty` / `MappingDefinitionCache` provide the Jackson logical-property metadata and collection content-type resolution reused by traversal; traversal must not rescan domain types. `DomainResourceWriter.toResource` always emits full linkage for every mapped relationship on an already-selected resource; inclusion traversal is a separate concern (see Inclusion behavior).
- Core `RelationshipPaginationKey` (`resourceType` + `relationshipName`) is the shape precedent for typed include allowances; jackson3 may introduce a public `RelationshipAllowance` (or equivalent) with that shape rather than overloading pagination semantics.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — new public types are `@NullMarked` with accurate `@Nullable`; [ADR-010](../../docs/adr/010-architectural-tests.md) — no new production package is introduced and the existing jackson3 allowlist (JDK, JSpecify, core public, annotations, `tools.jackson..`) remains unchanged.
- Phase 2.13 (Jackson 2 parity) will mirror this mapping-only context (no `ValidationContext` on the compound serialization context); refine that not-started milestone when implementing parity if its prose still diverges.

## Serialization context

The caller supplies an immutable `CompoundSerializationContext` carrying:

- requested include paths as `IncludePath` values (ordered list of relationship JSON:API names);
- an `IncludePolicy` deciding which relationships may be traversed for inclusion (default `IncludePolicy.denyAll()`);
- maximum relationship depth (default 10);
- maximum included-resource count (default 100).

Defaults request no included resources and apply finite safety limits. The context offers `withX()` copy methods matching the `ValidationContext` style; it is a *mapping* concern and does not carry a `ValidationContext` (validation continues to live on `JsonApiDocumentWriter`).

## Inclusion behavior

- Pre-traversal: validate every requested `IncludePath` segment-by-segment against Phase 2.2 mapping metadata (each segment must name a relationship on the relevant type, matched to `MappingProperty.jsonapiName()`), against `IncludePolicy`, and against `maxDepth` (path length cap); fail fast with `INVALID_INCLUDE_PATH`, `DENIED_RELATIONSHIP_INCLUDE`, or `INCLUDE_DEPTH_EXCEEDED` before any inclusion-traversal property access.
- Include intermediate resources required for full linkage along each requested path.
- Emit `included: []` when a supported, explicitly supplied include request resolves to no resources.
- Do not place resources in `included` that were not requested.
- **Policy vs linkage:** `IncludePolicy` governs *inclusion traversal only* — whether a related domain object is read in order to populate `included`. Linkage identifier emission remains Phase 2.2 `DomainResourceWriter.toResource` behavior and is unaffected by `denyAll()` / `allowing(...)`: a denied or unrequested relationship still appears as linkage (its `ResourceIdentifier`) on a selected resource; it is simply never *included*.
- **Access vs linkage:** Primary and included `ResourceObject`s are built with full linkage via `DomainResourceWriter.toResource` (or equivalent), which may read every mapped relationship getter for linkage emission on an already-selected resource. Access-counting and the “never read” guarantee apply only to **inclusion traversal**: reading related domain objects in order to populate `included`. Traversal into a related object happens only when that relationship segment is on a validated, policy-allowed requested path. Off-path relationships are never followed for inclusion and their getters are not invoked for traversal. Access-counting fixtures assert traversal getters, not that linkage getters on primary/included resources are skipped.
- Deduplicate by `ResourceIdentity` (type+id or type+lid via `ResourceObject.identityKey()`).
- Treat revisiting an already-included identity as deduplication (no recursion), not an error.
- Reject conflicting representations of the same identity (same key, unequal `ResourceObject`) with `CONFLICTING_INCLUDED_REPRESENTATION`.
- Enforce `maxDepth` and `maxIncluded`, failing with `INCLUDE_DEPTH_EXCEEDED` / `INCLUDE_COUNT_EXCEEDED`.
- **`maxDepth`:** a cap on requested *path length* (number of relationship segments from the primary resource along a requested path), not graph diameter. Checked at pre-traversal validation; runtime traversal never exceeds the explicit requested path, so depth is bounded by construction once paths are validated. Exceeding fails with a dotted JSON:API relationship path (e.g. `comments.author`).
- **`maxIncluded`:** enforced across the accumulated included set (not counting primary `data` resources).
- **Order:** process requested `IncludePath`s in caller order; within each path, walk relationship targets level-by-level (BFS along that path). Global `included` order is first-discovery across that walk. Primary resources remain in input order.
- **Diagnostic `propertyPath`:** use dotted JSON:API relationship names consistent with include syntax (e.g. `comments.author`), not only a single Java `logicalName`.

## Safety and ORM neutrality

The mapper has no persistence dependency and never calls JPA/Hibernate initialization APIs. Relationship/property access is ordinary Jackson/property access controlled by the inclusion context; if the caller supplies a lazy proxy or getter, that access may still initialize it and trigger I/O. Callers requiring no I/O must provide an access-safe, already-loaded graph. Depth, count, and conflict behavior produce structured `MappingDiagnostic` failures, not unbounded graph walks.

## Deliverables

- Public immutable `CompoundSerializationContext` with `IncludePath` (ordered list of JSON:API relationship names; `IncludePath.of(String)` parses dot-separated names) and `IncludePolicy` (`allowAll()` / `denyAll()` / `allowing(Set<RelationshipAllowance>)` where `RelationshipAllowance` is a public jackson3 record of `resourceType` + `relationshipName`, matching the `RelationshipPaginationKey` shape); `withX()` copy methods and finite defaults (depth 10, count 100).
- Extend `JsonApiResourceMapper` with context-accepting overloads:
  - `toDocument(Object, CompoundSerializationContext)` and `toDocument(Object, DocumentEnvelope, CompoundSerializationContext)`;
  - `toResourceCollection(Iterable<?>, CompoundSerializationContext)` and `toResourceCollection(Iterable<?>, DocumentEnvelope, CompoundSerializationContext)`.
  Add internal `CompoundInclusionEngine` that pre-validates paths, walks the graph for inclusion via selective relationship reads, builds primary and included resources with `DomainResourceWriter.toResource` (full linkage) and uses `extractIdentifier` where linkage-only identity is needed, deduplicates by `ResourceIdentity`, and produces an ordered `List<ResourceObject>` for `JsonApiDocument.included`. Update `buildDocument` to accept `@Nullable List<ResourceObject> included` (`null` = omit the member; empty list = emit `included: []`).
- Add `MappingDiagnostic` codes `INVALID_INCLUDE_PATH`, `DENIED_RELATIONSHIP_INCLUDE`, `CONFLICTING_INCLUDED_REPRESENTATION`, `INCLUDE_DEPTH_EXCEEDED`, `INCLUDE_COUNT_EXCEEDED`, each thrown with `resourceClass` and a dotted JSON:API relationship `propertyPath` where applicable.
- Spock `CompoundSerializationSpec` covering cyclic, shared, conflicting, empty, nested, and limit-exceeding graphs with explicit include paths; reuse `Article`/`Comment`/`Person` and add access-counting (traversal-scoped), conflicting-representation, and deep-nested test models; assert produced documents pass `JsonApiDocumentWriter` validation (full linkage) and `included: []` on empty resolution.
- Refresh `jsonapi-java-jackson3/README.md` (minimal usage + agent notes for opt-in inclusion), `package-info.java`, entry-point Javadoc, and `docs/conformance.md` marking compound inclusion **supported** with the exact opt-in policy.

## Non-goals

- Sparse fieldsets; Phase 2.8 adds field selection after inclusion behavior is stable.
- Inclusion defaults hidden in annotations, serializers, persistence providers, or framework adapters.
- JPA/Hibernate initialization APIs, repositories, fetch plans, authorization, or visibility policy; callers own access-safe graph preparation and persistence behavior.
- HTTP `include` query-parameter parsing; Phase 3.1 owns transport syntax.
- Jackson 2 compound serialization; Phase 2.13 owns parity.
- Selective linkage emission (omitting off-path relationships from a resource object's `relationships` member); full linkage on selected resources remains Phase 2.2 `toResource` behavior. No new selective-`toResource` API in this milestone.

## Implementation boundaries

- Public types live in `io.github.kazemek.jsonapi.jackson3`; the engine lives in `io.github.kazemek.jsonapi.jackson3.internal`. No new production package; no import of `core.internal` or `com.fasterxml.jackson..`; ArchUnit allowlist is unchanged.
- `CompoundSerializationContext` is immutable and thread-safe; `JsonApiResourceMapper` remains safe for concurrent use once created. Existing context-free overloads behave exactly as before (`included` absent / `null`).
- Traversal reuses `MappingDefinitionCache`/`ResourceMapping` for relationship content types; it does not reflect independently. To-one and to-many both resolve content type via the same `MappingProperty.accessor().getType()` path already used in `DomainResourceWriter`. Prefer package-visible helpers on `DomainResourceWriter` for content-type / to-many detection reused by the engine; duplicating those two private helpers in the engine is acceptable if elevating visibility is undesirable.
- Include policy is checked per relationship *before* inclusion-traversal property access; path membership is checked before reading a relationship property for traversal.
- The engine produces `List<ResourceObject>` (or `null` when no include request was supplied); `JsonApiDocument` is constructed via `buildDocument` with the populated `included` list when includes were requested (`null` omits the member; empty list emits `included: []`). The writer's aggregate validation runs unchanged; no `sparseFieldsetException` is set by this milestone.
- Files to add: `CompoundSerializationContext.java`, `IncludePath.java`, `IncludePolicy.java`, `RelationshipAllowance.java` (or equivalent public typed key), `internal/CompoundInclusionEngine.java`. Files to edit: `JsonApiResourceMapper.java` (overloads + `buildDocument(..., included)`), `MappingDiagnostic.java`, optionally `internal/DomainResourceWriter.java` (package-visible helpers only; no selective-`toResource`), `jsonapi-java-jackson3/README.md`, `package-info.java` (public), `docs/conformance.md`.

## Test strategy

- Cyclic graph: `Article.comments -> Comment.author -> Person` and `Article.author -> Person` with include paths `comments.author` and `author`; assert no infinite recursion, full linkage, first-encounter order, and that off-path relationships are never read **for inclusion traversal** (access-counting fixture scoped to traversal, not linkage getters on selected resources).
- Shared graph: two relationships pointing to the same `Person` included once.
- Conflicting representations: same identity supplied with differing attributes across two paths.
- Empty resolution: include path permitted but resolving to no resources yields `included: []`.
- Nested intermediates: deep path `comments.author` includes the intermediate `Comment` resources required for full linkage.
- Limits: construct a graph exceeding `maxDepth` (path segment count from primary) and one exceeding `maxIncluded`; assert the exact diagnostic code and dotted JSON:API `propertyPath`.
- Invalid paths: segment naming a non-relationship, and a denied relationship; assert `INVALID_INCLUDE_PATH` / `DENIED_RELATIONSHIP_INCLUDE`.
- Round-trip: feed produced documents through `JsonApiDocumentWriter` and assert aggregate validation passes (full linkage, identity uniqueness, lid consistency).
- Canonical fixtures: consider adding 1-2 richer compound fixtures (nested-intermediate, shared-identity) to `fixtures/jsonapi-1.1/` and the manifest so Phase 2.5's draft-schema cross-check and Phase 2.13's Jackson 2 parity can reuse bytes rather than test-only models.

## Acceptance criteria

- [ ] No related resource enters `included` without an explicit include request; nested paths include required intermediates; off-path relationships are not accessed for inclusion traversal (access-counting fixture); first-encounter output order is deterministic.
- [ ] Deduplication, conflicting representations, cycles, maximum depth, and maximum included-resource count have stable mapping diagnostics and focused tests; the default context requests no included resources with finite depth/count limits; `@NullMarked` packages with accurate `@Nullable` per ADR-009; the ArchUnit allowlist is unchanged with no `core.internal` or Jackson 2 production dependencies.
- [ ] Generated compound documents pass `JsonApiDocumentWriter` identity/full-linkage/lid validation; `included: []` is emitted for supported but empty include requests; context-free overloads still omit `included`.
- [ ] The canonical `module-docs` checklist passes and `docs/conformance.md` marks compound inclusion **supported** with the exact opt-in policy rather than implying automatic traversal.
- [ ] `./gradlew :jsonapi-java-jackson3:test --tests '*CompoundSerializationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
