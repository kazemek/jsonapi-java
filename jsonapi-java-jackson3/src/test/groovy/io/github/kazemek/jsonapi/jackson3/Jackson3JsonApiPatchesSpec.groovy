package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticlePatch
import io.github.kazemek.jsonapi.fixtures.domainread.FlatArticle
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

import java.lang.reflect.Type

class Jackson3JsonApiPatchesSpec extends Specification {

  @Shared
  Jackson3JsonApi jsonApi = JsonApiJackson3.jsonApi(JsonMapper.builder().build())

  def "readPatch binds supplied members while omitted members stay omitted"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"New title"}}}'

    when:
    def patch = jsonApi.patches().readPatch(json, ArticlePatch)

    then:
    patch.id() == "1"
    patch.title() == PatchPresence.present("New title")
    patch.body().isOmitted()
    patch.author().isOmitted()
    patch.comments().isOmitted()
  }

  def "readPatch keeps explicit null distinct from omitted"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":null},"relationships":{"author":{"data":null}}}}'

    when:
    def patch = jsonApi.patches().readPatch(json, ArticlePatch)

    then:
    patch.title() == PatchPresence.present(null)
    patch.author() == PatchPresence.present(null)
    patch.body().isOmitted()
  }

  def "readCommand projects only supplied changes with resource identity"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"New title"},"relationships":{"author":{"data":{"type":"people","id":"p1"}}}}}'

    when:
    def command = jsonApi.patches().readCommand(json, FlatArticle)

    then:
    command.resourceType() == FlatArticle
    command.identity() == "1"
    command.changes().size() == 2
  }

  def "bindPatch and bindCommand reuse an already-validated document"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"New title"}}}'
    def context = DocumentReadContext.of(
        ValidationContext.defaults().withDocumentUsage(DocumentUsage.UPDATE_REQUEST),
        PrimaryDataKind.RESOURCE)
    def document = jsonApi.documents().read(json, context)

    when:
    def patch = jsonApi.patches().bindPatch(document, ArticlePatch)
    def command = jsonApi.patches().bindCommand(document, FlatArticle)

    then:
    patch == jsonApi.patches().readPatch(json, ArticlePatch)
    command == jsonApi.patches().readCommand(json, FlatArticle)
  }

  def "generic Type overloads mirror the Class paths"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"New title"}}}'
    Type dtoType = ArticlePatch
    Type resourceType = FlatArticle

    when:
    def patch = jsonApi.patches().readPatch(json, dtoType)
    def command = jsonApi.patches().readCommand(json, resourceType)

    then:
    patch == jsonApi.patches().readPatch(json, ArticlePatch)
    command == jsonApi.patches().readCommand(json, FlatArticle)
  }

  def "stream sources mirror string sources without closing caller streams"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"New title"}}}'

    when:
    def patch = jsonApi.patches().readPatch(new ByteArrayInputStream(json.bytes), ArticlePatch)

    then:
    patch == jsonApi.patches().readPatch(json, ArticlePatch)
  }
}
