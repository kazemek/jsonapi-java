package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.validation.EndpointIdentity
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.api.ResourceWriteOptions
import io.github.kazemek.jsonapi.jackson.diagnostic.CodecFailureCategory
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiDocumentReadException
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoration
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecorator
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry
import io.github.kazemek.jsonapi.jackson.representation.IncludePath
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.fixtures.domainwrite.Article
import io.github.kazemek.jsonapi.fixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.fixtures.domainwrite.Person
import io.github.kazemek.jsonapi.fixtures.localid.LocalIdentityArticle
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class Jackson3JsonApiResourcesSpec extends Specification {

  @Shared
  Jackson3JsonApi jsonApi = JsonApiJackson3.jsonApi(JsonMapper.builder().build())

  def "round-trips a single resource through writeOne and readOne"() {
    given:
    def article = new Article("1", "Hello", "Body text", [
      new Comment("c1", "Nice", null)
    ], new Person("p1", "Alice"))

    when:
    def json = jsonApi.resources().writeOne(article)
    def actual = jsonApi.resources().readOne(json, FlatArticle)

    then:
    actual.id() == "1"
    actual.title() == "Hello"
    actual.body() == "Body text"
    actual.author() == ResourceIdentifier.of("people", "p1")
    actual.comments() == [
      ResourceIdentifier.of("comments", "c1")
    ]
  }

  def "readOne rejects collection primary data without coercion"() {
    given:
    def json = jsonApi.resources().writeMany([
      new Article("1", "T", "B", List.of(), null)
    ])

    when:
    jsonApi.resources().readOne(json, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
    ex.propertyPath() == "/data"
  }

  def "readMany rejects single-resource primary data without coercion"() {
    given:
    def json = jsonApi.resources().writeOne(new Article("1", "T", "B", List.of(), null))

    when:
    jsonApi.resources().readMany(json, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
    ex.propertyPath() == "/data"
  }

  def "readOneDocument retains top-level state without hydrating relationships"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"Hello"},"relationships":{"author":{"data":{"type":"people","id":"p1"}}}},"included":[{"type":"people","id":"p1","attributes":{"name":"Alice"}}],"meta":{"count":1},"links":{"self":"https://example.test/articles"},"jsonapi":{"version":"1.1"}}'

    when:
    def document = jsonApi.resources().readOneDocument(json, FlatArticle)

    then:
    document.resource().id() == "1"
    document.resource().title() == "Hello"
    document.resource().author() == ResourceIdentifier.of("people", "p1")
    document.meta() == Meta.of([count: 1])
    document.links() == Links.ofLinks([self: new Link.StringLink("https://example.test/articles")])
    document.jsonapi() == JsonApiObject.ofVersion("1.1")
    document.included().size() == 1
    document.included()[0].type() == "people"
    document.included()[0].id() == "p1"
  }

  def "writeOne carries the document envelope without mapper orchestration"() {
    given:
    def options = new ResourceWriteOptions(
        new DocumentEnvelope(
        Links.ofLinks([self: new Link.StringLink("https://example.test/articles")]),
        Meta.of([copyright: "2026"]),
        JsonApiObject.ofVersion("1.1")),
        RepresentationSelection.none())

    when:
    def json = jsonApi.resources().writeOne(new Article("1", "T", "B", List.of(), null), options)
    def roundTrip = jsonApi.documents().read(json, DocumentReadContext.resourceDefaults())

    then:
    roundTrip.links() == Links.ofLinks([self: new Link.StringLink("https://example.test/articles")])
    roundTrip.meta() == Meta.of([copyright: "2026"])
    roundTrip.jsonapi() == JsonApiObject.ofVersion("1.1")
  }

  def "writeCreateDocument authors a lid-only document while response writes require id"() {
    given:
    def local = new LocalIdentityArticle(null, "lid-1", "Draft")

    when:
    def createJson = jsonApi.resources().writeCreateDocument(local)

    then:
    createJson.contains('"lid":"lid-1"')
    !createJson.contains('"id"')

    when:
    jsonApi.resources().writeOne(local)

    then:
    thrown(JsonApiValidationException)
  }

  def "writeUpdateDocument compares the expected endpoint identity"() {
    given:
    def article = new Article("1", "T", "B", List.of(), null)

    expect:
    jsonApi.resources().writeUpdateDocument(article, new EndpointIdentity("articles", "1")).contains('"id":"1"')

    when:
    jsonApi.resources().writeUpdateDocument(article, new EndpointIdentity("articles", "other"))

    then:
    thrown(JsonApiValidationException)
  }

  def "registered decorators apply transparently to domain writes"() {
    given:
    def links = Links.ofLinks([self: new Link.StringLink("https://example.test/articles/1")])
    def registry = ResourceDecoratorRegistry.builder()
        .register(Article, { Article a -> ResourceDecoration.ofLinks(links) } as ResourceDecorator)
        .build()
    def decorated = JsonApiJackson3.builder(JsonMapper.builder().build()).decorators(registry).build()

    when:
    def json = decorated.resources().writeOne(new Article("1", "T", "B", List.of(), null))
    def roundTrip = decorated.documents().read(json, DocumentReadContext.resourceDefaults())
    def primary = (roundTrip.data() as DocumentData.SingleResource).resource()

    then:
    primary.links() == links
  }

  def "inclusion and sparse fieldsets compose without caller choreography"() {
    given:
    def options = new ResourceWriteOptions(
        new DocumentEnvelope(null, null, null),
        RepresentationSelection.builder().include(IncludePath.of("comments")).fields("articles", "title").build())
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())
    def runtime = JsonApiJackson3.builder(JsonMapper.builder().build()).representationPolicy(policy).build()
    def article = new Article("1", "Hello", "Body text", [
      new Comment("c1", "Nice", null)
    ], null)

    when:
    def json = runtime.resources().writeOne(article, options)
    def exemptions = DocumentReadContext.of(
        ValidationContext.defaults().withSparseFieldsetLinkageExemptions(
        Set.of(ResourceIdentity.ofId("comments", "c1"))),
        PrimaryDataKind.RESOURCE)
    def roundTrip = runtime.documents().read(json, exemptions)

    then:
    roundTrip.included() != null
    roundTrip.included().size() == 1
    roundTrip.included()[0].type() == "comments"
    !json.contains("Body text")
    json.contains("Hello")
  }

  def "id and lid bind to independent roles without coercion"() {
    given:
    def article = new LocalIdentityArticle("1", "lid-1", "T")

    when:
    def json = jsonApi.resources().writeOne(article)
    def actual = jsonApi.resources().readOne(json, LocalIdentityArticle)

    then:
    actual.id() == "1"
    actual.localId() == "lid-1"

    when:
    jsonApi.resources().readOne('{"data":{"type":"articles","lid":"lid-9","attributes":{"title":"T"}}}', LocalIdentityArticle)

    then:
    def validation = thrown(JsonApiDocumentReadException)
    validation.category() == CodecFailureCategory.AGGREGATE_VALIDATION
  }

  def "stream sinks mirror string results without closing caller streams"() {
    given:
    def article = new Article("1", "Hello", "B", List.of(), null)
    def out = new ByteArrayOutputStream()

    when:
    jsonApi.resources().writeOne(article, out)
    def fromStream = jsonApi.resources().readOne(new ByteArrayInputStream(out.toByteArray()), FlatArticle)

    then:
    fromStream == jsonApi.resources().readOne(out.toString("UTF-8"), FlatArticle)
  }
}
