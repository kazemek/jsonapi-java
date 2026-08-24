package io.github.kazemek.jsonapi.testsupport.domainpatch

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchPresence
import spock.lang.Specification

class PatchDtoScenariosCatalogSpec extends Specification {

  def "catalog covers every expectation kind and declaration validation"() {
    expect:
    PatchDtoScenarios.catalog().all().any { it.expectation() instanceof PatchDtoExpectation.Success }
    PatchDtoScenarios.catalog().all().any { it.expectation() instanceof PatchDtoExpectation.ReaderFailure }
    PatchDtoScenarios.catalog().all().any { it.expectation() instanceof PatchDtoExpectation.BinderFailure }
    PatchDtoScenarios.catalog().where({ it.id().contains("declaration") }).size() >= 1
  }

  def "catalog ids are unique"() {
    expect:
    PatchDtoScenarios.catalog().all()*.id.toSet().size() == PatchDtoScenarios.catalog().all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    PatchDtoScenarios.catalog().all().every { PatchDtoScenarios.catalog().byId(it.id).is(it) }
  }

  def "every scenario carries exactly one JSON document and one discriminated expectation"() {
    expect:
    PatchDtoScenarios.catalog().all().every { scenario ->
      assert scenario.documentJson() != null
      assert !scenario.documentJson().isBlank()
      assert scenario.expectation() instanceof PatchDtoExpectation.Success ||
      scenario.expectation() instanceof PatchDtoExpectation.ReaderFailure ||
      scenario.expectation() instanceof PatchDtoExpectation.BinderFailure
      true
    }
  }

  def "every target type lives in the shared domainpatch fixture package"() {
    expect:
    PatchDtoScenarios.catalog().all().every { scenario ->
      scenario.targetType().packageName == "io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch"
    }
  }

  def "success expectations carry identity, full member maps, and PatchPresence values"() {
    expect:
    PatchDtoScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchDtoExpectation.Success) {
        assert expectation.identity() != null
        assert expectation.members() != null
        assert !expectation.members().isEmpty()
        expectation.members().values().each { value ->
          assert value instanceof PatchPresence
        }
      }
    }
  }

  def "reader failures carry a known rule code and document-relative pointer"() {
    expect:
    PatchDtoScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchDtoExpectation.ReaderFailure) {
        assert expectation.code() instanceof ValidationRuleCode
        assert expectation.jsonPointer().startsWith("/")
      }
    }
  }

  def "binder failures carry a known diagnostic and resource-relative pointer"() {
    expect:
    PatchDtoScenarios.catalog().all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchDtoExpectation.BinderFailure) {
        assert expectation.diagnostic() instanceof MappingDiagnostic
        assert expectation.propertyPath().startsWith("/")
      }
    }
  }

  def "declaration failures use INVALID_PATCH_PROPERTY_TYPE"() {
    expect:
    PatchDtoScenarios.catalog().where({ it.id().contains("declaration") }).every { scenario ->
      def expectation = scenario.expectation()
      expectation instanceof PatchDtoExpectation.BinderFailure &&
          expectation.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    PatchDtoScenarios.catalog().where({ true })*.id == PatchDtoScenarios.catalog().all()*.id
    PatchDtoScenarios.catalog().where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    PatchDtoScenarios.catalog().byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown patch-dto scenario id: no such scenario"
  }
}
