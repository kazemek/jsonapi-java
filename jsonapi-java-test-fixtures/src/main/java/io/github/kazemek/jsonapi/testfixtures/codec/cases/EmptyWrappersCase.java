package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;

public final class EmptyWrappersCase {

  private EmptyWrappersCase() {}

  public static CodecFixture fixture() {
    var article =
        Models.resource(
            "articles",
            "1",
            Attributes.empty(),
            Relationships.empty(),
            Links.empty(),
            Meta.empty());
    return CodecFixture.of(
        "empty-wrappers",
        "Present-empty attributes, relationships, links, meta",
        "documents/empty-wrappers.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
