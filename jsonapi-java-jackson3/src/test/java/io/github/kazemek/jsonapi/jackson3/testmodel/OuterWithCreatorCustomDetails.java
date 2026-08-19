package io.github.kazemek.jsonapi.jackson3.testmodel;

import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Ordinary structured domain value type (record / creator-bound) with a bean-valued {@code details}
 * creator parameter carrying a property-scoped {@code @JsonDeserialize}. The customized {@code
 * details} member stays Atomic with the custom deserializer applied (ADR-014).
 */
public record OuterWithCreatorCustomDetails(
    @JsonDeserialize(using = CustomDetailsDeserializer.class) Details details) {}
