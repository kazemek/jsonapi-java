package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NullDataCase {

  private NullDataCase() {}

  public static CodecFixture fixture() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("reason", "deleted");
    return CodecFixture.of(
        "null-data",
        "Explicit data null with meta",
        "documents/null-data.json",
        new JsonApiDocument(
            DocumentData.NullData.INSTANCE, null, Meta.of(meta), null, null, null, Map.of()),
        null,
        SchemaKind.RESPONSE);
  }
}
