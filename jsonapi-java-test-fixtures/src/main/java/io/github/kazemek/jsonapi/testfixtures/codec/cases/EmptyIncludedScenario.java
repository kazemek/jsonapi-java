package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.List;
import java.util.Map;

public final class EmptyIncludedScenario {

  private EmptyIncludedScenario() {}

  public static CodecScenario scenario() {
    var article = Models.resource("articles", "1");
    return CodecScenario.of(
        "empty-included",
        "Present-empty included array with primary data",
        "documents/empty-included.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article), null, null, null, null, List.of(), Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
