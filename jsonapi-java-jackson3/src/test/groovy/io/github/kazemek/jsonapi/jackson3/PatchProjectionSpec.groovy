package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchCommand
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticlePatchDuplicateId
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatClassPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatRawPatchPresence
import io.github.kazemek.jsonapi.testfixtures.JsonApiFixtures
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.FlatArticlePatch
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.FlatArticleTitleOnlyPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionExpectation
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionScenario
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionScenarios
import io.github.kazemek.jsonapi.testfixtures.domainpatch.PatchDocuments
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.JavaType
import tools.jackson.databind.json.JsonMapper

class PatchProjectionSpec extends Specification {

  @Shared
  List<String> executedScenarioIds = []

  def cleanupSpec() {
    assert executedScenarioIds == PatchProjectionScenarios.all()*.id
  }

  def "shared patch projection catalog scenario: #scenario.id"() {
    given:
    executedScenarioIds.add(scenario.id())
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())

    when:
    def result = execute(scenario, reader, projector)

    then:
    assertExpectation(scenario, result)

    where:
    scenario << JsonApiFixtures.patchProjection().all()
  }

  def "patchProjector builder factory matches mapper factory"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def json = PatchDocuments.ARTICLE_TITLE_HELLO
    def command = reader.readValue(json, FlatArticle)

    when:
    def patch = projector.project(command, FlatArticleTitleOnlyPatch)

    then:
    patch == new FlatArticleTitleOnlyPatch(PatchPresence.present("Hello"))
  }

  def "JavaType overload projects patch DTO"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def baseMapper = JsonMapper.builder().build()
    def projector = JsonApiJackson3.patchProjector(baseMapper)
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)
    JavaType patchType = baseMapper.constructType(FlatArticleTitleOnlyPatch)

    when:
    def patch = projector.project(command, patchType)

    then:
    patch instanceof FlatArticleTitleOnlyPatch
    patch == new FlatArticleTitleOnlyPatch(PatchPresence.present("Hello"))
  }

  def "patchProjector does not mutate caller mapper"() {
    given:
    def callerMapper = JsonMapper.builder().build()
    def fingerprint = callerMapper.serializationConfig().hashCode()
    def reader = JsonApiJackson3.patchReader(callerMapper)
    def projector = JsonApiJackson3.patchProjector(callerMapper)
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)

    when:
    projector.project(command, FlatArticleTitleOnlyPatch)

    then:
    callerMapper.serializationConfig().hashCode() == fingerprint
    JsonApiJackson3.patchProjector(callerMapper) !== projector
  }

  def "attribute change against relationship-only patch property fails role check"() {
    given:
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = new PatchCommand<>(
        FlatArticle,
        "1",
        [
          new PatchChange.AttributeChange("author", "author", "wrong-role")
        ])

    when:
    projector.project(command, FlatArticlePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE
    ex.propertyPath() == "/author"
  }

  def "duplicate supplied changes fail during projection indexing"() {
    given:
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = new PatchCommand<>(
        FlatArticle,
        "1",
        [
          new PatchChange.AttributeChange("title", "title", "a"),
          new PatchChange.AttributeChange("title", "title", "b")
        ])

    when:
    projector.project(command, FlatArticleTitleOnlyPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE
    ex.propertyPath() == "/title"
  }

  def "raw PatchPresence property type is rejected"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)

    when:
    projector.project(command, FlatRawPatchPresence)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/title"
  }

  def "duplicate identifier on patch DTO fails during mapping resolution"() {
    when:
    JsonApiJackson3.patchProjector(JsonMapper.builder().build())
        .project(
        new PatchCommand<>(FlatArticle, "1", []),
        FlatArticlePatchDuplicateId)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.PATCH_IDENTIFIER_NOT_SUPPORTED
    ex.propertyPath() == "/firstId"
  }

  def "non-record patch DTO type is rejected"() {
    given:
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = new PatchCommand<>(FlatArticle, "1", [])

    when:
    projector.project(command, FlatClassPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
  }

  private static Object execute(
      PatchProjectionScenario scenario,
      JsonApiPatchReader reader,
      JsonApiPatchProjector projector) {
    try {
      PatchCommand<?> command = reader.readValue(scenario.documentJson(), scenario.commandTargetType())
      return projector.project(command, scenario.patchTargetType())
    } catch (JsonApiMappingException ex) {
      return ex
    }
  }

  private static void assertExpectation(PatchProjectionScenario scenario, Object result) {
    def expectation = scenario.expectation()
    if (expectation instanceof PatchProjectionExpectation.Success) {
      assert result == expectation.expectedPatch()
      return
    }
    if (expectation instanceof PatchProjectionExpectation.ProjectorFailure) {
      assert result instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) result
      assert ex.diagnostic() == expectation.diagnostic()
      assert ex.propertyPath() == expectation.propertyPath()
      return
    }
    throw new IllegalArgumentException("Unknown expectation: " + expectation)
  }
}
