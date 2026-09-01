package io.github.kazemek.jsonapi.testsupport.sparsefieldset

import io.github.kazemek.jsonapi.jackson.representation.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article
import java.util.EnumSet
import spock.lang.Specification

// Why this spec exists: SparseFieldsetScenarios is the version-neutral sparse-fieldset catalog
// shared by every Jackson major. Adapter suites iterate the whole catalog directly through their
// own mapper (Jackson 3 in SparseFieldsetSpec; Jackson 2 likewise later), so every entry must stay
// self-consistent. These tests
// enforce the local invariants that hold for any catalog entry regardless of catalog size: exactly
// one operation/request variant/discriminated expectation, and resolvable resource
// states or known diagnostics. Duplicate ids and generic catalog behavior belong to
// FixtureCatalogSpec. They fail fast on malformed
// entries instead of surfacing as confusing cross-module test failures.
//
// The catalog grows by addition: adding a scenario is a one-step action that the adapter suites
// pick up automatically. Adapter-specific behavior is documented in the adapter-local specs
// themselves, not enumerated here.
class SparseFieldsetScenariosCatalogSpec extends Specification {

  def "catalog uses each operation and request variant"() {
    expect:
    def operations = SparseFieldsetScenarios.catalog().all()*.operation() as Set
    assert operations.containsAll(EnumSet.allOf(SparseFieldsetOperation))
    def requests = SparseFieldsetScenarios.catalog().all()*.request()
    assert requests.any { it instanceof SparseFieldsetRequest.Single }
    assert requests.any { it instanceof SparseFieldsetRequest.Collection }
    assert requests.any { it instanceof SparseFieldsetRequest.Concurrent }
    assert requests.any { it instanceof SparseFieldsetRequest.IdentityPreservation }
  }

  def "success entries include mapped, unmapped, concurrent, and identity outcomes"() {
    expect:
    def expectations = SparseFieldsetScenarios.catalog().all()*.expectation()
    assert expectations.any { it instanceof SparseFieldsetExpectation.MappedSuccess }
    assert expectations.any { it instanceof SparseFieldsetExpectation.UnmappedSuccess }
    assert expectations.any { it instanceof SparseFieldsetExpectation.ConcurrentIsolation }
    assert expectations.any { it instanceof SparseFieldsetExpectation.IdentityPreservation }
    assert expectations.any { it instanceof SparseFieldsetExpectation.Failure }
  }

  def "mapped successes pin absent included distinctly from a present list"() {
    expect:
    def successes = SparseFieldsetScenarios.catalog().all()
        .findAll { it.expectation() instanceof SparseFieldsetExpectation.MappedSuccess }
        .collect { (SparseFieldsetExpectation.MappedSuccess) it.expectation() }
    assert successes.any { it.included() == null }
    successes.each { success ->
      assert !success.primary().type().isBlank()
      success.included()?.each { state ->
        assert !state.type().isBlank()
      }
    }
  }

  def "failure expectations pin a non-blank property path when present"() {
    expect:
    SparseFieldsetScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof SparseFieldsetExpectation.Failure) {
        assert expectation.propertyPath() == null || !expectation.propertyPath().isBlank()
        if (expectation.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT) {
          assert expectation.propertyPath() == null
          assert expectation.resourceClass() == null
        }
      }
    }
  }

  def "concurrent request and isolation expectation are paired"() {
    expect:
    SparseFieldsetScenarios.catalog().all().each { scenario ->
      def concurrentRequest = scenario.request() instanceof SparseFieldsetRequest.Concurrent
      def concurrentExpectation = scenario.expectation() instanceof SparseFieldsetExpectation.ConcurrentIsolation
      assert concurrentRequest == concurrentExpectation
    }
  }

  def "identity-preservation request and expectation are paired with four shapes"() {
    expect:
    SparseFieldsetScenarios.catalog().all().each { scenario ->
      def identityRequest = scenario.request() instanceof SparseFieldsetRequest.IdentityPreservation
      def identityExpectation = scenario.expectation() instanceof SparseFieldsetExpectation.IdentityPreservation
      assert identityRequest == identityExpectation
      if (identityRequest) {
        assert ((SparseFieldsetRequest.IdentityPreservation) scenario.request()).contexts().size() ==
        SparseFieldsetRequest.IdentityPreservation.SHAPE_COUNT
      }
    }
  }

  def "access-counting success carries the shared zero-read guarantee"() {
    given:
    def scenario = SparseFieldsetScenarios.catalog().byId("access counting proves linkage vs traversal split")

    expect:
    def success = (SparseFieldsetExpectation.MappedSuccess) scenario.expectation()
    assert success.zeroReads() != null
    assert success.zeroReads().unreadAttributes() == ["body"] as Set
    assert success.zeroReads().unreadRelationships() == ["comments"] as Set
  }

  def "collapsed duplicate fieldsets carry unique names"() {
    given:
    def scenario = SparseFieldsetScenarios.catalog().byId(
        "duplicate-free multi-field fieldset keeps title and author")
    def context = ((SparseFieldsetRequest.Single) scenario.request()).context()

    expect:
    context.fieldsets().get("articles") == ["title", "author"]
    context.fieldsets().get("articles").size() == context.fieldsets().get("articles").toSet().size()
  }

  def "scenario inputs are freshly constructed on each invocation"() {
    expect:
    SparseFieldsetScenarios.catalog().all().each { scenario ->
      def request = scenario.request()
      if (request instanceof SparseFieldsetRequest.Single) {
        assertFresh(request.supplier())
      } else if (request instanceof SparseFieldsetRequest.Collection) {
        assertFresh(request.supplier())
      } else if (request instanceof SparseFieldsetRequest.Concurrent) {
        assertFresh(request.first().supplier())
        assertFresh(request.second().supplier())
      } else if (request instanceof SparseFieldsetRequest.IdentityPreservation) {
        assertFresh(request.supplier())
      }
    }
  }

  def "scenario rejects an isolation expectation without a concurrent request"() {
    when:
    new SparseFieldsetScenario(
        "bad",
        SparseFieldsetOperation.TO_MAPPED_DOCUMENT,
        SparseFieldsetRequest.single(
        { new Article("1", "T", "B", List.of(), null) },
        CompoundSerializationContext.defaults()),
        SparseFieldsetExpectation.concurrentIsolation(
        SparseFieldsetExpectation.mapped(
        FieldsetResourceState.identity("articles", "1"), null, false),
        SparseFieldsetExpectation.mapped(
        FieldsetResourceState.identity("articles", "1"), null, false)))

    then:
    thrown(IllegalArgumentException)
  }

  def "scenario rejects a concurrent request without an isolation expectation"() {
    when:
    new SparseFieldsetScenario(
        "bad",
        SparseFieldsetOperation.TO_MAPPED_DOCUMENT,
        SparseFieldsetScenarios.catalog().byId(
        "concurrent fieldset mappings isolate documents and linkage exemptions").request(),
        SparseFieldsetExpectation.mapped(
        FieldsetResourceState.identity("articles", "1"), null, false))

    then:
    thrown(IllegalArgumentException)
  }

  def "identity preservation rejects a shape count other than four"() {
    when:
    new SparseFieldsetRequest.IdentityPreservation(
        { new Article("1", "T", "B", List.of(), null) },
        List.of(CompoundSerializationContext.defaults()))

    then:
    thrown(IllegalArgumentException)
  }

  def "failure constructor requires a diagnostic"() {
    when:
    new SparseFieldsetExpectation.Failure(null, null, null)

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
