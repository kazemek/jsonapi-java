# Jackson 2 Sparse Fieldsets

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** [Jackson 2 Compound Serialization](jackson2-compound-serialization.md)  
> **Status:** Not started
> **Work item:** KAZ-32

## Goal

Port explicit sparse-fieldset application to Jackson 2 without changing inclusion or validation
semantics, using common field policies and shared fieldset-write fixtures.

## Research and constraints

- jackson3 sparse fieldsets (`jsonapi-java-jackson3` README) define fieldsets by final JSON:API
  resource type/member names, explicit application allow-list policy, pre-access filtering, and the
  scoped full-linkage exception.
- `jsonapi-java-jackson-common` places `FieldPolicy`, fieldset members of
  `CompoundSerializationContext`, and related diagnostics in the common package.
- [Jackson 2 Compound Serialization](jackson2-compound-serialization.md) supplies Jackson 2 inclusion traversal; `JsonApiFixtures.sparseFieldset()` /
  `SparseFieldsetScenarios` is the shared sparse-fieldset scenario catalog.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — fieldsets and inclusion share
  explicit context but remain distinct policies.
- [Jackson 2 Domain-to-Resource Mapping](jackson2-domain-resource-mapping.md) mapping definitions remain authoritative; fieldsets cannot rescan domain properties.

## Deliverables

- Extend Jackson 2 mapped-document overloads to apply common validated per-resource-type fieldsets
  and application field policy.
- Filter mapped attributes and relationships by final JSON:API names for primary and included
  resources while retaining resource identity.
- Apply relationship filtering before property access and compound traversal.
- Consume `SparseFieldsetScenarios` and port scoped sparse-full-linkage validation
  behavior.
- Use `module-docs` to refresh Jackson 2 docs/Javadoc and conformance notes for fieldset behavior
  and delegated authorization/query parsing.

## Non-goals

- Parsing HTTP `fields[...]` parameters or authorizing fields implicitly.
- Changing Jackson logical names, include policy, or graph traversal defaults.
- DTO reads, typed envelopes, or PATCH commands.
- Redefining common field-policy types under the `jackson2` package.

## Implementation boundaries

- Unknown resource types/fields and disallowed fields fail with stable jackson3-equivalent
  diagnostics from the common contract.
- The full-linkage exception is enabled only when an actually applied fieldset caused omission.
- Production code uses Jackson 2 mapping with common context types and consumes shared fixtures
  directly.

## Test strategy

- Execute every scenario from the live `SparseFieldsetScenarios` catalog, collect executed
  scenario IDs, and assert `executedScenarioIds == catalogScenarioIds` so newly added scenarios
  cannot silently escape the suite.
- Compare output and diagnostics across majors and use access-counting relationships to prove
  filtered properties are not read.
- Preserve adapter-local major-specific test cases separately from the shared catalog.

## Acceptance criteria

- [ ] Jackson 2 fieldsets use final mapped names and match jackson3 output for primary and included
      resources while preserving identity; the suite executes every live `SparseFieldsetScenarios`
      scenario and asserts `executedScenarioIds == catalogScenarioIds`.
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
