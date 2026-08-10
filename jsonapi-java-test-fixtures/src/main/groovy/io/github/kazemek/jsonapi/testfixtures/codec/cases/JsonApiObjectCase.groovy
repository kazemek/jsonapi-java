package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class JsonApiObjectCase {
  private JsonApiObjectCase() {}

  static CodecFixture fixture() {
    def jsonapi = new JsonApiObject(
        '1.1',
        [
          'https://jsonapi.org/ext/atomic'
        ],
        [
          'https://example.com/profiles/flex'
        ],
        Meta.of(impl: 'jsonapi-java'),
        [:])
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'jsonapi-object',
        notes: 'jsonapi version, ext, profile, and meta',
        expectedPath: 'documents/jsonapi-object.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of('articles', '1')),
        null,
        null,
        jsonapi,
        null,
        null,
        [:]),
        context: Models.extContext())
  }
}
