package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;
import java.util.Set;

/** A JSON:API resource object. */
public record ResourceObject(
    String type,
    String id,
    String lid,
    Attributes attributes,
    Relationships relationships,
    Links links,
    Meta meta,
    Map<String, Object> additionalMembers) {

  public ResourceObject {
    if (type == null) {
      LocalValidation.fail(
          ValidationRuleCode.MISSING_RESOURCE_TYPE, "/data/type", "Resource object requires type");
    }
    if (!MemberNames.isValid(type)) {
      LocalValidation.fail(
          ValidationRuleCode.INVALID_MEMBER_NAME, "/data/type", "Invalid resource type: " + type);
    }
    validateFieldNamespace(attributes, relationships);
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers,
            "/data",
            "Invalid resource member name: ",
            Set.of("type", "id", "lid", "attributes", "relationships", "links", "meta"));
  }

  public static ResourceObject ofType(String type) {
    return new ResourceObject(type, null, null, null, null, null, null, Map.of());
  }

  public static ResourceObject of(String type, String id) {
    return new ResourceObject(type, id, null, null, null, null, null, Map.of());
  }

  public boolean hasId() {
    return id != null;
  }

  public boolean hasLid() {
    return lid != null;
  }

  public ResourceIdentity identityKey() {
    if (hasId()) {
      return ResourceIdentity.ofId(type, id);
    }
    if (hasLid()) {
      return ResourceIdentity.ofLid(type, lid);
    }
    return null;
  }

  public ResourceIdentifier toIdentifier() {
    return new ResourceIdentifier(type, id, lid, meta, additionalMembers);
  }

  private static void validateFieldNamespace(Attributes attributes, Relationships relationships) {
    if (attributes == null || relationships == null) {
      return;
    }
    for (String name : attributes.attributes().keySet()) {
      if (relationships.relationships().containsKey(name)) {
        LocalValidation.fail(
            ValidationRuleCode.MEMBER_NAME_COLLISION,
            "/data",
            "Attribute and relationship name collision: " + name);
      }
    }
  }
}
