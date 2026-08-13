package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.testfixtures.domainread.ConverterBehavior
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadExpectation
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadInput
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadScenario
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadScenarios
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticleWithArray
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatCountedThing
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatRequiredThing
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatThrowingCreatorThing
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.testmodel.FlatMappedArticle
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.json.JsonMapper

// Shared binder cases live in DomainReadScenarios. This spec runs every catalog entry and asserts
// executedScenarioIds == catalogScenarioIds so a later Jackson 2 binder suite can do the same.
// Adapter-local Jackson-API cases stay here (no shared manifest): custom deserializer, naming
// strategy, mix-in, JavaType entry points, linkage mapper, Optional unwrapping, short-circuit,
// cardinality-before-mapper, LINKAGE_MAPPING_FAILED, and mapper-returning-null.
// @Stepwise pins the declared feature order so the coverage feature always runs after the
// parameterized catalog iterations (Spock does not guarantee feature order otherwise).
@Stepwise
class ResourceBinderSpec extends Specification {

  @Shared
  List<String> executedScenarioIds = []

  @Unroll
  def "binds #scenario.id from the shared catalog"() {
    given:
    executedScenarioIds.add(scenario.id())

    when:
    def result = null
    def thrownException = null
    try {
      result = invoke(scenario)
    } catch (Throwable t) {
      thrownException = t
    }

    then:
    verifyOutcome(scenario, result, thrownException)

    where:
    scenario << DomainReadScenarios.all()
  }

  def "covers every shared domain-read scenario exactly once"() {
    expect:
    executedScenarioIds == DomainReadScenarios.all()*.id
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

  private Object invoke(DomainReadScenario scenario) {
    def binder = binderFor(scenario.converterBehavior())
    def input = scenario.input()
    if (input instanceof DomainReadInput.SingleResource) {
      return binder.fromResource(input.resource(), scenario.targetType())
    }
    if (input instanceof DomainReadInput.ResourceCollection) {
      return binder.fromResources(input.resources(), scenario.targetType())
    }
    if (input instanceof DomainReadInput.IncludedIsolation) {
      def reader = JsonApiJackson3.reader(
          JsonMapper.builder().build(), DocumentReadContext.resourceDefaults())
      def first = binder.fromResource(primaryResource(reader, input.primaryJson()), scenario.targetType())
      def second = binder.fromResource(
          primaryResource(reader, input.swappedIncludedJson()), scenario.targetType())
      return [first, second]
    }
    throw new IllegalArgumentException("Unknown input variant: " + input)
  }

  private static JsonApiResourceBinder binderFor(ConverterBehavior behavior) {
    def mapper = JsonMapper.builder().build()
    switch (behavior) {
      case ConverterBehavior.DEFAULT_CONVERT_VALUE:
        return JsonApiJackson3.resourceBinder(mapper)
      case ConverterBehavior.CUSTOM_PARSE_INVERSION:
        return JsonApiJackson3.resourceBinder(mapper, invertingConverter())
      case ConverterBehavior.PARSE_THROWING:
        return JsonApiJackson3.resourceBinder(mapper, throwingConverter())
      case ConverterBehavior.PARSE_RETURNING_NULL:
        return JsonApiJackson3.resourceBinder(mapper, nullParseConverter())
      default:
        throw new IllegalArgumentException("Unknown converter behavior: " + behavior)
    }
  }

  private static IdentifierConverter invertingConverter() {
    return new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return "prefix-" + idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            return wireIdentifier - "prefix-"
          }
        }
  }

  private static IdentifierConverter throwingConverter() {
    return new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            throw new IllegalArgumentException("bad id")
          }
        }
  }

  private static IdentifierConverter nullParseConverter() {
    return new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            return idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            return null
          }
        }
  }

  private static void verifyOutcome(
      DomainReadScenario scenario, Object result, Throwable thrownException) {
    def expectation = scenario.expectation()
    if (expectation instanceof DomainReadExpectation.Failure) {
      assert thrownException instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) thrownException
      assert ex.diagnostic() == expectation.diagnostic()
      if (expectation.propertyPath() != null) {
        assert ex.propertyPath() == expectation.propertyPath()
      }
      if (expectation.resourceClass() != null) {
        assert ex.resourceClass() == expectation.resourceClass()
      }
      assertAdapterLocalFailureDetails(scenario, ex)
      return
    }
    assert thrownException == null
    def expected = ((DomainReadExpectation.BoundValue) expectation).value()
    if (scenario.input() instanceof DomainReadInput.IncludedIsolation) {
      assert result instanceof List
      assert ((List) result).size() == 2
      assertBoundValue(expected, ((List) result)[0])
      assertBoundValue(expected, ((List) result)[1])
      return
    }
    assertBoundValue(expected, result)
  }

  private static void assertBoundValue(Object expected, Object actual) {
    if (expected instanceof FlatArticleWithArray) {
      def exp = (FlatArticleWithArray) expected
      def act = (FlatArticleWithArray) actual
      assert act.id() == exp.id()
      assert act.title() == exp.title()
      assert act.comments() == exp.comments()
      return
    }
    if (expected instanceof List) {
      assert actual instanceof List
      def expectedList = (List) expected
      def actualList = (List) actual
      assert actualList.size() == expectedList.size()
      expectedList.eachWithIndex { item, index ->
        assertBoundValue(item, actualList[index])
      }
      return
    }
    assert actual == expected
  }

  // Jackson-derived property-name paths and cause types stay adapter-local until Jackson 2 proves
  // them portable. Dispatch uses target type, diagnostic, and input shape - never scenario id.
  private static void assertAdapterLocalFailureDetails(
      DomainReadScenario scenario, JsonApiMappingException ex) {
    def expectation = (DomainReadExpectation.Failure) scenario.expectation()
    def target = scenario.targetType()
    if (expectation.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
        && target == FlatRequiredThing) {
      assert ex.propertyPath() == "/required"
      return
    }
    if (expectation.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
        && target == FlatThrowingCreatorThing) {
      assert ex.cause instanceof ValueInstantiationException
      // Observed Jackson 3 path: ValueInstantiationException has no named databind property.
      assert ex.propertyPath() == "/"
      return
    }
    if (expectation.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
        && target == FlatCountedThing) {
      // Observed Jackson 3 databind path for both the nested-map coercion failure and the
      // explicit-null-into-primitive failure.
      assert ex.propertyPath() == "/count"
    }
  }

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

  // Adapter-local DTO shapes (Jackson-API-specific binder cases)

  @JsonApiResource(type = "words")
  static class FlatWords {
    @JsonApiId String id
    int longFieldName
    int otherValue
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

  @JsonApiResource(type = "articles")
  static class FlatMappedOptionalArticle {
    @JsonApiId String id
    @JsonApiRelationship Optional<FlatAuthor> author
    @JsonApiRelationship List<FlatAuthor> contributors
  }
}
