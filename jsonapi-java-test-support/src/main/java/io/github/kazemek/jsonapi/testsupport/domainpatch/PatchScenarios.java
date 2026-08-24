package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.validation.EndpointIdentity;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import io.github.kazemek.jsonapi.jackson.StructuredMember;
import io.github.kazemek.jsonapi.jackson.StructuredMemberState;
import io.github.kazemek.jsonapi.jackson.StructuredPatch;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.TestSupportResources;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.Article;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithBox;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithBoxList;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithContainerAddress;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithDimensions;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithGeoAddress;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMapMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalAddress;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalCity;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithTags;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.MutableArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.PatchPresenceAddressArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.PatchPresenceAddressPatchArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.PatchPresenceTitleArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatCountedThing;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatIntIdArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatThingWithIgnored;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatUnregisteredRelationshipsArticle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The shared presence-aware PATCH catalog consumed by Jackson-major contract tests.
 *
 * <p>Scenario documents are named classpath resources under {@code jsonapi/corpus/1.1/patch/};
 * documents that low-level PATCH and typed PATCH consume identically reference the same resource.
 * The catalog grows by addition: scenarios are added as the PATCH surface grows, and adapter suites
 * pick them up through {@link #catalog()}. Consumers dispatch on {@link PatchExpectation}, never on
 * a scenario id.
 */
public final class PatchScenarios {

  private static final String ARTICLES = "articles";
  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";
  private static final String TITLE = "title";
  private static final String AUTHOR = "author";
  private static final String ADDRESS = "address";
  private static final String STREET = "street";
  private static final String NUMBERS = "numbers";
  private static final String SOURCE = "source";
  private static final String DISPLAY_NAME = "displayName";

  private static final List<PatchScenario> SCENARIOS =
      List.of(
          omittedAndSuppliedAttributes(),
          explicitNullAttribute(),
          attributeRename(),
          ignoredUnmappedOmittedFromChanges(),
          relationshipNullLinkage(),
          relationshipSingleLinkage(),
          relationshipEmptyCollection(),
          relationshipNonEmptyCollection(),
          relationshipCardinalityMismatch(),
          compoundIncludedIgnored(),
          endpointIdentityMismatch(),
          missingRelationshipData(),
          wrongPrimaryShape(),
          resourceTypeMismatch(),
          identifierConversionFailure(),
          attributeConversionFailure(),
          unsupportedRelationshipTarget(),
          ordinaryDomainNestedPartial(),
          ordinaryDomainNestedMultiLevel(),
          ordinaryDomainOptionalObject(),
          ordinaryDomainOptionalEmptyObject(),
          ordinaryDomainOptionalNull(),
          ordinaryDomainNestedOptionalMember(),
          ordinaryDomainUnknownNestedSkip(),
          ordinaryDomainNestedPrimitiveNull(),
          ordinaryDomainContainerAtomic(),
          ordinaryDomainGenericNestedJavaType(),
          ordinaryDomainGenericNestedMultiLevelJavaType(),
          ordinaryDomainContainerAtomicSet(),
          ordinaryDomainContainerAtomicMap(),
          lowLevelPresenceScalar(),
          lowLevelPresenceOrdinaryDomain(),
          lowLevelPresenceShapeRejected(),
          ordinaryDomainJavaBeanNestedPartial(),
          resourceMetaStructuredWithOrdering(),
          resourceMetaAtomicMap(),
          relationshipMetaWithData(),
          resourceMetaSuppliedUnmappedSkipped());

  private static final FixtureCatalog<PatchScenario> CATALOG =
      FixtureCatalog.of("patch", SCENARIOS);

  private PatchScenarios() {}

  public static FixtureCatalog<PatchScenario> catalog() {
    return CATALOG;
  }

  private static String doc(String stem) {
    return TestSupportResources.readCorpusUtf8("patch/" + stem + ".json");
  }

  private static PatchScenario omittedAndSuppliedAttributes() {
    return scenario(
        "patch-omitted-and-supplied-attributes",
        doc("omitted-and-supplied-attributes"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, "Hello"))));
  }

  private static PatchScenario explicitNullAttribute() {
    return scenario(
        "patch-explicit-null-attribute",
        doc("explicit-null-attribute"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, null))));
  }

  private static PatchScenario attributeRename() {
    return scenario(
        "patch-attribute-rename",
        doc("attribute-rename"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange("body-text", "body", "Content"))));
  }

  private static PatchScenario ignoredUnmappedOmittedFromChanges() {
    return scenario(
        "patch-ignored-unmapped-omitted-from-changes",
        doc("ignored-unmapped-attributes"),
        FlatThingWithIgnored.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange("name", "name", "visible"))));
  }

  private static PatchScenario relationshipNullLinkage() {
    return scenario(
        "patch-relationship-null-linkage",
        doc("relationship-null-linkage"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.RelationshipChange(AUTHOR, AUTHOR, null))));
  }

  private static PatchScenario relationshipSingleLinkage() {
    return scenario(
        "patch-relationship-single-linkage",
        doc("relationship-single-linkage"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.RelationshipChange(
                    AUTHOR, AUTHOR, ResourceIdentifier.of(PEOPLE, "p1")))));
  }

  private static PatchScenario relationshipEmptyCollection() {
    return scenario(
        "patch-relationship-empty-collection",
        doc("relationship-empty-collection"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.RelationshipChange(COMMENTS, COMMENTS, List.of()))));
  }

  private static PatchScenario relationshipNonEmptyCollection() {
    return scenario(
        "patch-relationship-non-empty-collection",
        doc("relationship-non-empty-collection"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.RelationshipChange(
                    COMMENTS,
                    COMMENTS,
                    List.of(
                        ResourceIdentifier.of(COMMENTS, "c1"),
                        ResourceIdentifier.of(COMMENTS, "c2"))))));
  }

  private static PatchScenario relationshipCardinalityMismatch() {
    return scenario(
        "patch-relationship-cardinality-mismatch",
        doc("relationship-cardinality-mismatch"),
        FlatArticle.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, "/relationships/author/data"));
  }

  private static PatchScenario compoundIncludedIgnored() {
    return scenario(
        "patch-compound-included-ignored",
        doc("compound-included-ignored"),
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(TITLE, TITLE, "T"),
                new PatchChange.RelationshipChange(
                    AUTHOR, AUTHOR, ResourceIdentifier.of(PEOPLE, "p1")))));
  }

  private static PatchScenario endpointIdentityMismatch() {
    return scenario(
        "patch-endpoint-identity-mismatch",
        doc("title-only"),
        FlatArticle.class,
        new EndpointIdentity(ARTICLES, "99"),
        PatchExpectation.readerFailure(ValidationRuleCode.ENDPOINT_IDENTITY_MISMATCH, "/data/id"));
  }

  private static PatchScenario missingRelationshipData() {
    return scenario(
        "patch-missing-relationship-data",
        doc("missing-relationship-data"),
        FlatArticle.class,
        null,
        PatchExpectation.readerFailure(
            ValidationRuleCode.RELATIONSHIP_DATA_REQUIRED, "/data/relationships/author/data"));
  }

  private static PatchScenario wrongPrimaryShape() {
    return scenario(
        "patch-wrong-primary-shape",
        doc("wrong-primary-shape"),
        FlatArticle.class,
        null,
        PatchExpectation.readerFailure(
            ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"));
  }

  private static PatchScenario resourceTypeMismatch() {
    return scenario(
        "patch-resource-type-mismatch",
        doc("resource-type-mismatch"),
        FlatArticle.class,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"));
  }

  private static PatchScenario identifierConversionFailure() {
    return scenario(
        "patch-identifier-conversion-failure",
        doc("identifier-not-an-integer"),
        FlatIntIdArticle.class,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id"));
  }

  private static PatchScenario attributeConversionFailure() {
    return scenario(
        "patch-attribute-conversion-failure",
        doc("attribute-conversion-failure"),
        FlatCountedThing.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE, "/attributes/count"));
  }

  private static PatchScenario unsupportedRelationshipTarget() {
    return scenario(
        "patch-unsupported-relationship-target",
        doc("relationship-single-linkage"),
        FlatUnregisteredRelationshipsArticle.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET, "/relationships/author/data"));
  }

  private static PatchScenario ordinaryDomainNestedPartial() {
    return scenario(
        "patch-ordinary-domain-nested-partial",
        doc("address-street-new-street"),
        Article.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET,
                                STREET,
                                new StructuredMemberState.Atomic("New Street"))))))));
  }

  private static PatchScenario ordinaryDomainNestedMultiLevel() {
    return scenario(
        "patch-ordinary-domain-nested-multi-level",
        doc("address-street-and-geo-lat"),
        ArticleWithGeoAddress.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S")),
                            new StructuredMember(
                                "geo",
                                "geo",
                                new StructuredMemberState.Structured(
                                    List.of(
                                        new StructuredMember(
                                            "lat",
                                            "lat",
                                            new StructuredMemberState.Atomic("1")))))))))));
  }

  private static PatchScenario ordinaryDomainOptionalObject() {
    return scenario(
        "patch-ordinary-domain-optional-object",
        doc("address-street-new-street"),
        ArticleWithOptionalAddress.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET,
                                STREET,
                                new StructuredMemberState.Atomic("New Street"))))))));
  }

  private static PatchScenario ordinaryDomainOptionalEmptyObject() {
    return scenario(
        "patch-ordinary-domain-optional-empty-object",
        doc("address-empty-object"),
        ArticleWithOptionalAddress.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS, ADDRESS, new StructuredPatch(List.of())))));
  }

  private static PatchScenario ordinaryDomainOptionalNull() {
    return scenario(
        "patch-ordinary-domain-optional-null",
        doc("address-explicit-null"),
        ArticleWithOptionalAddress.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange(ADDRESS, ADDRESS, null))));
  }

  private static PatchScenario ordinaryDomainNestedOptionalMember() {
    return scenario(
        "patch-ordinary-domain-nested-optional-member",
        doc("address-street-city-null"),
        ArticleWithOptionalCity.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S")),
                            new StructuredMember(
                                "city",
                                "city",
                                new StructuredMemberState.Atomic(Optional.empty()))))))));
  }

  private static PatchScenario ordinaryDomainUnknownNestedSkip() {
    return scenario(
        "patch-ordinary-domain-unknown-nested-skip",
        doc("address-bogus-and-street"),
        Article.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S"))))))));
  }

  private static PatchScenario ordinaryDomainNestedPrimitiveNull() {
    return scenario(
        "patch-ordinary-domain-nested-primitive-null",
        doc("dimensions-width-null"),
        ArticleWithDimensions.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE, "/attributes/dimensions/width"));
  }

  private static PatchScenario ordinaryDomainContainerAtomic() {
    return scenario(
        "patch-ordinary-domain-container-atomic",
        doc("tags-top-level"),
        ArticleWithTags.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange("tags", "tags", List.of("a", "b")))));
  }

  private static PatchScenario ordinaryDomainGenericNestedJavaType() {
    return scenario(
        "patch-ordinary-domain-generic-nested-javatype",
        doc("box-numbers"),
        ArticleWithBox.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    "box",
                    "box",
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                NUMBERS,
                                NUMBERS,
                                new StructuredMemberState.Atomic(List.of(1, 2)))))))));
  }

  private static PatchScenario ordinaryDomainGenericNestedMultiLevelJavaType() {
    return scenario(
        "patch-ordinary-domain-generic-nested-multilevel-javatype",
        doc("box-numbers-nested-lists"),
        ArticleWithBoxList.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    "box",
                    "box",
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                NUMBERS,
                                NUMBERS,
                                new StructuredMemberState.Atomic(
                                    List.of(List.of(1, 2), List.of(3))))))))));
  }

  private static PatchScenario ordinaryDomainContainerAtomicSet() {
    return scenario(
        "patch-ordinary-domain-container-atomic-set",
        doc("address-street-aliases"),
        ArticleWithContainerAddress.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S")),
                            new StructuredMember(
                                "aliases",
                                "aliases",
                                new StructuredMemberState.Atomic(Set.of("a", "b")))))))));
  }

  private static PatchScenario ordinaryDomainContainerAtomicMap() {
    return scenario(
        "patch-ordinary-domain-container-atomic-map",
        doc("address-street-scores"),
        ArticleWithContainerAddress.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S")),
                            new StructuredMember(
                                "scores",
                                "scores",
                                new StructuredMemberState.Atomic(Map.of("x", 1, "y", 2)))))))));
  }

  private static PatchScenario lowLevelPresenceScalar() {
    return scenario(
        "patch-lowlevel-presence-scalar",
        doc("title-only"),
        PatchPresenceTitleArticle.class,
        null,
        PatchExpectation.success("1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, "T"))));
  }

  private static PatchScenario lowLevelPresenceOrdinaryDomain() {
    return scenario(
        "patch-lowlevel-presence-ordinary-domain",
        doc("address-street"),
        PatchPresenceAddressArticle.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S"))))))));
  }

  private static PatchScenario lowLevelPresenceShapeRejected() {
    return scenario(
        "patch-lowlevel-presence-shape-rejected",
        doc("address-street"),
        PatchPresenceAddressPatchArticle.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/attributes/address"));
  }

  private static PatchScenario ordinaryDomainJavaBeanNestedPartial() {
    return scenario(
        "patch-ordinary-domain-javabean-nested-partial",
        doc("address-street"),
        MutableArticle.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.AttributeChange(
                    ADDRESS,
                    ADDRESS,
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                STREET, STREET, new StructuredMemberState.Atomic("S"))))))));
  }

  private static PatchScenario resourceMetaStructuredWithOrdering() {
    return scenario(
        "patch-resource-meta-structured-ordering",
        doc("meta-source-note-author-meta"),
        ArticleWithMeta.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.ResourceMetaChange(
                    "meta",
                    "meta",
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                SOURCE, SOURCE, new StructuredMemberState.Atomic("cms")),
                            new StructuredMember(
                                "note", "note", new StructuredMemberState.Atomic("n"))))),
                new PatchChange.AttributeChange(TITLE, TITLE, "T"),
                new PatchChange.RelationshipChange(
                    AUTHOR, AUTHOR, ResourceIdentifier.of(PEOPLE, "p1")),
                new PatchChange.RelationshipMetaChange(
                    AUTHOR,
                    "authorMeta",
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                DISPLAY_NAME,
                                DISPLAY_NAME,
                                new StructuredMemberState.Atomic("Alice"))))))));
  }

  private static PatchScenario resourceMetaAtomicMap() {
    return scenario(
        "patch-resource-meta-atomic-map",
        doc("title-with-meta-source"),
        ArticleWithMapMeta.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.ResourceMetaChange("meta", "meta", Map.of(SOURCE, "cms")),
                new PatchChange.AttributeChange(TITLE, TITLE, "T"))));
  }

  private static PatchScenario relationshipMetaWithData() {
    return scenario(
        "patch-relationship-meta-with-data",
        doc("author-meta-with-data"),
        ArticleWithMeta.class,
        null,
        PatchExpectation.success(
            "1",
            List.of(
                new PatchChange.RelationshipChange(
                    AUTHOR, AUTHOR, ResourceIdentifier.of(PEOPLE, "p1")),
                new PatchChange.RelationshipMetaChange(
                    AUTHOR,
                    "authorMeta",
                    new StructuredPatch(
                        List.of(
                            new StructuredMember(
                                DISPLAY_NAME,
                                DISPLAY_NAME,
                                new StructuredMemberState.Atomic("Alice"))))))));
  }

  private static PatchScenario resourceMetaSuppliedUnmappedSkipped() {
    return scenario(
        "patch-resource-meta-supplied-unmapped-skipped",
        doc("title-with-meta-source"),
        FlatArticle.class,
        null,
        PatchExpectation.success("1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, "T"))));
  }

  private static PatchScenario scenario(
      String id,
      String documentJson,
      Class<?> targetType,
      @Nullable EndpointIdentity expectedEndpointIdentity,
      PatchExpectation expectation) {
    return new PatchScenario(id, documentJson, targetType, expectedEndpointIdentity, expectation);
  }
}
