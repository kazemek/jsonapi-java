package io.github.kazemek.jsonapi.jackson3.testmodel;

/** Generic structured value type used to prove whole-meta JavaType binding preservation. */
public record MetaBox<T>(T value) {}
