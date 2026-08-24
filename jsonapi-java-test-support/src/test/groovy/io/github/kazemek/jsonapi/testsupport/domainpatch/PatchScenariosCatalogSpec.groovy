package io.github.kazemek.jsonapi.testsupport.domainpatch

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import spock.lang.Specification

class PatchScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    PatchScenarios.catalog().all()*.id.toSet().size() == PatchScenarios.catalog().all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    PatchScenarios.catalog().all().every { PatchScenarios.catalog().byId(it.id).is(it) }
  }

  def "every scenario carries exactly one JSON document and one discriminated expectation"() {
    expect:
    PatchScenarios.catalog().all().every { scenario ->
      assert scenario.documentJson() != null
      assert !scenario.documentJson().isBlank()
      assert scenario.expectation() instanceof PatchExpectation.Success ||
      scenario.expectation() instanceof PatchExpectation.ReaderFailure ||
      scenario.expectation() instanceof PatchExpectation.BinderFailure
      true
    }
  }

  def "every target type lives in a shared fixture package"() {
    expect:
    PatchScenarios.catalog().all().every { scenario ->
      def pkg = scenario.targetType().packageName
      pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.domainread" ||
          pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite" ||
          pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch"
    }
  }

  def "success expectations carry identity and change lists"() {
    expect:
    PatchScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.Success) {
        assert expectation.identity() != null
        assert expectation.changes() != null
      }
    }
  }

  def "reader failures carry a known rule code and document-relative pointer"() {
    expect:
    PatchScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.ReaderFailure) {
        assert expectation.code() instanceof ValidationRuleCode
        assert expectation.jsonPointer().startsWith("/")
      }
    }
  }

  def "binder failures carry a known diagnostic and resource-relative pointer"() {
    expect:
    PatchScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.BinderFailure) {
        assert expectation.diagnostic() instanceof MappingDiagnostic
        assert expectation.propertyPath().startsWith("/")
      }
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    PatchScenarios.catalog().where({ true })*.id == PatchScenarios.catalog().all()*.id
    PatchScenarios.catalog().where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    PatchScenarios.catalog().byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown patch scenario id: no such scenario"
  }
}
