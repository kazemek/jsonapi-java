package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Optional;

/** Presence-aware PATCH shape with a nested {@code PatchPresence<Optional<String>>} member. */
public record AddressWithOptionalCityPatch(
    PatchPresence<String> street, PatchPresence<Optional<String>> city) {}
