package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** Error object source pointer. */
public record ErrorSource(
    @Nullable String pointer,
    @Nullable String parameter,
    @Nullable String header,
    Map<String, @Nullable Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL =
      Set.of(JsonApiMembers.POINTER, JsonApiMembers.PARAMETER, JsonApiMembers.HEADER);

  public ErrorSource {
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers,
            "/errors/source",
            "Invalid error source member name: ",
            RESERVED_ADDITIONAL);
  }

  public static ErrorSource ofParameter(String parameter) {
    return new ErrorSource(null, parameter, null, Map.of());
  }
}
