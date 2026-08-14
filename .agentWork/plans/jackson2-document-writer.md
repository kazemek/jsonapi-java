# Jackson 2 Document Writer

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** None  
> **Status:** Not started
> **Work item:** KAZ-27

## Goal

Provide a Jackson 2 writer artifact with the same validated JSON:API document output contract as
`jsonapi-java-jackson3` `JsonApiDocumentWriter` (module README), consuming
`jsonapi-java-jackson-common` contracts and the shared codec scenarios through `JsonApiFixtures`.

## Research and constraints

- [ADR-007](../../docs/adr/007-module-boundaries.md) reserves separately compiled
  `jsonapi-java-jackson2` and `jsonapi-java-jackson3` artifacts;
  `jsonapi-java-jackson-common` holds Jackson-import-free policy and diagnostic types.
- [Jackson releases](https://github.com/FasterXML/jackson/wiki/Jackson-Releases) — this plan
  supports the Jackson `2.22.x` line, tested at BOM `2.22.1`; no earlier historical-minor claim is
  made until [Conformance and Hardening](conformance-and-hardening.md). The Gradle task is `:jsonapi-java-jackson2:jackson2CompatibilityTest`.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
  — Jackson 2 uses `com.fasterxml.jackson.*` while Jackson 3 uses `tools.jackson.*`; coexistence
  requires separately compiled handlers, not runtime major detection.
- `JsonApiFixtures.codec()` capability-tagged fixtures and [docs/conformance.md](../../docs/conformance.md)
  schema classifications are the parity contract; do not fork wire policy or expected JSON for the
  older Jackson API.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) — new public `jackson2` packages are
  `@NullMarked` with accurate `@Nullable` on absence-preserving and optional values.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — production code may depend on common
  contracts, core, annotations, JSpecify, and Jackson 2, never Jackson 3 or sibling internals.

## Deliverables

- Register `jsonapi-java-jackson2` with Jackson 2 databind, core, annotations, common contracts,
  JSpecify, and test-only ArchUnit; update ADR-010 and dependency-verification metadata for its
  exact production allowlist.
- Add the Jackson 2 public writer API under `io.github.kazemek.jsonapi.jackson2`, using common
  contexts/diagnostics and `com.fasterxml.jackson.*` mapper signatures.
- Port streaming serializers without a runtime dependency on `jsonapi-java-jackson3`; derive a
  configured copy/builder from caller Jackson 2 configuration so writer setup does not mutate the
  caller's mapper.
- Consume the `JsonApiFixtures.codec()` writer/schema capability catalogs to prove byte-policy and
  semantic parity with Jackson 3, including supplemental draft-schema cases.
- Use `module-docs` for the new artifact and update the root module registry and conformance notes
  to identify both major-specific writer implementations.

## Non-goals

- Jackson 2 reads; [Jackson 2 Document Reader](jackson2-document-reader.md) ports the read contract separately.
- Jackson 2 domain mapping, compound inclusion, or sparse fieldsets; [Jackson 2 Domain-to-Resource Mapping](jackson2-domain-resource-mapping.md), [Jackson 2 Compound Serialization](jackson2-compound-serialization.md), and [Jackson 2 Sparse Fieldsets](jackson2-sparse-fieldsets.md) own them.
- Duplicating common policy/diagnostic types under the `jackson2` package.
- A shared reflection bridge, runtime major-version detection, or one artifact containing both
  Jackson public APIs.
- Supporting closed or vulnerable Jackson 2 releases merely because they share the same major
  version.

## Implementation boundaries

- Production imports use `com.fasterxml.jackson.*` and `io.github.kazemek.jsonapi.jackson`, never
  `tools.jackson.*` or `jsonapi-java-jackson3`.
- Jackson-bound factories and serializers remain major-specific; neutral contexts and diagnostics
  come from the common module.
- Keep canonical ordering, absence/null/empty rules, flattened wrappers, nullable links, array-form
  `hreflang`, validation-before-output, and caller-configuration isolation unchanged.
- Tests select `JsonApiFixtures.codec()` fixtures by capability; expected wire fixtures must not be
  copied into divergent major-specific variants.

## Test strategy

- Run the `JsonApiFixtures.codec()` writer capability matrix through Jackson 2 and compare canonical
  output and parsed semantics with the shared expected resources.
- Add coexistence and dependency tests proving Jackson 2 and Jackson 3 packages can be present while
  each artifact only uses its own major's APIs plus common contracts.
- Run `:jsonapi-java-jackson2:jackson2CompatibilityTest` against Jackson `2.22.x` / BOM `2.22.1`.

## Acceptance criteria

- [ ] `jsonapi-java-jackson2` exposes only Jackson 2 public signatures, depends on common contracts
      for neutral types, and has no production/runtime dependency on Jackson 3, the Jackson 3
      artifact, or `core.internal`.
- [ ] The shared writer/schema contract produces the same canonical JSON and validation failures
      through Jackson 2 and Jackson 3 for every applicable `JsonApiFixtures.codec()` fixture.
- [ ] Writer configuration preserves caller mapper isolation: using the JSON:API writer does not
      alter ordinary serialization through the caller's original Jackson 2 mapper.
- [ ] New public `jackson2` packages are `@NullMarked` with accurate `@Nullable` (ADR-009); the
      canonical `module-docs` checklist passes; root/conformance registries distinguish both
      major-specific artifacts; and dependency verification is updated.
- [ ] `./gradlew :jsonapi-java-jackson2:jackson2CompatibilityTest` passes against Jackson `2.22.x`
      / BOM `2.22.1`, and the supported baseline plus task are explicit in module documentation.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
