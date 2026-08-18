package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Presence-aware nested PATCH shape (record / creator-bound) with wrapper-level
 * {@code @JsonDeserialize} on the {@code city} creator parameter, proving deserialization-side
 * customization on a creator parameter is detected and rejected on typed shape entry (ADR-014).
 */
public record CreatorCustomizedAddressPatch(
    PatchPresence<String> street,
    @JsonDeserialize(using = UpperCaseStringDeserializer.class) PatchPresence<String> city) {}
