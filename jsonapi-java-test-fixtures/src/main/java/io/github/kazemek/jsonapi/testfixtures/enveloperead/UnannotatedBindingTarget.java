package io.github.kazemek.jsonapi.testfixtures.enveloperead;

/** Registry-rejection fixture: a plain record with no {@code @JsonApiResource} annotation. */
public record UnannotatedBindingTarget() {

  /** Marker so this fixture type is not an empty record. */
  public boolean missingResourceAnnotation() {
    return true;
  }
}
