package io.github.kazemek.jsonapi.jackson3

import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.ErrorObject
import io.github.kazemek.jsonapi.core.model.ErrorSource
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.diagnostic.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind
import io.github.kazemek.jsonapi.fixtures.TestFixtureResources

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DocumentReaderSpec extends Specification {

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Shared
  DocumentReadContext resourceContext = DocumentReadContext.resourceDefaults()

  def "corpus manifests match adapter-local tables and reference unique resources"() {
    expect:
    assertManifestEntries(readManifestEntries('manifest.json', 'fixtures'), readerCases())
    assertNegativeManifestEntries(
        readManifestEntries('negative-manifest.json', 'cases'), negativeCases())
    assertManifestEntries(readManifestEntries('ambiguous-manifest.json', 'cases'), ambiguousCases())
  }

  def "reads fixture #id into a document that matches the constructed model"() {
    given:
    def json = readCorpusText(path as String)
    def context = DocumentReadContext.of(
        validationContext as ValidationContext, primaryDataKind as PrimaryDataKind)
    def reader = JsonApiJackson3.reader(mapper, context)
    def writer = JsonApiJackson3.writer(mapper, validationContext as ValidationContext)

    when:
    def document = reader.readValue(json)

    then:
    document == expected || wireEqual(writer, document, expected as JsonApiDocument)
    mapper.readTree(writer.writeValueAsString(document)) == mapper.readTree(json)

    where:
    [
      id,
      path,
      validationContext,
      primaryDataKind,
      expected
    ] << readerCases().collect { testCase ->
      [
        testCase.id,
        testCase.path,
        testCase.validationContext,
        testCase.primaryDataKind,
        testCase.expected
      ]
    }
  }

  def "all read sources decode #id equivalently"() {
    given:
    def json = readCorpusText(path as String)
    def context = DocumentReadContext.of(
        validationContext as ValidationContext, primaryDataKind as PrimaryDataKind)
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
    [
      id,
      path,
      validationContext,
      primaryDataKind
    ] << readerCases().collect { testCase ->
      [
        testCase.id,
        testCase.path,
        testCase.validationContext,
        testCase.primaryDataKind
      ]
    }
  }

  def "ambiguous case #id decodes under both PrimaryDataKind values"() {
    given:
    def json = readCorpusText(path as String)

    when:
    def asResource = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(ValidationContext.defaults(), PrimaryDataKind.RESOURCE))
        .readValue(json)
    def asIdentifier = JsonApiJackson3.reader(
        mapper, DocumentReadContext.of(ValidationContext.defaults(), PrimaryDataKind.RESOURCE_IDENTIFIER))
        .readValue(json)

    then:
    asResource == resourceDocument
    asIdentifier == identifierDocument

    and:
    def writer = JsonApiJackson3.writer(mapper, ValidationContext.defaults())
    mapper.readTree(writer.writeValueAsString(asResource)) == mapper.readTree(json)
    mapper.readTree(writer.writeValueAsString(asIdentifier)) == mapper.readTree(json)

    where:
    [
      id,
      path,
      resourceDocument,
      identifierDocument
    ] << ambiguousCases().collect { testCase ->
      [
        testCase.id,
        testCase.path,
        testCase.resourceDocument,
        testCase.identifierDocument
      ]
    }
  }

  def "negative corpus case #id fails with the documented diagnostics"() {
    given:
    def json = readCorpusText(path as String)
    def reader = JsonApiJackson3.reader(mapper, resourceContext)

    when:
    reader.readValue(json)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.valueOf(category as String)

    and:
    pointer == null || ex.jsonPointer() == pointer
    ex.ruleCode() == (ruleCode == null ? null : ValidationRuleCode.valueOf(ruleCode as String))
    !sourceLocation || ex.sourceLocation().isKnown()
    ex.cause == null
    json.isEmpty() || !ex.message.contains(json)

    where:
    [
      id,
      path,
      category,
      pointer,
      ruleCode,
      sourceLocation
    ] << negativeCases().collect { testCase ->
      [
        testCase.id,
        testCase.path,
        testCase.category,
        testCase.pointer,
        testCase.ruleCode,
        testCase.sourceLocation
      ]
    }
  }

  @Unroll
  def "empty String input #description reports MALFORMED_JSON"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue(input)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.jsonPointer() == ''
    ex.message == 'Expected a JSON:API document object'

    where:
    description       | input
    'empty'           | ''
    'whitespace-only' | '   '
  }

  @Unroll
  def "empty byte input #description reports MALFORMED_JSON"() {
    when:
    JsonApiJackson3.reader(mapper, resourceContext).readValue(input)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.MALFORMED_JSON
    ex.jsonPointer() == ''
    ex.message == 'Expected a JSON:API document object'

    where:
    description       | input
    'empty'           | ''.getBytes(StandardCharsets.UTF_8)
    'whitespace-only' | '  '.getBytes(StandardCharsets.UTF_8)
  }

  def "aggregate validation resource location is precise on Jackson 3"() {
    given:
    def json = readCorpusText('negative/aggregate-validation-resource-location.json')

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
    def context = DocumentReadContext.of(extContext(), PrimaryDataKind.RESOURCE)
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


  private static boolean wireEqual(
      JsonApiDocumentWriter writer,
      JsonApiDocument actual,
      JsonApiDocument expected) {
    def mapper = JsonMapper.builder().build()
    return mapper.readTree(writer.writeValueAsString(actual)) ==
        mapper.readTree(writer.writeValueAsString(expected))
  }

  private static String readCorpusText(String relativePath) {
    return TestFixtureResources.readCorpusUtf8(relativePath)
  }

  private static JsonApiDocument singleResourceDocument() {
    Map<String, Object> attributes = new LinkedHashMap<>()
    attributes.put('title', 'JSON:API paints my bikeshed!')
    def article = resource('articles', '1', Attributes.ofAttributes(attributes))
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument resourceCollectionDocument() {
    Map<String, Object> firstAttributes = new LinkedHashMap<>()
    firstAttributes.put('title', 'First')
    Map<String, Object> secondAttributes = new LinkedHashMap<>()
    secondAttributes.put('title', 'Second')
    def first = resource('articles', '1', Attributes.ofAttributes(firstAttributes))
    def second = resource('articles', '2', Attributes.ofAttributes(secondAttributes))
    return JsonApiDocument.withData(
        new DocumentData.ResourceCollection(List.of(first, second)))
  }

  private static JsonApiDocument singleIdentifierDocument() {
    return JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of('articles', '1')))
  }

  private static JsonApiDocument identifierCollectionDocument() {
    return JsonApiDocument.withData(
        new DocumentData.IdentifierCollection(
        List.of(ResourceIdentifier.of('articles', '1'), ResourceIdentifier.of('articles', '2'))))
  }

  private static JsonApiDocument nullDataDocument() {
    Map<String, Object> meta = new LinkedHashMap<>()
    meta.put('reason', 'deleted')
    return new JsonApiDocument(
        DocumentData.NullData.INSTANCE, null, Meta.of(meta), null, null, null, Map.of())
  }

  private static JsonApiDocument metaOnlyDocument() {
    Map<String, Object> meta = new LinkedHashMap<>()
    meta.put('copyright', 'Copyright 2026')
    return JsonApiDocument.withMeta(Meta.of(meta))
  }

  private static JsonApiDocument emptyIdentifierCollectionDocument() {
    return JsonApiDocument.withData(
        new DocumentData.IdentifierCollection(List.of()))
  }

  private static JsonApiDocument emptyWrappersDocument() {
    def article = resourceWithAll(
        'articles', '1', Attributes.empty(), Relationships.empty(), Links.empty(), Meta.empty())
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument emptyErrorsDocument() {
    return JsonApiDocument.withErrors(List.of())
  }

  private static JsonApiDocument emptyIncludedDocument() {
    def article = resource('articles', '1')
    return new JsonApiDocument(
        new DocumentData.SingleResource(article), null, null, null, null, List.of(), Map.of())
  }

  private static JsonApiDocument openValuesDocument() {
    Map<String, Object> nested = new LinkedHashMap<>()
    nested.put('tags', List.of('a', 'b'))
    Map<String, Object> counts = new LinkedHashMap<>()
    counts.put('views', 2)
    nested.put('counts', counts)

    Map<String, Object> attributes = new LinkedHashMap<>()
    attributes.put('nullable', null)
    attributes.put('nested', nested)
    attributes.put('intValue', 42)
    attributes.put('longValue', 9007199254740991L)
    attributes.put('floatValue', 1.5f)
    attributes.put('doubleValue', 2.25d)
    attributes.put('bigIntValue', new BigInteger('123456789012345678901234567890'))
    attributes.put('bigDecimalValue', new BigDecimal('1234567890.123456789'))

    def article = resource('articles', '1', Attributes.ofAttributes(attributes))
    Map<String, Object> meta = new LinkedHashMap<>()
    meta.put('flag', true)
    meta.put('nullMeta', null)

    return new JsonApiDocument(
        new DocumentData.SingleResource(article), null, Meta.of(meta), null, null, null, Map.of())
  }

  private static JsonApiDocument relationshipNullLinkageDocument() {
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put('author', new Relationship(RelationshipData.NullLinkage.INSTANCE, null, null, Map.of()))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(relationships))
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument relationshipEmptyToManyDocument() {
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put(
        'comments',
        new Relationship(
        RelationshipData.IdentifierCollectionLinkage.empty(), null, null, Map.of()))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(relationships))
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument relationshipLinkOnlyDocument() {
    Map<String, Link> authorLinks = new LinkedHashMap<>()
    authorLinks.put('self', new Link.StringLink('http://example.com/articles/1/relationships/author'))
    authorLinks.put('related', new Link.StringLink('http://example.com/articles/1/author'))
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put('author', Relationship.linkOnly(Links.ofLinks(authorLinks)))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(relationships))
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument relationshipMetaOnlyDocument() {
    Map<String, Object> meta = new LinkedHashMap<>()
    meta.put('inferred', true)
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put('author', Relationship.metaOnly(Meta.of(meta)))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(relationships))
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument stringAndObjectLinksDocument() {
    String selfHref = 'http://example.com/articles/1'
    Map<String, Link> resourceLinks = new LinkedHashMap<>()
    resourceLinks.put('self', new Link.StringLink(selfHref))
    def article = resourceWithLinks('articles', '1', Links.ofLinks(resourceLinks))

    Map<String, Object> relatedMeta = new LinkedHashMap<>()
    relatedMeta.put('count', 1)
    def related = new Link.ObjectLink(
        'http://example.com/articles/1/related',
        'related',
        null,
        'Related',
        'application/vnd.api+json',
        List.of('en'),
        Meta.of(relatedMeta),
        Map.of())

    Map<String, Link> topLinkEntries = new LinkedHashMap<>()
    topLinkEntries.put('self', new Link.StringLink(selfHref))
    topLinkEntries.put('related', related)
    topLinkEntries.put('next', null)

    return new JsonApiDocument(
        new DocumentData.ResourceCollection(List.of(article)),
        null,
        null,
        null,
        Links.ofLinks(topLinkEntries),
        null,
        Map.of())
  }

  private static JsonApiDocument errorsDocument() {
    Map<String, Link> errorLinks = new LinkedHashMap<>()
    errorLinks.put('about', new Link.StringLink('http://example.com/docs/errors/invalid'))
    def error = new ErrorObject(
        '1',
        Links.ofLinks(errorLinks),
        '422',
        'invalid',
        'Invalid Attribute',
        'Title is required',
        new ErrorSource('/data/attributes/title', null, null, Map.of()),
        null,
        Map.of())
    return JsonApiDocument.withErrors(List.of(error))
  }

  private static JsonApiDocument jsonApiObjectDocument() {
    Map<String, Object> meta = new LinkedHashMap<>()
    meta.put('impl', 'jsonapi-java')
    def jsonapi = new JsonApiObject(
        '1.1',
        List.of('https://jsonapi.org/ext/atomic'),
        List.of('https://example.com/profiles/flex'),
        Meta.of(meta),
        Map.of())
    return new JsonApiDocument(
        new DocumentData.SingleResource(new ResourceObject('articles', '1', null, null, null, null, null, Map.of())),
        null,
        null,
        jsonapi,
        null,
        null,
        Map.of())
  }

  private static JsonApiDocument compoundDocument() {
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '9')),
        null,
        null,
        Map.of()))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(relationships))
    Map<String, Object> includedAttributes = new LinkedHashMap<>()
    includedAttributes.put('name', 'Dan')
    def included = resource('people', '9', Attributes.ofAttributes(includedAttributes))
    return new JsonApiDocument(
        new DocumentData.SingleResource(article), null, null, null, null, List.of(included), Map.of())
  }

  private static JsonApiDocument compoundNestedIntermediateDocument() {
    Map<String, Relationship> articleRelationships = new LinkedHashMap<>()
    articleRelationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '9')),
        null,
        null,
        Map.of()))
    articleRelationships.put(
        'comments',
        new Relationship(
        new RelationshipData.IdentifierCollectionLinkage(
        List.of(ResourceIdentifier.of('comments', '5'), ResourceIdentifier.of('comments', '12'))),
        null,
        null,
        Map.of()))
    def article = resourceWithRelationships('articles', '1', Relationships.ofRelationships(articleRelationships))

    Map<String, Relationship> comment5Relationships = new LinkedHashMap<>()
    comment5Relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '2')),
        null,
        null,
        Map.of()))
    def comment5 = resourceWithAttributesAndRelationships(
        'comments', '5', Attributes.ofAttributes(attribute('body', 'First!')),
        Relationships.ofRelationships(comment5Relationships))
    def person2 = resource('people', '2', Attributes.ofAttributes(attribute('name', 'Ezra')))

    Map<String, Relationship> comment12Relationships = new LinkedHashMap<>()
    comment12Relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '9')),
        null,
        null,
        Map.of()))
    def comment12 = resourceWithAttributesAndRelationships(
        'comments', '12', Attributes.ofAttributes(attribute('body', 'I like XML better')),
        Relationships.ofRelationships(comment12Relationships))
    def person9 = resource('people', '9', Attributes.ofAttributes(attribute('name', 'Dan')))

    return new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        List.of(comment5, comment12, person2, person9),
        Map.of())
  }

  private static JsonApiDocument compoundSharedIdentityDocument() {
    def article1 = resourceWithRelationships('articles', '1', sharedAuthorRelationships())
    def article2 = resourceWithRelationships('articles', '2', sharedAuthorRelationships())
    Map<String, Object> includedAttributes = new LinkedHashMap<>()
    includedAttributes.put('name', 'Dan')
    def included = resource('people', '9', Attributes.ofAttributes(includedAttributes))
    return new JsonApiDocument(
        new DocumentData.ResourceCollection(List.of(article1, article2)),
        null,
        null,
        null,
        null,
        List.of(included),
        Map.of())
  }

  private static JsonApiDocument localIdentifierDocument() {
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.withLid('people', 'temp-author')),
        null,
        null,
        Map.of()))
    def article = new ResourceObject(
        'articles', null, 'temp-1', null, Relationships.ofRelationships(relationships), null, null, Map.of())
    return JsonApiDocument.withData(new DocumentData.SingleResource(article))
  }

  private static JsonApiDocument extensionAndAtMembersDocument() {
    Map<String, Object> attributes = new LinkedHashMap<>()
    attributes.put('title', 'Hello')
    Map<String, Object> additionalMembers = new LinkedHashMap<>()
    additionalMembers.put('@copyright', 'Copyright 2026')
    additionalMembers.put('ext:version', 1)
    def article = new ResourceObject(
        'articles',
        '1',
        null,
        Attributes.ofAttributes(attributes),
        null,
        null,
        null,
        additionalMembers)
    Map<String, Object> documentMembers = new LinkedHashMap<>()
    documentMembers.put('ext:request-id', 'abc-123')
    return new JsonApiDocument(
        new DocumentData.SingleResource(article), null, null, null, null, null, documentMembers)
  }

  private static JsonApiDocument memberOrderDocument() {
    def self = new Link.StringLink('http://example.com/articles/1')
    Map<String, Object> attributes = new LinkedHashMap<>()
    attributes.put('title', 'Ordered')
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '9')),
        null,
        null,
        Map.of()))
    Map<String, Link> resourceLinks = new LinkedHashMap<>()
    resourceLinks.put('self', self)
    Map<String, Object> resourceMeta = new LinkedHashMap<>()
    resourceMeta.put('created', '2026-01-01')
    Map<String, Object> resourceMembers = new LinkedHashMap<>()
    resourceMembers.put('ext:flag', true)
    def article = new ResourceObject(
        'articles',
        '1',
        'temp-1',
        Attributes.ofAttributes(attributes),
        Relationships.ofRelationships(relationships),
        Links.ofLinks(resourceLinks),
        Meta.of(resourceMeta),
        resourceMembers)

    Map<String, Object> documentMeta = new LinkedHashMap<>()
    documentMeta.put('copyright', 'Copyright 2026')
    Map<String, Link> documentLinks = new LinkedHashMap<>()
    documentLinks.put('self', self)
    Map<String, Object> documentMembers = new LinkedHashMap<>()
    documentMembers.put('ext:trace', 't-1')
    return new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        Meta.of(documentMeta),
        JsonApiObject.ofVersion('1.1'),
        Links.ofLinks(documentLinks),
        List.of(ResourceObject.of('people', '9')),
        documentMembers)
  }

  private static JsonApiDocument ambiguousObjectResourceDocument() {
    return JsonApiDocument.withData(
        new DocumentData.SingleResource(new ResourceObject('articles', '1', null, null, null, null, null, Map.of())))
  }

  private static JsonApiDocument ambiguousObjectIdentifierDocument() {
    return JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of('articles', '1')))
  }

  private static JsonApiDocument ambiguousEmptyArrayResourceDocument() {
    return JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of()))
  }

  private static JsonApiDocument ambiguousEmptyArrayIdentifierDocument() {
    return JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of()))
  }

  private static ResourceObject resource(String type, String id) {
    return new ResourceObject(type, id, null, null, null, null, null, Map.of())
  }

  private static ResourceObject resource(String type, String id, Attributes attributes) {
    return new ResourceObject(type, id, null, attributes, null, null, null, Map.of())
  }

  private static ResourceObject resourceWithRelationships(
      String type, String id, Relationships relationships) {
    return new ResourceObject(type, id, null, null, relationships, null, null, Map.of())
  }

  private static ResourceObject resourceWithLinks(String type, String id, Links links) {
    return new ResourceObject(type, id, null, null, null, links, null, Map.of())
  }

  private static ResourceObject resourceWithAll(
      String type,
      String id,
      Attributes attributes,
      Relationships relationships,
      Links links,
      Meta meta) {
    return new ResourceObject(type, id, null, attributes, relationships, links, meta, Map.of())
  }

  private static ResourceObject resourceWithAttributesAndRelationships(
      String type, String id, Attributes attributes, Relationships relationships) {
    return new ResourceObject(type, id, null, attributes, relationships, null, null, Map.of())
  }

  private static Relationships sharedAuthorRelationships() {
    Map<String, Relationship> relationships = new LinkedHashMap<>()
    relationships.put(
        'author',
        new Relationship(
        new RelationshipData.SingleLinkage(ResourceIdentifier.of('people', '9')),
        null,
        null,
        Map.of()))
    return Relationships.ofRelationships(relationships)
  }

  private static Map<String, Object> attribute(String name, String value) {
    Map<String, Object> attributes = new LinkedHashMap<>()
    attributes.put(name, value)
    return attributes
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

  private static List<ReaderCase> readerCases() {
    [
      new ReaderCase(
      'single-resource',
      'documents/single-resource.json',
      'Single resource primary data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      singleResourceDocument()),
      new ReaderCase(
      'resource-collection',
      'documents/resource-collection.json',
      'Resource collection primary data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      resourceCollectionDocument()),
      new ReaderCase(
      'single-identifier',
      'documents/single-identifier.json',
      'Single resource identifier primary data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE_IDENTIFIER,
      singleIdentifierDocument()),
      new ReaderCase(
      'identifier-collection',
      'documents/identifier-collection.json',
      'Identifier collection primary data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE_IDENTIFIER,
      identifierCollectionDocument()),
      new ReaderCase(
      'null-data',
      'documents/null-data.json',
      'Explicit data null with meta',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      nullDataDocument()),
      new ReaderCase(
      'meta-only',
      'documents/meta-only.json',
      'Absent data; meta-only document',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      metaOnlyDocument()),
      new ReaderCase(
      'empty-identifier-collection',
      'documents/empty-identifier-collection.json',
      'Empty primary data array',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE_IDENTIFIER,
      emptyIdentifierCollectionDocument()),
      new ReaderCase(
      'empty-wrappers',
      'documents/empty-wrappers.json',
      'Present-empty attributes, relationships, links, meta',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      emptyWrappersDocument()),
      new ReaderCase(
      'empty-errors',
      'documents/empty-errors.json',
      'Present-empty errors array',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      emptyErrorsDocument()),
      new ReaderCase(
      'empty-included',
      'documents/empty-included.json',
      'Present-empty included array with primary data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      emptyIncludedDocument()),
      new ReaderCase(
      'open-values',
      'documents/open-values.json',
      'Open JSON null, nested object/array, and numeric families in attributes/meta',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      openValuesDocument()),
      new ReaderCase(
      'relationship-null-linkage',
      'documents/relationship-null-linkage.json',
      'Explicit null to-one relationship data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      relationshipNullLinkageDocument()),
      new ReaderCase(
      'relationship-empty-to-many',
      'documents/relationship-empty-to-many.json',
      'Empty to-many relationship data array',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      relationshipEmptyToManyDocument()),
      new ReaderCase(
      'relationship-link-only',
      'documents/relationship-link-only.json',
      'Link-only relationship without data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      relationshipLinkOnlyDocument()),
      new ReaderCase(
      'relationship-meta-only',
      'documents/relationship-meta-only.json',
      'Meta-only relationship without data',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      relationshipMetaOnlyDocument()),
      new ReaderCase(
      'string-and-object-links',
      'documents/string-and-object-links.json',
      'String link, object link, null link, canonical hreflang array',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      stringAndObjectLinksDocument()),
      new ReaderCase(
      'errors-document',
      'documents/errors-document.json',
      'Top-level errors with source and links',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      errorsDocument()),
      new ReaderCase(
      'jsonapi-object',
      'documents/jsonapi-object.json',
      'jsonapi version, ext, profile, and meta',
      extContext(),
      PrimaryDataKind.RESOURCE,
      jsonApiObjectDocument()),
      new ReaderCase(
      'compound-document',
      'documents/compound-document.json',
      'Compound document with included resources',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      compoundDocument()),
      new ReaderCase(
      'compound-nested-intermediate',
      'documents/compound-nested-intermediate.json',
      'Compound document with nested comments.author intermediates',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      compoundNestedIntermediateDocument()),
      new ReaderCase(
      'compound-shared-identity',
      'documents/compound-shared-identity.json',
      'Compound collection sharing one included author identity',
      ValidationContext.defaults(),
      PrimaryDataKind.RESOURCE,
      compoundSharedIdentityDocument()),
      new ReaderCase(
      'local-identifier',
      'documents/local-identifier.json',
      'Resource and linkage with lid',
      createContext(),
      PrimaryDataKind.RESOURCE,
      localIdentifierDocument()),
      new ReaderCase(
      'extension-and-at-members',
      'documents/extension-and-at-members.json',
      'Extension and @ members on document and resource',
      extContext(),
      PrimaryDataKind.RESOURCE,
      extensionAndAtMembersDocument()),
      new ReaderCase(
      'member-order',
      'documents/member-order.json',
      'Canonical standard member order then additional members',
      extContext(),
      PrimaryDataKind.RESOURCE,
      memberOrderDocument())
    ]
  }

  private static List<NegativeCase> negativeCases() {
    [
      new NegativeCase(
      'malformed-json-without-payload',
      'negative/malformed-json-without-payload.json',
      'MALFORMED_JSON',
      null,
      null,
      false,
      'Unterminated object; diagnostics must not echo the source text'),
      new NegativeCase(
      'truncated-document-enclosing-path',
      'negative/truncated-document-enclosing-path.json',
      'MALFORMED_JSON',
      '/data',
      null,
      false,
      'End-of-input inside primary data; pointer captures the enclosing path'),
      new NegativeCase(
      'empty-input',
      'negative/empty-input.json',
      'MALFORMED_JSON',
      '',
      null,
      false,
      'Empty input and whitespace-only variants'),
      new NegativeCase(
      'trailing-content-after-document',
      'negative/trailing-content-after-document.json',
      'UNEXPECTED_TOKEN',
      '',
      null,
      false,
      'Whole-input reads reject content after the document'),
      new NegativeCase(
      'unexpected-token-path-location',
      'negative/unexpected-token-path-location.json',
      'UNEXPECTED_TOKEN',
      '',
      null,
      true,
      'Wrong root token reports path and location'),
      new NegativeCase(
      'duplicate-members',
      'negative/duplicate-members.json',
      'DUPLICATE_MEMBER',
      '/meta',
      null,
      true,
      'Repeated object member is rejected'),
      new NegativeCase(
      'local-validation-top-level',
      'negative/local-validation-top-level.json',
      'LOCAL_VALIDATION',
      '/data/type',
      'MISSING_RESOURCE_TYPE',
      false,
      'Core constructor failure exposes a rule code and top-level path'),
      new NegativeCase(
      'included-missing-type',
      'negative/included-missing-type.json',
      'LOCAL_VALIDATION',
      '/included/0/type',
      'MISSING_RESOURCE_TYPE',
      false,
      'Included resource without type reports a nested pointer'),
      new NegativeCase(
      'collection-missing-type',
      'negative/collection-missing-type.json',
      'LOCAL_VALIDATION',
      '/data/0/type',
      'MISSING_RESOURCE_TYPE',
      false,
      'Collection element without type reports an indexed pointer'),
      new NegativeCase(
      'relationship-identifier-missing-type',
      'negative/relationship-identifier-missing-type.json',
      'LOCAL_VALIDATION',
      '/data/relationships/author/data/type',
      'MISSING_RESOURCE_TYPE',
      false,
      'Relationship identifier without type reports a nested pointer'),
      new NegativeCase(
      'reserved-attribute',
      'negative/reserved-attribute.json',
      'LOCAL_VALIDATION',
      '/data/attributes/type',
      'RESERVED_FIELD_NAME',
      false,
      'Reserved member name inside attributes'),
      new NegativeCase(
      'missing-link-href',
      'negative/missing-link-href.json',
      'LOCAL_VALIDATION',
      '/data/links/self/href',
      'NULL_REQUIRED_VALUE',
      false,
      'Object-form link without href'),
      new NegativeCase(
      'invalid-dynamic-link-relation',
      'negative/invalid-dynamic-link-relation.json',
      'LOCAL_VALIDATION',
      '/links/foo~0bar~1baz',
      'INVALID_LINK_RELATION',
      false,
      'Dynamic link relation escapes pointer segments'),
      new NegativeCase(
      'invalid-dynamic-attribute-name',
      'negative/invalid-dynamic-attribute-name.json',
      'LOCAL_VALIDATION',
      '/data/attributes/foo~0bar~1baz',
      'INVALID_MEMBER_NAME',
      true,
      'Dynamic attribute name escapes pointer segments'),
      new NegativeCase(
      'aggregate-uri-link-relation',
      'negative/aggregate-uri-link-relation.json',
      'AGGREGATE_VALIDATION',
      '/links/http:~1~1example.com~1rel',
      'INVALID_LINKS_CONTEXT',
      true,
      'Aggregate link-context failure escapes pointer segments'),
      new NegativeCase(
      'aggregate-validation-resource-location',
      'negative/aggregate-validation-resource-location.json',
      'AGGREGATE_VALIDATION',
      '/data/1',
      'DUPLICATE_RESOURCE_IDENTITY',
      true,
      'Aggregate failure locates the offending resource; Jackson 3 also asserts line 4'),
      new NegativeCase(
      'extension-members-require-context',
      'documents/extension-and-at-members.json',
      'AGGREGATE_VALIDATION',
      '/ext:request-id',
      'DISALLOWED_ADDITIONAL_MEMBER',
      false,
      'Valid extension document fails under a context without the extension namespace')
    ]
  }

  private static List<AmbiguousCase> ambiguousCases() {
    [
      new AmbiguousCase(
      'ambiguous-object-primary-data',
      'documents/ambiguous-object-primary-data.json',
      'Object primary data whose decoded model depends on the read PrimaryDataKind',
      ambiguousObjectResourceDocument(),
      ambiguousObjectIdentifierDocument()),
      new AmbiguousCase(
      'ambiguous-empty-array-primary-data',
      'documents/ambiguous-empty-array-primary-data.json',
      'Empty-array primary data whose decoded model depends on the read PrimaryDataKind',
      ambiguousEmptyArrayResourceDocument(),
      ambiguousEmptyArrayIdentifierDocument())
    ]
  }

  private static List<Map<String, Object>> readManifestEntries(String file, String member) {
    def manifest = new JsonSlurper().parseText(readCorpusText(file)) as Map<String, Object>
    assert manifest.version == '1.1'
    assert manifest[member] instanceof List
    manifest[member] as List<Map<String, Object>>
  }

  private static boolean assertManifestEntries(
      List<Map<String, Object>> entries, List<?> expectedCases) {
    assert entries.collect { it.id as String } == expectedCases*.id
    assert entries.collect { it.path as String } == expectedCases*.path
    assert entries.collect { it.notes as String } == expectedCases*.notes
    assert entries*.id.toSet().size() == entries.size()
    assert entries*.path.toSet().size() == entries.size()
    assert entries.every { entry ->
      entry.id instanceof String && !entry.id.isBlank() &&
          entry.path instanceof String && !entry.path.isBlank() &&
          TestFixtureResources.corpusExists(entry.path as String)
    }
    true
  }

  private static boolean assertNegativeManifestEntries(
      List<Map<String, Object>> entries, List<NegativeCase> expectedCases) {
    assert entries.collect { entry ->
      [id: entry.id, path: entry.path]
    } == expectedCases.collect { testCase ->
      [id: testCase.id, path: testCase.path]
    }
    assert entries*.id.toSet().size() == entries.size()
    assert entries*.path.toSet().size() == entries.size()
    assert entries.every { entry ->
      entry.keySet() == ["id", "path", "notes"] as Set &&
      entry.id instanceof String && !entry.id.isBlank() &&
      entry.path instanceof String && !entry.path.isBlank() &&
      entry.notes instanceof String && !entry.notes.isBlank() &&
      TestFixtureResources.corpusExists(entry.path as String)
    }
    true
  }

  private static final class ReaderCase {
    final String id
    final String path
    final String notes
    final ValidationContext validationContext
    final PrimaryDataKind primaryDataKind
    final JsonApiDocument expected

    ReaderCase(
    String id,
    String path,
    String notes,
    ValidationContext validationContext,
    PrimaryDataKind primaryDataKind,
    JsonApiDocument expected) {
      this.id = id
      this.path = path
      this.notes = notes
      this.validationContext = validationContext
      this.primaryDataKind = primaryDataKind
      this.expected = expected
    }
  }

  private static final class NegativeCase {
    final String id
    final String path
    final String category
    final String pointer
    final String ruleCode
    final boolean sourceLocation
    final String notes

    NegativeCase(
    String id,
    String path,
    String category,
    String pointer,
    String ruleCode,
    boolean sourceLocation,
    String notes) {
      this.id = id
      this.path = path
      this.category = category
      this.pointer = pointer
      this.ruleCode = ruleCode
      this.sourceLocation = sourceLocation
      this.notes = notes
    }
  }

  private static final class AmbiguousCase {
    final String id
    final String path
    final String notes
    final JsonApiDocument resourceDocument
    final JsonApiDocument identifierDocument

    AmbiguousCase(
    String id,
    String path,
    String notes,
    JsonApiDocument resourceDocument,
    JsonApiDocument identifierDocument) {
      this.id = id
      this.path = path
      this.notes = notes
      this.resourceDocument = resourceDocument
      this.identifierDocument = identifierDocument
    }
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
