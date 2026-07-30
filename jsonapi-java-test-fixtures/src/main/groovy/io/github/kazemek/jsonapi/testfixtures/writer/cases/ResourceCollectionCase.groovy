package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class ResourceCollectionCase {
  private ResourceCollectionCase() {}

  static WriterFixture fixture() {
    def first = Models.resource('articles', '1', attributes: Attributes.ofAttributes(title: 'First'))
    def second = Models.resource('articles', '2', attributes: Attributes.ofAttributes(title: 'Second'))
    return WriterFixture.of(
        id: 'resource-collection',
        notes: 'Resource collection primary data',
        expectedPath: 'documents/resource-collection.json',
        document: JsonApiDocument.withData(new DocumentData.ResourceCollection([first, second])))
  }
}
