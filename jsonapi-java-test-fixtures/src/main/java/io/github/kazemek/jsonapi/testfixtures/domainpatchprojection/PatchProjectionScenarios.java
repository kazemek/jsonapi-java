package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import io.github.kazemek.jsonapi.testfixtures.domainpatch.PatchDocuments;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle;
import java.util.List;
import java.util.function.Predicate;

/**
 * Shared typed PATCH projection catalog consumed by Jackson-major contract tests.
 *
 * <p>Each scenario binds JSON into a {@link io.github.kazemek.jsonapi.jackson.PatchCommand} using
 * {@code commandTargetType}, then projects into {@code patchTargetType}.
 */
public final class PatchProjectionScenarios {

  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";

  private static final List<PatchProjectionScenario> SCENARIOS =
      List.of(
          omittedAndSuppliedAttributes(),
          explicitNullAttribute(),
          attributeRename(),
          jsonapiNameMatchDifferentJavaNames(),
          logicalNameDoesNotOverrideJsonapiName(),
          relationshipNullLinkage(),
          relationshipSingleLinkage(),
          relationshipEmptyCollection(),
          relationshipNonEmptyCollection(),
          compoundIncludedIgnored(),
          subsetAcceptsOverlappingChanges(),
          unrepresentableSuppliedChange(),
          identifierNotSupported(),
          implicitIdentifierNotSupported(),
          resourceTypeMismatch(),
          invalidPatchPropertyType(),
          incompatibleAttributeValueType(),
          incompatibleRelationshipCollectionValueType());

  private static final FixtureCatalog<PatchProjectionScenario> CATALOG =
      FixtureCatalog.of("patch-projection", SCENARIOS);

  private PatchProjectionScenarios() {}

  public static FixtureCatalog<PatchProjectionScenario> catalog() {
    return CATALOG;
  }

  public static List<PatchProjectionScenario> all() {
    return CATALOG.all();
  }

  public static PatchProjectionScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<PatchProjectionScenario> where(
      Predicate<? super PatchProjectionScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static PatchProjectionScenario omittedAndSuppliedAttributes() {
    return scenario(
        "patch-projection-omitted-and-supplied-attributes",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.present("Hello"),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario explicitNullAttribute() {
    return scenario(
        "patch-projection-explicit-null-attribute",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":null}}}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.present(null),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario attributeRename() {
    return scenario(
        "patch-projection-attribute-rename",
        PatchDocuments.ARTICLE_BODY_TEXT_CONTENT,
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.omitted(),
                PatchPresence.present("Content"),
                PatchPresence.omitted(),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario jsonapiNameMatchDifferentJavaNames() {
    return scenario(
        "patch-projection-jsonapi-name-match-different-java-names",
        PatchDocuments.ARTICLE_BODY_TEXT_CONTENT,
        FlatArticle.class,
        FlatArticleBodyTextPatch.class,
        PatchProjectionExpectation.success(
            new FlatArticleBodyTextPatch(PatchPresence.present("Content"))));
  }

  private static PatchProjectionScenario logicalNameDoesNotOverrideJsonapiName() {
    return scenario(
        "patch-projection-logical-name-does-not-override-jsonapi-name",
        PatchDocuments.ARTICLE_BODY_TEXT_CONTENT,
        FlatArticle.class,
        FlatArticleBodyNameMismatchPatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE, "/body-text"));
  }

  private static PatchProjectionScenario relationshipNullLinkage() {
    return scenario(
        "patch-projection-relationship-null-linkage",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":null}}}}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(null),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario relationshipSingleLinkage() {
    return scenario(
        "patch-projection-relationship-single-linkage",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}}}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario relationshipEmptyCollection() {
    return scenario(
        "patch-projection-relationship-empty-collection",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[]}}}}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(List.of()))));
  }

  private static PatchProjectionScenario relationshipNonEmptyCollection() {
    return scenario(
        "patch-projection-relationship-non-empty-collection",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"c1\"},{\"type\":\"comments\",\"id\":\"c2\"}]}}}}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.omitted(),
                PatchPresence.present(
                    List.of(
                        ResourceIdentifier.of(COMMENTS, "c1"),
                        ResourceIdentifier.of(COMMENTS, "c2"))))));
  }

  private static PatchProjectionScenario compoundIncludedIgnored() {
    return scenario(
        "patch-projection-compound-included-ignored",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}},\"included\":[{\"type\":\"people\",\"id\":\"p1\",\"attributes\":{\"name\":\"Alice\"}}]}",
        FlatArticle.class,
        FlatArticlePatch.class,
        PatchProjectionExpectation.success(
            new FlatArticlePatch(
                PatchPresence.present("T"),
                PatchPresence.omitted(),
                PatchPresence.present(ResourceIdentifier.of(PEOPLE, "p1")),
                PatchPresence.omitted())));
  }

  private static PatchProjectionScenario subsetAcceptsOverlappingChanges() {
    return scenario(
        "patch-projection-subset-accepts-overlapping-changes",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticleTitleOnlyPatch.class,
        PatchProjectionExpectation.success(
            new FlatArticleTitleOnlyPatch(PatchPresence.present("Hello"))));
  }

  private static PatchProjectionScenario unrepresentableSuppliedChange() {
    return scenario(
        "patch-projection-unrepresentable-supplied-change",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}}}",
        FlatArticle.class,
        FlatArticleTitleOnlyPatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE, "/author"));
  }

  private static PatchProjectionScenario identifierNotSupported() {
    return scenario(
        "patch-projection-identifier-not-supported",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticlePatchWithId.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.PATCH_IDENTIFIER_NOT_SUPPORTED, "/id"));
  }

  private static PatchProjectionScenario implicitIdentifierNotSupported() {
    return scenario(
        "patch-projection-implicit-identifier-not-supported",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticlePatchImplicitId.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.PATCH_IDENTIFIER_NOT_SUPPORTED, "/id"));
  }

  private static PatchProjectionScenario resourceTypeMismatch() {
    return scenario(
        "patch-projection-resource-type-mismatch",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatWrongTypeArticlePatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"));
  }

  private static PatchProjectionScenario invalidPatchPropertyType() {
    return scenario(
        "patch-projection-invalid-patch-property-type",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticlePlainTitlePatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/title"));
  }

  private static PatchProjectionScenario incompatibleAttributeValueType() {
    return scenario(
        "patch-projection-incompatible-attribute-value-type",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        FlatArticleIntegerTitlePatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/title"));
  }

  private static PatchProjectionScenario incompatibleRelationshipCollectionValueType() {
    return scenario(
        "patch-projection-incompatible-relationship-collection-value-type",
        PatchDocuments.ARTICLE_COMMENTS_ONE,
        FlatArticle.class,
        FlatArticleStringCommentsPatch.class,
        PatchProjectionExpectation.projectorFailure(
            MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE, "/comments"));
  }

  private static PatchProjectionScenario scenario(
      String id,
      String documentJson,
      Class<?> commandTargetType,
      Class<?> patchTargetType,
      PatchProjectionExpectation expectation) {
    return new PatchProjectionScenario(
        id, documentJson, commandTargetType, patchTargetType, expectation);
  }
}
