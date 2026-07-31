package io.github.kazemek.jsonapi.jackson3

import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixtures

import spock.lang.Shared
import spock.lang.Specification

class DocumentReaderSpec extends Specification {

  @Shared
  Path fixturesDir = Path.of(System.getProperty('jsonapi.fixtures.dir'))

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Shared
  DocumentReadContext resourceContext = DocumentReadContext.resourceDefaults()

  def "reads fixture #fixture.id into a document that matches the constructed model"() {
    given:
    def json = readFixtureText(fixture.expectedPath)
    def context = DocumentReadContext.of(fixture.context, primaryDataKind(fixture))
    def reader = JsonApiJackson3.reader(mapper, context)
    def writer = JsonApiJackson3.writer(mapper, fixture.context)

    when:
    def document = reader.readValue(json)

    then:
    document == fixture.document || wireEqual(writer, document, fixture.document)
    mapper.readTree(writer.writeValueAsString(document)) == mapper.readTree(json)

    where:
    fixture << WriterFixtures.all()
  }

  def "all read sources decode #fixture.id equivalently"() {
    given:
    def json = readFixtureText(fixture.expectedPath)
    def context = DocumentReadContext.of(fixture.context, primaryDataKind(fixture))
    def reader = JsonApiJackson3.reader(mapper, context)
    def expected = reader.readValue(json)
    def bytes = json.getBytes(StandardCharsets.UTF_8)
    def tracking = new CloseTrackingInputStream(new ByteArrayInputStream(bytes))
    def parser = reader.mapper().createParser(json)

    when:
    def fromBytes = reader.readValue(bytes)
    def fromStream = reader.readValue(tracking)
    def fromParser = reader.readValue(parser)

    then:
    fromBytes == expected
    fromStream == expected
    fromParser == expected
    !tracking.closed
    !parser.closed

    cleanup:
    parser?.close()

    where:
    fixture << [
      WriterFixtures.byId('single-resource')
    ]
  }

  def "preserves JSON null elements inside open-value arrays"() {
    given:
    def json = '{"meta":{"values":[null],"nested":{"items":[null,1]}}}'
    def reader = JsonApiJackson3.reader(mapper, resourceContext)

    when:
    def document = reader.readValue(json)

    then:
    document.meta().members().get('values') == [null]
    document.meta().members().get('nested').get('items') == [null, 1]
  }

  def "ambiguous object primary data obeys PrimaryDataKind"() {
    given:
    def json = '{"data":{"type":"articles","id":"1"}}'

    when:
    def asResource = JsonApiJackson3.reader(mapper, DocumentReadContext.resourceDefaults())
        .readValue(json)
    def asIdentifier = JsonApiJackson3.reader(mapper, DocumentReadContext.identifierDefaults())
        .readValue(json)

    then:
    asResource.data() instanceof DocumentData.SingleResource
    ((DocumentData.SingleResource) asResource.data()).resource() == ResourceObject.of('articles', '1')
    asIdentifier.data() instanceof DocumentData.SingleIdentifier
    ((DocumentData.SingleIdentifier) asIdentifier.data()).identifier() ==
        ResourceIdentifier.of('articles', '1')
  }

  def "ambiguous empty array primary data obeys PrimaryDataKind"() {
    given:
    def json = '{"data":[]}'

    when:
    def asResource = JsonApiJackson3.reader(mapper, DocumentReadContext.resourceDefaults())
        .readValue(json)
    def asIdentifier = JsonApiJackson3.reader(mapper, DocumentReadContext.identifierDefaults())
        .readValue(json)

    then:
    asResource.data() instanceof DocumentData.ResourceCollection
    ((DocumentData.ResourceCollection) asResource.data()).resources().isEmpty()
    asIdentifier.data() instanceof DocumentData.IdentifierCollection
    ((DocumentData.IdentifierCollection) asIdentifier.data()).identifiers().isEmpty()
  }

  def "malformed JSON reports MALFORMED_JSON without payload text"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.ruleCode() == null
    !ex.message.contains('{')
    ex.cause == null
  }

  def "truncated document reports MALFORMED_JSON with enclosing path"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{"data":{"type":"articles","id":')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.jsonPointer().startsWith('/data')
    ex.ruleCode() == null
  }

  def "empty input reports MALFORMED_JSON for #source"() {
    when:
    readWhole(source, input)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.jsonPointer() == ''
    ex.message == 'Expected a JSON:API document object'

    where:
    source   | input
    'string' | ''
    'string' | '   '
    'bytes'  | ''.getBytes(StandardCharsets.UTF_8)
    'bytes'  | '  '.getBytes(StandardCharsets.UTF_8)
  }

  def "trailing content after whole-input document is rejected"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{"meta":{}}{}')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.UNEXPECTED_TOKEN
    ex.message == 'Trailing content after JSON:API document'
  }

  def "caller-owned parser may sequence multiple root documents"() {
    given:
    def reader = JsonApiJackson3.reader(mapper, resourceContext)
    def parser = reader.mapper().createParser('{"meta":{"a":1}}{"meta":{"b":2}}')

    when:
    def first = reader.readValue(parser)
    def second = reader.readValue(parser)

    then:
    first.meta().members().get('a') == 1
    second.meta().members().get('b') == 2

    cleanup:
    parser?.close()
  }

  def "caller-owned parser advances past a prior scalar root before a document"() {
    given:
    def reader = JsonApiJackson3.reader(mapper, resourceContext)
    def parser = reader.mapper().createParser('"skip"{"meta":{"a":1}}')
    parser.nextToken() // VALUE_STRING

    when:
    def document = reader.readValue(parser)

    then:
    document.meta().members().get('a') == 1

    cleanup:
    parser?.close()
  }

  def "unexpected token reports path and location"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('[]')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.UNEXPECTED_TOKEN
    ex.jsonPointer() == ''
    ex.sourceLocation() != null
  }

  def "duplicate members are rejected"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{"meta":{},"meta":{}}')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.DUPLICATE_MEMBER
    ex.jsonPointer() == '/meta'
    ex.sourceLocation().isKnown()
  }

  def "local validation failures expose rule codes and top-level path"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{"data":{"id":"1"}}')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_TYPE
    ex.jsonPointer() == '/data/type'
    ex.message == 'Local validation failed'
    !ex.message.contains('1')
    ex.cause == null
  }

  def "included missing type reports nested pointer"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "data":{"type":"articles","id":"1"},
        "included":[{"id":"2"}]
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_TYPE
    ex.jsonPointer() == '/included/0/type'
    ex.message == 'Local validation failed'
  }

  def "collection missing type reports indexed pointer"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('{"data":[{"id":"1"}]}')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_TYPE
    ex.jsonPointer() == '/data/0/type'
  }

  def "relationship identifier missing type reports nested pointer"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "data":{
          "type":"articles",
          "id":"1",
          "relationships":{
            "author":{"data":{"id":"9"}}
          }
        }
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.MISSING_RESOURCE_TYPE
    ex.jsonPointer() == '/data/relationships/author/data/type'
  }

  def "reserved attribute reports attributes pointer"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "data":{
          "type":"articles",
          "id":"1",
          "attributes":{"type":"wrong"}
        }
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
    ex.jsonPointer() == '/data/attributes/type'
    ex.message == 'Local validation failed'
    !ex.message.contains('wrong')
  }

  def "missing link href reports LOCAL_VALIDATION"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "data":{
          "type":"articles",
          "id":"1",
          "links":{"self":{"title":"Self"}}
        }
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.NULL_REQUIRED_VALUE
    ex.jsonPointer() == '/data/links/self/href'
  }

  def "invalid dynamic link relation escapes pointer segments"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "meta":{},
        "links":{"foo~bar/baz":"https://example.com/"}
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.INVALID_LINK_RELATION
    ex.jsonPointer() == '/links/foo~0bar~1baz'
    ex.message == 'Local validation failed'
    !ex.message.contains('foo~bar')
    ex.cause == null
  }

  def "invalid dynamic attribute name escapes pointer segments"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "data":{
          "type":"articles",
          "id":"1",
          "attributes":{"foo~bar/baz":1}
        }
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.LOCAL_VALIDATION
    ex.ruleCode() == ValidationRuleCode.INVALID_MEMBER_NAME
    ex.jsonPointer() == '/data/attributes/foo~0bar~1baz'
    ex.message == 'Local validation failed'
    ex.cause == null
    ex.sourceLocation().isKnown()
  }

  def "aggregate URI link relation escapes pointer segments"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue('''
      {
        "meta":{},
        "links":{"http://example.com/rel":"https://example.com/"}
      }
      ''')

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.AGGREGATE_VALIDATION
    ex.ruleCode() == ValidationRuleCode.INVALID_LINKS_CONTEXT
    ex.jsonPointer() == '/links/http:~1~1example.com~1rel'
    ex.message == 'Aggregate validation failed'
    ex.cause == null
    ex.sourceLocation().isKnown()
  }

  def "aggregate validation failures expose rule codes and resource location"() {
    given:
    def json = '''
      {
        "data": [
          {"type":"articles","id":"1"},
          {"type":"articles","id":"1"}
        ]
      }
      '''.stripIndent().trim()

    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue(json)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.AGGREGATE_VALIDATION
    ex.ruleCode() == ValidationRuleCode.DUPLICATE_RESOURCE_IDENTITY
    ex.jsonPointer() == '/data/1'
    ex.message == 'Aggregate validation failed'
    !ex.message.contains('articles')
    ex.cause == null
    ex.sourceLocation().isKnown()
    ex.sourceLocation().lineNumber() == 4
    ex.sourceLocation().charOffset() < json.length() - 1
  }

  def "extension members require matching validation context"() {
    given:
    def json = readFixtureText('documents/extension-and-at-members.json')

    when:
    JsonApiJackson3.reader(mapper, DocumentReadContext.resourceDefaults()).readValue(json)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.AGGREGATE_VALIDATION
    ex.ruleCode() == ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER
    ex.message == 'Aggregate validation failed'
  }

  def "namespaced link relation decodes as Link not additional member"() {
    given:
    def validation = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of('ext'),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of())
    def context = DocumentReadContext.of(validation, PrimaryDataKind.RESOURCE)
    def reader = JsonApiJackson3.reader(mapper, context)

    when:
    def document = reader.readValue('''
      {
        "meta":{},
        "links":{"ext:custom":"https://example.com/ext"}
      }
      ''')

    then:
    document.links().links().get('ext:custom') instanceof Link.StringLink
    ((Link.StringLink) document.links().links().get('ext:custom')).href() == 'https://example.com/ext'
    !document.links().additionalMembers().containsKey('ext:custom')
  }

  def "bound context is exposed"() {
    given:
    def validation = ValidationContext.defaults().withDocumentUsage(DocumentUsage.CREATE_REQUEST)
    def context = DocumentReadContext.resourceDefaults()
        .withValidationContext(validation)
        .withPrimaryDataKind(PrimaryDataKind.RESOURCE_IDENTIFIER)
    def reader = JsonApiJackson3.reader(mapper, context)

    expect:
    reader.context().is(context)
    reader.context().validationContext().is(validation)
    reader.context().primaryDataKind() == PrimaryDataKind.RESOURCE_IDENTIFIER
  }

  private void readWhole(String source, Object input) {
    def reader = JsonApiJackson3.reader(mapper, resourceContext)
    if (source == 'string') {
      reader.readValue((String) input)
    } else {
      reader.readValue((byte[]) input)
    }
  }

  private static PrimaryDataKind primaryDataKind(WriterFixture fixture) {
    def data = fixture.document.data()
    if (data instanceof DocumentData.SingleIdentifier
        || data instanceof DocumentData.IdentifierCollection) {
      return PrimaryDataKind.RESOURCE_IDENTIFIER
    }
    return PrimaryDataKind.RESOURCE
  }

  private static boolean wireEqual(
      JsonApiDocumentWriter writer,
      JsonApiDocument actual,
      JsonApiDocument expected) {
    def mapper = JsonMapper.builder().build()
    return mapper.readTree(writer.writeValueAsString(actual)) ==
        mapper.readTree(writer.writeValueAsString(expected))
  }

  private String readFixtureText(String relativePath) {
    return Files.readString(fixturesDir.resolve(relativePath), StandardCharsets.UTF_8)
  }

  private static final class CloseTrackingInputStream extends FilterInputStream {
    private boolean closed

    CloseTrackingInputStream(InputStream delegate) {
      super(delegate)
    }

    @Override
    void close() {
      closed = true
      super.close()
    }

    boolean isClosed() {
      return closed
    }
  }
}
