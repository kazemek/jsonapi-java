package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonApiObjectScenario {

  private JsonApiObjectScenario() {}

  public static CodecScenario scenario() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("impl", "jsonapi-java");
    var jsonapi =
        new JsonApiObject(
            "1.1",
            List.of("https://jsonapi.org/ext/atomic"),
            List.of("https://example.com/profiles/flex"),
            Meta.of(meta),
            Map.of());
    return new CodecScenario(
        "jsonapi-object",
        "jsonapi version, ext, profile, and meta",
        "documents/jsonapi-object.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
            null,
            null,
            jsonapi,
            null,
            null,
            Map.of()),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        null,
        false,
        null,
        false);
  }
}
