package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import java.util.LinkedHashMap;
import java.util.Map;

final class MetaOnlyScenario {

  private MetaOnlyScenario() {}

  public static CodecScenario scenario() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("copyright", "Copyright 2026");
    return CodecScenario.of(
        "meta-only",
        "Absent data; meta-only document",
        "documents/meta-only.json",
        JsonApiDocument.withMeta(Meta.of(meta)),
        null,
        SchemaKind.RESPONSE);
  }
}
