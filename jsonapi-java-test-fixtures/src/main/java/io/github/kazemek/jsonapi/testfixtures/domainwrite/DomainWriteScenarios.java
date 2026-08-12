package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.DocumentEnvelope;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * The shared flat domain-to-resource write catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the mapping surface grows, and adapter
 * suites pick them up through {@link #all()}. Consumers dispatch on the {@link
 * DomainWriteOperation}/{@link DomainWriteInput} descriptor, never on a scenario id.
 */
public final class DomainWriteScenarios {

  private static final String COMMENTS = "comments";

  private static final String TAGS = "tags";

  private static final Set<Tag> TAGS_SET =
      Collections.unmodifiableSet(new LinkedHashSet<>(List.of(new Tag("java"), new Tag("groovy"))));

  private static final Links ENVELOPE_LINKS = Links.ofLinks(Collections.singletonMap("self", null));

  private static final Meta ENVELOPE_META = Meta.of(Map.of("key", "value"));

  private static final JsonApiObject ENVELOPE_JSONAPI = JsonApiObject.ofVersion("1.1");

  private static final List<DomainWriteScenario> SCENARIOS =
      List.of(
          new DomainWriteScenario(
              "maps a record with explicit @JsonApiId and @JsonApiAttribute",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new Article("1", "Hello", "Body text", List.of(), null)),
              null,
              DomainWriteOutcome.resource(
                  articleResource("1", "Hello", "Body text", List.of(), null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps attribute name override",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new Article("1", "Title", "Content", List.of(), null)),
              null,
              DomainWriteOutcome.resource(
                  articleResource("1", "Title", "Content", List.of(), null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps conventional id property",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new ConventionalId("42", "name value")),
              null,
              DomainWriteOutcome.resource(
                  new ResourceObject(
                      "conventionals",
                      "42",
                      null,
                      Attributes.ofAttributes(singleAttribute("name", "name value")),
                      null,
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps @JsonProperty naming",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new BlogWithJsonProperty("b1", "My Blog")),
              null,
              DomainWriteOutcome.resource(
                  new ResourceObject(
                      "blogs",
                      "b1",
                      null,
                      Attributes.ofAttributes(singleAttribute("blog_title", "My Blog")),
                      null,
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps nullable to-one relationship to null linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new Article("1", "T", "B", List.of(), null)),
              null,
              DomainWriteOutcome.resource(articleResource("1", "T", "B", List.of(), null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps to-one relationship to single linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new Article("1", "T", "B", List.of(), new Person("p1", "Alice"))),
              null,
              DomainWriteOutcome.resource(
                  articleResource("1", "T", "B", List.of(), new Person("p1", "Alice"))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps empty to-many relationship to empty linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new Article("1", "T", "B", List.of(), null)),
              null,
              DomainWriteOutcome.resource(articleResource("1", "T", "B", List.of(), null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps populated to-many relationship",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new Article(
                          "1",
                          "T",
                          "B",
                          List.of(
                              new Comment("c1", "Nice", null), new Comment("c2", "Great", null)),
                          null)),
              null,
              DomainWriteOutcome.resource(
                  articleResource(
                      "1",
                      "T",
                      "B",
                      List.of(new Comment("c1", "Nice", null), new Comment("c2", "Great", null)),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps Set-based to-many relationship",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new ArticleWithSet("1", "T", TAGS_SET)),
              null,
              DomainWriteOutcome.resource(articleWithSetResource()),
              new DomainWriteComparisonPolicy(
                  Map.of(
                      TAGS,
                      DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS))),
          new DomainWriteScenario(
              "maps mutable POJO",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new SamplePojo("p1", "Example", List.of())),
              null,
              DomainWriteOutcome.resource(
                  new ResourceObject(
                      "pojos",
                      "p1",
                      null,
                      Attributes.ofAttributes(singleAttribute("display-name", "Example")),
                      Relationships.ofRelationships(
                          Map.of(
                              COMMENTS,
                              relationship(RelationshipData.IdentifierCollectionLinkage.empty()))),
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "toDocument wraps resource in single-resource document",
              DomainWriteOperation.TO_DOCUMENT,
              new DomainWriteInput.SingleInput(() -> new Article("1", "T", "B", List.of(), null)),
              null,
              DomainWriteOutcome.document(
                  new JsonApiDocument(
                      new DocumentData.SingleResource(
                          articleResource("1", "T", "B", List.of(), null)),
                      null,
                      null,
                      null,
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "toResourceCollection wraps in resource-collection document",
              DomainWriteOperation.TO_RESOURCE_COLLECTION,
              new DomainWriteInput.CollectionInput(
                  () ->
                      List.of(
                          new Article("1", "One", "B1", List.of(), null),
                          new Article("2", "Two", "B2", List.of(), null))),
              null,
              DomainWriteOutcome.document(
                  new JsonApiDocument(
                      new DocumentData.ResourceCollection(
                          List.of(
                              articleResource("1", "One", "B1", List.of(), null),
                              articleResource("2", "Two", "B2", List.of(), null))),
                      null,
                      null,
                      null,
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "toDocument with envelope passes links, meta, and jsonapi",
              DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE,
              new DomainWriteInput.SingleInput(() -> new Article("1", "T", "B", List.of(), null)),
              new DocumentEnvelope(ENVELOPE_LINKS, ENVELOPE_META, ENVELOPE_JSONAPI),
              DomainWriteOutcome.document(
                  new JsonApiDocument(
                      new DocumentData.SingleResource(
                          articleResource("1", "T", "B", List.of(), null)),
                      null,
                      ENVELOPE_META,
                      ENVELOPE_JSONAPI,
                      ENVELOPE_LINKS,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "null input is rejected",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> null),
              null,
              DomainWriteOutcome.failure(NullPointerException.class),
              DomainWriteComparisonPolicy.ordered()));

  private static final FixtureCatalog<DomainWriteScenario> CATALOG =
      FixtureCatalog.of("domain-write", SCENARIOS);

  private DomainWriteScenarios() {}

  public static FixtureCatalog<DomainWriteScenario> catalog() {
    return CATALOG;
  }

  /** The shared catalog in catalog order; the list is immutable. */
  public static List<DomainWriteScenario> all() {
    return CATALOG.all();
  }

  /** Looks up a scenario by its stable id. */
  public static DomainWriteScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<DomainWriteScenario> where(Predicate<? super DomainWriteScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static ResourceObject articleResource(
      String id, String title, String body, List<Comment> comments, @Nullable Person author) {
    return new ResourceObject(
        "articles",
        id,
        null,
        Attributes.ofAttributes(articleAttributes(title, body)),
        Relationships.ofRelationships(
            articleRelationships(personLinkage(author), commentsLinkage(comments))),
        null,
        null,
        Map.of());
  }

  private static ResourceObject articleWithSetResource() {
    return new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes(singleAttribute("title", "T")),
        Relationships.ofRelationships(Map.of(TAGS, relationship(tagsLinkage()))),
        null,
        null,
        Map.of());
  }

  private static Map<String, Object> singleAttribute(String name, Object value) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(name, value);
    return attributes;
  }

  private static Map<String, Object> articleAttributes(String title, String body) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("title", title);
    attributes.put("body-text", body);
    return attributes;
  }

  private static Map<String, @Nullable Relationship> articleRelationships(
      RelationshipData authorLinkage, RelationshipData commentsLinkage) {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put("author", relationship(authorLinkage));
    relationships.put(COMMENTS, relationship(commentsLinkage));
    return relationships;
  }

  private static Relationship relationship(RelationshipData data) {
    return new Relationship(data, null, null, Map.of());
  }

  private static RelationshipData personLinkage(@Nullable Person author) {
    if (author == null) {
      return RelationshipData.NullLinkage.INSTANCE;
    }
    return new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", author.id(), null, null, Map.of()));
  }

  private static RelationshipData commentsLinkage(List<Comment> comments) {
    List<ResourceIdentifier> identifiers = new ArrayList<>(comments.size());
    for (Comment comment : comments) {
      identifiers.add(new ResourceIdentifier(COMMENTS, comment.id(), null, null, Map.of()));
    }
    return new RelationshipData.IdentifierCollectionLinkage(identifiers);
  }

  private static RelationshipData tagsLinkage() {
    List<ResourceIdentifier> identifiers = new ArrayList<>(TAGS_SET.size());
    for (Tag tag : TAGS_SET) {
      identifiers.add(new ResourceIdentifier(TAGS, tag.name(), null, null, Map.of()));
    }
    return new RelationshipData.IdentifierCollectionLinkage(identifiers);
  }
}
