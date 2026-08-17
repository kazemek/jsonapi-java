package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.testfixtures.Scenario;
import java.util.Objects;

/**
 * One typed PATCH projection scenario: JSON input, command mapping type, patch DTO type, and one
 * discriminated {@link PatchProjectionExpectation}.
 */
public record PatchProjectionScenario(
    String id,
    String documentJson,
    Class<?> commandTargetType,
    Class<?> patchTargetType,
    PatchProjectionExpectation expectation)
    implements Scenario {

  public PatchProjectionScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(documentJson, "documentJson");
    Objects.requireNonNull(commandTargetType, "commandTargetType");
    Objects.requireNonNull(patchTargetType, "patchTargetType");
    Objects.requireNonNull(expectation, "expectation");
  }

  @Override
  public String toString() {
    return id;
  }
}
