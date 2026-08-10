package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.DomainData
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.IncludedResources
import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.jackson3.testmodel.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.EmptyResourceType
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatLidArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatMappedArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatNode
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatStrictArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatThrowingArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.InvalidResourceType
import io.github.kazemek.jsonapi.jackson3.testmodel.Person
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixtures
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import spock.lang.Specification
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule

class DomainDocumentReaderSpec extends Specification {

  // Binding-shaped variant of the canonical `single-resource` fixture: the canonical document
  // carries no relationships and no `body-text`, so it cannot exercise relationship linkage
  // binding; the remaining documents reuse the canonical fixtures under fixtures/jsonapi-1.1.
  private static final String SINGLE_RESOURCE =
  '''
      {
        "data": {
          "type": "articles",
          "id": "1",
          "attributes": {
            "title": "JSON:API paints my bikeshed!",
            "body-text": "Content"
          },
          "relationships": {
            "author": {
              "data": {
                "type": "people",
                "id": "p1"
              }
            },
            "comments": {
              "data": [
                {
                  "type": "comments",
                  "id": "c1"
                }
              ]
            }
          }
        }
      }
      '''

  private static final String HETEROGENEOUS_COLLECTION =
  '''
      {
        "data": [
          {
            "type": "articles",
            "id": "1",
            "attributes": {
              "title": "First"
            }
          },
          {
            "type": "people",
            "id": "9",
            "attributes": {
              "name": "Dan"
            }
          }
        ]
      }
      '''

  // Document-level @ member for envelope `additionalMembers` coverage; the canonical
  // `extension-and-at-members` fixture keeps its @/ext members on the resource (an envelope
  // non-goal), so the fixture read asserts the ext member and this doc asserts the @ member.
  private static final String AT_MEMBER_DOCUMENT =
  '''
      {
        "data": {
          "type": "articles",
          "id": "1",
          "attributes": {
            "title": "Hello"
          }
        },
        "@request-id": "req-1"
      }
      '''

  // Positive: primary data states

  def "binds a single-resource document into a flat DTO envelope"() {
    given:
    def reader = newReader(FlatArticle)

    when:
    def envelope = reader.readValue(SINGLE_RESOURCE)

    then:
    envelope.data() instanceof DomainData.SingleResource
    def article = ((DomainData.SingleResource) envelope.data()).resource() as FlatArticle
    article.id() == "1"
    article.title() == "JSON:API paints my bikeshed!"
    article.body() == "Content"
    article.author() == ResourceIdentifier.of("people", "p1")
    article.comments() == [
      ResourceIdentifier.of("comments", "c1")
    ]
    envelope.errors() == null
    envelope.meta() == null
    envelope.jsonapi() == null
    envelope.links() == null
    envelope.included() == null
    envelope.additionalMembers().isEmpty()
  }

  def "binds a homogeneous resource collection in wire order"() {
    given:
    def reader = fixtureReader('resource-collection', FlatArticle)

    when:
    def envelope = reader.readValue(fixtureText('resource-collection'))

    then:
    envelope.data() instanceof DomainData.ResourceCollection
    def articles = ((DomainData.ResourceCollection) envelope.data()).resources()
    articles.size() == 2
    articles*.title == ["First", "Second"]
  }

  def "binds a heterogeneous collection through the registry"() {
    given:
    def reader = newReader(FlatArticle, Person)

    when:
    def envelope = reader.readValue(HETEROGENEOUS_COLLECTION)

    then:
    def resources = ((DomainData.ResourceCollection) envelope.data()).resources()
    resources == [
      new FlatArticle("1", "First", null, null, null),
      new Person("9", "Dan")
    ]
  }

  def "preserves explicit null data as NullData"() {
    given:
    def reader = fixtureReader('null-data')

    when:
    def envelope = reader.readValue(fixtureText('null-data'))

    then:
    envelope.data() == DomainData.NullData.INSTANCE
    envelope.meta().members().get("reason") == "deleted"
  }

  def "preserves absent data on a meta-only document"() {
    given:
    def reader = fixtureReader('meta-only')

    when:
    def envelope = reader.readValue(fixtureText('meta-only'))

    then:
    envelope.data() == null
    envelope.meta().members().get("copyright") == "Copyright 2026"
  }

  def "passes through identifier primary data without DTO binding"() {
    given:
    def reader = JsonApiJackson3.domainDocumentReader(
        JsonMapper.builder().build(), DocumentReadContext.identifierDefaults(), registry())

    when:
    def single = reader.readValue(fixtureText('single-identifier'))
    def collection = reader.readValue(fixtureText('identifier-collection'))

    then:
    single.data() == new DomainData.SingleIdentifier(ResourceIdentifier.of("articles", "1"))
    collection.data() instanceof DomainData.IdentifierCollection
    ((DomainData.IdentifierCollection) collection.data()).identifiers() ==
        [
          ResourceIdentifier.of("articles", "1"),
          ResourceIdentifier.of("articles", "2")
        ]
  }

  def "preserves errors without binding anything"() {
    given:
    def reader = fixtureReader('errors-document')

    when:
    def envelope = reader.readValue(fixtureText('errors-document'))

    then:
    envelope.data() == null
    envelope.errors() != null
    envelope.errors().size() == 1
    envelope.errors().get(0).status() == "422"
    envelope.errors().get(0).title() == "Invalid Attribute"
    envelope.errors().get(0).source().pointer() == "/data/attributes/title"
  }

  def "preserves jsonapi object, nullable links, and additional members"() {
    given:
    def reader = newReader(FlatArticle)
    def jsonapiReader = fixtureReader('jsonapi-object', FlatArticle)
    def extensionReader = fixtureReader('extension-and-at-members', FlatArticle)

    when:
    def jsonapi = jsonapiReader.readValue(fixtureText('jsonapi-object'))
    def links = reader.readValue(fixtureText('string-and-object-links'))
    def additional = extensionReader.readValue(fixtureText('extension-and-at-members'))
    def atMember = reader.readValue(AT_MEMBER_DOCUMENT)

    then:
    jsonapi.jsonapi().version() == "1.1"
    jsonapi.jsonapi().ext() == [
      "https://jsonapi.org/ext/atomic"
    ]
    jsonapi.jsonapi().profile() == [
      "https://example.com/profiles/flex"
    ]
    jsonapi.jsonapi().meta().members().get("impl") == "jsonapi-java"
    links.links().links().get("next") == null
    links.links().links().get("self") instanceof Link
    links.data() instanceof DomainData.ResourceCollection
    ((DomainData.ResourceCollection) links.data()).resources() == [
      new FlatArticle("1", null, null, null, null)
    ]
    additional.additionalMembers().get("ext:request-id") == "abc-123"
    atMember.additionalMembers().get("@request-id") == "req-1"
    ((DomainData.SingleResource) additional.data()).resource() as FlatArticle ==
        new FlatArticle("1", "Hello", null, null, null)
  }

  def "absent included stays null while present-empty included is a non-null empty IncludedResources"() {
    given:
    def reader = newReader(FlatArticle)

    when:
    def absent = reader.readValue(SINGLE_RESOURCE)
    def empty = reader.readValue(fixtureText('empty-included'))

    then:
    absent.included() == null
    empty.included() != null
    empty.included().resources().isEmpty()
    empty.included().find(ResourceIdentity.ofId("people", "9")).isEmpty()
  }

  // Positive: included binding

  def "binds included resources preserving wire order with identity lookup"() {
    given:
    def reader = fixtureReader('compound-document', FlatArticle, Person)

    when:
    def envelope = reader.readValue(fixtureText('compound-document'))

    then:
    def article = ((DomainData.SingleResource) envelope.data()).resource() as FlatArticle
    article.author() == ResourceIdentifier.of("people", "9")
    def included = envelope.included()
    included.resources() == [new Person("9", "Dan")]
    included.find(ResourceIdentity.ofId("people", "9")).get() == new Person("9", "Dan")
    included.find(ResourceIdentity.ofLid("people", "9")).isEmpty()
  }

  def "compound shared identity binds one included DTO reachable from both primary resources"() {
    given:
    def reader = fixtureReader('compound-shared-identity', FlatArticle, Person)

    when:
    def envelope = reader.readValue(fixtureText('compound-shared-identity'))

    then:
    def resources = ((DomainData.ResourceCollection) envelope.data()).resources()
    resources == [
      new FlatArticle("1", null, null, ResourceIdentifier.of("people", "9"), null),
      new FlatArticle("2", null, null, ResourceIdentifier.of("people", "9"), null)
    ]
    envelope.included().resources() == [new Person("9", "Dan")]
    envelope.included().find(ResourceIdentity.ofId("people", "9")).get() == new Person("9", "Dan")
  }

  def "shared identity yields one DTO instance reachable from both id and lid keys"() {
    given:
    def reader = newReader(FlatArticle, Person)
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
        null,
        null,
        null,
        null,
        [
          new ResourceObject("people", "9", "tmp-9", Attributes.ofAttributes([name: "Dan"]), null, null, null, Map.of())
        ],
        Map.of())

    when:
    def envelope = reader.fromDocument(document)

    then:
    def dto = new Person("9", "Dan")
    envelope.included().resources() == [dto]
    def byId = envelope.included().find(ResourceIdentity.ofId("people", "9"))
    def byLid = envelope.included().find(ResourceIdentity.ofLid("people", "tmp-9"))
    byId.get() == dto
    byLid.get() == dto
    byId.get().is(byLid.get())
  }

  def "fromDocument fails fast on duplicate included identities"() {
    given:
    def reader = newReader(FlatArticle, Person)
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
        null,
        null,
        null,
        null,
        [
          new ResourceObject("people", "9", null, Attributes.ofAttributes([name: "Dan"]), null, null, null, Map.of()),
          new ResourceObject("people", "9", null, Attributes.ofAttributes([name: "Other"]), null, null, null, Map.of())
        ],
        Map.of())

    when:
    reader.fromDocument(document)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION
    ex.propertyPath() == "/included/1"
    ex.resourceClass() == null
  }

  // Positive: metaAs

  def "metaAs returns null for both overloads when meta is absent"() {
    given:
    def reader = newReader(FlatArticle)

    when:
    def envelope = reader.readValue(SINGLE_RESOURCE)
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

  // Negative: registry and binder diagnostics

  def "unregistered resource-shaped primary fails at the document pointer with null resourceClass"() {
    given:
    def reader = newReader()
    def collectionReader = newReader()
    // Ambiguous type/id objects decoded as resources under resourceDefaults(); the identifier
    // pass-through case above keeps the identifier fixtures.
    def singleResource = '{"data":{"type":"articles","id":"1"}}'
    def resourceCollection =
        '''
        {
          "data": [
            {
              "type": "articles",
              "id": "1"
            },
            {
              "type": "articles",
              "id": "2"
            }
          ]
        }
        '''

    when:
    reader.readValue(singleResource)
    then:
    def single = thrown(JsonApiMappingException)
    single.diagnostic() == MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE
    single.propertyPath() == "/data"
    single.resourceClass() == null

    when:
    collectionReader.readValue(resourceCollection)
    then:
    def collection = thrown(JsonApiMappingException)
    collection.diagnostic() == MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE
    collection.propertyPath() == "/data/0"
    collection.resourceClass() == null
  }

  def "unregistered included type fails at the included index"() {
    given:
    def reader = newReader(FlatArticle)

    when:
    reader.readValue(fixtureText('compound-document'))

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE
    ex.propertyPath() == "/included/0"
    ex.resourceClass() == null
  }

  def "duplicate registry type names fail at build with the later registrant"() {
    when:
    registry(FlatArticle, FlatLidArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION
    ex.propertyPath() == "articles"
    ex.resourceClass() == FlatLidArticle
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
    def envelope = reader.readValue(HETEROGENEOUS_COLLECTION)

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

  def "registration rejects missing, empty, and invalid resource annotations"() {
    when:
    registry(FlatAuthor)
    then:
    def missing = thrown(JsonApiMappingException)
    missing.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    missing.resourceClass() == FlatAuthor

    when:
    registry(EmptyResourceType)
    then:
    def empty = thrown(JsonApiMappingException)
    empty.diagnostic() == MappingDiagnostic.INVALID_RESOURCE_TYPE
    empty.resourceClass() == EmptyResourceType

    when:
    registry(InvalidResourceType)
    then:
    def invalid = thrown(JsonApiMappingException)
    invalid.diagnostic() == MappingDiagnostic.INVALID_RESOURCE_TYPE
    invalid.resourceClass() == InvalidResourceType
  }

  def "binder failures surface with the document pointer joined to the binder path"() {
    given:
    def reader = newReader(FlatArticle, Person, FlatStrictArticle)

    when:
    reader.readValue(
        '''
        {
          "data": [
            {
              "type": "articles",
              "id": "1",
              "relationships": {
                "author": {
                  "data": [
                    {
                      "type": "people",
                      "id": "p1"
                    }
                  ]
                }
              }
            }
          ]
        }
        ''')
    then:
    def collection = thrown(JsonApiMappingException)
    collection.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    collection.propertyPath() == "/data/0/relationships/author/data"

    when:
    reader.readValue(
        '''
        {
          "data": {
            "type": "articles",
            "id": "1",
            "relationships": {
              "author": {
                "data": [
                  {
                    "type": "people",
                    "id": "p1"
                  }
                ]
              }
            }
          }
        }
        ''')
    then:
    def single = thrown(JsonApiMappingException)
    single.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    single.propertyPath() == "/data/relationships/author/data"

    when:
    reader.readValue(
        '''
        {
          "data": {
            "type": "articles",
            "id": "1",
            "relationships": {
              "author": {
                "data": {
                  "type": "people",
                  "id": "9"
                }
              },
              "comments": {
                "data": [
                  {
                    "type": "strict-articles",
                    "id": "2"
                  }
                ]
              }
            }
          },
          "included": [
            {
              "type": "people",
              "id": "9",
              "attributes": {
                "name": "Dan"
              }
            },
            {
              "type": "strict-articles",
              "id": "2",
              "attributes": {
                "title": "boom"
              }
            }
          ]
        }
        ''')
    then:
    def included = thrown(JsonApiMappingException)
    included.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    included.propertyPath() == "/included/1/title"
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

  def "root-level binder failures join to the document pointer without a trailing slash"() {
    given:
    def reader = newReader(FlatThrowingArticle)

    when:
    reader.readValue(
        '''
        {
          "data": {
            "type": "throwing-articles",
            "id": "1",
            "attributes": {
              "title": "boom"
            }
          }
        }
        ''')

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.propertyPath() == "/data"
  }

  // Isolation and immutability

  def "cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate"() {
    given:
    def reader = newReader(FlatNode)
    def json =
        '''
        {
          "data": {
            "type": "nodes",
            "id": "1",
            "relationships": {
              "parent": {
                "data": {
                  "type": "nodes",
                  "id": "2"
                }
              }
            }
          },
          "included": [
            {
              "type": "nodes",
              "id": "2",
              "relationships": {
                "parent": {
                  "data": {
                    "type": "nodes",
                    "id": "1"
                  }
                }
              }
            }
          ]
        }
        '''

    when:
    def envelope = reader.readValue(json)

    then:
    ((DomainData.SingleResource) envelope.data()).resource() as FlatNode ==
        new FlatNode("1", ResourceIdentifier.of("nodes", "2"))
    envelope.included().resources() == [
      new FlatNode("2", ResourceIdentifier.of("nodes", "1"))
    ]
  }

  def "independent envelopes sharing linkage never inject included DTOs"() {
    given:
    def reader = newReader(FlatArticle, Person)
    def article = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        author: Relationship.withData(new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "9")))),
        null,
        null,
        Map.of())
    def matching = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        [
          new ResourceObject("people", "9", null, Attributes.ofAttributes([name: "Dan"]), null, null, null, Map.of())
        ],
        Map.of())
    def unrelated = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        [
          new ResourceObject("people", "99", null, Attributes.ofAttributes([name: "Other"]), null, null, null, Map.of())
        ],
        Map.of())

    when:
    def withMatching = reader.fromDocument(matching)
    def withUnrelated = reader.fromDocument(unrelated)

    then:
    def primaryA = ((DomainData.SingleResource) withMatching.data()).resource() as FlatArticle
    def primaryB = ((DomainData.SingleResource) withUnrelated.data()).resource() as FlatArticle
    primaryA.author() == primaryB.author()
    primaryA.author() == ResourceIdentifier.of("people", "9")
    withMatching.included().resources() == [new Person("9", "Dan")]
    withUnrelated.included().resources() == [new Person("99", "Other")]
  }

  def "reader-derived envelope collections are mutation-safe"() {
    given:
    def reader = newReader(FlatArticle, Person)
    def envelope = reader.readValue(fixtureText('compound-document'))
    def errorEnvelope = reader.readValue(fixtureText('errors-document'))

    expect:
    envelope.included() != null
    envelope.included().resources() == [
      new Person("9", "Dan")
    ]
    envelope.included().find(ResourceIdentity.ofId("people", "9")).get() == new Person("9", "Dan")

    when:
    envelope.additionalMembers().put("k", "v")
    then:
    thrown(UnsupportedOperationException)

    when:
    errorEnvelope.errors().add(null)
    then:
    thrown(UnsupportedOperationException)

    when:
    envelope.included().resources().add("z")
    then:
    thrown(UnsupportedOperationException)
  }

  // Ownership

  def "caller-owned stream and parser remain open on success and failure"() {
    given:
    def reader = newReader(FlatArticle)
    def successStream = new CloseTrackingInputStream(new ByteArrayInputStream(SINGLE_RESOURCE.bytes))
    def failureStream = new CloseTrackingInputStream(new ByteArrayInputStream('{"data":'.bytes))
    def parser = JsonMapper.builder().build().createParser(SINGLE_RESOURCE)

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
    def duplicateIdentity =
        '''
        {
          "data": {
            "type": "articles",
            "id": "1",
            "relationships": {
              "author": {
                "data": {
                  "type": "people",
                  "id": "9"
                }
              }
            }
          },
          "included": [
            {
              "type": "people",
              "id": "9",
              "attributes": {
                "name": "Dan"
              }
            },
            {
              "type": "people",
              "id": "9",
              "attributes": {
                "name": "Other"
              }
            }
          ]
        }
        '''

    when:
    reader.readValue(duplicateIdentity)

    then:
    def ex = thrown(JsonApiDocumentReadException)
    ex.category() == CodecFailureCategory.AGGREGATE_VALIDATION
    ex.ruleCode() == ValidationRuleCode.DUPLICATE_RESOURCE_IDENTITY
    ex.jsonPointer() == "/included/1"
  }

  // Helpers

  private final Path fixturesDir = Path.of(System.getProperty('jsonapi.fixtures.dir'))

  private String fixtureText(String id) {
    CodecFixture fixture = CodecFixtures.byId(id)
    Files.readString(fixturesDir.resolve(fixture.expectedPath), StandardCharsets.UTF_8)
  }

  private static JsonApiDomainDocumentReader fixtureReader(String id, Class<?>... targetClasses) {
    CodecFixture fixture = CodecFixtures.byId(id)
    JsonApiJackson3.domainDocumentReader(
        JsonMapper.builder().build(),
        DocumentReadContext.of(fixture.context, PrimaryDataKind.RESOURCE),
        registry(targetClasses))
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

  // Local types

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
