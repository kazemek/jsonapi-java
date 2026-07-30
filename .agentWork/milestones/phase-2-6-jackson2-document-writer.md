# Phase 2.6 — Jackson 2 Document Writer

> **Module:** `jsonapi-java-jackson2`  
> **Dependencies:** Phases 2.1 and 2.5  
> **Status:** Not started

## Goal

Provide a Jackson 2 writer artifact with the same validated JSON:API document output contract as the completed Jackson 3 writer.

## Research and constraints

- Phase 2.1 amends vision and ADR-007 to reserve symmetric `jsonapi-java-jackson3` and `jsonapi-java-jackson2` artifacts; this module implements the second artifact without changing core.
- [Jackson releases](https://github.com/FasterXML/jackson/wiki/Jackson-Releases) — Jackson 2 and 3 are
  maintained as separate major lines. This milestone supports the Jackson `2.22.x` compatibility
  line, tested at BOM `2.22.1`; no earlier historical-minor compatibility claim is made until
  Phase 4. The Gradle compatibility task is
  `:jsonapi-java-jackson2:jackson2CompatibilityTest`.
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) — Jackson 2 uses `com.fasterxml.jackson.*` while Jackson 3 uses `tools.jackson.*`, allowing coexistence but requiring separately compiled public APIs and handlers.
- Phase 2.1 canonical fixtures and Phase 2.5 schema classifications are the parity contract; do not fork wire policy for the older Jackson API.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — the Jackson 2 module must not depend on Jackson 3 types, `core.internal`, or another integration module's internals.

## Deliverables

- Register `jsonapi-java-jackson2` with Jackson 2 databind, core, JSpecify, and test-only ArchUnit dependencies; update ADR-010 and dependency-verification metadata for its exact production allowlist.
- Add the Jackson 2 public writer API under `io.github.kazemek.jsonapi.jackson2`, mirroring the Jackson 3 concepts and method semantics while using `com.fasterxml.jackson.*` signatures.
- Port the streaming serializers without a runtime dependency on `jsonapi-java-jackson3`; derive a configured copy/builder from caller Jackson 2 configuration so writer setup does not mutate the caller's mapper.
- Reuse the canonical fixture corpus and usage classifications to prove byte-policy and semantic parity with Jackson 3, including the supplemental draft-schema cases already established in Phase 2.5.
- Use `module-docs` for the new artifact and update the root module registry and conformance notes to identify both major-specific writer implementations.

## Non-goals

- Jackson 2 reads; Phase 2.7 ports the read contract separately.
- Jackson 2 domain mapping, compound inclusion, or sparse fieldsets; Phases 2.12–2.14 port those
  contracts after document-codec parity.
- A shared reflection bridge, runtime major-version detection, or one artifact containing both Jackson public APIs.
- Supporting closed or vulnerable Jackson 2 releases merely because they share the same major version.

## Implementation boundaries

- Production imports use `com.fasterxml.jackson.*`, never `tools.jackson.*`; the published runtime graph
  contains Jackson 2 and core but not Jackson 3 or the Jackson 3 artifact.
- Public names and behavior should be mechanically recognizable across `jackson2` and `jackson3` packages, but Java types that expose Jackson APIs remain major-specific.
- Keep the Jackson 3 canonical ordering, absence/null/empty rules, flattened wrappers, nullable links, array-form `hreflang`, validation-before-output behavior, and caller-configuration isolation unchanged.
- Configure Jackson 2 tests to consume the Phase 2.1 manifest and expected JSON directly from `fixtures/jsonapi-1.1/`. Jackson-specific test code may differ, but expected wire fixtures must not be copied into divergent major-specific variants.

## Test strategy

- Run the Phase 2.1 writer contract matrix through Jackson 2 and compare exact canonical output and parsed semantics with the shared expected resources.
- Add coexistence and dependency tests proving Jackson 2 and Jackson 3 packages can be present in a test process while each artifact only uses its own major's APIs.
- Run `:jsonapi-java-jackson2:jackson2CompatibilityTest` against the Jackson `2.22.x` line using
  BOM `2.22.1`; do not imply support for earlier lines before Phase 4 defines a wider matrix.

## Acceptance criteria

- [ ] `jsonapi-java-jackson2` exposes only Jackson 2 (`com.fasterxml.jackson.*`) public signatures and has no production/runtime dependency on Jackson 3, the Jackson 3 artifact, or `core.internal`.
- [ ] The shared writer contract produces the same canonical JSON and validation failures through Jackson 2 and Jackson 3, including all schema-classified fixtures.
- [ ] Writer configuration preserves caller mapper behavior, and the supported Jackson `2.22.x` line,
      BOM `2.22.1` baseline, and `:jsonapi-java-jackson2:jackson2CompatibilityTest` task are
      explicit in module documentation and executable compatibility tests.
- [ ] The canonical `module-docs` checklist passes, root/conformance registries distinguish both major-specific artifacts, and dependency verification is updated.
- [ ] `./gradlew :jsonapi-java-jackson2:test` passes.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
