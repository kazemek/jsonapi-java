package io.github.kazemek.jsonapi.jackson3.testmodel;

/**
 * Ordinary traversable structured domain value type used by the low-level custom-deserializer
 * boundary fixtures (ADR-014).
 */
public record Details(String name) {}
