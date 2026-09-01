package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipDecoration;
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoration;
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import java.util.List;
import java.util.Map;

/**
 * Shared decoration catalog for Jackson-major-neutral resource-link decoration. Jackson 3 and
 * future Jackson 2 suites iterate {@link #catalog()} and verify via {@link DecorationVerifier}.
 */
public final class DecorationScenarios {

  private static final Links RESOURCE_LINKS =
      Links.ofLinks(Map.of("self", new Link.StringLink("https://example.test/articles/1")));

  private static final Links COMMENTS_LINKS =
      Links.ofLinks(
          Map.of(
              "self", new Link.StringLink("https://example.test/articles/1/relationships/comments"),
              "related", new Link.StringLink("https://example.test/articles/1/comments")));

  private static final Links EMPTY_LINKS = Links.empty();

  private static final List<DecorationScenario> SCENARIOS =
      List.of(
          new DecorationScenario(
              "resource links preserve attributes and linkage",
              () -> new Article("1", "Title", "Body", List.of(), null),
              ResourceDecoratorRegistry.builder()
                  .register(Article.class, article -> ResourceDecoration.ofLinks(RESOURCE_LINKS))
                  .build(),
              new ResourceObject(
                  "articles",
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of("title", "Title", "body-text", "Body")),
                  Relationships.ofRelationships(
                      Map.of(
                          "author",
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          "comments",
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(List.of()),
                              null,
                              null,
                              Map.of()))),
                  RESOURCE_LINKS,
                  null,
                  Map.of())),
          new DecorationScenario(
              "relationship links preserve linkage",
              () ->
                  new Article("1", "Title", "Body", List.of(new Comment("c1", "Nice", null)), null),
              ResourceDecoratorRegistry.builder()
                  .register(
                      Article.class,
                      article ->
                          ResourceDecoration.builder()
                              .relationship("comments", RelationshipDecoration.of(COMMENTS_LINKS))
                              .build())
                  .build(),
              new ResourceObject(
                  "articles",
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of("title", "Title", "body-text", "Body")),
                  Relationships.ofRelationships(
                      Map.of(
                          "author",
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          "comments",
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(
                                  List.of(
                                      new ResourceIdentifier(
                                          "comments", "c1", null, null, Map.of()))),
                              COMMENTS_LINKS,
                              null,
                              Map.of()))),
                  null,
                  null,
                  Map.of())),
          new DecorationScenario(
              "resource and relationship links together preserve meta",
              () ->
                  new Article(
                      "1",
                      "Title",
                      "Body",
                      List.of(new Comment("c1", "Nice", null)),
                      new Person("p1", "Alice")),
              ResourceDecoratorRegistry.builder()
                  .register(
                      Article.class,
                      article ->
                          ResourceDecoration.builder()
                              .links(RESOURCE_LINKS)
                              .relationship("comments", RelationshipDecoration.of(COMMENTS_LINKS))
                              .build())
                  .build(),
              new ResourceObject(
                  "articles",
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of("title", "Title", "body-text", "Body")),
                  Relationships.ofRelationships(
                      Map.of(
                          "author",
                          new Relationship(
                              new RelationshipData.SingleLinkage(
                                  new ResourceIdentifier("people", "p1", null, null, Map.of())),
                              null,
                              null,
                              Map.of()),
                          "comments",
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(
                                  List.of(
                                      new ResourceIdentifier(
                                          "comments", "c1", null, null, Map.of()))),
                              COMMENTS_LINKS,
                              null,
                              Map.of()))),
                  RESOURCE_LINKS,
                  null,
                  Map.of())),
          new DecorationScenario(
              "present-empty links are preserved",
              () -> new Article("1", "Title", "Body", List.of(), null),
              ResourceDecoratorRegistry.builder()
                  .register(Article.class, article -> ResourceDecoration.ofLinks(EMPTY_LINKS))
                  .build(),
              new ResourceObject(
                  "articles",
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of("title", "Title", "body-text", "Body")),
                  Relationships.ofRelationships(
                      Map.of(
                          "author",
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          "comments",
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(List.of()),
                              null,
                              null,
                              Map.of()))),
                  EMPTY_LINKS,
                  null,
                  Map.of())));

  private static final FixtureCatalog<DecorationScenario> CATALOG =
      FixtureCatalog.of("decoration", SCENARIOS);

  private DecorationScenarios() {}

  public static FixtureCatalog<DecorationScenario> catalog() {
    return CATALOG;
  }
}
