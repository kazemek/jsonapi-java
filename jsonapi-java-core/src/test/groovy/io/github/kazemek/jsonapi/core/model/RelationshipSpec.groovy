package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class RelationshipSpec extends Specification {

  def "relationship supports absent, null, single, and collection linkage"() {
    given:
    def absent = Relationship.linkOnly(Links.ofLinks([self: new Link.StringLink("http://example.com")]))
    def explicitNull = Relationship.withData(RelationshipData.NullLinkage.INSTANCE)
    def single = Relationship.withData(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9")))
    def collection = Relationship.withData(
        RelationshipData.IdentifierCollectionLinkage.empty())

    expect:
    !absent.hasDataMember()
    explicitNull.hasDataMember()
    explicitNull.data() instanceof RelationshipData.NullLinkage
    single.data() instanceof RelationshipData.SingleLinkage
    collection.data() instanceof RelationshipData.IdentifierCollectionLinkage
  }

  def "link-only and meta-only relationships are valid"() {
    when:
    def linkOnly = Relationship.linkOnly(Links.ofLinks([
      related: new Link.StringLink("http://example.com/comments")
    ]))
    def metaOnly = Relationship.metaOnly(Meta.of([total: 0]))

    then:
    !linkOnly.hasDataMember()
    linkOnly.links() != null
    !metaOnly.hasDataMember()
    metaOnly.meta() != null
  }

  def "empty relationship is rejected"() {
    when:
    new Relationship(null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_RELATIONSHIP_MEMBER
  }

  def "single linkage rejects null identifier"() {
    when:
    new RelationshipData.SingleLinkage(null)

    then:
    thrown(NullPointerException)
  }
}
