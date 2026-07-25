# Phase 2.3 — Compound Serialization Context

> **Module:** `jsonapi-java-jackson`  
> **Dependencies:** Phase 2.2  
> **Status:** Not started

## Goal

Add explicit, bounded compound-document inclusion and sparse fieldsets without turning relationship mapping into automatic graph traversal.

## Serialization context

The caller supplies an immutable context containing:

- requested include paths;
- sparse fieldsets by resource type;
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

## Sparse fieldsets

- Apply fieldsets to primary and included resources by resource type.
- Never emit fields outside an explicitly restricted fieldset.
- Preserve `type` and resource identity independently of field selection.
- Coordinate omitted relationship linkage with JSON:API's full-linkage exception.
- Apply fieldsets before traversal so excluded relationships are not accessed unnecessarily.

## Safety and ORM neutrality

The mapper performs no JPA initialization and has no persistence dependency. Accessing a relationship is ordinary Jackson/property access controlled by the inclusion context. Depth, count, and cycle behavior produce structured mapping diagnostics.

## Acceptance criteria

- [ ] No related resource enters `included` without an explicit include request or documented application default.
- [ ] Nested paths include required intermediate resources.
- [ ] Deduplication, conflicting identity, cycles, depth, and count limits are tested.
- [ ] Sparse fieldsets apply consistently to primary and included resources.
- [ ] Excluded relationships are not traversed.
- [ ] Output order is deterministic.
- [ ] `./gradlew :jsonapi-java-jackson:test` passes.
