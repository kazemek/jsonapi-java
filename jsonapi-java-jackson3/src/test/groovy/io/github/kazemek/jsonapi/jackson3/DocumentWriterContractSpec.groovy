package io.github.kazemek.jsonapi.jackson3

import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap
import java.util.List
import java.util.Map

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind
import io.github.kazemek.jsonapi.fixtures.TestFixtureResources

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DocumentWriterContractSpec extends Specification {

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Unroll
  def "writes independently constructed #description through every sink"() {
    given:
    def writer = JsonApiJackson3.writer(mapper, ValidationContext.defaults())
    def expected = mapper.readTree(expectedJson)
    def bytesOut = new ByteArrayOutputStream()
    def charsOut = new StringWriter()
    def generatorOut = new ByteArrayOutputStream()

    when:
    def asString = writer.writeValueAsString(document)
    def asBytes = writer.writeValueAsBytes(document)
    writer.writeValue(bytesOut, document)
    writer.writeValue(charsOut, document)
    def generator = writer.mapper().createGenerator(generatorOut)
    try {
      writer.writeValue(generator, document)
    } finally {
      generator.close()
    }

    then:
    mapper.readTree(asString) == expected
    mapper.readTree(asBytes) == expected
    mapper.readTree(bytesOut.toByteArray()) == expected
    mapper.readTree(charsOut.toString()) == expected
    mapper.readTree(generatorOut.toByteArray()) == expected
    new String(asBytes, StandardCharsets.UTF_8) == asString
    new String(bytesOut.toByteArray(), StandardCharsets.UTF_8) == asString
    charsOut.toString() == asString

    where:
    description | document | expectedJson
    "a resource with attributes" | directResourceDocument() | '{"data":{"type":"articles","id":"1","attributes":{"title":"Title"}}}'
    "a relationship linkage" | directRelationshipDocument() | '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"9"}}}}}'
    "a compound document" | directCompoundDocument() | '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"9"}}}},"included":[{"type":"people","id":"9","attributes":{"name":"Dan"}}]}'
    "explicit null data and meta" | directNullDataDocument() | '{"data":null,"meta":{"reason":"deleted"}}'
  }

  def "roundtrips fixture #path through reader and writer"() {
    given:
    def json = readCorpusText(path)
    def readContext = DocumentReadContext.of(
        context, primaryDataKind ?: PrimaryDataKind.RESOURCE)
    def reader = JsonApiJackson3.reader(mapper, readContext)
    def writer = JsonApiJackson3.writer(mapper, context)

    when:
    def document = reader.readValue(json)
    def actual = writer.writeValueAsString(document)

    then:
    mapper.readTree(actual) == mapper.readTree(json)

    where:
    path                                               | context                     | primaryDataKind
    'documents/single-resource.json'                   | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/resource-collection.json'               | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/single-identifier.json'                 | ValidationContext.defaults() | PrimaryDataKind.RESOURCE_IDENTIFIER
    'documents/identifier-collection.json'             | ValidationContext.defaults() | PrimaryDataKind.RESOURCE_IDENTIFIER
    'documents/null-data.json'                         | ValidationContext.defaults() | null
    'documents/meta-only.json'                         | ValidationContext.defaults() | null
    'documents/empty-identifier-collection.json'      | ValidationContext.defaults() | PrimaryDataKind.RESOURCE_IDENTIFIER
    'documents/empty-wrappers.json'                    | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/empty-errors.json'                      | ValidationContext.defaults() | null
    'documents/empty-included.json'                    | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/open-values.json'                       | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/relationship-null-linkage.json'         | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/relationship-empty-to-many.json'        | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/relationship-link-only.json'            | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/relationship-meta-only.json'            | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/string-and-object-links.json'           | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/errors-document.json'                   | ValidationContext.defaults() | null
    'documents/jsonapi-object.json'                    | extContext()                | PrimaryDataKind.RESOURCE
    'documents/compound-document.json'                 | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/compound-nested-intermediate.json'      | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/compound-shared-identity.json'          | ValidationContext.defaults() | PrimaryDataKind.RESOURCE
    'documents/local-identifier.json'                  | createContext()             | PrimaryDataKind.RESOURCE
    'documents/extension-and-at-members.json'          | extContext()                | PrimaryDataKind.RESOURCE
    'documents/member-order.json'                      | extContext()                | PrimaryDataKind.RESOURCE
  }

  def "emits exact UTF-8 member order"() {
    given:
    def context = extContext()
    def reader = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(context, PrimaryDataKind.RESOURCE))
    def document = reader.readValue(readCorpusText('documents/member-order.json'))
    def writer = JsonApiJackson3.writer(mapper, context)
    def expected = readCorpusText('documents/member-order.compact.json').trim()

    expect:
    writer.writeValueAsString(document) == expected
    new String(writer.writeValueAsBytes(document), StandardCharsets.UTF_8) == expected
  }

  def "emits array-form hreflang"() {
    given:
    def context = ValidationContext.defaults()
    def reader = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(context, PrimaryDataKind.RESOURCE))
    def document = reader.readValue(readCorpusText('documents/string-and-object-links.json'))
    def writer = JsonApiJackson3.writer(mapper, context)
    def json = writer.writeValueAsString(document)

    expect:
    json.contains('"hreflang":["en"]')
    !json.contains('"hreflang":"en"')
  }

  private static ValidationContext extContext() {
    return new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of('ext'),
        Set.of(),
        Set.of(),
        Set.of(),
        LinksContext.TOP_LEVEL,
        Map.of(),
        null)
  }

  private static ValidationContext createContext() {
    return new ValidationContext(
        DocumentUsage.CREATE_REQUEST,
        Set.of(),
        Set.of(),
        Set.of(),
        Set.of(),
        LinksContext.TOP_LEVEL,
        Map.of(),
        null)
  }

  private static String readCorpusText(String relativePath) {
    return TestFixtureResources.readCorpusUtf8(relativePath)
  }

  private static JsonApiDocument directResourceDocument() {
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes(["title": "Title"]),
        null,
        null,
        null,
        Map.of())
    return JsonApiDocument.withData(new DocumentData.SingleResource(resource))
  }

  private static JsonApiDocument directRelationshipDocument() {
    def relationships = new LinkedHashMap<String, Relationship>()
    relationships.put(
        "author",
        Relationship.withData(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9"))))
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(relationships),
        null,
        null,
        Map.of())
    return JsonApiDocument.withData(new DocumentData.SingleResource(resource))
  }

  private static JsonApiDocument directCompoundDocument() {
    def relationships = new LinkedHashMap<String, Relationship>()
    relationships.put(
        "author",
        Relationship.withData(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9"))))
    def primary = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(relationships),
        null,
        null,
        Map.of())
    def included = new ResourceObject(
        "people",
        "9",
        null,
        Attributes.ofAttributes(["name": "Dan"]),
        null,
        null,
        null,
        Map.of())
    return new JsonApiDocument(
        new DocumentData.SingleResource(primary), null, null, null, null, List.of(included), Map.of())
  }

  private static JsonApiDocument directNullDataDocument() {
    return new JsonApiDocument(
        DocumentData.NullData.INSTANCE,
        null,
        Meta.of(["reason": "deleted"]),
        null,
        null,
        null,
        Map.of())
  }
}
