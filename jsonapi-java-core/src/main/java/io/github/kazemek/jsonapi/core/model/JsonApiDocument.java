package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Top-level JSON:API document. */
public record JsonApiDocument(
    DocumentData data,
    List<ErrorObject> errors,
    Meta meta,
    JsonApiObject jsonapi,
    Links links,
    List<ResourceObject> included,
    Map<String, Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL =
      Set.of("data", "errors", "meta", "jsonapi", "links", "included");

  public JsonApiDocument {
    if (data != null && errors != null) {
      LocalValidation.fail(
          ValidationRuleCode.DATA_ERRORS_COEXIST, "", "data and errors must not coexist");
    }
    if (data == null && included != null) {
      LocalValidation.fail(
          ValidationRuleCode.INCLUDED_WITHOUT_DATA,
          "/included",
          "included must not be present without data");
    }
    if (errors != null) {
      errors = List.copyOf(errors);
    }
    if (included != null) {
      included = List.copyOf(included);
    }
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers, "", "Invalid document member name: ", RESERVED_ADDITIONAL);
    boolean hasTopLevelMember =
        data != null || errors != null || meta != null || hasExtensionMembers(additionalMembers);
    if (!hasTopLevelMember) {
      LocalValidation.fail(
          ValidationRuleCode.MISSING_TOP_LEVEL_MEMBER,
          "",
          "Document must contain at least one of data, errors, meta, or extension members");
    }
  }

  public static JsonApiDocument withData(DocumentData data) {
    return new JsonApiDocument(data, null, null, null, null, null, Map.of());
  }

  public static JsonApiDocument withErrors(List<ErrorObject> errors) {
    return new JsonApiDocument(null, errors, null, null, null, null, Map.of());
  }

  public static JsonApiDocument withMeta(Meta meta) {
    return new JsonApiDocument(null, null, meta, null, null, null, Map.of());
  }

  public boolean hasDataMember() {
    return data != null;
  }

  public boolean hasErrorsMember() {
    return errors != null;
  }

  public boolean hasIncludedMember() {
    return included != null;
  }

  private static boolean hasExtensionMembers(Map<String, ?> members) {
    if (members == null || members.isEmpty()) {
      return false;
    }
    for (String name : members.keySet()) {
      if (MemberNames.isExtensionMember(name)) {
        return true;
      }
    }
    return false;
  }
}
