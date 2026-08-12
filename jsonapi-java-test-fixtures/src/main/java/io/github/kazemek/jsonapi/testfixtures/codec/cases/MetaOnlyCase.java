package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MetaOnlyCase {

  private MetaOnlyCase() {}

  public static CodecFixture fixture() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("copyright", "Copyright 2026");
    return CodecFixture.of(
        "meta-only",
        "Absent data; meta-only document",
        "documents/meta-only.json",
        JsonApiDocument.withMeta(Meta.of(meta)),
        null,
        SchemaKind.RESPONSE);
  }
}
