# Phase 2.20 — Jackson 2 Sparse Fieldsets

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.8, 2.11, 2.19, and 2.25  
> **Status:** Not started

## Goal

Port explicit sparse-fieldset application to Jackson 2 without changing inclusion or validation
semantics, using common field policies and shared fieldset-write fixtures.

## Research and constraints

- Phase 2.8 defines fieldsets by final JSON:API resource type/member names, explicit application
  allow-list policy, pre-access filtering, and the scoped full-linkage exception.
- Phase 2.11 places `FieldPolicy`, fieldset members of `CompoundSerializationContext`, and related
  diagnostics in the common package.
- Phase 2.19 supplies Jackson 2 inclusion traversal; Phase 2.25 owns the shared sparse-fieldset
  scenario catalog.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — fieldsets and inclusion share
  explicit context but remain distinct policies.
- Phase 2.18 mapping definitions remain authoritative; fieldsets cannot rescan domain properties.

## Deliverables

- Extend Jackson 2 mapped-document overloads to apply common validated per-resource-type fieldsets
  and application field policy.
- Filter mapped attributes and relationships by final JSON:API names for primary and included
  resources while retaining resource identity.
- Apply relationship filtering before property access and compound traversal.
- Consume Phase 2.25 sparse-fieldset scenarios and port scoped sparse-full-linkage validation
  behavior.
- Use `module-docs` to refresh Jackson 2 docs/Javadoc and conformance notes for fieldset behavior
  and delegated authorization/query parsing.

## Non-goals

- Parsing HTTP `fields[...]` parameters or authorizing fields implicitly.
- Changing Jackson logical names, include policy, or graph traversal defaults.
- DTO reads, typed envelopes, or PATCH commands.
- Redefining common field-policy types under the `jackson2` package.

## Implementation boundaries

- Unknown resource types/fields and disallowed fields fail with stable Phase 2.8-equivalent
  diagnostics from the common contract.
- The full-linkage exception is enabled only when an actually applied fieldset caused omission.
- Production code uses Jackson 2 mapping with common context types and consumes shared fixtures
  directly.

## Test strategy

- Parameterize Phase 2.25 unrestricted, empty, renamed, per-type, attribute-only,
  relationship-only, primary, and included fieldset scenarios.
- Compare output and diagnostics across majors and use access-counting relationships to prove
  filtered properties are not read.

## Acceptance criteria

- [ ] Jackson 2 fieldsets use final mapped names and match Phase 2.8 output for primary and included
      resources while preserving identity for every applicable shared scenario.
- [ ] Excluded relationships are neither accessed nor traversed, and full-linkage exceptions are
      enabled only for actual fieldset omission.
- [ ] Unknown/disallowed names have parity diagnostics; no Jackson 3/runtime dependency exists; and
      common field-policy types are not redefined.
- [ ] The canonical `module-docs` checklist passes and conformance notes retain application-owned
      authorization and query parsing; public fieldset APIs satisfy ADR-009 `@NullMarked` /
      `@Nullable` rules.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*SparseFieldsetSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
