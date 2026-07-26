package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class ResourceObjectSpec extends Specification {

  def "resource identifier requires type and id or lid"() {
    when:
    ResourceIdentifier.of("articles", "1")

    then:
    noExceptionThrown()

    when:
    ResourceIdentifier.withLid("articles", "local-1")

    then:
    noExceptionThrown()

    when:
    new ResourceIdentifier("articles", null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_ID
  }

  def "resource object permits type-only construction for create context"() {
    when:
    def resource = ResourceObject.ofType("articles")

    then:
    resource.type() == "articles"
    !resource.hasId()
    !resource.hasLid()
  }

  def "invalid resource type fails with stable code"() {
    when:
    ResourceObject.of("_bad", "1")

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_MEMBER_NAME
  }

  def "empty and ascii-space resource types fail member-name grammar"() {
    when:
    new ResourceObject(" ", "1", null, null, null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_MEMBER_NAME

    when:
    new ResourceObject("", "1", null, null, null, null, null, [:])

    then:
    def ex2 = thrown(JsonApiValidationException)
    ex2.ruleCode() == ValidationRuleCode.INVALID_MEMBER_NAME
  }

  def "unicode whitespace resource type is accepted when member-name grammar allows it"() {
    when:
    def resource = new ResourceObject("\u2003", "1", null, null, null, null, null, [:])
    def identifier = new ResourceIdentifier("\u2003", "1", null, null, [:])

    then:
    resource.type() == "\u2003"
    identifier.type() == "\u2003"
  }

  def "null resource type fails as missing"() {
    when:
    new ResourceObject(null, "1", null, null, null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_TYPE
  }
}
