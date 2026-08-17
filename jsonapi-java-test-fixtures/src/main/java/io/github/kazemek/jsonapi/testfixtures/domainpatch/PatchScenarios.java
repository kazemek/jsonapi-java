package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.validation.EndpointIdentity;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatCountedThing;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatIntIdArticle;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatPersonArticle;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatThingWithIgnored;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * The shared presence-aware PATCH catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the PATCH surface grows, and adapter
 * suites pick them up through {@link #all()}. Consumers dispatch on {@link PatchExpectation}, never
 * on a scenario id.
 */
public final class PatchScenarios {

  private static final String ARTICLES = "articles";
  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";
  private static final String TITLE = "title";
  private static final String AUTHOR = "author";

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
          unsupportedRelationshipTarget());

  private static final FixtureCatalog<PatchScenario> CATALOG =
      FixtureCatalog.of("patch", SCENARIOS);

  private PatchScenarios() {}

  public static FixtureCatalog<PatchScenario> catalog() {
    return CATALOG;
  }

  /** The shared catalog in catalog order; the list is immutable. */
  public static List<PatchScenario> all() {
    return CATALOG.all();
  }

  /** Looks up a scenario by its stable id. */
  public static PatchScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<PatchScenario> where(Predicate<? super PatchScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static PatchScenario omittedAndSuppliedAttributes() {
    return scenario(
        "patch-omitted-and-supplied-attributes",
        PatchDocuments.ARTICLE_TITLE_HELLO,
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, "Hello"))));
  }

  private static PatchScenario explicitNullAttribute() {
    return scenario(
        "patch-explicit-null-attribute",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":null}}}",
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange(TITLE, TITLE, null))));
  }

  private static PatchScenario attributeRename() {
    return scenario(
        "patch-attribute-rename",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"body-text\":\"Content\"}}}",
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange("body-text", "body", "Content"))));
  }

  private static PatchScenario ignoredUnmappedOmittedFromChanges() {
    return scenario(
        "patch-ignored-unmapped-omitted-from-changes",
        "{\"data\":{\"type\":\"things\",\"id\":\"1\",\"attributes\":{\"name\":\"visible\",\"secret\":\"hidden\",\"unexpected\":\"ignored\"}}}",
        FlatThingWithIgnored.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.AttributeChange("name", "name", "visible"))));
  }

  private static PatchScenario relationshipNullLinkage() {
    return scenario(
        "patch-relationship-null-linkage",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":null}}}}",
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.RelationshipChange(AUTHOR, AUTHOR, null))));
  }

  private static PatchScenario relationshipSingleLinkage() {
    return scenario(
        "patch-relationship-single-linkage",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[]}}}}",
        FlatArticle.class,
        null,
        PatchExpectation.success(
            "1", List.of(new PatchChange.RelationshipChange(COMMENTS, COMMENTS, List.of()))));
  }

  private static PatchScenario relationshipNonEmptyCollection() {
    return scenario(
        "patch-relationship-non-empty-collection",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"c1\"},{\"type\":\"comments\",\"id\":\"c2\"}]}}}}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":[]}}}}",
        FlatArticle.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, "/relationships/author/data"));
  }

  private static PatchScenario compoundIncludedIgnored() {
    return scenario(
        "patch-compound-included-ignored",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}},\"included\":[{\"type\":\"people\",\"id\":\"p1\",\"attributes\":{\"name\":\"Alice\"}}]}",
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
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"}}}",
        FlatArticle.class,
        new EndpointIdentity(ARTICLES, "99"),
        PatchExpectation.readerFailure(ValidationRuleCode.ENDPOINT_IDENTITY_MISMATCH, "/data/id"));
  }

  private static PatchScenario missingRelationshipData() {
    return scenario(
        "patch-missing-relationship-data",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"meta\":{\"note\":\"no-data\"}}}}}",
        FlatArticle.class,
        null,
        PatchExpectation.readerFailure(
            ValidationRuleCode.RELATIONSHIP_DATA_REQUIRED, "/data/relationships/author/data"));
  }

  private static PatchScenario wrongPrimaryShape() {
    return scenario(
        "patch-wrong-primary-shape",
        "{\"data\":[{\"type\":\"articles\",\"id\":\"1\"}]}",
        FlatArticle.class,
        null,
        PatchExpectation.readerFailure(
            ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"));
  }

  private static PatchScenario resourceTypeMismatch() {
    return scenario(
        "patch-resource-type-mismatch",
        "{\"data\":{\"type\":\"people\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"}}}",
        FlatArticle.class,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"));
  }

  private static PatchScenario identifierConversionFailure() {
    return scenario(
        "patch-identifier-conversion-failure",
        "{\"data\":{\"type\":\"articles\",\"id\":\"not-an-int\",\"attributes\":{\"title\":\"T\"}}}",
        FlatIntIdArticle.class,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id"));
  }

  private static PatchScenario attributeConversionFailure() {
    return scenario(
        "patch-attribute-conversion-failure",
        "{\"data\":{\"type\":\"things\",\"id\":\"1\",\"attributes\":{\"count\":\"not-an-int\"}}}",
        FlatCountedThing.class,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE, "/count"));
  }

  private static PatchScenario unsupportedRelationshipTarget() {
    return scenario(
        "patch-unsupported-relationship-target",
        "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}}}",
        FlatPersonArticle.class,
        null,
        PatchExpectation.binderFailure(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET, "/relationships/author/data"));
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
