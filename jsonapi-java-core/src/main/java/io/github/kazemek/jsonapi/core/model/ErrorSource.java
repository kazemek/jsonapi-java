package io.github.kazemek.jsonapi.core.model;

/** Error object source pointer. */
public record ErrorSource(String pointer, String parameter, String header) {}
