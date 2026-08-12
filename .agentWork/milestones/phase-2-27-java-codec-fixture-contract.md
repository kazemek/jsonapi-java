# Phase 2.27 — Java Codec Fixture Contract

> **Scope:** `jsonapi-java-test-fixtures` main sources (`io.github.kazemek.jsonapi.testfixtures.codec` and `.codec.cases`), build configuration, ADR-010  
> **Dependencies:** Phases 2.12 and 2.13  
> **Status:** Not started

## Goal

Replace the Groovy codec fixture branch with a behavior-equivalent pure-Java `@NullMarked`
implementation under unchanged packages and API surface, so `jsonapi-java-test-fixtures` main
sources are single-language (Java) and every consumer compiles and passes without modification.

## Research and constraints

- The `codec` package is the last Groovy main-source code in the repository: 9 support types
  (`CodecFixture`, `CodecFixtures`, `Models`, `NegativeCodecCase`, `NegativeCodecCases`,
  `AmbiguousPrimaryDataCase`, `AmbiguousPrimaryDataCases`, `SchemaKind`, `SchemaDisagreement`) and
  26 case classes under `codec.cases` (1422 lines total). The `domainwrite` package is already
  Java. Consumers are the 5 jackson3 specs (`DocumentWriterContractSpec`, `JsonApiDraftSchemaSpec`,
  `DocumentReaderSpec`, `DocumentWriterSinkSpec`, `DomainDocumentReaderSpec`) and the 3 catalog
  specs plus the ArchUnit spec in test-fixtures.
- The retrieval API must stay byte-for-byte source compatible: `CodecFixtures.all()/byId()/
  writable()/readable()/schemaChecked()/exactUtf8()/hreflangArray()`, `NegativeCodecCases.all()/
  byId()`, `AmbiguousPrimaryDataCases.all()/byId()`, `SchemaKind` enum, and the `CodecFixture` /
  `NegativeCodecCase` / `AmbiguousPrimaryDataCase` / `SchemaDisagreement` property shapes. Fixture
  ids and expected JSON paths are stable across majors (Phase 2.12 rule); type renames belong to
  Phase 2.28, not here.
- Wire-visible member order: `MemberOrderCase`, `OpenValuesCase`, and `additionalMembers` depend
  on map insertion order; converted construction must use `LinkedHashMap` explicitly (the
  `domainwrite` package already models this). `toString()` must remain `id` for the value types
  (Spock data-pipe iteration names use it).
- Nullness: `compileJava` runs NullAway with `RequireExplicitNullMarking` (ADR-009), so all new
  Java main sources must be `@NullMarked`; nullable members become `@Nullable` record components
  (`CodecFixture.primaryDataKind`, `schemaKind`, `schemaDisagreement`, `exactUtf8Path`;
  `NegativeCodecCase.pointer`, `ruleCode`).
- Neutrality: the ArchUnit allowlist (ADR-010) forbids `tools.jackson..`,
  `com.fasterxml.jackson.databind..`, major-specific adapter packages, and `core.internal..`.
  `NegativeCodecCases` currently loads `negative-manifest.json` with `groovy.json.JsonSlurper`;
  Java has no built-in JSON parser, so the replacement is the Jakarta JSON Processing API
  ([JSON-P](https://jakarta.ee/specifications/jsonp/)) with the Eclipse Parsson implementation
  (`jakarta.json:jakarta.json-api` + `org.eclipse.parsson:parsson`) — standard, version-neutral,
  no Jackson on the fixtures classpath.
- `jsonapi-java-test-fixtures/build.gradle.kts` declares `implementation(libs.groovy.all)`; the
  `jsonapi-java-library` convention plugin already adds `groovy-all` at test scope for Spock, so
  dropping the implementation dependency confines Groovy to `src/test`. Catalog specs keep
  `JsonSlurper` in test scope as an independent manifest cross-check of the production loader.
- New dependencies require regenerating `gradle/verification-metadata.xml` with sha256 (per
  `AGENTS.md`).
- The `module-docs` skill applies: public in-repo entry points change implementation language.

## Deliverables

- Convert the 9 support types to `@NullMarked` Java records/classes in the same packages and with
  the same names: `CodecFixture` (record with 13 components, 4 `@Nullable`), `CodecFixtures`
  (catalog with identical static entry points), `Models` (typed static factory overloads replacing
  Groovy named-argument helpers), `NegativeCodecCase(s)`, `AmbiguousPrimaryDataCase(s)`,
  `SchemaKind`, `SchemaDisagreement` (keeps `List<Map<String, String>> expected` shape consumed by
  `JsonApiDraftSchemaSpec`).
- Convert all 26 case classes to Java under `codec.cases` with identical ids, expected paths,
  notes, and capability flags, preserving map insertion order via `LinkedHashMap`.
- Add a neutral JSON manifest loader (JSON-P + Parsson) for `negative-manifest.json` — the only
  manifest main sources parse today (`NegativeCodecCases`) — replacing `JsonSlurper`, with a
  loader-internal resolution of `jsonapi.fixtures.dir` that preserves the current behavior and
  error message. `AmbiguousPrimaryDataCases` stays an explicit Java catalog (it never loads a
  manifest; the ambiguous manifest remains a spec-side cross-check). The shared `FixtureDirectory`
  covering both fixture-dir properties plus spec migration is Phase 2.28 scope.
- Update build configuration: remove `implementation(libs.groovy.all)` from
  `jsonapi-java-test-fixtures/build.gradle.kts`, add JSON-P API + Parsson to the version catalog,
  amend the `TestFixturesDependencyRulesSpec` allowlist and ADR-010 (drop `groovy..` /
  `org.codehaus.groovy..`, add `jakarta.json..` / `org.eclipse.parsson..`), and regenerate
  `gradle/verification-metadata.xml`.
- Update module documentation (`jsonapi-java-test-fixtures/README.md` package map and agent notes,
  `package-info.java` for the codec packages) via the `module-docs` skill.

## Non-goals

- Renames, the `Scenario`/`FixtureCatalog` contract, the `JsonApiFixtures` facade, or any API
  surface change — Phase 2.28 owns them.
- Changes to `fixtures/jsonapi-1.1/` documents, manifests, or expected diagnostics.
- Changes to consumer specs: they must compile and pass untouched (they are the equivalence
  oracle for this conversion).
- Converting test sources to Java; Spock specs remain Groovy by repository convention.

## Implementation boundaries

- Packages, class names, and the consumer-facing retrieval surface (`CodecFixtures.all()/byId()/
  writable()/readable()/schemaChecked()/exactUtf8()/hreflangArray()`, `NegativeCodecCases.all()/
  byId()`, `AmbiguousPrimaryDataCases.all()/byId()`, the `SchemaKind` enum, and the property shapes
  of the value types) are frozen during this milestone; only the implementation language and the
  manifest parsing library change. The internal Groovy named-argument factories (`CodecFixture.of(
  Map)`, `NegativeCodecCase.of(Map)`, `AmbiguousPrimaryDataCase.of(Map)`, `SchemaDisagreement.of(
  Map)`) are not consumer-facing (only the case classes call them) and are replaced by typed
  factories / record constructors with the existing defaults preserved (`context`,
  `writable`/`readable` true, `assertExactUtf8`/`assertHreflangArray` false).
- Main sources never depend on either Jackson major or on `core.internal`; the amended ArchUnit
  rule enforces this on main bytecode.
- Groovy is confined to `src/test` (Spock specs and their independent `JsonSlurper` manifest
  cross-checks).

## Test strategy

- The 3 existing catalog specs (`CodecFixturesCatalogSpec`, `NegativeCodecCasesCatalogSpec`,
  `AmbiguousPrimaryDataCasesCatalogSpec`), the ArchUnit spec, and the 5 jackson3 specs run
  unchanged — passing proves semantic equivalence of the conversion.
- Catalog specs keep `JsonSlurper` in test scope so the production loader is cross-checked
  against an independent parse.
- NullAway enforces `@NullMarked`/`@Nullable` on the converted Java main sources.

## Acceptance criteria

- [ ] No `src/main/groovy` sources remain in `jsonapi-java-test-fixtures` (and none anywhere else
      in the repository); `groovy-all` is test-scope only.
- [ ] All codec fixture ids, expected paths, notes, capability flags, and diagnostics semantics
      are identical to the Groovy version, proven by the 3 catalog specs and 5 jackson3 specs
      passing with zero source changes.
- [ ] The negative manifest loads through the JSON-P loader with an identical case set;
      `AmbiguousPrimaryDataCases` remains an explicit Java catalog, and the catalog specs still
      cross-check both manifests independently via `JsonSlurper`.
- [ ] NullAway and `RequireExplicitNullMarking` pass on the converted sources; nullable members
      are typed `@Nullable`; `toString()` returns `id`; map insertion order is preserved for
      member-order-sensitive fixtures.
- [ ] ADR-010 and `TestFixturesDependencyRulesSpec` are amended (no `groovy..` /
      `org.codehaus.groovy..` allowlist entries; `jakarta.json..` / `org.eclipse.parsson..`
      added) and the ArchUnit spec passes.
- [ ] JSON-P dependencies are declared in `gradle/libs.versions.toml`, the
      `jsonapi-java-test-fixtures` build no longer has a main-scope Groovy dependency, and
      `gradle/verification-metadata.xml` is regenerated.
- [ ] The canonical `module-docs` checklist passes for the changed test-fixtures package maps and
      entry points.
- [ ] `./gradlew clean build` passes; Spotless passes (`./gradlew spotlessApply` then
      `./gradlew spotlessCheck`); Sonar Quality Gate passes — if `SONAR_TOKEN` is unavailable,
      report Sonar blocked and that CI must still pass the gate.
