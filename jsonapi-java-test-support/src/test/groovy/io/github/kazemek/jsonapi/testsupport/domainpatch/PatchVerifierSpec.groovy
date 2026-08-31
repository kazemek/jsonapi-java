package io.github.kazemek.jsonapi.testsupport.domainpatch

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.MappingLocation
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchCommand
import io.github.kazemek.jsonapi.jackson.SourceLocation
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithArray
import spock.lang.Specification

class PatchVerifierSpec extends Specification {

  def "success compares identity, type, and array-valued relationship changes by content"() {
    given:
    def identifiers = [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ] as ResourceIdentifier[]
    def expected = new PatchChange.RelationshipChange("comments", "comments", identifiers)
    def actualIdentifiers = [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ] as ResourceIdentifier[]
    def actual = new PatchChange.RelationshipChange("comments", "comments", actualIdentifiers)
    def scenario = new PatchScenario(
        "array-linkage",
        "{}",
        FlatArticleWithArray,
        null,
        PatchExpectation.success("1", [expected]))
    def command = new PatchCommand(FlatArticleWithArray, "1", [actual])

    when:
    PatchVerifier.verify(scenario, command)

    then:
    noExceptionThrown()
    PatchVerifier.valuesEqual(identifiers, actualIdentifiers)
    !PatchVerifier.valuesEqual(identifiers, [
      ResourceIdentifier.of("comments", "c1")
    ] as ResourceIdentifier[])
  }

  def "list-valued changes still compare with ordinary equality"() {
    given:
    def change = new PatchChange.RelationshipChange(
        "comments", "comments", [
          ResourceIdentifier.of("comments", "c1")
        ])
    def scenario = new PatchScenario(
        "list-linkage",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.success("1", [change]))
    def command = new PatchCommand(FlatArticle, "1", [change])

    expect:
    PatchVerifier.changeEqual(change, change)
    PatchVerifier.verify(scenario, command)
  }

  def "reader and binder failures compare code, pointer, and diagnostic"() {
    given:
    def readerScenario = new PatchScenario(
        "reader",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.readerFailure(ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"))
    def binderScenario = new PatchScenario(
        "binder",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"))

    when:
    PatchVerifier.verify(
        readerScenario,
        new JsonApiDocumentReadException(
        CodecFailureCategory.AGGREGATE_VALIDATION,
        "/data",
        SourceLocation.UNKNOWN,
        ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE,
        "update"))
    PatchVerifier.verify(
        binderScenario,
        new JsonApiMappingException(
        MappingDiagnostic.RESOURCE_TYPE_MISMATCH, FlatArticle, MappingLocation.parse("/type")))

    then:
    noExceptionThrown()
  }

  def "wrong result type fails"() {
    given:
    def scenario = new PatchScenario(
        "wrong",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.success("1", List.of()))

    when:
    PatchVerifier.verify(scenario, null)

    then:
    thrown(AssertionError)

    when:
    PatchVerifier.verify(scenario, "not a command")

    then:
    thrown(AssertionError)
  }

  def "reader and binder failures reject the wrong exception type"() {
    given:
    def readerScenario = new PatchScenario(
        "reader-wrong",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.readerFailure(ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"))
    def binderScenario = new PatchScenario(
        "binder-wrong",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"))

    when:
    PatchVerifier.verify(readerScenario, "not a reader failure")

    then:
    thrown(AssertionError)

    when:
    PatchVerifier.verify(binderScenario, "not a binder failure")

    then:
    thrown(AssertionError)
  }

  def "success rejects a change-count or change-content mismatch"() {
    given:
    def expected = new PatchChange.AttributeChange("title", "title", "Hello")
    def extra = new PatchChange.AttributeChange("body", "body", "B")
    def scenario = new PatchScenario(
        "changes",
        "{}",
        FlatArticle,
        null,
        PatchExpectation.success("1", [expected]))
    def tooMany = new PatchCommand(FlatArticle, "1", [expected, extra])
    def different = new PatchCommand(
        FlatArticle, "1", [
          new PatchChange.AttributeChange("title", "title", "Other")
        ])

    when:
    PatchVerifier.verify(scenario, tooMany)

    then:
    thrown(AssertionError)

    when:
    PatchVerifier.verify(scenario, different)

    then:
    thrown(AssertionError)
  }

  def "valuesEqual compares primitive arrays and treats one-sided null as unequal"() {
    expect:
    PatchVerifier.valuesEqual([1, 2] as int[], [1, 2] as int[])
    !PatchVerifier.valuesEqual([1, 2] as int[], [1, 3] as int[])
    !PatchVerifier.valuesEqual([1, 2] as int[], [1] as int[])
    !PatchVerifier.valuesEqual("a", null)
    !PatchVerifier.valuesEqual(null, "a")
    !PatchVerifier.changeEqual(
        new PatchChange.AttributeChange("title", "title", "A"),
        new PatchChange.RelationshipChange("author", "author", null))
  }
}
