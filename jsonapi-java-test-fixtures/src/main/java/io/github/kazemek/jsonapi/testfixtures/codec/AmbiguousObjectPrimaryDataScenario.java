package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;

final class AmbiguousObjectPrimaryDataScenario {

  private AmbiguousObjectPrimaryDataScenario() {}

  public static AmbiguousPrimaryDataScenario scenario() {
    return new AmbiguousPrimaryDataScenario(
        "ambiguous-object-primary-data",
        "Object primary data decoding to either a resource or an identifier model",
        "documents/ambiguous-object-primary-data.json",
        JsonApiDocument.withData(
            new DocumentData.SingleResource(ResourceObject.of("articles", "1"))),
        JsonApiDocument.withData(
            new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1"))),
        ValidationContext.defaults());
  }
}
