package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

/** Ordinary structured domain value type with a nested structured member (ADR-014). */
public record AddressWithGeo(String street, Geo geo) {}
