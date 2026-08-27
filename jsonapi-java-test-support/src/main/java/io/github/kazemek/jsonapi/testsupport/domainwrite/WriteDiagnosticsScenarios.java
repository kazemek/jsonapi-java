package io.github.kazemek.jsonapi.testsupport.domainwrite;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.WriteDiagnosticsFixtures;
import java.util.List;

/**
 * The shared resource-write diagnostics catalog consumed by Jackson-major contract tests.
 *
 * <p>Each entry maps one deliberately mis-declared or invalid-instance carrier through the
 * adapter's own writer and pins the expected semantic diagnostic category plus either the stable
 * wire location (resource-relative JSON Pointer) or an absent location for class-level and
 * specification failures. {@code RelationshipLinkage} declaration failures (ADR-017) live here so
 * every Jackson major iterates them. The catalog grows by addition; adapter suites pick entries up
 * through {@link #catalog()} and dispatch on {@link WriteDiagnosticScenario}, never on a scenario
 * id.
 */
public final class WriteDiagnosticsScenarios {

  private static final String COMMENTS = "comments";
  private static final String PEOPLE = "people";
  private static final String EDITOR = "editor";
  private static final String AUTHOR_DATA_META = "/relationships/author/data/meta";

  private static final List<WriteDiagnosticScenario> SCENARIOS =
      List.of(
          new WriteDiagnosticScenario(
              "missing-resource-annotation",
              "Unannotated instance is rejected as MISSING_RESOURCE_ANNOTATION with no location",
              Object::new,
              MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
              null),
          new WriteDiagnosticScenario(
              "empty-resource-type",
              "Empty @JsonApiResource type is INVALID_RESOURCE_TYPE with no location",
              () -> new WriteDiagnosticsFixtures.EmptyTypeEntity("1"),
              MappingDiagnostic.INVALID_RESOURCE_TYPE,
              null),
          new WriteDiagnosticScenario(
              "invalid-resource-type-characters",
              "Type containing forbidden characters is INVALID_RESOURCE_TYPE with no location",
              () -> new WriteDiagnosticsFixtures.InvalidTypeEntity("1"),
              MappingDiagnostic.INVALID_RESOURCE_TYPE,
              null),
          new WriteDiagnosticScenario(
              "missing-identifier-property",
              "Annotated resource without identifier property is MISSING_IDENTIFIER",
              () -> new WriteDiagnosticsFixtures.NoIdEntity("test"),
              MappingDiagnostic.MISSING_IDENTIFIER,
              null),
          new WriteDiagnosticScenario(
              "null-identifier-value-at-id",
              "Null identifier value is MISSING_IDENTIFIER at the /id coordinate",
              () -> new WriteDiagnosticsFixtures.NullIdEntity(null),
              MappingDiagnostic.MISSING_IDENTIFIER,
              "/id"),
          new WriteDiagnosticScenario(
              "duplicate-role-annotations",
              "Identifier member carrying a second role annotation is DUPLICATE_ROLE",
              () -> new WriteDiagnosticsFixtures.DuplicateRoleEntity("1"),
              MappingDiagnostic.DUPLICATE_ROLE,
              null),
          new WriteDiagnosticScenario(
              "attribute-relationship-name-collision",
              "Attribute and relationship mapped to one name are NAME_COLLISION",
              () -> new WriteDiagnosticsFixtures.NameCollisionEntity("1", "a", "b"),
              MappingDiagnostic.NAME_COLLISION,
              null),
          new WriteDiagnosticScenario(
              "duplicate-attribute-names-at-container",
              "Two attributes sharing a wire name are NAME_COLLISION at the attribute container",
              () -> new WriteDiagnosticsFixtures.DuplicateAttrNameEntity("1", "a", "b"),
              MappingDiagnostic.NAME_COLLISION,
              "/attributes/same"),
          new WriteDiagnosticScenario(
              "duplicate-relationship-names-at-data",
              "Two relationships sharing a wire name are NAME_COLLISION at the linkage coordinate",
              () -> new WriteDiagnosticsFixtures.DuplicateRelNameEntity("1", "a", "b"),
              MappingDiagnostic.NAME_COLLISION,
              "/relationships/same/data"),
          new WriteDiagnosticScenario(
              "invalid-attribute-name-override",
              "Attribute override with forbidden characters is INVALID_ATTRIBUTE_NAME",
              () -> new WriteDiagnosticsFixtures.InvalidAttrNameEntity("1", "v"),
              MappingDiagnostic.INVALID_ATTRIBUTE_NAME,
              null),
          new WriteDiagnosticScenario(
              "reserved-attribute-name-type",
              "Attribute override using reserved name 'type' is INVALID_ATTRIBUTE_NAME",
              () -> new WriteDiagnosticsFixtures.ReservedAttrNameEntity("1", "v"),
              MappingDiagnostic.INVALID_ATTRIBUTE_NAME,
              null),
          new WriteDiagnosticScenario(
              "invalid-relationship-name-override",
              "Relationship override with forbidden characters is INVALID_RELATIONSHIP_NAME",
              () -> new WriteDiagnosticsFixtures.InvalidRelNameEntity("1", "o"),
              MappingDiagnostic.INVALID_RELATIONSHIP_NAME,
              null),
          new WriteDiagnosticScenario(
              "reserved-relationship-name-id",
              "Relationship override using reserved name 'id' is INVALID_RELATIONSHIP_NAME",
              () -> new WriteDiagnosticsFixtures.ReservedRelNameEntity("1", "o"),
              MappingDiagnostic.INVALID_RELATIONSHIP_NAME,
              null),
          new WriteDiagnosticScenario(
              "failing-attribute-getter-at-wire-name",
              "Throwing attribute getter is UNSUPPORTED_ATTRIBUTE_VALUE at the member coordinate",
              () -> new WriteDiagnosticsFixtures.FailingAttrEntity("1", "anything"),
              MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
              "/attributes/badAttr"),
          new WriteDiagnosticScenario(
              "renamed-failing-attribute-getter-reports-wire-name",
              "Renamed throwing getter reports /attributes/body-text, never the logical name",
              () -> new WriteDiagnosticsFixtures.RenamedFailingAttrEntity("1", "anything"),
              MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
              "/attributes/body-text"),
          new WriteDiagnosticScenario(
              "failing-identifier-getter-at-id",
              "Throwing identifier getter is MISSING_IDENTIFIER at /id",
              () -> new WriteDiagnosticsFixtures.FailingIdEntity("1"),
              MappingDiagnostic.MISSING_IDENTIFIER,
              "/id"),
          new WriteDiagnosticScenario(
              "write-only-accessor-missing-reader",
              "Annotated property without readable accessor is MISSING_ACCESSOR at its coordinate",
              () -> {
                var entity = new WriteDiagnosticsFixtures.MissingAccessorEntity("1");
                entity.setSecret("hidden");
                return entity;
              },
              MappingDiagnostic.MISSING_ACCESSOR,
              "/attributes/secret"),
          new WriteDiagnosticScenario(
              "unsupported-runtime-collection-shape-at-data",
              "long[] to-many relationship is UNSUPPORTED_RELATIONSHIP_VALUE at the renamed data "
                  + "coordinate",
              () -> new WriteDiagnosticsFixtures.RenamedArrayRelEntity("1", new long[] {1L, 2L}),
              MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
              "/relationships/ext-values/data"),
          new WriteDiagnosticScenario(
              "mixed-to-many-elements-at-data",
              "Mixed to-many elements are UNSUPPORTED_RELATIONSHIP_VALUE at the relationship data",
              () ->
                  new WriteDiagnosticsFixtures.RenamedMixedRelEntity(
                      "1",
                      List.of(
                          io.github.kazemek.jsonapi.core.model.ResourceIdentifier.of(COMMENTS, "1"),
                          new Object())),
              MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
              "/relationships/ext-items/data"),
          new WriteDiagnosticScenario(
              "unresolvable-declared-collection-content-at-data",
              "Raw erased iterable to-many is UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE at data",
              () ->
                  new WriteDiagnosticsFixtures.RenamedBagRelEntity(
                      "1", new WriteDiagnosticsFixtures.RawBag(new Object())),
              MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
              "/relationships/ext-bag/data"),
          new WriteDiagnosticScenario(
              "raw-relationship-linkage",
              "Raw RelationshipLinkage is UNRESOLVED_GENERIC_TYPE at relationship data",
              () ->
                  new WriteDiagnosticsFixtures.RawRelationshipLinkageEntity(
                      "1", new RelationshipLinkage<>(ResourceIdentifier.of(PEOPLE, "p1"), null)),
              MappingDiagnostic.UNRESOLVED_GENERIC_TYPE,
              "/relationships/author/data"),
          new WriteDiagnosticScenario(
              "nested-relationship-linkage-target",
              "Nested RelationshipLinkage as target is INVALID_IDENTIFIER_META_TARGET",
              () ->
                  new WriteDiagnosticsFixtures.NestedRelationshipLinkageEntity(
                      "1",
                      new RelationshipLinkage<>(
                          new RelationshipLinkage<>(
                              ResourceIdentifier.of(PEOPLE, "p1"), new AuthorIdMeta(EDITOR)),
                          new AuthorIdMeta(EDITOR))),
              MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET,
              AUTHOR_DATA_META),
          new WriteDiagnosticScenario(
              "scalar-relationship-linkage-meta",
              "Scalar RelationshipLinkage meta type is INVALID_IDENTIFIER_META_TARGET",
              () ->
                  new WriteDiagnosticsFixtures.ScalarMetaRelationshipLinkageEntity(
                      "1", new RelationshipLinkage<>(ResourceIdentifier.of(PEOPLE, "p1"), EDITOR)),
              MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET,
              AUTHOR_DATA_META),
          new WriteDiagnosticScenario(
              "list-relationship-linkage-meta",
              "List RelationshipLinkage meta type is INVALID_IDENTIFIER_META_TARGET",
              () ->
                  new WriteDiagnosticsFixtures.ListMetaRelationshipLinkageEntity(
                      "1",
                      new RelationshipLinkage<>(
                          ResourceIdentifier.of(PEOPLE, "p1"), List.of(new AuthorIdMeta(EDITOR)))),
              MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET,
              AUTHOR_DATA_META));

  private static final FixtureCatalog<WriteDiagnosticScenario> CATALOG =
      FixtureCatalog.of("write-diagnostics", SCENARIOS);

  private WriteDiagnosticsScenarios() {}

  public static FixtureCatalog<WriteDiagnosticScenario> catalog() {
    return CATALOG;
  }
}
