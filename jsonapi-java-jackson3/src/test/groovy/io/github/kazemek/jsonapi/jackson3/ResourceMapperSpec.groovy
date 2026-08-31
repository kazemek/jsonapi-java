package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.testsupport.domainwrite.DomainWriteInput
import io.github.kazemek.jsonapi.testsupport.domainwrite.DomainWriteOperation
import io.github.kazemek.jsonapi.testsupport.domainwrite.DomainWriteScenario
import io.github.kazemek.jsonapi.testsupport.domainwrite.DomainWriteScenarios
import io.github.kazemek.jsonapi.testsupport.domainwrite.DomainWriteVerifier
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

// Runs every entry of the shared domain-write catalog directly through this adapter's mapper.
// Catalog completeness and stable ids are owned by the test-support catalog integrity specs;
// Jackson-API-specific behavior stays in adapter-local specs. Semantic comparison lives in
// DomainWriteVerifier so Jackson 2 does not copy resource/document comparison.
class ResourceMapperSpec extends Specification {

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Unroll
  def "derives #scenario.id from the shared catalog"() {
    when:
    def result = null
    def thrownException = null
    try {
      result = invoke(scenario)
    } catch (Throwable t) {
      thrownException = t
    }

    then:
    DomainWriteVerifier.verify(scenario, result, thrownException)

    where:
    scenario << DomainWriteScenarios.catalog().all()
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
}
