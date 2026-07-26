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

/** Flat attributes wrapper separating semantic members from pass-through members. */
public final class Attributes {

  private static final String PATH = "/attributes";
  private static final Set<String> RESERVED = Set.of("type", "id");

  private final Map<String, Object> members;
  private final Map<String, Object> additionalMembers;

  private Attributes(Map<String, Object> members, Map<String, Object> additionalMembers) {
    this.members = members;
    this.additionalMembers = additionalMembers;
  }

  public static Attributes empty() {
    return new Attributes(Map.of(), Map.of());
  }

  public static Attributes of(Map<String, ?> attributes, Map<String, ?> additionalMembers) {
    Map<String, Object> attrCopy = copyMembers(attributes, false);
    Map<String, Object> additionalCopy = copyMembers(additionalMembers, true);
    OrderedMaps.requireNoCollisions(attrCopy, additionalCopy, "attributes", PATH);
    return new Attributes(attrCopy, additionalCopy);
  }

  public static Attributes ofAttributes(Map<String, ?> attributes) {
    return of(attributes, Map.of());
  }

  public Map<String, Object> attributes() {
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

  private static Map<String, Object> copyMembers(Map<String, ?> source, boolean allowPassThrough) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<String, ?> entry : source.entrySet()) {
      String name = entry.getKey();
      validateAttributeName(name, allowPassThrough);
      copy.put(name, OpenJsonValues.copy(entry.getValue(), PATH + "/" + name));
    }
    return OrderedMaps.copyOfNullableValues(copy);
  }

  private static void validateAttributeName(String name, boolean allowPassThrough) {
    if (MemberNames.isAtMember(name)) {
      if (!allowPassThrough || !MemberNames.isValid(name)) {
        LocalValidation.fail(
            allowPassThrough
                ? ValidationRuleCode.INVALID_MEMBER_NAME
                : ValidationRuleCode.RESERVED_FIELD_NAME,
            PATH + "/" + name,
            allowPassThrough
                ? "Invalid attribute member name: " + name
                : "Attribute names cannot start with @: " + name);
      }
      return;
    }
    if (MemberNames.isExtensionMember(name)) {
      if (!allowPassThrough) {
        LocalValidation.fail(
            ValidationRuleCode.RESERVED_FIELD_NAME,
            PATH + "/" + name,
            "Extension members must use additionalMembers: " + name);
      }
      if (!MemberNames.isValid(name)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_MEMBER_NAME,
            PATH + "/" + name,
            "Invalid attribute member name: " + name);
      }
      return;
    }
    if (!MemberNames.isValid(name)) {
      LocalValidation.fail(
          ValidationRuleCode.INVALID_MEMBER_NAME,
          PATH + "/" + name,
          "Invalid attribute name: " + name);
    }
    if (RESERVED.contains(name)) {
      LocalValidation.fail(
          ValidationRuleCode.RESERVED_FIELD_NAME,
          PATH + "/" + name,
          "Reserved attribute name: " + name);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attributes that)) {
      return false;
    }
    return members.equals(that.members) && additionalMembers.equals(that.additionalMembers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(members, additionalMembers);
  }
}
