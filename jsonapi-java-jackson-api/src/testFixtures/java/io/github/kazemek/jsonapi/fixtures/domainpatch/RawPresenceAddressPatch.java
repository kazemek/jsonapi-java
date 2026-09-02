package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Invalid nested shape: a raw {@code PatchPresence} member (no type argument). */
@SuppressWarnings({"rawtypes"})
public record RawPresenceAddressPatch(PatchPresence street, PatchPresence<String> city) {}
