package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import tools.jackson.databind.module.SimpleModule;

/**
 * Registers the minimal internal {@link PatchPresence} deserializer on the derived binder mapper
 * used by the typed PATCH DTO path. The caller's mapper is never mutated.
 */
public final class PatchPresenceModule extends SimpleModule {

  public PatchPresenceModule() {
    super("jsonapi-java-patch-presence");
    addDeserializer(PatchPresence.class, new PatchPresenceDeserializer());
  }
}
