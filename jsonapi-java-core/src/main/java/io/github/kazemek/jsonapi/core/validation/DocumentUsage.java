package io.github.kazemek.jsonapi.core.validation;

/** Declares how a document is used for context-sensitive validation. */
public enum DocumentUsage {
  CREATE_REQUEST,
  UPDATE_REQUEST,
  RESPONSE_OR_OTHER
}
