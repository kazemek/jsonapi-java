package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Nested presence-aware whole-meta shape whose member is renamed to a distinct wire name. */
public record RenamedMetaBeanPatch(
    @JsonProperty("w_source") PatchPresence<String> source, PatchPresence<String> note) {}
