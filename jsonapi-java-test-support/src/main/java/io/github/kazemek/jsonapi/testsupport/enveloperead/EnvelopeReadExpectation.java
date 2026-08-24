package io.github.kazemek.jsonapi.testsupport.enveloperead;

import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.jackson.DomainData;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated envelope-read expectation: complete bound envelope values, a mapping diagnostic
 * joined to the document pointer, or mutation-safety of reader-derived collections.
 */
public sealed interface EnvelopeReadExpectation
    permits EnvelopeReadExpectation.BoundEnvelope,
        EnvelopeReadExpectation.Failure,
        EnvelopeReadExpectation.MutationSafety {

  /**
   * Complete envelope members. Java {@code null} means the member is absent; {@link
   * DomainData.NullData} is explicit JSON {@code null}; a non-null empty {@link
   * IncludedExpectation} is present-empty {@code included}.
   */
  record BoundEnvelope(
      @Nullable DomainData data,
      @Nullable IncludedExpectation included,
      @Nullable List<ErrorObject> errors,
      @Nullable JsonApiObject jsonapi,
      @Nullable Links links,
      @Nullable Meta meta,
      Map<String, @Nullable Object> additionalMembers)
      implements EnvelopeReadExpectation {

    public BoundEnvelope {
      Objects.requireNonNull(additionalMembers, "additionalMembers");
      errors = errors == null ? null : List.copyOf(errors);
      additionalMembers = Collections.unmodifiableMap(new LinkedHashMap<>(additionalMembers));
    }
  }

  record Failure(
      MappingDiagnostic diagnostic, @Nullable String propertyPath, @Nullable Class<?> resourceClass)
      implements EnvelopeReadExpectation {
    public Failure {
      Objects.requireNonNull(diagnostic, "diagnostic");
    }
  }

  /**
   * Successful bind whose envelope collections must reject mutation with {@link
   * UnsupportedOperationException}.
   */
  record MutationSafety(
      BoundEnvelope bound, boolean additionalMembers, boolean errors, boolean includedResources)
      implements EnvelopeReadExpectation {
    public MutationSafety {
      Objects.requireNonNull(bound, "bound");
    }
  }

  static BoundEnvelope bound(
      @Nullable DomainData data,
      @Nullable IncludedExpectation included,
      @Nullable List<ErrorObject> errors,
      @Nullable JsonApiObject jsonapi,
      @Nullable Links links,
      @Nullable Meta meta,
      Map<String, @Nullable Object> additionalMembers) {
    return new BoundEnvelope(data, included, errors, jsonapi, links, meta, additionalMembers);
  }

  static Failure failure(MappingDiagnostic diagnostic, @Nullable String propertyPath) {
    return new Failure(diagnostic, propertyPath, null);
  }

  static Failure failure(
      MappingDiagnostic diagnostic,
      @Nullable String propertyPath,
      @Nullable Class<?> resourceClass) {
    return new Failure(diagnostic, propertyPath, resourceClass);
  }

  static MutationSafety mutationSafe(
      BoundEnvelope bound, boolean additionalMembers, boolean errors, boolean includedResources) {
    return new MutationSafety(bound, additionalMembers, errors, includedResources);
  }
}
