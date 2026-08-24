package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.TestSupportResources;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AddressWithContainersPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AddressWithGeoPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AddressWithOptionalCityPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AddressWithTagsPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMetaPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticlePatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithAddressTagsPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithBoxPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithContainerAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithDirectPresentAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithGeoPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMapMetaPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMetaPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMixedAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalCityPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalMetaPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithRawAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.BoxPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.DirectPresentPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.GeoPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.IntIdPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.MutableAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.MutableArticleWithAddressPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.NonPatchPresencePatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.OptionalPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.PresenceIdPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.RawPatchPresencePatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.UnannotatedPatch;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The shared direct typed PATCH DTO catalog consumed by Jackson-major contract tests.
 *
 * <p>Scenario documents are named classpath resources under {@code jsonapi/corpus/1.1/patch/};
 * documents that low-level PATCH and typed PATCH consume identically reference the same resource.
 * The catalog grows by addition: scenarios are added as the direct PATCH DTO surface grows, and
 * adapter suites pick them up through {@link #all()}. Consumers dispatch on {@link
 * PatchDtoExpectation}, never on a scenario id.
 */
public final class PatchDtoScenarios {

  private static final String TITLE = "title";
  private static final String BODY = "body";
  private static final String AUTHOR = "author";
  private static final String COMMENTS = "comments";
  private static final String SUBTITLE = "subtitle";
  private static final String ADDRESS = "address";
  private static final String PEOPLE = "people";
  private static final String TITLE_PATH = "/attributes/title";
  private static final String ADDRESS_ATTRIBUTE_PATH = "/attributes/address";

  private static final List<PatchDtoScenario> SCENARIOS =
      List.of(
          omittedAndSuppliedAttributes(),
          explicitNullAttribute(),
          explicitNullOptionalInner(),
          attributeRename(),
          relationshipNullLinkage(),
          relationshipSingleLinkage(),
          relationshipEmptyCollection(),
          relationshipNonEmptyCollection(),
          identityOnly(),
          wrongPrimaryShape(),
          resourceTypeMismatch(),
          unknownAttribute(),
          unknownRelationship(),
          identifierConversionFailure(),
          relationshipCardinalityMismatch(),
          declarationNonPatchPresence(),
          declarationRawPatchPresence(),
          declarationDirectPresent(),
          declarationUnannotatedMember(),
          declarationPresenceId(),
          nestedPartialStructuredObject(),
          nestedEmptyStructuredObject(),
          nestedExplicitNull(),
          nestedOmitted(),
          nestedMultiLevel(),
          nestedOptionalObject(),
          nestedOptionalNull(),
          nestedOptionalMemberNull(),
          nestedContainerAtomic(),
          nestedGenericJavaType(),
          nestedContainerAtomicSet(),
          nestedContainerAtomicMap(),
          nestedNonObjectWire(),
          nestedUnknownMember(),
          nestedDeclarationMixed(),
          nestedDeclarationRaw(),
          nestedDeclarationDirectPresent(),
          nestedInvalidShapeOmitted(),
          javabeanNestedPartial(),
          metaRecursiveResourceAndRelationship(),
          metaAtomicMap(),
          metaOptionalObject(),
          metaEmptyObject(),
          metaOmitted());

  private static final FixtureCatalog<PatchDtoScenario> CATALOG =
      FixtureCatalog.of("patch-dto", SCENARIOS);

  private PatchDtoScenarios() {}

  public static FixtureCatalog<PatchDtoScenario> catalog() {
    return CATALOG;
  }

  private static String doc(String stem) {
    return TestSupportResources.readCorpusUtf8("patch/" + stem + ".json");
  }

  private static PatchDtoScenario omittedAndSuppliedAttributes() {
    return scenario(
        "patch-dto-omitted-and-supplied-attributes",
        doc("omitted-and-supplied-attributes"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.present("Hello"),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario explicitNullAttribute() {
    return scenario(
        "patch-dto-explicit-null-attribute",
        doc("explicit-null-attribute"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.present(null),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario explicitNullOptionalInner() {
    return scenario(
        "patch-dto-explicit-null-optional-inner",
        doc("subtitle-explicit-null"),
        OptionalPatch.class,
        PatchDtoExpectation.success("1", optionalArticle(PatchPresence.present(Optional.empty()))));
  }

  private static PatchDtoScenario attributeRename() {
    return scenario(
        "patch-dto-attribute-rename",
        doc("attribute-rename"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.omitted(),
                PatchPresence.present("Content"),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario relationshipNullLinkage() {
    return scenario(
        "patch-dto-relationship-null-linkage",
        doc("relationship-null-linkage"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(null),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario relationshipSingleLinkage() {
    return scenario(
        "patch-dto-relationship-single-linkage",
        doc("relationship-single-linkage"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario relationshipEmptyCollection() {
    return scenario(
        "patch-dto-relationship-empty-collection",
        doc("relationship-empty-collection"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(List.of()))));
  }

  private static PatchDtoScenario relationshipNonEmptyCollection() {
    return scenario(
        "patch-dto-relationship-non-empty-collection",
        doc("relationship-non-empty-collection"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "1",
            article(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(
                    List.of(
                        ResourceIdentifier.of(COMMENTS, "c1"),
                        ResourceIdentifier.of(COMMENTS, "c2"))))));
  }

  private static PatchDtoScenario identityOnly() {
    return scenario(
        "patch-dto-identity-only",
        doc("identity-other-id"),
        ArticlePatch.class,
        PatchDtoExpectation.success(
            "7",
            article(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario wrongPrimaryShape() {
    return scenario(
        "patch-dto-wrong-primary-shape",
        doc("wrong-primary-shape"),
        ArticlePatch.class,
        PatchDtoExpectation.readerFailure(
            ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"));
  }

  private static PatchDtoScenario resourceTypeMismatch() {
    return scenario(
        "patch-dto-resource-type-mismatch",
        doc("resource-type-mismatch"),
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"));
  }

  private static PatchDtoScenario unknownAttribute() {
    return scenario(
        "patch-dto-unknown-attribute",
        doc("attribute-unknown-member"),
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/attributes/bogus"));
  }

  private static PatchDtoScenario unknownRelationship() {
    return scenario(
        "patch-dto-unknown-relationship",
        doc("relationship-unknown-member"),
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/relationships/bogus"));
  }

  private static PatchDtoScenario identifierConversionFailure() {
    return scenario(
        "patch-dto-identifier-conversion-failure",
        doc("things-identifier-not-an-integer"),
        IntIdPatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id"));
  }

  private static PatchDtoScenario relationshipCardinalityMismatch() {
    return scenario(
        "patch-dto-relationship-cardinality-mismatch",
        doc("relationship-cardinality-mismatch"),
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, "/relationships/author/data"));
  }

  private static PatchDtoScenario declarationNonPatchPresence() {
    return scenario(
        "patch-dto-declaration-non-patch-presence",
        doc("title-only"),
        NonPatchPresencePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationRawPatchPresence() {
    return scenario(
        "patch-dto-declaration-raw-patch-presence",
        doc("title-only"),
        RawPatchPresencePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationDirectPresent() {
    return scenario(
        "patch-dto-declaration-direct-present",
        doc("title-only"),
        DirectPresentPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationUnannotatedMember() {
    return scenario(
        "patch-dto-declaration-unannotated-member",
        doc("note-attribute"),
        UnannotatedPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/attributes/note"));
  }

  private static PatchDtoScenario declarationPresenceId() {
    return scenario(
        "patch-dto-declaration-presence-id",
        doc("identity-only"),
        PresenceIdPatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/id"));
  }

  private static PatchDtoScenario nestedPartialStructuredObject() {
    return scenario(
        "patch-dto-nested-partial-structured-object",
        doc("address-street-new-street"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressPatch(
                        PatchPresence.present("New Street"), PatchPresence.omitted())))));
  }

  private static PatchDtoScenario nestedEmptyStructuredObject() {
    return scenario(
        "patch-dto-nested-empty-structured-object",
        doc("address-empty-object"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressPatch(PatchPresence.omitted(), PatchPresence.omitted())))));
  }

  private static PatchDtoScenario nestedExplicitNull() {
    return scenario(
        "patch-dto-nested-explicit-null",
        doc("address-explicit-null"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.present(null))));
  }

  private static PatchDtoScenario nestedOmitted() {
    return scenario(
        "patch-dto-nested-omitted",
        doc("identity-only"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.omitted())));
  }

  private static PatchDtoScenario nestedMultiLevel() {
    return scenario(
        "patch-dto-nested-multi-level",
        doc("address-street-and-geo-lat"),
        ArticleWithGeoPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressWithGeoPatch(
                        PatchPresence.present("S"),
                        PatchPresence.present(
                            new GeoPatch(PatchPresence.present("1"), PatchPresence.omitted())))))));
  }

  private static PatchDtoScenario nestedOptionalObject() {
    return scenario(
        "patch-dto-nested-optional-object",
        doc("address-street"),
        ArticleWithOptionalAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    Optional.of(
                        new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted()))))));
  }

  private static PatchDtoScenario nestedOptionalNull() {
    return scenario(
        "patch-dto-nested-optional-null",
        doc("address-explicit-null"),
        ArticleWithOptionalAddressPatch.class,
        PatchDtoExpectation.success(
            "1", addressPatchMembers(PatchPresence.present(Optional.empty()))));
  }

  private static PatchDtoScenario nestedOptionalMemberNull() {
    return scenario(
        "patch-dto-nested-optional-member-null",
        doc("address-street-city-null"),
        ArticleWithOptionalCityPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressWithOptionalCityPatch(
                        PatchPresence.present("S"), PatchPresence.present(Optional.empty()))))));
  }

  private static PatchDtoScenario nestedContainerAtomic() {
    return scenario(
        "patch-dto-nested-container-atomic",
        doc("address-street-tags"),
        ArticleWithAddressTagsPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressWithTagsPatch(
                        PatchPresence.present("S"), PatchPresence.present(List.of("a", "b")))))));
  }

  private static PatchDtoScenario nestedGenericJavaType() {
    return scenario(
        "patch-dto-nested-generic-javatype",
        doc("box-numbers"),
        ArticleWithBoxPatch.class,
        PatchDtoExpectation.success(
            "1",
            boxPatchMembers(
                PatchPresence.present(new BoxPatch<>(PatchPresence.present(List.of(1, 2)))))));
  }

  private static PatchDtoScenario nestedContainerAtomicSet() {
    return scenario(
        "patch-dto-nested-container-atomic-set",
        doc("address-street-aliases"),
        ArticleWithContainerAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressWithContainersPatch(
                        PatchPresence.present("S"),
                        PatchPresence.present(Set.of("a", "b")),
                        PatchPresence.omitted(),
                        PatchPresence.omitted())))));
  }

  private static PatchDtoScenario nestedContainerAtomicMap() {
    return scenario(
        "patch-dto-nested-container-atomic-map",
        doc("address-street-scores"),
        ArticleWithContainerAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new AddressWithContainersPatch(
                        PatchPresence.present("S"),
                        PatchPresence.omitted(),
                        PatchPresence.omitted(),
                        PatchPresence.present(Map.of("x", 1, "y", 2)))))));
  }

  private static PatchDtoScenario nestedNonObjectWire() {
    return scenario(
        "patch-dto-nested-non-object-wire",
        doc("address-scalar-wire"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedUnknownMember() {
    return scenario(
        "patch-dto-nested-unknown-member",
        doc("address-unknown-member"),
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/attributes/address/bogus"));
  }

  private static PatchDtoScenario nestedDeclarationMixed() {
    return scenario(
        "patch-dto-nested-declaration-mixed",
        doc("address-street-city"),
        ArticleWithMixedAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedDeclarationRaw() {
    return scenario(
        "patch-dto-nested-declaration-raw",
        doc("address-street-city"),
        ArticleWithRawAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedDeclarationDirectPresent() {
    return scenario(
        "patch-dto-nested-declaration-direct-present",
        doc("address-street-city"),
        ArticleWithDirectPresentAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedInvalidShapeOmitted() {
    return scenario(
        "patch-dto-nested-invalid-shape-omitted",
        doc("identity-only"),
        ArticleWithMixedAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.omitted())));
  }

  private static PatchDtoScenario javabeanNestedPartial() {
    return scenario(
        "patch-dto-javabean-nested-partial",
        doc("address-street"),
        MutableArticleWithAddressPatch.class,
        PatchDtoExpectation.success(
            "1",
            addressPatchMembers(
                PatchPresence.present(
                    new MutableAddressPatch(
                        PatchPresence.present("S"), PatchPresence.omitted())))));
  }

  private static Map<String, PatchPresence<?>> article(
      PatchPresence<?> title,
      PatchPresence<?> body,
      PatchPresence<?> author,
      PatchPresence<?> comments) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(4);
    members.put(TITLE, title);
    members.put(BODY, body);
    members.put(AUTHOR, author);
    members.put(COMMENTS, comments);
    return members;
  }

  private static Map<String, PatchPresence<?>> optionalArticle(PatchPresence<?> subtitle) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(1);
    members.put(SUBTITLE, subtitle);
    return members;
  }

  private static Map<String, PatchPresence<?>> addressPatchMembers(PatchPresence<?> address) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(1);
    members.put(ADDRESS, address);
    return members;
  }

  private static Map<String, PatchPresence<?>> boxPatchMembers(PatchPresence<?> box) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(1);
    members.put("box", box);
    return members;
  }

  private static PatchDtoScenario metaRecursiveResourceAndRelationship() {
    return scenario(
        "patch-dto-meta-recursive-resource-and-relationship",
        doc("meta-source-note-author-meta"),
        ArticleWithMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            metaPatchMembers(
                PatchPresence.present("T"),
                PatchPresence.present(
                    new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.present("n"))),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.present(new AuthorMeta("Alice")))));
  }

  private static PatchDtoScenario metaAtomicMap() {
    return scenario(
        "patch-dto-meta-atomic-map",
        doc("meta-map-with-relationship"),
        ArticleWithMapMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            metaPatchMembers(
                PatchPresence.present("T"),
                PatchPresence.present(Map.of("source", "cms")),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.present(Map.of("displayName", "Alice")))));
  }

  private static PatchDtoScenario metaOptionalObject() {
    return scenario(
        "patch-dto-meta-optional-object",
        doc("title-with-meta-source-note"),
        ArticleWithOptionalMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            optionalMetaPatchMembers(
                PatchPresence.present("T"),
                PatchPresence.present(Optional.of(new ArticleMeta("cms", "n"))))));
  }

  private static PatchDtoScenario metaEmptyObject() {
    return scenario(
        "patch-dto-meta-empty-object",
        doc("meta-empty-object"),
        ArticleWithMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            metaPatchMembers(
                PatchPresence.present("T"),
                PatchPresence.present(
                    new ArticleMetaPatch(PatchPresence.omitted(), PatchPresence.omitted())),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchDtoScenario metaOmitted() {
    return scenario(
        "patch-dto-meta-omitted",
        doc("identity-only"),
        ArticleWithMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            metaPatchMembers(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static Map<String, PatchPresence<?>> metaPatchMembers(
      PatchPresence<?> title,
      PatchPresence<?> meta,
      PatchPresence<?> author,
      PatchPresence<?> authorMeta) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(4);
    members.put(TITLE, title);
    members.put("meta", meta);
    members.put(AUTHOR, author);
    members.put("authorMeta", authorMeta);
    return members;
  }

  private static Map<String, PatchPresence<?>> optionalMetaPatchMembers(
      PatchPresence<?> title, PatchPresence<?> meta) {
    Map<String, PatchPresence<?>> members = LinkedHashMap.newLinkedHashMap(2);
    members.put(TITLE, title);
    members.put("meta", meta);
    return members;
  }

  private static PatchDtoScenario scenario(
      String id, String documentJson, Class<?> targetType, PatchDtoExpectation expectation) {
    return new PatchDtoScenario(id, documentJson, targetType, null, expectation);
  }
}
