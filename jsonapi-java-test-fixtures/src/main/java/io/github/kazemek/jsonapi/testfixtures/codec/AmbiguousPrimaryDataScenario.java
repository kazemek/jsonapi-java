package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.Scenario;
import java.util.Objects;

/**
 * One shared dual-success primary-data case: a wire document whose decoded model depends on the
 * explicit {@link PrimaryDataKind}, plus the expected model under both kinds. Such cases must never
 * be classified as failure fixtures.
 */
public record AmbiguousPrimaryDataScenario(
    String id,
    String notes,
    String expectedPath,
    JsonApiDocument resourceDocument,
    JsonApiDocument identifierDocument,
    ValidationContext context)
    implements Scenario {

  public AmbiguousPrimaryDataScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(notes, "notes");
    Objects.requireNonNull(expectedPath, "expectedPath");
    Objects.requireNonNull(resourceDocument, "resourceDocument");
    Objects.requireNonNull(identifierDocument, "identifierDocument");
    Objects.requireNonNull(context, "context");
  }

  public JsonApiDocument expectedFor(PrimaryDataKind kind) {
    return kind == PrimaryDataKind.RESOURCE ? resourceDocument : identifierDocument;
  }

  @Override
  public String toString() {
    return id;
  }
}
