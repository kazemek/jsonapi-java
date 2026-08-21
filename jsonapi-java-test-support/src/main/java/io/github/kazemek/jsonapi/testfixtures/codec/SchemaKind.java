package io.github.kazemek.jsonapi.testfixtures.codec;

/**
 * Draft-schema kind a fixture is validated against. Kind names match the JSON:API 1.1 draft-PR
 * schema files pinned under the vendored classpath namespace {@code
 * jsonapi/schema/vendor/1.1-pr1603/}.
 */
public enum SchemaKind {
  RESPONSE,
  CREATE,
  UPDATE,
  UPDATE_RELATIONSHIP
}
