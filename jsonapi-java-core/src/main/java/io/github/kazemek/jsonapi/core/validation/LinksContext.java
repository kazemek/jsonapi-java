package io.github.kazemek.jsonapi.core.validation;

/** Context for validating links in different document locations. */
public enum LinksContext {
  TOP_LEVEL,
  RESOURCE,
  RELATIONSHIP,
  ERROR
}
