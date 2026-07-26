package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.internal.OpenJsonValues
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class JsonApiDocumentSpec extends Specification {

  def "top-level data and errors cannot coexist"() {
    when:
    new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
        [ErrorObject.ofTitle("fail")],
        null, null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DATA_ERRORS_COEXIST
  }

  def "included requires data member"() {
    when:
    new JsonApiDocument(null, null, null, null, null, [
      ResourceObject.of("articles", "1")
    ], [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INCLUDED_WITHOUT_DATA
  }

  def "document requires at least one top-level member"() {
    when:
    new JsonApiDocument(null, null, null, null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_TOP_LEVEL_MEMBER
  }

  def "at-only additional members do not satisfy top-level member requirement"() {
    when:
    new JsonApiDocument(null, null, null, null, null, null, ["@context": "https://example.com"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_TOP_LEVEL_MEMBER
  }

  def "namespaced extension member satisfies top-level member requirement"() {
    when:
    def doc = new JsonApiDocument(null, null, null, null, null, null, ["myext:version": "1"])

    then:
    doc.additionalMembers()["myext:version"] == "1"
  }

  def "reserved top-level names rejected in additional members"() {
    when:
    new JsonApiDocument(null, null, Meta.of([x: 1]), null, null, null, [data: "x"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "error object requires at least one standard member"() {
    when:
    new ErrorObject(null, null, null, null, null, null, null, null, ["@ignored": "v"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_ERROR_MEMBER
  }

  def "error object rejects reserved additional member names"() {
    when:
    new ErrorObject(null, null, null, null, "Title", null, null, null, [title: "dup"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "meta and open json values are defensively copied"() {
    given:
    def nested = [items: ["a"]]
    def meta = Meta.of([nested: nested])
    nested.items << "b"

    expect:
    ((List) ((Map) meta.members().nested).items).size() == 1
    !OpenJsonValues.isValid(Double.POSITIVE_INFINITY)
  }

  def "meta preserves explicit null values and encounter order"() {
    when:
    def meta = Meta.of([z: 1, missing: null, a: 2])

    then:
    meta.members().containsKey("missing")
    meta.members().missing == null
    meta.members().keySet().toList() == ["z", "missing", "a"]
  }

  def "document collections are defensively copied"() {
    given:
    def errors = [ErrorObject.ofTitle("fail")]
    def included = [
      ResourceObject.of("comments", "1")
    ]
    def article = new ResourceObject(
        "articles", "1", null, null,
        Relationships.ofRelationships([
          comments: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("comments", "1")))
        ]),
        null, null, [:])
    def data = new DocumentData.SingleResource(article)
    def doc = new JsonApiDocument(data, null, null, null, null, included, [:])
    def errorDoc = JsonApiDocument.withErrors(errors)

    when:
    included << ResourceObject.of("comments", "2")
    errors << ErrorObject.ofTitle("other")

    then:
    doc.included().size() == 1
    errorDoc.errors().size() == 1
  }

  def "null error and included elements are rejected"() {
    when:
    JsonApiDocument.withErrors([
      ErrorObject.ofTitle("fail"),
      null
    ])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.NULL_COLLECTION_ELEMENT
    ex.jsonPointer() == "/errors/1"

    when:
    new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
        null, null, null, null,
        [null],
        [:])

    then:
    def ex2 = thrown(JsonApiValidationException)
    ex2.ruleCode() == ValidationRuleCode.NULL_COLLECTION_ELEMENT
    ex2.jsonPointer() == "/included/0"
  }

  def "error source preserves additional members"() {
    when:
    def source = new ErrorSource(null, "include", null, ["ext:info": 1, "@ctx": "x"])

    then:
    source.additionalMembers()["ext:info"] == 1
    source.additionalMembers()["@ctx"] == "x"
  }
}
