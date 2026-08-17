package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection

import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import spock.lang.Specification

class PatchProjectionScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    PatchProjectionScenarios.all()*.id.toSet().size() == PatchProjectionScenarios.all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    PatchProjectionScenarios.all().every { PatchProjectionScenarios.byId(it.id).is(it) }
  }

  def "every scenario carries one JSON document and one discriminated expectation"() {
    expect:
    PatchProjectionScenarios.all().every { scenario ->
      assert scenario.documentJson() != null
      assert !scenario.documentJson().isBlank()
      assert scenario.expectation() instanceof PatchProjectionExpectation.Success ||
      scenario.expectation() instanceof PatchProjectionExpectation.ProjectorFailure
      true
    }
  }

  def "command and patch target types live in shared fixture packages"() {
    expect:
    PatchProjectionScenarios.all().every { scenario ->
      def commandPkg = scenario.commandTargetType().packageName
      def patchPkg = scenario.patchTargetType().packageName
      (commandPkg == "io.github.kazemek.jsonapi.testfixtures.domainread" ||
          commandPkg == "io.github.kazemek.jsonapi.testfixtures.domainwrite" ||
          commandPkg == "io.github.kazemek.jsonapi.testfixtures.domainpatch") &&
          patchPkg == "io.github.kazemek.jsonapi.testfixtures.domainpatchprojection"
    }
  }

  def "projector failures carry a known diagnostic and property path"() {
    expect:
    PatchProjectionScenarios.all().each { scenario ->
      def expectation = scenario.expectation()
      if (expectation instanceof PatchProjectionExpectation.ProjectorFailure) {
        assert expectation.diagnostic() instanceof MappingDiagnostic
        assert expectation.propertyPath().startsWith("/")
      }
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    PatchProjectionScenarios.where({ true })*.id == PatchProjectionScenarios.all()*.id
    PatchProjectionScenarios.where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    PatchProjectionScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown patch-projection scenario id: no such scenario"
  }
}
