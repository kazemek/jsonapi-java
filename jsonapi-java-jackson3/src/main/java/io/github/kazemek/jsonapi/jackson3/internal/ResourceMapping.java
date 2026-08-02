package io.github.kazemek.jsonapi.jackson3.internal;

import java.util.List;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

record ResourceMapping(
    String resourceType,
    @Nullable MappingProperty identifierProperty,
    List<MappingProperty> attributes,
    List<MappingProperty> relationships,
    JavaType domainType) {}
