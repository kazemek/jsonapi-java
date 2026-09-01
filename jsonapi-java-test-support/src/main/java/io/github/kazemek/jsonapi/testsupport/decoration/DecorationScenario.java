package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * One immutable decoration scenario: a stable id, a domain object supplier, a decorator registry,
 * optional selection/policy, and a discriminated {@link DecorationOutcome}.
 *
 * <p>Scenarios are Jackson-major-neutral; adapter suites supply the configured mapper and invoke
 * {@link DecorationVerifier}.
 */
public record DecorationScenario(
    String id,
    Supplier<Object> domainSupplier,
    ResourceDecoratorRegistry decorators,
    @Nullable RepresentationSelection selection,
    @Nullable RepresentationPolicy policy,
    DecorationOutcome outcome)
    implements Scenario {

  @SuppressWarnings("ConstantValue")
  public DecorationScenario {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("id must not be null or empty");
    }
    if (domainSupplier == null) {
      throw new IllegalArgumentException("domainSupplier must not be null");
    }
    if (decorators == null) {
      throw new IllegalArgumentException("decorators must not be null");
    }
    if (outcome == null) {
      throw new IllegalArgumentException("outcome must not be null");
    }
  }

  @Override
  public String id() {
    return id;
  }
}
