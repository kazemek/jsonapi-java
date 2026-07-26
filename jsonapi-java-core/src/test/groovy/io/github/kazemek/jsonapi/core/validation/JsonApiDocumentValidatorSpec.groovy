package io.github.kazemek.jsonapi.core.validation

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.ErrorObject
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

class JsonApiDocumentValidatorSpec extends Specification {

  def validator = new JsonApiDocumentValidator()

  def "create request permits resource without id"() {
    given:
    def doc = JsonApiDocument.withData(
        new DocumentData.SingleResource(ResourceObject.ofType("articles")))
    def context = ValidationContext.defaults().withDocumentUsage(DocumentUsage.CREATE_REQUEST)

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "response requires resource id"() {
    given:
    def doc = JsonApiDocument.withData(
        new DocumentData.SingleResource(ResourceObject.ofType("articles")))

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESOURCE_ID_REQUIRED
    ex.jsonPointer() == "/data/id"
  }

  def "duplicate included resources are rejected"() {
    given:
    def article = ResourceObject.of("articles", "1")
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [
          article,
          ResourceObject.of("articles", "1")
        ],
        [:])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DUPLICATE_INCLUDED_RESOURCE
  }

  def "full linkage is enforced for included resources"() {
    given:
    def article = ResourceObject.of("articles", "1")
    def orphan = ResourceObject.of("comments", "99")
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [orphan],
        [:])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.FULL_LINKAGE_VIOLATION
  }

  def "sparse fieldset exception skips full linkage"() {
    given:
    def article = ResourceObject.of("articles", "1")
    def orphan = ResourceObject.of("comments", "99")
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [orphan],
        [:])
    def context = ValidationContext.defaults().withSparseFieldsetException(true)

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "extension members require allowed namespace"() {
    given:
    def doc = new JsonApiDocument(
        null, null, null, null, null, null,
        ["myext:version": "1.0"])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER
  }

  def "allowed extension namespace passes validation"() {
    given:
    def doc = new JsonApiDocument(
        null, null, null, null, null, null,
        ["myext:version": "1.0"])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("myext"),
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

  def "relationship pagination requires cardinality hint"() {
    given:
    def article = new ResourceObject(
        "articles", "1", null, null,
        Relationships.ofRelationships([
          comments: Relationship.linkOnly(Links.ofLinks([
            first: new Link.StringLink("http://example.com/c?page=1")
          ]))
        ]),
        null, null, [:])
    def doc = JsonApiDocument.withData(new DocumentData.SingleResource(article))

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RELATIONSHIP_PAGINATION_REQUIRES_HINT
  }

  def "relationship pagination passes with hint"() {
    given:
    def article = new ResourceObject(
        "articles", "1", null, null,
        Relationships.ofRelationships([
          comments: Relationship.linkOnly(Links.ofLinks([
            first: new Link.StringLink("http://example.com/c?page=1")
          ]))
        ]),
        null, null, [:])
    def doc = JsonApiDocument.withData(new DocumentData.SingleResource(article))
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of("comments", true))

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "inconsistent local identifiers are rejected"() {
    given:
    def primary = new ResourceObject(
        "articles", null, "a1", null, null, null, null, [:])
    def included1 = new ResourceObject("articles", null, "a1", Attributes.ofAttributes([t: 1]), null, null, null, [:])
    def included2 = new ResourceObject("articles", null, "a1", Attributes.ofAttributes([t: 2]), null, null, null, [:])
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(primary),
        null, null, null, null,
        [included1, included2],
        [:])
    def context = ValidationContext.defaults()
        .withDocumentUsage(DocumentUsage.CREATE_REQUEST)
        .withSparseFieldsetException(true)

    when:
    validator.validate(doc, context)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INCONSISTENT_LOCAL_IDENTIFIER
  }

  def "unknown top-level unnamespaced members are rejected"() {
    given:
    def doc = new JsonApiDocument(
        null, null, Meta.of([count: 1]), null, null, null,
        [custom: "value"])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.UNKNOWN_ADDITIONAL_MEMBER
    ex.jsonPointer() == "/custom"
  }

  def "allowed profile member names pass validation"() {
    given:
    def doc = new JsonApiDocument(
        null, null, Meta.of([count: 1]), null, null, null,
        [custom: "value"])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of(),
        Set.of("custom"),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())

    when:
    validator.validate(doc, context)

    then:
    noExceptionThrown()
  }

  def "transitive full linkage reaches included relationship targets"() {
    given:
    def author = ResourceObject.of("people", "9")
    def comment = new ResourceObject(
        "comments", "5", null, null,
        Relationships.ofRelationships([
          author: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9")))
        ]),
        null, null, [:])
    def article = new ResourceObject(
        "articles", "1", null, null,
        Relationships.ofRelationships([
          comments: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("comments", "5")))
        ]),
        null, null, [:])
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [comment, author],
        [:])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    noExceptionThrown()
  }

  def "non-transitive orphan beyond one hop is rejected"() {
    given:
    def author = ResourceObject.of("people", "9")
    def article = ResourceObject.of("articles", "1")
    def doc = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [author],
        [:])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.FULL_LINKAGE_VIOLATION
  }

  def "resource links reject non-standard members"() {
    given:
    def article = new ResourceObject(
        "articles", "1", null, null, null,
        Links.ofLinks([related: new Link.StringLink("http://example.com")]),
        null, [:])
    def doc = JsonApiDocument.withData(new DocumentData.SingleResource(article))

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LINKS_CONTEXT
  }

  def "error links accept about and type"() {
    given:
    def error = new ErrorObject(
        null,
        Links.ofLinks([
          about: new Link.StringLink("http://example.com/docs"),
          type : new Link.StringLink("http://example.com/errors/1")
        ]),
        "400", null, "Bad Request", null, null, null, [:])
    def doc = JsonApiDocument.withErrors([error])

    when:
    validator.validate(doc, ValidationContext.defaults())

    then:
    noExceptionThrown()
  }

  def "disallowed profile uri is rejected"() {
    given:
    def doc = new JsonApiDocument(
        null, null, Meta.empty(),
        new JsonApiObject("1.1", null, [
          "https://example.com/profiles/a"
        ], null, [:]),
        null, null, [:])
    def context = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of("https://example.com/profiles/b"),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())

    when:
    validator.validate(doc, context)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER
  }
}
