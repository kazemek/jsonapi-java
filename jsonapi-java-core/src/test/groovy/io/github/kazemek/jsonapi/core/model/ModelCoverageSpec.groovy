package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.internal.OpenJsonValues
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class ModelCoverageSpec extends Specification {

  def "error source record holds pointer parameter and header"() {
    when:
    def source = new ErrorSource("/data", "include", "Authorization")

    then:
    source.pointer() == "/data"
    source.parameter() == "include"
    source.header() == "Authorization"
  }

  def "resource identifier lid identity and meta are retained"() {
    when:
    def id = new ResourceIdentifier("articles", null, "local-1", Meta.of([a: 1]),
    ["ext:x": "y"])

    then:
    id.hasLid()
    !id.hasId()
    id.identityKey() == "lid:articles:local-1"
    id.meta().members().a == 1
    id.additionalMembers()["ext:x"] == "y"
  }

  def "resource object lid identity and toIdentifier"() {
    when:
    def resource = new ResourceObject("articles", null, "a1", Attributes.empty(),
        Relationships.empty(), Links.empty(), Meta.empty(), [:])

    then:
    resource.hasLid()
    resource.identityKey() == "lid:articles:a1"
    resource.toIdentifier().lid() == "a1"
    resource.attributes().isEmpty()
    resource.relationships().isEmpty()
    resource.links().isEmpty()
  }

  def "relationship factories and equality"() {
    given:
    def data = Relationship.withData(RelationshipData.NullLinkage.INSTANCE)
    def same = Relationship.withData(RelationshipData.NullLinkage.INSTANCE)

    expect:
    data.hasDataMember()
    data == same
    data.hashCode() == same.hashCode()
  }

  def "jsonapi object ofVersion and additional members"() {
    when:
    def obj = new JsonApiObject("1.1", null, null, Meta.of([x: 1]), ["ext:a": 1])

    then:
    JsonApiObject.ofVersion("1.1").version() == "1.1"
    obj.meta().members().x == 1
    obj.additionalMembers()["ext:a"] == 1
  }

  def "error object with source and meta"() {
    when:
    def error = new ErrorObject("1", null, "400", "bad", "Title", "Detail",
        new ErrorSource("/data", null, null), Meta.of([n: 1]), [:])

    then:
    error.id() == "1"
    error.source().pointer() == "/data"
    error.meta().members().n == 1
  }

  def "links flatten empty and standard members by context"() {
    expect:
    Links.empty().isEmpty()
    Links.empty().flatten().isEmpty()
    Links.standardMembers(LinksContext.TOP_LEVEL).contains("self")
    Links.standardMembers(LinksContext.RESOURCE).contains("self")
    Links.standardMembers(LinksContext.RELATIONSHIP).contains("related")
    Links.standardMembers(LinksContext.ERROR).contains("about")
    Links.ofLinks([self: new Link.StringLink("http://example.com")]).hasStandardMember("self", LinksContext.RESOURCE)
  }

  def "attributes and relationships flatten and equality"() {
    given:
    def a1 = Attributes.ofAttributes([title: "t"])
    def a2 = Attributes.ofAttributes([title: "t"])
    def r1 = Relationships.ofRelationships([:])
    def r2 = Relationships.empty()

    expect:
    a1 == a2
    a1.flatten().title == "t"
    r1 == r2
    r1.flatten().isEmpty()
  }

  def "meta equality and empty"() {
    expect:
    Meta.empty().isEmpty()
    Meta.of([a: 1]) == Meta.of([a: 1])
    Meta.of([a: 1]).toString().contains("a")
  }

  def "object link ofHref and describedby validation"() {
    when:
    def link = Link.ObjectLink.ofHref("http://example.com")

    then:
    link.href() == "http://example.com"

    when:
    new Link.ObjectLink("http://example.com", null, "bad href", null, null, null, null)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_URI_REFERENCE
  }

  def "open json helpers cover maps lists and invalid keys"() {
    expect:
    OpenJsonValues.isValid(null)
    OpenJsonValues.isValid("s")
    OpenJsonValues.isValid(true)
    OpenJsonValues.isValid(1)
    OpenJsonValues.isValid([1, "a"])
    OpenJsonValues.isValid([k: 1])
    !OpenJsonValues.isValid(Double.NaN)
    !OpenJsonValues.isValid([(1): "x"])
    OpenJsonValues.copyMap([a: 1]).a == 1
    OpenJsonValues.copyStringList(["a", "b"]) == ["a", "b"]
    OpenJsonValues.copyStringList(null).isEmpty()
    OpenJsonValues.copyMap(null).isEmpty()

    when:
    OpenJsonValues.copy([(1): "x"], "/m")

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_OPEN_JSON_VALUE

    when:
    OpenJsonValues.copy(Float.NEGATIVE_INFINITY, "/m")

    then:
    thrown(JsonApiValidationException)
  }

  def "document with identifier primary data helpers"() {
    given:
    def doc = JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1")))
    def collection = JsonApiDocument.withData(
        new DocumentData.IdentifierCollection([
          ResourceIdentifier.of("articles", "1")
        ]))
    def resources = JsonApiDocument.withData(
        new DocumentData.ResourceCollection([
          ResourceObject.of("articles", "1")
        ]))

    expect:
    doc.hasDataMember()
    !doc.hasErrorsMember()
    !doc.hasIncludedMember()
    collection.data() instanceof DocumentData.IdentifierCollection
    resources.data() instanceof DocumentData.ResourceCollection
    JsonApiDocument.withMeta(Meta.of([c: 1])).meta().members().c == 1
  }

  def "invalid additional member names fail"() {
    when:
    new ResourceObject("articles", "1", null, null, null, null, null, ["_bad": 1])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_MEMBER_NAME
  }

  def "reserved relationship names fail"() {
    when:
    Relationships.ofRelationships([type: Relationship.metaOnly(Meta.of([x: 1]))])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }
}
