package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.representation.CompoundSerializationContext;
import io.github.kazemek.jsonapi.jackson.representation.FieldAllowance;
import io.github.kazemek.jsonapi.jackson.representation.FieldPolicy;
import io.github.kazemek.jsonapi.jackson.representation.IncludePath;
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.BlogWithJsonProperty;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import io.github.kazemek.jsonapi.testsupport.fixtures.sparsefieldset.AccessCountingFieldsetArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.sparsefieldset.ArticleWithRenamedAuthor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * The shared sparse-fieldset write catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the fieldset surface grows, and adapter
 * suites pick them up through {@link #catalog()}. Consumers dispatch on the {@link
 * SparseFieldsetOperation}/{@link SparseFieldsetRequest} descriptor, never on a scenario id.
 */
public final class SparseFieldsetScenarios {

  private static final String ARTICLES = "articles";
  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";
  private static final String BLOGS = "blogs";
  private static final String TITLE = "title";
  private static final String BODY_TEXT = "body-text";
  private static final String BODY = "body";
  private static final String AUTHOR = "author";
  private static final String WRITTEN_BY = "written-by";
  private static final String NAME = "name";
  private static final String BLOG_TITLE = "blog_title";
  private static final String TITLE_VALUE = "Title";
  private static final String BODY_VALUE = "Body";

  private static final FieldPolicy TITLE_ONLY_ALLOWANCE =
      FieldPolicy.allowing(Set.of(FieldAllowance.of(ARTICLES, TITLE)));

  private static final List<SparseFieldsetScenario> SCENARIOS =
      List.of(
          mappedDocument(
              "unrestricted MappedDocument matches Phase 2.2 attributes and relationships",
              SparseFieldsetScenarios::article,
              CompoundSerializationContext.defaults(),
              SparseFieldsetExpectation.mapped(unrestrictedArticle(), null, false)),
          unmappedDocument(
              "three-argument toDocument with empty fieldset map remains Phase 2.3 equivalent",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of(AUTHOR), Map.of()),
              SparseFieldsetExpectation.unmapped(
                  unrestrictedArticle(), List.of(unrestrictedPerson(dan())))),
          mappedDocument(
              "present empty list selects no attributes or relationships",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of())),
              SparseFieldsetExpectation.mapped(
                  FieldsetResourceState.identity(ARTICLES, "1"), null, false)),
          mappedDocument(
              "present empty list for included type selects no attributes or relationships",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of(AUTHOR), Map.of(PEOPLE, List.of())),
              SparseFieldsetExpectation.mapped(
                  unrestrictedArticle(),
                  List.of(FieldsetResourceState.identity(PEOPLE, "9")),
                  false)),
          mappedDocument(
              "present empty list with denyAll succeeds without DENIED_FIELDSET_FIELD",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of())).withFieldPolicy(FieldPolicy.denyAll()),
              SparseFieldsetExpectation.mapped(
                  FieldsetResourceState.identity(ARTICLES, "1"), null, false)),
          unmappedDocument(
              "three-argument toDocument rejects non-empty fieldsets",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT)),
          unmappedCollection(
              () -> List.of(article()),
              fieldsets(Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT)),
          mappedDocument(
              "attribute-only fieldset via toMappedDocument",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mapped(titleOnlyArticle(), null, false)),
          mappedCollection(
              () -> List.of(article()),
              fieldsets(Map.of(ARTICLES, List.of(AUTHOR))),
              SparseFieldsetExpectation.mapped(authorOnlyArticle(), null, false)),
          mappedDocument(
              "renamed JsonProperty fieldset names use final JSON:API names",
              () -> new BlogWithJsonProperty("b1", "Hello"),
              fieldsets(Map.of(BLOGS, List.of(BLOG_TITLE))),
              SparseFieldsetExpectation.mapped(
                  FieldsetResourceState.of(
                      BLOGS,
                      "b1",
                      List.of(BLOG_TITLE),
                      Map.of(BLOG_TITLE, "Hello"),
                      null,
                      Map.of()),
                  null,
                  false)),
          mappedDocument(
              "renamed JsonApiAttribute fieldset uses body-text",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(BODY_TEXT))),
              SparseFieldsetExpectation.mapped(
                  FieldsetResourceState.of(
                      ARTICLES,
                      "1",
                      List.of(BODY_TEXT),
                      Map.of(BODY_TEXT, BODY_VALUE),
                      null,
                      Map.of()),
                  null,
                  false)),
          mappedDocument(
              "renamed JsonApiRelationship fieldset uses written-by",
              () -> new ArticleWithRenamedAuthor("1", TITLE_VALUE, dan()),
              fieldsets(Map.of(ARTICLES, List.of(WRITTEN_BY))),
              SparseFieldsetExpectation.mapped(writtenByArticle(), null, false)),
          mappedDocument(
              "unknown JsonApiRelationship rename fails against Java property name",
              () -> new ArticleWithRenamedAuthor("1", TITLE_VALUE, dan()),
              fieldsets(Map.of(ARTICLES, List.of(AUTHOR))),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.INVALID_FIELDSET_FIELD, null, ArticleWithRenamedAuthor.class)),
          mappedDocument(
              "per-type fieldsets do not strip unrelated included types",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mapped(
                  titleOnlyArticle(), List.of(unrestrictedPerson(dan())), true)),
          mappedDocument(
              "include author with fields articles title omits linkage and sets exception",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mapped(
                  titleOnlyArticle(), List.of(unrestrictedPerson(dan())), true)),
          mappedDocument(
              "nested include comments.author with fields comments body",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of("comments.author"), Map.of(COMMENTS, List.of(BODY))),
              SparseFieldsetExpectation.mapped(
                  unrestrictedArticle(),
                  List.of(
                      bodyOnlyComment(comment5()),
                      bodyOnlyComment(comment12()),
                      unrestrictedPerson(ezra()),
                      unrestrictedPerson(dan())),
                  true)),
          mappedDocument(
              "attribute-only omission with fully linked includes keeps exception false",
              SparseFieldsetScenarios::article,
              includeAndFields(List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE, AUTHOR, COMMENTS))),
              SparseFieldsetExpectation.mapped(
                  titleWithRelationshipsArticle(), List.of(unrestrictedPerson(dan())), false)),
          mappedDocument(
              "access counting proves linkage vs traversal split",
              () ->
                  new AccessCountingFieldsetArticle(
                      "1", TITLE_VALUE, BODY_VALUE, dan(), List.of(comment5())),
              includeAndFields(List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mappedWithZeroReads(
                  titleOnlyArticle(),
                  List.of(unrestrictedPerson(dan())),
                  true,
                  new ZeroReadGuarantee(Set.of(BODY), Set.of(COMMENTS)))),
          mappedDocument(
              "unknown fieldset field fails with INVALID_FIELDSET_FIELD",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of("nope"))),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.INVALID_FIELDSET_FIELD, null, Article.class)),
          mappedDocument(
              "denyAll rejects first present fieldset name",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE, AUTHOR)))
                  .withFieldPolicy(FieldPolicy.denyAll()),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.DENIED_FIELDSET_FIELD, null, Article.class)),
          mappedDocument(
              "missing FieldAllowance denies with DENIED_FIELDSET_FIELD",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE, AUTHOR)))
                  .withFieldPolicy(TITLE_ONLY_ALLOWANCE),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.DENIED_FIELDSET_FIELD, null, Article.class)),
          mappedDocument(
              "unmapped name wins over policy denial",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of("nope", TITLE)))
                  .withFieldPolicy(FieldPolicy.denyAll()),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.INVALID_FIELDSET_FIELD, null, Article.class)),
          mappedDocument(
              "unused fieldset type keys are ignored",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of("tags", List.of(NAME), ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mapped(titleOnlyArticle(), null, false)),
          mappedDocument(
              "duplicate-free multi-field fieldset keeps title and author",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE, AUTHOR))),
              SparseFieldsetExpectation.mapped(titleAndAuthorArticle(), null, false)),
          mappedDocument(
              "FieldAllowance-satisfied fieldset succeeds",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE))).withFieldPolicy(TITLE_ONLY_ALLOWANCE),
              SparseFieldsetExpectation.mapped(titleOnlyArticle(), null, false)),
          mappedDocument(
              "FieldAllowance denies a present field not in the allowance set",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(AUTHOR))).withFieldPolicy(TITLE_ONLY_ALLOWANCE),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.DENIED_FIELDSET_FIELD, null, Article.class)),
          new SparseFieldsetScenario(
              "identity preserved under every fieldset shape",
              SparseFieldsetOperation.TO_MAPPED_DOCUMENT,
              SparseFieldsetRequest.identityPreservation(
                  SparseFieldsetScenarios::article,
                  List.of(
                      fieldsets(Map.of()),
                      fieldsets(Map.of(ARTICLES, List.of())),
                      fieldsets(Map.of(ARTICLES, List.of(TITLE))),
                      fieldsets(Map.of(ARTICLES, List.of(AUTHOR))))),
              SparseFieldsetExpectation.identity(ARTICLES, "1")),
          mappedDocument(
              "surviving fields keep mapping definition order",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(AUTHOR, BODY_TEXT, TITLE))),
              SparseFieldsetExpectation.mapped(titleBodyTextAuthorArticle(), null, false)),
          new SparseFieldsetScenario(
              "concurrent fieldset mappings isolate documents and linkage exemptions",
              SparseFieldsetOperation.TO_MAPPED_DOCUMENT,
              SparseFieldsetRequest.concurrent(
                  new SparseFieldsetSide(
                      SparseFieldsetScenarios::article,
                      includeAndFields(List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE)))),
                  new SparseFieldsetSide(
                      SparseFieldsetScenarios::article,
                      includeAndFields(
                          List.of(AUTHOR), Map.of(ARTICLES, List.of(TITLE, AUTHOR, COMMENTS))))),
              SparseFieldsetExpectation.concurrentIsolation(
                  SparseFieldsetExpectation.mapped(
                      titleOnlyArticle(), List.of(unrestrictedPerson(dan())), true),
                  SparseFieldsetExpectation.mapped(
                      titleWithRelationshipsArticle(), List.of(unrestrictedPerson(dan())), false))),
          mappedDocument(
              "full field list keeps the unrestricted resource state",
              SparseFieldsetScenarios::article,
              fieldsets(Map.of(ARTICLES, List.of(TITLE, AUTHOR, COMMENTS, BODY_TEXT))),
              SparseFieldsetExpectation.mapped(unrestrictedArticle(), null, false)),
          mappedDocument(
              "empty fieldset still emits resource meta",
              SparseFieldsetScenarios::articleWithMeta,
              fieldsets(Map.of(ARTICLES, List.of())),
              SparseFieldsetExpectation.mapped(emptyFieldsetArticleWithMeta(), null, false)),
          mappedDocument(
              "title-only fieldset keeps resource meta and omits the excluded relationship",
              SparseFieldsetScenarios::articleWithMeta,
              fieldsets(Map.of(ARTICLES, List.of(TITLE))),
              SparseFieldsetExpectation.mapped(titleOnlyArticleWithMeta(), null, false)),
          mappedDocument(
              "meta is not a valid sparse-fieldset field name",
              SparseFieldsetScenarios::articleWithMeta,
              fieldsets(Map.of(ARTICLES, List.of("meta"))),
              SparseFieldsetExpectation.failure(
                  MappingDiagnostic.INVALID_FIELDSET_FIELD, null, ArticleWithMeta.class)));

  private static final FixtureCatalog<SparseFieldsetScenario> CATALOG =
      FixtureCatalog.of("sparse-fieldset", SCENARIOS);

  private SparseFieldsetScenarios() {}

  public static FixtureCatalog<SparseFieldsetScenario> catalog() {
    return CATALOG;
  }

  private static SparseFieldsetScenario mappedDocument(
      String id,
      Supplier<@Nullable Object> supplier,
      CompoundSerializationContext context,
      SparseFieldsetExpectation expectation) {
    return new SparseFieldsetScenario(
        id,
        SparseFieldsetOperation.TO_MAPPED_DOCUMENT,
        SparseFieldsetRequest.single(supplier, context),
        expectation);
  }

  private static SparseFieldsetScenario mappedCollection(
      Supplier<Iterable<?>> supplier,
      CompoundSerializationContext context,
      SparseFieldsetExpectation expectation) {
    return new SparseFieldsetScenario(
        "relationship-only fieldset via toMappedResourceCollection",
        SparseFieldsetOperation.TO_MAPPED_RESOURCE_COLLECTION,
        SparseFieldsetRequest.collection(supplier, context),
        expectation);
  }

  private static SparseFieldsetScenario unmappedDocument(
      String id,
      Supplier<@Nullable Object> supplier,
      CompoundSerializationContext context,
      SparseFieldsetExpectation expectation) {
    return new SparseFieldsetScenario(
        id,
        SparseFieldsetOperation.TO_DOCUMENT,
        SparseFieldsetRequest.single(supplier, context),
        expectation);
  }

  private static SparseFieldsetScenario unmappedCollection(
      Supplier<Iterable<?>> supplier,
      CompoundSerializationContext context,
      SparseFieldsetExpectation expectation) {
    return new SparseFieldsetScenario(
        "three-argument toResourceCollection rejects non-empty fieldsets",
        SparseFieldsetOperation.TO_RESOURCE_COLLECTION,
        SparseFieldsetRequest.collection(supplier, context),
        expectation);
  }

  private static CompoundSerializationContext fieldsets(Map<String, List<String>> fields) {
    return CompoundSerializationContext.defaults().withFieldsets(fields);
  }

  private static CompoundSerializationContext includeAndFields(
      List<String> paths, Map<String, List<String>> fields) {
    List<IncludePath> includePaths = new ArrayList<>(paths.size());
    for (String path : paths) {
      includePaths.add(IncludePath.of(path));
    }
    return CompoundSerializationContext.defaults()
        .withIncludePaths(includePaths)
        .withIncludePolicy(IncludePolicy.allowAll())
        .withFieldsets(fields);
  }

  private static Person dan() {
    return new Person("9", "Dan");
  }

  private static Person ezra() {
    return new Person("2", "Ezra");
  }

  private static Comment comment5() {
    return new Comment("5", "First!", ezra());
  }

  private static Comment comment12() {
    return new Comment("12", "I like XML better", dan());
  }

  private static Article article() {
    return new Article("1", TITLE_VALUE, BODY_VALUE, List.of(comment5(), comment12()), dan());
  }

  private static ArticleWithMeta articleWithMeta() {
    return new ArticleWithMeta(
        "1",
        "T",
        ResourceIdentifier.of(PEOPLE, "p1"),
        new ArticleMeta("cms", "n"),
        new AuthorMeta("Alice"));
  }

  private static FieldsetResourceState unrestrictedArticle() {
    return FieldsetResourceState.of(
        ARTICLES,
        "1",
        List.of(TITLE, BODY_TEXT),
        articleAttributes(),
        List.of(COMMENTS, AUTHOR),
        articleLinkage());
  }

  private static FieldsetResourceState titleOnlyArticle() {
    return FieldsetResourceState.of(
        ARTICLES, "1", List.of(TITLE), Map.of(TITLE, TITLE_VALUE), null, Map.of());
  }

  private static FieldsetResourceState authorOnlyArticle() {
    return FieldsetResourceState.of(
        ARTICLES, "1", null, Map.of(), List.of(AUTHOR), Map.of(AUTHOR, personLinkage(dan())));
  }

  private static FieldsetResourceState titleAndAuthorArticle() {
    return FieldsetResourceState.of(
        ARTICLES,
        "1",
        List.of(TITLE),
        Map.of(TITLE, TITLE_VALUE),
        List.of(AUTHOR),
        Map.of(AUTHOR, personLinkage(dan())));
  }

  private static FieldsetResourceState titleBodyTextAuthorArticle() {
    return FieldsetResourceState.of(
        ARTICLES,
        "1",
        List.of(TITLE, BODY_TEXT),
        articleAttributes(),
        List.of(AUTHOR),
        Map.of(AUTHOR, personLinkage(dan())));
  }

  private static FieldsetResourceState titleWithRelationshipsArticle() {
    return FieldsetResourceState.of(
        ARTICLES,
        "1",
        List.of(TITLE),
        Map.of(TITLE, TITLE_VALUE),
        List.of(COMMENTS, AUTHOR),
        articleLinkage());
  }

  private static FieldsetResourceState writtenByArticle() {
    return FieldsetResourceState.of(
        ARTICLES,
        "1",
        null,
        Map.of(),
        List.of(WRITTEN_BY),
        Map.of(WRITTEN_BY, personLinkage(dan())));
  }

  private static FieldsetResourceState emptyFieldsetArticleWithMeta() {
    return FieldsetResourceState.of(
        ARTICLES, "1", null, Map.of(), null, Map.of(), articleResourceMeta());
  }

  private static FieldsetResourceState titleOnlyArticleWithMeta() {
    return FieldsetResourceState.of(
        ARTICLES, "1", List.of(TITLE), Map.of(TITLE, "T"), null, Map.of(), articleResourceMeta());
  }

  private static Meta articleResourceMeta() {
    Map<String, Object> members = new LinkedHashMap<>();
    members.put("source", "cms");
    members.put("note", "n");
    return Meta.of(members);
  }

  private static FieldsetResourceState unrestrictedPerson(Person person) {
    return FieldsetResourceState.of(
        PEOPLE,
        person.id(),
        List.of(NAME),
        Map.of(NAME, Objects.requireNonNull(person.name(), "person.name")),
        null,
        Map.of());
  }

  private static FieldsetResourceState bodyOnlyComment(Comment comment) {
    return FieldsetResourceState.of(
        COMMENTS,
        comment.id(),
        List.of(BODY),
        Map.of(BODY, Objects.requireNonNull(comment.body(), "comment.body")),
        null,
        Map.of());
  }

  private static Map<String, Object> articleAttributes() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(TITLE, TITLE_VALUE);
    attributes.put(BODY_TEXT, BODY_VALUE);
    return attributes;
  }

  private static Map<String, RelationshipData> articleLinkage() {
    Map<String, RelationshipData> linkage = new LinkedHashMap<>();
    linkage.put(COMMENTS, commentsLinkage());
    linkage.put(AUTHOR, personLinkage(dan()));
    return linkage;
  }

  private static RelationshipData personLinkage(Person person) {
    return new RelationshipData.SingleLinkage(
        new ResourceIdentifier(PEOPLE, person.id(), null, null, Map.of()));
  }

  private static RelationshipData commentsLinkage() {
    return new RelationshipData.IdentifierCollectionLinkage(
        List.of(
            new ResourceIdentifier(COMMENTS, "5", null, null, Map.of()),
            new ResourceIdentifier(COMMENTS, "12", null, null, Map.of())));
  }
}
