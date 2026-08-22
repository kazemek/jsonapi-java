package io.github.kazemek.jsonapi.jackson3.internal;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.bean.UnwrappingBeanPropertyWriter;
import tools.jackson.databind.ser.impl.PropertySerializerMap;
import tools.jackson.databind.util.NameTransformer;

/**
 * A {@link BeanPropertyWriter} that applies its resolved writer state to a supplied property value.
 *
 * <p>The ordinary writer reads its accessor inside {@link #serializeAsProperty}. Property-scoped
 * JSON:API mapping has already read the accessor to establish the value to map, so reading it again
 * could make inclusion and the mapped value disagree. This adapter keeps the normal writer's
 * resolved suppression, serializer, null serializer, and type serializer state while replacing the
 * accessor read with the supplied value.
 */
final class RawValueBeanPropertyWriter extends BeanPropertyWriter {

  private final BeanPropertyWriter delegate;

  RawValueBeanPropertyWriter(BeanPropertyWriter base) {
    super(base);
    this.delegate = base;
  }

  @Override
  public BeanPropertyWriter rename(NameTransformer transformer) {
    return new RawValueBeanPropertyWriter(delegate.rename(transformer));
  }

  @Override
  public BeanPropertyWriter unwrappingWriter(NameTransformer unwrapper) {
    return new RawValueBeanPropertyWriter(delegate.unwrappingWriter(unwrapper));
  }

  @Override
  public boolean isUnwrapping() {
    return delegate.isUnwrapping();
  }

  @Override
  public void assignSerializer(ValueSerializer<Object> serializer) {
    delegate.assignSerializer(serializer);
    super.assignSerializer(delegate.getSerializer());
  }

  @Override
  public void assignNullSerializer(ValueSerializer<Object> nullSerializer) {
    delegate.assignNullSerializer(nullSerializer);
    super.assignNullSerializer(nullSerializer);
  }

  @Override
  public void assignTypeSerializer(TypeSerializer typeSerializer) {
    delegate.assignTypeSerializer(typeSerializer);
    super.assignTypeSerializer(typeSerializer);
  }

  @Override
  public void serializeAsProperty(
      Object bean, JsonGenerator generator, SerializationContext context) throws Exception {
    delegate.serializeAsProperty(bean, generator, context);
  }

  @Override
  public void serializeAsOmittedProperty(
      Object bean, JsonGenerator generator, SerializationContext context) throws Exception {
    delegate.serializeAsOmittedProperty(bean, generator, context);
  }

  @Override
  public void serializeAsElement(Object bean, JsonGenerator generator, SerializationContext context)
      throws Exception {
    delegate.serializeAsElement(bean, generator, context);
  }

  @Override
  public void serializeAsOmittedElement(
      Object bean, JsonGenerator generator, SerializationContext context) throws Exception {
    delegate.serializeAsOmittedElement(bean, generator, context);
  }

  @Override
  public void depositSchemaProperty(JsonObjectFormatVisitor visitor, SerializationContext context) {
    delegate.depositSchemaProperty(visitor, context);
  }

  void serializeAsRawProperty(
      Object bean, @Nullable Object value, JsonGenerator generator, SerializationContext context) {
    if (value == null) {
      if (delegate.isUnwrapping()) {
        return;
      }
      if (_suppressableValue != null && context.includeFilterSuppressNulls(_suppressableValue)) {
        return;
      }
      if (_nullSerializer != null) {
        generator.writeName(_name);
        _nullSerializer.serialize(null, generator, context);
      }
      return;
    }

    ValueSerializer<Object> serializer = delegate.getSerializer();
    if (serializer == null) {
      if (delegate instanceof UnwrappingBeanPropertyWriter unwrappingProperty) {
        serializer = unwrappingProperty.findUnwrappingSerializer(context);
      } else {
        Class<?> rawType = value.getClass();
        PropertySerializerMap serializers = _dynamicSerializers;
        serializer = serializers.serializerFor(rawType);
        if (serializer == null) {
          serializer = _findAndAddDynamic(serializers, rawType, context);
        }
      }
    }

    if (_suppressableValue != null) {
      if (MARKER_FOR_EMPTY == _suppressableValue) {
        if (serializer.isEmpty(context, value)) {
          return;
        }
      } else if (_suppressableValue.equals(value)) {
        return;
      }
    }

    if (value == bean && _handleSelfReference(bean, generator, context, serializer)) {
      return;
    }
    if (!serializer.isUnwrappingSerializer()) {
      generator.writeName(_name);
    }
    if (_typeSerializer == null) {
      serializer.serialize(value, generator, context);
    } else {
      serializer.serializeWithType(value, generator, context, _typeSerializer);
    }
  }
}
