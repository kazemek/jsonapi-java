package io.github.kazemek.jsonapi.jackson.api

import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.validation.EndpointIdentity
import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.mapping.IncludedResources
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection
import spock.lang.Specification

class LevelOneApiContractSpec extends Specification {

  def "write options defaults compose empty envelope, empty selection, and default policy"() {
    when:
    def options = ResourceWriteOptions.defaults()

    then:
    options.envelope() == new DocumentEnvelope(null, null, null)
    options.selection() == RepresentationSelection.none()
    options.policy() == RepresentationPolicy.defaults()
  }

  def "write options derivations preserve independent values"() {
    given:
    def envelope = new DocumentEnvelope(Links.empty(), Meta.empty(), JsonApiObject.ofVersion("1.1"))
    def selection = RepresentationSelection.builder().include("comments.author").build()
    def policy = RepresentationPolicy.defaults().withMaxIncludeDepth(2)

    when:
    def options = ResourceWriteOptions.defaults()
        .withEnvelope(envelope)
        .withSelection(selection)
        .withPolicy(policy)

    then:
    options.envelope().is(envelope)
    options.selection().is(selection)
    options.policy().is(policy)
  }

  def "write options reject null components"() {
    when:
    new ResourceWriteOptions(null, RepresentationSelection.none(), RepresentationPolicy.defaults())

    then:
    thrown(NullPointerException)

    when:
    ResourceWriteOptions.defaults().withEnvelope(null)

    then:
    thrown(NullPointerException)

    when:
    ResourceWriteOptions.defaults().withSelection(null)

    then:
    thrown(NullPointerException)

    when:
    ResourceWriteOptions.defaults().withPolicy(null)

    then:
    thrown(NullPointerException)
  }

  def "absent per-write jsonapi stays distinct from an explicit per-write value"() {
    expect:
    ResourceWriteOptions.defaults().envelope().jsonapi() == null
    new ResourceWriteOptions(
        new DocumentEnvelope(null, null, JsonApiObject.ofVersion("1.1")),
        RepresentationSelection.none(),
        RepresentationPolicy.defaults()).envelope().jsonapi() == JsonApiObject.ofVersion("1.1")
  }

  def "typed single document requires a resource and allows absent top-level members"() {
    when:
    def document = new ResourceDocument<>("dto", null, null, null, null)

    then:
    document.resource() == "dto"
    document.meta() == null
    document.links() == null
    document.jsonapi() == null
    document.included() == null

    when:
    new ResourceDocument<>(null, null, null, null, null)

    then:
    thrown(NullPointerException)
  }

  def "typed collection document copies resources and rejects null elements"() {
    given:
    def source = new ArrayList<>(["a"])

    when:
    def document = new ResourceCollectionDocument<>(source, null, null, null, null)
    source.add("b")

    then:
    document.resources() == ["a"]

    when:
    document.resources().add("c")

    then:
    thrown(UnsupportedOperationException)

    when:
    new ResourceCollectionDocument<>([null], null, null, null, null)

    then:
    thrown(NullPointerException)
  }

  def "included state is preserved by reference on typed documents"() {
    given:
    def included = IncludedResources.of(["x"], [[] as Set])

    expect:
    new ResourceDocument<>("dto", null, null, null, included).included().is(included)
    new ResourceCollectionDocument<>(["dto"], null, null, null, included).included().is(included)
  }

  def "root exposes exactly the four cohesive facets"() {
    expect:
    JsonApi.getMethods().collect { it.name }.toSet() == [
      "resources",
      "relationships",
      "documents",
      "patches"
    ] as Set
    JsonApi.getMethod("resources").returnType == JsonApiResources
    JsonApi.getMethod("relationships").returnType == JsonApiRelationships
    JsonApi.getMethod("documents").returnType == JsonApiDocuments
    JsonApi.getMethod("patches").returnType == JsonApiPatches
    JsonApi.isInterface()
    [
      JsonApiResources,
      JsonApiRelationships,
      JsonApiDocuments,
      JsonApiPatches
    ].every { it.isInterface() }
  }

  def "ordinary read and write overloads exist without leaking advanced seams"() {
    expect:
    JsonApiResources.getMethod("readOne", String, Class) != null
    JsonApiResources.getMethod("readOne", InputStream, Class) != null
    JsonApiResources.getMethod("readMany", String, Class) != null
    JsonApiResources.getMethod("readMany", InputStream, Class) != null
    JsonApiResources.getMethod("readOneDocument", String, Class) != null
    JsonApiResources.getMethod("readManyDocument", String, Class) != null
    JsonApiResources.getMethod("writeOne", Object, ResourceWriteOptions) != null
    JsonApiResources.getMethod("writeMany", Iterable, ResourceWriteOptions) != null
    JsonApiResources.getMethod("writeCreateDocument", Object, ResourceWriteOptions) != null
    JsonApiResources.getMethod("writeUpdateDocument", Object, EndpointIdentity, ResourceWriteOptions) != null
    JsonApiRelationships.getMethod("readToOne", String) != null
    JsonApiRelationships.getMethod("readToMany", String) != null
    JsonApiRelationships.getMethod("writeToOne", io.github.kazemek.jsonapi.core.model.ResourceIdentifier) != null
    JsonApiDocuments.getMethod("read", String, DocumentReadContext) != null
    JsonApiPatches.getMethod("readPatch", String, Class) != null
    JsonApiPatches.getMethod("readCommand", String, Class) != null
    JsonApiPatches.getMethod("bindPatch", io.github.kazemek.jsonapi.core.model.JsonApiDocument, Class) != null
    JsonApiPatches.getMethod("bindCommand", io.github.kazemek.jsonapi.core.model.JsonApiDocument, Class) != null
  }

  def "no neutral level-1 signature references Jackson implementation types"() {
    given:
    def apiTypes = [
      JsonApi,
      JsonApiResources,
      JsonApiRelationships,
      JsonApiDocuments,
      JsonApiPatches,
      ResourceWriteOptions,
      ResourceDocument,
      ResourceCollectionDocument
    ]

    when:
    def offending = []
    apiTypes.each { type ->
      (type.methods.toList() + type.declaredMethods.toList()).each { method ->
        ([method.returnType] + method.parameterTypes.toList()).each { param ->
          def name = param.name
          if (name.startsWith("tools.jackson.")
              || name.startsWith("com.fasterxml.")
              || name.contains(".jackson2.")
              || name.contains(".jackson3.")) {
            offending.add(type.simpleName + "#" + method.name + " -> " + name)
          }
        }
      }
    }

    then:
    offending.isEmpty()
  }
}
