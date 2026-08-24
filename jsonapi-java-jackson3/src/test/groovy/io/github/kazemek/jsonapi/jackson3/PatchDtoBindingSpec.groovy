package io.github.kazemek.jsonapi.jackson3

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.EndpointIdentity
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.jackson3.testmodel.GenericPatch
import io.github.kazemek.jsonapi.testfixtures.JsonApiFixtures
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticlePatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.OptionalPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.PatchDtoExpectation
import io.github.kazemek.jsonapi.testfixtures.domainpatch.PatchDtoScenario
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.util.Optional
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.type.TypeFactory
import tools.jackson.databind.util.Converter

class PatchDtoBindingSpec extends Specification {

  @Shared
  List<String> executedScenarioIds = []

  def cleanupSpec() {
    assert executedScenarioIds == JsonApiFixtures.patchDto().all()*.id
  }

  def "shared patch dto catalog scenario: #scenario.id"() {
    given:
    executedScenarioIds.add(scenario.id())
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())

    when:
    def result = execute(scenario, reader)

    then:
    assertExpectation(scenario, result)

    where:
    scenario << JsonApiFixtures.patchDto().all()
  }

  def "implicit id property binds without @JsonApiId"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"9","attributes":{"title":"T"}}}'

    when:
    def dto = reader.readValue(json, ImplicitIdPatch)

    then:
    dto.id == "9"
    dto.title == PatchPresence.present("T")
  }

  def "generic PATCH DTO binds through a parameterized JavaType"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def javaType = mapper.typeFactory.constructParametricType(GenericPatch, String)
    def json = '{"data":{"type":"articles","id":"42","attributes":{"title":"Hello"}}}'

    when:
    def dto = reader.readValue(json, javaType)

    then:
    dto instanceof GenericPatch
    dto.id() == "42"
    dto.title() == PatchPresence.present("Hello")
  }

  def "generic PATCH DTO binds through fromDocument with a parameterized JavaType"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def javaType = mapper.typeFactory.constructParametricType(GenericPatch, String)
    def document = decodeUpdateDocument(
        '{"data":{"type":"articles","id":"42","attributes":{"title":"Hi"}}}')

    when:
    def dto = reader.fromDocument(document, javaType)

    then:
    dto instanceof GenericPatch
    dto.id() == "42"
    dto.title() == PatchPresence.present("Hi")
  }

  def "to-many PatchPresence<List<AuthorId>> converts with a custom linkage mapper"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.IdentifierCollectionLinkage) {
        return data.identifiers().collect { id -> new AuthorId(id.type(), id.id()) }
      }
      return null
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(AuthorId): mapper])
    def nonEmpty =
        '{"data":{"type":"articles","id":"1","relationships":{"contributors":{"data":[{"type":"authors","id":"a1"},{"type":"authors","id":"a2"}]}}}}'
    def empty =
        '{"data":{"type":"articles","id":"1","relationships":{"contributors":{"data":[]}}}}'

    when:
    def dto = reader.readValue(nonEmpty, AuthorListPatch)
    def emptyDto = reader.readValue(empty, AuthorListPatch)

    then:
    dto.id == "1"
    dto.contributors ==
        PatchPresence.present([
          new AuthorId("authors", "a1"),
          new AuthorId("authors", "a2")
        ])
    emptyDto.contributors == PatchPresence.present([])
  }

  def "Optional inner relationship binds null linkage as Present(Optional.empty())"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.SingleLinkage) {
        def identifier = ((RelationshipData.SingleLinkage) data).identifier()
        return new AuthorId(identifier.type(), identifier.id())
      }
      return null
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(AuthorId): mapper])
    def json = '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":null}}}}'

    when:
    def dto = reader.readValue(json, AuthorOptionalPatch)

    then:
    dto.id == "1"
    dto.author == PatchPresence.present(Optional.empty())
  }

  def "PatchPresence<Map<String,Value>> attribute binds"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"tags":{"a":{"label":"A"}}}}}'

    when:
    def dto = reader.readValue(json, MapPatch)

    then:
    dto.id == "1"
    dto.tags == PatchPresence.present([a: new Value("A")])
  }

  def "property-level @JsonDeserialize on a PatchPresence member is rejected"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, WrapperDeserializePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/title"
  }

  def "property-level @JsonSerialize on a PatchPresence member is rejected"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, WrapperSerializePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/title"
  }

  def "inner type customization still works"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"hello"}}}'

    when:
    def dto = reader.readValue(json, LoudPatch)

    then:
    dto.id == "1"
    dto.title == PatchPresence.present(new LoudValue("HELLO"))
  }

  def "property-level @JsonDeserialize(converter) on a PatchPresence member is rejected"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, WrapperConverterDeserializePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/title"
  }

  def "property-level @JsonSerialize(converter) on a PatchPresence member is rejected"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, WrapperConverterSerializePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/title"
  }

  def "mix-in wrapper @JsonDeserialize on a PatchPresence member is rejected"() {
    given:
    def mapper = JsonMapper.builder()
        .addMixIn(MixinTargetPatch, MixInWithDeserializer)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, MixinTargetPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/title"
  }

  def "naming strategy is honored for PATCH DTO members"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"display_title":"T"}}}'

    when:
    def dto = reader.readValue(json, SnakePatch)

    then:
    dto.id == "1"
    dto.displayTitle == PatchPresence.present("T")
  }

  def "presence tri-state is invariant to UPPER_CAMEL_CASE naming"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def withNulls = '{"data":{"type":"articles","id":"1","attributes":{"Title":"T","Body":null}}}'
    def withOptional = '{"data":{"type":"articles","id":"1","attributes":{"Title":"T","Subtitle":null}}}'

    when:
    def dto = reader.readValue(withNulls, MixedPatch)
    def optionalDto = reader.readValue(withOptional, MixedPatch)

    then:
    dto.id == "1"
    dto.title == PatchPresence.present("T")
    dto.body == PatchPresence.present(null)
    dto.subtitle.isOmitted()
    optionalDto.subtitle == PatchPresence.present(Optional.empty())
  }

  def "presence tri-state is invariant to UPPER_SNAKE_CASE naming"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.UPPER_SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def withNulls = '{"data":{"type":"articles","id":"1","attributes":{"TITLE":"T","BODY":null}}}'
    def withOptional = '{"data":{"type":"articles","id":"1","attributes":{"TITLE":"T","SUBTITLE":null}}}'

    when:
    def dto = reader.readValue(withNulls, MixedPatch)
    def optionalDto = reader.readValue(withOptional, MixedPatch)

    then:
    dto.id == "1"
    dto.title == PatchPresence.present("T")
    dto.body == PatchPresence.present(null)
    dto.subtitle.isOmitted()
    optionalDto.subtitle == PatchPresence.present(Optional.empty())
  }

  def "omitted, present-value, and present-null are distinct"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def allJson =
        '{"data":{"type":"articles","id":"1","attributes":{"title":"T","subtitle":"S","body":null}}}'
    def omittedJson = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    def all = reader.readValue(allJson, MixedPatch)
    def omitted = reader.readValue(omittedJson, MixedPatch)

    then:
    all.title == PatchPresence.present("T")
    all.subtitle == PatchPresence.present(Optional.of("S"))
    all.body == PatchPresence.present(null)
    !all.body.isOmitted()
    omitted.title == PatchPresence.present("T")
    omitted.subtitle.isOmitted()
    omitted.body.isOmitted()
  }

  def "tri-state survives caller NON_ABSENT and NON_EMPTY inclusion"() {
    given:
    def nonAbsent = readerFor(JsonInclude.Include.NON_ABSENT)
    def nonEmpty = readerFor(JsonInclude.Include.NON_EMPTY)
    def withNull = '{"data":{"type":"articles","id":"1","attributes":{"title":"T","subtitle":null}}}'
    def withNullOnly = '{"data":{"type":"articles","id":"1","attributes":{"title":null}}}'

    when:
    def absentDto = nonAbsent.readValue(withNull, MixedPatch)
    def emptyDto = nonEmpty.readValue(withNull, MixedPatch)

    then:
    absentDto.title == PatchPresence.present("T")
    absentDto.subtitle == PatchPresence.present(Optional.empty())
    emptyDto.title == PatchPresence.present("T")
    emptyDto.subtitle == PatchPresence.present(Optional.empty())

    when:
    def absentNull = nonAbsent.readValue(withNullOnly, MixedPatch)
    def emptyNull = nonEmpty.readValue(withNullOnly, MixedPatch)

    then:
    absentNull.title == PatchPresence.present(null)
    absentNull.subtitle.isOmitted()
    emptyNull.title == PatchPresence.present(null)
    emptyNull.subtitle.isOmitted()
  }

  def "patchDtoReader derives a mapper and never mutates the caller mapper"() {
    given:
    def builder = JsonMapper.builder()
    def base = builder.build()
    def before = base.serializationConfig().getDefaultPropertyInclusion().getValueInclusion()
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    def reader1 = JsonApiJackson3.patchDtoReader(base)
    def reader2 = JsonApiJackson3.patchDtoReader(base)
    def dto = reader1.readValue(json, MixedPatch)

    then:
    base.serializationConfig().getDefaultPropertyInclusion().getValueInclusion() == before
    dto.title == PatchPresence.present("T")
    reader2.readValue(json, MixedPatch).title == PatchPresence.present("T")
  }

  def "unknown supplied member is reported before a known member's invalid conversion"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"bogus":"x","count":"not-an-int"}}}'

    when:
    reader.readValue(json, CountPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    ex.propertyPath() == "/attributes/bogus"
  }

  def "unknown supplied relationship names are escaped as JSON Pointer segments"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())

    when:
    reader.readValue(
        '{"data":{"type":"articles","id":"1","relationships":{"bogus":{"data":null}}}}',
        TitlePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    // Escaping itself is exercised end-to-end by the structured PATCH specs: top-level wire
    // member names are namespace-validated, so pointer-sensitive characters can only reach a
    // diagnostic location inside attribute values.
    ex.propertyPath() == "/relationships/bogus"
  }

  def "unknown supplied relationship is reported before a known relationship conversion"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"bogus":{"data":null},"author":{"data":[]}}}}'

    when:
    reader.readValue(json, ArticlePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    ex.propertyPath() == "/relationships/bogus"
  }

  def "null dtoType reports dtoType in the message"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1"}}'

    when:
    reader.readValue(json, (Class) null)

    then:
    def ex = thrown(NullPointerException)
    ex.message == "dtoType"
  }

  def "fromDocument binds an already-validated document without re-validation"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def document = decodeUpdateDocument(
        '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}')

    when:
    def dto = reader.fromDocument(document, ArticlePatch)

    then:
    dto.id == "1"
    dto.title == PatchPresence.present("Hello")
    dto.body.isOmitted()
    dto.author.isOmitted()
    dto.comments.isOmitted()
  }

  def "fromDocument skips a supplied mapped relationship without data as Omitted"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(
        new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(null, null, Meta.of([note: "x"]), Map.of())]),
        null,
        null,
        Map.of())),
        null, null, null, null, null, Map.of())

    when:
    def dto = reader.fromDocument(document, ArticlePatch)

    then:
    dto.id == "1"
    dto.author.isOmitted()
    dto.title.isOmitted()
  }

  def "byte array, stream, and parser entry points bind and leave caller sources open"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}'
    def bytes = json.bytes
    def stream = new CloseTrackingInputStream(new ByteArrayInputStream(bytes))
    def parser = mapper.createParser(bytes)

    when:
    def fromBytes = reader.readValue(bytes, ArticlePatch)
    def fromStream = reader.readValue(stream, ArticlePatch)
    def fromParser = reader.readValue(parser, ArticlePatch)

    then:
    fromBytes.title == PatchPresence.present("Hello")
    fromStream.title == PatchPresence.present("Hello")
    fromParser.title == PatchPresence.present("Hello")
    !stream.closed
    !parser.closed

    cleanup:
    parser?.close()
  }

  def "JavaType stream and parser entry points bind"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def javaType = mapper.constructType(ArticlePatch)
    def bytes = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'.bytes
    def stream = new CloseTrackingInputStream(new ByteArrayInputStream(bytes))
    def parser = mapper.createParser(bytes)

    when:
    def fromStream = reader.readValue(stream, javaType)
    def fromParser = reader.readValue(parser, javaType)

    then:
    fromStream instanceof ArticlePatch
    ((ArticlePatch) fromStream).title == PatchPresence.present("T")
    fromParser instanceof ArticlePatch
    !stream.closed
    !parser.closed

    cleanup:
    parser?.close()
  }

  def "fromDocument rejects null and non-single-resource primary data"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def metaOnly = new JsonApiDocument(
        null, null, Meta.of([note: "x"]), null, null, null, Map.of())

    when:
    reader.fromDocument(null, ArticlePatch)

    then:
    thrown(NullPointerException)

    when:
    reader.fromDocument(metaOnly, ArticlePatch)

    then:
    thrown(IllegalArgumentException)

    when:
    reader.fromDocument(
        new JsonApiDocument(DocumentData.NullData.INSTANCE, null, null, null, null, null, Map.of()),
        ArticlePatch)

    then:
    thrown(IllegalArgumentException)
  }

  def "mapper factory overloads bind successfully"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}'
    def mapper = JsonMapper.builder().build()

    when:
    def fromMapper = JsonApiJackson3.patchDtoReader(mapper).readValue(json, ArticlePatch)
    def fromJavaType = JsonApiJackson3.patchDtoReader(mapper)
        .readValue(json, mapper.constructType(ArticlePatch))

    then:
    fromMapper.title == PatchPresence.present("Hello")
    fromJavaType instanceof ArticlePatch
    ((ArticlePatch) fromJavaType).title == PatchPresence.present("Hello")
  }

  def "named IdentifierConverter is used for identity"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object value) {
            return String.valueOf(value)
          }

          @Override
          Object parse(String wire) {
            return "parsed-" + wire
          }
        }
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), converter)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    def dto = reader.readValue(json, ArticlePatch)

    then:
    dto.id == "parsed-1"
  }

  def "endpoint identity mismatch fails validation on readValue"() {
    given:
    def context = ValidationContext.defaults()
        .withExpectedEndpointIdentity(new EndpointIdentity("articles", "99"))
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build(), context)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, ArticlePatch)

    then:
    thrown(JsonApiDocumentReadException)
  }

  private static JsonApiPatchDtoReader readerFor(JsonInclude.Include include) {
    def builder = JsonMapper.builder()
    builder.changeDefaultPropertyInclusion({ value ->
      value.withValueInclusion(include).withContentInclusion(include)
    })
    JsonApiJackson3.patchDtoReader(builder.build())
  }

  private static Object execute(PatchDtoScenario scenario, JsonApiPatchDtoReader reader) {
    try {
      return reader.readValue(scenario.documentJson(), scenario.targetType())
    } catch (JsonApiDocumentReadException | JsonApiMappingException ex) {
      return ex
    }
  }

  private static void assertExpectation(PatchDtoScenario scenario, Object result) {
    def expectation = scenario.expectation()
    if (expectation instanceof PatchDtoExpectation.Success) {
      assert result != null
      assert scenario.targetType().isInstance(result)
      assert readMember(result, "id") == expectation.identity()
      expectation.members().each { name, expected ->
        assert readMember(result, name) == expected
      }
      return
    }
    if (expectation instanceof PatchDtoExpectation.ReaderFailure) {
      assert result instanceof JsonApiDocumentReadException
      def ex = (JsonApiDocumentReadException) result
      assert ex.ruleCode() == expectation.code()
      assert ex.jsonPointer() == expectation.jsonPointer()
      return
    }
    if (expectation instanceof PatchDtoExpectation.BinderFailure) {
      assert result instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) result
      assert ex.diagnostic() == expectation.diagnostic()
      assert ex.propertyPath() == expectation.propertyPath()
      return
    }
    throw new IllegalArgumentException("Unknown expectation: " + expectation)
  }

  private static Object readMember(Object dto, String name) {
    return dto.getClass().getMethod(name).invoke(dto)
  }

  private static JsonApiDocument decodeUpdateDocument(String json) {
    return JsonApiJackson3.reader(
        JsonMapper.builder().build(),
        DocumentReadContext.of(
        ValidationContext.defaults().withDocumentUsage(DocumentUsage.UPDATE_REQUEST),
        PrimaryDataKind.RESOURCE))
        .readValue(json)
  }

  static class CloseTrackingInputStream extends FilterInputStream {
    boolean closed

    CloseTrackingInputStream(InputStream delegate) {
      super(delegate)
    }

    @Override
    void close() {
      closed = true
      super.close()
    }
  }

  static class UppercaseDeserializer extends StdDeserializer<String> {
    UppercaseDeserializer() {
      super(String.class)
    }

    @Override
    String deserialize(JsonParser parser, DeserializationContext context) {
      return parser.getValueAsString().toUpperCase()
    }
  }

  @JsonApiResource(type = "articles")
  static class ImplicitIdPatch {
    String id
    PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class AuthorListPatch {
    @JsonApiId String id
    @JsonApiRelationship PatchPresence<List<AuthorId>> contributors
  }

  @JsonApiResource(type = "articles")
  static class AuthorOptionalPatch {
    @JsonApiId String id
    @JsonApiRelationship PatchPresence<Optional<AuthorId>> author
  }

  @JsonApiResource(type = "articles")
  static class MapPatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<Map<String, Value>> tags
  }

  @JsonApiResource(type = "articles")
  static class WrapperDeserializePatch {
    @JsonApiId String id
    @JsonDeserialize(using = UppercaseDeserializer)
    PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class WrapperSerializePatch {
    @JsonApiId String id
    @JsonSerialize(using = MarkerSerializer)
    PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class WrapperConverterDeserializePatch {
    @JsonApiId String id
    @JsonDeserialize(converter = IdentityConverter)
    PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class WrapperConverterSerializePatch {
    @JsonApiId String id
    @JsonSerialize(converter = IdentityConverter)
    PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class MixinTargetPatch {
    @JsonApiId String id
    PatchPresence<String> title
  }

  static abstract class MixInWithDeserializer {
    @JsonDeserialize(using = UppercaseDeserializer)
    abstract PatchPresence<String> getTitle()
  }

  @JsonApiResource(type = "articles")
  static class LoudPatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<LoudValue> title
  }

  @JsonApiResource(type = "articles")
  static class SnakePatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<String> displayTitle
  }

  @JsonApiResource(type = "articles")
  static class MixedPatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<String> title
    @JsonApiAttribute PatchPresence<Optional<String>> subtitle
    @JsonApiAttribute PatchPresence<String> body
  }

  @JsonApiResource(type = "articles")
  static class CountPatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<Integer> count
  }

  @JsonApiResource(type = "articles")
  static class TitlePatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<String> title
  }

  static class AuthorId {
    String type
    String id

    AuthorId() {}

    AuthorId(String type, String id) {
      this.type = type
      this.id = id
    }

    boolean equals(Object other) {
      other instanceof AuthorId && type == other.type && id == other.id
    }

    int hashCode() {
      Objects.hash(type, id)
    }
  }

  static class Value {
    String label

    Value() {}

    Value(String label) {
      this.label = label
    }

    boolean equals(Object other) {
      other instanceof Value && label == other.label
    }

    int hashCode() {
      Objects.hash(label)
    }
  }

  static class MarkerSerializer extends ValueSerializer<Object> {
    @Override
    void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) {
      gen.writeString("marker")
    }
  }

  @JsonDeserialize(using = LoudDeserializer)
  @JsonSerialize(using = LoudSerializer)
  static class LoudValue {
    final String value

    LoudValue(String value) {
      this.value = value
    }

    boolean equals(Object other) {
      other instanceof LoudValue && value == other.value
    }

    int hashCode() {
      Objects.hash(value)
    }
  }

  static class LoudDeserializer extends StdDeserializer<LoudValue> {
    LoudDeserializer() {
      super(LoudValue)
    }

    @Override
    LoudValue deserialize(JsonParser parser, DeserializationContext context) {
      return new LoudValue(parser.getValueAsString().toUpperCase())
    }
  }

  static class LoudSerializer extends ValueSerializer<LoudValue> {
    @Override
    void serialize(LoudValue value, JsonGenerator gen, SerializationContext ctxt) {
      gen.writeString(value.value)
    }
  }

  static class IdentityConverter implements Converter<Object, Object> {
    @Override
    Object convert(DeserializationContext ctxt, Object value) {
      return value
    }

    @Override
    Object convert(SerializationContext ctxt, Object value) {
      return value
    }

    @Override
    JavaType getInputType(TypeFactory typeFactory) {
      return typeFactory.constructType(Object)
    }

    @Override
    JavaType getOutputType(TypeFactory typeFactory) {
      return typeFactory.constructType(Object)
    }
  }
}
