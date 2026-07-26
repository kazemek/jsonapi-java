package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class AttributesRelationshipsSpec extends Specification {

  def "present-empty attributes differs from absent attributes"() {
    given:
    def withEmpty = new ResourceObject("articles", "1", null, Attributes.empty(), null, null, null, [:])
    def withAbsent = ResourceObject.of("articles", "1")

    expect:
    withEmpty.attributes() != null
    withEmpty.attributes().isEmpty()
    withAbsent.attributes() == null
  }

  def "extension and at members in attributes are pass-through"() {
    when:
    def attrs = Attributes.of([title: "T"], ["@context": "http://example.com", "ext:name": "v"])

    then:
    attrs.attributes() == [title: "T"]
    attrs.additionalMembers().containsKey("@context")
    attrs.additionalMembers().containsKey("ext:name")
  }

  def "at member cannot be an attribute name"() {
    when:
    Attributes.ofAttributes(["@illegal": "v"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "reserved field names are rejected in attributes"() {
    when:
    Attributes.ofAttributes([type: "articles"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "attribute and relationship name collision is rejected"() {
    when:
    new ResourceObject(
        "articles", "1", null,
        Attributes.ofAttributes([author: "a"]),
        Relationships.ofRelationships([author: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9")))]),
        null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MEMBER_NAME_COLLISION
  }

  def "flatten rejects collisions between groups"() {
    when:
    Attributes.of([title: "T"], [title: "duplicate"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MEMBER_NAME_COLLISION
  }

  def "present-empty relationships differs from absent relationships"() {
    given:
    def withEmpty = new ResourceObject("articles", "1", null, null, Relationships.empty(), null, null, [:])
    def withAbsent = ResourceObject.of("articles", "1")

    expect:
    withEmpty.relationships() != null
    withEmpty.relationships().isEmpty()
    withAbsent.relationships() == null
  }

  def "extension and at members in relationships are pass-through"() {
    when:
    def rels = Relationships.of([:], ["@context": "http://example.com", "ext:name": "v"])

    then:
    rels.additionalMembers().containsKey("@context")
    rels.additionalMembers().containsKey("ext:name")
  }

  def "extension and at members in links are pass-through"() {
    when:
    def links = Links.of([:], ["@context": "http://example.com", "ext:name": "v"])

    then:
    links.additionalMembers().containsKey("@context")
    links.additionalMembers().containsKey("ext:name")
  }

  def "attributes and links preserve explicit null values on flatten"() {
    when:
    def attrs = Attributes.ofAttributes([title: null, body: "x"])
    def links = Links.ofLinks([next: null, self: new Link.StringLink("http://example.com")])

    then:
    attrs.flatten().containsKey("title")
    attrs.flatten().title == null
    links.flatten().containsKey("next")
    links.flatten().next == null
  }

  def "resource identifier rejects reserved additional member names"() {
    when:
    new ResourceIdentifier("articles", "1", null, null, [type: "x"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "identity keys distinguish id values that look like lid prefixes"() {
    expect:
    ResourceIdentifier.of("articles", "lid:temp").identityKey() !=
        ResourceIdentifier.withLid("articles", "temp").identityKey()
  }
}
