# Phase 2.3 — Jackson 3 Compound Serialization Context

> **Module:** `jsonapi-java-jackson3`  
> **Dependencies:** Phase 2.2  
> **Status:** Not started

## Goal

Add explicit, bounded compound-document inclusion without turning relationship mapping into automatic graph traversal.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) — a relationship creates linkage but never requests automatic inclusion; include paths and traversal limits remain explicit application policy.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — domain mapping and inclusion are separate operations, and persistence/lazy-loading behavior is not owned by the library.
- [JSON:API compound documents](https://jsonapi.org/format/1.1/#document-compound-documents) — included resources require full linkage and unique resource identity.
- `JsonApiDocumentValidator` — generated documents must pass duplicate-identity, local-identifier, and full-linkage validation before writing.

## Serialization context

The caller supplies an immutable context containing:

- requested include paths;
- an allow-list/policy for includable relationships;
- maximum relationship depth;
- maximum included-resource count;
- extension/profile validation context.

Defaults request no included resources and apply finite safety limits.

## Inclusion behavior

- Validate every requested path against mapping metadata and the application policy.
- Include intermediate resources required by full linkage.
- Emit `included: []` when a supported, explicitly supplied include request resolves to no resources.
- Do not include resources that were not requested.
- Deduplicate by `type`+`id`, or `type`+`lid` where applicable.
- Treat revisiting the same identity as deduplication, not infinite recursion.
- Reject conflicting representations of the same identity.
- Preserve deterministic first-encounter order.

## Safety and ORM neutrality

The mapper performs no JPA initialization and has no persistence dependency. Accessing a relationship is ordinary Jackson/property access controlled by the inclusion context. Depth, count, and cycle behavior produce structured mapping diagnostics.

## Non-goals

- Sparse fieldsets; Phase 2.8 adds field selection after inclusion behavior is stable.
- Inclusion defaults hidden in annotations, serializers, persistence providers, or framework adapters.
- JPA initialization, repositories, fetch plans, authorization, or visibility policy.

## Test strategy

- Use cyclic, shared, conflicting, empty, and nested domain graphs with explicit include paths.
- Assert full linkage, intermediate-resource inclusion, identity deduplication, first-encounter order, and no access to relationships outside requested paths.
- Cover invalid paths, denied relationships, cycles, conflicting identities, and depth/count limits with stable mapping codes and logical paths.

## Acceptance criteria

- [ ] No related resource enters `included` without an explicit include request; nested paths include required intermediates, excluded relationships are not accessed, and first-encounter output order is deterministic.
- [ ] Deduplication, conflicting identity, cycles, maximum depth, and maximum included-resource count have stable mapping diagnostics and focused tests.
- [ ] Generated compound documents pass core identity/full-linkage validation and preserve explicit extension/profile policy without persistence or framework dependencies.
- [ ] The canonical `module-docs` checklist passes and conformance documentation states the exact opt-in inclusion policy rather than implying automatic traversal.
- [ ] `./gradlew :jsonapi-java-jackson3:test` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
