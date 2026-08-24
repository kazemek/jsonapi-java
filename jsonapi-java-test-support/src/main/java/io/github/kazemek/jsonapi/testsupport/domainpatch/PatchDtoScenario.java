package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.core.validation.EndpointIdentity;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One immutable direct typed PATCH DTO scenario: a stable id, one JSON document, a target PATCH DTO
 * class, optional expected endpoint identity, and one discriminated {@link PatchDtoExpectation}.
 */
public record PatchDtoScenario(
    String id,
    String documentJson,
    Class<?> targetType,
    @Nullable EndpointIdentity expectedEndpointIdentity,
    PatchDtoExpectation expectation)
    implements Scenario {

  public PatchDtoScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(documentJson, "documentJson");
    Objects.requireNonNull(targetType, "targetType");
    Objects.requireNonNull(expectation, "expectation");
  }

  @Override
  public String toString() {
    return id;
  }
}
