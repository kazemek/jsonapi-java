package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Invalid nested shape: a member declared directly as {@link PatchPresence.Present} rather than
 * {@code PatchPresence<T>}.
 */
public record DirectPresentAddressPatch(
    PatchPresence.Present<String> street, PatchPresence<String> city) {}
