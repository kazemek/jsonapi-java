package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

/**
 * Concrete POJO carrying root {@code @JsonTypeInfo}; its root deserializer is wrapped by a {@code
 * TypeDeserializer} (decorated), yet it is still an object-shaped whole-meta target (ADR-015).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({@JsonSubTypes.Type(value = ConcreteTypedMeta.class, name = "concrete")})
public class ConcreteTypedMeta {

  private String value;

  public ConcreteTypedMeta() {}

  public ConcreteTypedMeta(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ConcreteTypedMeta that)) {
      return false;
    }
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
