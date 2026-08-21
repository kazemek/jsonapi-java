package io.github.kazemek.jsonapi.testfixtures.compoundwrite

import io.github.kazemek.jsonapi.testfixtures.domainwrite.Article
import spock.lang.Specification

// Why this spec exists: CompoundWriteScenarios is the version-neutral compound-inclusion catalog
// shared by every Jackson major. Adapter suites run the whole catalog through their own mapper —
// Jackson 3 asserts executedScenarioIds == catalogScenarioIds in CompoundSerializationSpec, and
// Phase 2.19 mandates the same for Jackson 2 — so every entry must stay self-consistent. These
// tests enforce the local invariants that hold for any catalog entry regardless of catalog size:
// unique stable ids, exactly one request variant/discriminated expectation, resolvable included
// identities or known diagnostics, and the absent-included versus present-empty-array distinction.
// They fail fast on malformed entries instead of surfacing as confusing cross-module test failures.
//
// The catalog grows by addition: adding a scenario is a one-step action that the adapter suites
// pick up automatically. Adapter-specific behavior is documented in the adapter-local specs
// themselves, not enumerated here.
class CompoundWriteScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    CompoundWriteScenarios.all()*.id.toSet().size() == CompoundWriteScenarios.all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    CompoundWriteScenarios.all().every { CompoundWriteScenarios.byId(it.id).is(it) }
  }

  def "catalog uses each request variant"() {
    expect:
    def requests = CompoundWriteScenarios.all()*.request()
    assert requests.any { it instanceof CompoundWriteRequest.ContextFree }
    assert requests.any { it instanceof CompoundWriteRequest.Document }
    assert requests.any { it instanceof CompoundWriteRequest.Collection }
    assert requests.any { it instanceof CompoundWriteRequest.Concurrent }
  }

  def "success entries include both absent included and present empty array"() {
    expect:
    def successes = CompoundWriteScenarios.all()
        .findAll { it.expectation() instanceof CompoundWriteExpectation.Success }
        .collect { (CompoundWriteExpectation.Success) it.expectation() }
    assert successes.any { it.included() == null }
    assert successes.any { it.included() != null && it.included().isEmpty() }
    successes.each { success ->
      success.included()?.each { ref ->
        assert !ref.type().isBlank()
        assert !ref.id().isBlank()
      }
    }
  }

  def "failure expectations pin a non-blank property path when present"() {
    expect:
    CompoundWriteScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof CompoundWriteExpectation.Failure) {
        assert expectation.propertyPath() == null || !expectation.propertyPath().isBlank()
      }
    }
  }

  def "concurrent request and isolation expectation are paired"() {
    expect:
    CompoundWriteScenarios.all().each { scenario ->
      def concurrentRequest = scenario.request() instanceof CompoundWriteRequest.Concurrent
      def concurrentExpectation = scenario.expectation() instanceof CompoundWriteExpectation.ConcurrentIsolation
      assert concurrentRequest == concurrentExpectation
    }
  }

  def "context-free requests omit included"() {
    expect:
    CompoundWriteScenarios.all().each { scenario ->
      if (scenario.request() instanceof CompoundWriteRequest.ContextFree) {
        assert scenario.expectation() instanceof CompoundWriteExpectation.Success
        assert ((CompoundWriteExpectation.Success) scenario.expectation()).included() == null
      }
    }
  }

  def "traversal-delta successes pin off-path relationship and zero delta"() {
    expect:
    CompoundWriteScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof CompoundWriteExpectation.Success && expectation.offPathRelationship() != null) {
        assert expectation.expectedTraversalDelta() == 0
        assert !expectation.offPathRelationship().isBlank()
      }
    }
  }

  def "scenario inputs are freshly constructed on each invocation"() {
    expect:
    CompoundWriteScenarios.all().each { scenario ->
      def request = scenario.request()
      if (request instanceof CompoundWriteRequest.ContextFree) {
        assertFresh(request.supplier())
      } else if (request instanceof CompoundWriteRequest.Document) {
        assertFresh(request.supplier())
      } else if (request instanceof CompoundWriteRequest.Collection) {
        assertFresh(request.supplier())
      } else if (request instanceof CompoundWriteRequest.Concurrent) {
        assertFresh(request.first().supplier())
        assertFresh(request.second().supplier())
      }
    }
  }

  def "one-shot iterable rejects a second iterator on the same instance"() {
    given:
    def scenario = CompoundWriteScenarios.byId("one-shot iterable is materialized once")
    def iterable = ((CompoundWriteRequest.Collection) scenario.request()).supplier().get()

    when:
    iterable.iterator()
    iterable.iterator()

    then:
    thrown(IllegalStateException)
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    CompoundWriteScenarios.where({ true })*.id == CompoundWriteScenarios.all()*.id
    CompoundWriteScenarios.where({ false }).isEmpty()
  }

  def "byId rejects unknown ids"() {
    when:
    CompoundWriteScenarios.byId("no such scenario")

    then:
    thrown(IllegalArgumentException)
  }

  def "success rejects mismatched off-path and delta"() {
    when:
    new CompoundWriteExpectation.Success(List.of(), "comments", null)

    then:
    thrown(IllegalArgumentException)
  }

  def "success rejects a non-zero traversal delta"() {
    when:
    new CompoundWriteExpectation.Success(List.of(), "comments", 1)

    then:
    thrown(IllegalArgumentException)
  }

  def "scenario rejects an isolation expectation without a concurrent request"() {
    when:
    new CompoundWriteScenario(
        "bad",
        CompoundWriteRequest.contextFree { new Article("1", "T", "B", List.of(), null) },
        CompoundWriteExpectation.concurrentIsolation(
        CompoundWriteExpectation.omitted(),
        CompoundWriteExpectation.omitted()))

    then:
    thrown(IllegalArgumentException)
  }

  def "scenario rejects a concurrent request without an isolation expectation"() {
    when:
    new CompoundWriteScenario(
        "bad",
        CompoundWriteScenarios.byId("concurrent compound mappings isolate included sets").request(),
        CompoundWriteExpectation.omitted())

    then:
    thrown(IllegalArgumentException)
  }

  def "failure constructor requires a diagnostic"() {
    when:
    new CompoundWriteExpectation.Failure(null, null, null)

    then:
    thrown(NullPointerException)
  }

  private static void assertFresh(def supplier) {
    def first = supplier.get()
    def second = supplier.get()
    if (first == null) {
      assert second == null
    } else {
      assert !first.is(second)
    }
  }
}
