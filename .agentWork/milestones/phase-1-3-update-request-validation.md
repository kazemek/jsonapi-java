# Phase 1.3 — Resource Update Request Validation

> **Module:** `jsonapi-java-core`  
> **Dependencies:** Phase 1.1  
> **Status:** Not started

## Goal

Validate the JSON:API 1.1 single-resource update-document contract while preserving every
presence state needed by later PATCH binding.

## Research and constraints

- [JSON:API 1.1 updating resources](https://jsonapi.org/format/1.1/#crud-updating) — a resource
  PATCH contains one resource object with `type` and `id`; omitted attributes/relationships retain
  current values, and every supplied relationship must contain replacement `data`.
- [ADR-012](../../docs/adr/012-resource-patch-binding.md) — core validates update shape and
  endpoint identity, while adapters bind commands and applications apply them.
- [ADR-002](../../docs/adr/002-document-representation.md) — nullable containing members, nullable
  map values, and sealed relationship linkage already preserve the required absence/null states.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — update rules extend aggregate
  validation; they do not add mutable or bypass construction paths.
- `DocumentUsage` and `ValidationContext` own context-sensitive request rules. The update contract
  must compose with extension/profile, links, and full-linkage policies.

## Deliverables

- Add an `UPDATE_REQUEST` document usage and immutable update validation context with an optional
  caller-supplied expected endpoint `type`+`id`.
- Validate that primary data is present and is exactly one `ResourceObject` with `type` and `id`;
  reject `NullData`, collections, resource identifiers, and lid-only resources with stable codes.
- Validate every supplied relationship in the primary resource has a `data` member while
  preserving null, single, empty collection, and non-empty collection linkage.
- Add stable rule codes and exact document paths for invalid primary shape, missing update
  identifier, missing relationship data, and endpoint/body identity mismatch.
- Refresh core Javadoc, module docs, and conformance entries for update validation and its
  application-owned HTTP/mutation boundaries.

## Non-goals

- Parsing HTTP methods, request URLs, route variables, headers, or media types.
- Binding update members to DTO properties; Phase 2.11 owns Jackson 3 patch commands.
- Applying updates, authorization, persistence, transaction behavior, or relationship endpoints.
- JSON Merge Patch, JSON Patch, bulk updates, atomic operations, or create semantics.
- Making links, metadata, extension/profile members, or `included` patchable properties.

## Implementation boundaries

- Update validation is an additional `JsonApiDocumentValidator` path using public model states;
  it must not alter constructor validity for response, create, or other document usages.
- `DocumentData.NullData` remains meaningful for general documents but is invalid here because an
  update request requires `SingleResource`.
- An absent `attributes`/`relationships` wrapper and a present-empty wrapper both request no
  changes, but remain structurally distinct in the returned model. Within attributes, key absence
  differs from a present null value.
- A supplied relationship may also contain links, meta, or valid additional members, but it must
  contain `data`; explicit `NullLinkage` and an empty identifier collection are valid replacements.
- Expected endpoint identity comparison runs only when supplied and compares JSON:API `type` and
  id-kind identity. HTTP adapters/applications remain responsible for deriving that expectation.

## Test strategy

- Add focused Spock matrices for every primary-data variant, missing/present identifiers,
  absent/empty/non-empty attributes and relationships, null attributes, and all linkage variants.
- Cover relationship objects with and without `data`, including link/meta/additional combinations,
  and assert exact rule code/path results.
- Prove update usage composes with existing extension/profile and full-linkage validation and does
  not change create or response behavior.

## Acceptance criteria

- [ ] Update usage accepts exactly valid single-resource update documents and rejects every other
      primary-data state with stable rule codes and paths.
- [ ] Omitted/present/null attribute and relationship states survive validation unchanged, and
      every supplied relationship requires replacement `data`.
- [ ] Optional endpoint identity matching covers success plus type/id mismatch without adding HTTP
      concerns to core.
- [ ] Public update context APIs satisfy ADR-009 nullness, the canonical `module-docs` checklist
      passes, and conformance documentation marks only core update validation **supported**.
- [ ] `./gradlew :jsonapi-java-core:test --tests '*UpdateRequestValidationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
