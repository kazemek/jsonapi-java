package io.github.kazemek.jsonapi.testsupport.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import java.util.List;

/** Explicit catalog of shared dual-success ambiguous primary-data scenarios in manifest order. */
public final class AmbiguousPrimaryDataScenarios {

  private static final FixtureCatalog<AmbiguousPrimaryDataScenario> CATALOG =
      FixtureCatalog.of(
          "ambiguous-primary-data", List.of(ambiguousObject(), ambiguousEmptyArray()));

  private AmbiguousPrimaryDataScenarios() {}

  public static FixtureCatalog<AmbiguousPrimaryDataScenario> catalog() {
    return CATALOG;
  }

  private static AmbiguousPrimaryDataScenario ambiguousObject() {
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

  private static AmbiguousPrimaryDataScenario ambiguousEmptyArray() {
    return new AmbiguousPrimaryDataScenario(
        "ambiguous-empty-array-primary-data",
        "Empty-array primary data decoding to either a resource or an identifier model",
        "documents/ambiguous-empty-array-primary-data.json",
        JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of())),
        JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of())),
        ValidationContext.defaults());
  }
}
