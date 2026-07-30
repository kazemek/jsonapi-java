# Phase 2.14 — Jackson 2 Sparse Fieldsets

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.8 and 2.13  
> **Status:** Not started

## Goal

Port explicit sparse-fieldset application to Jackson 2 without changing inclusion or validation
semantics.

## Research and constraints

- Phase 2.8 defines fieldsets by final JSON:API resource type/member names, explicit application
  allow-list policy, pre-access filtering, and the scoped full-linkage exception.
- Phase 2.13 supplies Jackson 2 inclusion traversal and serialization context.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — fieldsets and inclusion share
  explicit context but remain distinct policies.
- Phase 2.12 mapping definitions remain authoritative; fieldsets cannot rescan domain properties.

## Deliverables

- Extend the Jackson 2 serialization context with validated per-resource-type fieldsets and
  application field policy.
- Filter mapped attributes and relationships by final JSON:API names for primary and included
  resources while retaining resource identity.
- Apply relationship filtering before property access and compound traversal.
- Port scoped sparse-full-linkage validation behavior and shared positive/negative fixtures.
- Refresh Jackson 2 docs/Javadoc and conformance notes for fieldset behavior and delegated
  authorization/query parsing.

## Non-goals

- Parsing HTTP `fields[...]` parameters or authorizing fields implicitly.
- Changing Jackson logical names, include policy, or graph traversal defaults.
- DTO reads, typed envelopes, or PATCH commands.

## Implementation boundaries

- Unknown resource types/fields and disallowed fields fail with stable Phase 2.8-equivalent
  diagnostics.
- The full-linkage exception is enabled only when an actually applied fieldset caused omission.
- Production code uses Jackson 2 mapping/context only and consumes shared fixtures directly.

## Test strategy

- Reuse unrestricted, empty, renamed, per-type, attribute-only, relationship-only, primary, and
  included fieldset fixtures.
- Compare output and diagnostics across majors and use access-counting relationships to prove
  filtered properties are not read.

## Acceptance criteria

- [ ] Jackson 2 fieldsets use final mapped names and match Phase 2.8 output for primary and included
      resources while preserving identity.
- [ ] Excluded relationships are neither accessed nor traversed, and full-linkage exceptions are
      enabled only for actual fieldset omission.
- [ ] Unknown/disallowed names have parity diagnostics and no Jackson 3/runtime dependency exists.
- [ ] The canonical `module-docs` checklist passes and conformance notes retain application-owned
      authorization and query parsing.
- [ ] `./gradlew :jsonapi-java-jackson2:test --tests '*SparseFieldsetSpec'` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
