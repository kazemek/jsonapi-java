package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import java.util.List;
import java.util.Objects;

/**
 * Discriminated PATCH expectation: a successful command, a reader-validation failure, or a binder
 * failure.
 */
public sealed interface PatchExpectation
    permits PatchExpectation.Success,
        PatchExpectation.ReaderFailure,
        PatchExpectation.BinderFailure {

  /** Successful bind: typed identity and exact change list. */
  record Success(Object identity, List<PatchChange> changes) implements PatchExpectation {
    public Success {
      Objects.requireNonNull(identity, "identity");
      Objects.requireNonNull(changes, "changes");
      changes = List.copyOf(changes);
    }
  }

  /** Aggregate/document reader validation failure. */
  record ReaderFailure(ValidationRuleCode code, String jsonPointer) implements PatchExpectation {
    public ReaderFailure {
      Objects.requireNonNull(code, "code");
      Objects.requireNonNull(jsonPointer, "jsonPointer");
    }
  }

  /** Presence-aware binder failure. */
  record BinderFailure(MappingDiagnostic diagnostic, String propertyPath)
      implements PatchExpectation {
    public BinderFailure {
      Objects.requireNonNull(diagnostic, "diagnostic");
      Objects.requireNonNull(propertyPath, "propertyPath");
    }
  }

  static Success success(Object identity, List<PatchChange> changes) {
    return new Success(identity, changes);
  }

  static ReaderFailure readerFailure(ValidationRuleCode code, String jsonPointer) {
    return new ReaderFailure(code, jsonPointer);
  }

  static BinderFailure binderFailure(MappingDiagnostic diagnostic, String propertyPath) {
    return new BinderFailure(diagnostic, propertyPath);
  }
}
