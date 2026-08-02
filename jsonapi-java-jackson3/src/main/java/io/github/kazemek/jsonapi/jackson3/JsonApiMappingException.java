package io.github.kazemek.jsonapi.jackson3;

import java.io.Serial;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when the domain-to-resource mapping layer encounters a structural or value problem.
 *
 * <p>Every exception carries a stable {@link MappingDiagnostic} code. Optional {@link
 * #resourceClass()} and {@link #propertyPath()} identify the failing type and logical property for
 * callers that want to surface mapping issues in monitoring or user-facing messages.
 */
public final class JsonApiMappingException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final MappingDiagnostic diagnostic;
  private final @Nullable Class<?> resourceClass;
  private final @Nullable String propertyPath;

  public JsonApiMappingException(
      MappingDiagnostic diagnostic,
      @Nullable Class<?> resourceClass,
      @Nullable String propertyPath) {
    this(diagnostic, resourceClass, propertyPath, diagnostic.name(), null);
  }

  public JsonApiMappingException(
      MappingDiagnostic diagnostic,
      @Nullable Class<?> resourceClass,
      @Nullable String propertyPath,
      String message) {
    this(diagnostic, resourceClass, propertyPath, message, null);
  }

  public JsonApiMappingException(
      MappingDiagnostic diagnostic,
      @Nullable Class<?> resourceClass,
      @Nullable String propertyPath,
      String message,
      @Nullable Throwable cause) {
    super(message, cause);
    this.diagnostic = Objects.requireNonNull(diagnostic, "diagnostic");
    this.resourceClass = resourceClass;
    this.propertyPath = propertyPath;
  }

  public MappingDiagnostic diagnostic() {
    return diagnostic;
  }

  public @Nullable Class<?> resourceClass() {
    return resourceClass;
  }

  public @Nullable String propertyPath() {
    return propertyPath;
  }
}
