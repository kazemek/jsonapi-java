package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiMeta
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.PatchPresence
import spock.lang.Specification
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.json.JsonMapper

class PropertyScopedAuthoritySpec extends Specification {

  def "attribute and both meta locations use direct property serializers"() {
    given:
    def article = new DirectPropertyArticle(
        "1",
        "title",
        new StructuredValue("detail"),
        new MetaValue("resource"),
        ResourceIdentifier.of("people", "p1"),
        new MetaValue("relationship"))

    when:
    def resource = JsonApiJackson3.resourceMapper(JsonMapper.builder().build()).toResource(article)

    then:
    resource.attributes().attributes() == [
      title: "property:title",
      details: [encoded: "detail"]
    ]
    resource.meta().members() == [encoded: "resource"]
    resource.relationships().relationships().author.meta().members() == [encoded: "relationship"]
  }

  def "ordinary uncustomized scalar attributes retain their value"() {
    given:
    def article = new OrdinaryPropertyArticle("1", "title")

    when:
    def resource = JsonApiJackson3.resourceMapper(JsonMapper.builder().build()).toResource(article)

    then:
    resource.attributes().attributes() == [title: "title"]
  }

  def "ordinary null attributes use their contextual null serializer"() {
    given:
    def article = new NullSerializedArticle("1", null)

    when:
    def resource = JsonApiJackson3.resourceMapper(JsonMapper.builder().build()).toResource(article)

    then:
    resource.attributes().attributes() == [title: "property:null"]
  }

  def "custom identifier target is deserialized in property context without root coercion"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), identifierConverter())

    when:
    def article = binder.fromResource(resourceWithId("rich-articles", "1"), RichIdArticle)

    then:
    article.id == new PropertyIdentifier("parsed-1")
  }

  def "flat identifier construction failures retain the identifier diagnostic"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), identifierConverter())

    when:
    binder.fromResource(resourceWithId("articles", "1"), FailingIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "flat lid-only identifier construction failures retain the lid diagnostic pointer"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), identifierConverter())

    when:
    binder.fromResource(resourceWithLid("articles", "local-1"), FailingIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/lid"
  }

  def "attribute and resource meta mix-in serializers remain property-scoped"() {
    given:
    def mapper = JsonMapper.builder()
        .addMixIn(MixinPropertyArticle, PropertyCustomizationMixIn)
        .build()
    def article = new MixinPropertyArticle("1", "title", new MetaValue("resource"))

    when:
    def resource = JsonApiJackson3.resourceMapper(mapper).toResource(article)

    then:
    resource.attributes().attributes() == [title: "mixin:title"]
    resource.meta().members() == [encoded: "resource"]
  }

  def "flat read applies the property deserializer after identifier parsing"() {
    given:
    def readerMapper = JsonMapper.builder().build()
    def binder = JsonApiJackson3.resourceBinder(readerMapper, identifierConverter())
    def resource = resourceWithId("articles", "1")

    when:
    def article = binder.fromResource(resource, DirectIdArticle)

    then:
    article.id == "property:parsed-1"
  }

  def "flat read honors an identifier deserializer supplied by a mix-in"() {
    given:
    def mapper = JsonMapper.builder()
        .addMixIn(MixinIdArticle, IdDeserializerMixIn)
        .build()
    def binder = JsonApiJackson3.resourceBinder(mapper, identifierConverter())

    when:
    def article = binder.fromResource(resourceWithId("articles", "1"), MixinIdArticle)

    then:
    article.id == "property:parsed-1"
  }

  def "low-level PATCH applies the property deserializer after identifier parsing"() {
    given:
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        identifierConverter())

    when:
    def command = reader.readValue(
        '{"data":{"type":"articles","id":"1"}}', DirectIdArticle)

    then:
    command.identity() == "property:parsed-1"
    command.changes().isEmpty()
  }

  def "low-level property conversion preserves convertValue root-unwrapped semantics"() {
    given:
    def mapper = JsonMapper.builder()
        .enable(SerializationFeature.WRAP_ROOT_VALUE)
        .build()
    def reader = JsonApiJackson3.patchReader(mapper, ValidationContext.defaults(), identifierConverter())

    when:
    def command = reader.readValue(
        '{"data":{"type":"articles","id":"1","attributes":{"title":"title"}}}',
        DirectIdArticle)

    then:
    command.identity() == "property:parsed-1"
    command.changes()[0].value() == "property:title"
  }

  def "low-level PATCH honors an identifier deserializer supplied by a mix-in"() {
    given:
    def mapper = JsonMapper.builder()
        .addMixIn(MixinIdArticle, IdDeserializerMixIn)
        .build()
    def reader = JsonApiJackson3.patchReader(
        mapper, ValidationContext.defaults(), identifierConverter())

    when:
    def command = reader.readValue(
        '{"data":{"type":"articles","id":"1"}}', MixinIdArticle)

    then:
    command.identity() == "property:parsed-1"
  }

  def "low-level PATCH normalizes identifier converter mapping exceptions"() {
    given:
    def reader = JsonApiJackson3.patchReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), mappingThrowingConverter())

    when:
    reader.readValue('{"data":{"type":"articles","id":"1"}}', DirectIdArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "typed PATCH applies the property deserializer after identifier parsing"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(),
        ValidationContext.defaults(),
        identifierConverter())

    when:
    def patch = reader.readValue(
        '{"data":{"type":"articles","id":"1"}}', DirectIdPatch)

    then:
    patch.id == "property:parsed-1"
    patch.title == PatchPresence.omitted()
  }

  def "typed PATCH honors an identifier deserializer supplied by a mix-in"() {
    given:
    def mapper = JsonMapper.builder()
        .addMixIn(MixinIdPatch, IdDeserializerMixIn)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(
        mapper, ValidationContext.defaults(), identifierConverter())

    when:
    def patch = reader.readValue(
        '{"data":{"type":"articles","id":"1"}}', MixinIdPatch)

    then:
    patch.id == "property:parsed-1"
    patch.title == PatchPresence.omitted()
  }

  def "typed PATCH retains the identifier diagnostic for target construction failures"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), identifierConverter())

    when:
    reader.readValue('{"data":{"type":"articles","id":"1"}}', FailingIdPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "typed PATCH normalizes identifier converter mapping exceptions"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(
        JsonMapper.builder().build(), ValidationContext.defaults(), mappingThrowingConverter())

    when:
    reader.readValue('{"data":{"type":"articles","id":"1"}}', DirectIdPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED
    ex.propertyPath() == "/id"
  }

  def "identifier wire semantics remain JSON:API-owned"() {
    given:
    def article = new JsonApiOwnedIdentifier("1")

    when:
    def resource = JsonApiJackson3.resourceMapper(JsonMapper.builder().build()).toResource(article)

    then:
    resource.id() == "1"
  }

  def "property serialization cannot bypass object-shaped meta validation"() {
    given:
    def article = new ScalarSerializedMetaArticle("1", new MetaValue("resource"))

    when:
    JsonApiJackson3.resourceMapper(JsonMapper.builder().build()).toResource(article)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_META_TARGET
    ex.propertyPath() == "/meta"
  }

  private static IdentifierConverter identifierConverter() {
    new IdentifierConverter() {
          @Override
          String convert(Object value) {
            value.toString()
          }

          @Override
          Object parse(String wire) {
            "parsed-" + wire
          }
        }
  }

  private static IdentifierConverter mappingThrowingConverter() {
    new IdentifierConverter() {
          @Override
          String convert(Object value) {
            value.toString()
          }

          @Override
          Object parse(String wire) {
            throw new JsonApiMappingException(
            MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
            DirectIdArticle,
            "/wrong",
            "converter failure")
          }
        }
  }

  private static ResourceObject resourceWithId(String type, String id) {
    new ResourceObject(type, id, null, null, null, null, null, [:])
  }

  private static ResourceObject resourceWithLid(String type, String lid) {
    new ResourceObject(type, null, lid, null, null, null, null, [:])
  }

  static class PropertySerializer extends ValueSerializer<Object> {
    @Override
    void serialize(Object value, JsonGenerator generator, SerializationContext context) {
      generator.writeString("property:" + value)
    }
  }

  static class NullPropertySerializer extends ValueSerializer<Object> {
    @Override
    void serialize(Object value, JsonGenerator generator, SerializationContext context) {
      generator.writeString("property:null")
    }
  }

  static class MixinPropertySerializer extends ValueSerializer<Object> {
    @Override
    void serialize(Object value, JsonGenerator generator, SerializationContext context) {
      generator.writeString("mixin:" + value)
    }
  }

  static class StructuredSerializer extends ValueSerializer<StructuredValue> {
    @Override
    void serialize(StructuredValue value, JsonGenerator generator, SerializationContext context) {
      generator.writeStartObject()
      generator.writeName("encoded")
      generator.writeString(value.value)
      generator.writeEndObject()
    }
  }

  static class MetaSerializer extends ValueSerializer<MetaValue> {
    @Override
    void serialize(MetaValue value, JsonGenerator generator, SerializationContext context) {
      generator.writeStartObject()
      generator.writeName("encoded")
      generator.writeString(value.value)
      generator.writeEndObject()
    }
  }

  static class ScalarMetaSerializer extends ValueSerializer<MetaValue> {
    @Override
    void serialize(MetaValue value, JsonGenerator generator, SerializationContext context) {
      generator.writeString(value.value)
    }
  }

  static class IdentifierDeserializer extends StdDeserializer<String> {
    IdentifierDeserializer() {
      super(String)
    }

    @Override
    String deserialize(JsonParser parser, DeserializationContext context) {
      "property:" + parser.getValueAsString()
    }
  }

  static class PropertyIdentifierDeserializer extends StdDeserializer<PropertyIdentifier> {
    PropertyIdentifierDeserializer() {
      super(PropertyIdentifier)
    }

    @Override
    PropertyIdentifier deserialize(JsonParser parser, DeserializationContext context) {
      new PropertyIdentifier(parser.getValueAsString())
    }
  }

  static class FailingIdentifierDeserializer extends StdDeserializer<String> {
    FailingIdentifierDeserializer() {
      super(String)
    }

    @Override
    String deserialize(JsonParser parser, DeserializationContext context) {
      throw ValueInstantiationException.from(
      parser, "identifier construction failed", context.constructType(String))
    }
  }

  @JsonApiResource(type = "articles")
  static class DirectPropertyArticle {
    @JsonApiId String id
    @JsonApiAttribute @JsonSerialize(using = PropertySerializer) String title
    @JsonApiAttribute @JsonSerialize(using = StructuredSerializer) StructuredValue details
    @JsonApiMeta @JsonSerialize(using = MetaSerializer) MetaValue meta
    @JsonApiRelationship ResourceIdentifier author
    @JsonApiRelationshipMeta("author") @JsonSerialize(using = MetaSerializer) MetaValue authorMeta

    DirectPropertyArticle(
    String id,
    String title,
    StructuredValue details,
    MetaValue meta,
    ResourceIdentifier author,
    MetaValue authorMeta) {
      this.id = id
      this.title = title
      this.details = details
      this.meta = meta
      this.author = author
      this.authorMeta = authorMeta
    }
  }

  @JsonApiResource(type = "ordinary-articles")
  static class OrdinaryPropertyArticle {
    @JsonApiId String id
    String title

    OrdinaryPropertyArticle(String id, String title) {
      this.id = id
      this.title = title
    }
  }

  @JsonApiResource(type = "articles")
  static class NullSerializedArticle {
    @JsonApiId String id
    @JsonApiAttribute @JsonSerialize(nullsUsing = NullPropertySerializer) String title

    NullSerializedArticle(String id, String title) {
      this.id = id
      this.title = title
    }
  }

  static class StructuredValue {
    String value

    StructuredValue(String value) {
      this.value = value
    }
  }

  static class MetaValue {
    String value

    MetaValue(String value) {
      this.value = value
    }
  }

  static class PropertyIdentifier {
    String value

    PropertyIdentifier(String value) {
      this.value = value
    }

    boolean equals(Object other) {
      other instanceof PropertyIdentifier && value == other.value
    }

    int hashCode() {
      value.hashCode()
    }
  }

  @JsonApiResource(type = "mixin-articles")
  static class MixinPropertyArticle {
    @JsonApiId String id
    String title
    @JsonApiMeta MetaValue meta

    MixinPropertyArticle(String id, String title, MetaValue meta) {
      this.id = id
      this.title = title
      this.meta = meta
    }
  }

  static abstract class PropertyCustomizationMixIn {
    @JsonSerialize(using = MixinPropertySerializer)
    abstract String getTitle()

    @JsonSerialize(using = MetaSerializer)
    abstract MetaValue getMeta()
  }

  @JsonApiResource(type = "articles")
  static class DirectIdArticle {
    @JsonApiId @JsonDeserialize(using = IdentifierDeserializer) String id
    @JsonDeserialize(using = IdentifierDeserializer) String title
  }

  @JsonApiResource(type = "articles")
  static class MixinIdArticle {
    @JsonApiId String id
    String title
  }

  static abstract class IdDeserializerMixIn {
    @JsonDeserialize(using = IdentifierDeserializer)
    abstract String getId()
  }

  @JsonApiResource(type = "rich-articles")
  static class RichIdArticle {
    @JsonApiId @JsonDeserialize(using = PropertyIdentifierDeserializer) PropertyIdentifier id

    RichIdArticle() {}
  }

  @JsonApiResource(type = "articles")
  static class FailingIdArticle {
    @JsonApiId @JsonDeserialize(using = FailingIdentifierDeserializer) String id
    String title
  }

  @JsonApiResource(type = "articles")
  static class ScalarSerializedMetaArticle {
    @JsonApiId String id
    @JsonApiMeta @JsonSerialize(using = ScalarMetaSerializer) MetaValue meta

    ScalarSerializedMetaArticle(String id, MetaValue meta) {
      this.id = id
      this.meta = meta
    }
  }

  @JsonApiResource(type = "articles")
  static class DirectIdPatch {
    @JsonApiId @JsonDeserialize(using = IdentifierDeserializer) String id
    @JsonApiAttribute PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class MixinIdPatch {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class FailingIdPatch {
    @JsonApiId @JsonDeserialize(using = FailingIdentifierDeserializer) String id
    @JsonApiAttribute PatchPresence<String> title
  }

  @JsonApiResource(type = "articles")
  static class JsonApiOwnedIdentifier {
    @JsonApiId @JsonSerialize(using = PropertySerializer) String id

    JsonApiOwnedIdentifier(String id) {
      this.id = id
    }
  }
}
