package io.github.kazemek.jsonapi.testsupport.domainwrite

import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import spock.lang.Specification

// Why this spec exists: WriteDiagnosticsScenarios is the version-neutral write-diagnostics
// contract shared by every Jackson major. Adapter suites run the whole catalog through their own
// writer and assert full-catalog coverage, so every entry must stay self-consistent. These tests
// enforce the local invariants that hold for any entry regardless of catalog size: unique stable
// ids, resolvable carriers producing distinct instances per call, known diagnostics, and either a
// resource-relative pointer or an intentionally absent location.
class WriteDiagnosticsScenariosCatalogSpec extends Specification {

  def "every scenario carries a known diagnostic and a valid location contract"() {
    expect:
    WriteDiagnosticsScenarios.catalog().all().every { scenario ->
      assert scenario.diagnostic() instanceof MappingDiagnostic
      if (scenario.propertyPath() == null) {
        true
      } else {
        assert scenario.propertyPath().startsWith("/")
        true
      }
    }
  }

  def "entity suppliers build fresh instances on every call"() {
    given:
    def scenarios = WriteDiagnosticsScenarios.catalog().all()

    expect:
    scenarios.every { scenario ->
      def first = scenario.entity().get()
      def second = scenario.entity().get()
      first != null && second != null && !first.is(second)
    }
  }

  def "location composition cases pin wire names rather than logical names"() {
    expect:
    WriteDiagnosticsScenarios.catalog().byId("renamed-failing-attribute-getter-reports-wire-name")
        .propertyPath() == "/attributes/body-text"
    WriteDiagnosticsScenarios.catalog().byId("unsupported-runtime-collection-shape-at-data")
        .propertyPath() == "/relationships/ext-values/data"
  }
}
