package io.github.kazemek.jsonapi.testsupport.decoration

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument
import spock.lang.Specification

class DecorationVerifierSpec extends Specification {

  def "verifies resource success"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("resource links preserve attributes and linkage")
    def expected = ((DecorationOutcome.ResourceSuccess) scenario.outcome()).expected()

    when:
    DecorationVerifier.verify(scenario, expected, null)

    then:
    noExceptionThrown()

    when:
    def wrong = new ResourceObject(expected.type(), expected.id(), expected.lid(), expected.attributes(), expected.relationships(), null, expected.meta(), expected.additionalMembers())
    DecorationVerifier.verify(scenario, wrong, null)

    then:
    thrown(AssertionError)
  }

  def "verifies document success with included"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("included resource receives decoration")
    def outcome = (DecorationOutcome.DocumentSuccess) scenario.outcome()
    def doc = new JsonApiDocument(new DocumentData.SingleResource(outcome.expectedPrimary()), null, null, null, null, outcome.expectedIncluded(), Map.of())

    when:
    DecorationVerifier.verify(scenario, doc, null)

    then:
    noExceptionThrown()
  }

  def "verifies mapped document success"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("sparse fieldset does not resurrect decorated relationship")
    def outcome = (DecorationOutcome.MappedDocumentSuccess) scenario.outcome()
    def doc = new JsonApiDocument(new DocumentData.SingleResource(outcome.expectedPrimary()), null, null, null, null, outcome.expectedIncluded(), Map.of())
    def mapped = new MappedDocument(doc, Set.of())

    when:
    DecorationVerifier.verify(scenario, mapped, null)

    then:
    noExceptionThrown()
  }

  def "verifies failure"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("unknown relationship target is invalid")
    def ex = new JsonApiMappingException(MappingDiagnostic.INVALID_DECORATION_TARGET, String, null, "msg")

    when:
    DecorationVerifier.verify(scenario, null, ex)

    then:
    noExceptionThrown()

    when:
    DecorationVerifier.verify(scenario, null, null)

    then:
    thrown(AssertionError)
  }

  def "rejects wrong diagnostic"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("unknown relationship target is invalid")
    def ex = new JsonApiMappingException(MappingDiagnostic.INVALID_DECORATION_STATE, String, null, "msg")

    when:
    DecorationVerifier.verify(scenario, null, ex)

    then:
    thrown(AssertionError)
  }

  def "rejects unexpected exception for success"() {
    given:
    def scenario = DecorationScenarios.catalog().byId("resource links preserve attributes and linkage")

    when:
    DecorationVerifier.verify(scenario, null, new RuntimeException("boom"))

    then:
    thrown(AssertionError)
  }

  def "DecorationScenario validation"() {
    when:
    new DecorationScenario(null, { new Object() }, ResourceDecoratorRegistry.empty(), null, null, new DecorationOutcome.ResourceSuccess(new ResourceObject("a", "1", null, null, null, null, null, Map.of())))

    then:
    thrown(IllegalArgumentException)

    when:
    new DecorationScenario("", { new Object() }, ResourceDecoratorRegistry.empty(), null, null, new DecorationOutcome.ResourceSuccess(new ResourceObject("a", "1", null, null, null, null, null, Map.of())))

    then:
    thrown(IllegalArgumentException)
  }

  def "DecorationOutcome validation"() {
    when:
    new DecorationOutcome.ResourceSuccess(null)

    then:
    thrown(IllegalArgumentException)

    when:
    new DecorationOutcome.Failure(null)

    then:
    thrown(IllegalArgumentException)
  }
}
