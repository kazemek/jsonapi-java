package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import java.util.List;

/**
 * Ordinary structured domain value type with a generically-typed {@code List<Integer>} member,
 * proving the low-level path preserves the full {@code JavaType} through atomic conversion
 * (ADR-014): a raw {@code List} class would lose the {@code Integer} element type during {@code
 * convertValue}.
 */
public record Box(List<Integer> numbers) {}
