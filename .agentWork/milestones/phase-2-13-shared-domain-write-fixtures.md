# Phase 2.13 — Shared Domain Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceMapperSpec`  
> **Dependencies:** Phases 2.2 and 2.11  
> **Status:** Not started

## Goal

Provide one version-neutral flat domain-to-resource write scenario catalog that proves mapping
parity without copying test models or expectations per Jackson major.

## Research and constraints

- Jackson 3 `ResourceMapperSpec` currently shares module-local models; a Jackson 2 port would
  otherwise copy those models and the flat-mapping matrix.
- [ADR-004](../../docs/adr/004-jackson-integration.md) makes each major's configured logical
  property model authoritative; shared fixtures may define inputs and semantic outcomes but cannot
  replace major-specific serializer/mix-in integration tests.
- Phase 2.11 supplies common public policy and diagnostic types; fixture code must not depend on
  either major adapter's production internals.
- Closed shared flat-mapping inventory from Phase 2.2 / `ResourceMapperSpec` (these test names
  only):
  `maps a record with explicit @JsonApiId and @JsonApiAttribute`; `maps attribute name override`;
  `maps conventional id property`; `maps @JsonProperty naming`; `maps nullable to-one relationship
  to null linkage`; `maps to-one relationship to single linkage`; `maps empty to-many relationship
  to empty linkage`; `maps populated to-many relationship`; `maps Set-based to-many relationship`;
  `maps mutable POJO`; `toDocument wraps resource in single-resource document`; `toResourceCollection
  wraps in resource-collection document`; `toDocument with envelope passes links, meta, and
  jsonapi`; `null input is rejected`.
- Adapter-local / out of this milestone: `IdentifierConversionSpec` cases;
  `ResourceMappingJacksonFeaturesSpec`; `TitleSerializer` / `@JsonSerialize`; mix-ins that need
  major-specific setup.
- `jsonapi-java-test-fixtures` currently depends on core + Groovy only; shared annotated models need
  `jsonapi-java-annotations`, Phase 2.11 common types, and shared `jackson-annotations` where
  `@JsonProperty` / `@JsonCreator` appear. New Java packages require explicit null-marking.

## Deliverables

- Move Jackson-neutral annotated domain models needed for the closed shared inventory into
  `jsonapi-java-test-fixtures`, with package-info null-marking and Gradle dependencies on
  annotations, common contracts, and shared Jackson annotations only.
- Add an immutable flat-mapping scenario catalog covering exactly the closed shared test names above
  with expected core resources and stable common diagnostic codes/paths.
- Refactor Jackson 3 `ResourceMapperSpec` to consume the shared catalog while retaining adapter-local
  serializer/mix-in/isolation cases and leaving IdentifierConversion /
  ResourceMappingJacksonFeatures suites out of this extraction.
- Add catalog integrity tests so later Jackson 2 suites must run every applicable shared scenario
  and document every major-specific exclusion.
- Document the test-fixtures package map, major-neutral boundary, and extension workflow for write
  scenarios.

## Non-goals

- Compound inclusion or sparse-fieldset catalogs; Phases 2.24 and 2.25 own those extractions.
- Flat DTO reads, typed envelopes, or PATCH fixtures.
- Sharing production mapping implementations or introducing a common Jackson introspection API.
- Moving test models that import `tools.jackson.*` or `com.fasterxml.jackson.databind.*`.
- Extracting `IdentifierConversionSpec`, `ResourceMappingJacksonFeaturesSpec`, or major-specific
  serializer/mix-in cases into the shared catalog.

## Implementation boundaries

- Shared production-like fixture models may depend on core, annotations, common Jackson contracts,
  and annotations common to supported Jackson lines, but not a major-specific databind/core API.
- Expected results are core/common values and stable diagnostics, never adapter implementation
  classes.
- Relationship mapping scenarios assert linkage only and never populate `included`.

## Test strategy

- Run each shared flat-mapping scenario through Jackson 3 and compare full semantic results and
  diagnostics.
- Keep focused Jackson 3-only serializer/mix-in/isolation cases and prove they compose with shared
  models where applicable.
- Add catalog tests for unique ids, declared capabilities, complete expected outcomes, and explicit
  exclusions.

## Acceptance criteria

- [ ] The closed shared `ResourceMapperSpec` inventory (the named tests above) is present in the
      shared catalog without Jackson-major production imports, and adapter-local /
      out-of-milestone exclusions (`IdentifierConversionSpec`, `ResourceMappingJacksonFeaturesSpec`,
      `TitleSerializer` / `@JsonSerialize`, major-specific mix-ins) are named.
- [ ] Jackson 3 `ResourceMapperSpec` consumes the shared scenarios for those named tests and retains
      only demonstrably major-specific cases locally.
- [ ] Shared expectations prove core resources and common diagnostic parity rather than merely
      comparing serialized text.
- [ ] Catalog integrity prevents an adapter from omitting an applicable shared case; test-fixtures
      module docs and Gradle/nullness boundaries for the new packages are recorded.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
