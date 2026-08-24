package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;

/**
 * One immutable compound-inclusion scenario: a stable id, exactly one {@link CompoundWriteRequest},
 * and one discriminated {@link CompoundWriteExpectation}.
 */
public record CompoundWriteScenario(
    String id, CompoundWriteRequest request, CompoundWriteExpectation expectation)
    implements Scenario {

  public CompoundWriteScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(expectation, "expectation");
    boolean concurrentRequest = request instanceof CompoundWriteRequest.Concurrent;
    boolean concurrentExpectation =
        expectation instanceof CompoundWriteExpectation.ConcurrentIsolation;
    if (concurrentRequest != concurrentExpectation) {
      throw new IllegalArgumentException(
          "Concurrent request and ConcurrentIsolation expectation must be used together: " + id);
    }
  }
}
