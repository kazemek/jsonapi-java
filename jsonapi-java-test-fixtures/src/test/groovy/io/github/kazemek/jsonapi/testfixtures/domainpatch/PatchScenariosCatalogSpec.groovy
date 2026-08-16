package io.github.kazemek.jsonapi.testfixtures.domainpatch

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import spock.lang.Specification

class PatchScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    PatchScenarios.all()*.id.toSet().size() == PatchScenarios.all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    PatchScenarios.all().every { PatchScenarios.byId(it.id).is(it) }
  }

  def "every scenario carries exactly one JSON document and one discriminated expectation"() {
    expect:
    PatchScenarios.all().every { scenario ->
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
    PatchScenarios.all().every { scenario ->
      def pkg = scenario.targetType().packageName
      pkg == "io.github.kazemek.jsonapi.testfixtures.domainread" ||
          pkg == "io.github.kazemek.jsonapi.testfixtures.domainwrite" ||
          pkg == "io.github.kazemek.jsonapi.testfixtures.domainpatch"
    }
  }

  def "success expectations carry identity and change lists"() {
    expect:
    PatchScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.Success) {
        assert expectation.identity() != null
        assert expectation.changes() != null
      }
    }
  }

  def "reader failures carry a known rule code and document-relative pointer"() {
    expect:
    PatchScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.ReaderFailure) {
        assert expectation.code() instanceof ValidationRuleCode
        assert expectation.jsonPointer().startsWith("/")
      }
    }
  }

  def "binder failures carry a known diagnostic and resource-relative pointer"() {
    expect:
    PatchScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchExpectation.BinderFailure) {
        assert expectation.diagnostic() instanceof MappingDiagnostic
        assert expectation.propertyPath().startsWith("/")
      }
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    PatchScenarios.where({ true })*.id == PatchScenarios.all()*.id
    PatchScenarios.where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    PatchScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown patch scenario id: no such scenario"
  }
}
