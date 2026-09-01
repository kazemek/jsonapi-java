package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Presence-aware nested PATCH shape for resource meta (ADR-014/015 recursion). */
public record ArticleMetaPatch(PatchPresence<String> source, PatchPresence<String> note) {}
