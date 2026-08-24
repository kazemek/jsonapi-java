package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite.AccessCountingArticle
import io.github.kazemek.jsonapi.testsupport.compoundwrite.CompoundWriteExpectation
import io.github.kazemek.jsonapi.testsupport.compoundwrite.CompoundWriteRequest
import io.github.kazemek.jsonapi.testsupport.compoundwrite.CompoundWriteScenario
import io.github.kazemek.jsonapi.testsupport.compoundwrite.CompoundWriteScenarios
import io.github.kazemek.jsonapi.testsupport.compoundwrite.CompoundWriteSide
import io.github.kazemek.jsonapi.testsupport.compoundwrite.IncludedResourceRef
import groovy.json.JsonSlurper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

// Shared compound-inclusion cases live in CompoundWriteScenarios. This spec runs every catalog
// entry and asserts executedScenarioIds == catalogScenarioIds so a later Jackson 2 suite can do
// the same. Adapter-local remains empty unless a Jackson-API-specific case appears; suite-local
// round-trip serialization and absolute getter-read counts stay here.
// @Stepwise pins the declared feature order so the coverage feature always runs after the
// parameterized catalog iterations (Spock does not guarantee feature order otherwise).
@Stepwise
class CompoundSerializationSpec extends Specification {

  private static final Set<String> ROUND_TRIP_IDS = Set.of(
  "includes nested intermediates for comments.author",
  "shared identity is included once",
  "self-reference primary is not re-emitted in included",
  "cyclic graph with repeated segment path terminates",
  "multi-primary multi-path first-discovery order")

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Shared
  def writer = JsonApiJackson3.writer(JsonMapper.builder().build())

  @Shared
  Set<String> executedScenarioIds = new LinkedHashSet<>()

  @Unroll
  def "compound write #scenario.id from the shared catalog"() {
    given:
    executedScenarioIds.add(scenario.id())

    when:
    def thrownException = null
    def document = null
    try {
      document = execute(scenario)
    } catch (Exception e) {
      thrownException = e
    }

    then:
    verify(scenario, document, thrownException)

    where:
    scenario << CompoundWriteScenarios.catalog().all()
  }

  def "covers every shared compound-write scenario exactly once"() {
    given:
    def catalogIds = CompoundWriteScenarios.catalog().all()*.id as Set

    expect:
    // Selective --tests runs can execute a subset of the @Unroll iterations. Assert
    // full-catalog coverage only when this spec actually ran every catalog entry.
    executedScenarioIds.size() != catalogIds.size() || executedScenarioIds == catalogIds
  }

  private JsonApiDocument execute(CompoundWriteScenario scenario) {
    def request = scenario.request()
    if (request instanceof CompoundWriteRequest.Concurrent) {
      executeConcurrent(
          request, (CompoundWriteExpectation.ConcurrentIsolation) scenario.expectation())
      return null
    }
    def expectation = scenario.expectation()
    if (expectation instanceof CompoundWriteExpectation.Success
        && expectation.expectedTraversalDelta() != null) {
      return executeWithTraversalDelta(request, (CompoundWriteExpectation.Success) expectation)
    }
    return invoke(request)
  }

  private JsonApiDocument invoke(CompoundWriteRequest request) {
    if (request instanceof CompoundWriteRequest.ContextFree) {
      return mapper.toDocument(request.supplier().get())
    }
    if (request instanceof CompoundWriteRequest.Document) {
      return mapper.toDocument(request.supplier().get(), null, context(request))
    }
    if (request instanceof CompoundWriteRequest.Collection) {
      return mapper.toResourceCollection(request.supplier().get(), null, context(request))
    }
    throw new IllegalArgumentException("Unsupported request: " + request)
  }

  private JsonApiDocument executeWithTraversalDelta(
      CompoundWriteRequest request, CompoundWriteExpectation.Success expectation) {
    def documentRequest = (CompoundWriteRequest.Document) request
    def withInclude = documentRequest.supplier().get()
    def document = mapper.toDocument(withInclude, null, context(documentRequest))
    def baseline = documentRequest.supplier().get()
    mapper.toDocument(baseline, null, context(documentRequest).withIncludePaths(List.of()))
    def delta =
        readsFor(withInclude, expectation.offPathRelationship()) -
        readsFor(baseline, expectation.offPathRelationship())
    assert delta == expectation.expectedTraversalDelta()
    // Absolute getter-read counts remain Jackson 3 suite-local (ADR-004).
    if (withInclude instanceof AccessCountingArticle) {
      assert withInclude.authorReads == 2
      assert withInclude.commentsReads == 1
    }
    return document
  }

  private static void executeConcurrent(
      CompoundWriteRequest.Concurrent request,
      CompoundWriteExpectation.ConcurrentIsolation isolation) {
    def shared = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def latch = new CountDownLatch(2)
    def start = new CountDownLatch(1)
    def err = new AtomicReference<Throwable>()
    def pool = Executors.newFixedThreadPool(2)
    try {
      pool.submit(concurrentTask(shared, request.first(), isolation.first().included(), start, latch, err) as Runnable)
      pool.submit(concurrentTask(shared, request.second(), isolation.second().included(), start, latch, err) as Runnable)
      start.countDown()
      assert latch.await(10, TimeUnit.SECONDS)
      assert err.get() == null
    } finally {
      pool.shutdownNow()
    }
  }

  private static Closure concurrentTask(
      JsonApiResourceMapper shared,
      CompoundWriteSide side,
      List<IncludedResourceRef> expectedIncluded,
      CountDownLatch start,
      CountDownLatch latch,
      AtomicReference<Throwable> err) {
    return {
      try {
        start.await()
        200.times {
          def doc = shared.toDocument(side.supplier().get(), null, context(side))
          assertIncluded(doc, expectedIncluded)
        }
      } catch (Throwable t) {
        err.compareAndSet(null, t)
      } finally {
        latch.countDown()
      }
    }
  }

  private void verify(
      CompoundWriteScenario scenario, JsonApiDocument document, Throwable thrownException) {
    def expectation = scenario.expectation()
    if (expectation instanceof CompoundWriteExpectation.Failure) {
      assert thrownException instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) thrownException
      assert ex.diagnostic() == expectation.diagnostic()
      assert ex.propertyPath() == expectation.propertyPath()
      assert ex.resourceClass() == expectation.resourceClass()
      return
    }
    if (expectation instanceof CompoundWriteExpectation.ConcurrentIsolation) {
      assert thrownException == null
      return
    }
    assert thrownException == null
    def success = (CompoundWriteExpectation.Success) expectation
    assertIncluded(document, success.included())
    if (scenario.id() in ROUND_TRIP_IDS) {
      def json = writer.writeValueAsString(document)
      def parsed = new JsonSlurper().parseText(json)
      assert parsed.included*.type == success.included()*.type()
      assert parsed.included*.id == success.included()*.id()
    }
  }

  private static void assertIncluded(
      JsonApiDocument document, List<IncludedResourceRef> expected) {
    if (expected == null) {
      assert document.included() == null
      assert !document.hasIncludedMember()
      return
    }
    assert document.included() != null
    assert document.hasIncludedMember()
    assert document.included().size() == expected.size()
    expected.eachWithIndex { IncludedResourceRef ref, int i ->
      assert document.included()[i].type() == ref.type()
      assert document.included()[i].id() == ref.id()
    }
  }

  private static CompoundSerializationContext context(CompoundWriteRequest.Document request) {
    return CompoundSerializationContext.defaults()
        .withIncludePaths(request.includePaths())
        .withIncludePolicy(request.includePolicy())
        .withMaxDepth(request.maxDepth())
        .withMaxIncluded(request.maxIncluded())
  }

  private static CompoundSerializationContext context(CompoundWriteRequest.Collection request) {
    return CompoundSerializationContext.defaults()
        .withIncludePaths(request.includePaths())
        .withIncludePolicy(request.includePolicy())
        .withMaxDepth(request.maxDepth())
        .withMaxIncluded(request.maxIncluded())
  }

  private static CompoundSerializationContext context(CompoundWriteSide side) {
    return CompoundSerializationContext.defaults()
        .withIncludePaths(side.includePaths())
        .withIncludePolicy(side.includePolicy())
        .withMaxDepth(side.maxDepth())
        .withMaxIncluded(side.maxIncluded())
  }

  private static int readsFor(Object domain, String relationship) {
    def counting = (AccessCountingArticle) domain
    if (relationship == "comments") {
      return counting.commentsReads
    }
    if (relationship == "author") {
      return counting.authorReads
    }
    throw new IllegalArgumentException("Unknown off-path relationship: " + relationship)
  }
}
