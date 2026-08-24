package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import io.github.kazemek.jsonapi.jackson.IncludePath;
import io.github.kazemek.jsonapi.jackson.IncludePolicy;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.RelationshipAllowance;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.AccessCountingArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.ConflictArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.CyclicNode;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.DeepNode;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.LinkedArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.ModeratedComment;
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.PolymorphicArticle;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Tag;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * The shared compound-inclusion write catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the inclusion surface grows, and adapter
 * suites pick them up through {@link #all()}. Consumers dispatch on the {@link
 * CompoundWriteRequest} variant, never on a scenario id.
 */
public final class CompoundWriteScenarios {

  private static final int DEFAULT_MAX_DEPTH = 10;

  private static final int DEFAULT_MAX_INCLUDED = 100;

  private static final String AUTHOR = "author";
  private static final String COMMENTS = "comments";
  private static final String COMMENTS_AUTHOR = "comments.author";
  private static final String PEOPLE = "people";
  private static final String ARTICLES = "articles";
  private static final String NODES = "nodes";
  private static final String RELATED = "related";
  private static final String REVIEWER = "reviewer";
  private static final String UNKNOWN = "unknown";
  private static final String CHILD_CHILD = "child.child";
  private static final String CHILD_CHILD_CHILD = "child.child.child";
  private static final String COMMENTS_BOGUS = "comments.bogus";
  private static final String TITLE = "Title";
  private static final String BODY = "Body";

  private static final IncludePolicy NESTED_COMMENTS_AUTHOR =
      IncludePolicy.allowing(
          Set.of(
              RelationshipAllowance.of(ARTICLES, COMMENTS),
              RelationshipAllowance.of(COMMENTS, AUTHOR)));

  private static final IncludePolicy ARTICLES_OWNED_RELATIONSHIPS_ONLY =
      IncludePolicy.allowing(
          Set.of(
              RelationshipAllowance.of(ARTICLES, COMMENTS),
              RelationshipAllowance.of(ARTICLES, AUTHOR)));

  private static final IncludePolicy ARTICLES_COMMENTS_ONLY =
      IncludePolicy.allowing(Set.of(RelationshipAllowance.of(ARTICLES, COMMENTS)));

  private static final List<CompoundWriteScenario> SCENARIOS =
      List.of(
          new CompoundWriteScenario(
              "context-free overloads omit included",
              CompoundWriteRequest.contextFree(CompoundWriteScenarios::article),
              CompoundWriteExpectation.omitted()),
          document(
              "empty include path list omits included",
              CompoundWriteScenarios::article,
              IncludePolicy.denyAll(),
              CompoundWriteExpectation.omitted()),
          document(
              "includes nested intermediates for comments.author",
              CompoundWriteScenarios::article,
              CompoundWriteExpectation.included(
                  ref(COMMENTS, "5"), ref(COMMENTS, "12"), ref(PEOPLE, "2"), ref(PEOPLE, "9")),
              COMMENTS_AUTHOR),
          collection(
              "shared identity is included once",
              () -> List.of(article("1", "A", "B"), article("2", "C", "D")),
              CompoundWriteExpectation.included(ref(PEOPLE, "9")),
              AUTHOR),
          document(
              "empty resolution emits included empty array",
              () -> new Article("1", "T", "B", List.of(), dan()),
              CompoundWriteExpectation.emptyIncluded(),
              COMMENTS),
          collection(
              "self-reference primary is not re-emitted in included",
              CompoundWriteScenarios::linkedArticles,
              CompoundWriteExpectation.emptyIncluded(),
              RELATED),
          document(
              "prefix-overlapping paths traverse suffixes",
              CompoundWriteScenarios::article,
              CompoundWriteExpectation.included(
                  ref(COMMENTS, "5"), ref(COMMENTS, "12"), ref(PEOPLE, "2"), ref(PEOPLE, "9")),
              COMMENTS,
              COMMENTS_AUTHOR),
          document(
              "converging different-suffix paths still traverse",
              CompoundWriteScenarios::article,
              CompoundWriteExpectation.included(
                  ref(COMMENTS, "5"), ref(COMMENTS, "12"), ref(PEOPLE, "2"), ref(PEOPLE, "9")),
              COMMENTS_AUTHOR,
              AUTHOR),
          document(
              "conflicting representations fail",
              () -> new ConflictArticle("10", new Person("1", "Alice"), new Person("1", "Bob")),
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION),
              AUTHOR,
              REVIEWER),
          document(
              "off-path relationships are not read for inclusion traversal",
              () -> new AccessCountingArticle("1", dan(), List.of(comment5())),
              CompoundWriteExpectation.includedWithOffPathDelta(
                  List.of(ref(PEOPLE, "9")), COMMENTS),
              AUTHOR),
          collection(
              "heterogeneous collection fails on later type",
              () -> List.of(article(), new Tag("java")),
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INVALID_INCLUDE_PATH, null, Tag.class),
              AUTHOR),
          collection(
              "one-shot iterable is materialized once",
              () ->
                  onceIterable(
                      new Article("1", "A", "B", List.of(), dan()),
                      new Article("2", "C", "D", List.of(), ezra())),
              CompoundWriteExpectation.included(ref(PEOPLE, "9"), ref(PEOPLE, "2")),
              AUTHOR),
          document(
              "nested policy matches owner resource type",
              CompoundWriteScenarios::article,
              NESTED_COMMENTS_AUTHOR,
              CompoundWriteExpectation.included(
                  ref(COMMENTS, "5"), ref(COMMENTS, "12"), ref(PEOPLE, "2"), ref(PEOPLE, "9")),
              COMMENTS_AUTHOR),
          document(
              "nested policy denies wrong owner type",
              CompoundWriteScenarios::article,
              ARTICLES_OWNED_RELATIONSHIPS_ONLY,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE, null, Comment.class),
              COMMENTS_AUTHOR),
          document(
              "maxDepth zero rejects non-empty path",
              CompoundWriteScenarios::article,
              IncludePolicy.allowAll(),
              0,
              DEFAULT_MAX_INCLUDED,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED, null, Article.class),
              AUTHOR),
          document(
              "path longer than maxDepth fails",
              CompoundWriteScenarios::article,
              IncludePolicy.allowAll(),
              1,
              DEFAULT_MAX_INCLUDED,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED, null, Article.class),
              COMMENTS_AUTHOR),
          document(
              "maxIncluded zero fails on first included resource",
              CompoundWriteScenarios::article,
              IncludePolicy.allowAll(),
              DEFAULT_MAX_DEPTH,
              0,
              CompoundWriteExpectation.failure(MappingDiagnostic.INCLUDE_COUNT_EXCEEDED),
              AUTHOR),
          document(
              "maxIncluded exceeded fails",
              CompoundWriteScenarios::article,
              IncludePolicy.allowAll(),
              DEFAULT_MAX_DEPTH,
              2,
              CompoundWriteExpectation.failure(MappingDiagnostic.INCLUDE_COUNT_EXCEEDED),
              COMMENTS_AUTHOR),
          document(
              "mapper-time unknown relationship fails",
              CompoundWriteScenarios::article,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INVALID_INCLUDE_PATH, null, Article.class),
              UNKNOWN),
          document(
              "denied relationship fails before traversal",
              CompoundWriteScenarios::article,
              IncludePolicy.denyAll(),
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE, null, Article.class),
              AUTHOR),
          document(
              "multi-failure precedence is depth then mapping then policy in path order",
              CompoundWriteScenarios::article,
              IncludePolicy.allowAll(),
              1,
              DEFAULT_MAX_INCLUDED,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED, null, Article.class),
              COMMENTS_AUTHOR,
              UNKNOWN),
          document(
              "multi-failure mapping beats policy on the same segment",
              CompoundWriteScenarios::article,
              IncludePolicy.denyAll(),
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INVALID_INCLUDE_PATH, null, Article.class),
              UNKNOWN),
          document(
              "multi-failure request-list order prefers first path mapping over later policy",
              CompoundWriteScenarios::article,
              IncludePolicy.denyAll(),
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INVALID_INCLUDE_PATH, null, Article.class),
              UNKNOWN,
              AUTHOR),
          document(
              "multi-failure nested segment mapping beats later-segment policy",
              CompoundWriteScenarios::article,
              ARTICLES_COMMENTS_ONLY,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.INVALID_INCLUDE_PATH, null, Comment.class),
              COMMENTS_BOGUS),
          document(
              "runtime nested owner type re-checks include policy",
              CompoundWriteScenarios::polymorphicArticle,
              NESTED_COMMENTS_AUTHOR,
              CompoundWriteExpectation.failure(
                  MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE, null, ModeratedComment.class),
              COMMENTS_AUTHOR),
          collection(
              "empty primary collection still enforces maxDepth",
              // Fresh list per call: List.of() returns a shared empty instance and would
              // break the per-invocation freshness invariant.
              ArrayList::new,
              IncludePolicy.allowAll(),
              0,
              CompoundWriteExpectation.failure(MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED),
              AUTHOR),
          document(
              "cyclic graph with repeated segment path terminates",
              CompoundWriteScenarios::cyclicRoot,
              CompoundWriteExpectation.included(ref(NODES, "2")),
              CHILD_CHILD_CHILD),
          collection(
              "multi-primary multi-path first-discovery order",
              CompoundWriteScenarios::multiPrimaryArticles,
              CompoundWriteExpectation.included(
                  ref(PEOPLE, "9"), ref(COMMENTS, "5"), ref(PEOPLE, "2"), ref(COMMENTS, "12")),
              AUTHOR,
              COMMENTS),
          document(
              "deep nested path includes the chain",
              CompoundWriteScenarios::deepRoot,
              CompoundWriteExpectation.included(ref(NODES, "2"), ref(NODES, "3")),
              CHILD_CHILD),
          new CompoundWriteScenario(
              "concurrent compound mappings isolate included sets",
              CompoundWriteRequest.concurrent(
                  side(() -> new Article("1", "T", "B", List.of(), dan()), AUTHOR),
                  side(
                      () -> new Article("2", "T", "B", List.of(comment5()), null),
                      COMMENTS_AUTHOR)),
              CompoundWriteExpectation.concurrentIsolation(
                  CompoundWriteExpectation.included(ref(PEOPLE, "9")),
                  CompoundWriteExpectation.included(ref(COMMENTS, "5"), ref(PEOPLE, "2")))));

  private static final FixtureCatalog<CompoundWriteScenario> CATALOG =
      FixtureCatalog.of("compound-write", SCENARIOS);

  private CompoundWriteScenarios() {}

  public static FixtureCatalog<CompoundWriteScenario> catalog() {
    return CATALOG;
  }

  private static CompoundWriteScenario document(
      String id,
      Supplier<@Nullable Object> supplier,
      CompoundWriteExpectation expectation,
      String... dottedPaths) {
    return document(
        id,
        supplier,
        IncludePolicy.allowAll(),
        DEFAULT_MAX_DEPTH,
        DEFAULT_MAX_INCLUDED,
        expectation,
        dottedPaths);
  }

  private static CompoundWriteScenario document(
      String id,
      Supplier<@Nullable Object> supplier,
      IncludePolicy policy,
      CompoundWriteExpectation expectation,
      String... dottedPaths) {
    return document(
        id, supplier, policy, DEFAULT_MAX_DEPTH, DEFAULT_MAX_INCLUDED, expectation, dottedPaths);
  }

  private static CompoundWriteScenario document(
      String id,
      Supplier<@Nullable Object> supplier,
      IncludePolicy policy,
      int maxDepth,
      int maxIncluded,
      CompoundWriteExpectation expectation,
      String... dottedPaths) {
    return new CompoundWriteScenario(
        id,
        CompoundWriteRequest.document(supplier, paths(dottedPaths), policy, maxDepth, maxIncluded),
        expectation);
  }

  private static CompoundWriteScenario collection(
      String id,
      Supplier<Iterable<?>> supplier,
      CompoundWriteExpectation expectation,
      String... dottedPaths) {
    return collection(
        id, supplier, IncludePolicy.allowAll(), DEFAULT_MAX_DEPTH, expectation, dottedPaths);
  }

  private static CompoundWriteScenario collection(
      String id,
      Supplier<Iterable<?>> supplier,
      IncludePolicy policy,
      int maxDepth,
      CompoundWriteExpectation expectation,
      String... dottedPaths) {
    return new CompoundWriteScenario(
        id,
        CompoundWriteRequest.collection(
            supplier, paths(dottedPaths), policy, maxDepth, DEFAULT_MAX_INCLUDED),
        expectation);
  }

  private static CompoundWriteSide side(Supplier<Object> supplier, String... dottedPaths) {
    return new CompoundWriteSide(
        supplier,
        paths(dottedPaths),
        IncludePolicy.allowAll(),
        DEFAULT_MAX_DEPTH,
        DEFAULT_MAX_INCLUDED);
  }

  private static List<IncludePath> paths(String... dotted) {
    List<IncludePath> result = new ArrayList<>(dotted.length);
    for (String path : dotted) {
      result.add(IncludePath.of(path));
    }
    return result;
  }

  private static IncludedResourceRef ref(String type, String id) {
    return IncludedResourceRef.of(type, id);
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
    return new Article("1", TITLE, BODY, List.of(comment5(), comment12()), dan());
  }

  private static Article article(String id, String title, String body) {
    return new Article(id, title, body, List.of(), dan());
  }

  private static List<LinkedArticle> linkedArticles() {
    LinkedArticle a1 = new LinkedArticle("1", null);
    return List.of(a1, new LinkedArticle("2", a1));
  }

  private static PolymorphicArticle polymorphicArticle() {
    return new PolymorphicArticle("1", TITLE, List.of(new ModeratedComment("5", "First!", ezra())));
  }

  private static CyclicNode cyclicRoot() {
    CyclicNode n1 = new CyclicNode("1", "a");
    CyclicNode n2 = new CyclicNode("2", "b");
    n1.setChild(n2);
    n2.setChild(n1);
    return n1;
  }

  private static List<Article> multiPrimaryArticles() {
    return List.of(
        new Article("1", "A", "B", List.of(comment5()), dan()),
        new Article("2", "C", "D", List.of(comment12()), ezra()));
  }

  private static DeepNode deepRoot() {
    DeepNode leaf = new DeepNode("3", "leaf", null);
    DeepNode mid = new DeepNode("2", "mid", leaf);
    return new DeepNode("1", "root", mid);
  }

  private static Iterable<Object> onceIterable(Object... elements) {
    return new OnceIterable(List.of(elements));
  }

  private static final class OnceIterable implements Iterable<Object> {
    private final List<Object> elements;
    private boolean consumed;

    private OnceIterable(List<Object> elements) {
      this.elements = List.copyOf(elements);
    }

    @Override
    public Iterator<Object> iterator() {
      if (consumed) {
        throw new IllegalStateException("iterable already consumed");
      }
      consumed = true;
      return elements.iterator();
    }
  }
}
