package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.testfixtures.Scenario;
import java.util.Objects;

/**
 * One immutable flat resource-to-DTO binding scenario: a stable id, exactly one {@link
 * DomainReadInput}, a target DTO class, a {@link ConverterBehavior} discriminator, and one
 * discriminated {@link DomainReadExpectation}.
 */
public record DomainReadScenario(
    String id,
    DomainReadInput input,
    Class<?> targetType,
    ConverterBehavior converterBehavior,
    DomainReadExpectation expectation)
    implements Scenario {

  public DomainReadScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(input, "input");
    Objects.requireNonNull(targetType, "targetType");
    Objects.requireNonNull(converterBehavior, "converterBehavior");
    Objects.requireNonNull(expectation, "expectation");
  }
}
