package io.github.kazemek.jsonapi.fixtures.enveloperead;

/** Registry-rejection fixture: a plain record with no {@code @JsonApiResource} annotation. */
@SuppressWarnings("java:S2094") // intentional empty type-token for registry rejection
public record UnannotatedBindingTarget() {}
