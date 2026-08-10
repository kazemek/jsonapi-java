package io.github.kazemek.jsonapi.jackson3

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithArray
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithFormattedTitle
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithOptional
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithOptionalId
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithOptionalRelationship
import io.github.kazemek.jsonapi.jackson3.testmodel.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.CreatorBasedArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ExtendedBlog
import io.github.kazemek.jsonapi.jackson3.testmodel.FormattedTitle
import spock.lang.Specification
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper

class ResourceMappingJacksonFeaturesSpec extends Specification {

  @JsonApiResource(type = "things")
  static class ThingWithIgnored {
    @JsonApiId String id
    @JsonIgnore
    @JsonApiAttribute(name = "secret") String confidential
    String name
  }

  def "@JsonIgnore excludes property from mapping"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def thing = new ThingWithIgnored(id: "1", confidential: "hidden", name: "visible")

    when:
    def resource = mapper.toResource(thing)

    then:
    resource.attributes().attributes().containsKey("name")
    !resource.attributes().attributes().containsKey("secret")
  }

  @JsonApiResource(type = "named")
  static class NamedThing {
    @JsonApiId String id
    String value
  }

  abstract static class MixInDef {
    @JsonApiAttribute(name = "custom-name")
    abstract String getValue()
  }

  def "mix-in resolves property-level annotation"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .addMixIn(NamedThing, MixInDef)
        .build()
    def mapper = JsonApiJackson3.resourceMapper(jsonMapper)
    def thing = new NamedThing(id: "1", value: "hello")

    when:
    def resource = mapper.toResource(thing)

    then:
    resource.attributes().attributes().containsKey("custom-name")
  }

  def "naming strategy renames attributes"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def mapper = JsonApiJackson3.resourceMapper(jsonMapper)
    def thing = new ThingWithMultipleWords(id: "1", longFieldName: 10, otherValue: 42)

    when:
    def resource = mapper.toResource(thing)

    then:
    resource.attributes().attributes().containsKey("long_field_name")
    resource.attributes().attributes().containsKey("other_value")
  }

  @JsonApiResource(type = "words")
  static class ThingWithMultipleWords {
    @JsonApiId String id
    int longFieldName
    int otherValue
  }

  def "creator-based immutable POJO maps properties"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new CreatorBasedArticle("42", "Hello")

    when:
    def resource = mapper.toResource(article)

    then:
    resource.type() == "articles"
    resource.id() == "42"
    resource.attributes().attributes().title == "Hello"
  }

  def "inherited properties from base class are mapped"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def blog = new ExtendedBlog("b1", "My Blog", "A description")

    when:
    def resource = mapper.toResource(blog)

    then:
    resource.type() == "blogs"
    resource.attributes().attributes().name == "My Blog"
    resource.attributes().attributes().description == "A description"
  }

  def "custom ValueSerializer honored via convertValue"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithFormattedTitle("1", new FormattedTitle("Hello"))

    when:
    def resource = mapper.toResource(article)

    then:
    resource.attributes().attributes().title == "[FORMATTED] Hello"
  }

  def "Optional present attribute is unwrapped"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithOptional("1", "Title", Optional.of("Sub"))

    when:
    def resource = mapper.toResource(article)

    then:
    resource.attributes().attributes().title == "Title"
    resource.attributes().attributes().subtitle == "Sub"
  }

  def "Optional empty attribute is mapped"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithOptional("1", "Title", Optional.empty())

    when:
    def resource = mapper.toResource(article)

    then:
    resource.attributes().attributes().title == "Title"
    !resource.attributes().attributes().containsKey("subtitle")
  }

  def "array to-many relationship produces collection linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def c1 = new Comment("c1", "Nice", null)
    def c2 = new Comment("c2", "Great", null)
    def article = new ArticleWithArray("1", "T", [c1, c2] as Comment[])

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("comments")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().size() == 2
    linkage.identifiers().get(0).type() == "comments"
    linkage.identifiers().get(0).id() == "c1"
  }

  @JsonApiResource(type = "badrels")
  static class BadCollectionRel {
    @JsonApiId String id
    @JsonApiRelationship List<Object> items
  }

  def "raw collection element type rejects with UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new BadCollectionRel(id: "1", items: [new Object()])

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE
  }

  def "identifier converter returning null is rejected"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return null
          }
        }
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build(), converter)
    def article = new ArticleWithOptional("1", "T", Optional.empty())

    when:
    mapper.toResource(article)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
  }

  def "identifier converter throwing RuntimeException is propagated"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            throw new IllegalArgumentException("bad id")
          }
        }
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build(), converter)
    def article = new ArticleWithOptional("1", "T", Optional.empty())

    when:
    mapper.toResource(article)

    then:
    thrown(IllegalArgumentException)
  }

  @JsonApiResource(type = "intarray")
  static class IntArrayEntity {
    @JsonApiId String id
    @JsonApiRelationship int[] values
  }

  def "to-many with unsupported runtime collection type throws UNSUPPORTED_RELATIONSHIP_VALUE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new IntArrayEntity(id: "1", values: [1, 2, 3] as int[])

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE
  }

  @JsonApiResource(type = "mixed")
  static class MixedRelEntity {
    @JsonApiId String id
    @JsonApiRelationship List<Object> items
  }

  def "mixed element types in to-many linkage throw UNSUPPORTED_RELATIONSHIP_VALUE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def ri = new ResourceIdentifier("comments", "1", null, null, Map.of())
    def entity = new MixedRelEntity(id: "1", items: [ri, new Object()])

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE
  }

  def "mixed element types with unsupported element first throw UNSUPPORTED_RELATIONSHIP_VALUE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def ri = new ResourceIdentifier("comments", "1", null, null, Map.of())
    def entity = new MixedRelEntity(id: "1", items: [new Object(), ri])

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE
  }

  def "leading null in to-many ResourceIdentifier collection produces correct linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def ri = new ResourceIdentifier("comments", "1", null, null, Map.of())
    def entity = new MixedRelEntity(id: "1", items: [null, ri])

    when:
    def resource = mapper.toResource(entity)

    then:
    def rel = resource.relationships().relationships().get("items")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().size() == 1
    linkage.identifiers().get(0).type() == "comments"
    linkage.identifiers().get(0).id() == "1"
  }

  @JsonApiResource(type = "mixedarray")
  static class MixedRelArrayEntity {
    @JsonApiId String id
    @JsonApiRelationship ResourceIdentifier[] items
  }

  def "leading null in to-many ResourceIdentifier array produces correct linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def ri = new ResourceIdentifier("comments", "1", null, null, Map.of())
    def entity = new MixedRelArrayEntity(id: "1", items: [null, ri] as ResourceIdentifier[])

    when:
    def resource = mapper.toResource(entity)

    then:
    def rel = resource.relationships().relationships().get("items")
    rel.data() instanceof RelationshipData.IdentifierCollectionLinkage
    def linkage = (RelationshipData.IdentifierCollectionLinkage) rel.data()
    linkage.identifiers().size() == 1
    linkage.identifiers().get(0).type() == "comments"
    linkage.identifiers().get(0).id() == "1"
  }

  def "present Optional id is unwrapped to identifier string"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithOptionalId(Optional.of("99"), "Title")

    when:
    def resource = mapper.toResource(article)

    then:
    resource.type() == "articles"
    resource.id() == "99"
  }

  def "empty Optional id throws MISSING_IDENTIFIER"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithOptionalId(Optional.empty(), "Title")

    when:
    mapper.toResource(article)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
  }

  def "present Optional to-one relationship produces correct single linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def comment = new Comment("c1", "Nice", null)
    def article = new ArticleWithOptionalRelationship("1", Optional.of(comment))

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("comment")
    rel.data() instanceof RelationshipData.SingleLinkage
    def linkage = (RelationshipData.SingleLinkage) rel.data()
    linkage.identifier().type() == "comments"
    linkage.identifier().id() == "c1"
  }

  def "empty Optional to-one relationship produces null linkage"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithOptionalRelationship("1", Optional.empty())

    when:
    def resource = mapper.toResource(article)

    then:
    def rel = resource.relationships().relationships().get("comment")
    rel.data() == RelationshipData.NullLinkage.INSTANCE
  }
}
