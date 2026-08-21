package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Presence-aware PATCH shape with a deeper nested {@link GeoPatch} member (ADR-014). */
public record AddressWithGeoPatch(PatchPresence<String> street, PatchPresence<GeoPatch> geo) {}
