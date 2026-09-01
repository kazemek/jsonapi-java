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

  def "every scenario carries a domain supplier, registry, and discriminated outcome"() {
    expect:
    DecorationScenarios.catalog().all().every { scenario ->
      assert scenario.domainSupplier() != null
      assert scenario.decorators() != null
      assert scenario.outcome() != null
      if (scenario.outcome() instanceof DecorationOutcome.ResourceSuccess) {
        assert ((DecorationOutcome.ResourceSuccess) scenario.outcome()).expected() != null
      } else if (scenario.outcome() instanceof DecorationOutcome.DocumentSuccess) {
        assert ((DecorationOutcome.DocumentSuccess) scenario.outcome()).expectedPrimary() != null
      } else if (scenario.outcome() instanceof DecorationOutcome.MappedDocumentSuccess) {
        assert ((DecorationOutcome.MappedDocumentSuccess) scenario.outcome()).expectedPrimary() != null
      } else if (scenario.outcome() instanceof DecorationOutcome.Failure) {
        assert ((DecorationOutcome.Failure) scenario.outcome()).expectedDiagnostic() != null
      }
      true
    }
  }

  def "expected resources carry preserved type and linkage"() {
    expect:
    DecorationScenarios.catalog().all().each { scenario ->
      def outcome = scenario.outcome()
      ResourceObject expected = null
      if (outcome instanceof DecorationOutcome.ResourceSuccess) {
        expected = ((DecorationOutcome.ResourceSuccess) outcome).expected()
      } else if (outcome instanceof DecorationOutcome.DocumentSuccess) {
        expected = ((DecorationOutcome.DocumentSuccess) outcome).expectedPrimary()
      } else if (outcome instanceof DecorationOutcome.MappedDocumentSuccess) {
        expected = ((DecorationOutcome.MappedDocumentSuccess) outcome).expectedPrimary()
      }
      if (expected != null) {
        assert expected.type() != null
        assert expected.type() == "articles"
        if (expected.relationships() != null) {
          expected.relationships().relationships().each { name, Relationship rel ->
            assert rel.data() != null || rel.links() != null
          }
        }
      }
    }
  }

  def "present-empty links scenario is distinct from absent"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("present-empty links are preserved")
    def outcome = (DecorationOutcome.ResourceSuccess) scenario.outcome()

    expect:
    outcome.expected().links() != null
    outcome.expected().links().isEmpty()
    DecorationVerifier.verify(scenario, outcome.expected(), null)
  }

  def "verifier rejects mismatched links"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("resource links preserve attributes and linkage")
    def expected = ((DecorationOutcome.ResourceSuccess) scenario.outcome()).expected()
    def wrong = new ResourceObject(
        expected.type(),
        expected.id(),
        expected.lid(),
        expected.attributes(),
        expected.relationships(),
        null,
        expected.meta(),
        expected.additionalMembers())

    when:
    DecorationVerifier.verify(scenario, wrong, null)

    then:
    thrown(AssertionError)
  }

  def "document success scenarios have consistent included expectations"() {
    expect:
    DecorationScenarios.catalog().all().each { scenario ->
      def outcome = scenario.outcome()
      if (outcome instanceof DecorationOutcome.DocumentSuccess) {
        def success = (DecorationOutcome.DocumentSuccess) outcome
        assert scenario.selection() != null
        assert scenario.policy() != null
        if (success.expectedIncluded() != null) {
          assert !success.expectedIncluded().isEmpty()
        }
      }
      if (outcome instanceof DecorationOutcome.MappedDocumentSuccess) {
        assert scenario.selection() != null
        assert scenario.policy() != null
      }
    }
  }

  def "failure scenarios carry expected diagnostics"() {
    expect:
    DecorationScenarios.catalog().all().findAll { it.outcome() instanceof DecorationOutcome.Failure }.each { scenario ->
      def failure = (DecorationOutcome.Failure) scenario.outcome()
      assert failure.expectedDiagnostic() != null
    }
    DecorationScenarios.catalog().all().count { it.outcome() instanceof DecorationOutcome.Failure } >= 3
  }
}
