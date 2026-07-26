package io.github.kazemek.jsonapi.core.validation

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.ErrorObject
import io.github.kazemek.jsonapi.core.model.ErrorSource
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import spock.lang.Specification

class ValidatorCoverageSpec extends Specification {

  def validator = new JsonApiDocumentValidator()

  def "validates null data resource collections and identifier collections"() {
    given:
    def nullDoc = JsonApiDocument.withData(DocumentData.NullData.INSTANCE)
    def collection = JsonApiDocument.withData(new DocumentData.ResourceCollection([
      ResourceObject.of("articles", "1"),
      ResourceObject.of("articles", "2")
    ]))
    def ids = JsonApiDocument.withData(new DocumentData.IdentifierCollection([
      ResourceIdentifier.of("articles", "1")
    ]))
    def singleId = JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1")))

    when:
    validator.validate(nullDoc, ValidationContext.defaults())
    validator.validate(collection, ValidationContext.defaults())
    validator.validate(ids, ValidationContext.defaults())
    validator.validate(singleId, ValidationContext.defaults())

    then:
    noExceptionThrown()
  }

  def "validates nested meta extension members and relationship meta"() {
    given:
    def article = new ResourceObject(
        "articles", "1", null,
        Attributes.of([title: "t"], ["ext:a": 1]),
        Relationships.of([
          author: new Relationship(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9")),
          null,
          Meta.of(["ext:b": 1]),
          [:])
        ], ["ext:c": 1]),
        Links.ofLinks([self: new Link.StringLink("http://example.com/a/1")]),
        Meta.of(["ext:d": 1]),
        ["ext:e": 1])
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        Meta.of(["ext:top": 1]),
        JsonApiObject.ofVersion("1.1"),
        Links.ofLinks([self: new Link.StringLink("http://example.com")]),
        null,
        ["ext:doc": 1])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("ext"),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "rejects disallowed meta extension namespace"() {
    given:
    def doc = JsonApiDocument.withMeta(Meta.of(["ext:x": 1]))

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER
  }

  def "validates error with meta and nested additional members"() {
    given:
    def error = new ErrorObject(
        null, null, null, null, "t", null,
        new ErrorSource(null, "include", null),
        Meta.of([n: 1]),
        ["ext:e": 1])
    def doc = JsonApiDocument.withErrors([error])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("ext"),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "identifier collection linkage is validated"() {
    given:
    def article = new ResourceObject(
        "articles", "1", null, null,
        Relationships.ofRelationships([
          tags: Relationship.withData(new RelationshipData.IdentifierCollectionLinkage([
            ResourceIdentifier.of("tags", "1")
          ]))
        ]),
        null, null, [:])
    def doc = JsonApiDocument.withData(new DocumentData.SingleResource(article))

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    noExceptionThrown()
  }

  def "allowed profile uri passes when configured"() {
    given:
    def uri = "https://example.com/profiles/a"
    def doc = new JsonApiDocument(
        null, null, Meta.empty(),
        new JsonApiObject("1.1", null, [uri], null, [:]),
        null, null, [:])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of(uri),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "at members are accepted without profile policy"() {
    given:
    def doc = new JsonApiDocument(null, null, Meta.of([c: 1]), null, null, null, ["@context": "x"])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    noExceptionThrown()
  }
}
