package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchCommand
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.jackson3.ParameterizedBindingFixtures.GenericValue
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatMappedArticle
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatMappedOptionalArticle
import io.github.kazemek.jsonapi.testsupport.domainpatch.PatchScenario
import io.github.kazemek.jsonapi.testsupport.domainpatch.PatchScenarios
import io.github.kazemek.jsonapi.testsupport.domainpatch.PatchVerifier
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatCountedThing
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.util.Optional
import spock.lang.Specification
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JavaType
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.json.JsonMapper

class PatchBindingSpec extends Specification {

  private static final Set<String> FROM_DOCUMENT_IDS = [
    "patch-omitted-and-supplied-attributes",
    "patch-resource-type-mismatch"
  ] as Set

  def "shared patch catalog scenario: #scenario.id"() {
    given:
    def reader = patchReaderFor(scenario)

    when:
    def result = execute(scenario, reader)

    then:
    PatchVerifier.verify(scenario, result)

    where:
    scenario << PatchScenarios.catalog().all()
  }

  def "custom deserializer applies to attribute change"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"things","id":"1","attributes":{"title":"hello"}}}'

    when:
    def command = reader.readValue(json, FlatLoudThing)

    then:
    command.identity() == "1"
    command.changes() == [
      new PatchChange.AttributeChange("title", "title", "HELLO")
    ]
  }

  def "patch-custom-linkage-conversion"() {
    given:
    def mapper = { RelationshipData data, target ->
      def identifier = ((RelationshipData.SingleLinkage) data).identifier()
      return new FlatAuthor(identifier.type(), identifier.id())
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(FlatAuthor): mapper])
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"p1"}}}}}'

    when:
    def command = reader.readValue(json, FlatMappedArticle)

    then:
    command.identity() == "1"
    command.changes() == [
      new PatchChange.RelationshipChange("author", "author", new FlatAuthor("people", "p1"))
    ]
  }

  def "explicit null on Optional attribute stores value == null"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":null}}}'

    when:
    def command = reader.readValue(json, FlatOptionalTitleArticle)

    then:
    command.changes().size() == 1
    command.changes()[0].value() == null
    !(command.changes()[0].value() instanceof Optional)
  }

  def "fromDocument missing id"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.ofType("articles")),
        null, null, null, null, null, Map.of())

    when:
    reader.fromDocument(document, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "fromDocument JavaType returns PatchCommand wildcard with raw resourceType"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def document = decodeUpdateDocument(
        '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}')
    def javaType = mapper.constructType(FlatArticle)

    when:
    PatchCommand<?> command = reader.fromDocument(document, javaType)

    then:
    command.resourceType() == FlatArticle
    command.identity() == "1"
    command.changes() == [
      new PatchChange.AttributeChange("title", "title", "Hello")
    ]
  }

  def "generic attribute type resolves through the parameterized JavaType"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def document = decodeUpdateDocument(
        '{"data":{"type":"things","id":"1","attributes":{"value":"42"}}}')
    def javaType = mapper.typeFactory.constructParametricType(GenericValue, Integer)

    when:
    PatchCommand<?> command = reader.fromDocument(document, javaType)

    then:
    command.resourceType() == GenericValue
    command.identity() == "1"
    command.changes().size() == 1
    command.changes()[0].value() == 42
    command.changes()[0].value() instanceof Integer
  }

  def "explicit null on primitive attribute fails even when FAIL_ON_NULL_FOR_PRIMITIVES is off"() {
    given:
    def mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def json = '{"data":{"type":"things","id":"1","attributes":{"count":null}}}'

    when:
    reader.readValue(json, FlatCountedThing)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/attributes/count"
  }

  def "mapper and JavaType factory overloads bind successfully"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}'
    def mapper = JsonMapper.builder().build()
    def mapperReader = JsonApiJackson3.patchReader(mapper)
    def javaType = JsonMapper.builder().build().constructType(FlatArticle)

    when:
    def fromMapper = mapperReader.readValue(json, FlatArticle)
    def fromJavaType = JsonApiJackson3.patchReader(JsonMapper.builder().build())
        .readValue(json, javaType)

    then:
    fromMapper.identity() == "1"
    fromMapper.changes() == [
      new PatchChange.AttributeChange("title", "title", "Hello")
    ]
    fromJavaType.resourceType() == FlatArticle
    fromJavaType.identity() == "1"
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
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), converter)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    def command = reader.readValue(json, FlatArticle)

    then:
    command.identity() == "parsed-1"
  }

  def "fromDocument rejects null and non-single-resource primary data"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def metaOnly = new JsonApiDocument(
        null, null, Meta.of([note: "x"]), null, null, null, Map.of())

    when:
    reader.fromDocument(null, FlatArticle)

    then:
    thrown(NullPointerException)

    when:
    reader.fromDocument(metaOnly, FlatArticle)

    then:
    thrown(IllegalArgumentException)

    when:
    reader.fromDocument(
        new JsonApiDocument(DocumentData.NullData.INSTANCE, null, null, null, null, null, Map.of()),
        FlatArticle)

    then:
    thrown(IllegalArgumentException)

    when:
    reader.fromDocument(
        new JsonApiDocument(
        new DocumentData.ResourceCollection(List.of()),
        null, null, null, null, null, Map.of()),
        FlatArticle)

    then:
    thrown(IllegalArgumentException)

    when:
    reader.fromDocument(
        new JsonApiDocument(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1")),
        null, null, null, null, null, Map.of()),
        FlatArticle)

    then:
    thrown(IllegalArgumentException)
  }

  def "caller-owned stream and parser remain open on success and failure"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def mapper = JsonMapper.builder().build()
    def successBytes =
        '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'.bytes
    def failureBytes = '{"data":'.bytes
    def successStream = new CloseTrackingInputStream(new ByteArrayInputStream(successBytes))
    def failureStream = new CloseTrackingInputStream(new ByteArrayInputStream(failureBytes))
    def parser = mapper.createParser(successBytes)
    def failureParser = mapper.createParser(failureBytes)

    when:
    def command = reader.readValue(successStream, FlatArticle)
    def fromParser = reader.readValue(parser, FlatArticle)

    then:
    command.identity() == "1"
    fromParser.identity() == "1"
    !successStream.closed
    !parser.closed

    when:
    reader.readValue(failureStream, FlatArticle)

    then:
    thrown(JsonApiDocumentReadException)
    !failureStream.closed

    when:
    reader.readValue(failureParser, FlatArticle)

    then:
    thrown(JsonApiDocumentReadException)
    !failureParser.closed

    cleanup:
    parser?.close()
    failureParser?.close()
  }

  def "duplicate mapping definitions fail before a command escapes"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, FlatDuplicateAttributeArticle)

    then:
    thrown(JsonApiMappingException)
  }

  def "typed identity is never listed among changes"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"title":"T"},"relationships":{"author":{"data":null}}}}'

    when:
    def command = reader.readValue(json, FlatArticle)

    then:
    command.identity() == "1"
    command.changes()*.logicalName() == ["title", "author"]
  }

  def "byte array Class and JavaType entry points bind successfully"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"}}}'.bytes
    def javaType = mapper.constructType(FlatArticle)

    when:
    def fromClass = reader.readValue(json, FlatArticle)
    def fromJavaType = reader.readValue(json, javaType)

    then:
    fromClass.identity() == "1"
    fromJavaType.resourceType() == FlatArticle
    fromJavaType.identity() == "1"
  }

  def "JavaType stream and parser entry points leave caller-owned sources open"() {
    given:
    def mapper = JsonMapper.builder().build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def javaType = mapper.constructType(FlatArticle)
    def bytes = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'.bytes
    def stream = new CloseTrackingInputStream(new ByteArrayInputStream(bytes))
    def parser = mapper.createParser(bytes)

    when:
    def fromStream = reader.readValue(stream, javaType)
    def fromParser = reader.readValue(parser, javaType)

    then:
    fromStream.identity() == "1"
    fromParser.identity() == "1"
    !stream.closed
    !parser.closed

    cleanup:
    parser?.close()
  }

  def "IdentifierConverter parse failure and null are IDENTIFIER_CONVERSION_FAILED"() {
    given:
    def throwing = new IdentifierConverter() {
          @Override
          String convert(Object value) {
            return String.valueOf(value)
          }

          @Override
          Object parse(String wire) {
            throw new IllegalStateException("boom")
          }
        }
    def nullParse = new IdentifierConverter() {
          @Override
          String convert(Object value) {
            return String.valueOf(value)
          }

          @Override
          Object parse(String wire) {
            return null
          }
        }
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    JsonApiJackson3.patchReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), throwing)
        .readValue(json, FlatArticle)

    then:
    def throwingEx = thrown(JsonApiMappingException)
    throwingEx.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED

    when:
    JsonApiJackson3.patchReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), nullParse)
        .readValue(json, FlatArticle)

    then:
    def nullEx = thrown(JsonApiMappingException)
    nullEx.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
  }

  def "relationship cardinality mismatch is reported before linkage mapper runs"() {
    given:
    def invoked = false
    def mapper = { RelationshipData data, JavaType target ->
      invoked = true
      return null
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(FlatAuthor): mapper])
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":[{"type":"people","id":"p1"}]},"contributors":{"data":{"type":"people","id":"p1"}}}}}'

    when:
    reader.readValue(json, FlatMappedArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    !invoked
  }

  def "linkage mapper exception is LINKAGE_MAPPING_FAILED"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      throw new IllegalStateException("boom")
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(FlatAuthor): mapper])
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"p1"}}}}}'

    when:
    reader.readValue(json, FlatMappedArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.LINKAGE_MAPPING_FAILED
    ex.propertyPath() == "/relationships/author/data"
  }

  def "empty to-many and Optional relationship changes bind"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.NullLinkage) {
        return null
      }
      if (data instanceof RelationshipData.IdentifierCollectionLinkage) {
        return data.identifiers().collect { id -> new FlatAuthor(id.type(), id.id()) }
      }
      def identifier = ((RelationshipData.SingleLinkage) data).identifier()
      return new FlatAuthor(identifier.type(), identifier.id())
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        IdentifierConverter.defaults(),
        [(FlatAuthor): mapper])
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":null},"contributors":{"data":[]}}}}'

    when:
    def command = reader.readValue(json, FlatMappedOptionalArticle)

    then:
    command.changes() == [
      new PatchChange.RelationshipChange("author", "author", Optional.empty()),
      new PatchChange.RelationshipChange("contributors", "contributors", [])
    ]
  }

  def "mapper factory with ValidationContext and IdentifierConverter binds"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object value) {
            return String.valueOf(value)
          }

          @Override
          Object parse(String wire) {
            return "b-" + wire
          }
        }
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), converter)
    def json = '{"data":{"type":"articles","id":"9","attributes":{"title":"T"}}}'

    when:
    def command = reader.readValue(json, FlatArticle)

    then:
    command.identity() == "b-9"
  }

  private static JsonApiPatchReader patchReaderFor(PatchScenario scenario) {
    def context = ValidationContext.defaults()
    if (scenario.expectedEndpointIdentity() != null) {
      context = context.withExpectedEndpointIdentity(scenario.expectedEndpointIdentity())
    }
    return JsonApiJackson3.patchReader(JsonMapper.builder().build(), context)
  }

  private static Object execute(PatchScenario scenario, JsonApiPatchReader reader) {
    try {
      if (FROM_DOCUMENT_IDS.contains(scenario.id())) {
        def document = decodeUpdateDocument(scenario.documentJson())
        return reader.fromDocument(document, scenario.targetType())
      }
      return reader.readValue(scenario.documentJson(), scenario.targetType())
    } catch (JsonApiDocumentReadException | JsonApiMappingException ex) {
      return ex
    }
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

  @JsonApiResource(type = "things")
  static class FlatLoudThing {
    @JsonApiId String id
    @JsonDeserialize(using = UppercaseDeserializer)
    String title
  }

  @JsonApiResource(type = "articles")
  static class FlatOptionalTitleArticle {
    @JsonApiId String id
    Optional<String> title
  }

  @JsonApiResource(type = "articles")
  static class FlatDuplicateAttributeArticle {
    @JsonApiId String id
    @JsonApiAttribute(name = "title")
    String title
    @JsonApiAttribute(name = "title")
    String alsoTitle
  }
}
