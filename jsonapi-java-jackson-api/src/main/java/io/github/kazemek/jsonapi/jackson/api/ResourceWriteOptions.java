package io.github.kazemek.jsonapi.jackson.api;

import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import java.util.Objects;

/**
 * Ordinary resource-write options composing existing neutral semantics.
 *
 * <p>Carries the per-write document envelope (top-level links, meta, and JSON:API object) together
 * with the per-operation representation selection (include paths and sparse fieldsets) and its
 * governing representation policy. An absent per-write {@code jsonapi} member (a null component on
 * the envelope) is distinct from an explicit per-write {@link
 * io.github.kazemek.jsonapi.core.model.JsonApiObject}: explicit values override future
 * application-lifetime document defaults, while absent values leave those defaults in effect.
 *
 * <p>Use {@link #defaults()} for the documented ordinary behavior: no document-level members, no
 * inclusion or fieldsets, and the default representation policy.
 */
public record ResourceWriteOptions(
    DocumentEnvelope envelope, RepresentationSelection selection, RepresentationPolicy policy) {

  public ResourceWriteOptions {
    Objects.requireNonNull(envelope, "envelope");
    Objects.requireNonNull(selection, "selection");
    Objects.requireNonNull(policy, "policy");
  }

  /** Returns options with no envelope members, no selection, and the default policy. */
  public static ResourceWriteOptions defaults() {
    return new ResourceWriteOptions(
        new DocumentEnvelope(null, null, null),
        RepresentationSelection.none(),
        RepresentationPolicy.defaults());
  }

  /** Returns options with the given envelope and this selection and policy. */
  public ResourceWriteOptions withEnvelope(DocumentEnvelope envelope) {
    return new ResourceWriteOptions(envelope, selection, policy);
  }

  /** Returns options with the given selection and this envelope and policy. */
  public ResourceWriteOptions withSelection(RepresentationSelection selection) {
    return new ResourceWriteOptions(envelope, selection, policy);
  }

  /** Returns options with the given policy and this envelope and selection. */
  public ResourceWriteOptions withPolicy(RepresentationPolicy policy) {
    return new ResourceWriteOptions(envelope, selection, policy);
  }
}
