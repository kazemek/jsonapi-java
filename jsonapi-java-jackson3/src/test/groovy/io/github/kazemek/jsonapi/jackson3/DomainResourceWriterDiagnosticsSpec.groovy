package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.testsupport.domainwrite.WriteDiagnosticScenario
import io.github.kazemek.jsonapi.testsupport.domainwrite.WriteDiagnosticsScenarios
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

/**
 * Jackson 3 runner for the shared write-diagnostics catalog. Every scenario maps its carrier
 * through this adapter's own resource writer and asserts the shared semantic diagnostic category
 * and stable wire location. Jackson-specific cause chains and introspection mechanics stay in
 * adapter-local specs.
 */
class DomainResourceWriterDiagnosticsSpec extends Specification {

  def "shared write diagnostics catalog scenario: #scenario.id"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = scenario.entity().get()

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == scenario.diagnostic()
    if (scenario.propertyPath() == null) {
      // Class-level and specification failures have no document member coordinate; the location
      // is absent, never "", "/", or a logical property name.
      ex.location() == null
    } else {
      ex.propertyPath() == scenario.propertyPath()
    }

    where:
    scenario << WriteDiagnosticsScenarios.catalog().all()
  }

  def "runs the complete shared write diagnostics catalog"() {
    given:
    def executed = [] as Set

    when:
    WriteDiagnosticsScenarios.catalog().all().each { WriteDiagnosticScenario scenario ->
      def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
      try {
        mapper.toResource(scenario.entity().get())
        throw new AssertionError("expected mapping failure for ${scenario.id}")
      } catch (JsonApiMappingException ignored) {
        executed << scenario.id
      }
    }

    then:
    executed == WriteDiagnosticsScenarios.catalog().all()*.id as Set
  }
}
