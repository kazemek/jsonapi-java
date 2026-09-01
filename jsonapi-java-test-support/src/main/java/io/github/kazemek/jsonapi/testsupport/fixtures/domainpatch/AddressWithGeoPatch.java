package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Presence-aware PATCH shape with a deeper nested {@link GeoPatch} member (ADR-014). */
public record AddressWithGeoPatch(PatchPresence<String> street, PatchPresence<GeoPatch> geo) {}
