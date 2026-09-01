package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipDecoration
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoration
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecorator
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry
import io.github.kazemek.jsonapi.testsupport.decoration.DecorationScenarios
import io.github.kazemek.jsonapi.testsupport.decoration.DecorationVerifier
import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

class ResourceDecorationSpec extends Specification {

  Links resourceLinks = Links.ofLinks([self: new Link.StringLink("https://example.test/articles/1")])
  Links commentsLinks = Links.ofLinks([self: new Link.StringLink("https://example.test/articles/1/relationships/comments"), related: new Link.StringLink("https://example.test/articles/1/comments")])

  @Unroll
  def "shared catalog #scenario.id"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build(), scenario.decorators())

    when:
    def result = null
    def thrown = null
    try {
      def domain = scenario.domainSupplier().get()
      def outcome = scenario.outcome()
      if (outcome instanceof io.github.kazemek.jsonapi.testsupport.decoration.DecorationOutcome.ResourceSuccess) {
        result = mapper.toResource(domain)
      } else if (outcome instanceof io.github.kazemek.jsonapi.testsupport.decoration.DecorationOutcome.DocumentSuccess) {
        result = mapper.toDocument(domain, null, scenario.selection(), scenario.policy())
      } else if (outcome instanceof io.github.kazemek.jsonapi.testsupport.decoration.DecorationOutcome.MappedDocumentSuccess) {
        result = mapper.toMappedDocument(domain, null, scenario.selection(), scenario.policy())
      } else if (outcome instanceof io.github.kazemek.jsonapi.testsupport.decoration.DecorationOutcome.Failure) {
        result = mapper.toResource(domain)
      }
    } catch (Throwable t) {
      thrown = t
    }

    then:
    DecorationVerifier.verify(scenario, result, thrown)

    where:
    scenario << DecorationScenarios.catalog().all()
  }

  def "configured Jackson rename follows logical identity"() {
    given:
    ResourceDecorator<DecorationFixtures.RenamedRelationshipArticle> decorator = { a ->
      ResourceDecoration.builder().relationship("comments", RelationshipDecoration.links(commentsLinks)).build()
    }
    def registry = ResourceDecoratorRegistry.builder().register(DecorationFixtures.RenamedRelationshipArticle, decorator).build()
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build(), registry)
    def article = new DecorationFixtures.RenamedRelationshipArticle("1", "Title", [
      new DecorationFixtures.Comment("c1", "B", null)
    ], new DecorationFixtures.Person("p1", "Alice"))

    when:
    def resource = mapper.toResource(article)

    then:
    resource.relationships().relationships().containsKey("article-comments")
    !resource.relationships().relationships().containsKey("comments")
    resource.relationships().relationships().get("article-comments").links() == commentsLinks
    resource.relationships().relationships().get("article-comments").data().identifiers().size() == 1
  }

  def "generic type specialization retains decoration registry lookup on raw class"() {
    given:
    Links thingLinks = Links.ofLinks([self: new Link.StringLink("https://example.test/things/t1")])
    ResourceDecorator<DecorationFixtures.Thing> thingDecorator = { t -> ResourceDecoration.builder().links(thingLinks).build() }
    ResourceDecorator<DecorationFixtures.GenericContainer<DecorationFixtures.Thing>> containerDecorator = { c -> ResourceDecoration.builder().links(resourceLinks).build() }
    def registry = ResourceDecoratorRegistry.builder()
        .register(DecorationFixtures.Thing, thingDecorator)
        .register(DecorationFixtures.GenericContainer, containerDecorator)
        .build()
    def base = JsonMapper.builder().build()
    def mapper = JsonApiJackson3.resourceMapper(base, registry)
    def thing = new DecorationFixtures.Thing("t1", "Thing")
    def container = new DecorationFixtures.GenericContainer<DecorationFixtures.Thing>("c1", "Container", thing)
    def containerType = base.typeFactory.constructParametricType(DecorationFixtures.GenericContainer, DecorationFixtures.Thing)

    when:
    def resource = mapper.toResource(container, containerType)

    then:
    resource.links() == resourceLinks
  }

  def "decorator registry is immutable and safe for concurrent use"() {
    given:
    def registry = ResourceDecoratorRegistry.builder()
        .register(DecorationFixtures.ArticleWithMeta, { a -> ResourceDecoration.empty() } as ResourceDecorator)
        .build()

    when:
    registry.asMap().put(String, { s -> ResourceDecoration.empty() } as ResourceDecorator)

    then:
    thrown(UnsupportedOperationException)
  }

  def "present-empty resource and relationship links are preserved"() {
    given:
    Links empty = Links.empty()
    ResourceDecorator<DecorationFixtures.ArticleWithMeta> decorator = { a ->
      ResourceDecoration.builder().links(empty).relationship("comments", RelationshipDecoration.of(empty)).build()
    }
    def registry = ResourceDecoratorRegistry.builder().register(DecorationFixtures.ArticleWithMeta, decorator).build()
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build(), registry)
    def article = new DecorationFixtures.ArticleWithMeta("1", "Title", null, [
      new DecorationFixtures.Comment("c1", "B", null)
    ], null)

    when:
    def resource = mapper.toResource(article)
    def doc = mapper.toDocument(article)
    def json = JsonApiJackson3.writer(JsonMapper.builder().build()).writeValueAsString(doc)
    def tree = JsonMapper.builder().build().readTree(json)

    then:
    resource.links() == empty
    resource.links() != null
    resource.relationships().relationships().comments.links() == empty
    tree.get("data").get("links").isObject()
    tree.get("data").get("links").isEmpty()
    tree.get("data").get("relationships").get("comments").get("links").isObject()
    tree.get("data").get("relationships").get("comments").get("links").isEmpty()
  }
}
