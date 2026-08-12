package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NullDataScenario {

  private NullDataScenario() {}

  public static CodecScenario scenario() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("reason", "deleted");
    return CodecScenario.of(
        "null-data",
        "Explicit data null with meta",
        "documents/null-data.json",
        new JsonApiDocument(
            DocumentData.NullData.INSTANCE, null, Meta.of(meta), null, null, null, Map.of()),
        null,
        SchemaKind.RESPONSE);
  }
}
