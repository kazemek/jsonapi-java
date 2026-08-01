package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.jackson3.testmodel.Article
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithSet
import io.github.kazemek.jsonapi.jackson3.testmodel.BlogWithJsonProperty
import io.github.kazemek.jsonapi.jackson3.testmodel.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.ConventionalId
import io.github.kazemek.jsonapi.jackson3.testmodel.Person
import io.github.kazemek.jsonapi.jackson3.testmodel.Tag
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class ResourceMapperSpec extends Specification {

  @JsonApiResource(type = "pojos")
  static class SamplePojo {
    @JsonApiId String id
    @JsonApiAttribute(name = "display-name") String name
    @JsonApiRelationship List<Comment> comments

    SamplePojo() {}

    SamplePojo(String id, String name, List<Comment> comments) {
      this.id = id
      this.name = name
      this.comments = comments
    }
  }

  def "maps a record with explicit @JsonApiId and @JsonApiAttribute"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "Hello", "Body text", [], null)

    when:
    def resource = mapper.toResource(article)

    then:
    resource.type() == "articles"
    resource.id() == "1"
    resource.lid() == null
    resource.attributes() != null
    resource.attributes().attributes().title == "Hello"
    resource.attributes().attributes().get("body-text") == "Body text"
  }

  def "maps attribute name override"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "Title", "Content", [], null)

    when:
    def resource = mapper.toResource(article)

    then:
    resource.attributes().attributes().containsKey("title")
    resource.attributes().attributes().containsKey("body-text")
  }

  def "maps conventional id property"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def conventional = new ConventionalId("42", "name value")

    when:
    def resource = mapper.toResource(conventional)

    then:
    resource.type() == "conventionals"
    resource.id() == "42"
  }

  def "maps @JsonProperty naming"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def blog = new BlogWithJsonProperty("b1", "My Blog")

    when:
    def resource = mapper.toResource(blog)

    then:
    resource.type() == "blogs"
    resource.id() == "b1"
    resource.attributes().attributes().containsKey("blog_title")
  }

  def "maps nullable to-one relationship to null linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "T", "B", [], null)

    when:
    def resource = mapper.toResource(article)

    then:
    resource.relationships() != null
    resource.relationships().relationships().containsKey("author")
    def rel = resource.relationships().relationships().get("author")
    rel.data() instanceof RelationshipData.NullLinkage
  }

  def "maps to-one relationship to single linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def person = new Person("p1", "Alice")
    def article = new Article("1", "T", "B", [], person)

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("author")
    rel.data() instanceof RelationshipData.SingleLinkage
    def linkage = (RelationshipData.SingleLinkage) rel.data()
    linkage.identifier().type() == "people"
    linkage.identifier().id() == "p1"
  }

  def "maps empty to-many relationship to empty linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "T", "B", [], null)

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("comments")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().isEmpty()
  }

  def "maps populated to-many relationship"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def c1 = new Comment("c1", "Nice", null)
    def c2 = new Comment("c2", "Great", null)
    def article = new Article("1", "T", "B", [c1, c2], null)

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("comments")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().size() == 2
    linkage.identifiers().get(0).type() == "comments"
    linkage.identifiers().get(0).id() == "c1"
    linkage.identifiers().get(1).type() == "comments"
    linkage.identifiers().get(1).id() == "c2"
  }

  def "maps Set-based to-many relationship"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def tag1 = new Tag("java")
    def tag2 = new Tag("groovy")
    def article = new ArticleWithSet("1", "T", [tag1, tag2] as Set)

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("tags")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().size() == 2
  }

  def "maps mutable POJO"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def pojo = new SamplePojo("p1", "Example", [])

    when:
    def resource = mapper.toResource(pojo)

    then:
    resource.type() == "pojos"
    resource.id() == "p1"
    resource.attributes().attributes().get("display-name") == "Example"
  }

  def "toDocument wraps resource in single-resource document"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "T", "B", [], null)

    when:
    def doc = mapper.toDocument(article)

    then:
    doc.data() != null
    doc.data() instanceof DocumentData.SingleResource
  }

  def "toResourceCollection wraps in resource-collection document"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def a1 = new Article("1", "One", "B1", [], null)
    def a2 = new Article("2", "Two", "B2", [], null)

    when:
    def doc = mapper.toResourceCollection([a1, a2])

    then:
    doc.data() != null
    doc.data() instanceof DocumentData.ResourceCollection
    def coll = (DocumentData.ResourceCollection) doc.data()
    coll.resources().size() == 2
  }

  def "toDocument with envelope passes links, meta, and jsonapi"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new Article("1", "T", "B", [], null)
    def links = Links.ofLinks([self: null])
    def meta = Meta.of(["key": "value"])
    def jsonapi = JsonApiObject.ofVersion("1.1")
    def envelope = new DocumentEnvelope(links, meta, jsonapi)

    when:
    def doc = mapper.toDocument(article, envelope)

    then:
    doc.links() == links
    doc.meta() == meta
    doc.jsonapi() == jsonapi
  }

  def "null input is rejected"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

    when:
    mapper.toResource(null)

    then:
    thrown(NullPointerException)
  }
}
