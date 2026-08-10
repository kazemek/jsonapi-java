package io.github.kazemek.jsonapi.testfixtures.codec

/**
 * Draft-schema kind a fixture is validated against. Kind names match the JSON:API 1.1 draft-PR
 * schema files pinned under {@code fixtures/jsonapi-schema/1.1-pr1603/}.
 */
enum SchemaKind {
  RESPONSE,
  CREATE,
  UPDATE,
  UPDATE_RELATIONSHIP
}
