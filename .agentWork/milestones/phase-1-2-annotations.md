# Phase 1.2 — Domain-Mapping Annotations

> **Module:** `jsonapi-java-annotations`  
> **Dependencies:** Phases 0.1, 0.5, and 0.8  
> **Status:** Complete

## Goal

Provide a runtime-visible annotation API with no functional third-party runtime dependencies that lets domain classes and records declare JSON:API resource, identifier, attribute-name, and relationship-name roles for later Jackson mapping.

## Research and constraints

- [`docs/vision.md`](../../docs/vision.md) — annotations are opt-in metadata in their own artifact; Jackson remains authoritative for logical property discovery, visibility, names, values, and serialization.
- [ADR-004](../../docs/adr/004-jackson-integration.md) — annotations override JSON:API role or field name only; they do not invent a second property model. `@JsonApiAttribute` exists for an optional JSON:API field-name override (empty `name()` keeps Jackson's logical name).
- [ADR-007](../../docs/adr/007-module-boundaries.md) and [ADR-008](../../docs/adr/008-public-namespace.md) — add the `jsonapi-java-annotations` submodule and place its public API in `io.github.kazemek.jsonapi.annotation` without core, Jackson, persistence, or framework dependencies.
- [ADR-005](../../docs/adr/005-domain-mapping-and-inclusion.md) — `@JsonApiRelationship` identifies linkage and its name only; it must not request inclusion or carry fetch, cascade, repository, or ORM policy.
- [JSON:API v1.1 member names](https://jsonapi.org/format/1.1/#document-member-names) — resource `type` and non-empty JSON:API field-name overrides must satisfy the case-sensitive member-name grammar. Annotation instances only store metadata, so Phase 2.2 performs this validation when it builds a mapping definition.
- [Java SE 21 `ElementType`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/ElementType.html) and [`RetentionPolicy`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html) — property annotations explicitly target fields, methods, parameters, and record components and use runtime retention for mapper introspection.
- [ADR-009](../../docs/adr/009-jspecify-nullness.md) and the shared `jsonapi-java-library` plugin — the production package is `@NullMarked`; JSpecify remains compile-only and does not become a published runtime dependency.
- [ADR-010](../../docs/adr/010-architectural-tests.md) — ArchUnit enforces package/type dependency allowlists for production sources; extend the ADR when adding module rules.

## Deliverables

- Register `jsonapi-java-annotations` in `settings.gradle.kts` with a minimal build applying `jsonapi-java-library`; add the `io.github.kazemek.jsonapi.annotation` production package.
- Add four `@Documented`, runtime-retained annotation types with these exact elements and targets: `@JsonApiResource(type = "...")` on types with required `String type()` and no `@Inherited`; marker `@JsonApiId` on fields, methods, parameters, and record components; and `@JsonApiAttribute` / `@JsonApiRelationship` on those property locations with `String name() default ""`.
- Document the metadata contract in focused public Javadoc: an explicit `@JsonApiId` or Phase 2.2's conventional logical property named `id` supplies the identifier; the empty `name` sentinel retains the Jackson logical property name; identifiers and relationships cease to be default attributes; annotations never make Jackson-ignored properties visible; relationship metadata requests linkage, not inclusion; and Phase 2.2 owns logical-property conflict, value-shape, identifier-conversion, and member-name validation.
- Add reflection and usage-fixture Spock tests for annotation elements, defaults, meta-annotations, exact targets, non-inheritance, and legal placement on POJOs and records without adding Jackson to this module.
- Use the `module-docs` skill to create the module README and package documentation (Minimal usage is an annotated record/POJO sample, not a no-entry-point note), register the module in the root README, and update `docs/conformance.md` with a Phase 1.2 annotation-metadata section whose vocabulary rows are **supported**, while Jackson mapping rows remain **deferred** to Phase 2.2.

## Non-goals

- Jackson introspection, logical-property resolution, mapping-definition errors, identifier conversion, relationship cardinality, or resource-object construction; these remain Phase 2.2 work.
- Runtime member-name validation in the annotation artifact or a dependency on core solely to reuse its validator.
- Inclusion paths, sparse fieldsets, traversal, persistence behavior, query behavior, or framework integration.
- Annotation elements for custom converters, inclusion, fetch, cascade, repositories, or ORM-specific behavior; no converter or identifier-conversion SPI ships in this artifact.

## Implementation boundaries

- This milestone is independent of Phase 1.1; it introduces metadata only and does not consume the core document model.
- `type()` has no default. `name()` defaults to the empty string only as the explicit “use Jackson's logical property name” sentinel; a non-empty override is interpreted and validated only by Phase 2.2. Annotation `String` elements are non-null; the empty string is the rename sentinel, never Java `null`.
- `@JsonApiResource` is usable on classes, records, interfaces, enums, and annotation interfaces at Java compile time because `ElementType.TYPE` covers those declarations. The annotation module does not claim that every legal placement is mappable; Phase 2.2 defines and diagnoses supported domain shapes.
- Property annotations include `ElementType.RECORD_COMPONENT` as well as the declaration targets through which Java and Jackson may expose a logical property. Phase 2.2 must collapse propagated occurrences into one logical property rather than inventing field/getter precedence.
- Production code may import only `java.lang.annotation` and JSpecify package metadata. The artifact has no functional third-party runtime dependency.

## Test strategy

- Reflect over each annotation type to assert `RUNTIME`, `@Documented`, exact `@Target` sets, absence of `@Inherited`, required/defaulted elements, return types, and marker shape.
- Compile and reflect over small record and POJO fixtures to prove resource and property annotations can be declared at all intended locations without Jackson.
- ArchUnit Spock spec imports `main` classes under `io.github.kazemek.jsonapi.annotation` and fails the build on illegal type dependencies (JDK + JSpecify + self only).
- Keep Jackson naming, mix-in, ignored-property, propagation-conflict, cardinality, conversion, and invalid-name cases in Phase 2.2, where behavior can be tested through Jackson's logical property model.

## Acceptance criteria

- [x] Reflection and usage-fixture tests prove all four exact annotation contracts, including `name()` defaults, creator-parameter and record-component targets, and non-inheritance.
- [x] Public Javadoc defines metadata-only default-attribute, identifier, rename, and linkage semantics without implementing or contradicting Phase 2.2 mapping policy.
- [x] The runtime classpath has no functional third-party artifacts (Gradle `compileOnly` JSpecify / no implementation deps), annotation elements contain no converter, inclusion, persistence, query, or framework concerns, and ArchUnit enforces the production type-dependency allowlist.
- [x] The canonical `module-docs` checklist passes, including `@NullMarked` package documentation, root module registration, and the scoped conformance update (Phase 1.2 annotation vocabulary **supported**; Jackson mapping still **deferred**).
- [x] `./gradlew :jsonapi-java-annotations:test` passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
