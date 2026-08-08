# Phase 1.4 — Core Identity and Linkage Hardening

> **Module:** `jsonapi-java-core`
> **Packages:** `io.github.kazemek.jsonapi.core.validation`
> **Dependencies:** Phase 1.1, Phase 1.3
> **Status:** Complete

## Goal

Close the justified post–Phase 1.1 identity/linkage gaps: reject cross-alias duplicates in
identifier collections after id↔lid binding, lock cyclic and shared-included full-linkage
regressions in tests, and document representation-strict duplicate policy via module docs.

## Research and constraints

- [JSON:API 1.1 resource linkage](https://jsonapi.org/format/1.1/#document-resource-object-linkage)
  and [compound documents](https://jsonapi.org/format/1.1/#document-compound-documents) — linkage
  arrays must not repeat the same resource identity; full linkage is transitive; cycles are allowed
  when every included resource remains reachable.
- [JSON:API 1.1 local identifiers](https://jsonapi.org/format/1.1/#document-resource-object-local-identifiers)
  — id and lid may appear together; partners must be consistent. Consequence: uniqueness must use
  canonical aliases after `IdentityRegistry` binding, not only raw id/id or lid/lid pairs.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — aggregate validation and defensive
  copies; keep local vs document separation; do not extract new service layers for this fix.
- In-scope hardening (inline): alias-aware uniqueness for identifier collections; keep
  representation-strict `ResourceObject.equals` duplicates and document them; add cyclic and
  multi-primary shared-included full-linkage regressions. Deferred: coarser `/included` pointers,
  wrapper-accessor immutability lock-in, nested unknown-member policy expansions, Phase 4.1.
- Existing `ensureUniqueIdentifierIdentities` (`JsonApiDocumentValidator.java`) and private
  `IdentityRegistry` already bind id↔lid and enforce `ResourceObject.equals` on resource
  re-registration; reuse `DUPLICATE_RESOURCE_IDENTITY` (no new rule code). Cross-alias fixtures must
  establish binding **outside** the target collection (a primary/included resource with both id and
  lid, or a dual id+lid identifier in a **different** array), then place id-only and lid-only
  partners in the collection under test. A dual binder **inside** the same array is already rejected
  by the early raw id/lid scan and does not prove the post-registry check.

## Design decisions (locked during planning)

- **D1 — Representation-strict duplicates:** Keep rejecting same canonical identity with unequal
  `ResourceObject` representations via `equals`. Document this as intentional library policy in
  `docs/conformance.md` (update the Duplicate resource identities row). Do not open a new ADR
  unless a future milestone flips to identity-only uniqueness.
- **D2 — Cross-alias collection check timing:** After `IdentityRegistry` registration in
  `validateCompoundDocument` (primary resources, linkage identifiers, and included resources),
  re-walk primary `IdentifierCollection` and every relationship `IdentifierCollectionLinkage`
  under **primary and included** resources **before** any early return that skips full linkage
  (`included == null` or `sparseFieldsetException()`). Canonical uniqueness is not conditioned on
  full linkage and must run for documents with absent `included`. Ensure each identifier’s
  `canonicalIdentity` (or the identity itself when unbound) appears at most once per array. Keep
  the existing early raw id/lid scan for fast local duplicates.
- **D3 — No structural refactor:** Do not extract `IdentityRegistry`, split the validator, add
  builders, or redesign sealed data hierarchies.

## Deliverables

1. **Canonical uniqueness for identifier collections.** In `JsonApiDocumentValidator`, after the
   registry is populated in `validateCompoundDocument` and **before** the full-linkage early return,
   reject cross-alias duplicates in primary and relationship identifier collections (primary +
   included resources) via `DUPLICATE_RESOURCE_IDENTITY` at the second occurrence’s pointer
   (`/data/{i}` or `/…/relationships/{name}/data/{i}`).
2. **Identity/linkage regression tests** in `JsonApiDocumentValidatorSpec`: (a) primary
   `IdentifierCollection` with id-only and lid-only partners whose binding was established
   **outside** that collection; (b) relationship `IdentifierCollectionLinkage` with the same
   external-binding pattern (under a primary or included resource); (c) cyclic included graph
   validates; (d) multi-primary shared included validates (mirror
   `fixtures/jsonapi-1.1/documents/compound-shared-identity.json`).
3. **Conformance note** for representation-strict duplicate detection and alias-aware collection
   uniqueness on the Duplicate resource identities row in `docs/conformance.md` (Full linkage row
   only if wording must mention cyclic/shared coverage).
4. **Module docs** for `jsonapi-java-core` per the `module-docs` skill: agent invariant for
   representation-strict and alias-aware duplicate detection; align `validation/package-info.java`
   and entry-point Javadoc if validate-flow wording must mention the stricter collection check.

## Non-goals

- Extracting or publicizing `IdentityRegistry`; splitting `JsonApiDocumentValidator`.
- Changing `ResourceObject.equals`-based resource duplicate semantics to identity-only.
- Refining `FULL_LINKAGE_VIOLATION` pointers to per-orphan indexes.
- Wrapper-accessor immutability regressions and nested `UNKNOWN_ADDITIONAL_MEMBER` policy
  expansions (follow-up hardening; not required to complete this milestone).
- Phase 4.1 fuzz/limits/security/benchmarks; Jackson/Spring modules; builders; new rule codes.

## Implementation boundaries

- Touch only `jsonapi-java-core` validation (and tests), `JsonApiDocumentValidator.java`,
  `docs/conformance.md`, and `jsonapi-java-core` module docs (`README.md`, package-info, entry-point
  Javadoc as required by `module-docs`).
- Public API surface unchanged except documented behavior for previously accepted cross-alias
  duplicates in identifier collections (stricter validation).
- No new packages, modules, or ADRs.

## Test strategy

- Negative (both required): primary `IdentifierCollection` and relationship
  `IdentifierCollectionLinkage` containing an id-only identifier and a lid-only identifier that
  resolve to the same canonical identity via binding established **outside** that collection (e.g.
  an included or primary `ResourceObject` with both id and lid, or a dual id+lid identifier in a
  different array)—not a dual binder inside the same array → `DUPLICATE_RESOURCE_IDENTITY`. Use
  `DocumentUsage.CREATE_REQUEST` when any identifier is lid-only (otherwise `RESOURCE_ID_REQUIRED`
  fires first), matching existing lid-collection specs.
- Positive: cyclic A↔B included reachable from primary; two primary resources sharing one included
  (mirror the shared-identity fixture shape).

## Acceptance criteria

- [x] Cross-alias partner duplicates throw `DUPLICATE_RESOURCE_IDENTITY` after registry binding for
      both primary `IdentifierCollection` and relationship `IdentifierCollectionLinkage`, using
      fixtures whose binding is established outside the collection under test; raw id/id and lid/lid
      duplicates still fail.
- [x] Cyclic included and multi-primary shared-included documents validate successfully under
      `JsonApiDocumentValidator`.
- [x] `docs/conformance.md` states representation-strict duplicates and alias-aware collection
      uniqueness.
- [x] The canonical `module-docs` checklist passes for `jsonapi-java-core`.
- [x] `./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.validation.JsonApiDocumentValidatorSpec'`
      passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, report Sonar
      blocked and that CI must still pass the gate.
