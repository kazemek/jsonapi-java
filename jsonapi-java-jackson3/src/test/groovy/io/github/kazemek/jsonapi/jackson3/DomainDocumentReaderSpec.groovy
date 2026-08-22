package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.DomainData
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.JsonApiFixtures
import io.github.kazemek.jsonapi.testfixtures.TestSupportResources
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatLidArticle
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeBindingDocument
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeEntryPoint
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadCase
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadExpectation
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadInput
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadScenario
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadVariant
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReaderContext
import io.github.kazemek.jsonapi.testfixtures.enveloperead.FlatThrowingArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatMappedArticle
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule

// Shared typed-envelope cases live in EnvelopeReadScenarios. This spec runs every catalog entry
// and asserts executedScenarioIds == catalogScenarioIds so a later Jackson 2 envelope suite can
// do the same. Adapter-local cases stay here (no shared manifest): metaAs, JavaType registrations,
// builder-based domainDocumentReader overloads, custom linkage mappers, caller-owned streams,
// malformed input, and validation failures.
// @Stepwise pins the declared feature order so the coverage feature always runs after the
// parameterized catalog iterations (Spock does not guarantee feature order otherwise).
@Stepwise
class DomainDocumentReaderSpec extends Specification {

  @Shared
  List<String> executedScenarioIds = []

  @Unroll
  def "envelope read #scenario.id from the shared catalog"() {
    given:
    executedScenarioIds.add(scenario.id())

    when:
    def results = execute(scenario)

    then:
    verify(scenario, results)

    where:
    scenario << JsonApiFixtures.envelopeRead().all()
  }

  def "covers every shared envelope-read scenario exactly once"() {
    expect:
    executedScenarioIds == JsonApiFixtures.envelopeRead().all()*.id
  }

  def "metaAs returns null for both overloads when meta is absent"() {
    given:
    def reader = newReader(FlatArticle)

    when:
    def envelope = reader.readValue(bindingText(EnvelopeBindingDocument.SINGLE_RESOURCE))
    def javaType = JsonMapper.builder().build().constructType(MetaPayload)

    then:
    envelope.metaAs(MetaPayload) == null
    envelope.metaAs(javaType) == null
  }

  def "metaAs converts via the caller-mapper module on both entry paths and both overloads"() {
    given:
    def module = new SimpleModule()
    module.addDeserializer(MetaPayload, new CountValueDeserializer())
    def base = JsonMapper.builder().addModule(module).build()
    def reader = JsonApiJackson3.domainDocumentReader(
        base, DocumentReadContext.resourceDefaults(), registry())
    def json = '{"meta":{"count":3}}'
    def payloadType = base.constructType(MetaPayload)

    when:
    def fromRead = reader.readValue(json)
    def document = JsonApiJackson3.reader(base, DocumentReadContext.resourceDefaults()).readValue(json)
    def fromDocument = reader.fromDocument(document)

    then:
    fromRead.metaAs(MetaPayload) == new MetaPayload(3)
    fromRead.metaAs(payloadType) == new MetaPayload(3)
    fromDocument.metaAs(MetaPayload) == new MetaPayload(3)
    fromDocument.metaAs(payloadType) == new MetaPayload(3)
  }

  def "incompatible metaAs target is UNSUPPORTED_ATTRIBUTE_VALUE at /meta"() {
    given:
    def reader = newReader()

    when:
    reader.readValue('{"meta":{"name":"x"}}').metaAs(MetaPayload)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/meta"
    ex.resourceClass() == null
  }

  def "JavaType registrations bind through the same registry gate"() {
    given:
    def base = JsonMapper.builder().build()
    def registry = ResourceTypeRegistry.builder()
        .register(base.constructType(FlatArticle))
        .register(Person)
        .build()
    def reader = JsonApiJackson3.domainDocumentReader(
        base, DocumentReadContext.resourceDefaults(), registry)

    when:
    def envelope = reader.readValue(bindingText(EnvelopeBindingDocument.HETEROGENEOUS_COLLECTION))

    then:
    ((DomainData.ResourceCollection) envelope.data()).resources() ==
        [
          new FlatArticle("1", "First", null, null, null),
          new Person("9", "Dan")
        ]
  }

  def "builder-based domainDocumentReader overloads derive readers that bind identically"() {
    given:
    def builder = JsonMapper.builder()
    def registry = registry(FlatArticle)
    def threeArg = JsonApiJackson3.domainDocumentReader(
        builder, DocumentReadContext.resourceDefaults(), registry)
    def fourArg = JsonApiJackson3.domainDocumentReader(
        builder, DocumentReadContext.resourceDefaults(), registry, IdentifierConverter.defaults())
    def fiveArg = JsonApiJackson3.domainDocumentReader(
        builder,
        DocumentReadContext.resourceDefaults(),
        registry,
        IdentifierConverter.defaults(),
        Map.of())

    when:
    def fromThree = threeArg.readValue(fixtureText('resource-collection'))
    def fromFour = fourArg.readValue(fixtureText('resource-collection'))
    def fromFive = fiveArg.readValue(fixtureText('resource-collection'))

    then:
    ((DomainData.ResourceCollection) fromThree.data()).resources()*.title == ["First", "Second"]
    ((DomainData.ResourceCollection) fromFour.data()).resources()*.title == ["First", "Second"]
    ((DomainData.ResourceCollection) fromFive.data()).resources()*.title == ["First", "Second"]
  }

  def "custom linkage mappers apply to primary and included resources"() {
    given:
    def authorMapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.SingleLinkage) {
        def identifier = ((RelationshipData.SingleLinkage) data).identifier()
        return new Person(identifier.id(), null)
      }
      ((RelationshipData.IdentifierCollectionLinkage) data).identifiers().collect {
        new Person(it.id(), null)
      }
    } as RelationshipLinkageMapper
    def flatAuthorMapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.SingleLinkage) {
        def identifier = ((RelationshipData.SingleLinkage) data).identifier()
        return new FlatAuthor(identifier.type(), identifier.id())
      }
      ((RelationshipData.IdentifierCollectionLinkage) data).identifiers().collect {
        new FlatAuthor(it.type(), it.id())
      }
    } as RelationshipLinkageMapper
    def reader = JsonApiJackson3.domainDocumentReader(
        JsonMapper.builder().build(),
        DocumentReadContext.resourceDefaults(),
        registry(FlatMappedArticle, Comment, Person),
        IdentifierConverter.defaults(),
        [(FlatAuthor): flatAuthorMapper, (Person): authorMapper])
    def json =
        '''
        {
          "data": {
            "type": "articles",
            "id": "1",
            "relationships": {
              "author": {
                "data": {
                  "type": "people",
                  "id": "p1"
                }
              },
              "contributors": {
                "data": [
                  {
                    "type": "comments",
                    "id": "c1"
                  }
                ]
              }
            }
          },
          "included": [
            {
              "type": "comments",
              "id": "c1",
              "relationships": {
                "author": {
                  "data": {
                    "type": "people",
                    "id": "p1"
                  }
                }
              }
            },
            {
              "type": "people",
              "id": "p1"
            }
          ]
        }
        '''

    when:
    def envelope = reader.readValue(json)

    then:
    ((DomainData.SingleResource) envelope.data()).resource() as FlatMappedArticle ==
        new FlatMappedArticle(
        "1", null, new FlatAuthor("people", "p1"), [
          new FlatAuthor("comments", "c1")
        ])
    envelope.included().resources() == [
      new Comment("c1", null, new Person("p1", null)),
      new Person("p1", null)
    ]
  }

  def "caller-owned stream and parser remain open on success and failure"() {
    given:
    def reader = newReader(FlatArticle)
    def successBytes = bindingText(EnvelopeBindingDocument.SINGLE_RESOURCE).bytes
    def successStream = new CloseTrackingInputStream(new ByteArrayInputStream(successBytes))
    def failureStream = new CloseTrackingInputStream(new ByteArrayInputStream('{"data":'.bytes))
    def parser = JsonMapper.builder().build().createParser(
        bindingText(EnvelopeBindingDocument.SINGLE_RESOURCE))

    when:
    def envelope = reader.readValue(successStream)
    def fromParser = reader.readValue(parser)

    then:
    envelope.data() instanceof DomainData.SingleResource
    fromParser.data() instanceof DomainData.SingleResource
    !successStream.closed
    !parser.closed

    when:
    reader.readValue(failureStream)

    then:
    thrown(JsonApiDocumentReadException)
    !failureStream.closed

    cleanup:
    parser?.close()
  }

  def "malformed input stays JsonApiDocumentReadException with category and location"() {
    given:
    def reader = newReader()

    when:
    reader.readValue('{"data":')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.jsonPointer() == ""
  }

  def "validation failures keep the originating rule code"() {
    given:
    def reader = newReader(FlatArticle, Person)

    when:
    reader.readValue(bindingText(EnvelopeBindingDocument.DUPLICATE_INCLUDED_IDENTITIES))

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.AGGREGATE_VALIDATION
    ex.ruleCode() == ValidationRuleCode.DUPLICATE_RESOURCE_IDENTITY
    ex.jsonPointer() == "/included/1"
  }

  private List execute(EnvelopeReadScenario scenario) {
    def variant = scenario.variant()
    if (variant instanceof EnvelopeReadVariant.Registry) {
      return variant.attempts().collect { attempt -> executeRegistry(attempt) }
    }
    def binding = (EnvelopeReadVariant.DocumentBinding) variant
    return binding.cases().collect { envelopeCase -> executeCase(binding, envelopeCase) }
  }

  private static Object executeRegistry(EnvelopeReadVariant.RegistryAttempt attempt) {
    try {
      def builder = ResourceTypeRegistry.builder()
      for (Class<?> target : attempt.targetClasses()) {
        builder.register(target)
      }
      builder.build()
      return null
    } catch (JsonApiMappingException ex) {
      return ex
    }
  }

  private Object executeCase(
      EnvelopeReadVariant.DocumentBinding binding, EnvelopeReadCase envelopeCase) {
    def reader = readerFor(binding, envelopeCase)
    try {
      if (binding.entryPoint() == EnvelopeEntryPoint.FROM_DOCUMENT) {
        return reader.fromDocument(
            ((EnvelopeReadInput.CoreDocument) envelopeCase.input()).document())
      }
      return reader.readValue(wireText(envelopeCase.input()))
    } catch (Exception ex) {
      return ex
    }
  }

  private static JsonApiDomainDocumentReader readerFor(
      EnvelopeReadVariant.DocumentBinding binding, EnvelopeReadCase envelopeCase) {
    def builder = ResourceTypeRegistry.builder()
    for (Class<?> target : binding.targetClasses()) {
      builder.register(target)
    }
    JsonApiJackson3.domainDocumentReader(
        JsonMapper.builder().build(), resolveContext(envelopeCase), builder.build())
  }

  private static DocumentReadContext resolveContext(EnvelopeReadCase envelopeCase) {
    switch (envelopeCase.readerContext()) {
      case EnvelopeReaderContext.RESOURCE_DEFAULTS:
        return DocumentReadContext.resourceDefaults()
      case EnvelopeReaderContext.IDENTIFIER_DEFAULTS:
        return DocumentReadContext.identifierDefaults()
      case EnvelopeReaderContext.CODEC_DERIVED:
        CodecScenario fixture =
        CodecScenarios.byId(
        ((EnvelopeReadInput.CodecFixture) envelopeCase.input()).codecScenarioId())
        return DocumentReadContext.of(
            fixture.context(),
            fixture.primaryDataKind() != null ? fixture.primaryDataKind() : PrimaryDataKind.RESOURCE)
      default:
        throw new IllegalArgumentException("Unknown reader context: " + envelopeCase.readerContext())
    }
  }

  private static void verify(EnvelopeReadScenario scenario, List results) {
    def variant = scenario.variant()
    if (variant instanceof EnvelopeReadVariant.Registry) {
      variant.attempts().eachWithIndex { attempt, index ->
        def ex = results[index]
        assert ex instanceof JsonApiMappingException
        assert ex.diagnostic() == attempt.diagnostic()
        assert ex.resourceClass() == attempt.resourceClass()
        assert ex.propertyPath() == attempt.propertyPath()
      }
      return
    }
    def binding = (EnvelopeReadVariant.DocumentBinding) variant
    binding.cases().eachWithIndex { envelopeCase, index ->
      verifyCase(envelopeCase, results[index])
    }
  }

  private static void verifyCase(EnvelopeReadCase envelopeCase, Object result) {
    def expectation = envelopeCase.expectation()
    if (expectation instanceof EnvelopeReadExpectation.Failure) {
      assert result instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) result
      assert ex.diagnostic() == expectation.diagnostic()
      assert ex.propertyPath() == expectation.propertyPath()
      assert ex.resourceClass() == expectation.resourceClass()
      return
    }
    if (expectation instanceof EnvelopeReadExpectation.MutationSafety) {
      assert result instanceof JsonApiDomainDocument
      def envelope = (JsonApiDomainDocument) result
      verifyBound(envelope, expectation.bound())
      if (expectation.additionalMembers()) {
        try {
          envelope.additionalMembers().put("k", "v")
          assert false: "expected UnsupportedOperationException"
        } catch (UnsupportedOperationException ignored) {
        }
      }
      if (expectation.errors()) {
        try {
          envelope.errors().add(null)
          assert false: "expected UnsupportedOperationException"
        } catch (UnsupportedOperationException ignored) {
        }
      }
      if (expectation.includedResources()) {
        try {
          envelope.included().resources().add("z")
          assert false: "expected UnsupportedOperationException"
        } catch (UnsupportedOperationException ignored) {
        }
      }
      return
    }
    assert result instanceof JsonApiDomainDocument
    verifyBound((JsonApiDomainDocument) result, (EnvelopeReadExpectation.BoundEnvelope) expectation)
  }

  private static void verifyBound(
      JsonApiDomainDocument envelope, EnvelopeReadExpectation.BoundEnvelope expected) {
    assert envelope.data() == expected.data()
    assert envelope.errors() == expected.errors()
    assert envelope.jsonapi() == expected.jsonapi()
    assert envelope.links() == expected.links()
    assert envelope.meta() == expected.meta()
    assert envelope.additionalMembers() == expected.additionalMembers()
    if (expected.included() == null) {
      assert envelope.included() == null
      return
    }
    assert envelope.included() != null
    def included = envelope.included()
    def exp = expected.included()
    assert included.resources() == exp.resources()
    def present = []
    exp.probes().each { probe ->
      def found = included.find(probe.identity())
      if (probe.expectedPresent()) {
        assert found.isPresent()
        assert exp.resources().contains(found.get())
        present << found.get()
      } else {
        assert found.isEmpty()
      }
    }
    if (exp.sharedInstanceAcrossPresentProbes() && present.size() > 1) {
      present.tail().each { assert it.is(present.head()) }
    }
  }

  // ============================== MAPPING-LOCATION COMPOSITION (KAZ-83) ==============================
  //
  // Binder failures compose structurally with the document prefix: a resource-relative binder
  // location joins under /data, /data/<index>, or /included/<index>; a binder failure without a
  // location reports just the document prefix. Locations carry wire names, never Jackson logical
  // names, and segments are RFC 6901-escaped.

  def "single primary data composes to /data plus the resource-local pointer"() {
    given:
    def reader = newReader(LocationArticle)

    when:
    reader.readValue(
        '{"data":{"type":"loc-articles","id":"1","attributes":{"title":"oops"}}}')

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/data/attributes/title"
  }

  def "collection primary data composes the element index"() {
    given:
    def reader = newReader(LocationArticle)

    when:
    reader.readValue(
        '{"data":[' +
        '{"type":"loc-articles","id":"1","attributes":{"title":"1"}},' +
        '{"type":"loc-articles","id":"2","attributes":{"title":"oops"}}]}')

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/data/1/attributes/title"
  }

  def "included resources compose the included index"() {
    given:
    def reader = newReader(LocationArticle)
    // fromDocument skips aggregate validation, so the included element can be reached directly.
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("loc-articles", "1")),
        null,
        null,
        null,
        null,
        List.of(
        new ResourceObject(
        "loc-articles",
        "9",
        null,
        Attributes.ofAttributes(Map.of("title", "oops")),
        null,
        null,
        null,
        Map.of())),
        Map.of())

    when:
    reader.fromDocument(document)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/included/0/attributes/title"
  }

  def "nested resource-local locations compose deeply under the document prefix"() {
    given:
    def reader = newReader(NestedLocationArticle)

    when:
    reader.readValue(
        '{"data":{"type":"loc-nested","id":"1","attributes":{"address":{"city":"oops"}}}}')

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/data/attributes/address/city"
  }

  def "renamed wire members report the JSON:API name, never the logical name"() {
    given:
    def reader = newReader(RenamedLocationArticle)

    when:
    reader.readValue(
        '{"data":{"type":"loc-renamed","id":"1","attributes":{"headline":"oops"}}}')

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    // Wire coordinate headline; the Jackson/logical property name title must not leak.
    ex.propertyPath() == "/data/attributes/headline"
  }

  def "locationless binder failures report only the document prefix"() {
    given:
    def reader = newReader(FlatThrowingArticle)
    // fromDocument skips aggregate validation, so the included element can be reached directly.
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("throwing-articles", "1")),
        null,
        null,
        null,
        null,
        List.of(
        new ResourceObject(
        "throwing-articles",
        "2",
        null,
        Attributes.ofAttributes(Map.of("title", "boom")),
        null,
        null,
        null,
        Map.of())),
        Map.of())

    when:
    reader.fromDocument(document)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    // No member location exists on the failure path; composition keeps the meaningful document
    // location instead of inventing one.
    ex.location().pointer() == "/included/0"
  }

  def "registry declaration failures carry no member location"() {
    when:
    ResourceTypeRegistry.builder()
        .register(FlatArticle)
        .register(FlatLidArticle)
        .build()

    then:
    def conflicting = thrown(JsonApiMappingException)
    conflicting.diagnostic() == MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION
    conflicting.location() == null

    when:
    ResourceTypeRegistry.builder().register(Object.class).build()

    then:
    def missing = thrown(JsonApiMappingException)
    missing.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    missing.location() == null
  }

  @JsonApiResource(type = "loc-articles")
  static class LocationArticle {
    @JsonApiId String id
    @JsonApiAttribute int title
  }

  @JsonApiResource(type = "loc-renamed")
  static class RenamedLocationArticle {
    @JsonApiId String id
    @JsonApiAttribute(name = "headline") int title
  }

  static class NestedAddress {
    int city
  }

  @JsonApiResource(type = "loc-nested")
  static class NestedLocationArticle {
    @JsonApiId String id
    @JsonApiAttribute NestedAddress address
  }

  private String wireText(EnvelopeReadInput input) {
    if (input instanceof EnvelopeReadInput.CodecFixture) {
      return fixtureText(input.codecScenarioId())
    }
    if (input instanceof EnvelopeReadInput.BindingDocument) {
      return bindingText(input.document())
    }
    throw new IllegalArgumentException("No wire text for " + input)
  }

  private static String fixtureText(String id) {
    CodecScenario fixture = CodecScenarios.byId(id)
    TestSupportResources.readCorpusUtf8(fixture.expectedPath)
  }

  private static String bindingText(EnvelopeBindingDocument document) {
    TestSupportResources.readCorpusUtf8(document.relativePath())
  }

  private static ResourceTypeRegistry registry(Class<?>... targetClasses) {
    def builder = ResourceTypeRegistry.builder()
    for (Class<?> target : targetClasses) {
      builder.register(target)
    }
    builder.build()
  }

  private static JsonApiDomainDocumentReader newReader(Class<?>... targetClasses) {
    JsonApiJackson3.domainDocumentReader(
        JsonMapper.builder().build(), DocumentReadContext.resourceDefaults(), registry(targetClasses))
  }

  static class MetaPayload {
    final int count

    MetaPayload(int count) {
      this.count = count
    }

    boolean equals(Object other) {
      other instanceof MetaPayload && ((MetaPayload) other).count == count
    }

    int hashCode() {
      count
    }
  }

  static class CountValueDeserializer extends StdDeserializer<MetaPayload> {
    CountValueDeserializer() {
      super(MetaPayload)
    }

    @Override
    MetaPayload deserialize(JsonParser parser, DeserializationContext context) {
      if (parser.currentToken() == JsonToken.START_OBJECT) {
        parser.nextToken()
      }
      if (parser.currentToken() == JsonToken.PROPERTY_NAME) {
        parser.nextToken()
      }
      new MetaPayload(parser.getIntValue())
    }
  }

  static class CloseTrackingInputStream extends FilterInputStream {
    boolean closed = false

    CloseTrackingInputStream(InputStream delegate) {
      super(delegate)
    }

    @Override
    void close() {
      closed = true
      super.close()
    }
  }
}
