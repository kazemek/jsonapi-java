package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The shared direct typed PATCH DTO catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the direct PATCH DTO surface grows, and
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
  private static final String TITLE_PATH = "/title";
  private static final String ADDRESS_ATTRIBUTE_PATH = "/attributes/address";
  private static final String IDENTITY_ONLY_DOCUMENT =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\"}}";
  private static final String MIXED_ADDRESS_DOCUMENT =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"city\":\"C\"}}}}";
  private static final String DECLARATION_DOCUMENT =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"}}}";

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

  /** The shared catalog in catalog order; the list is immutable. */
  public static List<PatchDtoScenario> all() {
    return CATALOG.all();
  }

  /** Looks up a scenario by its stable id. */
  public static PatchDtoScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<PatchDtoScenario> where(Predicate<? super PatchDtoScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static PatchDtoScenario omittedAndSuppliedAttributes() {
    return scenario(
        "patch-dto-omitted-and-supplied-attributes",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"Hello\"}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":null}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"subtitle\":null}}}",
        OptionalPatch.class,
        PatchDtoExpectation.success("1", optionalArticle(PatchPresence.present(Optional.empty()))));
  }

  private static PatchDtoScenario attributeRename() {
    return scenario(
        "patch-dto-attribute-rename",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"body-text\":\"Content\"}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":null}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[]}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"c1\"},{\"type\":\"comments\",\"id\":\"c2\"}]}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"7\"}}",
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
        "{\"data\":[{\"type\":\"articles\",\"id\":\"1\"}]}",
        ArticlePatch.class,
        PatchDtoExpectation.readerFailure(
            ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"));
  }

  private static PatchDtoScenario resourceTypeMismatch() {
    return scenario(
        "patch-dto-resource-type-mismatch",
        "{\"data\":{\"type\":\"people\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"}}}",
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"));
  }

  private static PatchDtoScenario unknownAttribute() {
    return scenario(
        "patch-dto-unknown-attribute",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"bogus\":\"x\"}}}",
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/attributes/bogus"));
  }

  private static PatchDtoScenario unknownRelationship() {
    return scenario(
        "patch-dto-unknown-relationship",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"bogus\":{\"data\":null}}}}",
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/relationships/bogus"));
  }

  private static PatchDtoScenario identifierConversionFailure() {
    return scenario(
        "patch-dto-identifier-conversion-failure",
        "{\"data\":{\"type\":\"things\",\"id\":\"not-an-int\",\"attributes\":{\"name\":\"T\"}}}",
        IntIdPatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id"));
  }

  private static PatchDtoScenario relationshipCardinalityMismatch() {
    return scenario(
        "patch-dto-relationship-cardinality-mismatch",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":[]}}}}",
        ArticlePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, "/relationships/author/data"));
  }

  private static PatchDtoScenario declarationNonPatchPresence() {
    return scenario(
        "patch-dto-declaration-non-patch-presence",
        DECLARATION_DOCUMENT,
        NonPatchPresencePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationRawPatchPresence() {
    return scenario(
        "patch-dto-declaration-raw-patch-presence",
        DECLARATION_DOCUMENT,
        RawPatchPresencePatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationDirectPresent() {
    return scenario(
        "patch-dto-declaration-direct-present",
        DECLARATION_DOCUMENT,
        DirectPresentPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, TITLE_PATH));
  }

  private static PatchDtoScenario declarationUnannotatedMember() {
    return scenario(
        "patch-dto-declaration-unannotated-member",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"note\":\"n\"}}}",
        UnannotatedPatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/note"));
  }

  private static PatchDtoScenario declarationPresenceId() {
    return scenario(
        "patch-dto-declaration-presence-id",
        IDENTITY_ONLY_DOCUMENT,
        PresenceIdPatch.class,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/id"));
  }

  private static PatchDtoScenario nestedPartialStructuredObject() {
    return scenario(
        "patch-dto-nested-partial-structured-object",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"New Street\"}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":null}}}",
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.present(null))));
  }

  private static PatchDtoScenario nestedOmitted() {
    return scenario(
        "patch-dto-nested-omitted",
        IDENTITY_ONLY_DOCUMENT,
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.omitted())));
  }

  private static PatchDtoScenario nestedMultiLevel() {
    return scenario(
        "patch-dto-nested-multi-level",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"geo\":{\"lat\":\"1\"}}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\"}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":null}}}",
        ArticleWithOptionalAddressPatch.class,
        PatchDtoExpectation.success(
            "1", addressPatchMembers(PatchPresence.present(Optional.empty()))));
  }

  private static PatchDtoScenario nestedOptionalMemberNull() {
    return scenario(
        "patch-dto-nested-optional-member-null",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"city\":null}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"tags\":[\"a\",\"b\"]}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"box\":{\"numbers\":[\"1\",\"2\"]}}}}",
        ArticleWithBoxPatch.class,
        PatchDtoExpectation.success(
            "1",
            boxPatchMembers(
                PatchPresence.present(new BoxPatch<>(PatchPresence.present(List.of(1, 2)))))));
  }

  private static PatchDtoScenario nestedContainerAtomicSet() {
    return scenario(
        "patch-dto-nested-container-atomic-set",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"aliases\":[\"a\",\"b\"]}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\",\"scores\":{\"x\":\"1\",\"y\":\"2\"}}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":\"not-an-object\"}}}",
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedUnknownMember() {
    return scenario(
        "patch-dto-nested-unknown-member",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"bogus\":\"x\"}}}}",
        ArticleWithAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.UNKNOWN_PATCH_MEMBER, "/attributes/address/bogus"));
  }

  private static PatchDtoScenario nestedDeclarationMixed() {
    return scenario(
        "patch-dto-nested-declaration-mixed",
        MIXED_ADDRESS_DOCUMENT,
        ArticleWithMixedAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedDeclarationRaw() {
    return scenario(
        "patch-dto-nested-declaration-raw",
        MIXED_ADDRESS_DOCUMENT,
        ArticleWithRawAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedDeclarationDirectPresent() {
    return scenario(
        "patch-dto-nested-declaration-direct-present",
        MIXED_ADDRESS_DOCUMENT,
        ArticleWithDirectPresentAddressPatch.class,
        PatchDtoExpectation.binderFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, ADDRESS_ATTRIBUTE_PATH));
  }

  private static PatchDtoScenario nestedInvalidShapeOmitted() {
    return scenario(
        "patch-dto-nested-invalid-shape-omitted",
        IDENTITY_ONLY_DOCUMENT,
        ArticleWithMixedAddressPatch.class,
        PatchDtoExpectation.success("1", addressPatchMembers(PatchPresence.omitted())));
  }

  private static PatchDtoScenario javabeanNestedPartial() {
    return scenario(
        "patch-dto-javabean-nested-partial",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"address\":{\"street\":\"S\"}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\","
            + "\"attributes\":{\"title\":\"T\"},"
            + "\"meta\":{\"source\":\"cms\",\"note\":\"n\"},"
            + "\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"},\"meta\":{\"displayName\":\"Alice\"}}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\","
            + "\"attributes\":{\"title\":\"T\"},"
            + "\"meta\":{\"source\":\"cms\"},"
            + "\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"},\"meta\":{\"displayName\":\"Alice\"}}}}}",
        ArticleWithMapMetaPatch.class,
        PatchDtoExpectation.success(
            "1",
            mapMetaPatchMembers(
                PatchPresence.present("T"),
                PatchPresence.present(Map.of("source", "cms")),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.present(Map.of("displayName", "Alice")))));
  }

  private static PatchDtoScenario metaOptionalObject() {
    return scenario(
        "patch-dto-meta-optional-object",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"meta\":{\"source\":\"cms\",\"note\":\"n\"}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"meta\":{}}}",
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
        IDENTITY_ONLY_DOCUMENT,
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

  private static Map<String, PatchPresence<?>> mapMetaPatchMembers(
      PatchPresence<?> title,
      PatchPresence<?> meta,
      PatchPresence<?> author,
      PatchPresence<?> authorMeta) {
    return metaPatchMembers(title, meta, author, authorMeta);
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
