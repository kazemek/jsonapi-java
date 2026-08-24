package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Map;
import java.util.Objects;

/**
 * Discriminated typed PATCH DTO expectation: a successful direct DTO bind, a reader-validation
 * failure, or a binder/declaration failure.
 */
public sealed interface PatchDtoExpectation
    permits PatchDtoExpectation.Success,
        PatchDtoExpectation.ReaderFailure,
        PatchDtoExpectation.BinderFailure {

  /**
   * Successful direct bind: typed identity and the exact expected {@link PatchPresence} value per
   * logical member name (every mapped member is asserted).
   */
  record Success(Object identity, Map<String, PatchPresence<?>> members)
      implements PatchDtoExpectation {
    public Success {
      Objects.requireNonNull(identity, "identity");
      Objects.requireNonNull(members, "members");
      members = Map.copyOf(members);
    }
  }

  /** Aggregate/document reader validation failure. */
  record ReaderFailure(ValidationRuleCode code, String jsonPointer) implements PatchDtoExpectation {
    public ReaderFailure {
      Objects.requireNonNull(code, "code");
      Objects.requireNonNull(jsonPointer, "jsonPointer");
    }
  }

  /** Direct PATCH DTO binder or declaration failure. */
  record BinderFailure(MappingDiagnostic diagnostic, String propertyPath)
      implements PatchDtoExpectation {
    public BinderFailure {
      Objects.requireNonNull(diagnostic, "diagnostic");
      Objects.requireNonNull(propertyPath, "propertyPath");
    }
  }

  static Success success(Object identity, Map<String, PatchPresence<?>> members) {
    return new Success(identity, members);
  }

  static ReaderFailure readerFailure(ValidationRuleCode code, String jsonPointer) {
    return new ReaderFailure(code, jsonPointer);
  }

  static BinderFailure binderFailure(MappingDiagnostic diagnostic, String propertyPath) {
    return new BinderFailure(diagnostic, propertyPath);
  }
}
