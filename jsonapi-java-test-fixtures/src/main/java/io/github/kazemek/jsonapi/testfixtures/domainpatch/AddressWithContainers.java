package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import java.util.Map;
import java.util.Set;

/**
 * Ordinary structured domain value type with {@code Set}/{@code array}/{@code Map} members, proving
 * the low-level path treats each as one atomic replacement boundary rather than recursing into
 * elements or map keys (ADR-014).
 */
public record AddressWithContainers(
    String street, Set<String> aliases, String[] initials, Map<String, Integer> scores) {}
