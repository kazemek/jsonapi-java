package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SingleResourceScenario {

  private SingleResourceScenario() {}

  public static CodecScenario scenario() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("title", "JSON:API paints my bikeshed!");
    var article = Models.resource("articles", "1", Attributes.ofAttributes(attributes));
    return CodecScenario.of(
        "single-resource",
        "Single resource primary data",
        "documents/single-resource.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
