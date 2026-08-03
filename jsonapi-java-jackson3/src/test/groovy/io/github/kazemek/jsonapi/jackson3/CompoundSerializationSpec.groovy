package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson3.testmodel.AccessCountingArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.Article
import io.github.kazemek.jsonapi.jackson3.testmodel.BaseComment
import io.github.kazemek.jsonapi.jackson3.testmodel.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.ConflictArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.CyclicNode
import io.github.kazemek.jsonapi.jackson3.testmodel.DeepNode
import io.github.kazemek.jsonapi.jackson3.testmodel.LinkedArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ModeratedComment
import io.github.kazemek.jsonapi.jackson3.testmodel.Person
import io.github.kazemek.jsonapi.jackson3.testmodel.PolymorphicArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.Tag
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class CompoundSerializationSpec extends Specification {

  def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
  def writer = JsonApiJackson3.writer(JsonMapper.builder().build())

  def dan = new Person("9", "Dan")
  def ezra = new Person("2", "Ezra")
  def comment5 = new Comment("5", "First!", ezra)
  def comment12 = new Comment("12", "I like XML better", dan)
  def article = new Article("1", "Title", "Body", [comment5, comment12], dan)

  private static CompoundSerializationContext allow(String... paths) {
    return CompoundSerializationContext.defaults()
        .withIncludePaths(paths.collect { IncludePath.of(it) })
        .withIncludePolicy(IncludePolicy.allowAll())
  }

  def "context-free overloads omit included"() {
    when:
    def doc = mapper.toDocument(article)

    then:
    doc.included() == null
    !doc.hasIncludedMember()
  }

  def "empty include path list omits included"() {
    when:
    def doc = mapper.toDocument(article, null, CompoundSerializationContext.defaults())

    then:
    doc.included() == null
  }

  def "includes nested intermediates for comments.author"() {
    when:
    def doc = mapper.toDocument(article, null, allow("comments.author"))

    then:
    doc.included()*.type() == [
      "comments",
      "comments",
      "people",
      "people"
    ]
    doc.included()*.id() == ["5", "12", "2", "9"]
    writer.writeValueAsString(doc) // round-trip validation
  }

  def "shared identity is included once"() {
    given:
    def a1 = new Article("1", "A", "B", [], dan)
    def a2 = new Article("2", "C", "D", [], dan)

    when:
    def doc = mapper.toResourceCollection([a1, a2], null, allow("author"))

    then:
    doc.included().size() == 1
    doc.included()[0].id() == "9"
    writer.writeValueAsString(doc)
  }

  def "empty resolution emits included empty array"() {
    given:
    def emptyComments = new Article("1", "T", "B", [], dan)

    when:
    def doc = mapper.toDocument(emptyComments, null, allow("comments"))

    then:
    doc.included() != null
    doc.included().isEmpty()
    doc.hasIncludedMember()
  }

  def "self-reference primary is not re-emitted in included"() {
    given:
    def a1 = new LinkedArticle("1", null)
    def a2 = new LinkedArticle("2", a1)

    when:
    def doc = mapper.toResourceCollection([a1, a2], null, allow("related"))

    then:
    doc.included() != null
    doc.included().isEmpty()
    writer.writeValueAsString(doc)
  }

  def "prefix-overlapping paths traverse suffixes"() {
    when:
    def doc = mapper.toDocument(article, null, allow("comments", "comments.author"))

    then:
    doc.included()*.id() == ["5", "12", "2", "9"]
  }

  def "converging different-suffix paths still traverse"() {
    when:
    def doc = mapper.toDocument(article, null, allow("comments.author", "author"))

    then:
    // BFS along comments.author: both comments, then authors; author path dedups dan
    doc.included()*.id() == ["5", "12", "2", "9"]
    doc.included().count { it.id() == "9" } == 1
  }

  def "conflicting representations fail"() {
    given:
    def alice = new Person("1", "Alice")
    def bob = new Person("1", "Bob")
    def conflict = new ConflictArticle("10", alice, bob)

    when:
    mapper.toDocument(conflict, null, allow("author", "reviewer"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION
  }

  def "off-path relationships are not read for inclusion traversal"() {
    given:
    def counting = new AccessCountingArticle("1", dan, [comment5])

    when:
    def doc = mapper.toDocument(counting, null, allow("author"))

    then:
    doc.included()*.id() == ["9"]
    // linkage on primary reads both; traversal follows only author
    counting.authorReads == 2
    counting.commentsReads == 1
  }

  def "heterogeneous collection fails on later type"() {
    given:
    def tag = new Tag("java")

    when:
    mapper.toResourceCollection([article, tag], null, allow("author"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == Tag
    e.propertyPath() == "author"
  }

  def "one-shot iterable is materialized once"() {
    given:
    def a1 = new Article("1", "A", "B", [], dan)
    def a2 = new Article("2", "C", "D", [], dan)
    def once = onceIterable([a1, a2])

    when:
    def doc = mapper.toResourceCollection(once, null, allow("author"))

    then:
    (doc.data() as DocumentData.ResourceCollection).resources().size() == 2
    doc.included().size() == 1
  }

  def "nested policy matches owner resource type"() {
    given:
    def policy = IncludePolicy.allowing(Set.of(
        RelationshipAllowance.of("articles", "comments"),
        RelationshipAllowance.of("comments", "author"),
        ))
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("comments.author")
        ])
        .withIncludePolicy(policy)

    when:
    def doc = mapper.toDocument(article, null, context)

    then:
    doc.included()*.id() == ["5", "12", "2", "9"]
  }

  def "nested policy denies wrong owner type"() {
    given:
    def policy = IncludePolicy.allowing(Set.of(
        RelationshipAllowance.of("articles", "comments"),
        // permit author on articles but not on comments
        RelationshipAllowance.of("articles", "author"),
        ))
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("comments.author")
        ])
        .withIncludePolicy(policy)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE
    e.resourceClass() == Comment
    e.propertyPath() == "comments.author"
  }

  def "maxDepth zero rejects non-empty path"() {
    given:
    def context = allow("author").withMaxDepth(0)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED
    e.propertyPath() == "author"
  }

  def "path longer than maxDepth fails"() {
    given:
    def context = allow("comments.author").withMaxDepth(1)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED
    e.propertyPath() == "comments.author"
  }

  def "maxIncluded zero fails on first included resource"() {
    given:
    def context = allow("author").withMaxIncluded(0)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_COUNT_EXCEEDED
  }

  def "maxIncluded exceeded fails"() {
    given:
    def context = allow("comments.author").withMaxIncluded(2)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_COUNT_EXCEEDED
  }

  def "negative limits are rejected"() {
    when:
    CompoundSerializationContext.defaults().withMaxDepth(-1)

    then:
    thrown(IllegalArgumentException)

    when:
    CompoundSerializationContext.defaults().withMaxIncluded(-1)

    then:
    thrown(IllegalArgumentException)
  }

  def "factory-time malformed paths fail with raw input"() {
    when:
    IncludePath.of(input)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == null
    e.propertyPath() == input

    where:
    input << [
      "",
      ".a",
      "a.",
      "a..b",
      " ",
      "a. .b"
    ]
  }

  def "mapper-time unknown relationship fails"() {
    when:
    mapper.toDocument(article, null, allow("unknown"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == Article
    e.propertyPath() == "unknown"
  }

  def "denied relationship fails before traversal"() {
    given:
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([IncludePath.of("author")])
        .withIncludePolicy(IncludePolicy.denyAll())

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE
    e.propertyPath() == "author"
  }

  def "multi-failure precedence is depth then mapping then policy in path order"() {
    given:
    // first path exceeds depth; second would be invalid mapping — depth wins
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("comments.author"),
          IncludePath.of("nope")
        ])
        .withIncludePolicy(IncludePolicy.allowAll())
        .withMaxDepth(1)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED
    e.propertyPath() == "comments.author"
  }

  def "multi-failure mapping beats policy on the same segment"() {
    given:
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([IncludePath.of("unknown")])
        .withIncludePolicy(IncludePolicy.denyAll())

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.propertyPath() == "unknown"
  }

  def "multi-failure request-list order prefers first path mapping over later policy"() {
    given:
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("unknown"),
          IncludePath.of("author")
        ])
        .withIncludePolicy(IncludePolicy.denyAll())

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.propertyPath() == "unknown"
  }

  def "multi-failure nested segment mapping beats later-segment policy"() {
    given:
    def policy = IncludePolicy.allowing(Set.of(
        RelationshipAllowance.of("articles", "comments"),
        ))
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("comments.bogus")
        ])
        .withIncludePolicy(policy)

    when:
    mapper.toDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == Comment
    e.propertyPath() == "comments.bogus"
  }

  def "runtime nested owner type re-checks include policy"() {
    given:
    def moderated = new ModeratedComment("5", "First!", ezra)
    def poly = new PolymorphicArticle("1", "Title", [moderated] as List<BaseComment>)
    def policy = IncludePolicy.allowing(Set.of(
        RelationshipAllowance.of("articles", "comments"),
        RelationshipAllowance.of("comments", "author"),
        ))
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("comments.author")
        ])
        .withIncludePolicy(policy)

    when:
    mapper.toDocument(poly, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE
    e.resourceClass() == ModeratedComment
    e.propertyPath() == "comments.author"
  }

  def "empty primary collection still enforces maxDepth"() {
    given:
    def context = allow("author").withMaxDepth(0)

    when:
    mapper.toResourceCollection([], null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED
    e.resourceClass() == null
    e.propertyPath() == "author"
  }

  def "cyclic graph with repeated segment path terminates"() {
    given:
    def n1 = new CyclicNode("1", "a")
    def n2 = new CyclicNode("2", "b")
    n1.setChild(n2)
    n2.setChild(n1)

    when:
    def doc = mapper.toDocument(n1, null, allow("child.child.child"))

    then:
    doc.included()*.id() == ["2"]
    writer.writeValueAsString(doc)
  }

  def "multi-primary multi-path first-discovery order"() {
    given:
    def a1 = new Article("1", "A", "B", [comment5], dan)
    def a2 = new Article("2", "C", "D", [comment12], ezra)

    when:
    def doc = mapper.toResourceCollection([a1, a2], null, allow("author", "comments"))

    then:
    // outer primary, inner path: a1.author, a1.comments, a2.author, a2.comments
    doc.included()*.id() == ["9", "5", "2", "12"]
    writer.writeValueAsString(doc)
  }

  def "deep nested path includes the chain"() {
    given:
    def leaf = new DeepNode("3", "leaf", null)
    def mid = new DeepNode("2", "mid", leaf)
    def root = new DeepNode("1", "root", mid)

    when:
    def doc = mapper.toDocument(root, null, allow("child.child"))

    then:
    doc.included()*.id() == ["2", "3"]
  }

  def "concurrent compound mappings isolate included sets"() {
    given:
    def shared = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def latch = new CountDownLatch(2)
    def start = new CountDownLatch(1)
    def err = new AtomicReference<Throwable>()
    def pool = Executors.newFixedThreadPool(2)

    def task1 = {
      try {
        start.await()
        def localDan = new Person("9", "Dan")
        def a = new Article("1", "T", "B", [], localDan)
        def doc = shared.toDocument(a, null, allow("author"))
        assert doc.included().size() == 1
        assert doc.included()[0].id() == "9"
      } catch (Throwable t) {
        err.compareAndSet(null, t)
      } finally {
        latch.countDown()
      }
    }
    def task2 = {
      try {
        start.await()
        def localEzra = new Person("2", "Ezra")
        def c = new Comment("5", "Hi", localEzra)
        def a = new Article("2", "T", "B", [c], null)
        def doc = shared.toDocument(a, null, allow("comments.author"))
        assert doc.included()*.id() == ["5", "2"]
      } catch (Throwable t) {
        err.compareAndSet(null, t)
      } finally {
        latch.countDown()
      }
    }

    when:
    pool.submit(task1 as Runnable)
    pool.submit(task2 as Runnable)
    start.countDown()
    def finished = latch.await(10, TimeUnit.SECONDS)
    pool.shutdownNow()

    then:
    finished
    err.get() == null
  }

  private static Iterable<?> onceIterable(List<?> elements) {
    return new Iterable<Object>() {
          private boolean consumed = false

          @Override
          Iterator<Object> iterator() {
            if (consumed) {
              throw new IllegalStateException("iterable already consumed")
            }
            consumed = true
            return elements.iterator() as Iterator<Object>
          }
        }
  }
}
