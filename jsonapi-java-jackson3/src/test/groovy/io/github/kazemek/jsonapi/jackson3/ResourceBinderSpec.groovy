package io.github.kazemek.jsonapi.jackson3

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.mapping.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipLinkage
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticleMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticleWithMapMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticleWithOptionalMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.AuthorIdMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.AuthorMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.CommentIdMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.WholeMetaTargetFixtures
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticleWithArray
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticleWithOptional
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticleWithSet
import io.github.kazemek.jsonapi.fixtures.domainread.FlatCountedThing
import io.github.kazemek.jsonapi.fixtures.domainread.FlatCreatorArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatDefaultedArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatInheritedBlog
import io.github.kazemek.jsonapi.fixtures.domainread.FlatIntIdArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatLidArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatMetaArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatMutableArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatRelationshipLinkageArticle
import io.github.kazemek.jsonapi.fixtures.domainread.FlatRequiredThing
import io.github.kazemek.jsonapi.fixtures.domainread.FlatThingWithIgnored
import io.github.kazemek.jsonapi.fixtures.domainread.FlatThrowingCreatorThing
import io.github.kazemek.jsonapi.fixtures.domainread.FlatUnregisteredRelationshipsArticle
import io.github.kazemek.jsonapi.fixtures.domainwrite.ArticleWithUnannotatedExtra
import io.github.kazemek.jsonapi.fixtures.domainwrite.BlogWithJsonProperty
import io.github.kazemek.jsonapi.fixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.fixtures.domainwrite.ConventionalId
import io.github.kazemek.jsonapi.fixtures.domainwrite.Person
import io.github.kazemek.jsonapi.fixtures.domainwrite.RelationshipLinkageContainerFixtures.MapRelationshipLinkageArticle
import io.github.kazemek.jsonapi.fixtures.domainwrite.RelationshipLinkageContainerFixtures.ArrayRelationshipLinkageArticle
import io.github.kazemek.jsonapi.fixtures.domainwrite.RelationshipLinkageContainerFixtures.OptionalRelationshipLinkageArticle
import io.github.kazemek.jsonapi.fixtures.domainwrite.RelationshipLinkageContainerFixtures.RenamedRelationshipLinkageArticle
import io.github.kazemek.jsonapi.jackson3.DirectionalityReadFixtures
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatAuthor
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatMappedArticle
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatMappedOptionalArticle
import io.github.kazemek.jsonapi.jackson3.ParameterizedBindingFixtures.GenericArticle
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.InjectableValues
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.json.JsonMapper

import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.Set

class ResourceBinderSpec extends Specification {

  private static final String ARTICLES = "articles"
  private static final String AUTHOR = "author"
  private static final String COMMENTS = "comments"
  private static final String PEOPLE = "people"
  private static final String THINGS = "things"

  @Shared
  JsonApiResourceBinder binder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

  @Unroll
  def "binds #id successfully"() {
    when:
    def actual = bind(input, targetType, converter)

    then:
    actual == expected

    where:
    id | input | targetType | expected | converter
    "record attributes and built-in relationships" | one(resource(ARTICLES, "1", attrs("title", "Hello", "body-text", "Content"), rels(AUTHOR, single(PEOPLE, "p1"), COMMENTS, collection(COMMENTS, ["c1", "c2"])))) | FlatArticle | new FlatArticle("1", "Hello", "Content", ResourceIdentifier.of(PEOPLE, "p1"), [
      ResourceIdentifier.of(COMMENTS, "c1"),
      ResourceIdentifier.of(COMMENTS, "c2")
    ]) | null
    "mutable DTO" | one(resource(ARTICLES, "1", attrs("title", "Hello"), rels(AUTHOR, single(PEOPLE, "p1")))) | FlatMutableArticle | new FlatMutableArticle("1", "Hello", ResourceIdentifier.of(PEOPLE, "p1")) | null
    "immutable creator DTO" | one(resource(ARTICLES, "42", attrs("title", "Creator"), null)) | FlatCreatorArticle | new FlatCreatorArticle("42", "Creator") | null
    "lid-only resource" | one(resourceWithLid(ARTICLES, "lid-1", attrs("title", "T"))) | FlatLidArticle | new FlatLidArticle("lid-1", "T") | null
    "resource without id or lid" | one(resourceWithLid(ARTICLES, null, attrs("title", "T"))) | FlatLidArticle | new FlatLidArticle(null, "T") | null
    "present Optional relationship" | one(resource(ARTICLES, "1", null, rels(AUTHOR, single(PEOPLE, "p1")))) | FlatArticleWithOptional | new FlatArticleWithOptional("1", null, Optional.of(ResourceIdentifier.of(PEOPLE, "p1"))) | null
    "explicit null Optional relationship" | one(resource(ARTICLES, "1", null, rels(AUTHOR, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))) | FlatArticleWithOptional | new FlatArticleWithOptional("1", null, Optional.empty()) | null
    "array relationship" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, ["c1", "c2"])))) | FlatArticleWithArray | new FlatArticleWithArray("1", null, identifierArray(COMMENTS, "c1", "c2")) | null
    "Set relationship" | one(resource(ARTICLES, "1", null, rels("tags", collection("tags", ["t1", "t2"])))) | FlatArticleWithSet | new FlatArticleWithSet("1", null, Set.of(ResourceIdentifier.of("tags", "t1"), ResourceIdentifier.of("tags", "t2"))) | null
    "Map RelationshipLinkage relationship" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collectionWithIdentifierMeta(COMMENTS, ["c1"], [Meta.of([pinned: true])] )))) | MapRelationshipLinkageArticle | new MapRelationshipLinkageArticle("1", [
      new RelationshipLinkage<>(identifier(COMMENTS, "c1", Meta.of([pinned: true])), [pinned: true])
    ]) | null
    "whole resource and relationship meta" | one(resourceWithMeta(attrs("title", "Hello"), relsWithMeta(single(PEOPLE, "p1"), Meta.of([displayName: "Alice"])), Meta.of([source: "cms", note: "n"]))) | FlatMetaArticle | new FlatMetaArticle("1", "Hello", ResourceIdentifier.of(PEOPLE, "p1"), new ArticleMeta("cms", "n"), new AuthorMeta("Alice")) | null
    "Map whole resource and relationship meta" | one(resourceWithMeta(attrs("title", "Hello"), relsWithMeta(single(PEOPLE, "p1"), Meta.of([displayName: "Alice"])), Meta.of([source: "cms"]))) | ArticleWithMapMeta | new ArticleWithMapMeta("1", "Hello", ResourceIdentifier.of(PEOPLE, "p1"), [source: "cms"], [displayName: "Alice"]) | null
    "Optional whole resource meta" | one(resourceWithMeta(attrs("title", "Hello"), null, Meta.of([source: "cms", note: "n"]))) | ArticleWithOptionalMeta | new ArticleWithOptionalMeta("1", "Hello", null, Optional.of(new ArticleMeta("cms", "n")), Optional.empty()) | null
    "to-one identifier meta" | one(resource(ARTICLES, "1", null, rels(AUTHOR, toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of([role: "editor"])))) ) | FlatRelationshipLinkageArticle | new FlatRelationshipLinkageArticle("1", null, new RelationshipLinkage<>(identifier(PEOPLE, "p1", Meta.of([role: "editor"])), new AuthorIdMeta("editor")), null, null, null) | null
    "to-many identifier meta" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collectionWithIdentifierMeta(COMMENTS, ["c1", "c2"], [Meta.of([pinned: true]), null])))) | FlatRelationshipLinkageArticle | new FlatRelationshipLinkageArticle("1", null, null, [
      new RelationshipLinkage<>(identifier(COMMENTS, "c1", Meta.of([pinned: true])), new CommentIdMeta(true)),
      new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c2"), null)
    ], null, null) | null
    "array RelationshipLinkage relationship" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collectionWithIdentifierMeta(COMMENTS, ["c1", "c2"], [Meta.of([pinned: true]), null])))) | ArrayRelationshipLinkageArticle | new ArrayRelationshipLinkageArticle("1", [
      new RelationshipLinkage<>(identifier(COMMENTS, "c1", Meta.of([pinned: true])), new CommentIdMeta(true)),
      new RelationshipLinkage<>(ResourceIdentifier.of(COMMENTS, "c2"), null)
    ] as RelationshipLinkage[]) | null
    "Optional RelationshipLinkage relationship" | one(resource(ARTICLES, "1", null, rels(AUTHOR, toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of([role: "editor"])))) ) | OptionalRelationshipLinkageArticle | new OptionalRelationshipLinkageArticle("1", Optional.of(new RelationshipLinkage<>(identifier(PEOPLE, "p1", Meta.of([role: "editor"])), new AuthorIdMeta("editor")))) | null
    "renamed RelationshipLinkage relationship" | one(resource(ARTICLES, "1", null, rels(AUTHOR, toOneWithIdentifierMeta(PEOPLE, "p1", Meta.of([role: "editor"])))) ) | RenamedRelationshipLinkageArticle | new RenamedRelationshipLinkageArticle("1", new RelationshipLinkage<>(identifier(PEOPLE, "p1", Meta.of([role: "editor"])), new AuthorIdMeta("editor"))) | null
    "inherited properties" | one(resource("blogs", "b1", attrs("name", "My Blog", "description", "A description"), null)) | FlatInheritedBlog | new FlatInheritedBlog("b1", "My Blog", "A description") | null
    "ignored property" | one(resource(THINGS, "1", attrs("name", "visible", "secret", "hidden"), null)) | FlatThingWithIgnored | new FlatThingWithIgnored("1", "visible", null) | null
    "explicit null preserves default semantics" | one(resource(ARTICLES, "1", nullableAttr("title"), null)) | FlatDefaultedArticle | new FlatDefaultedArticle("1", null, "default") | null
    "unannotated property is ignored" | one(resource(ARTICLES, "1", attrs("title", "Hello", "ignoredExtra", "secret"), null)) | ArticleWithUnannotatedExtra | new ArticleWithUnannotatedExtra("1", "Hello", null) | null
    "conventional identifier" | one(resource("conventionals", "42", attrs("name", "name value"), null)) | ConventionalId | new ConventionalId("42", "name value") | null
    "default numeric identifier conversion" | one(resource(ARTICLES, "42", attrs("title", "T"), null)) | FlatIntIdArticle | new FlatIntIdArticle(42, "T") | null
    "custom IdentifierConverter" | one(resource(ARTICLES, "prefix-42", attrs("title", "T"), null)) | FlatIntIdArticle | new FlatIntIdArticle(42, "T") | invertingConverter()
    "empty List relationship" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, [])))) | FlatArticle | new FlatArticle("1", null, null, null, []) | null
    "explicit null to-one relationship" | one(resource(ARTICLES, "1", null, rels(AUTHOR, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))) | FlatArticle | new FlatArticle("1", null, null, null, null) | null
    "omitted relationship does not bind" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, ["c1"])))) | FlatArticle | new FlatArticle("1", null, null, null, [
      ResourceIdentifier.of(COMMENTS, "c1")
    ]) | null
    "relationship meta without linkage" | one(resourceWithMeta(attrs("title", "Hello"), rels(AUTHOR, Relationship.metaOnly(Meta.of([displayName: "Alice"]))), null)) | FlatMetaArticle | new FlatMetaArticle("1", "Hello", null, null, new AuthorMeta("Alice")) | null
    "resource collection" | many([
      resource(ARTICLES, "1", attrs("title", "One"), null),
      resource(ARTICLES, "2", attrs("title", "Two"), null)
    ]) | FlatArticle | [
      new FlatArticle("1", "One", null, null, null),
      new FlatArticle("2", "Two", null, null, null)
    ] | null
    "included resources are isolated" | includedInput() | FlatArticle | [
      new FlatArticle("1", "T", null, ResourceIdentifier.of(PEOPLE, "p1"), null),
      new FlatArticle("1", "T", null, ResourceIdentifier.of(PEOPLE, "p1"), null)
    ] | null
  }

  @Unroll
  def "fails with #id due to mapping diagnostic"() {
    when:
    bind(input, targetType, converter)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == diagnostic
    ex.propertyPath() == propertyPath
    ex.resourceClass() == resourceClass

    where:
    id | input | targetType | diagnostic | propertyPath | resourceClass | converter
    "resource type mismatch" | one(resource(PEOPLE, "p1", null, null)) | FlatArticle | MappingDiagnostic.RESOURCE_TYPE_MISMATCH | "/type" | FlatArticle | null
    "resource collection validates every element type" | many([
      resource(ARTICLES, "1", null, null),
      resource(PEOPLE, "p1", null, null)
    ]) | FlatArticle | MappingDiagnostic.RESOURCE_TYPE_MISMATCH | "/type" | FlatArticle | null
    "unregistered to-one relationship target" | one(resource(ARTICLES, "1", null, rels(AUTHOR, single(PEOPLE, "p1")))) | FlatUnregisteredRelationshipsArticle | MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET | "/relationships/author/data" | Person | null
    "unregistered to-many relationship target" | one(resource(ARTICLES, "1", null, rels(COMMENTS, collection(COMMENTS, ["c1"])))) | FlatUnregisteredRelationshipsArticle | MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET | "/relationships/comments/data" | List | null
    "identifier converter throws" | one(resource(ARTICLES, "42", null, null)) | FlatIntIdArticle | MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED | "/id" | Integer | throwingConverter()
    "identifier converter returns null" | one(resource(ARTICLES, "42", null, null)) | FlatIntIdArticle | MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED | "/id" | Integer | nullParseConverter()
    "identifier cannot be coerced" | one(resource(ARTICLES, "not-a-number", null, null)) | FlatIntIdArticle | MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED | "/id" | FlatIntIdArticle | null
    "required creator input is absent" | one(resource(THINGS, "1", attrs("title", "present"), null)) | FlatRequiredThing | MappingDiagnostic.MISSING_CREATOR_INPUT | "/attributes/required" | FlatRequiredThing | null
    "creator rejects supplied value" | one(resource(THINGS, "1", attrs("title", "boom"), null)) | FlatThrowingCreatorThing | MappingDiagnostic.MISSING_CREATOR_INPUT | null | FlatThrowingCreatorThing | null
    "attribute value cannot be coerced" | one(resource(THINGS, "1", nestedCountAttrs(), null)) | FlatCountedThing | MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE | "/attributes/count" | FlatCountedThing | null
    "explicit null cannot bind to primitive" | one(resource(THINGS, "1", nullableAttr("count"), null)) | FlatCountedThing | MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE | "/attributes/count" | FlatCountedThing | null
    "scalar whole-meta target" | one(resource(ARTICLES, "1", null, null)) | WholeMetaTargetFixtures.ScalarMetaArticle | MappingDiagnostic.INVALID_META_TARGET | "/meta" | WholeMetaTargetFixtures.ScalarMetaArticle | null
    "list whole-meta target" | one(resource(ARTICLES, "1", null, null)) | WholeMetaTargetFixtures.ListMetaArticle | MappingDiagnostic.INVALID_META_TARGET | "/meta" | WholeMetaTargetFixtures.ListMetaArticle | null
    "UUID whole-meta target" | one(resource(ARTICLES, "1", null, null)) | WholeMetaTargetFixtures.UuidMetaArticle | MappingDiagnostic.INVALID_META_TARGET | "/meta" | WholeMetaTargetFixtures.UuidMetaArticle | null
    "java.time whole-meta target" | one(resource(ARTICLES, "1", null, null)) | WholeMetaTargetFixtures.InstantMetaArticle | MappingDiagnostic.INVALID_META_TARGET | "/meta" | WholeMetaTargetFixtures.InstantMetaArticle | null
    "URI whole-meta target" | one(resource(ARTICLES, "1", null, null)) | WholeMetaTargetFixtures.UriMetaArticle | MappingDiagnostic.INVALID_META_TARGET | "/meta" | WholeMetaTargetFixtures.UriMetaArticle | null
    "collection linkage on to-one" | one(resource(ARTICLES, "1", null, rels(AUTHOR, collection(PEOPLE, ["p1", "p2"])))) | FlatArticle | MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH | "/relationships/author/data" | ResourceIdentifier | null
    "null linkage on to-many" | one(resource(ARTICLES, "1", null, rels(COMMENTS, Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))) | FlatArticle | MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH | "/relationships/comments/data" | List | null
    "single linkage on to-many" | one(resource(ARTICLES, "1", null, rels(COMMENTS, single(COMMENTS, "c1")))) | FlatArticle | MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH | "/relationships/comments/data" | List | null
    "empty collection linkage on to-one" | one(resource(ARTICLES, "1", null, rels(AUTHOR, collection(PEOPLE, [])))) | FlatArticle | MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH | "/relationships/author/data" | ResourceIdentifier | null
    "getter-only attribute" | one(resource("getter-only", "1", attrs("title", "supplied"), null)) | DirectionalityReadFixtures.GetterOnly | MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY | "/attributes/title" | DirectionalityReadFixtures.GetterOnly | null
    "getter-only identifier from id" | one(resource("getter-only-id", "supplied", null, null)) | DirectionalityReadFixtures.GetterOnlyIdentifier | MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY | "/id" | DirectionalityReadFixtures.GetterOnlyIdentifier | null
    "getter-only identifier from lid" | one(resourceWithLid("getter-only-id", "client-lid", null)) | DirectionalityReadFixtures.GetterOnlyIdentifier | MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY | "/lid" | DirectionalityReadFixtures.GetterOnlyIdentifier | null
  }

  def "naming strategy renames bound attribute keys"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def localBinder = JsonApiJackson3.resourceBinder(jsonMapper)
    def resource = resource("words", "1", [long_field_name: 10, other_value: 42], null)

    when:
    def thing = localBinder.fromResource(resource, FlatWords)

    then:
    thing.longFieldName == 10
    thing.otherValue == 42
  }

  def "mix-in attribute name is honored"() {
    given:
    def jsonMapper = JsonMapper.builder()
        .addMixIn(FlatNamedThing, FlatMixInDef)
        .build()
    def localBinder = JsonApiJackson3.resourceBinder(jsonMapper)
    def resource = resource("named", "1", ["custom-name": "hello"], null)

    when:
    def thing = localBinder.fromResource(resource, FlatNamedThing)

    then:
    thing.value == "hello"
  }

  def "custom deserializer applies to attribute value"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resource("things", "1", [title: "hello"], null)

    when:
    def thing = localBinder.fromResource(resource, FlatLoudThing)

    then:
    thing.title == "HELLO"
  }

  def "setter-only mapped property is supported by ordinary flat reads"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    def dto = localBinder.fromResource(resource("setter-only", "1", [title: "bound"], null),
    DirectionalityReadFixtures.SetterOnly)

    then:
    dto.id == "1"
    dto.titleValue() == "bound"
  }

  def "creator-only mapped property is supported by ordinary flat reads"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    def dto = localBinder.fromResource(resource("creator-only", "1", [title: "bound"], null),
    DirectionalityReadFixtures.CreatorOnly)

    then:
    dto.idValue() == "1"
    dto.titleValue() == "bound"
  }

  def "supplied injection-only creator property is rejected"() {
    given:
    def mapper = JsonMapper.builder()
        .injectableValues(new InjectableValues.Std().addValue("injected-title", "injected"))
        .build()
    def localBinder = JsonApiJackson3.resourceBinder(mapper)

    when:
    localBinder.fromResource(
        resource("injection-only", "1", [title: "supplied"], null),
        DirectionalityReadFixtures.InjectionOnly)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY
    ex.propertyPath() == "/attributes/title"
    ex.resourceClass() == DirectionalityReadFixtures.InjectionOnly
  }

  def "omitted injection-only creator property still uses Jackson injection"() {
    given:
    def mapper = JsonMapper.builder()
        .injectableValues(new InjectableValues.Std().addValue("injected-title", "injected"))
        .build()
    def localBinder = JsonApiJackson3.resourceBinder(mapper)

    when:
    def dto = localBinder.fromResource(
        resource("injection-only", "1", null, null),
        DirectionalityReadFixtures.InjectionOnly)

    then:
    dto.idValue() == "1"
    dto.titleValue() == "injected"
  }

  def "root type information does not hide effective flat-read properties"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    def dto = localBinder.fromResource(
        resource("root-typed", "1", [title: "bound"], null),
        DirectionalityReadFixtures.RootTyped)

    then:
    dto.id == "1"
    dto.title == "bound"
  }

  def "Jackson write-only mapped property is supported by ordinary flat reads"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    def dto = localBinder.fromResource(resource("write-only", "1", [title: "bound"], null),
    DirectionalityReadFixtures.WriteOnly)

    then:
    dto.id == "1"
    dto.titleValue() == "bound"
  }

  def "supplied getter-only mapped property is rejected instead of silently discarded"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    localBinder.fromResource(resource("getter-only", "1", [title: "supplied"], null),
    DirectionalityReadFixtures.GetterOnly)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY
    ex.propertyPath() == "/attributes/title"
    ex.resourceClass() == DirectionalityReadFixtures.GetterOnly
  }

  def "supplied getter-only identifier is rejected at /id"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())

    when:
    localBinder.fromResource(resource("getter-only-id", "supplied", null, null),
        DirectionalityReadFixtures.GetterOnlyIdentifier)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY
    ex.propertyPath() == "/id"
    ex.resourceClass() == DirectionalityReadFixtures.GetterOnlyIdentifier
  }

  def "supplied getter-only identifier is rejected at /lid"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def resource = resourceWithLid("getter-only-id", "client-lid", null)

    when:
    localBinder.fromResource(resource, DirectionalityReadFixtures.GetterOnlyIdentifier)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY
    ex.propertyPath() == "/lid"
    ex.resourceClass() == DirectionalityReadFixtures.GetterOnlyIdentifier
  }

  def "supplied property excluded by the default deserialization view is rejected"() {
    given:
    def mapper = JsonMapper.builder()
        .defaultDeserializationView(DirectionalityReadFixtures.IncludedInReadView)
        .build()
    def localBinder = JsonApiJackson3.resourceBinder(mapper)

    when:
    localBinder.fromResource(
        resource("view-restricted", "1", [hidden: "supplied"], null),
        DirectionalityReadFixtures.ViewRestricted)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NON_DESERIALIZABLE_PROPERTY
    ex.propertyPath() == "/attributes/hidden"
    ex.resourceClass() == DirectionalityReadFixtures.ViewRestricted
  }

  def "JavaType entry points bind resource and collection"() {
    given:
    def localBinder = JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
    def javaType = JsonMapper.builder().build().constructType(FlatArticle)

    when:
    def article = localBinder.fromResource(resource(ARTICLES, "1", [title: "T"], null), javaType)
    def articles = localBinder.fromResources(
        [
          resource(ARTICLES, "1", null, null),
          resource(ARTICLES, "2", null, null)
        ], javaType)

    then:
    article instanceof FlatArticle
    (article as FlatArticle).title() == "T"
    articles.size() == 2
  }

  def "generic relationship target resolves through the parameterized JavaType"() {
    given:
    def mapper = JsonMapper.builder().build()
    def localBinder = JsonApiJackson3.resourceBinder(mapper)
    def javaType =
        mapper.typeFactory.constructParametricType(GenericArticle, ResourceIdentifier)
    def resource = resource(ARTICLES, "1", null, [author: single(PEOPLE, "p1")])

    when:
    def dto = localBinder.fromResource(resource, javaType)

    then:
    dto instanceof GenericArticle
    ((GenericArticle) dto).author() == ResourceIdentifier.of(PEOPLE, "p1")
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
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        ARTICLES, "1", null,
        [author: single(PEOPLE, "p1"),
          contributors: collection(PEOPLE, ["p1", "p2"])])

    when:
    def article = localBinder.fromResource(resource, FlatMappedArticle)

    then:
    article.author() == new FlatAuthor(PEOPLE, "p1")
    article.contributors() == [
      new FlatAuthor(PEOPLE, "p1"),
      new FlatAuthor(PEOPLE, "p2")
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
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        ARTICLES, "1", null,
        [author: single(PEOPLE, "p1"),
          contributors: collection(PEOPLE, ["p1", "p2"])])

    when:
    def article = localBinder.fromResource(resource, FlatMappedOptionalArticle)

    then:
    seenTypes*.rawClass == [FlatAuthor, List]
    article.author == Optional.of(new FlatAuthor(PEOPLE, "p1"))
    article.contributors == [
      new FlatAuthor(PEOPLE, "p1"),
      new FlatAuthor(PEOPLE, "p2")
    ]
  }

  def "NullLinkage and empty linkage short-circuit without invoking the mapper"() {
    given:
    def invoked = false
    def mapper = { RelationshipData data, JavaType target ->
      invoked = true
      return null
    } as RelationshipLinkageMapper
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        ARTICLES, "1", null,
        [author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE),
          contributors: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())])

    when:
    def article = localBinder.fromResource(resource, FlatMappedArticle)

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
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(
        ARTICLES, "1", null,
        [author: collection(PEOPLE, ["p1"]),
          contributors: single(PEOPLE, "p1")])

    when:
    localBinder.fromResource(resource, FlatMappedArticle)

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
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(ARTICLES, "1", null, [author: single(PEOPLE, "p1")])

    when:
    localBinder.fromResource(resource, FlatMappedArticle)

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
    def localBinder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): mapper])
    def resource = resource(ARTICLES, "1", null, [author: single(PEOPLE, "p1")])

    when:
    def article = localBinder.fromResource(resource, FlatMappedArticle)

    then:
    article.author() == null
  }

  private Object bind(Map input, Class targetType, IdentifierConverter converter) {
    def localBinder = converter == null
        ? binder
        : JsonApiJackson3.resourceBinder(JsonMapper.builder().build(), converter)
    def kind = input["kind"]
    def value = input["value"]
    switch (kind) {
      case "single":
        return localBinder.fromResource(value as ResourceObject, targetType)
      case "collection":
        return localBinder.fromResources(value as List, targetType)
      case "dual":
        List<JsonApiDocument> documents = value as List<JsonApiDocument>
        return documents.collect { JsonApiDocument document ->
          localBinder.fromResource(primaryResource(document), targetType)
        }
      default:
        throw new IllegalArgumentException("Unknown binder input: " + kind)
    }
  }

  private static Map one(ResourceObject resource) {
    [kind: "single", value: resource]
  }

  private static Map many(List<ResourceObject> resources) {
    [kind: "collection", value: resources]
  }

  private static Map dual(JsonApiDocument first, JsonApiDocument second) {
    [kind: "dual", value: [first, second]]
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

  private static ResourceObject resourceWithLid(String type, String lid, Map attrs) {
    new ResourceObject(
        type,
        null,
        lid,
        attrs == null ? null : Attributes.ofAttributes(attrs),
        null,
        null,
        null,
        Map.of())
  }

  private static ResourceObject resourceWithMeta(Map attrs, Map rels, Meta meta) {
    new ResourceObject(
        ARTICLES,
        "1",
        null,
        attrs == null ? null : Attributes.ofAttributes(attrs),
        rels == null ? null : Relationships.ofRelationships(rels),
        null,
        meta,
        Map.of())
  }

  private static JsonApiDocument document(ResourceObject resource, List<ResourceObject> included) {
    new JsonApiDocument(
        new DocumentData.SingleResource(resource),
        null,
        null,
        null,
        null,
        included,
        Map.of())
  }

  private static Map includedInput() {
    def firstPrimary = resource(ARTICLES, "1", attrs("title", "T"), rels(AUTHOR, single(PEOPLE, "p1")))
    def secondPrimary = resource(ARTICLES, "1", attrs("title", "T"), rels(AUTHOR, single(PEOPLE, "p1")))
    dual(
        document(firstPrimary, [
          resource(PEOPLE, "p1", attrs("name", "Alice"), null)
        ]),
        document(secondPrimary, [
          resource(PEOPLE, "p1", attrs("name", "AliceChanged"), null)
        ]))
  }

  private static Relationship single(String type, String id) {
    Relationship.withData(new RelationshipData.SingleLinkage(ResourceIdentifier.of(type, id)))
  }

  private static Relationship collection(String type, List<String> ids) {
    Relationship.withData(
        new RelationshipData.IdentifierCollectionLinkage(
        ids.collect { ResourceIdentifier.of(type, it) }))
  }

  private static Relationship collectionWithIdentifierMeta(
      String type, List<String> ids, List<Meta> metas) {
    List<ResourceIdentifier> identifiers = new ArrayList<>(ids.size())
    for (int i = 0; i < ids.size(); i++) {
      identifiers.add(identifier(type, ids.get(i), metas.get(i)))
    }
    Relationship.withData(new RelationshipData.IdentifierCollectionLinkage(identifiers))
  }

  private static Relationship toOneWithIdentifierMeta(String type, String id, Meta meta) {
    new Relationship(
        new RelationshipData.SingleLinkage(identifier(type, id, meta)),
        null,
        null,
        Map.of())
  }

  private static Map relsWithMeta(Relationship relationship, Meta meta) {
    [(AUTHOR): new Relationship(relationship.data(), relationship.links(), meta, Map.of())]
  }

  private static ResourceIdentifier identifier(String type, String id, Meta meta) {
    new ResourceIdentifier(type, id, null, meta, Map.of())
  }

  private static ResourceIdentifier[] identifierArray(String type, String... ids) {
    ids.collect { ResourceIdentifier.of(type, it) } as ResourceIdentifier[]
  }

  private static Map attrs(Object... keyValues) {
    Map attributes = new LinkedHashMap()
    for (int i = 0; i < keyValues.length; i += 2) {
      attributes.put(keyValues[i], keyValues[i + 1])
    }
    attributes
  }

  private static Map nullableAttr(String name) {
    [(name): null]
  }

  private static Map rels(Object... keyValues) {
    Map relationships = new LinkedHashMap()
    for (int i = 0; i < keyValues.length; i += 2) {
      relationships.put(keyValues[i], keyValues[i + 1])
    }
    relationships
  }

  private static Map nestedCountAttrs() {
    [count: [nested: 1]]
  }

  private static IdentifierConverter invertingConverter() {
    new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            "prefix-" + idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            wireIdentifier - "prefix-"
          }
        }
  }

  private static IdentifierConverter throwingConverter() {
    new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            throw new IllegalArgumentException("bad id")
          }
        }
  }

  private static IdentifierConverter nullParseConverter() {
    new IdentifierConverter() {
          @Override
          String convert(Object idValue) {
            idValue.toString()
          }

          @Override
          Object parse(String wireIdentifier) {
            null
          }
        }
  }

  private static ResourceObject primaryResource(JsonApiDocument document) {
    def data = document.data()
    assert data instanceof DocumentData.SingleResource
    ((DocumentData.SingleResource) data).resource()
  }

  @JsonApiResource(type = "words")
  static class FlatWords {
    @JsonApiId String id
    @JsonApiAttribute int longFieldName
    @JsonApiAttribute int otherValue
  }

  @JsonApiResource(type = "named")
  static class FlatNamedThing {
    @JsonApiId String id
    String value
  }

  abstract static class FlatMixInDef {
    @JsonApiAttribute @JsonProperty("custom-name")
    abstract String getValue()
  }

  static class UppercaseDeserializer extends StdDeserializer<String> {
    UppercaseDeserializer() {
      super(String.class)
    }

    @Override
    String deserialize(JsonParser parser, DeserializationContext context) {
      parser.getValueAsString().toUpperCase()
    }
  }

  @JsonApiResource(type = "things")
  static class FlatLoudThing {
    @JsonApiId String id
    @JsonDeserialize(using = UppercaseDeserializer)
    @JsonApiAttribute
    String title
  }
}
