package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Typed scenario input: a single nullable domain value or a resource collection.
 *
 * <p>Suppliers defer input construction until the adapter consumer invokes the scenario, so fresh
 * domain instances are mapped on each run.
 */
public sealed interface DomainWriteInput
    permits DomainWriteInput.SingleInput, DomainWriteInput.CollectionInput {

  record SingleInput(Supplier<@Nullable Object> supplier) implements DomainWriteInput {}

  record CollectionInput(Supplier<Iterable<?>> supplier) implements DomainWriteInput {}
}
