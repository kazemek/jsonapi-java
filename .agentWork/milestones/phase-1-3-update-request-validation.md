# Phase 1.3 — Resource Update Request Validation

> **Module:** `jsonapi-java-core`
> **Packages:** `io.github.kazemek.jsonapi.core.validation`
> **Dependencies:** Phase 1.1
> **Status:** Complete

## Goal

Validate the JSON:API 1.1 single-resource update-document contract as a new `UPDATE_REQUEST`
document usage, preserving every presence state (absent vs explicit `null`, single vs collection,
link-only vs data-bearing) needed by later PATCH binding, and optionally compare the document
resource identity against a caller-supplied expected endpoint identity.

## Research and constraints

- [JSON:API 1.1 updating resources](https://jsonapi.org/format/1.1/#crud-updating) — a resource
  PATCH contains one resource object with `type` and `id`; omitted attributes/relationships retain
  current values, and every supplied relationship must contain replacement `data`.
- [ADR-012](../../docs/adr/012-resource-patch-binding.md) — core validates update shape and endpoint
  identity; adapters bind commands and applications apply them. The core contract this milestone
  implements is recorded at lines 23–33.
- [ADR-002](../../docs/adr/002-document-representation.md) — nullable containing members, nullable
  map values, and sealed relationship linkage already preserve the required absence/null states.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — update rules extend aggregate
  validation; they do not add mutable or bypass construction paths.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — `@NullMarked` packages and `@Nullable` for
  absence/null-preserving values; NullAway enforces this on `main` sources.
- `DocumentUsage` and `ValidationContext` own context-sensitive request rules. The update contract
  must compose with extension/profile, links, and full-linkage policies already implemented in
  `JsonApiDocumentValidator`.

## Design decisions (locked during planning)

- **D1 — Endpoint identity representation:** Add a new
  `record EndpointIdentity(String type, String id)` to `io.github.kazemek.jsonapi.core.validation`
  and carry it as one `@Nullable EndpointIdentity expectedEndpointIdentity` component on
  `ValidationContext` with a `withExpectedEndpointIdentity(@Nullable EndpointIdentity)` wither.
  `ValidationContext.defaults()` passes `null` (identity comparison off). This reuses the
  established wither pattern (`ValidationContext.java:68–99`) and matches the milestone's
  "optional caller-supplied expected endpoint `type`+`id`" wording exactly.
- **D2 — Rule-code strategy:** Add three new codes and reuse `RESOURCE_ID_REQUIRED` for the
  missing-update-identifier case. The existing `validateResourceIdentity`
  (`JsonApiDocumentValidator.java:290–301`) already throws `RESOURCE_ID_REQUIRED` at `/data/id`
  for any non-create usage, so `UPDATE_REQUEST` (which is non-create) inherits id-absent and
  lid-only rejection for free. The three new codes are listed under Deliverable 3 below.

## Deliverables

1. **Usage enum and update context.** Add `UPDATE_REQUEST` to `DocumentUsage` (`DocumentUsage.java`).
   Add the `@Nullable EndpointIdentity expectedEndpointIdentity` record component to
   `ValidationContext` plus a `withExpectedEndpointIdentity(@Nullable EndpointIdentity)` wither;
   `defaults()` passes `null`; the three existing withers (`withDocumentUsage`,
   `withLinksContext`, `withSparseFieldsetException`) pass the current endpoint identity through.
   Add `record EndpointIdentity(String type, String id)` to `core.validation` with a compact
   constructor that rejects null `type`/`id` via `LocalValidation.requireNonNull` at
   `/endpointIdentity/type` and `/endpointIdentity/id`. The package `core.validation` is already
   `@NullMarked` (`package-info.java`). Because adding a record component changes the canonical
   constructor signature, the ~25 external `new ValidationContext(...)` call sites across three
   modules (core tests: `JsonApiDocumentValidatorSpec.groovy` and `ValidatorCoverageSpec.groovy`,
   `jsonapi-java-test-fixtures` `Models.groovy`, and the `jsonapi-java-jackson3` reader spec
   `DocumentReaderSpec.groovy`) plus 4 internal to `ValidationContext` itself must pass `null` for
   the trailing `expectedEndpointIdentity` component (compiler-driven; `defaults()` and the existing
   withers carry it through).

2. **Primary and per-resource update rules** in `JsonApiDocumentValidator`, scoped to the primary
   update resource only so `included` resources and their relationships run only the existing
   non-update paths:
   - **Primary shape (two sites)** — `validate()` dispatches `validatePrimaryData` only when
     `document.data() != null` (`JsonApiDocumentValidator.java:66`), so absent `data` (a valid
     meta-only / errors-only / extension-only document) is unreachable at the planned primary-data
     hook. Split the check across two sites:
     - At the primary-data dispatch site in `validate()` (`JsonApiDocumentValidator.java:66`),
       cover the **absent-`data`** case: when
       `document.data() == null && context.documentUsage() == UPDATE_REQUEST` throw
       `ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE` at `PATH_DATA` (`/data`).
     - In `validatePrimaryData` after the `data == null` early-return
       (`JsonApiDocumentValidator.java:84`), cover the **non-single** forms: when
       `context.documentUsage() == UPDATE_REQUEST && !(data instanceof DocumentData.SingleResource)`
       throw `ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE` at `PATH_DATA`. The early-return
       stays as defensive code; the remaining `SingleResource` case (`JsonApiDocumentValidator.java:91`)
       then proceeds.
   - **Relationship `data` (primary scoped)** — add a new check in
     `validateResourceRelationships` (which holds the resource `path`, `JsonApiDocumentValidator.java:134`),
     inside the existing single loop, guarded by
     `context.documentUsage() == UPDATE_REQUEST && PATH_DATA.equals(path) && !entry.getValue().hasDataMember()`
     → throw `ValidationRuleCode.RELATIONSHIP_DATA_REQUIRED` at
     `JsonPointers.child(path + PATH_RELATIONSHIPS, entry.getKey()) + PATH_DATA`
     (`/data/relationships/<name>/data`). `PATH_DATA.equals(path)` is true only for the primary
     resource (`included` is `/included/<i>`), so included link-only relationships are never
     rejected and the check short-circuits before `validateRelationship` for primary link-only
     relationships (so they surface `RELATIONSHIP_DATA_REQUIRED`, not `MISSING_RELATIONSHIP_MEMBER`).
   - **Endpoint identity (primary scoped)** — add `validateUpdateEndpointIdentity(resource, path, context)`
     and call it from `validateResource` (`JsonApiDocumentValidator.java:109`) immediately after
     `validateResourceIdentity`, guarded by `usage == UPDATE_REQUEST && PATH_DATA.equals(path)` so
     included identities are never compared to the endpoint. Only compare when
     `context.expectedEndpointIdentity() != null`: type mismatch → `ENDPOINT_IDENTITY_MISMATCH` at
     `path + "/type"`; id mismatch → `ENDPOINT_IDENTITY_MISMATCH` at `path + "/id"`.
   - **No change** for id/lid: the existing non-create branch in `validateResourceIdentity`
     (`JsonApiDocumentValidator.java:292–300`) requires `id` outside create context, so
     `UPDATE_REQUEST` inherits id-absent and lid-only rejection (`RESOURCE_ID_REQUIRED` at
     `/data/id`) for the primary resource and any `included` resources.

3. **Stable rule codes and JSON paths.** Add three codes to `ValidationRuleCode` (alphabetized
   per existing order):

   | Code                              | Path                                | Trigger                                                                                       |
   |-----------------------------------|-------------------------------------|-----------------------------------------------------------------------------------------------|
   | `ENDPOINT_IDENTITY_MISMATCH`      | `/data/type` or `/data/id`          | Optional expected endpoint identity supplied and the body `type` or `id` does not match.       |
   | `RELATIONSHIP_DATA_REQUIRED`      | `/data/relationships/<name>/data`  | `UPDATE_REQUEST` and a supplied relationship has no `data` member (`relationship.data() == null`). |
   | `UPDATE_REQUIRES_SINGLE_RESOURCE` | `/data`                             | `UPDATE_REQUEST` and primary data is absent, `NullData`, a resource collection, or any identifier primary data. |

   Reuse `RESOURCE_ID_REQUIRED` at `/data/id` for id-absent and lid-only resources (already
   enforced by `validateResourceIdentity`). A supplied relationship is one present in
   `Relationships.relationships()`; if `relationships` itself is absent or present-empty the rule
   does not apply.

4. **Conformance and module documentation.** Add a new
   `## Resource update request validation (Phase 1.3 — supported)` section to `docs/conformance.md`
   with rows marking the four supported rules above plus the presence-preservation rows; mark
   command application **deferred** (Phases 2.11/2.17) and HTTP/route identity derivation
   **out of scope** (application-owned). Reconcile the existing `docs/conformance.md:87` row
   ("Presence-aware resource-update commands — deferred — Phases 1.3, 2.11, and 2.17") to
   "Core update validation supported (Phase 1.3); command binding deferred to Phases 2.11 and
   2.17". Update the intro (`docs/conformance.md:5–9`) to include Phase 1.3 as a seeding milestone.
   Use the `module-docs` skill to refresh `jsonapi-java-core/README.md` (one sentence in the
   validate-flow paragraph: update requests use `UPDATE_REQUEST` and an optional expected endpoint
   identity for body/endpoint identity checks; HTTP/route derivation and mutation stay
   application-owned — already scoped by Non-goals, lines 25–27) and
   `io/github/kazemek/jsonapi/core/validation/package-info.java` (extend the Javadoc with a clause
   on update-request usage and optional endpoint-identity comparison).

5. **Focused Spock spec** `jsonapi-java-core/src/test/groovy/io/github/kazemek/jsonapi/core/validation/UpdateRequestValidationSpec.groovy`.
   Build documents programmatically in Groovy (no JSON parsing in core; mirror the
   `thrown(JsonApiValidationException)` + `ruleCode()` + `jsonPointer()` style at
   `JsonApiDocumentValidatorSpec.groovy:35–47` and matrix `where:` tables). Cases are listed under
   Test strategy below.

## Non-goals

- Parsing HTTP methods, request URLs, route variables, headers, or media types.
- Binding update members to DTO properties; Phase 2.11 owns Jackson 3 patch commands.
- Applying updates, authorization, persistence, transaction behavior, or relationship endpoints.
- JSON Merge Patch, JSON Patch, bulk updates, atomic operations, or create semantics.
- Making links, metadata, extension/profile members, or `included` patchable properties.
- A dedicated `UpdateValidationContext` type or a new `JsonApiDocumentValidator` entry point.
- Any `settings.gradle.kts` change, new submodule, or new ADR (ADR-012 already
  records the consequential decision). The milestone-index status flip to `Complete` on delivery is
  a repository workflow step, not implementation scope.

## Implementation boundaries

- Update validation is an additional `JsonApiDocumentValidator` path using public model states;
  it must not alter constructor validity for response, create, or other document usages.
- The `validate()` sequence (additional members → meta → jsonapi → links → errors → primary →
  compound) is preserved. The only structural change is one `UPDATE_REQUEST` absent-data guard at
  the primary-data dispatch site (`JsonApiDocumentValidator.java:66`); all other update rules are
  scoped to the primary resource (path exactly `/data`), so `included` resources and their
  relationships run only the existing non-update paths. `CREATE_REQUEST` and `RESPONSE_OR_OTHER`
  behavior must not regress; the compound-non-regression checks below prove this.
- `DocumentData.NullData` remains meaningful for general documents but is invalid here because an
  update request requires `SingleResource`.
- `UPDATE_REQUEST` is non-create, so the inherited `validateResourceIdentity` `id` requirement
  applies to the primary resource **and** any `included` resources; nested create-via-PATCH of
  lid-only `included` resources is out of scope (consistent with the create-semantics non-goal).
- Under `UPDATE_REQUEST`, relationship linkage identifiers also inherit the non-create `id`
  requirement via `validateIdentifier` (`JsonApiDocumentValidator.java:278`); a lid-only linkage
  identifier in a primary relationship's `data` is rejected with `RESOURCE_ID_REQUIRED` at
  `/data/relationships/<name>/data/id` (single linkage) or `…/data/<i>/id` (collection element),
  consistent with non-create (response) semantics.
- The primary-relationship `data` check fires at the top of the `validateResourceRelationships`
  loop, before per-relationship additional-member validation (`validateRelationship` →
  `validateAdditionalMembers`), so a data-less primary relationship carrying a disallowed extension
  member reports `RELATIONSHIP_DATA_REQUIRED`, not `DISALLOWED_ADDITIONAL_MEMBER` — deliberate
  update-contract-first fail-fast ordering.
- An absent `attributes`/`relationships` wrapper and a present-empty wrapper both request no
  changes, but remain structurally distinct in the returned model. Within attributes, key absence
  differs from a present null value (`Attributes` is a null-preserving ordered map;
  `Attributes.empty()` is present-empty).
- A supplied relationship may also contain links, meta, or valid additional members, but it must
  contain `data`; explicit `NullLinkage` and an empty `IdentifierCollectionLinkage` are valid
  replacements.
- Expected endpoint identity comparison runs only when supplied and compares JSON:API `type` and
  id-kind identity. HTTP adapters/applications remain responsible for deriving that expectation.
- `registerLinkageFromResource` (`JsonApiDocumentValidator.java:432–434`) already skips
  relationships with `data() == null`, so the new relationship rule composes with full-linkage
  validation without conflict.
- `CoreDependencyRulesSpec` (ArchUnit, ADR-010) already permits JDK + JSpecify + self-module
  dependencies for `core.validation`; new types in the same package require no allowlist change.

## Test strategy

`UpdateRequestValidationSpec` (one spec, programmatic fixtures, fail-fast exception assertions).
The four coverage groups below collectively prove the goal; each case asserts exact rule code and
JSON pointer path unless it is an accepted case.

- **Primary-data variants:** `data` absent; `NullData`; singleton `ResourceCollection`;
  multi-element `ResourceCollection`; `SingleIdentifier`; `IdentifierCollection` — each throws
  `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`. Explicit **errors-only** and **meta-only** documents
  under `UPDATE_REQUEST` are also rejected with `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`
  (documents the JSON:API §7.2 strictness edge — a data-less PATCH body — beyond the generic
  "data absent" case; intentional per ADR-012 "rejects absent data"). A single `ResourceObject`
  with `type`+`id` is accepted.
- **Identifier state:** id-absent resource → `RESOURCE_ID_REQUIRED` at `/data/id`; lid-only
  resource → `RESOURCE_ID_REQUIRED` at `/data/id`; id present with and without a lid → accepted.
- **Relationships:** `relationships` absent → accepted; present-empty → accepted; relationship with
  `data` — `NullLinkage`, `SingleLinkage`, empty `IdentifierCollectionLinkage`, non-empty
  `IdentifierCollectionLinkage` → each accepted; relationship with `data` plus links/meta/permitted
  extension members → accepted; relationship without `data` (self-link-only, meta-only,
  extension-only) → `RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`; a lid-only
  linkage identifier in a primary relationship's `data` (single or collection element) →
  `RESOURCE_ID_REQUIRED` at `/data/relationships/<name>/data/id` or `…/data/<i>/id` (inherited
  non-create `validateIdentifier` id requirement).
- **Attributes:** absent; present-empty; present with a value; present with an explicit `null`
  value key (verified by read-back equality) — all survive validation unchanged.
- **Endpoint identity:** no expected supplied → accepted; expected matches → accepted; type
  mismatch → `ENDPOINT_IDENTITY_MISMATCH` at `/data/type`; id mismatch →
  `ENDPOINT_IDENTITY_MISMATCH` at `/data/id`.
- **Composition and non-regression:** an update document with a context-allowed extension member →
  accepted; an update document with `included` → full-linkage validation runs; an id-less create
  document stays valid under `CREATE_REQUEST`; a `NullData` document stays valid under
  `RESPONSE_OR_OTHER`; an update document with a present-`data` relationship still runs links, meta,
  and full-linkage checks under `UPDATE_REQUEST` (`validateLinksOnlyRelationshipQualification`
  correctly no-ops when `data() != null`).
- **Compound scoping:** a compound update document (primary `SingleResource` + `included` resource
  with a link-only relationship) is accepted under `UPDATE_REQUEST`; the included link-only
  relationship is not rejected with `RELATIONSHIP_DATA_REQUIRED`, and the included identity is not
  compared to the endpoint (no `ENDPOINT_IDENTITY_MISMATCH`).

## Acceptance criteria

- [ ] `UPDATE_REQUEST` accepts exactly valid single-resource update documents and rejects every
      other primary-data state (`NullData`, resource collections, both identifier forms, absent
      data) with `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`; id-absent and lid-only resources are
      rejected with `RESOURCE_ID_REQUIRED` at `/data/id`.
- [ ] Omitted, present, and present-null attribute keys and omitted, present-empty, and present
      relationship wrappers survive validation unchanged, and every supplied relationship requires
      replacement `data` (`RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`); all
      four `RelationshipData` linkage variants are accepted. The relationship `data` requirement is
      scoped to the primary resource — `included` link-only relationships remain accepted.
- [ ] Optional endpoint identity matching accepts matching `type`+`id`, rejects `type` mismatch at
      `/data/type` and `id` mismatch at `/data/id` with `ENDPOINT_IDENTITY_MISMATCH`, and stays off
      when no expected identity is supplied, without adding HTTP concerns to core. Endpoint-identity
      comparison is scoped to the primary resource — `included` identities are not compared to the
      endpoint.
- [ ] New `ValidationContext` and `EndpointIdentity` components satisfy ADR-009 nullness
      (`@NullMarked` package; accurate `@Nullable` on `expectedEndpointIdentity`), and the
      canonical `module-docs` checklist passes; conformance documentation marks only core update
      validation **supported** (command binding stays deferred).
- [ ] `./gradlew :jsonapi-java-core:test --tests '*UpdateRequestValidationSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes via the `sonar-quality-gate` skill; if `SONAR_TOKEN` is
      unavailable, report Sonar blocked and that CI must still pass the gate.