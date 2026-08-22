package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.DomainData
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchPresence
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

// Adapter-specific regression coverage for the configured-Jackson resource metadata
// authority: class-level @JsonApiResource metadata is resolved through the configured mapper's
// introspection (so class-level mix-ins provide or override it) everywhere — direct domain write,
// flat read/binding, low-level PATCH, typed PATCH DTO, registry key derivation, and declared
// to-many relationship target validation. Mix-in mechanics are Jackson-specific and stay local.
class ConfiguredResourceMetadataAuthoritySpec extends Specification {

  JsonMapper mixinMapper() {
    JsonMapper.builder()
        .addMixIn(MixinOnlyArticle, ArticleTypeMixin)
        .addMixIn(MixinFlatArticle, FlatArticleTypeMixin)
        .addMixIn(MixinPatchTarget, PatchTargetTypeMixin)
        .addMixIn(MixinPresencePatch, PresencePatchTypeMixin)
        .addMixIn(MixinComment, CommentTypeMixin)
        .build()
  }

  // ---- fixtures ----------------------------------------------------------

  /** No direct annotation anywhere: only the configured mix-in supplies resource metadata. */
  static class MixinOnlyArticle {
    String id
    String title
  }

  @JsonApiResource(type = "mixin-articles")
  interface ArticleTypeMixin {}

  /** Direct annotation that a configured mix-in overrides (mix-in precedence). */
  @JsonApiResource(type = "direct-articles")
  static class DirectlyTypedArticle {
    String id
    String title
  }

  @JsonApiResource(type = "resource-bases")
  static class ResourceBase {
    String id
  }

  static class ResourceChild extends ResourceBase {
    String title
  }

  @JsonApiResource(type = "interface-resources")
  interface ResourceTypeInterface {}

  static class InterfaceResource implements ResourceTypeInterface {
    String id
  }

  @JsonApiResource(type = "override-articles")
  interface OverridingTypeMixin {}

  /** Read/binding DTO whose wire type exists only through its mix-in. */
  static class MixinFlatArticle {
    String id
    String title
  }

  @JsonApiResource(type = "mixin-flat-articles")
  interface FlatArticleTypeMixin {}

  /** Low-level PATCH target whose wire type exists only through its mix-in. */
  static class MixinPatchTarget {
    String id
    @JsonApiAttribute String title
  }

  @JsonApiResource(type = "mixin-patch-articles")
  interface PatchTargetTypeMixin {}

  /** Typed PATCH DTO whose wire type exists only through its mix-in. */
  static class MixinPresencePatch {
    String id
    @JsonApiAttribute PatchPresence<String> title
  }

  @JsonApiResource(type = "mixin-presence-articles")
  interface PresencePatchTypeMixin {}

  /** Declared to-many element type whose wire type exists only through its mix-in. */
  static class MixinComment {
    String id
  }

  @JsonApiResource(type = "mixin-comments")
  interface CommentTypeMixin {}

  @JsonApiResource(type = "blogs")
  static class BlogWithMixinComments {
    String id
    @JsonApiRelationship List<MixinComment> comments
  }

  /** Directly annotated clashing type for registry conflict dispatch semantics. */
  @JsonApiResource(type = "mixin-flat-articles")
  static class ClashingFlatArticle {
    String id
  }

  // ---- helpers -----------------------------------------------------------

  private static ResourceObject resource(String type, String id, Map attrs) {
    new ResourceObject(
        type,
        id,
        null,
        attrs == null ? null : Attributes.ofAttributes(attrs),
        null,
        null,
        null,
        Map.of())
  }

  private static String updateJson(String type, String id, String title) {
    '{"data":{"type":"' + type + '","id":"' + id + '","attributes":{"title":"' + title + '"}}}'
  }

  // ---- 1. direct domain write ---------------------------------------------

  def "domain write uses the mix-in-provided resource type"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(mixinMapper())
    def article = new MixinOnlyArticle(id: "1", title: "Hello")

    when:
    def resource = mapper.toResource(article)

    then:
    resource.type() == "mixin-articles"
    resource.id() == "1"
    resource.attributes().attributes().title == "Hello"
  }

  def "configured mix-in overrides the directly declared resource type on write"() {
    given:
    def overrideMapper = JsonMapper.builder()
        .addMixIn(DirectlyTypedArticle, OverridingTypeMixin)
        .build()
    def mapper = JsonApiJackson3.resourceMapper(overrideMapper)

    when:
    def resource = mapper.toResource(new DirectlyTypedArticle(id: "2", title: "Hi"))

    then:
    resource.type() == "override-articles"
  }

  def "plain mapper without the mix-in still rejects the unannotated type"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

    when:
    mapper.toResource(new MixinOnlyArticle(id: "1", title: "Hello"))

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    ex.resourceClass() == MixinOnlyArticle
  }

  def "resource metadata does not inherit from an annotated superclass"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

    when:
    mapper.toResource(new ResourceChild(id: "1", title: "Child"))

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    ex.resourceClass() == ResourceChild
  }

  def "resource metadata does not inherit from an annotated interface"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

    when:
    mapper.toResource(new InterfaceResource(id: "1"))

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    ex.resourceClass() == InterfaceResource
  }

  // ---- 2. ordinary flat read / binding ------------------------------------

  def "flat binding expects the mix-in-derived wire type"() {
    given:
    def binder = JsonApiJackson3.resourceBinder(mixinMapper())

    when:
    def dto = binder.fromResource(
        resource("mixin-flat-articles", "9", [title: "Bound"]), MixinFlatArticle)

    then:
    dto.id == "9"
    dto.title == "Bound"

    when:
    binder.fromResource(resource("articles", "9", [title: "Bound"]), MixinFlatArticle)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
  }

  // ---- 3. low-level PATCH --------------------------------------------------

  def "low-level PATCH recognizes the mix-in-declared resource type"() {
    given:
    def reader = JsonApiJackson3.patchReader(mixinMapper())

    when:
    def command = reader.readValue(updateJson("mixin-patch-articles", "7", "New"), MixinPatchTarget)

    then:
    command.resourceType() == MixinPatchTarget
    command.identity() == "7"
    command.changes().size() == 1
    ((PatchChange.AttributeChange) command.changes().first()).value() == "New"

    when:
    reader.readValue(updateJson("patch-articles", "7", "New"), MixinPatchTarget)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
  }

  // ---- 4. typed PATCH DTO ---------------------------------------------------

  def "typed PATCH DTO binds through the mix-in-declared resource type"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(mixinMapper())

    when:
    def dto = reader.readValue(
        updateJson("mixin-presence-articles", "5", "Updated"), MixinPresencePatch)

    then:
    dto.id == "5"
    dto.title == PatchPresence.present("Updated")

    when:
    reader.readValue(updateJson("presence-articles", "5", "Updated"), MixinPresencePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
  }

  // ---- 5. typed-envelope registry dispatch ----------------------------------

  def "registry keys derive from configured metadata and dispatch honors mix-ins"() {
    given:
    def base = mixinMapper()
    def registry = ResourceTypeRegistry.builder(base)
        .register(MixinFlatArticle)
        .build()
    def reader = JsonApiJackson3.domainDocumentReader(
        base, DocumentReadContext.resourceDefaults(), registry)
    def json =
        '{"data":{"type":"mixin-flat-articles","id":"3","attributes":{"title":"Enveloped"}}}'

    when:
    def envelope = reader.readValue(json)

    then:
    ((DomainData.SingleResource) envelope.data()).resource() instanceof MixinFlatArticle
  }

  def "JavaType registration keys through configured metadata too"() {
    given:
    def base = mixinMapper()
    def registry = ResourceTypeRegistry.builder(base)
        .register(base.constructType(MixinFlatArticle))
        .build()
    def reader = JsonApiJackson3.domainDocumentReader(
        base, DocumentReadContext.resourceDefaults(), registry)

    when:
    def envelope = reader.readValue(
        '{"data":[{"type":"mixin-flat-articles","id":"1","attributes":{"title":"A"}},'
        + '{"type":"mixin-flat-articles","id":"2","attributes":{"title":"B"}}]}')

    then:
    ((DomainData.ResourceCollection) envelope.data()).resources()*.id == ["1", "2"]
  }

  def "registration without the configured mix-in fails with no reflection fallback"() {
    when:
    ResourceTypeRegistry.builder(JsonMapper.builder().build())
        .register(MixinFlatArticle)
        .build()

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    ex.resourceClass() == MixinFlatArticle
  }

  def "registry metadata lookup does not inherit resource annotations"() {
    when:
    ResourceTypeRegistry.builder(JsonMapper.builder().build())
        .register(ResourceChild)
        .build()

    then:
    def childEx = thrown(JsonApiMappingException)
    childEx.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    childEx.resourceClass() == ResourceChild

    when:
    ResourceTypeRegistry.builder(JsonMapper.builder().build())
        .register(InterfaceResource)
        .build()

    then:
    def interfaceEx = thrown(JsonApiMappingException)
    interfaceEx.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
    interfaceEx.resourceClass() == InterfaceResource
  }

  def "the same class registers under different keys for different configured mappers"() {
    given:
    def plainKey = ResourceTypeRegistry.builder(JsonMapper.builder().build())
        .register(DirectlyTypedArticle)
        .build()
    def overrideBase = JsonMapper.builder()
        .addMixIn(DirectlyTypedArticle, OverridingTypeMixin)
        .build()
    def overrideKey = ResourceTypeRegistry.builder(overrideBase)
        .register(DirectlyTypedArticle)
        .build()

    expect:
    plainKey.resolve("direct-articles") != null
    plainKey.resolve("override-articles") == null
    overrideKey.resolve("override-articles") != null
    overrideKey.resolve("direct-articles") == null
  }

  def "duplicate configured keys still fail at build with CONFLICTING_TYPE_REGISTRATION"() {
    when:
    ResourceTypeRegistry.builder(mixinMapper())
        .register(MixinFlatArticle)
        .register(ClashingFlatArticle)
        .build()

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION
    ex.resourceClass() == ClashingFlatArticle
  }

  // ---- 6. declared to-many relationship target validation -------------------

  def "declared to-many linkage derives element types from configured metadata"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(mixinMapper())
    def blog = new BlogWithMixinComments(
        id: "b1",
        comments: [
          new MixinComment(id: "c1"),
          new MixinComment(id: "c2")
        ])

    when:
    def resource = mapper.toResource(blog)

    then:
    def linkage = (RelationshipData.IdentifierCollectionLinkage) resource.relationships()
        .relationships().get("comments").data()
    linkage.identifiers() == [
      ResourceIdentifier.of("mixin-comments", "c1"),
      ResourceIdentifier.of("mixin-comments", "c2")
    ]
  }

  def "declared to-many element type without configured metadata keeps its diagnostic"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def blog = new BlogWithMixinComments(id: "b1", comments: [new MixinComment(id: "c1")])

    when:
    mapper.toResource(blog)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE
    ex.resourceClass() == MixinComment
  }
}
