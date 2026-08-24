package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

/** Ordinary structured domain value type for multi-level low-level recursion (ADR-014). */
public record Geo(String lat, String lon) {}
