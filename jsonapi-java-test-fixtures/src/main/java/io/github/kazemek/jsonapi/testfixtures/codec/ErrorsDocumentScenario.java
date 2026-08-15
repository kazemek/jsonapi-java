package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.ErrorSource;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Link;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class ErrorsDocumentScenario {

  private ErrorsDocumentScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Link> errorLinks = new LinkedHashMap<>();
    errorLinks.put("about", Models.stringLink("http://example.com/docs/errors/invalid"));
    var error =
        new ErrorObject(
            "1",
            Models.links(errorLinks),
            "422",
            "invalid",
            "Invalid Attribute",
            "Title is required",
            new ErrorSource("/data/attributes/title", null, null, Map.of()),
            null,
            Map.of());
    return CodecScenario.of(
        "errors-document",
        "Top-level errors with source and links",
        "documents/errors-document.json",
        JsonApiDocument.withErrors(List.of(error)),
        null,
        SchemaKind.RESPONSE);
  }
}
