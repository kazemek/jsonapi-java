package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract polymorphic value type used by the low-level atomic-conversion fixture, proving a
 * property-level {@code TypeDeserializer} is preserved through the containing {@code
 * SettableBeanProperty.deserialize} (ADR-014).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({@JsonSubTypes.Type(value = EmailContact.class, name = "email")})
public abstract class Contact {}
