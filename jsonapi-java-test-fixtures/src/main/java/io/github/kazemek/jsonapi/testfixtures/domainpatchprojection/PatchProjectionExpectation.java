package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.Objects;

/** Discriminated typed PATCH projection expectation. */
public sealed interface PatchProjectionExpectation
    permits PatchProjectionExpectation.Success, PatchProjectionExpectation.ProjectorFailure {

  /** Successful projection to the expected patch DTO instance. */
  record Success(Object expectedPatch) implements PatchProjectionExpectation {
    public Success {
      Objects.requireNonNull(expectedPatch, "expectedPatch");
    }
  }

  /** Projection failure after a successful patch bind. */
  record ProjectorFailure(MappingDiagnostic diagnostic, String propertyPath)
      implements PatchProjectionExpectation {
    public ProjectorFailure {
      Objects.requireNonNull(diagnostic, "diagnostic");
      Objects.requireNonNull(propertyPath, "propertyPath");
    }
  }

  static Success success(Object expectedPatch) {
    return new Success(expectedPatch);
  }

  static ProjectorFailure projectorFailure(MappingDiagnostic diagnostic, String propertyPath) {
    return new ProjectorFailure(diagnostic, propertyPath);
  }
}
