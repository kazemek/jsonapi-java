package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.BlogWithJsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * The shared flat resource-to-DTO binding catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the binder surface grows, and adapter
 * suites pick them up through {@link #all()}. Consumers dispatch on the {@link DomainReadInput}
 * variant and {@link ConverterBehavior}, never on a scenario id.
 */
public final class DomainReadScenarios {

  private static final String ARTICLES = "articles";
  private static final String AUTHOR = "author";
  private static final String COMMENTS = "comments";
  private static final String PEOPLE = "people";
  private static final String TITLE = "title";
  private static final String THINGS = "things";

  private static final String INCLUDED_PRIMARY =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}},\"included\":[{\"type\":\"people\",\"id\":\"p1\",\"attributes\":{\"name\":\"Alice\"}}]}";

  private static final String INCLUDED_SWAPPED =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"T\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"p1\"}}}},\"included\":[{\"type\":\"people\",\"id\":\"p1\",\"attributes\":{\"name\":\"AliceChanged\"}}]}";

  private static final List<DomainReadScenario> SCENARIOS =
      List.of(
          new DomainReadScenario(
              "binds record with id, attributes, and built-in ResourceIdentifier relationships",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, "Hello", "body-text", "Content"),
                      rels(
                          AUTHOR,
                          single(PEOPLE, "p1"),
                          COMMENTS,
                          collection(COMMENTS, List.of("c1", "c2"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticle(
                      "1",
                      "Hello",
                      "Content",
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      List.of(
                          ResourceIdentifier.of(COMMENTS, "c1"),
                          ResourceIdentifier.of(COMMENTS, "c2"))))),
          new DomainReadScenario(
              "binds mutable POJO",
              DomainReadInput.single(
                  resource(
                      ARTICLES, "1", attrs(TITLE, "Hello"), rels(AUTHOR, single(PEOPLE, "p1")))),
              FlatMutableArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatMutableArticle("1", "Hello", ResourceIdentifier.of(PEOPLE, "p1")))),
          new DomainReadScenario(
              "binds immutable creator-based POJO",
              DomainReadInput.single(resource(ARTICLES, "42", attrs(TITLE, "Creator"), null)),
              FlatCreatorArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatCreatorArticle("42", "Creator"))),
          new DomainReadScenario(
              "binds inherited properties",
              DomainReadInput.single(
                  resource(
                      "blogs",
                      "b1",
                      attrs("name", "My Blog", "description", "A description"),
                      null)),
              FlatInheritedBlog.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatInheritedBlog("b1", "My Blog", "A description"))),
          new DomainReadScenario(
              "binds @JsonProperty named attribute",
              DomainReadInput.single(resource("blogs", "b1", attrs("blog_title", "My Blog"), null)),
              BlogWithJsonProperty.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new BlogWithJsonProperty("b1", "My Blog"))),
          new DomainReadScenario(
              "@JsonIgnore property is not bound",
              DomainReadInput.single(
                  resource(THINGS, "1", attrs("name", "visible", "secret", "hidden"), null)),
              FlatThingWithIgnored.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatThingWithIgnored("1", "visible", null))),
          new DomainReadScenario(
              "default identifier conversion binds non-String id via convertValue",
              DomainReadInput.single(resource(ARTICLES, "42", attrs(TITLE, "T"), null)),
              FlatIntIdArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatIntIdArticle(42, "T"))),
          new DomainReadScenario(
              "custom IdentifierConverter parse inverts the wire form",
              DomainReadInput.single(resource(ARTICLES, "prefix-42", attrs(TITLE, "T"), null)),
              FlatIntIdArticle.class,
              ConverterBehavior.CUSTOM_PARSE_INVERSION,
              DomainReadExpectation.bound(new FlatIntIdArticle(42, "T"))),
          new DomainReadScenario(
              "lid-only resource binds into identifier property",
              DomainReadInput.single(resourceWithLid(ARTICLES, null, "l1", attrs(TITLE, "T"))),
              FlatLidArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatLidArticle("l1", "T"))),
          new DomainReadScenario(
              "resource without id or lid omits the identifier property",
              DomainReadInput.single(resourceWithLid(ARTICLES, null, null, attrs(TITLE, "T"))),
              FlatLidArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatLidArticle(null, "T"))),
          new DomainReadScenario(
              "explicit-null attribute binds null and omitted attribute keeps its default",
              DomainReadInput.single(resource(ARTICLES, "1", nullableAttr(TITLE, null), null)),
              FlatDefaultedArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatDefaultedArticle("1", null, "default"))),
          new DomainReadScenario(
              "unmapped resource attributes are ignored",
              DomainReadInput.single(
                  resource(ARTICLES, "1", attrs(TITLE, "T", "unexpected", "ignored"), null)),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatArticle("1", "T", null, null, null))),
          new DomainReadScenario(
              "fromResources binds homogeneous collection in order",
              DomainReadInput.collection(
                  List.of(
                      resource(ARTICLES, "1", attrs(TITLE, "One"), null),
                      resource(ARTICLES, "2", attrs(TITLE, "Two"), null))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  List.of(
                      new FlatArticle("1", "One", null, null, null),
                      new FlatArticle("2", "Two", null, null, null)))),
          new DomainReadScenario(
              "fromResources validates every element type",
              DomainReadInput.collection(
                  List.of(resource(ARTICLES, "1", null, null), resource(PEOPLE, "p1", null, null))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type")),
          new DomainReadScenario(
              "omitted to-one relationship key is not bound",
              DomainReadInput.single(
                  resource(
                      ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, List.of("c1"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticle(
                      "1", null, null, null, List.of(ResourceIdentifier.of(COMMENTS, "c1"))))),
          new DomainReadScenario(
              "links-or-meta-only to-one relationship is not bound",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(AUTHOR, Relationship.metaOnly(Meta.of(Map.of("k", "v")))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatArticle("1", null, null, null, null))),
          new DomainReadScenario(
              "NullLinkage on to-one binds null",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(AUTHOR, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatArticle("1", null, null, null, null))),
          new DomainReadScenario(
              "collection linkage on to-one is a cardinality mismatch",
              DomainReadInput.single(
                  resource(
                      ARTICLES, "1", null, rels(AUTHOR, collection(PEOPLE, List.of("p1", "p2"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                  "/relationships/author/data")),
          new DomainReadScenario(
              "empty collection linkage on to-many binds empty collection",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS,
                          Relationship.withData(
                              RelationshipData.IdentifierCollectionLinkage.empty())))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatArticle("1", null, null, null, List.of()))),
          new DomainReadScenario(
              "empty collection linkage on to-many binds empty Set",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          "tags",
                          Relationship.withData(
                              RelationshipData.IdentifierCollectionLinkage.empty())))),
              FlatArticleWithSet.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatArticleWithSet("1", null, Set.of()))),
          new DomainReadScenario(
              "empty collection linkage on to-many binds empty array",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS,
                          Relationship.withData(
                              RelationshipData.IdentifierCollectionLinkage.empty())))),
              FlatArticleWithArray.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticleWithArray("1", null, new ResourceIdentifier[0]))),
          new DomainReadScenario(
              "non-empty collection linkage on to-many binds List",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(COMMENTS, collection(COMMENTS, List.of("c1", "c2"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticle(
                      "1",
                      null,
                      null,
                      null,
                      List.of(
                          ResourceIdentifier.of(COMMENTS, "c1"),
                          ResourceIdentifier.of(COMMENTS, "c2"))))),
          new DomainReadScenario(
              "non-empty collection linkage on to-many binds Set",
              DomainReadInput.single(
                  resource(
                      ARTICLES, "1", null, rels("tags", collection("tags", List.of("t1", "t2"))))),
              FlatArticleWithSet.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticleWithSet(
                      "1",
                      null,
                      Set.of(
                          ResourceIdentifier.of("tags", "t1"),
                          ResourceIdentifier.of("tags", "t2"))))),
          new DomainReadScenario(
              "non-empty collection linkage on to-many binds array",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(COMMENTS, collection(COMMENTS, List.of("c1", "c2"))))),
              FlatArticleWithArray.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticleWithArray(
                      "1",
                      null,
                      new ResourceIdentifier[] {
                        ResourceIdentifier.of(COMMENTS, "c1"), ResourceIdentifier.of(COMMENTS, "c2")
                      }))),
          new DomainReadScenario(
              "NullLinkage on to-many is a cardinality mismatch",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                  "/relationships/comments/data")),
          new DomainReadScenario(
              "single linkage on to-many is a cardinality mismatch",
              DomainReadInput.single(
                  resource(ARTICLES, "1", null, rels(COMMENTS, single(COMMENTS, "c1")))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                  "/relationships/comments/data")),
          new DomainReadScenario(
              "empty collection linkage on to-one is a cardinality mismatch",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          AUTHOR,
                          Relationship.withData(
                              RelationshipData.IdentifierCollectionLinkage.empty())))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                  "/relationships/author/data")),
          new DomainReadScenario(
              "NullLinkage on Optional to-one binds empty Optional",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(AUTHOR, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))),
              FlatArticleWithOptional.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticleWithOptional("1", null, Optional.empty()))),
          new DomainReadScenario(
              "SingleLinkage on Optional to-one binds present Optional",
              DomainReadInput.single(
                  resource(ARTICLES, "1", null, rels(AUTHOR, single(PEOPLE, "p1")))),
              FlatArticleWithOptional.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticleWithOptional(
                      "1", null, Optional.of(ResourceIdentifier.of(PEOPLE, "p1"))))),
          new DomainReadScenario(
              "resource type mismatch is RESOURCE_TYPE_MISMATCH at /type",
              DomainReadInput.single(resource(PEOPLE, "p1", null, null)),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type", FlatArticle.class)),
          new DomainReadScenario(
              "unregistered to-one relationship target is UNSUPPORTED_RELATIONSHIP_TARGET",
              DomainReadInput.single(
                  resource(ARTICLES, "1", null, rels(AUTHOR, single(PEOPLE, "p1")))),
              FlatPersonArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET, "/relationships/author/data")),
          new DomainReadScenario(
              "unregistered to-many relationship target is UNSUPPORTED_RELATIONSHIP_TARGET",
              DomainReadInput.single(
                  resource(
                      ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, List.of("c1"))))),
              FlatCommentArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
                  "/relationships/comments/data")),
          new DomainReadScenario(
              "identifier parse exception is IDENTIFIER_CONVERSION_FAILED at /id",
              DomainReadInput.single(resource(ARTICLES, "42", null, null)),
              FlatIntIdArticle.class,
              ConverterBehavior.PARSE_THROWING,
              DomainReadExpectation.failure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id")),
          new DomainReadScenario(
              "identifier parse returning null is IDENTIFIER_CONVERSION_FAILED",
              DomainReadInput.single(resource(ARTICLES, "42", null, null)),
              FlatIntIdArticle.class,
              ConverterBehavior.PARSE_RETURNING_NULL,
              DomainReadExpectation.failure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id")),
          new DomainReadScenario(
              "identifier coercion failure is IDENTIFIER_CONVERSION_FAILED",
              DomainReadInput.single(resource(ARTICLES, "not-a-number", null, null)),
              FlatIntIdArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, "/id")),
          new DomainReadScenario(
              "absent required creator property is MISSING_CREATOR_INPUT",
              DomainReadInput.single(resource(THINGS, "1", attrs(TITLE, "present"), null)),
              FlatRequiredThing.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.MISSING_CREATOR_INPUT)),
          new DomainReadScenario(
              "creator throwing during instantiation is MISSING_CREATOR_INPUT",
              DomainReadInput.single(resource(THINGS, "1", attrs(TITLE, "boom"), null)),
              FlatThrowingCreatorThing.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.MISSING_CREATOR_INPUT)),
          new DomainReadScenario(
              "attribute value that cannot coerce is UNSUPPORTED_ATTRIBUTE_VALUE",
              DomainReadInput.single(resource(THINGS, "1", nestedCountAttrs(), null)),
              FlatCountedThing.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE)),
          new DomainReadScenario(
              "explicit-null attribute into primitive property is UNSUPPORTED_ATTRIBUTE_VALUE",
              DomainReadInput.single(resource(THINGS, "1", nullableAttr("count", null), null)),
              FlatCountedThing.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE)),
          new DomainReadScenario(
              "binder never sees document included resources",
              DomainReadInput.includedIsolation(INCLUDED_PRIMARY, INCLUDED_SWAPPED),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticle("1", "T", null, ResourceIdentifier.of(PEOPLE, "p1"), null))));

  private static final FixtureCatalog<DomainReadScenario> CATALOG =
      FixtureCatalog.of("domain-read", SCENARIOS);

  private DomainReadScenarios() {}

  public static FixtureCatalog<DomainReadScenario> catalog() {
    return CATALOG;
  }

  /** The shared catalog in catalog order; the list is immutable. */
  public static List<DomainReadScenario> all() {
    return CATALOG.all();
  }

  /** Looks up a scenario by its stable id. */
  public static DomainReadScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<DomainReadScenario> where(Predicate<? super DomainReadScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static ResourceObject resource(
      String type,
      @Nullable String id,
      @Nullable Map<String, @Nullable Object> attributes,
      @Nullable Map<String, Relationship> relationships) {
    return new ResourceObject(
        type,
        id,
        null,
        attributes == null ? null : Attributes.ofAttributes(attributes),
        relationships == null
            ? null
            : Relationships.ofRelationships(copyRelationships(relationships)),
        null,
        null,
        Map.of());
  }

  private static ResourceObject resourceWithLid(
      String type,
      @Nullable String id,
      @Nullable String lid,
      Map<String, @Nullable Object> attributes) {
    return new ResourceObject(
        type, id, lid, Attributes.ofAttributes(attributes), null, null, null, Map.of());
  }

  private static Map<String, @Nullable Relationship> copyRelationships(
      Map<String, Relationship> relationships) {
    Map<String, @Nullable Relationship> copy = new LinkedHashMap<>();
    copy.putAll(relationships);
    return copy;
  }

  private static Relationship single(String type, String id) {
    return Relationship.withData(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of(type, id)));
  }

  private static Relationship collection(String type, List<String> ids) {
    List<ResourceIdentifier> identifiers = new ArrayList<>(ids.size());
    for (String id : ids) {
      identifiers.add(ResourceIdentifier.of(type, id));
    }
    return Relationship.withData(new RelationshipData.IdentifierCollectionLinkage(identifiers));
  }

  private static Map<String, @Nullable Object> attrs(Object... keyValues) {
    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      attributes.put((String) keyValues[i], keyValues[i + 1]);
    }
    return attributes;
  }

  private static Map<String, @Nullable Object> nullableAttr(String name, @Nullable Object value) {
    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    attributes.put(name, value);
    return attributes;
  }

  private static Map<String, Relationship> rels(Object... keyValues) {
    Map<String, Relationship> relationships = new LinkedHashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      relationships.put((String) keyValues[i], (Relationship) keyValues[i + 1]);
    }
    return relationships;
  }

  private static Map<String, @Nullable Object> nestedCountAttrs() {
    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("nested", 1);
    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    attributes.put("count", nested);
    return attributes;
  }
}
