package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/**
 * Invalid nested shape: a member declared directly as {@link PatchPresence.Present} rather than
 * {@code PatchPresence<T>}.
 */
public record DirectPresentAddressPatch(
    PatchPresence.Present<String> street, PatchPresence<String> city) {}
