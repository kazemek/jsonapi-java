# Phase 2.13 — Jackson 2 Compound Serialization

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.3 and 2.12  
> **Status:** Not started

## Goal

Port explicit, bounded compound-document inclusion to Jackson 2 with Jackson 3 semantic parity.

## Research and constraints

- Phase 2.3 defines requested include paths, allow-list policy, limits, traversal, deduplication,
  cycle handling, conflict detection, and deterministic order.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — linkage never implies inclusion,
  and persistence/lazy-loading policy remains application-owned.
- Phase 2.12 supplies Jackson 2 mapping definitions; traversal reuses them and must not rescan
  domain types.
- Shared compound fixtures, not runtime delegation, establish parity between Jackson majors.

## Deliverables

- Add the Jackson 2 immutable serialization context with include paths, relationship policy,
  maximum depth/count, and validation context.
- Port requested-path validation and relationship traversal using Phase 2.12 definitions.
- Port identity deduplication, required intermediate inclusion, cycle handling, conflicting
  representation failure, and deterministic first-encounter order.
- Run the shared compound fixture/diagnostic matrix through both major-specific artifacts.
- Refresh Jackson 2 module docs/Javadoc and conformance notes for opt-in inclusion.

## Non-goals

- Sparse fieldsets; Phase 2.14 owns them.
- Implicit inclusion, ORM/lazy-loading integration, repositories, or authorization.
- Jackson 2 flat DTO reads, envelopes, or PATCH binding.

## Implementation boundaries

- Defaults request no included resources and retain finite safety limits.
- Excluded relationships are not accessed; include policy is checked before property access.
- Production code imports no Jackson 3 or sibling internal types, and shared fixtures are not
  copied into major-specific variants.

## Test strategy

- Reuse Phase 2.3 cyclic, shared, nested, empty, conflict, limit, and invalid-path domain graphs.
- Compare included resources, order, linkage, and stable diagnostics across Jackson 2 and 3.

## Acceptance criteria

- [ ] Explicit include requests produce the same included resources, intermediates, linkage, and
      first-encounter order as Phase 2.3.
- [ ] Policy rejection, conflicts, cycles, depth/count limits, and invalid paths have parity
      diagnostics, and excluded relationships are not accessed.
- [ ] Jackson 2 compound code has no Jackson 3/runtime or sibling-internal dependency.
- [ ] The canonical `module-docs` checklist passes and conformance documentation records exact
      opt-in Jackson 2 inclusion behavior.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*CompoundSerializationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
