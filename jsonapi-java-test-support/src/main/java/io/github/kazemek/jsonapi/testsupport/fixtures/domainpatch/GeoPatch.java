package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Deeper nested presence-aware PATCH shape proving multi-level typed recursion (ADR-014). */
public record GeoPatch(PatchPresence<String> lat, PatchPresence<String> lon) {}
