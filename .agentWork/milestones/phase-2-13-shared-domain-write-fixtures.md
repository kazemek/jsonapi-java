# Phase 2.13 — Shared Domain Write Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceMapperSpec`  
> **Dependencies:** Phases 2.2 and 2.11  
> **Status:** Not started

## Goal

Provide one version-neutral flat domain-to-resource write fixture catalog—shared annotated
models plus scenario expectations—in `jsonapi-java-test-fixtures` and migrate Jackson 3
`ResourceMapperSpec` onto it, so Jackson 2 can later reuse the same models and expectations
without per-major copies.

## Research and constraints

- Closed shared `ResourceMapperSpec` inventory (exact test names, verified against
  `jsonapi-java-jackson3/src/test/groovy/.../ResourceMapperSpec.groovy`):
  `maps a record with explicit @JsonApiId and @JsonApiAttribute`; `maps attribute name override`;
  `maps conventional id property`; `maps @JsonProperty naming`; `maps nullable to-one relationship
  to null linkage`; `maps to-one relationship to single linkage`; `maps empty to-many relationship
  to empty linkage`; `maps populated to-many relationship`; `maps Set-based to-many relationship`;
  `maps mutable POJO`; `toDocument wraps resource in single-resource document`; `toResourceCollection
  wraps in resource-collection document`; `toDocument with envelope passes links, meta, and
  jsonapi`; `null input is rejected`.
- The 8 backing models live today under
  `jsonapi-java-jackson3/src/test/java/io/github/kazemek/jsonapi/jackson3/testmodel/` (7 records)
  plus the inline `static class SamplePojo` declared inside `ResourceMapperSpec`. Their imports
  are major-neutral — only `io.github.kazemek.jsonapi.annotation.*`
  (`@JsonApiResource`/`@JsonApiId`/`@JsonApiAttribute`/`@JsonApiRelationship`),
  `com.fasterxml.jackson.annotation.JsonProperty` (`BlogWithJsonProperty` only), and JDK types
  (`List`/`Set`/`String`). None import `tools.jackson.*` or `com.fasterxml.jackson.databind.*`,
  so they move as-is: `Article`, `ArticleWithSet`, `BlogWithJsonProperty`, `Comment`,
  `ConventionalId`, `Person`, `Tag`, and `SamplePojo` (extracted to a top-level class).
- Phase 2.13 is the authoritative owner of the fixed Java package
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` under
  `jsonapi-java-test-fixtures/src/main/java/`. It owns the eight moved models, the
  `DomainWriteScenario` operation/input/outcome types, `DomainWriteComparisonPolicy`, the
  `DomainWriteScenarios` catalog entry
  point, and the `DomainWriteExclusionManifest`; `DomainWriteExclusion` is an immutable record of
  non-null `suiteId`, `DomainWriteExclusionCategory`, and non-blank `reason`, with the category
  enum fixed to `ADAPTER_SPECIFIC` or `LATER_FIXTURE_PHASE`; later fixture phases reuse these types
  and do not create parallel copies or package names.
- Deleting the seven old records also affects same-package Java test helpers that currently resolve
  `Comment` or `Person` without imports. The complete helper inventory is:
  `ArticleWithArray.java` (`Comment`), `ArticleWithOptionalRelationship.java` (`Comment`),
  `ArticleWithRenamedAuthor.java` (`Person`), `ConflictArticle.java` (`Person`),
  `AccessCountingArticle.java` (`Person`, `Comment`), `AccessCountingFieldsetArticle.java`
  (`Person`, `Comment`), `BaseComment.java` (`Person`), and `ModeratedComment.java` (`Person`).
  These helper models remain under `io.github.kazemek.jsonapi.jackson3.testmodel` with explicit
  imports from `io.github.kazemek.jsonapi.testfixtures.domainwrite`; the migration is not complete
  until all Java and Groovy test-source references to the seven deleted records resolve to the
  shared package.
- Null-bearing members requiring `@Nullable` per ADR-009 once moved into a Java `@NullMarked`
  package: `Article.author` (passed `null` in tests 1, 2, 5, 7, 8, 11, 12, 13 — producing
  `RelationshipData.NullLinkage`; a non-null `Person` appears only in test 6); `Comment.author`
  (passed `null` in test 8's `Comment("c1","Nice",null)` and `Comment("c2","Great",null)`); and
  `SamplePojo.{id, name, comments}` — these last three are not exercised as null by the closed
  inventory (test 10 uses the all-args constructor with non-null arguments), but `SamplePojo`
  exposes a public no-arg constructor that leaves all three fields null, so under `@NullMarked`
  they must be `@Nullable` (and the mutable fields can be assigned null after construction).
  The explicitly repointed `DomainDocumentReaderSpec` also constructs `new Person(identifier.id(),
  null)`, `new Comment("c1", null, new Person("p1", null))`, and `new Person("p1", null)`, so
  `Person.name` and `Comment.body` are `@Nullable` even though those nulls are outside the closed
  14-case write inventory. `Article.comments`, `ArticleWithSet.tags`, and the remaining record
  components are non-null in all retained call sites and stay non-`@Nullable`. The
  `jsonapi-java-library` convention plugin wires
  NullAway (`onlyNullMarked = true`, JSpecify mode) and the Error Prone
  `RequireExplicitNullMarking` error on `compileJava`, so Java `main` sources under
  `io.github.kazemek.jsonapi.*` are enforced; the new package therefore must ship a
  `package-info.java` with `@NullMarked`.
- `jackson-annotations` is major-neutral: `:jsonapi-java-jackson3:dependencies --configuration
  compileClasspath` shows `tools.jackson.core:jackson-databind:3.2.1` transitively pulling
  `com.fasterxml.jackson.core:jackson-annotations:2.22`, the same artifact Jackson 2 consumes.
  `BlogWithJsonProperty`'s `@JsonProperty` therefore stays major-neutral. The
  `jsonapi-java-test-fixtures/build.gradle.kts` currently declares only
  `api(project(":jsonapi-java-jackson-common"))`, `api(project(":jsonapi-java-core"))`, and
  `implementation(libs.groovy.all)` — it has **no** `jsonapi-java-annotations` project dependency
  and **no** jackson artifact, so both `api(project(":jsonapi-java-annotations"))` and a new
  `implementation(libs.jackson.annotations)` catalog alias (pinned to the Jackson 3 BOM-constrained
  2.22, already covered by `gradle/verification-metadata.xml`) must be added. `implementation`
  suffices because `@JsonProperty` is `RUNTIME`-retained and every adapter already pulls
  `jackson-annotations` via its own databind.
- [ADR-004](../../docs/adr/004-jackson-integration.md) makes each major's configured logical
  property model authoritative; shared fixtures define domain inputs and expected
  core/common semantic outcomes but cannot replace major-specific serializer, mix-in, naming
  strategy, or `@JsonIgnore` integration tests. Expected version-neutral values are core
  (`ResourceObject`, `JsonApiDocument`, `RelationshipData.NullLinkage`/`SingleLinkage`/
  `IdentifierCollectionLinkage`, `DocumentData.SingleResource`/`ResourceCollection`, `Links`,
  `Meta`, `JsonApiObject`) and Phase 2.11 common (`DocumentEnvelope`); the null-input scenario
  is a shared adapter-entry-point rejection: Jackson 3's `toResource(null)` raises
  `NullPointerException` before a core resource is constructed, and any later Jackson 2 consumer
  must preserve that exception type; it is not a `MappingDiagnostic` or a core outcome.
- Adapter-local / out of scope by exact spec name: `IdentifierConversionSpec` (uses
  `tools.jackson.databind.json.JsonMapper` and `IdentifierConverter`); `TitleSerializer` /
  `@JsonSerialize` and `ResourceMappingJacksonFeaturesSpec` (use `PropertyNamingStrategies`,
  `@JsonIgnore`, custom `ValueSerializer`, mix-ins — confirmed by import inspection; the latter
  imports `Comment` but not `Person` directly). Eight jackson3 specs import a moved model
  (`ResourceMapperSpec` plus the seven others verified by `rg`: `IdentifierConversionSpec`,
  `ResourceMappingJacksonFeaturesSpec`, `ResourceMapperIsolationSpec`, `ResourceBinderSpec`,
  `SparseFieldsetSpec`, `CompoundSerializationSpec`, and `DomainDocumentReaderSpec`); those seven
  keep their behavior unchanged but must repoint their moved-model imports to the new shared
  package, and are **not** added to the shared catalog. The closed
  `DomainWriteExclusionManifest` contains exactly these fully qualified suite-id/category/reason
  triples: `io.github.kazemek.jsonapi.jackson3.IdentifierConversionSpec` /
  `ADAPTER_SPECIFIC` (identifier conversion),
  `io.github.kazemek.jsonapi.jackson3.ResourceMappingJacksonFeaturesSpec` /
  `ADAPTER_SPECIFIC` (Jackson mix-ins, naming strategy, serializers, optional values, and
  unsupported relationship cases), `io.github.kazemek.jsonapi.jackson3.ResourceMapperIsolationSpec`
  / `ADAPTER_SPECIFIC` (mapper/builder isolation and converter-overload behavior),
  `io.github.kazemek.jsonapi.jackson3.ResourceBinderSpec` / `LATER_FIXTURE_PHASE` (flat
  resource-to-DTO binding), `io.github.kazemek.jsonapi.jackson3.SparseFieldsetSpec` /
  `LATER_FIXTURE_PHASE` (sparse fieldsets and mapped documents),
  `io.github.kazemek.jsonapi.jackson3.CompoundSerializationSpec` / `LATER_FIXTURE_PHASE`
  (compound inclusion), and `io.github.kazemek.jsonapi.jackson3.DomainDocumentReaderSpec` /
  `LATER_FIXTURE_PHASE` (domain document/read binding). This manifest is the only exclusion set
  whose completeness the catalog-integrity spec validates; model names and wildcard lists are not
  completeness claims.
- Phase 2.14 owns the flat DTO-binding scenario catalog and the behavioral refactor of
  `ResourceBinderSpec`, but it depends on Phase 2.13 for the fixed domain-write package and the
  initial import repoint. Phase 2.13 performs only that mechanical `ResourceBinderSpec` import
  migration; it does not extract binder scenarios, and Phase 2.14 must reuse `BlogWithJsonProperty`,
  `Comment`, and `Person` from `domainwrite` rather than moving or redefining them.
- This milestone adds the first Java production sources to `jsonapi-java-test-fixtures`
  (verified: only `src/main/groovy/` exists today; no `src/main/java`). [ADR-010](../../docs/adr/010-architectural-tests.md)
  requires each library module that owns production Java sources to enforce package/type
  dependency rules with ArchUnit (`testImplementation`) and explicitly prohibits source-import
  scanners as a coupling substitute. The test-fixtures major-neutral invariant (no `tools.jackson..`
  or `com.fasterxml.jackson.databind..`) is precisely such a coupling check, so it is enforced by
  a new `TestFixturesDependencyRulesSpec` ArchUnit rule — not by a source-import scan — and the
  catalog-integrity spec is scoped to catalog-level defects only. ADR-010's "Current allowlists" is
  amended to register the `io.github.kazemek.jsonapi.testfixtures..` allowlist permitting `java..`,
  `org.jspecify.annotations..`, `groovy..`, `org.codehaus.groovy..`,
  `io.github.kazemek.jsonapi.{annotation, core.model, core.validation, jackson}..`,
  `com.fasterxml.jackson.annotation..`, and intra-`testfixtures..`, and forbidding
  `tools.jackson..`, `com.fasterxml.jackson.databind..`, major-specific adapter packages
  (`jackson2..`, `jackson3..`), and `core.internal..`. The `groovy..` / `org.codehaus.groovy..`
  entries are required because the package includes compiled Groovy bytecode (the `codec` catalog
  implements `groovy.lang.GroovyObject` and `NegativeCodecCases` imports `groovy.json.JsonSlurper`);
  ArchUnit sees both the existing Groovy `codec` bytecode (which also imports `core.model..`,
  `core.validation..`, and `jackson.PrimaryDataKind`) and the new `domainwrite` Java bytecode, so
  the allowlist covers both source languages.

## Deliverables

- Move the 8 shared annotated models (`Article`, `ArticleWithSet`, `BlogWithJsonProperty`,
  `Comment`, `ConventionalId`, `Person`, `Tag`, and the inline `SamplePojo` extracted to a
  top-level class) into the fixed Java `@NullMarked` package
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` under `src/main/java/`, preserving their
  current Jackson-visible property shapes; add `@Nullable` on `Article.author`, `Comment.author`,
  `Person.name`, `Comment.body`, and `SamplePojo.{id, name, comments}`;
  add `api(project(":jsonapi-java-annotations"))`, `implementation(libs.jackson.annotations)`, and
  `testImplementation(libs.archunit)` to `jsonapi-java-test-fixtures/build.gradle.kts` (the
  ArchUnit dependency required by ADR-010, since this milestone adds the module's first production
  Java sources); delete the 7 moved records from `jsonapi-java-jackson3/.../testmodel/` and remove
  the inline `SamplePojo` from the spec.
- Add an immutable flat-write-mapping scenario catalog covering exactly the 14 closed test
  names in the new Java `@NullMarked` package. Each immutable `DomainWriteScenario` carries one
  stable id (the exact existing test name), one `DomainWriteOperation` from
  `TO_RESOURCE`, `TO_DOCUMENT`, `TO_DOCUMENT_WITH_ENVELOPE`, or `TO_RESOURCE_COLLECTION`, one
  typed input (`SingleInput(Supplier<@Nullable Object>)` for a single domain value or
  `CollectionInput(Supplier<Iterable<?>>)` for a resource collection), an `@Nullable
  DocumentEnvelope` argument, and one discriminated `DomainWriteOutcome`. A successful outcome
  contains exactly one neutral core value (`ResourceObject` for `TO_RESOURCE`, `JsonApiDocument`
  for the document operations); a failure contains exactly one expected exception class. The
  numeric references below use the 1-based order of the closed inventory listed above. The
  operation/input/envelope matrix is fixed: ids 1-10 and 14 use `TO_RESOURCE` with `SingleInput`
  (id 14's supplier returns null), id 11 uses `TO_DOCUMENT` with no envelope, id 12 uses
  `TO_RESOURCE_COLLECTION` with `CollectionInput`, and id 13 uses
  `TO_DOCUMENT_WITH_ENVELOPE` with a non-null envelope. No adapter type or id-specific dispatch
  belongs in the catalog; the consumer dispatches only on the operation/input descriptor, never
  on a scenario id. Document expectations include the exact
  `DocumentData.SingleResource`/`ResourceCollection`, envelope members, and absent `included`.
  Each scenario also carries one non-null `DomainWriteComparisonPolicy` whose immutable relationship
  order map uses `ORDERED` by default and permits `UNORDERED_IDENTIFIER_PAIRS` only for the `tags`
  relationship in the Set-based scenario; the policy is data consumed by the comparator, not an
  id/type heuristic.
  Relationship expectations include exact linkage variants and `(type,id)` identifiers: ordered
  single/List values for `author`/`comments`, and an explicit unordered comparison policy for the
  Set-based `tags` case. The catalog and model sources are major-neutral (enforced by the ArchUnit
  rule below). Expose the immutable catalog through `DomainWriteScenarios.all()` in closed
  inventory order and `DomainWriteScenarios.byId(String)`; expose the same package's
  `DomainWriteExclusionManifest.all()` as the reusable seven-entry exclusion manifest.
- Refactor Jackson 3 `ResourceMapperSpec` to derive the 14 named cases from the shared catalog
  and to retain only demonstrably major-specific cases locally; repoint every other jackson3 spec
  that imports a moved model to the shared package (verified by `rg` over the jackson3 test
  source set): `IdentifierConversionSpec` (`Person`), `ResourceMappingJacksonFeaturesSpec`
  (`Comment`; `Person` is reachable transitively via `Comment.author` but has no import to
  repoint), `ResourceMapperIsolationSpec` (`Article`), `ResourceBinderSpec`
  (`BlogWithJsonProperty`, `Comment`, `Person`), `SparseFieldsetSpec` (`Article`,
  `BlogWithJsonProperty`, `Comment`, `Person`), `CompoundSerializationSpec` (`Article`,
  `Comment`, `Person`, `Tag`), and `DomainDocumentReaderSpec` (`Comment`, `Person`) — without
  otherwise changing those adapter-local specs (several are owned by already-Complete milestones
  2.3, 2.8, 2.9, and 2.10, and must keep their behavior unchanged). The
  `ResourceBinderSpec` change here is import-only; Phase 2.14 performs its later catalog
  extraction after this milestone. Also add explicit `domainwrite` imports to the complete same-
  package Java helper inventory (`ArticleWithArray`, `ArticleWithOptionalRelationship`,
  `ArticleWithRenamedAuthor`, `ConflictArticle`, `AccessCountingArticle`,
  `AccessCountingFieldsetArticle`, `BaseComment`, and `ModeratedComment`) while keeping those
  adapter-specific helper models local. Keep
  `IdentifierConversionSpec`, `ResourceMappingJacksonFeaturesSpec`, and all
  serializer/mix-in/naming-strategy cases out of the shared catalog.
- Add a `TestFixturesDependencyRulesSpec` ArchUnit spec (per ADR-010) enforcing the
  `io.github.kazemek.jsonapi.testfixtures..` major-neutral allowlist described above — failing on
  any dependency on `tools.jackson..`, `com.fasterxml.jackson.databind..`, a major-specific adapter
  package, or `core.internal..` — rather than a source-import scan (prohibited by ADR-010); add a
  catalog-integrity spec (mirroring `CodecFixturesCatalogSpec`) asserting unique scenario ids, that
  every entry carries exactly one operation, typed input, envelope state, discriminated outcome,
  and comparison policy, that the declared catalog equals exactly the 14-case closed inventory and
  operation/input/comparison-policy matrix, and that `DomainWriteExclusionManifest` equals the seven
  fully qualified suite-id/
  category/reason triples above; amend ADR-010's "Current allowlists" to register the test-fixtures
  allowlist. The ArchUnit and catalog specs, `DomainWriteScenarios`, `DomainWriteExclusion`,
  `DomainWriteExclusionCategory`, and `DomainWriteExclusionManifest` all live under the fixed
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` package; the manifest is shared
  `src/main/java` data, not a test-local hard-coded list.
  Document the Jackson 2 extension workflow (run every applicable shared scenario and maintain a
  closed, reasoned exclusion manifest for that adapter) in the test-fixtures module docs.
- Use the `module-docs` skill to update `jsonapi-java-test-fixtures` documentation: add the new
  `io.github.kazemek.jsonapi.testfixtures.domainwrite` package to the package table; add the
  `DomainWriteScenarios.all()`/`byId(String)` and `DomainWriteExclusionManifest.all()`
  (`DomainWriteComparisonPolicy`, `DomainWriteExclusion`/`DomainWriteExclusionCategory`) entry
  points to "Minimal usage"; update
  "Non-goals" (remove the write-fixtures deferral, keep read, compound,
  sparse-fieldset, typed-envelope, and PATCH deferrals); extend the agent notes with the ArchUnit
  major-neutral boundary, null-bearing members, and the extension workflow; and refresh the
  existing `jsonapi-java-test-fixtures` row in the root `README.md` **Project structure** table
  (not the Module registry table, which is reserved for published/available modules) to mention the
  shared domain-write catalog.

## Non-goals

- Compound inclusion and sparse-fieldset catalogs (Phases 2.24 and 2.25).
- Flat DTO read fixtures (Phase 2.14), PATCH binding (Phase 2.15), and typed-envelope read
  fixtures (Phase 2.26).
- Sharing production mapping implementations or introducing a common Jackson introspection API
  (ADR-004).
- Moving or extracting major-specific models or specs (`IdentifierConversionSpec`,
  `ResourceMappingJacksonFeaturesSpec`, and any suite in `DomainWriteExclusionManifest`) into the
  shared catalog; the manifest is the finite exclusion boundary for this milestone.
- Moving or refactoring the flat DTO-binding scenarios in `ResourceBinderSpec`; Phase 2.14 owns
  that catalog after consuming the shared models and import migration from this milestone.
- Changing the mapper contract, introducing new mapping behaviors, adding Jackson 2, or altering
  the Jackson-visible property shapes/annotations of the moved models (preserves ADR-004
  introspection parity).
- Weakening or exempting ADR-010's project-wide ArchUnit policy, or changing another module's
  allowlist; this milestone registers a new test-fixtures allowlist under the existing policy and
  uses ArchUnit (not a source-import scan) for the major-neutral coupling check.

## Implementation boundaries

- Shared models and catalog import only `io.github.kazemek.jsonapi.annotation.*`,
  `io.github.kazemek.jsonapi.core.model.*`, `io.github.kazemek.jsonapi.core.validation.*`,
  `io.github.kazemek.jsonapi.jackson.*` (Phase 2.11 common), `com.fasterxml.jackson.annotation.*`
  (jackson-annotations), `org.jspecify.annotations.*` (compile-only JSpecify), and JDK types — never `tools.jackson.*`,
  `com.fasterxml.jackson.databind.*`, `io.github.kazemek.jsonapi.core.internal`, or a major-specific
  adapter package.
- Expected outcomes are core/common values (`ResourceObject`, `RelationshipData` linkage variants,
  `JsonApiDocument`, `DocumentData` variants, `Links`, `Meta`, `JsonApiObject`, `DocumentEnvelope`);
  never adapter implementation classes and never serialized text.
- Relationship expectations preserve the relationship key, linkage variant, exact identifier
  type/id values, and deterministic order for single/List values; the Set-based `tags` expectation
  compares the exact identifier pairs as an unordered set. No relationship scenario populates
  `included`.
- `DomainWriteScenario` owns a non-null `DomainWriteComparisonPolicy` with an immutable relationship
  name/order map; the only allowed unordered value is `UNORDERED_IDENTIFIER_PAIRS` for `tags`, and
  every other relationship comparison is `ORDERED`. Catalog integrity rejects missing, unknown, or
  over-broad policy entries.
- The fixed Java package `io.github.kazemek.jsonapi.testfixtures.domainwrite` under
  `src/main/java` owns the models, `DomainWriteScenario` operation/input/outcome types,
  `DomainWriteComparisonPolicy`, `DomainWriteScenarios`, and `DomainWriteExclusionManifest`; the
  package is `@NullMarked` per
  ADR-009. `Supplier<@Nullable Object>` and `@Nullable DocumentEnvelope` preserve the catalog's
  intentional null states, while outcome values and non-null collection suppliers remain non-null.
  NullAway and `RequireExplicitNullMarking` (wired in `jsonapi-java-library`) enforce this at
  compile time.
- `jackson-annotations` is `implementation` in test-fixtures (runtime-retained; consumers already
  resolve it via their own databind). No Jackson databind/core dependency enters test-fixtures.
- The major-neutral coupling above is enforced by a `TestFixturesDependencyRulesSpec` ArchUnit rule
  (per ADR-010; `testImplementation(libs.archunit)`), not a source-import scan. ArchUnit sees both
  the Groovy `codec` and the Java `domainwrite` bytecode, so the registered
  `io.github.kazemek.jsonapi.testfixtures..` allowlist covers both.
- Adapter-local specs that reference moved models keep their major-specific behavior unchanged;
  only their import of a moved model is repointed. `ResourceMapperSpec` invokes the catalog through
  the operation/input descriptor and discriminated outcome, not scenario-id conditionals. The
  fixed package and named `DomainWriteScenarios`/`DomainWriteExclusionManifest` types are the only
  shared domain-write entry points; Phase 2.14 consumes them and must not define duplicate model
  classes or a competing manifest. The
  `jsonapi-java-annotations` project and
  `jackson-annotations` artifact dependencies are scoped to `jsonapi-java-test-fixtures` and do
  not flow into `jsonapi-java-core` or `jsonapi-java-jackson-common`.

## Test strategy

- A new `TestFixturesDependencyRulesSpec` ArchUnit rule enforces the major-neutral coupling (per
  ADR-010), and a new catalog-integrity spec verifies the 14-case closed inventory, the exact
  operation/input/envelope/outcome matrix, unique ids, complete expected outcomes, and equality
  with the closed `DomainWriteExclusionManifest`.
- Jackson 3 `ResourceMapperSpec` parameterizes the 14 cases from the shared catalog, invokes each
  through its operation/input descriptor, and compares full semantic results — `type`, `id`, `lid`,
  attribute keys/values, relationship keys, linkage variants, exact identifier type/id values,
  ordered List identifiers, unordered Set identifiers, document data, envelope members, and
  absent `included` — plus the adapter-entry-point `NullPointerException`, never serialized text.
- Existing Jackson 3-only serializer/mix-in/naming-strategy/isolation cases (notably
  `TitleSerializer`, `@JsonSerialize`, `@JsonIgnore`, `PropertyNamingStrategies`) remain in
  adapter-local specs and continue to pass, with shared-model imports repointed where needed.
- Every repointed adapter-local spec (`IdentifierConversionSpec`,
  `ResourceMappingJacksonFeaturesSpec`, `ResourceMapperIsolationSpec`, `ResourceBinderSpec`,
  `SparseFieldsetSpec`, `CompoundSerializationSpec`, and `DomainDocumentReaderSpec`) passes
  unchanged behaviorally; their assertions are not modified. The Java helper inventory also
  compiles with explicit shared-package imports while retaining its adapter-specific helper types.

## Acceptance criteria

- [ ] The 8 shared models live in the fixed `@NullMarked` Java package
      `io.github.kazemek.jsonapi.testfixtures.domainwrite` under
      `jsonapi-java-test-fixtures/src/main/java/` with no `tools.jackson.*` or
      `com.fasterxml.jackson.databind.*` imports; `Article.author`, `Comment.author`,
      `Person.name`, `Comment.body`, and `SamplePojo.{id, name, comments}` are `@Nullable`; the
      7 moved records are deleted from `jsonapi-java-jackson3/.../testmodel/` and the inline
      `SamplePojo` is removed from `ResourceMapperSpec`; the complete same-package Java helper
      inventory (`ArticleWithArray`, `ArticleWithOptionalRelationship`, `ArticleWithRenamedAuthor`,
      `ConflictArticle`, `AccessCountingArticle`, `AccessCountingFieldsetArticle`, `BaseComment`,
      and `ModeratedComment`) remains local with explicit imports from `domainwrite`, and every
      Java/Groovy test-source reference to a deleted model resolves to the shared package.
- [ ] A shared flat-write-mapping catalog exposes exactly the 14 named scenarios with stable ids
      equal to the closed `ResourceMapperSpec` test names; every entry has exactly one operation,
      the matching typed single/collection input, the correct nullable/non-null envelope state,
      and one discriminated success/failure outcome. Success values are `ResourceObject` for
      `TO_RESOURCE` or `JsonApiDocument` for document operations; document data, envelope members,
      exact relationship identifiers/type/id values, ordered List identifiers, unordered Set
      identifiers, and absent `included` are asserted semantically. The null-input entry uses
      `Supplier<@Nullable Object>` and expects `NullPointerException`; the Java catalog package
      has `@NullMarked` with accurate `@Nullable` on every nullable catalog field/type use, each
      entry carries a `DomainWriteComparisonPolicy`, and only the Set `tags` entry uses
      `UNORDERED_IDENTIFIER_PAIRS` while all other relationships use `ORDERED`; and
      `ResourceMapperSpec` dispatches by operation rather than scenario id, and the catalog is
      consumed through `DomainWriteScenarios.all()`/`byId(String)`.
- [ ] Jackson 3 `ResourceMapperSpec` derives the 14 named cases from the shared catalog and
      retains only the named major-specific cases locally; the named adapter-local exclusions
      (`IdentifierConversionSpec`, `ResourceMappingJacksonFeaturesSpec`,
      `TitleSerializer`/`@JsonSerialize`, major-specific mix-ins) are documented and absent from
      the shared catalog; no `io.github.kazemek.jsonapi.jackson3.testmodel.*` import remains for
      the 8 moved models; every other jackson3 spec that imported a moved model
      (`IdentifierConversionSpec`, `ResourceMappingJacksonFeaturesSpec`,
      `ResourceMapperIsolationSpec`, `ResourceBinderSpec`, `SparseFieldsetSpec`,
      `CompoundSerializationSpec`, `DomainDocumentReaderSpec`) keeps its behavior unchanged with
      moved-model imports repointed; Phase 2.14 owns the subsequent `ResourceBinderSpec` catalog
      extraction and reuses the Phase 2.13 package/models.
- [ ] A `TestFixturesDependencyRulesSpec` ArchUnit rule (per ADR-010) fails on any dependency from
      `io.github.kazemek.jsonapi.testfixtures..` on `tools.jackson..`,
      `com.fasterxml.jackson.databind..`, a major-specific adapter package, or `core.internal..`;
      a catalog-integrity spec fails on missing/duplicate scenario ids, an invalid operation/input/
      envelope/outcome combination, incomplete expected outcomes, a catalog or operation matrix
      that is not exactly the 14-case set, an invalid/missing comparison policy, or a
      `DomainWriteExclusionManifest` that is not exactly
      the seven fully qualified suite-id/category/reason triples defined in the milestone; the
      manifest is the shared `src/main/java` entry point
      `io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteExclusionManifest`; ADR-010's
      "Current allowlists" registers the test-fixtures allowlist; the Jackson 2 extension workflow
      (run every applicable shared scenario and maintain a closed, reasoned exclusion manifest) is
      documented.
- [ ] `jsonapi-java-test-fixtures` adds `api(project(":jsonapi-java-annotations"))`,
      `implementation(libs.jackson.annotations)`, and `testImplementation(libs.archunit)`, and no
      Jackson databind/core production/runtime dependency; the canonical `module-docs` checklist
      passes for the fixed `io.github.kazemek.jsonapi.testfixtures.domainwrite` package map,
      `DomainWriteScenarios.all()`/`byId(String)` and `DomainWriteExclusionManifest.all()` entry
      points, and non-goals update, and
      the existing `jsonapi-java-test-fixtures` row in the root `README.md` **Project structure**
      table is refreshed to mention the shared domain-write catalog (no Module registry row, which
      is reserved for published/available modules).
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that
      CI must still pass the gate.
