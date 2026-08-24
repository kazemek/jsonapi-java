package io.github.kazemek.jsonapi.testsupport.domainwrite;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated scenario outcome: either a success carrying exactly one neutral core value — a
 * {@link ResourceObject} for {@link DomainWriteOperation#TO_RESOURCE} or a {@link JsonApiDocument}
 * for the document operations — or a failure carrying exactly one expected exception class.
 */
public sealed interface DomainWriteOutcome
    permits DomainWriteOutcome.Success, DomainWriteOutcome.Failure {

  /**
   * Successful outcome holding exactly one core value: the {@code resource} for {@code TO_RESOURCE}
   * scenarios, the {@code document} for document scenarios.
   */
  record Success(@Nullable ResourceObject resource, @Nullable JsonApiDocument document)
      implements DomainWriteOutcome {

    public Success {
      if ((resource == null) == (document == null)) {
        throw new IllegalArgumentException(
            "Success outcome requires exactly one of resource or document");
      }
    }
  }

  /** Failure outcome carrying the expected exception type of the adapter entry point. */
  record Failure(Class<? extends Throwable> exception) implements DomainWriteOutcome {}

  static Success resource(ResourceObject resource) {
    return new Success(resource, null);
  }

  static Success document(JsonApiDocument document) {
    return new Success(null, document);
  }

  static Failure failure(Class<? extends Throwable> exception) {
    return new Failure(exception);
  }
}
