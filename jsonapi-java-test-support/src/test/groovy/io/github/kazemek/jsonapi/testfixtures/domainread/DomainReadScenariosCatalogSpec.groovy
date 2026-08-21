package io.github.kazemek.jsonapi.testfixtures.domainread

import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import spock.lang.Specification

// Why this spec exists: DomainReadScenarios is the version-neutral flat binder catalog shared by
// every Jackson major. Adapter suites run the whole catalog through their own binder — Jackson 3
// asserts executedScenarioIds == catalogScenarioIds in ResourceBinderSpec, and Phase 2.21 mandates
// the same for Jackson 2 — so every entry must stay self-consistent. These tests enforce the local
// invariants that hold for any catalog entry regardless of catalog size: unique stable ids, exactly
// one input variant/converter discriminator/discriminated expectation, resolvable target DTO
// classes, and complete bound values or known diagnostics. They fail fast on malformed entries
// instead of surfacing as confusing cross-module test failures.
//
// The catalog grows by addition: adding a scenario is a one-step action that the adapter suites
// pick up automatically. Adapter-specific behavior is documented in the adapter-local specs
// themselves, not enumerated here.
class DomainReadScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    DomainReadScenarios.all()*.id.toSet().size() == DomainReadScenarios.all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    DomainReadScenarios.all().every { DomainReadScenarios.byId(it.id).is(it) }
  }

  def "every scenario carries exactly one input variant, converter behavior, and discriminated expectation"() {
    expect:
    DomainReadScenarios.all().every { scenario ->
      assert scenario.input() instanceof DomainReadInput.SingleResource ||
      scenario.input() instanceof DomainReadInput.ResourceCollection ||
      scenario.input() instanceof DomainReadInput.IncludedIsolation
      assert scenario.converterBehavior() != null
      assert scenario.expectation() instanceof DomainReadExpectation.BoundValue ||
      scenario.expectation() instanceof DomainReadExpectation.Failure
      true
    }
  }

  def "every target type lives in a shared fixture package"() {
    expect:
    DomainReadScenarios.all().every { scenario ->
      def pkg = scenario.targetType().packageName
      pkg == "io.github.kazemek.jsonapi.testfixtures.domainread" ||
          pkg == "io.github.kazemek.jsonapi.testfixtures.domainwrite"
    }
  }

  def "bound-value expectations are complete for the input variant"() {
    expect:
    DomainReadScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof DomainReadExpectation.BoundValue) {
        def value = expectation.value()
        if (scenario.input() instanceof DomainReadInput.ResourceCollection) {
          assert value instanceof List
          assert !((List) value).isEmpty()
          assert ((List) value).every { scenario.targetType().isInstance(it) }
        } else {
          assert scenario.targetType().isInstance(value)
        }
      }
    }
  }

  def "failure expectations carry a known diagnostic and a path only when shared"() {
    expect:
    DomainReadScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof DomainReadExpectation.Failure) {
        assert expectation.diagnostic() != null
        if (expectation.propertyPath() != null) {
          assert expectation.propertyPath().startsWith("/")
        }
        if (expectation.resourceClass() != null) {
          assert expectation.resourceClass() == scenario.targetType()
          assert expectation.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
        }
      }
    }
  }

  def "included-isolation input carries two distinct wire documents"() {
    expect:
    DomainReadScenarios.all().each { scenario ->
      def input = scenario.input()
      if (input instanceof DomainReadInput.IncludedIsolation) {
        assert input.primaryJson() != input.swappedIncludedJson()
        assert input.primaryJson().contains("\"included\"")
        assert input.swappedIncludedJson().contains("\"included\"")
      }
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    DomainReadScenarios.where({ true })*.id == DomainReadScenarios.all()*.id
    DomainReadScenarios.where({ false }).isEmpty()
  }

  def "byId rejects unknown ids"() {
    when:
    DomainReadScenarios.byId("no such scenario")

    then:
    thrown(IllegalArgumentException)
  }
}
