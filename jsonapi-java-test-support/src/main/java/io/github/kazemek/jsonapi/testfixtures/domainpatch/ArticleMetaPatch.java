package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Presence-aware nested PATCH shape for resource meta (ADR-014/015 recursion). */
public record ArticleMetaPatch(PatchPresence<String> source, PatchPresence<String> note) {}
