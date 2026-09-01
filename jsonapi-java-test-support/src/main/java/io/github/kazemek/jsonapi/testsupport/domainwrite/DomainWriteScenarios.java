package io.github.kazemek.jsonapi.testsupport.domainwrite;

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
import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope;
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMapMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithRelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentsRelationshipMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.WholeMetaTargetFixtures;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ArticleWithSet;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ArticleWithUnannotatedExtra;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.BlogWithJsonProperty;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ConventionalId;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.InheritedBlogFixtures;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.RelationshipContainerFixtures;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.RelationshipLinkageContainerFixtures;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.SamplePojo;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Tag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The shared flat domain-to-resource write catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the mapping surface grows, and adapter
 * suites pick them up through {@link #catalog()}. Consumers dispatch on the {@link
 * DomainWriteOperation}/{@link DomainWriteInput} descriptor, never on a scenario id.
 */
@SuppressWarnings({"unchecked", "SameParameterValue"})
public final class DomainWriteScenarios {

  private static final String COMMENTS = "comments";

  private static final String PEOPLE = "people";

  private static final String ARTICLES = "articles";

  private static final String TITLE = "title";

  private static final String ALICE = "Alice";

  private static final String EDITOR = "editor";

  private static final String ROLE = "role";

  private static final String DISPLAY_NAME = "displayName";

  private static final String TAGS = "tags";

  private static final String AUTHOR = "author";

  private static final String TITLE_TEXT = "Title";

  private static final String MY_BLOG = "My Blog";

  private static final String GREAT = "Great";

  private static final String PINNED = "pinned";

  private static final String EXT_HREF = "ext:href";

  private static final String EXAMPLE_HREF = "https://example.test/p1";

  private static final String SOURCE = "source";

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
                  () -> new Article("1", TITLE_TEXT, "Content", List.of(), null)),
              null,
              DomainWriteOutcome.resource(
                  articleResource("1", TITLE_TEXT, "Content", List.of(), null)),
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
              "unannotated extra property is not an attribute",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new ArticleWithUnannotatedExtra("1", TITLE_TEXT, "secret")),
              null,
              DomainWriteOutcome.resource(attributesOnlyArticle("1", Map.of(TITLE, TITLE_TEXT))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps @JsonProperty naming",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(() -> new BlogWithJsonProperty("b1", MY_BLOG)),
              null,
              DomainWriteOutcome.resource(
                  new ResourceObject(
                      "blogs",
                      "b1",
                      null,
                      Attributes.ofAttributes(singleAttribute("blog_title", MY_BLOG)),
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
                  () -> new Article("1", "T", "B", List.of(), new Person("p1", ALICE))),
              null,
              DomainWriteOutcome.resource(
                  articleResource("1", "T", "B", List.of(), new Person("p1", ALICE))),
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
                          List.of(new Comment("c1", "Nice", null), new Comment("c2", GREAT, null)),
                          null)),
              null,
              DomainWriteOutcome.resource(
                  articleResource(
                      "1",
                      "T",
                      "B",
                      List.of(new Comment("c1", "Nice", null), new Comment("c2", GREAT, null)),
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
              "maps to-one identifier meta onto linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          new RelationshipLinkage<>(
                              ResourceIdentifier.of(PEOPLE, "p1"), new AuthorIdMeta(EDITOR)),
                          List.of(),
                          null,
                          null)),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                      null,
                      List.of(),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps to-many identifier meta with each wrapper element",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          null,
                          List.of(
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(COMMENTS, "c1"), new CommentIdMeta(true)),
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(COMMENTS, "c2"), null)),
                          null,
                          null)),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      null,
                      null,
                      List.of(
                          identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                          ResourceIdentifier.of(COMMENTS, "c2")),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "null wrapper meta leaves ResourceIdentifier meta in place",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          new RelationshipLinkage<>(
                              identifier(
                                  PEOPLE,
                                  "p1",
                                  null,
                                  Meta.of(Map.of(ROLE, EDITOR)),
                                  Map.of(EXT_HREF, EXAMPLE_HREF)),
                              null),
                          List.of(),
                          null,
                          null)),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      identifier(
                          PEOPLE,
                          "p1",
                          null,
                          Meta.of(Map.of(ROLE, EDITOR)),
                          Map.of(EXT_HREF, EXAMPLE_HREF)),
                      null,
                      List.of(),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "writes relationship meta and identifier meta independently",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          new RelationshipLinkage<>(
                              ResourceIdentifier.of(PEOPLE, "p1"), new AuthorIdMeta(EDITOR)),
                          List.of(
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(COMMENTS, "c1"), new CommentIdMeta(true))),
                          new AuthorMeta(ALICE),
                          new CommentsRelationshipMeta("open"))),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                      Meta.of(Map.of(DISPLAY_NAME, ALICE)),
                      List.of(identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true)))),
                      Meta.of(Map.of("status", "open")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "writes an empty to-many RelationshipLinkage collection",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new ArticleWithRelationshipLinkage("1", "T", null, List.of(), null, null)),
              null,
              DomainWriteOutcome.resource(identifierMetaArticle(null, null, List.of(), null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps array to-many RelationshipLinkage identifier meta",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipLinkageContainerFixtures.ArrayRelationshipLinkageArticle(
                          "1",
                          new RelationshipLinkage[] {
                            new RelationshipLinkage<>(
                                ResourceIdentifier.of(COMMENTS, "c1"), new CommentIdMeta(true)),
                            new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c2"), null)
                          })),
              null,
              DomainWriteOutcome.resource(
                  commentsOnlyArticle(
                      List.of(
                          identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                          ResourceIdentifier.of(COMMENTS, "c2")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps Set of RelationshipLinkage identifier meta",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipLinkageContainerFixtures.SetRelationshipLinkageArticle(
                          "1",
                          Set.of(
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(COMMENTS, "c1"),
                                  new CommentIdMeta(true))))),
              null,
              DomainWriteOutcome.resource(
                  commentsOnlyArticle(
                      List.of(identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true)))))),
              new DomainWriteComparisonPolicy(
                  Map.of(
                      COMMENTS,
                      DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS))),
          new DomainWriteScenario(
              "maps Optional RelationshipLinkage identifier meta",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipLinkageContainerFixtures.OptionalRelationshipLinkageArticle(
                          "1",
                          Optional.of(
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(PEOPLE, "p1"), new AuthorIdMeta(EDITOR))))),
              null,
              DomainWriteOutcome.resource(
                  authorOnlyArticle(identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps Map identifier meta on to-many RelationshipLinkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipLinkageContainerFixtures.MapRelationshipLinkageArticle(
                          "1",
                          List.of(
                              new RelationshipLinkage<>(
                                  ResourceIdentifier.of(COMMENTS, "c1"), Map.of(PINNED, true))))),
              null,
              DomainWriteOutcome.resource(
                  commentsOnlyArticle(
                      List.of(identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true)))))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps renamed RelationshipLinkage identifier meta onto the wire name",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipLinkageContainerFixtures.RenamedRelationshipLinkageArticle(
                          "1",
                          new RelationshipLinkage<>(
                              ResourceIdentifier.of(PEOPLE, "p1"), new AuthorIdMeta(EDITOR)))),
              null,
              DomainWriteOutcome.resource(
                  authorOnlyArticle(identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "identifier-meta overlay preserves ResourceIdentifier lid",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          new RelationshipLinkage<>(
                              identifier(PEOPLE, null, "lid-1", null, Map.of()),
                              new AuthorIdMeta(EDITOR)),
                          List.of(),
                          null,
                          null)),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      identifier(PEOPLE, null, "lid-1", Meta.of(Map.of(ROLE, EDITOR)), Map.of()),
                      null,
                      List.of(),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "identifier-meta overlay preserves additional members",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithRelationshipLinkage(
                          "1",
                          "T",
                          new RelationshipLinkage<>(
                              identifier(
                                  PEOPLE,
                                  "p1",
                                  null,
                                  Meta.of(Map.of(ROLE, "old")),
                                  Map.of(EXT_HREF, EXAMPLE_HREF)),
                              new AuthorIdMeta(EDITOR)),
                          List.of(),
                          null,
                          null)),
              null,
              DomainWriteOutcome.resource(
                  identifierMetaArticle(
                      identifier(
                          PEOPLE,
                          "p1",
                          null,
                          Meta.of(Map.of(ROLE, EDITOR)),
                          Map.of(EXT_HREF, EXAMPLE_HREF)),
                      null,
                      List.of(),
                      null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps resource meta and relationship meta",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithMeta(
                          "1",
                          "T",
                          ResourceIdentifier.of(PEOPLE, "p1"),
                          new ArticleMeta("cms", "n"),
                          new AuthorMeta(ALICE))),
              null,
              DomainWriteOutcome.resource(
                  articleWithMetaResource(
                      Meta.of(Map.of(SOURCE, "cms", "note", "n")),
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      Meta.of(Map.of(DISPLAY_NAME, ALICE)))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "null meta properties omit meta members",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new ArticleWithMeta("1", "T", null, null, null)),
              null,
              DomainWriteOutcome.resource(articleWithMetaResource(null, null, null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "empty map meta emits empty members",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new ArticleWithMapMeta("1", "T", null, Map.of(), null)),
              null,
              DomainWriteOutcome.resource(articleWithMetaResource(Meta.empty(), null, null)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "populated map meta writes resource and relationship members",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithMapMeta(
                          "1",
                          "T",
                          ResourceIdentifier.of(PEOPLE, "p1"),
                          Map.of(SOURCE, "cms"),
                          Map.of(DISPLAY_NAME, ALICE))),
              null,
              DomainWriteOutcome.resource(
                  articleWithMetaResource(
                      Meta.of(Map.of(SOURCE, "cms")),
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      Meta.of(Map.of(DISPLAY_NAME, ALICE)))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "maps renamed relationship meta onto the wire name",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new WholeMetaTargetFixtures.RenamedRelationshipMetaArticle(
                          "1",
                          "T",
                          ResourceIdentifier.of(PEOPLE, "p1"),
                          new ArticleMeta("cms", "n"),
                          new AuthorMeta(ALICE))),
              null,
              DomainWriteOutcome.resource(
                  articleWithMetaResource(
                      Meta.of(Map.of(SOURCE, "cms", "note", "n")),
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      Meta.of(Map.of(DISPLAY_NAME, ALICE)))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "Object whole-meta target writes a map value",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new WholeMetaTargetFixtures.ObjectMetaArticle("1", Map.of(SOURCE, "cms"))),
              null,
              DomainWriteOutcome.resource(objectMetaArticle(Meta.of(Map.of(SOURCE, "cms")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "Optional-wrapped bean meta writes unwrapped members",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new ArticleWithOptionalMeta(
                          "1",
                          "T",
                          null,
                          Optional.of(new ArticleMeta("cms", "n")),
                          Optional.of(new AuthorMeta(ALICE)))),
              null,
              DomainWriteOutcome.resource(
                  articleWithMetaResource(
                      Meta.of(Map.of(SOURCE, "cms", "note", "n")),
                      null,
                      Meta.of(Map.of(DISPLAY_NAME, ALICE)))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "present Optional attribute is unwrapped",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithOptionalAttribute(
                          "1", TITLE_TEXT, Optional.of("Sub"))),
              null,
              DomainWriteOutcome.resource(
                  attributesOnlyArticle("1", Map.of(TITLE, TITLE_TEXT, "subtitle", "Sub"))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "empty Optional attribute is omitted",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithOptionalAttribute(
                          "1", TITLE_TEXT, Optional.empty())),
              null,
              DomainWriteOutcome.resource(attributesOnlyArticle("1", Map.of(TITLE, TITLE_TEXT))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "array to-many relationship produces collection linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithCommentArray(
                          "1",
                          "T",
                          new Comment[] {
                            new Comment("c1", "Nice", null), new Comment("c2", GREAT, null)
                          })),
              null,
              DomainWriteOutcome.resource(
                  titledCommentsArticle(
                      "T",
                      List.of(
                          ResourceIdentifier.of(COMMENTS, "c1"),
                          ResourceIdentifier.of(COMMENTS, "c2")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "present Optional to-one relationship produces single linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithOptionalRelationship(
                          "1", Optional.of(new Comment("c1", "Nice", null)))),
              null,
              DomainWriteOutcome.resource(
                  commentRelationshipArticle(
                      new RelationshipData.SingleLinkage(ResourceIdentifier.of(COMMENTS, "c1")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "empty Optional to-one relationship produces null linkage",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithOptionalRelationship(
                          "1", Optional.empty())),
              null,
              DomainWriteOutcome.resource(
                  commentRelationshipArticle(RelationshipData.NullLinkage.INSTANCE)),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "present Optional id is unwrapped to the identifier string",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithOptionalId(
                          Optional.of("99"), TITLE_TEXT)),
              null,
              DomainWriteOutcome.resource(attributesOnlyArticle("99", Map.of(TITLE, TITLE_TEXT))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "inherited properties from a base class are mapped",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () -> new InheritedBlogFixtures.ExtendedBlog("b1", MY_BLOG, "A description")),
              null,
              DomainWriteOutcome.resource(
                  new ResourceObject(
                      "blogs",
                      "b1",
                      null,
                      Attributes.ofAttributes(
                          Map.of("name", MY_BLOG, "description", "A description")),
                      null,
                      null,
                      null,
                      Map.of())),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "leading null in a to-many ResourceIdentifier collection is skipped",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithNullableIdentifierList(
                          "1", nullableList(null, ResourceIdentifier.of(COMMENTS, "1")))),
              null,
              DomainWriteOutcome.resource(
                  itemsRelationshipArticle(List.of(ResourceIdentifier.of(COMMENTS, "1")))),
              DomainWriteComparisonPolicy.ordered()),
          new DomainWriteScenario(
              "leading null in a to-many ResourceIdentifier array is skipped",
              DomainWriteOperation.TO_RESOURCE,
              new DomainWriteInput.SingleInput(
                  () ->
                      new RelationshipContainerFixtures.ArticleWithNullableIdentifierArray(
                          "1",
                          new @Nullable ResourceIdentifier[] {
                            null, ResourceIdentifier.of(COMMENTS, "1")
                          })),
              null,
              DomainWriteOutcome.resource(
                  itemsRelationshipArticle(List.of(ResourceIdentifier.of(COMMENTS, "1")))),
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

  private static ResourceObject articleResource(
      String id, String title, String body, List<Comment> comments, @Nullable Person author) {
    return new ResourceObject(
        ARTICLES,
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
        ARTICLES,
        "1",
        null,
        Attributes.ofAttributes(singleAttribute(TITLE, "T")),
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
    attributes.put(TITLE, title);
    attributes.put("body-text", body);
    return attributes;
  }

  private static Map<String, @Nullable Relationship> articleRelationships(
      RelationshipData authorLinkage, RelationshipData commentsLinkage) {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(AUTHOR, relationship(authorLinkage));
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
        new ResourceIdentifier(PEOPLE, author.id(), null, null, Map.of()));
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

  private static ResourceObject identifierMetaArticle(
      @Nullable ResourceIdentifier author,
      @Nullable Meta authorRelationshipMeta,
      List<ResourceIdentifier> comments,
      @Nullable Meta commentsRelationshipMeta) {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        new Relationship(
            author == null
                ? RelationshipData.NullLinkage.INSTANCE
                : new RelationshipData.SingleLinkage(author),
            null,
            authorRelationshipMeta,
            Map.of()));
    relationships.put(
        COMMENTS,
        new Relationship(
            new RelationshipData.IdentifierCollectionLinkage(comments),
            null,
            commentsRelationshipMeta,
            Map.of()));
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        Attributes.ofAttributes(singleAttribute(TITLE, "T")),
        Relationships.ofRelationships(relationships),
        null,
        null,
        Map.of());
  }

  private static ResourceIdentifier identifier(String type, String id, @Nullable Meta meta) {
    return identifier(type, id, null, meta, Map.of());
  }

  private static ResourceIdentifier identifier(
      String type,
      @Nullable String id,
      @Nullable String lid,
      @Nullable Meta meta,
      Map<String, Object> additionalMembers) {
    return new ResourceIdentifier(type, id, lid, meta, additionalMembers);
  }

  private static ResourceObject commentsOnlyArticle(List<ResourceIdentifier> comments) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        null,
        Relationships.ofRelationships(
            Map.of(
                COMMENTS,
                new Relationship(
                    new RelationshipData.IdentifierCollectionLinkage(comments),
                    null,
                    null,
                    Map.of()))),
        null,
        null,
        Map.of());
  }

  private static ResourceObject authorOnlyArticle(ResourceIdentifier author) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        null,
        Relationships.ofRelationships(
            Map.of(
                AUTHOR,
                new Relationship(
                    new RelationshipData.SingleLinkage(author), null, null, Map.of()))),
        null,
        null,
        Map.of());
  }

  private static ResourceObject articleWithMetaResource(
      @Nullable Meta resourceMeta,
      @Nullable ResourceIdentifier author,
      @Nullable Meta authorRelationshipMeta) {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        new Relationship(
            author == null
                ? RelationshipData.NullLinkage.INSTANCE
                : new RelationshipData.SingleLinkage(author),
            null,
            authorRelationshipMeta,
            Map.of()));
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        Attributes.ofAttributes(singleAttribute(TITLE, "T")),
        Relationships.ofRelationships(relationships),
        null,
        resourceMeta,
        Map.of());
  }

  private static ResourceObject objectMetaArticle(Meta resourceMeta) {
    return new ResourceObject(ARTICLES, "1", null, null, null, null, resourceMeta, Map.of());
  }

  private static ResourceObject attributesOnlyArticle(String id, Map<String, Object> attributes) {
    return new ResourceObject(
        ARTICLES, id, null, Attributes.ofAttributes(attributes), null, null, null, Map.of());
  }

  private static ResourceObject titledCommentsArticle(
      String title, List<ResourceIdentifier> comments) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        Attributes.ofAttributes(singleAttribute(TITLE, title)),
        Relationships.ofRelationships(
            Map.of(
                COMMENTS,
                new Relationship(
                    new RelationshipData.IdentifierCollectionLinkage(comments),
                    null,
                    null,
                    Map.of()))),
        null,
        null,
        Map.of());
  }

  private static ResourceObject commentRelationshipArticle(RelationshipData data) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        null,
        Relationships.ofRelationships(
            Map.of("comment", new Relationship(data, null, null, Map.of()))),
        null,
        null,
        Map.of());
  }

  private static ResourceObject itemsRelationshipArticle(List<ResourceIdentifier> items) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        null,
        Relationships.ofRelationships(
            Map.of(
                "items",
                new Relationship(
                    new RelationshipData.IdentifierCollectionLinkage(items),
                    null,
                    null,
                    Map.of()))),
        null,
        null,
        Map.of());
  }

  @SafeVarargs
  private static <T> List<@Nullable T> nullableList(@Nullable T... values) {
    List<@Nullable T> list = new ArrayList<>(values.length);
    Collections.addAll(list, values);
    return list;
  }
}
