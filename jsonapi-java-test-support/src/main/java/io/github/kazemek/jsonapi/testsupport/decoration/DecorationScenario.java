package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.mapping.ResourceDecoratorRegistry;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.function.Supplier;

/**
 * One immutable decoration scenario: a stable id, a domain object supplier, a decorator registry,
 * and the expected decorated {@link ResourceObject}.
 *
 * <p>Scenarios are Jackson-major-neutral; adapter suites supply the configured mapper and invoke
 * {@link DecorationVerifier}.
 */
public record DecorationScenario(
    String id,
    Supplier<Object> domainSupplier,
    ResourceDecoratorRegistry decorators,
    ResourceObject expected)
    implements Scenario {

  @Override
  public String id() {
    return id;
  }
}
