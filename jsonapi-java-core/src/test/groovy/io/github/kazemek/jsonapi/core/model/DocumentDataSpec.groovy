package io.github.kazemek.jsonapi.core.model

import spock.lang.Specification

class DocumentDataSpec extends Specification {

  def "absent data differs from explicit null data"() {
    given:
    def absent = new JsonApiDocument(null, null, Meta.of([count: 1]), null, null, null, [:])
    def explicitNull = JsonApiDocument.withData(DocumentData.NullData.INSTANCE)

    expect:
    !absent.hasDataMember()
    explicitNull.hasDataMember()
    explicitNull.data() instanceof DocumentData.NullData
    absent != explicitNull
  }

  def "supports single and collection primary data variants"() {
    given:
    def article = ResourceObject.of("articles", "1")
    def single = JsonApiDocument.withData(new DocumentData.SingleResource(article))
    def collection = JsonApiDocument.withData(
        new DocumentData.ResourceCollection([
          article,
          ResourceObject.of("articles", "2")
        ]))
    def singleId = JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1")))
    def idCollection = JsonApiDocument.withData(
        new DocumentData.IdentifierCollection([
          ResourceIdentifier.of("articles", "1"),
          ResourceIdentifier.of("articles", "2")
        ]))

    expect:
    single.data() instanceof DocumentData.SingleResource
    collection.data() instanceof DocumentData.ResourceCollection
    singleId.data() instanceof DocumentData.SingleIdentifier
    idCollection.data() instanceof DocumentData.IdentifierCollection
    ((DocumentData.ResourceCollection) collection.data()).resources().size() == 2
  }

  def "empty collection is not null or absent"() {
    when:
    def data = new DocumentData.IdentifierCollection([])

    then:
    data.identifiers().isEmpty()
    data != DocumentData.NullData.INSTANCE
  }

  def "single resource and identifier reject null payloads"() {
    when:
    new DocumentData.SingleResource(null)

    then:
    thrown(NullPointerException)

    when:
    new DocumentData.SingleIdentifier(null)

    then:
    thrown(NullPointerException)
  }
}
