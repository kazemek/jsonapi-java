package io.github.kazemek.jsonapi.jackson3.internal;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.introspect.BeanPropertyDefinition;

/**
 * Direction-neutral view of one JSON:API-mapped Jackson property.
 *
 * <p>The write mapping and the flat-read mapping use different Jackson property authorities. This
 * view keeps their shared JSON:API role and wire-name metadata together without making a
 * serialization accessor proof of deserialization bindability.
 */
interface MappingPropertyView {

  BeanPropertyDefinition definition();

  String logicalName();

  String jsonapiName();

  PropertyRole role();

  JavaType type();
}
