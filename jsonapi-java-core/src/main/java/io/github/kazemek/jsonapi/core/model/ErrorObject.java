package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;
import java.util.Set;

/** JSON:API error object. */
public record ErrorObject(
    String id,
    Links links,
    String status,
    String code,
    String title,
    String detail,
    ErrorSource source,
    Meta meta,
    Map<String, Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL =
      Set.of("id", "links", "status", "code", "title", "detail", "source", "meta");

  public ErrorObject {
    boolean hasStandardMember =
        id != null
            || links != null
            || status != null
            || code != null
            || title != null
            || detail != null
            || source != null
            || meta != null;
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers, "/errors", "Invalid error member name: ", RESERVED_ADDITIONAL);
    if (!hasStandardMember) {
      LocalValidation.fail(
          ValidationRuleCode.MISSING_ERROR_MEMBER,
          "/errors",
          "Error object must contain at least one standard member");
    }
  }

  public static ErrorObject ofTitle(String title) {
    return new ErrorObject(null, null, null, null, title, null, null, null, Map.of());
  }
}
