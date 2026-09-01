package io.github.kazemek.jsonapi.testsupport.decoration

import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.ResourceObject
import spock.lang.Specification

class DecorationScenariosCatalogSpec extends Specification {

  def "catalog ids are unique and stable"() {
    given:
    def ids = DecorationScenarios.catalog().all().collect { it.id() }

    expect:
    ids.size() == ids.toSet().size()
    !ids.any { it == null || it.isEmpty() }
  }

  def "every scenario carries a domain supplier, registry, and expected resource"() {
    expect:
    DecorationScenarios.catalog().all().every { scenario ->
      assert scenario.domainSupplier() != null
      assert scenario.decorators() != null
      assert scenario.expected() != null
      assert scenario.expected() instanceof ResourceObject
      true
    }
  }

  def "expected resources carry preserved type and linkage"() {
    expect:
    DecorationScenarios.catalog().all().each { scenario ->
      def expected = scenario.expected()
      assert expected.type() != null
      assert expected.type() == "articles"
      if (expected.relationships() != null) {
        expected.relationships().relationships().each { name, Relationship rel ->
          assert rel.data() != null || rel.links() != null
        }
      }
    }
  }

  def "present-empty links scenario is distinct from absent"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("present-empty links are preserved")

    expect:
    scenario.expected().links() != null
    scenario.expected().links().isEmpty()
    DecorationVerifier.verify(scenario, scenario.expected())
  }

  def "verifier rejects mismatched links"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("resource links preserve attributes and linkage")
    def wrong = new ResourceObject(
        scenario.expected().type(),
        scenario.expected().id(),
        scenario.expected().lid(),
        scenario.expected().attributes(),
        scenario.expected().relationships(),
        null,
        scenario.expected().meta(),
        scenario.expected().additionalMembers())

    when:
    DecorationVerifier.verify(scenario, wrong)

    then:
    thrown(AssertionError)
  }
}
