package io.github.kazemek.jsonapi.jackson3.internal

import java.math.BigDecimal
import spock.lang.Specification
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.json.JsonMapper

class PropertyScopedValueConverterSpec extends Specification {

  def "property writes fall back when the containing bean has no matching writer"() {
    given:
    def mapper = JsonMapper.builder().build()
    def converter = new PropertyScopedValueConverter(mapper)

    expect:
    converter.serialize(
        mapper.constructType(WriteBean), "missing", new WriteBean("value"), "raw", "fallback")
        .value() == "fallback"
  }

  def "property writes return null when the assigned null serializer emits no value"() {
    given:
    def mapper = JsonMapper.builder().build()
    def converter = new PropertyScopedValueConverter(mapper)

    when:
    def result = converter.serialize(
        mapper.constructType(EmptyNullBean), "value", new EmptyNullBean(null), null, null)

    then:
    !result.emitted()
    result.value() == null
  }

  def "property writes preserve configured BigDecimal parsing for numeric values"() {
    given:
    def mapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .build()
    def converter = new PropertyScopedValueConverter(mapper)

    when:
    def value = converter.serialize(
        mapper.constructType(DecimalBean), "amount", new DecimalBean(new BigDecimal("1.50")),
        new BigDecimal("1.50"), null).value()

    then:
    value == new BigDecimal("1.50")
    value.class == BigDecimal
  }

  static class WriteBean {
    String value

    WriteBean(String value) {
      this.value = value
    }
  }

  static class EmptyNullBean {
    @JsonSerialize(nullsUsing = EmptyNullSerializer)
    String value

    EmptyNullBean(String value) {
      this.value = value
    }
  }

  static class DecimalBean {
    @JsonSerialize(using = DecimalSerializer)
    BigDecimal amount

    DecimalBean(BigDecimal amount) {
      this.amount = amount
    }
  }

  static class EmptyNullSerializer extends ValueSerializer<Object> {
    @Override
    void serialize(Object value, JsonGenerator generator, SerializationContext context) {}
  }

  static class DecimalSerializer extends ValueSerializer<BigDecimal> {
    @Override
    void serialize(BigDecimal value, JsonGenerator generator, SerializationContext context) {
      generator.writeNumber(value)
    }
  }
}
