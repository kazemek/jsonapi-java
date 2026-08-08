# Phase 1.6 — Links Additional Member Conformance

> **Module:** `jsonapi-java-core`
> **Packages:** `io.github.kazemek.jsonapi.core.model`, `io.github.kazemek.jsonapi.core.internal`
> **Dependencies:** Phase 1.1, Phase 1.5
> **Status:** Complete

## Goal

Reject context-standard link relation names in `Links.additionalMembers` at construction so those
names cannot appear as open JSON pass-through and flatten/write cannot emit non-link values under
standard link keys.

## Research and constraints

- [JSON:API 1.1 links](https://jsonapi.org/format/1.1/#document-links) — within a links object, a
  link MUST be a URI-reference string, a link object, or `null`. Consequence: standard link names
  must not be storable as arbitrary open JSON in `additionalMembers`.
- Context-specific standard link members (same source, plus resource / relationship / error
  sections): top-level `self` / `related` / `describedby` / pagination; resource `self`;
  relationship `self` / `related` / pagination; error `about` / `type`. Consequence: reserve the
  union of these names at construction because `Links` has no `LinksContext` on the factory path.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — local invariants belong in the
  public construction path via stable `ValidationRuleCode` and JSON Pointer-like paths; keep local
  vs aggregate separation; do not extract a new validation service.
- Existing pattern: [`Relationship`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/model/Relationship.java)
  and [`Link.ObjectLink`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/model/Link.java)
  reject dedicated names via [`AdditionalMembers.copy`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/internal/AdditionalMembers.java)
  + `RESERVED_FIELD_NAME`. [`Links`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/model/Links.java)
  currently validates additional names with `MemberNames` only and already rejects key collisions
  between the typed `links` map and `additionalMembers`.
- Jackson [`MemberClassifier`](../../jsonapi-java-jackson3/src/main/java/io/github/kazemek/jsonapi/jackson3/internal/MemberClassifier.java)
  already routes only `@` members into `Links.additionalMembers` on read; do not change codecs.
- [`docs/conformance.md`](../../docs/conformance.md) “Reserved dedicated members in additional maps”
  lists document, resource, identifier, relationship, error, jsonapi, link, source — add `Links`.
- No new ADR. Nullness unchanged. Stricter local construction / agent-relevant invariant, so refresh
  `jsonapi-java-core` docs per the `module-docs` skill (do not copy its checklist into this file).

## Deliverables

1. **`Links` reserved additional members:** reserve the union of all `LinksContext` standard names
   (`self`, `related`, `describedby`, `first`, `last`, `prev`, `next`, `about`, `type` via
   `JsonApiMembers`) and reject them in `additionalMembers` with `RESERVED_FIELD_NAME` at `/links/<name>`,
   reusing `AdditionalMembers.copy` (or equivalent `OrderedMaps.rejectReservedNames`) while keeping
   existing `MemberNames` checks, `@` pass-through, and map collision rejection.
2. **Focused Spock coverage** in `LinkSpec` (which already hosts `Links` construction tests): each
   reserved name rejected in `additionalMembers`; `@context` still accepted; reserved names still
   allowed in the typed `links()` map; map-key collision still rejected for a non-reserved
   overlapping key (for example `ext:custom` in both maps). Reserved names present only in
   `additionalMembers`, or in both maps, fail as `RESERVED_FIELD_NAME` (reserved check runs before
   collision).
3. **Conformance row** in `docs/conformance.md` updating reserved-additional coverage to include
   `Links`.
4. **Module docs** for `jsonapi-java-core` per the `module-docs` skill: agent/invariant notes that
   standard link relation names are reserved out of `Links.additionalMembers`.

## Non-goals

- Changing `LinksContext`, aggregate link-context rules, or `JsonApiDocumentValidator` link policy.
- Forcing extension members onto only one channel, or changing `@`-member open-JSON value semantics.
- Changing the typed `links()` map contract (`Link` values, including extension relation keys).
- Jackson reader/writer changes, new packages, new validation services, new dependencies, or a new
  ADR.
- Phase 4.1 conformance/hardening umbrella work; Spring / Jackson 2 modules.

## Implementation boundaries

- Touch `jsonapi-java-core` only: `Links` construction, `LinkSpec`, `docs/conformance.md`, and
  `jsonapi-java-core` module docs required by `module-docs`.
- Prefer constructor/`Links.of` local validation only; do not redesign `Links`’ public shape or move
  context into the factory API.
- Public API surface unchanged except stricter rejection of reserved names in `additionalMembers`.

## Test strategy

- Negative: `Links.of(Map.of(), Map.of("<reserved>", value))` for each reserved name throws
  `JsonApiValidationException` with `ruleCode == RESERVED_FIELD_NAME` and pointer under `/links/…`
  (mirror `LinkSpec` / `RelationshipSpec` reserved-name conventions).
- Positive: `@context` (and other `@` members) still construct via `additionalMembers`; `self` /
  `related` / pagination / `about` / `type` remain valid keys in the typed `links()` map with
  `Link` values.
- Regression: overlapping non-reserved key in both maps (for example `ext:custom`) still fails with
  the existing collision rule; reserved dual-map inputs fail as `RESERVED_FIELD_NAME` instead.

## Acceptance criteria

- [x] `Links.additionalMembers` rejects the union of context-standard link names with
      `RESERVED_FIELD_NAME`; `@` pass-through and typed `links()` map behavior remain intact.
- [x] `docs/conformance.md` documents `Links` in reserved dedicated members in additional maps.
- [x] The canonical `module-docs` checklist passes for `jsonapi-java-core`.
- [x] `./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.model.LinkSpec'`
      passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, report Sonar
      blocked and that CI must still pass the gate.
