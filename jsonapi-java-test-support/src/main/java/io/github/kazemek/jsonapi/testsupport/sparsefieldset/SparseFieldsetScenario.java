package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;

/**
 * One immutable sparse-fieldset scenario: a stable id, exactly one {@link SparseFieldsetOperation},
 * exactly one {@link SparseFieldsetRequest}, and one discriminated {@link
 * SparseFieldsetExpectation}.
 */
public record SparseFieldsetScenario(
    String id,
    SparseFieldsetOperation operation,
    SparseFieldsetRequest request,
    SparseFieldsetExpectation expectation)
    implements Scenario {

  public SparseFieldsetScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(expectation, "expectation");
    boolean concurrentRequest = request instanceof SparseFieldsetRequest.Concurrent;
    boolean concurrentExpectation =
        expectation instanceof SparseFieldsetExpectation.ConcurrentIsolation;
    if (concurrentRequest != concurrentExpectation) {
      throw new IllegalArgumentException(
          "Concurrent request and ConcurrentIsolation expectation must be used together: " + id);
    }
    boolean identityRequest = request instanceof SparseFieldsetRequest.IdentityPreservation;
    boolean identityExpectation =
        expectation instanceof SparseFieldsetExpectation.IdentityPreservation;
    if (identityRequest != identityExpectation) {
      throw new IllegalArgumentException(
          "IdentityPreservation request and expectation must be used together: " + id);
    }
    boolean collectionOperation =
        operation == SparseFieldsetOperation.TO_RESOURCE_COLLECTION
            || operation == SparseFieldsetOperation.TO_MAPPED_RESOURCE_COLLECTION;
    boolean collectionRequest = request instanceof SparseFieldsetRequest.Collection;
    if (!concurrentRequest && !identityRequest && collectionOperation != collectionRequest) {
      throw new IllegalArgumentException(
          "Collection operations require a Collection request: " + id);
    }
    boolean mappedOperation =
        operation == SparseFieldsetOperation.TO_MAPPED_DOCUMENT
            || operation == SparseFieldsetOperation.TO_MAPPED_RESOURCE_COLLECTION;
    if (expectation instanceof SparseFieldsetExpectation.UnmappedSuccess && mappedOperation) {
      throw new IllegalArgumentException(
          "UnmappedSuccess is only valid for three-argument document operations: " + id);
    }
    if (expectation instanceof SparseFieldsetExpectation.MappedSuccess && !mappedOperation) {
      throw new IllegalArgumentException(
          "MappedSuccess is only valid for MappedDocument operations: " + id);
    }
    if ((concurrentRequest || identityRequest)
        && operation != SparseFieldsetOperation.TO_MAPPED_DOCUMENT) {
      throw new IllegalArgumentException(
          "Concurrent and identity-preservation scenarios use TO_MAPPED_DOCUMENT: " + id);
    }
  }
}
