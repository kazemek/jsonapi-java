package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.internal.OpenJsonValues;
import io.github.kazemek.jsonapi.core.internal.OrderedMaps;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Flat relationships wrapper separating semantic members from pass-through members. */
public final class Relationships {

  private static final String PATH = "/relationships";
  private static final Set<String> RESERVED = Set.of("type", "id");

  private final Map<String, Relationship> members;
  private final Map<String, Object> additionalMembers;

  private Relationships(Map<String, Relationship> members, Map<String, Object> additionalMembers) {
    this.members = members;
    this.additionalMembers = additionalMembers;
  }

  public static Relationships empty() {
    return new Relationships(Map.of(), Map.of());
  }

  public static Relationships of(
      Map<String, Relationship> relationships, Map<String, ?> additionalMembers) {
    Map<String, Relationship> relCopy = OrderedMaps.copyOfNullableValues(relationships);
    for (Map.Entry<String, Relationship> entry : relCopy.entrySet()) {
      String name = entry.getKey();
      validateRelationshipName(name, PATH + "/" + name, false);
      if (entry.getValue() == null) {
        LocalValidation.fail(
            ValidationRuleCode.NULL_RELATIONSHIP_VALUE,
            PATH + "/" + name,
            "Relationship value must not be null: " + name);
      }
    }
    Map<String, Object> additionalCopy = copyAdditionalMembers(additionalMembers);
    OrderedMaps.requireNoCollisions(
        relCopy, castRelationships(additionalCopy), "relationships", PATH);
    return new Relationships(relCopy, additionalCopy);
  }

  public static Relationships ofRelationships(Map<String, Relationship> relationships) {
    return of(relationships, Map.of());
  }

  public Map<String, Relationship> relationships() {
    return members;
  }

  public Map<String, Object> additionalMembers() {
    return additionalMembers;
  }

  public boolean isEmpty() {
    return members.isEmpty() && additionalMembers.isEmpty();
  }

  public Map<String, Object> flatten() {
    Map<String, Object> flat = new LinkedHashMap<>();
    flat.putAll(members);
    flat.putAll(additionalMembers);
    return OrderedMaps.copyOfNullableValues(flat);
  }

  private static void validateRelationshipName(String name, String path, boolean allowPassThrough) {
    if (MemberNames.isAtMember(name)) {
      if (!allowPassThrough || !MemberNames.isValid(name)) {
        LocalValidation.fail(
            allowPassThrough
                ? ValidationRuleCode.INVALID_MEMBER_NAME
                : ValidationRuleCode.RESERVED_FIELD_NAME,
            path,
            allowPassThrough
                ? "Invalid relationship member name: " + name
                : "Relationship names cannot start with @: " + name);
      }
      return;
    }
    if (MemberNames.isExtensionMember(name)) {
      if (!allowPassThrough) {
        LocalValidation.fail(
            ValidationRuleCode.RESERVED_FIELD_NAME,
            path,
            "Extension members must use additionalMembers: " + name);
      }
      if (!MemberNames.isValid(name)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_MEMBER_NAME,
            path,
            "Invalid relationship member name: " + name);
      }
      return;
    }
    if (!MemberNames.isValid(name)) {
      LocalValidation.fail(
          ValidationRuleCode.INVALID_MEMBER_NAME, path, "Invalid relationship name: " + name);
    }
    if (RESERVED.contains(name)) {
      LocalValidation.fail(
          ValidationRuleCode.RESERVED_FIELD_NAME, path, "Reserved relationship name: " + name);
    }
  }

  private static Map<String, Object> copyAdditionalMembers(Map<String, ?> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<String, ?> entry : source.entrySet()) {
      String name = entry.getKey();
      validateRelationshipName(name, PATH + "/" + name, true);
      copy.put(name, OpenJsonValues.copy(entry.getValue(), PATH + "/" + name));
    }
    return OrderedMaps.copyOfNullableValues(copy);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Relationship> castRelationships(Map<String, Object> map) {
    return (Map) map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Relationships that)) {
      return false;
    }
    return members.equals(that.members) && additionalMembers.equals(that.additionalMembers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(members, additionalMembers);
  }
}
