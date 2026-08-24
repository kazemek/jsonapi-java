package io.github.kazemek.jsonapi.testsupport.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenario;
import io.github.kazemek.jsonapi.testsupport.codec.Models;
import io.github.kazemek.jsonapi.testsupport.codec.SchemaDisagreement;
import io.github.kazemek.jsonapi.testsupport.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class StringAndObjectLinksScenario {

  private StringAndObjectLinksScenario() {}

  public static CodecScenario scenario() {
    String selfHref = "http://example.com/articles/1";
    Map<String, @Nullable Link> resourceLinks = new LinkedHashMap<>();
    resourceLinks.put("self", Models.stringLink(selfHref));
    var article = Models.resource("articles", "1", Models.links(resourceLinks));

    Map<String, Object> relatedMeta = new LinkedHashMap<>();
    relatedMeta.put("count", 1);
    var related =
        Models.objectLink(
            "http://example.com/articles/1/related",
            "related",
            "Related",
            "application/vnd.api+json",
            List.of("en"),
            Meta.of(relatedMeta));

    Map<String, @Nullable Link> topLinkEntries = new LinkedHashMap<>();
    topLinkEntries.put("self", Models.stringLink(selfHref));
    topLinkEntries.put("related", related);
    topLinkEntries.put("next", null);
    var topLinks = Models.links(topLinkEntries);

    return new CodecScenario(
        "string-and-object-links",
        "String link, object link, null link, canonical hreflang array",
        "documents/string-and-object-links.json",
        new JsonApiDocument(
            new DocumentData.ResourceCollection(List.of(article)),
            null,
            null,
            null,
            topLinks,
            null,
            Map.of()),
        ValidationContext.defaults(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "hreflang canonical list form; draft linkObject.hreflang only accepts a string",
            List.of(Map.of("keyword", "type", "path", "/links/related/hreflang"))),
        false,
        null,
        true);
  }
}
