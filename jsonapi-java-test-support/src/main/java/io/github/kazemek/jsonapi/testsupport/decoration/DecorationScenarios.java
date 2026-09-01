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

  private static final String ARTICLE_TYPE = "articles";
  private static final String COMMENTS_TYPE = "comments";
  private static final String PEOPLE_TYPE = "people";
  private static final String TITLE_ATTR = "title";
  private static final String BODY_ATTR = "body-text";
  private static final String AUTHOR_REL = "author";
  private static final String COMMENTS_REL = "comments";
  private static final String TITLE_VALUE = "Title";
  private static final String BODY_VALUE = "Body";
  private static final String COMMENT_ID = "c1";
  private static final String PERSON_ID = "p1";
  private static final String PERSON_NAME = "Alice";
  private static final String COMMENT_BODY = "Nice";

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
              () -> new Article("1", TITLE_VALUE, BODY_VALUE, List.of(), null),
              ResourceDecoratorRegistry.builder()
                  .register(Article.class, article -> ResourceDecoration.ofLinks(RESOURCE_LINKS))
                  .build(),
              new ResourceObject(
                  ARTICLE_TYPE,
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of(TITLE_ATTR, TITLE_VALUE, BODY_ATTR, BODY_VALUE)),
                  Relationships.ofRelationships(
                      Map.of(
                          AUTHOR_REL,
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          COMMENTS_REL,
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
                  new Article(
                      "1",
                      TITLE_VALUE,
                      BODY_VALUE,
                      List.of(new Comment(COMMENT_ID, COMMENT_BODY, null)),
                      null),
              ResourceDecoratorRegistry.builder()
                  .register(
                      Article.class,
                      article ->
                          ResourceDecoration.builder()
                              .relationship(COMMENTS_REL, RelationshipDecoration.of(COMMENTS_LINKS))
                              .build())
                  .build(),
              new ResourceObject(
                  ARTICLE_TYPE,
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of(TITLE_ATTR, TITLE_VALUE, BODY_ATTR, BODY_VALUE)),
                  Relationships.ofRelationships(
                      Map.of(
                          AUTHOR_REL,
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          COMMENTS_REL,
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(
                                  List.of(
                                      new ResourceIdentifier(
                                          COMMENTS_TYPE, COMMENT_ID, null, null, Map.of()))),
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
                      TITLE_VALUE,
                      BODY_VALUE,
                      List.of(new Comment(COMMENT_ID, COMMENT_BODY, null)),
                      new Person(PERSON_ID, PERSON_NAME)),
              ResourceDecoratorRegistry.builder()
                  .register(
                      Article.class,
                      article ->
                          ResourceDecoration.builder()
                              .links(RESOURCE_LINKS)
                              .relationship(COMMENTS_REL, RelationshipDecoration.of(COMMENTS_LINKS))
                              .build())
                  .build(),
              new ResourceObject(
                  ARTICLE_TYPE,
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of(TITLE_ATTR, TITLE_VALUE, BODY_ATTR, BODY_VALUE)),
                  Relationships.ofRelationships(
                      Map.of(
                          AUTHOR_REL,
                          new Relationship(
                              new RelationshipData.SingleLinkage(
                                  new ResourceIdentifier(
                                      PEOPLE_TYPE, PERSON_ID, null, null, Map.of())),
                              null,
                              null,
                              Map.of()),
                          COMMENTS_REL,
                          new Relationship(
                              new RelationshipData.IdentifierCollectionLinkage(
                                  List.of(
                                      new ResourceIdentifier(
                                          COMMENTS_TYPE, COMMENT_ID, null, null, Map.of()))),
                              COMMENTS_LINKS,
                              null,
                              Map.of()))),
                  RESOURCE_LINKS,
                  null,
                  Map.of())),
          new DecorationScenario(
              "present-empty links are preserved",
              () -> new Article("1", TITLE_VALUE, BODY_VALUE, List.of(), null),
              ResourceDecoratorRegistry.builder()
                  .register(Article.class, article -> ResourceDecoration.ofLinks(EMPTY_LINKS))
                  .build(),
              new ResourceObject(
                  ARTICLE_TYPE,
                  "1",
                  null,
                  Attributes.ofAttributes(Map.of(TITLE_ATTR, TITLE_VALUE, BODY_ATTR, BODY_VALUE)),
                  Relationships.ofRelationships(
                      Map.of(
                          AUTHOR_REL,
                          new Relationship(
                              RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()),
                          COMMENTS_REL,
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
