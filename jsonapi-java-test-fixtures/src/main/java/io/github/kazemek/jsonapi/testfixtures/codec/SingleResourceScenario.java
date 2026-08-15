package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.LinkedHashMap;
import java.util.Map;

final class SingleResourceScenario {

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
