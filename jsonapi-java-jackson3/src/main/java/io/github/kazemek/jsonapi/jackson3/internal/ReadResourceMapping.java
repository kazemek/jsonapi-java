package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.MappingLocation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * JSON:API role and wire metadata for ordinary flat reads.
 *
 * <p>Unlike {@link ResourceMapping}, this mapping is resolved from Jackson's deserialization
 * introspection and records whether each mapped logical property has an effective deserialization
 * target. It intentionally does not replace or weaken the serialization-oriented write mapping.
 */
record ReadResourceMapping(
    String resourceType,
    @Nullable ReadMappingProperty identifierProperty,
    List<ReadMappingProperty> attributes,
    List<ReadMappingProperty> relationships,
    @Nullable ReadMappingProperty resourceMeta,
    List<ReadMappingProperty> relationshipMetaProperties,
    JavaType domainType) {

  /**
   * Maps logical Jackson property names to resource-relative wire locations for
   * construction-failure translation. The declared type remains available for serialization-only
   * properties so a missing member never creates a synthetic input value merely by being present in
   * the mapping.
   */
  Map<String, StructuredValueBinder.ConstructionStart> constructionStartsByLogicalName(
      @Nullable MappingLocation identifierLocation) {
    Map<String, StructuredValueBinder.ConstructionStart> starts = new LinkedHashMap<>();
    if (identifierProperty != null && identifierLocation != null) {
      starts.put(
          identifierProperty.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              identifierLocation, identifierProperty.type()));
    }
    for (ReadMappingProperty property : attributes) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              MappingLocation.of("attributes", property.jsonapiName()), property.type()));
    }
    for (ReadMappingProperty property : relationships) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipLinkageSupport.relationshipLocation(property), property.type()));
    }
    if (resourceMeta != null) {
      starts.put(
          resourceMeta.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipMetaSupport.resourceMetaLocation(), resourceMeta.type()));
    }
    for (ReadMappingProperty property : relationshipMetaProperties) {
      starts.put(
          property.jacksonName(),
          new StructuredValueBinder.ConstructionStart(
              RelationshipMetaSupport.relationshipMetaLocation(property.jsonapiName()),
              property.type()));
    }
    return starts;
  }
}
