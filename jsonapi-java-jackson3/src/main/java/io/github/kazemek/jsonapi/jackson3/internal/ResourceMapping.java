package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.MappingLocation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

record ResourceMapping(
    String resourceType,
    @Nullable MappingProperty identifierProperty,
    List<MappingProperty> attributes,
    List<MappingProperty> relationships,
    @Nullable MappingProperty resourceMeta,
    List<MappingProperty> relationshipMetaProperties,
    JavaType domainType) {

  /**
   * Maps every member's configured Jackson external name to its construction-translation start: the
   * member's resource-relative wire location plus declared type. Identifiers use {@code
   * identifierLocation} ({@code /id} or {@code /lid}, depending on what the resource supplied),
   * attributes start at {@code /attributes/<wire-name>}, relationships at {@code
   * /relationships/<wire-name>/data}, resource meta at {@code /meta}, and relationship meta at
   * {@code /relationships/<wire-name>/meta}. Shared by the flat binder and the typed PATCH DTO
   * binder so construction-failure translation cannot drift.
   */
  Map<String, StructuredValueBinder.ConstructionStart> constructionStartsByJacksonName(
      @Nullable MappingLocation identifierLocation) {
    Map<String, StructuredValueBinder.ConstructionStart> starts = new LinkedHashMap<>();
    if (identifierProperty != null && identifierLocation != null) {
      starts.put(
          identifierProperty.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              identifierLocation, identifierProperty.accessor().getType()));
    }
    for (MappingProperty property : attributes) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              MappingLocation.of("attributes", property.jsonapiName()),
              property.accessor().getType()));
    }
    for (MappingProperty property : relationships) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipLinkageSupport.relationshipLocation(property),
              property.accessor().getType()));
    }
    if (resourceMeta != null) {
      starts.put(
          resourceMeta.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipMetaSupport.resourceMetaLocation(), resourceMeta.accessor().getType()));
    }
    for (MappingProperty property : relationshipMetaProperties) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipMetaSupport.relationshipMetaLocation(property.jsonapiName()),
              property.accessor().getType()));
    }
    return starts;
  }
}
