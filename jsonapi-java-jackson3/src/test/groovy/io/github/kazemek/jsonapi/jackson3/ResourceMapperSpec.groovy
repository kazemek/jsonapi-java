package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteComparisonPolicy
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteInput
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteOperation
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteOutcome
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenario
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenarios
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

// @Stepwise pins the declared feature order so the coverage feature always runs after the
// parameterized catalog iterations (Spock does not guarantee feature order otherwise).
@Stepwise
class ResourceMapperSpec extends Specification {

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Shared
  Set<String> executedScenarioIds = new LinkedHashSet<>()

  @Unroll
  def "derives #scenario.id from the shared catalog"() {
    given:
    executedScenarioIds.add(scenario.id())

    when:
    def result = null
    def thrownException = null
    try {
      result = invoke(scenario)
    } catch (Throwable t) {
      thrownException = t
    }

    then:
    verifyOutcome(scenario, result, thrownException)

    where:
    scenario << DomainWriteScenarios.all()
  }

  def "covers every shared domain-write scenario exactly once"() {
    expect:
    executedScenarioIds == DomainWriteScenarios.all()*.id as Set
  }

  private Object invoke(DomainWriteScenario scenario) {
    switch (scenario.operation()) {
      case DomainWriteOperation.TO_RESOURCE:
        return mapper.toResource(singleValue(scenario))
      case DomainWriteOperation.TO_DOCUMENT:
        return mapper.toDocument(singleValue(scenario))
      case DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE:
        return mapper.toDocument(singleValue(scenario), scenario.envelope())
      case DomainWriteOperation.TO_RESOURCE_COLLECTION:
        def input = (DomainWriteInput.CollectionInput) scenario.input()
        return mapper.toResourceCollection(input.supplier().get())
      default:
        throw new IllegalArgumentException("Unknown operation: " + scenario.operation())
    }
  }

  private static Object singleValue(DomainWriteScenario scenario) {
    return ((DomainWriteInput.SingleInput) scenario.input()).supplier().get()
  }

  private static void verifyOutcome(
      DomainWriteScenario scenario, Object result, Throwable thrownException) {
    def outcome = scenario.outcome()
    if (outcome instanceof DomainWriteOutcome.Failure) {
      assert thrownException != null
      assert outcome.exception().isInstance(thrownException)
      return
    }
    assert thrownException == null
    assertSemantics(scenario, (DomainWriteOutcome.Success) outcome, result)
  }

  private static void assertSemantics(
      DomainWriteScenario scenario, DomainWriteOutcome.Success success, Object result) {
    def operation = scenario.operation()
    if (operation == DomainWriteOperation.TO_RESOURCE) {
      assert result instanceof ResourceObject
      assertResource(success.resource(), (ResourceObject) result, scenario.comparisonPolicy())
      return
    }
    if (operation == DomainWriteOperation.TO_DOCUMENT || operation == DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE || operation == DomainWriteOperation.TO_RESOURCE_COLLECTION) {
      assert result instanceof JsonApiDocument
      assertDocument(success.document(), (JsonApiDocument) result, scenario.comparisonPolicy())
      return
    }
    throw new IllegalArgumentException("Unknown operation: " + operation)
  }

  private static void assertResource(
      ResourceObject expected, ResourceObject actual, DomainWriteComparisonPolicy policy) {
    assert actual.type() == expected.type()
    assert actual.id() == expected.id()
    assert actual.lid() == expected.lid()
    assert attributesEqual(expected.attributes(), actual.attributes())
    assert relationshipsEqual(expected.relationships(), actual.relationships(), policy)
  }

  private static void assertDocument(
      JsonApiDocument expected, JsonApiDocument actual, DomainWriteComparisonPolicy policy) {
    switch (expected.data()) {
      case DocumentData.SingleResource:
        assert actual.data() instanceof DocumentData.SingleResource
        assertResource(
            ((DocumentData.SingleResource) expected.data()).resource(),
            ((DocumentData.SingleResource) actual.data()).resource(),
            policy)
        break
      case DocumentData.ResourceCollection:
        assert actual.data() instanceof DocumentData.ResourceCollection
        def expectedResources = ((DocumentData.ResourceCollection) expected.data()).resources()
        def actualResources = ((DocumentData.ResourceCollection) actual.data()).resources()
        assert actualResources.size() == expectedResources.size()
        for (int i = 0; i < expectedResources.size(); i++) {
          assertResource(expectedResources[i], actualResources[i], policy)
        }
        break
      default:
        throw new IllegalArgumentException("Unsupported expected primary data: " + expected.data())
    }
    assert actual.meta() == expected.meta()
    assert actual.jsonapi() == expected.jsonapi()
    assert actual.links() == expected.links()
    assert expected.included() == null
    assert actual.included() == null
  }

  private static boolean attributesEqual(Attributes expected, Attributes actual) {
    if (expected == null || actual == null) {
      return expected == actual
    }
    return expected.attributes() == actual.attributes()
  }

  private static boolean relationshipsEqual(
      Relationships expected,
      Relationships actual,
      DomainWriteComparisonPolicy policy) {
    if (expected == null || actual == null) {
      return expected == actual
    }
    assert actual.relationships().keySet() == expected.relationships().keySet()
    for (Map.Entry<String, Relationship> entry : expected.relationships().entrySet()) {
      def expectedData = entry.value.data()
      def actualData = actual.relationships().get(entry.key).data()
      assert linkageEqual(expectedData, actualData, policy.orderFor(entry.key))
    }
    return true
  }

  private static boolean linkageEqual(
      RelationshipData expected,
      RelationshipData actual,
      DomainWriteComparisonPolicy.ComparisonOrder order) {
    switch (expected) {
      case RelationshipData.NullLinkage:
        return actual instanceof RelationshipData.NullLinkage
      case RelationshipData.SingleLinkage:
        if (!(actual instanceof RelationshipData.SingleLinkage)) {
          return false
        }
        return ((RelationshipData.SingleLinkage) actual).identifier() == expected.identifier()
      case RelationshipData.IdentifierCollectionLinkage:
        if (!(actual instanceof RelationshipData.IdentifierCollectionLinkage)) {
          return false
        }
        def expectedIdentifiers = expected.identifiers()
        def actualIdentifiers = ((RelationshipData.IdentifierCollectionLinkage) actual).identifiers()
        if (expectedIdentifiers.size() != actualIdentifiers.size()) {
          return false
        }
        if (order == DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS) {
          return expectedIdentifiers.toSet() == actualIdentifiers.toSet()
        }
        return expectedIdentifiers == actualIdentifiers
      default:
        return false
    }
  }
}
