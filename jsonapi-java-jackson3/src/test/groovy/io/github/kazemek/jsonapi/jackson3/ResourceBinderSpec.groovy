package io.github.kazemek.jsonapi.jackson3

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.testfixtures.domainwrite.BlogWithJsonProperty
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticleWithArray
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticleWithOptional
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatArticleWithSet
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatCreatorArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatIntIdArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatLidArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatMappedArticle
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person
import spock.lang.Specification
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.json.JsonMapper

class ResourceBinderSpec extends Specification {

  def "binds record with id, attributes, and built-in ResourceIdentifier relationships"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource(
        "articles", "1",
        [title: "Hello", "body-text": "Content"],
        [author: single("people", "p1"), comments: collection("comments", ["c1", "c2"])])

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.id() == "1"
    article.title() == "Hello"
    article.body() == "Content"
    article.author() == ResourceIdentifier.of("people", "p1")
    article.comments() == [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ]
  }

  def "binds mutable POJO"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", [title: "Hello"], [author: single("people", "p1")])

    when:
    def article = binder.fromResource(resource, FlatMutableArticle)

    then:
    article.id == "1"
    article.title == "Hello"
    article.author == ResourceIdentifier.of("people", "p1")
  }

  def "binds immutable creator-based POJO"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "42", [title: "Creator"], null)

    when:
    def article = binder.fromResource(resource, FlatCreatorArticle)

    then:
    article.id == "42"
    article.title == "Creator"
  }

  def "binds inherited properties"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("blogs", "b1", [name: "My Blog", description: "A description"], null)

    when:
    def blog = binder.fromResource(resource, FlatInheritedBlog)

    then:
    blog.id == "b1"
    blog.name == "My Blog"
    blog.description == "A description"
  }

  def "binds @JsonProperty named attribute"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("blogs", "b1", ["blog_title": "My Blog"], null)

    when:
    def blog = binder.fromResource(resource, BlogWithJsonProperty)

    then:
    blog.title() == "My Blog"
  }

  def "naming strategy renames bound attribute keys"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def binder = JsonApiJackson3.resourceBinder(jsonMapper)
    def resource = resource("words", "1", [long_field_name: 10, other_value: 42], null)

    when:
    def thing = binder.fromResource(resource, FlatWords)

    then:
    thing.longFieldName == 10
    thing.otherValue == 42
  }

  def "@JsonIgnore property is not bound"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [name: "visible", secret: "hidden"], null)

    when:
    def thing = binder.fromResource(resource, FlatThingWithIgnored)

    then:
    thing.name == "visible"
    thing.confidential == null
  }

  def "mix-in attribute name is honored"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .addMixIn(FlatNamedThing, FlatMixInDef)
        .build()
    def binder = JsonApiJackson3.resourceBinder(jsonMapper)
    def resource = resource("named", "1", ["custom-name": "hello"], null)

    when:
    def thing = binder.fromResource(resource, FlatNamedThing)

    then:
    thing.value == "hello"
  }

  def "custom deserializer applies to attribute value"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [title: "hello"], null)

    when:
    def thing = binder.fromResource(resource, FlatLoudThing)

    then:
    thing.title == "HELLO"
  }

  def "default identifier conversion binds non-String id via convertValue"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "42", [title: "T"], null)

    when:
    def article = binder.fromResource(resource, FlatIntIdArticle)

    then:
    article.id() == 42
  }

  def "custom IdentifierConverter parse inverts the wire form"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return "prefix-" + idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            return wireIdentifier - "prefix-"
          }
        }
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build(), converter)
    def resource = resource("articles", "prefix-42", [title: "T"], null)

    when:
    def article = binder.fromResource(resource, FlatIntIdArticle)

    then:
    article.id() == 42
  }
  def "lid-only resource binds into identifier property"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = new ResourceObject("articles", null, "l1", Attributes.ofAttributes([title: "T"]), null, null, null, Map.of())

    when:
    def article = binder.fromResource(resource, FlatLidArticle)

    then:
    article.id() == "l1"
    article.title() == "T"
  }

  def "resource without id or lid omits the identifier property"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = new ResourceObject("articles", null, null, Attributes.ofAttributes([title: "T"]), null, null, null, Map.of())

    when:
    def article = binder.fromResource(resource, FlatLidArticle)

    then:
    article.id() == null
    article.title() == "T"
  }

  def "explicit-null attribute binds null and omitted attribute keeps its default"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", [title: null], null)

    when:
    def article = binder.fromResource(resource, FlatDefaultedArticle)

    then:
    article.title == null
    article.body == "default"
  }

  def "unmapped resource attributes are ignored"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", [title: "T", unexpected: "ignored"], null)

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.title() == "T"
    article.body() == null
  }

  def "fromResources binds homogeneous collection in order"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resources = [
      resource("articles", "1", [title: "One"], null),
      resource("articles", "2", [title: "Two"], null)
    ]

    when:
    def articles = binder.fromResources(resources, FlatArticle)

    then:
    articles.size() == 2
    articles*.id() == ["1", "2"]
    articles*.title() == ["One", "Two"]
  }

  def "fromResources validates every element type"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resources = [
      resource("articles", "1", null, null),
      resource("people", "p1", null, null)
    ]

    when:
    binder.fromResources(resources, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
    ex.propertyPath() == "/type"
  }

  def "JavaType entry points bind resource and collection"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def javaType = JsonMapper.builder().build().constructType(FlatArticle)

    when:
    def article = binder.fromResource(resource("articles", "1", [title: "T"], null), javaType)
    def articles = binder.fromResources(
        [
          resource("articles", "1", null, null),
          resource("articles", "2", null, null)
        ], javaType)

    then:
    article instanceof FlatArticle
    (article as FlatArticle).title() == "T"
    articles.size() == 2
  }

  // Relationship matrix: to-one ResourceIdentifier

  def "omitted to-one relationship key is not bound"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    def article = binder.fromResource(resource("articles", "1", null, [comments: collection("comments", ["c1"])]), FlatArticle)

    then:
    article.author() == null
  }

  def "links-or-meta-only to-one relationship is not bound"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: Relationship.metaOnly(Meta.of([k: "v"]))])

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.author() == null
  }

  def "NullLinkage on to-one binds null"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE)])

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.author() == null
  }

  def "collection linkage on to-one is a cardinality mismatch"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: collection("people", ["p1", "p2"])])

    when:
    binder.fromResource(resource, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    ex.propertyPath() == "/relationships/author/data"
  }

  // Relationship matrix: to-many ResourceIdentifier

  def "empty collection linkage on to-many binds empty collection"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.comments() == []
  }

  def "empty collection linkage on to-many binds empty Set"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [tags: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    def article = binder.fromResource(resource, FlatArticleWithSet)

    then:
    article.tags() == [] as Set
  }

  def "empty collection linkage on to-many binds empty array"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    def article = binder.fromResource(resource, FlatArticleWithArray)

    then:
    article.comments() == [] as ResourceIdentifier[]
  }

  def "non-empty collection linkage on to-many binds List"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: collection("comments", ["c1", "c2"])])

    when:
    def article = binder.fromResource(resource, FlatArticle)

    then:
    article.comments() == [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ]
  }

  def "non-empty collection linkage on to-many binds Set"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [tags: collection("tags", ["t1", "t2"])])

    when:
    def article = binder.fromResource(resource, FlatArticleWithSet)

    then:
    article.tags() == [
      ResourceIdentifier.of("tags", "t1"),
      ResourceIdentifier.of("tags", "t2")
    ] as Set
  }

  def "non-empty collection linkage on to-many binds array"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: collection("comments", ["c1", "c2"])])

    when:
    def article = binder.fromResource(resource, FlatArticleWithArray)

    then:
    article.comments() == [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ] as ResourceIdentifier[]
  }

  def "NullLinkage on to-many is a cardinality mismatch"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: Relationship.withData(RelationshipData.NullLinkage.INSTANCE)])

    when:
    binder.fromResource(resource, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    ex.propertyPath() == "/relationships/comments/data"
  }

  def "single linkage on to-many is a cardinality mismatch"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: single("comments", "c1")])

    when:
    binder.fromResource(resource, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
  }

  def "empty collection linkage on to-one is a cardinality mismatch"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    binder.fromResource(resource, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
  }

  // Relationship matrix: Optional to-one

  def "NullLinkage on Optional to-one binds empty Optional"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE)])

    when:
    def article = binder.fromResource(resource, FlatArticleWithOptional)

    then:
    article.author() == Optional.empty()
  }

  def "SingleLinkage on Optional to-one binds present Optional"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: single("people", "p1")])

    when:
    def article = binder.fromResource(resource, FlatArticleWithOptional)

    then:
    article.author() == Optional.of(ResourceIdentifier.of("people", "p1"))
  }

  // Custom linkage mappers

  def "registered linkage mapper binds to-one single linkage and to-many collection"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      if (data instanceof RelationshipData.SingleLinkage) {
        def identifier = ((RelationshipData.SingleLinkage) data).identifier()
        return new FlatAuthor(identifier.type(), identifier.id())
      }
      ((RelationshipData.IdentifierCollectionLinkage) data).identifiers().collect {
        new FlatAuthor(it.type(), it.id())
      }
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        "articles", "1", null,
        [author: single("people", "p1"),
          contributors: collection("people", ["p1", "p2"])])

    when:
    def article = binder.fromResource(resource, FlatMappedArticle)

    then:
    article.author() == new FlatAuthor("people", "p1")
    article.contributors() == [
      new FlatAuthor("people", "p1"),
      new FlatAuthor("people", "p2")
    ]
  }

  def "mapper receives Optional-unwrapped to-one type and collection to-many type"() {
    given:
    def seenTypes = []
    def mapper = { RelationshipData data, JavaType target ->
      seenTypes.add(target)
      if (data instanceof RelationshipData.SingleLinkage) {
        def identifier = ((RelationshipData.SingleLinkage) data).identifier()
        return new FlatAuthor(identifier.type(), identifier.id())
      }
      ((RelationshipData.IdentifierCollectionLinkage) data).identifiers().collect {
        new FlatAuthor(it.type(), it.id())
      }
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        "articles", "1", null,
        [author: single("people", "p1"),
          contributors: collection("people", ["p1", "p2"])])

    when:
    def article = binder.fromResource(resource, FlatMappedOptionalArticle)

    then:
    seenTypes*.rawClass == [FlatAuthor, List]
    article.author == Optional.of(new FlatAuthor("people", "p1"))
    article.contributors == [
      new FlatAuthor("people", "p1"),
      new FlatAuthor("people", "p2")
    ]
  }

  def "NullLinkage and empty linkage short-circuit without invoking the mapper"() {
    given:
    def invoked = false
    def mapper = { RelationshipData data, JavaType target ->
      invoked = true
      return null
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        "articles", "1", null,
        [author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE),
          contributors: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    def article = binder.fromResource(resource, FlatMappedArticle)

    then:
    article.author() == null
    article.contributors() == []
    !invoked
  }

  def "cardinality is enforced before the mapper is invoked"() {
    given:
    def invoked = false
    def mapper = { RelationshipData data, JavaType target ->
      invoked = true
      return null
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        "articles", "1", null,
        [author: collection("people", ["p1"]),
          contributors: single("people", "p1")])

    when:
    binder.fromResource(resource, FlatMappedArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH
    !invoked
  }

  def "mapper exception is reported as LINKAGE_MAPPING_FAILED"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      throw new IllegalStateException("boom")
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource("articles", "1", null, [author: single("people", "p1")])

    when:
    binder.fromResource(resource, FlatMappedArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.LINKAGE_MAPPING_FAILED
    ex.propertyPath() == "/relationships/author/data"
  }

  def "mapper returning null binds null property"() {
    given:
    def mapper = { RelationshipData data, JavaType target ->
      null
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource("articles", "1", null, [author: single("people", "p1")])

    when:
    def article = binder.fromResource(resource, FlatMappedArticle)

    then:
    article.author() == null
  }

  // Negative diagnostics

  def "resource type mismatch is RESOURCE_TYPE_MISMATCH at /type"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("people", "p1", null, null)

    when:
    binder.fromResource(resource, FlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
    ex.propertyPath() == "/type"
    ex.resourceClass() == FlatArticle
  }

  def "unregistered to-one relationship target is UNSUPPORTED_RELATIONSHIP_TARGET"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [author: single("people", "p1")])

    when:
    binder.fromResource(resource, FlatPersonArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET
    ex.propertyPath() == "/relationships/author/data"
  }

  def "unregistered to-many relationship target is UNSUPPORTED_RELATIONSHIP_TARGET"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "1", null, [comments: collection("comments", ["c1"])])

    when:
    binder.fromResource(resource, FlatCommentArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET
    ex.propertyPath() == "/relationships/comments/data"
  }

  def "identifier parse exception is IDENTIFIER_CONVERSION_FAILED at /id"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            throw new IllegalArgumentException("bad id")
          }
        }
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build(), converter)
    def resource = resource("articles", "42", null, null)

    when:
    binder.fromResource(resource, FlatIntIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "identifier parse returning null is IDENTIFIER_CONVERSION_FAILED"() {
    given:
    def converter = new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            return null
          }
        }
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build(), converter)
    def resource = resource("articles", "42", null, null)

    when:
    binder.fromResource(resource, FlatIntIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "identifier coercion failure is IDENTIFIER_CONVERSION_FAILED"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("articles", "not-a-number", null, null)

    when:
    binder.fromResource(resource, FlatIntIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "absent required creator property is MISSING_CREATOR_INPUT"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [title: "present"], null)

    when:
    binder.fromResource(resource, FlatRequiredThing)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.propertyPath() == "/required"
  }

  def "creator throwing during instantiation is MISSING_CREATOR_INPUT"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [title: "boom"], null)

    when:
    binder.fromResource(resource, FlatThrowingCreatorThing)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.cause instanceof ValueInstantiationException
  }

  def "attribute value that cannot coerce is UNSUPPORTED_ATTRIBUTE_VALUE"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [count: [nested: 1]], null)

    when:
    binder.fromResource(resource, FlatCountedThing)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/count"
  }

  def "explicit-null attribute into primitive property is UNSUPPORTED_ATTRIBUTE_VALUE"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [count: null], null)

    when:
    binder.fromResource(resource, FlatCountedThing)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
  }

  // Compound isolation

  def "binder never sees document included resources"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def reader = JsonApiJackson3.reader(JsonMapper.builder().build(), DocumentReadContext.resourceDefaults())
    def primary = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"},"relationships":{"author":{"data":{"type":"people","id":"p1"}}}},"included":[{"type":"people","id":"p1","attributes":{"name":"Alice"}}]}'
    def swapped = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"},"relationships":{"author":{"data":{"type":"people","id":"p1"}}}},"included":[{"type":"people","id":"p1","attributes":{"name":"AliceChanged"}}]}'
    def first = binder.fromResource(primaryResource(reader, primary), FlatArticle)
    def second = binder.fromResource(primaryResource(reader, swapped), FlatArticle)

    expect:
    first == second
    first.author() == ResourceIdentifier.of("people", "p1")
    first.title() == "T"
  }

  // Fixtures

  private static ResourceObject primaryResource(JsonApiDocumentReader reader, String json) {
    def document = reader.readValue(json)
    ((DocumentData.SingleResource) document.data()).resource()
  }

  private static ResourceObject resource(String type, String id, Map attrs, Map rels) {
    new ResourceObject(
        type,
        id,
        null,
        attrs == null ? null : Attributes.ofAttributes(attrs),
        rels == null ? null : Relationships.ofRelationships(rels),
        null,
        null,
        Map.of())
  }

  private static Relationship single(String type, String id) {
    Relationship.withData(new RelationshipData.SingleLinkage(ResourceIdentifier.of(type, id)))
  }

  private static Relationship collection(String type, List<String> ids) {
    Relationship.withData(
        new RelationshipData.IdentifierCollectionLinkage(ids.collect { ResourceIdentifier.of(type, it) }))
  }

  // Local DTO shapes

  @JsonApiResource(type = "articles")
  static class FlatMutableArticle {
    @JsonApiId String id
    String title
    @JsonApiRelationship ResourceIdentifier author

    FlatMutableArticle() {}
  }

  @JsonApiResource(type = "words")
  static class FlatWords {
    @JsonApiId String id
    int longFieldName
    int otherValue
  }

  @JsonApiResource(type = "things")
  static class FlatThingWithIgnored {
    @JsonApiId String id
    @JsonIgnore
    @JsonApiAttribute(name = "secret") String confidential
    String name
  }

  @JsonApiResource(type = "named")
  static class FlatNamedThing {
    @JsonApiId String id
    String value
  }

  abstract static class FlatMixInDef {
    @JsonApiAttribute(name = "custom-name")
    abstract String getValue()
  }

  static class UppercaseDeserializer extends StdDeserializer<String> {
    UppercaseDeserializer() {
      super(String.class)
    }

    @Override
    String deserialize(JsonParser parser, DeserializationContext context) {
      return parser.getValueAsString().toUpperCase()
    }
  }

  @JsonApiResource(type = "things")
  static class FlatLoudThing {
    @JsonApiId String id
    @JsonDeserialize(using = UppercaseDeserializer)
    String title
  }

  @JsonApiResource(type = "blogs")
  static class FlatInheritedBlog extends FlatBlogBase {
    String description

    FlatInheritedBlog() {}
  }

  static class FlatBlogBase {
    @JsonApiId String id
    String name

    FlatBlogBase() {}
  }

  @JsonApiResource(type = "articles")
  static class FlatPersonArticle {
    @JsonApiId String id
    @JsonApiRelationship Person author
  }

  @JsonApiResource(type = "articles")
  static class FlatCommentArticle {
    @JsonApiId String id
    @JsonApiRelationship List<Comment> comments
  }

  @JsonApiResource(type = "things")
  static class FlatRequiredThing {
    private final String id
    private final String required

    @JsonCreator
    FlatRequiredThing(
    @JsonProperty("id") @JsonApiId String id,
    @JsonProperty(value = "required", required = true) String required) {
      this.id = id
      this.required = required
    }

    String getId() {
      return id
    }

    String getRequired() {
      return required
    }
  }

  @JsonApiResource(type = "things")
  static class FlatCountedThing {
    @JsonApiId String id
    int count
  }

  @JsonApiResource(type = "things")
  static class FlatThrowingCreatorThing {
    private final String id
    private final String title

    @JsonCreator
    FlatThrowingCreatorThing(
    @JsonProperty("id") @JsonApiId String id, @JsonProperty("title") String title) {
      if (title == "boom") {
        throw new IllegalArgumentException("creator rejected value")
      }
      this.id = id
      this.title = title
    }

    String getId() {
      return id
    }

    String getTitle() {
      return title
    }
  }

  @JsonApiResource(type = "articles")
  static class FlatDefaultedArticle {
    @JsonApiId String id
    String title = "default"
    String body = "default"

    FlatDefaultedArticle() {}
  }

  @JsonApiResource(type = "articles")
  static class FlatMappedOptionalArticle {
    @JsonApiId String id
    @JsonApiRelationship Optional<FlatAuthor> author
    @JsonApiRelationship List<FlatAuthor> contributors
  }
}
