package io.github.kazemek.jsonapi.jackson3

import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.codec.AmbiguousPrimaryDataScenarios
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenarios
import io.github.kazemek.jsonapi.testsupport.codec.NegativeCodecScenarios

import spock.lang.Shared
import spock.lang.Specification

class DocumentReaderSpec extends Specification {

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Shared
  DocumentReadContext resourceContext = DocumentReadContext.resourceDefaults()

  def "reads fixture #fixture.id into a document that matches the constructed model"() {
    given:
    def json = readFixtureText(fixture.expectedPath)
    def context = DocumentReadContext.of(
        fixture.context, fixture.primaryDataKind ?: PrimaryDataKind.RESOURCE)
    def reader = JsonApiJackson3.reader(mapper, context)
    def writer = JsonApiJackson3.writer(mapper, fixture.context)

    when:
    def document = reader.readValue(json)

    then:
    document == fixture.document || wireEqual(writer, document, fixture.document)
    mapper.readTree(writer.writeValueAsString(document)) == mapper.readTree(json)

    where:
    fixture << CodecScenarios.catalog().where { it.readable }
  }

  def "all read sources decode #fixture.id equivalently"() {
    given:
    def json = readFixtureText(fixture.expectedPath)
    def context = DocumentReadContext.of(
        fixture.context, fixture.primaryDataKind ?: PrimaryDataKind.RESOURCE)
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
    fixture << CodecScenarios.catalog().where { it.readable }
  }

  def "ambiguous case #fixture.id decodes under both PrimaryDataKind values"() {
    given:
    def json = readFixtureText(fixture.expectedPath)

    when:
    def asResource = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(fixture.context, PrimaryDataKind.RESOURCE))
        .readValue(json)
    def asIdentifier = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(fixture.context, PrimaryDataKind.RESOURCE_IDENTIFIER))
        .readValue(json)

    then:
    asResource == fixture.resourceDocument
    asIdentifier == fixture.identifierDocument

    and:
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    mapper.readTree(writer.writeValueAsString(asResource)) == mapper.readTree(json)
    mapper.readTree(writer.writeValueAsString(asIdentifier)) == mapper.readTree(json)

    where:
    fixture << AmbiguousPrimaryDataScenarios.catalog().all()
  }

  def "negative corpus case #fixture.id fails with the documented diagnostics"() {
    given:
    def json = readFixtureText(fixture.path)
    def reader = JsonApiJackson3.reader(mapper, resourceContext)

    when:
    reader.readValue(json)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.valueOf(fixture.category)

    and:
    if (fixture.pointer != null) {
      assert ex.jsonPointer() == fixture.pointer
    }

    and:
    if (fixture.ruleCode != null) {
      assert ex.ruleCode() == ValidationRuleCode.valueOf(fixture.ruleCode)
    } else {
      assert ex.ruleCode() == null
    }

    and:
    if (fixture.sourceLocation) {
      assert ex.sourceLocation().isKnown()
    }
    ex.cause == null
    if (!json.isEmpty()) {
      assert !ex.message.contains(json)
    }

    where:
    fixture << NegativeCodecScenarios.catalog().all()
  }

  def "empty input and whitespace-only variants report MALFORMED_JSON for #source"() {
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

  def "aggregate validation resource location is precise on Jackson 3"() {
    given:
    def entry = NegativeCodecScenarios.catalog().byId('aggregate-validation-resource-location')
    def json = readFixtureText(entry.path)

    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue(json)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.sourceLocation().isKnown()
    ex.sourceLocation().lineNumber() == 4
    ex.sourceLocation().charOffset() < json.length() - 1
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

  def "namespaced link relation decodes as Link not additional member"() {
    given:
    def validation = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of('ext'),
        Set.of(),
        Set.of(),
        Set.of(),
        LinksContext.TOP_LEVEL,
        Map.of(),
        null)
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

  private static boolean wireEqual(
      JsonApiDocumentWriter writer,
      JsonApiDocument actual,
      JsonApiDocument expected) {
    def mapper = JsonMapper.builder().build()
    return mapper.readTree(writer.writeValueAsString(actual)) ==
        mapper.readTree(writer.writeValueAsString(expected))
  }

  private static String readFixtureText(String relativePath) {
    return TestSupportResources.readCorpusUtf8(relativePath)
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
