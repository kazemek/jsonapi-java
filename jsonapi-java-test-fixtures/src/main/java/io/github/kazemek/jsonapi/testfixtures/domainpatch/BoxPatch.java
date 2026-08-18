package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.List;

/**
 * Presence-aware PATCH shape with a generically-typed {@code List<Integer>} inner member, proving
 * the typed path preserves the full {@code JavaType} through atomic conversion (ADR-014): a raw
 * {@code List} class would lose the {@code Integer} element type during {@code convertValue}.
 */
public record BoxPatch(PatchPresence<List<Integer>> numbers) {}
