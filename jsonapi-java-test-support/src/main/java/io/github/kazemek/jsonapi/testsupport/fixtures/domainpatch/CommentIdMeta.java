package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import org.jspecify.annotations.Nullable;

/** Shared application-owned identifier-side meta for one to-many comments linkage element. */
public record CommentIdMeta(@Nullable Boolean pinned) {}
