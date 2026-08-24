package io.github.kazemek.jsonapi.testsupport.domainwrite

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceObject
import spock.lang.Specification
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.ArticleWithSet;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.SamplePojo;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Tag;

// Why this spec exists: DomainWriteScenarios is the version-neutral flat write catalog shared by
// every Jackson major. Adapter suites run the whole catalog through their own mapper — Jackson 3
// asserts executedScenarioIds == catalogScenarioIds in ResourceMapperSpec, and Phase 2.18 mandates
// the same for Jackson 2 — so every entry must stay self-consistent. These tests enforce the local
// invariants that hold for any catalog entry regardless of catalog size: unique stable ids, exactly
// one operation/typed input/envelope state/discriminated outcome/policy, complete expected
// outcomes, and valid comparison policies. They fail fast on malformed entries instead of
// surfacing as confusing cross-module test failures.
//
// The catalog grows by addition: adding a scenario is a one-step action that the adapter suites
// pick up automatically. Adapter-specific behavior is documented in the adapter-local specs
// themselves, not enumerated here.
class DomainWriteScenariosCatalogSpec extends Specification {

  def "catalog ids are unique"() {
    expect:
    DomainWriteScenarios.catalog().all()*.id.toSet().size() == DomainWriteScenarios.catalog().all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    DomainWriteScenarios.catalog().all().every { DomainWriteScenarios.catalog().byId(it.id).is(it) }
  }

  def "every scenario carries exactly one operation, typed input, discriminated outcome, and policy"() {
    expect:
    DomainWriteScenarios.catalog().all().every { scenario ->
      assert scenario.operation() != null
      assert scenario.input() instanceof DomainWriteInput.SingleInput || scenario.input() instanceof DomainWriteInput.CollectionInput
      assert scenario.outcome() instanceof DomainWriteOutcome.Success || scenario.outcome() instanceof DomainWriteOutcome.Failure
      assert scenario.comparisonPolicy() != null
      true
    }
  }

  def "operation, input, and envelope state are consistent per operation"() {
    expect:
    DomainWriteScenarios.catalog().all().each { scenario ->
      def operation = scenario.operation()
      if (operation == DomainWriteOperation.TO_RESOURCE_COLLECTION) {
        assert scenario.input() instanceof DomainWriteInput.CollectionInput
        assert scenario.envelope() == null
      } else {
        assert scenario.input() instanceof DomainWriteInput.SingleInput
        if (operation == DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE) {
          assert scenario.envelope() != null
        } else {
          assert scenario.envelope() == null
        }
      }
    }
  }

  def "successful outcomes hold exactly one core value matching the operation"() {
    expect:
    DomainWriteScenarios.catalog().all().each { scenario ->
      def outcome = scenario.outcome()
      if (outcome instanceof DomainWriteOutcome.Success) {
        if (scenario.operation() == DomainWriteOperation.TO_RESOURCE) {
          assert outcome.resource() != null
          assert outcome.document() == null
        } else {
          assert outcome.document() != null
          assert outcome.resource() == null
        }
      } else if (outcome instanceof DomainWriteOutcome.Failure) {
        assert outcome.exception() != null
      }
    }
  }

  def "document outcomes carry exact primary data and absent included"() {
    expect:
    DomainWriteScenarios.catalog().all().each { scenario ->
      def outcome = scenario.outcome()
      if (outcome instanceof DomainWriteOutcome.Success && outcome.document() != null) {
        def document = outcome.document()
        assert document.included() == null
        if (scenario.operation() == DomainWriteOperation.TO_RESOURCE_COLLECTION) {
          assert document.data() instanceof DocumentData.ResourceCollection
        } else {
          assert document.data() instanceof DocumentData.SingleResource
        }
      }
    }
  }

  def "comparison policies reference existing relationships and only to-many linkage may be unordered"() {
    expect:
    DomainWriteScenarios.catalog().all().each { scenario ->
      def policy = scenario.comparisonPolicy()
      assert policy != null
      def expectedNames = expectedRelationshipNames(scenario)
      policy.relationshipOrder().each { name, order ->
        assert name in expectedNames
        if (order == DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS) {
          assert expectedLinkageIsCollection(scenario, name)
        }
      }
      expectedNames.each { name ->
        def order = policy.orderFor(name)
        assert order == DomainWriteComparisonPolicy.ComparisonOrder.ORDERED || order == DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS
      }
    }
  }

  def "the Set-based scenario input collection rejects mutation"() {
    given:
    def scenario = DomainWriteScenarios.catalog().byId("maps Set-based to-many relationship")
    def article = (ArticleWithSet) ((DomainWriteInput.SingleInput) scenario.input()).supplier().get()

    when:
    article.tags().add(new Tag("kotlin"))

    then:
    thrown(UnsupportedOperationException)
  }

  def "scenario inputs are freshly constructed on each invocation"() {
    expect:
    DomainWriteScenarios.catalog().all().each { scenario ->
      def input = scenario.input()
      def first = input.supplier().get()
      def second = input.supplier().get()
      if (first == null) {
        assert second == null
      } else {
        assert !first.is(second)
      }
    }
  }

  def "the mutable POJO model exposes its bean surface"() {
    given:
    def pojo = new SamplePojo()

    when:
    pojo.setId("p9")
    pojo.setName("Bean")
    pojo.setComments(List.of())

    then:
    pojo.getId() == "p9"
    pojo.getName() == "Bean"
    pojo.getComments() == List.of()
  }

  def "byId rejects unknown ids"() {
    when:
    DomainWriteScenarios.catalog().byId("no such scenario")

    then:
    thrown(IllegalArgumentException)
  }

  def "success outcomes reject empty or double values"() {
    when:
    new DomainWriteOutcome.Success(null, null)

    then:
    thrown(IllegalArgumentException)
  }

  private static Set<String> expectedRelationshipNames(DomainWriteScenario scenario) {
    def outcome = scenario.outcome()
    if (!(outcome instanceof DomainWriteOutcome.Success)) {
      return [] as Set
    }
    if (outcome.resource() != null) {
      return relationshipNames(outcome.resource())
    }
    def data = outcome.document().data()
    if (data instanceof DocumentData.SingleResource) {
      return relationshipNames(data.resource())
    }
    if (data instanceof DocumentData.ResourceCollection) {
      return data.resources().collectMany { relationshipNames(it) } as Set
    }
    return [] as Set
  }

  private static boolean expectedLinkageIsCollection(DomainWriteScenario scenario, String relationshipName) {
    def outcome = scenario.outcome()
    if (!(outcome instanceof DomainWriteOutcome.Success)) {
      return false
    }
    if (outcome.resource() != null) {
      return linkageIsCollection(outcome.resource(), relationshipName)
    }
    def data = outcome.document().data()
    if (data instanceof DocumentData.SingleResource) {
      return linkageIsCollection(data.resource(), relationshipName)
    }
    if (data instanceof DocumentData.ResourceCollection) {
      return data.resources()
          .findAll { relationshipNames(it).contains(relationshipName) }
          .every { linkageIsCollection(it, relationshipName) }
    }
    return false
  }

  private static boolean linkageIsCollection(ResourceObject resource, String relationshipName) {
    if (resource.relationships() == null) {
      return false
    }
    def relationship = resource.relationships().relationships().get(relationshipName)
    return relationship != null && relationship.data() instanceof RelationshipData.IdentifierCollectionLinkage
  }

  private static Set<String> relationshipNames(ResourceObject resource) {
    if (resource.relationships() == null) {
      return [] as Set
    }
    return resource.relationships().relationships().keySet() as Set
  }
}
