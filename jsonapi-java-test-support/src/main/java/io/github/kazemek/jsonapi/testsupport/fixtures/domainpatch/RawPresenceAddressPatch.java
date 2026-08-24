package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Invalid nested shape: a raw {@code PatchPresence} member (no type argument). */
public record RawPresenceAddressPatch(PatchPresence street, PatchPresence<String> city) {}
