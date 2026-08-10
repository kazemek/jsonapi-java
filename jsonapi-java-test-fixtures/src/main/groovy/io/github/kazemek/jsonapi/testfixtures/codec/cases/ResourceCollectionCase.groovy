package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class ResourceCollectionCase {
  private ResourceCollectionCase() {}

  static CodecFixture fixture() {
    def first = Models.resource('articles', '1', attributes: Attributes.ofAttributes(title: 'First'))
    def second = Models.resource('articles', '2', attributes: Attributes.ofAttributes(title: 'Second'))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'resource-collection',
        notes: 'Resource collection primary data',
        expectedPath: 'documents/resource-collection.json',
        document: JsonApiDocument.withData(new DocumentData.ResourceCollection([first, second])))
  }
}
