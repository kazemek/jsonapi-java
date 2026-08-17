package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchCommand
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson3.testmodel.CountedThingPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticlePatchDuplicateId
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatClassPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatRawPatchPresence
import io.github.kazemek.jsonapi.jackson3.testmodel.GenericTitlePatch
import io.github.kazemek.jsonapi.jackson3.testmodel.OmittedTitlePatch
import io.github.kazemek.jsonapi.jackson3.testmodel.OptionalIntegerTitlePatch
import io.github.kazemek.jsonapi.jackson3.testmodel.OptionalTitleArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.OptionalTitlePatch
import io.github.kazemek.jsonapi.jackson3.testmodel.PresentTitlePatch
import io.github.kazemek.jsonapi.jackson3.testmodel.TitleAndSubtitlePatch
import io.github.kazemek.jsonapi.testfixtures.JsonApiFixtures
import io.github.kazemek.jsonapi.testfixtures.domainpatch.PatchDocuments
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.FlatArticleBodyTextPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.FlatArticlePatch
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.FlatArticleTitleOnlyPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionExpectation
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionScenario
import io.github.kazemek.jsonapi.testfixtures.domainpatchprojection.PatchProjectionScenarios
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatCountedThing
import java.util.Optional
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
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

  def "snake_case property naming still constructs records from Java component names"() {
    given:
    def namingMapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchReader(namingMapper)
    def projector = JsonApiJackson3.patchProjector(namingMapper)
    def command = reader.readValue(PatchDocuments.ARTICLE_BODY_TEXT_CONTENT, FlatArticle)

    when:
    def patch = projector.project(command, FlatArticleBodyTextPatch)

    then:
    patch == new FlatArticleBodyTextPatch(PatchPresence.present("Content"))
  }

  def "parameterized JavaType preserves PatchPresence value bindings"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def mapper = JsonMapper.builder().build()
    def projector = JsonApiJackson3.patchProjector(mapper)
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)
    JavaType stringPatch =
        mapper.getTypeFactory().constructParametricType(GenericTitlePatch, String)
    JavaType integerPatch =
        mapper.getTypeFactory().constructParametricType(GenericTitlePatch, Integer)

    when:
    def compatible = projector.project(command, stringPatch)

    then:
    compatible instanceof GenericTitlePatch
    compatible.title() == PatchPresence.present("Hello")

    when:
    projector.project(command, integerPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/title"
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

  def "relationship change against attribute-only patch property fails role check"() {
    given:
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = new PatchCommand<>(
        FlatArticle,
        "1",
        [
          new PatchChange.RelationshipChange("title", "title", null)
        ])

    when:
    projector.project(command, FlatArticleTitleOnlyPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE
    ex.propertyPath() == "/title"
  }

  def "patch DTO members absent from the command mapping stay omitted"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)

    when:
    def patch = projector.project(command, TitleAndSubtitlePatch)

    then:
    patch == new TitleAndSubtitlePatch(PatchPresence.present("Hello"), PatchPresence.omitted())
  }

  def "Optional attribute value types are compared recursively"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, OptionalTitleArticle)

    when:
    def compatible = projector.project(command, OptionalTitlePatch)

    then:
    compatible == new OptionalTitlePatch(PatchPresence.present(Optional.of("Hello")))

    when:
    projector.project(command, OptionalIntegerTitlePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/title"
  }

  def "primitive command attributes are compatible with boxed PatchPresence value types"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = reader.readValue(
        '{"data":{"type":"things","id":"1","attributes":{"count":3}}}',
        FlatCountedThing)

    when:
    def patch = projector.project(command, CountedThingPatch)

    then:
    patch == new CountedThingPatch(PatchPresence.present(3))
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

  def "Present and Omitted property declarations are rejected"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def projector = JsonApiJackson3.patchProjector(JsonMapper.builder().build())
    def command = reader.readValue(PatchDocuments.ARTICLE_TITLE_HELLO, FlatArticle)

    when:
    projector.project(command, patchType)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/title"

    where:
    patchType << [
      PresentTitlePatch,
      OmittedTitlePatch
    ]
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
