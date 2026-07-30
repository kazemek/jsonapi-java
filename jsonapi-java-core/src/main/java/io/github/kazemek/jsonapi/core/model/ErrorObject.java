package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** JSON:API error object. */
public record ErrorObject(
    @Nullable String id,
    @Nullable Links links,
    @Nullable String status,
    @Nullable String code,
    @Nullable String title,
    @Nullable String detail,
    @Nullable ErrorSource source,
    @Nullable Meta meta,
    Map<String, @Nullable Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL =
      Set.of(
          JsonApiMembers.ID,
          JsonApiMembers.LINKS,
          JsonApiMembers.STATUS,
          JsonApiMembers.CODE,
          JsonApiMembers.TITLE,
          JsonApiMembers.DETAIL,
          JsonApiMembers.SOURCE,
          JsonApiMembers.META);

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
