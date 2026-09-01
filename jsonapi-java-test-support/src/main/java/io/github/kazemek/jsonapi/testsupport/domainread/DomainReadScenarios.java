package io.github.kazemek.jsonapi.testsupport.domainread;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.TestSupportResources;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMapMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithOptionalMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentsRelationshipMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.WholeMetaTargetFixtures;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithArray;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithOptional;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithSet;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatCountedThing;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatCreatorArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatDefaultedArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatInheritedBlog;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatIntIdArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatLidArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatMetaArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatMutableArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatRelationshipLinkageArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatRequiredThing;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatThingWithIgnored;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatThrowingCreatorThing;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatUnregisteredRelationshipsArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ArticleWithUnannotatedExtra;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.BlogWithJsonProperty;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ConventionalId;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.RelationshipLinkageContainerFixtures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The shared flat resource-to-DTO binding catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the binder surface grows, and adapter
 * suites pick them up through {@link #catalog()}. Consumers dispatch on the {@link DomainReadInput}
 * variant and {@link ConverterBehavior}, never on a scenario id.
 */
@SuppressWarnings({"unchecked", "SameParameterValue"})
public final class DomainReadScenarios {

  private static final String ARTICLES = "articles";
  private static final String AUTHOR = "author";
  private static final String ALICE = "Alice";
  private static final String COMMENTS = "comments";
  private static final String DISPLAY_NAME = "displayName";
  private static final String EDITOR = "editor";
  private static final String PINNED = "pinned";
  private static final String PEOPLE = "people";
  private static final String ROLE = "role";
  private static final String TITLE = "title";
  private static final String THINGS = "things";
  private static final String HELLO = "Hello";
  private static final String MY_BLOG = "My Blog";
  private static final String REL_AUTHOR_DATA = "/relationships/author/data";
  private static final String REL_COMMENTS_DATA = "/relationships/comments/data";
  private static final String META_PATH = "/meta";
  private static final String SOURCE = "source";

  private static final String INCLUDED_PRIMARY = doc("included-isolation-primary");
  private static final String INCLUDED_SWAPPED = doc("included-isolation-swapped");

  private static String doc(String stem) {
    return TestSupportResources.readCorpusUtf8("domain-read/" + stem + ".json");
  }

  private static final List<DomainReadScenario> SCENARIOS =
      List.of(
          new DomainReadScenario(
              "binds record with id, attributes, and built-in ResourceIdentifier relationships",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO, "body-text", "Content"),
                      rels(
                          AUTHOR,
                          toOne(PEOPLE, "p1"),
                          COMMENTS,
                          toMany(COMMENTS, List.of("c1", "c2"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatArticle(
                      "1",
                      HELLO,
                      "Content",
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      List.of(
                          ResourceIdentifier.of(COMMENTS, "c1"),
                          ResourceIdentifier.of(COMMENTS, "c2"))))),
          new DomainReadScenario(
              "binds mutable POJO",
              DomainReadInput.single(
                  resource(ARTICLES, "1", attrs(TITLE, HELLO), rels(AUTHOR, toOne(PEOPLE, "p1")))),
              FlatMutableArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatMutableArticle("1", HELLO, ResourceIdentifier.of(PEOPLE, "p1")))),
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
                      "blogs", "b1", attrs("name", MY_BLOG, "description", "A description"), null)),
              FlatInheritedBlog.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatInheritedBlog("b1", MY_BLOG, "A description"))),
          new DomainReadScenario(
              "binds @JsonProperty named attribute",
              DomainReadInput.single(resource("blogs", "b1", attrs("blog_title", MY_BLOG), null)),
              BlogWithJsonProperty.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new BlogWithJsonProperty("b1", MY_BLOG))),
          new DomainReadScenario(
              "unannotated extra property is not bound as an attribute",
              DomainReadInput.single(
                  resource(ARTICLES, "1", attrs(TITLE, HELLO, "ignoredExtra", "secret"), null)),
              ArticleWithUnannotatedExtra.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new ArticleWithUnannotatedExtra("1", HELLO, null))),
          new DomainReadScenario(
              "binds conventional id property",
              DomainReadInput.single(
                  resource("conventionals", "42", attrs("name", "name value"), null)),
              ConventionalId.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new ConventionalId("42", "name value"))),
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
              DomainReadInput.single(resourceWithLid("l1", attrs(TITLE, "T"))),
              FlatLidArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatLidArticle("l1", "T"))),
          new DomainReadScenario(
              "resource without id or lid omits the identifier property",
              DomainReadInput.single(resourceWithLid(null, attrs(TITLE, "T"))),
              FlatLidArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatLidArticle(null, "T"))),
          new DomainReadScenario(
              "explicit-null attribute binds null and omitted attribute keeps its default",
              DomainReadInput.single(resource(ARTICLES, "1", nullableAttr(TITLE), null)),
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
                  resource(ARTICLES, "1", null, rels(COMMENTS, toMany(COMMENTS, List.of("c1"))))),
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
                  resource(ARTICLES, "1", null, rels(AUTHOR, toMany(PEOPLE, List.of("p1", "p2"))))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, REL_AUTHOR_DATA)),
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
                      ARTICLES, "1", null, rels(COMMENTS, toMany(COMMENTS, List.of("c1", "c2"))))),
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
                  resource(ARTICLES, "1", null, rels("tags", toMany("tags", List.of("t1", "t2"))))),
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
                      ARTICLES, "1", null, rels(COMMENTS, toMany(COMMENTS, List.of("c1", "c2"))))),
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
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, REL_COMMENTS_DATA)),
          new DomainReadScenario(
              "single linkage on to-many is a cardinality mismatch",
              DomainReadInput.single(
                  resource(ARTICLES, "1", null, rels(COMMENTS, toOne(COMMENTS, "c1")))),
              FlatArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, REL_COMMENTS_DATA)),
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
                  MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH, REL_AUTHOR_DATA)),
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
                  resource(ARTICLES, "1", null, rels(AUTHOR, toOne(PEOPLE, "p1")))),
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
                  resource(ARTICLES, "1", null, rels(AUTHOR, toOne(PEOPLE, "p1")))),
              FlatUnregisteredRelationshipsArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET, REL_AUTHOR_DATA)),
          new DomainReadScenario(
              "unregistered to-many relationship target is UNSUPPORTED_RELATIONSHIP_TARGET",
              DomainReadInput.single(
                  resource(ARTICLES, "1", null, rels(COMMENTS, toMany(COMMENTS, List.of("c1"))))),
              FlatUnregisteredRelationshipsArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(
                  MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET, REL_COMMENTS_DATA)),
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
              DomainReadInput.single(resource(THINGS, "1", nullableAttr("count"), null)),
              FlatCountedThing.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE)),
          new DomainReadScenario(
              "binds resource meta and relationship meta",
              DomainReadInput.single(
                  resourceWithMeta(
                      attrs(TITLE, HELLO),
                      relsWithMeta(toOne(PEOPLE, "p1"), Meta.of(Map.of(DISPLAY_NAME, ALICE))),
                      Meta.of(Map.of(SOURCE, "cms", "note", "n")))),
              FlatMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatMetaArticle(
                      "1",
                      HELLO,
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      new ArticleMeta("cms", "n"),
                      new AuthorMeta(ALICE)))),
          new DomainReadScenario(
              "binds renamed relationship meta from the wire name",
              DomainReadInput.single(
                  resourceWithMeta(
                      attrs(TITLE, HELLO),
                      relsWithMeta(toOne(PEOPLE, "p1"), Meta.of(Map.of(DISPLAY_NAME, ALICE))),
                      Meta.of(Map.of(SOURCE, "cms", "note", "n")))),
              WholeMetaTargetFixtures.RenamedRelationshipMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new WholeMetaTargetFixtures.RenamedRelationshipMetaArticle(
                      "1",
                      HELLO,
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      new ArticleMeta("cms", "n"),
                      new AuthorMeta(ALICE)))),
          new DomainReadScenario(
              "absent meta leaves meta properties null",
              DomainReadInput.single(resource(ARTICLES, "1", attrs(TITLE, HELLO), null)),
              FlatMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new FlatMetaArticle("1", HELLO, null, null, null))),
          new DomainReadScenario(
              "meta-only relationship binds its meta on the read side",
              DomainReadInput.single(
                  resourceWithMeta(
                      attrs(TITLE, HELLO),
                      relsOnlyMeta(Meta.of(Map.of(DISPLAY_NAME, ALICE))),
                      null)),
              FlatMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatMetaArticle("1", HELLO, null, null, new AuthorMeta(ALICE)))),
          new DomainReadScenario(
              "binds to-one identifier meta",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(
                          AUTHOR,
                          toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR)))))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      new RelationshipLinkage<>(
                          identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                          new AuthorIdMeta(EDITOR)),
                      null,
                      null,
                      null))),
          new DomainReadScenario(
              "binds to-many identifier meta on each wrapper element",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(
                          COMMENTS,
                          toManyWithIdentifierMetas(
                              COMMENTS,
                              List.of("c1", "c2"),
                              nullableList(Meta.of(Map.of(PINNED, true)), null))))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      null,
                      List.of(
                          new RelationshipLinkage<>(
                              identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                              new CommentIdMeta(true)),
                          new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c2"), null)),
                      null,
                      null))),
          new DomainReadScenario(
              "absent identifier meta leaves wrapper meta null",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(
                          AUTHOR, toOne(PEOPLE, "p1"), COMMENTS, toMany(COMMENTS, List.of("c1"))))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      new RelationshipLinkage<>(ResourceIdentifier.of(PEOPLE, "p1"), null),
                      List.of(
                          new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c1"), null)),
                      null,
                      null))),
          new DomainReadScenario(
              "empty identifier meta object binds an empty application value",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(AUTHOR, toOneWithIdentifierMeta(PEOPLE, "p1", Meta.empty())))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      new RelationshipLinkage<>(
                          identifier(PEOPLE, "p1", Meta.empty()), new AuthorIdMeta(null)),
                      null,
                      null,
                      null))),
          new DomainReadScenario(
              "identifier meta is distinct from relationship meta",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(
                          AUTHOR,
                          toOneWithBothMeta(
                              PEOPLE,
                              "p1",
                              Meta.of(Map.of(ROLE, EDITOR)),
                              Meta.of(Map.of(DISPLAY_NAME, ALICE))),
                          COMMENTS,
                          toManyWithBothMeta(
                              COMMENTS,
                              List.of("c1"),
                              nullableList(Meta.of(Map.of(PINNED, true))),
                              Meta.of(Map.of("status", "open")))))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      new RelationshipLinkage<>(
                          identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                          new AuthorIdMeta(EDITOR)),
                      List.of(
                          new RelationshipLinkage<>(
                              identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                              new CommentIdMeta(true))),
                      new AuthorMeta(ALICE),
                      new CommentsRelationshipMeta("open")))),
          new DomainReadScenario(
              "binds array to-many identifier meta on each wrapper element",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS,
                          toManyWithIdentifierMetas(
                              COMMENTS,
                              List.of("c1", "c2"),
                              nullableList(Meta.of(Map.of(PINNED, true)), null))))),
              RelationshipLinkageContainerFixtures.ArrayRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new RelationshipLinkageContainerFixtures.ArrayRelationshipLinkageArticle(
                      "1",
                      new RelationshipLinkage[] {
                        new RelationshipLinkage<>(
                            identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                            new CommentIdMeta(true)),
                        new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c2"), null)
                      }))),
          new DomainReadScenario(
              "binds Set of RelationshipLinkage identifier meta",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS,
                          toManyWithIdentifierMetas(
                              COMMENTS,
                              List.of("c1"),
                              nullableList(Meta.of(Map.of(PINNED, true))))))),
              RelationshipLinkageContainerFixtures.SetRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new RelationshipLinkageContainerFixtures.SetRelationshipLinkageArticle(
                      "1",
                      Set.of(
                          new RelationshipLinkage<>(
                              identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                              new CommentIdMeta(true)))))),
          new DomainReadScenario(
              "binds Optional RelationshipLinkage identifier meta",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          AUTHOR,
                          toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR)))))),
              RelationshipLinkageContainerFixtures.OptionalRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new RelationshipLinkageContainerFixtures.OptionalRelationshipLinkageArticle(
                      "1",
                      Optional.of(
                          new RelationshipLinkage<>(
                              identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                              new AuthorIdMeta(EDITOR)))))),
          new DomainReadScenario(
              "binds Map identifier meta on to-many RelationshipLinkage",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          COMMENTS,
                          toManyWithIdentifierMetas(
                              COMMENTS,
                              List.of("c1"),
                              nullableList(Meta.of(Map.of(PINNED, true))))))),
              RelationshipLinkageContainerFixtures.MapRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new RelationshipLinkageContainerFixtures.MapRelationshipLinkageArticle(
                      "1",
                      List.of(
                          new RelationshipLinkage<>(
                              identifier(COMMENTS, "c1", Meta.of(Map.of(PINNED, true))),
                              Map.of(PINNED, true)))))),
          new DomainReadScenario(
              "binds renamed RelationshipLinkage identifier meta from the wire name",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      null,
                      rels(
                          AUTHOR,
                          toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR)))))),
              RelationshipLinkageContainerFixtures.RenamedRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new RelationshipLinkageContainerFixtures.RenamedRelationshipLinkageArticle(
                      "1",
                      new RelationshipLinkage<>(
                          identifier(PEOPLE, "p1", Meta.of(Map.of(ROLE, EDITOR))),
                          new AuthorIdMeta(EDITOR))))),
          new DomainReadScenario(
              "binds identifier lid with identifier meta and drops additional members",
              DomainReadInput.single(
                  resource(
                      ARTICLES,
                      "1",
                      attrs(TITLE, HELLO),
                      rels(
                          AUTHOR,
                          Relationship.withData(
                              new RelationshipData.SingleLinkage(
                                  identifier(
                                      PEOPLE,
                                      null,
                                      "lid-1",
                                      Meta.of(Map.of(ROLE, EDITOR)),
                                      Map.of("ext:href", "https://example.test/p1"))))))),
              FlatRelationshipLinkageArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new FlatRelationshipLinkageArticle(
                      "1",
                      HELLO,
                      new RelationshipLinkage<>(
                          identifier(
                              PEOPLE, null, "lid-1", Meta.of(Map.of(ROLE, EDITOR)), Map.of()),
                          new AuthorIdMeta(EDITOR)),
                      null,
                      null,
                      null))),
          new DomainReadScenario(
              "empty map meta binds an empty map",
              DomainReadInput.single(resourceWithMeta(null, null, Meta.empty())),
              ArticleWithMapMeta.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(new ArticleWithMapMeta("1", null, null, Map.of(), null))),
          new DomainReadScenario(
              "populated map meta binds resource and relationship members",
              DomainReadInput.single(
                  resourceWithMeta(
                      attrs(TITLE, HELLO),
                      relsWithMeta(toOne(PEOPLE, "p1"), Meta.of(Map.of(DISPLAY_NAME, ALICE))),
                      Meta.of(Map.of(SOURCE, "cms")))),
              ArticleWithMapMeta.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new ArticleWithMapMeta(
                      "1",
                      HELLO,
                      ResourceIdentifier.of(PEOPLE, "p1"),
                      Map.of(SOURCE, "cms"),
                      Map.of(DISPLAY_NAME, ALICE)))),
          new DomainReadScenario(
              "Optional-wrapped bean meta binds present Optional",
              DomainReadInput.single(
                  resourceWithMeta(
                      attrs(TITLE, HELLO), null, Meta.of(Map.of(SOURCE, "cms", "note", "n")))),
              ArticleWithOptionalMeta.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.bound(
                  new ArticleWithOptionalMeta(
                      "1",
                      HELLO,
                      null,
                      Optional.of(new ArticleMeta("cms", "n")),
                      Optional.empty()))),
          new DomainReadScenario(
              "scalar whole-meta target is INVALID_META_TARGET",
              DomainReadInput.single(resource(ARTICLES, "1", null, null)),
              WholeMetaTargetFixtures.ScalarMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.INVALID_META_TARGET, META_PATH)),
          new DomainReadScenario(
              "UUID whole-meta target is INVALID_META_TARGET",
              DomainReadInput.single(resource(ARTICLES, "1", null, null)),
              WholeMetaTargetFixtures.UuidMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.INVALID_META_TARGET, META_PATH)),
          new DomainReadScenario(
              "java.time whole-meta target is INVALID_META_TARGET",
              DomainReadInput.single(resource(ARTICLES, "1", null, null)),
              WholeMetaTargetFixtures.InstantMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.INVALID_META_TARGET, META_PATH)),
          new DomainReadScenario(
              "URI whole-meta target is INVALID_META_TARGET",
              DomainReadInput.single(resource(ARTICLES, "1", null, null)),
              WholeMetaTargetFixtures.UriMetaArticle.class,
              ConverterBehavior.DEFAULT_CONVERT_VALUE,
              DomainReadExpectation.failure(MappingDiagnostic.INVALID_META_TARGET, META_PATH)),
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

  private static ResourceObject resourceWithMeta(
      @Nullable Map<String, @Nullable Object> attributes,
      @Nullable Map<String, Relationship> relationships,
      @Nullable Meta meta) {
    return new ResourceObject(
        ARTICLES,
        "1",
        null,
        attributes == null ? null : Attributes.ofAttributes(attributes),
        relationships == null
            ? null
            : Relationships.ofRelationships(copyRelationships(relationships)),
        null,
        meta,
        Map.of());
  }

  private static Map<String, Relationship> relsWithMeta(Relationship relationship, Meta meta) {
    Map<String, Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR, new Relationship(relationship.data(), relationship.links(), meta, Map.of()));
    return relationships;
  }

  private static Map<String, Relationship> relsOnlyMeta(Meta meta) {
    Map<String, Relationship> relationships = new LinkedHashMap<>();
    relationships.put(AUTHOR, Relationship.metaOnly(meta));
    return relationships;
  }

  private static ResourceObject resourceWithLid(
      @Nullable String lid, Map<String, @Nullable Object> attributes) {
    return new ResourceObject(
        ARTICLES, null, lid, Attributes.ofAttributes(attributes), null, null, null, Map.of());
  }

  private static Map<String, @Nullable Relationship> copyRelationships(
      Map<String, Relationship> relationships) {
    return new LinkedHashMap<>(relationships);
  }

  private static Relationship toOne(String type, String id) {
    return Relationship.withData(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of(type, id)));
  }

  private static Relationship toOneWithIdentifierMeta(String type, String id, Meta identifierMeta) {
    return new Relationship(
        new RelationshipData.SingleLinkage(identifier(type, id, identifierMeta)),
        null,
        null,
        Map.of());
  }

  private static Relationship toOneWithBothMeta(
      String type, String id, Meta identifierMeta, Meta relationshipMeta) {
    return new Relationship(
        new RelationshipData.SingleLinkage(identifier(type, id, identifierMeta)),
        null,
        relationshipMeta,
        Map.of());
  }

  private static Relationship toManyWithIdentifierMetas(
      String type, List<String> ids, List<@Nullable Meta> metas) {
    List<ResourceIdentifier> identifiers = new ArrayList<>(ids.size());
    for (int i = 0; i < ids.size(); i++) {
      identifiers.add(identifier(type, ids.get(i), metas.get(i)));
    }
    return Relationship.withData(new RelationshipData.IdentifierCollectionLinkage(identifiers));
  }

  private static Relationship toManyWithBothMeta(
      String type, List<String> ids, List<@Nullable Meta> identifierMetas, Meta relationshipMeta) {
    List<ResourceIdentifier> identifiers = new ArrayList<>(ids.size());
    for (int i = 0; i < ids.size(); i++) {
      identifiers.add(identifier(type, ids.get(i), identifierMetas.get(i)));
    }
    return new Relationship(
        new RelationshipData.IdentifierCollectionLinkage(identifiers),
        null,
        relationshipMeta,
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

  @SafeVarargs
  private static <T> List<@Nullable T> nullableList(@Nullable T... values) {
    List<@Nullable T> list = new ArrayList<>(values.length);
    Collections.addAll(list, values);
    return list;
  }

  private static Relationship toMany(String type, List<String> ids) {
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

  private static Map<String, @Nullable Object> nullableAttr(String name) {
    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    attributes.put(name, null);
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
