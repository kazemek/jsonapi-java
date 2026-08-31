package io.github.kazemek.jsonapi.testsupport.domainpatch

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.MappingLocation
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.SourceLocation
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticlePatch
import spock.lang.Specification

class PatchDtoVerifierSpec extends Specification {

  def "success compares identity and PatchPresence members"() {
    given:
    def title = PatchPresence.present("Hello")
    def scenario = new PatchDtoScenario(
        "ok",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.success("1", [title: title]))
    def dto = new ArticlePatch("1", title, PatchPresence.omitted(), PatchPresence.omitted(), PatchPresence.omitted())

    when:
    PatchDtoVerifier.verify(scenario, dto)

    then:
    noExceptionThrown()
  }

  def "reader and binder failures compare code, pointer, and diagnostic"() {
    given:
    def readerScenario = new PatchDtoScenario(
        "reader",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.readerFailure(ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"))
    def binderScenario = new PatchDtoScenario(
        "binder",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"))

    when:
    PatchDtoVerifier.verify(
        readerScenario,
        new JsonApiDocumentReadException(
        CodecFailureCategory.AGGREGATE_VALIDATION,
        "/data",
        SourceLocation.UNKNOWN,
        ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE,
        "update"))
    PatchDtoVerifier.verify(
        binderScenario,
        new JsonApiMappingException(
        MappingDiagnostic.RESOURCE_TYPE_MISMATCH, ArticlePatch, MappingLocation.parse("/type")))

    then:
    noExceptionThrown()
  }

  def "wrong result type or member mismatch fails"() {
    given:
    def scenario = new PatchDtoScenario(
        "wrong",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.success("1", [title: PatchPresence.present("Hello")]))

    when:
    PatchDtoVerifier.verify(scenario, "not a dto")

    then:
    thrown(AssertionError)

    when:
    PatchDtoVerifier.verify(scenario, null)

    then:
    thrown(AssertionError)

    when:
    PatchDtoVerifier.verify(
        scenario,
        new ArticlePatch(
        "1",
        PatchPresence.present("Other"),
        PatchPresence.omitted(),
        PatchPresence.omitted(),
        PatchPresence.omitted()))

    then:
    thrown(AssertionError)

    when:
    PatchDtoVerifier.verify(
        scenario,
        new ArticlePatch(
        "2",
        PatchPresence.present("Hello"),
        PatchPresence.omitted(),
        PatchPresence.omitted(),
        PatchPresence.omitted()))

    then:
    thrown(AssertionError)
  }

  def "missing DTO accessor fails closed"() {
    given:
    def dto = new ArticlePatch(
        "1",
        PatchPresence.omitted(),
        PatchPresence.omitted(),
        PatchPresence.omitted(),
        PatchPresence.omitted())

    when:
    PatchDtoVerifier.readMember(dto, "noSuchMember")

    then:
    thrown(AssertionError)
  }

  def "reader and binder failures reject the wrong exception type"() {
    given:
    def readerScenario = new PatchDtoScenario(
        "reader-wrong",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.readerFailure(ValidationRuleCode.UPDATE_REQUIRES_SINGLE_RESOURCE, "/data"))
    def binderScenario = new PatchDtoScenario(
        "binder-wrong",
        "{}",
        ArticlePatch,
        null,
        PatchDtoExpectation.binderFailure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type"))

    when:
    PatchDtoVerifier.verify(readerScenario, "nope")

    then:
    thrown(AssertionError)

    when:
    PatchDtoVerifier.verify(binderScenario, "nope")

    then:
    thrown(AssertionError)
  }
}
