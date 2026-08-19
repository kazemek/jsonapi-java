package io.github.kazemek.jsonapi.jackson3.internal

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.StructuredMember
import io.github.kazemek.jsonapi.jackson.StructuredMemberState
import io.github.kazemek.jsonapi.jackson.StructuredPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.Address
import io.github.kazemek.jsonapi.testfixtures.domainpatch.AddressPatch
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

/**
 * Drives the {@link StructuredValueBinder} engine directly from non-attribute starting pointers
 * (e.g. a structured {@code meta} location) with no {@code ResourceMapping} / {@code MappingProperty}
 * / {@code @JsonApiAttribute} / {@code AttributeChange} in scope, proving the KAZ-77 reuse boundary
 * (ADR-014): the engine is location-neutral and outer-state-policy-free.
 */
class StructuredValueBinderSpec extends Specification {

  private static final String META = "/meta"

  private static JsonMapper mapper() {
    JsonMapper.builder().addModule(new PatchPresenceModule()).build()
  }

  def "typed engine binds a presence-aware shape from a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(mapper())
    def target = mapper().typeFactory.constructParametricType(PatchPresence, AddressPatch)

    when:
    def value = binder.typedMemberValue([street: "S"], target, META, AddressPatch)

    then:
    value instanceof Map
    def map = (Map) value
    map.keySet() == ["street", "city"] as Set
    map.street instanceof PresenceMarker
    map.street.present()
    map.street.value() == "S"
    map.city instanceof PresenceMarker
    !map.city.present()
  }

  def "typed engine produces an explicit-null value the caller policy may reject"() {
    given:
    def binder = new StructuredValueBinder(mapper())
    def target = mapper().typeFactory.constructParametricType(PatchPresence, AddressPatch)

    when:
    def value = binder.typedMemberValue(null, target, META, AddressPatch)

    then: // the engine is outer-state-policy-free; rejecting Present(null) is the caller's policy
    value == null
  }

  def "typed engine accumulates the supplied pointer for nested failures"() {
    given:
    def binder = new StructuredValueBinder(mapper())
    def target = mapper().typeFactory.constructParametricType(PatchPresence, AddressPatch)

    when:
    binder.typedMemberValue([bogus: "x"], target, META, AddressPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    ex.propertyPath() == "/meta/bogus"
  }

  def "typed engine rejects a mixed shape at a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(mapper())
    def target = mapper().typeFactory.constructParametricType(
        PatchPresence, io.github.kazemek.jsonapi.testfixtures.domainpatch.MixedAddressPatch)

    when:
    binder.typedMemberValue([street: "S", city: "C"], target, META, AddressPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == META
  }

  def "typed engine converts a non-object wire against a presence-aware shape at a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(mapper())
    def target = mapper().typeFactory.constructParametricType(PatchPresence, AddressPatch)

    when:
    binder.typedMemberValue("not-an-object", target, META, AddressPatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == META
  }

  def "low-level engine binds an ordinary domain bean from a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(JsonMapper.builder().build())
    def declared = JsonMapper.builder().build().constructType(Address)

    expect:
    binder.lowLevelKind(declared, [street: "S"], null, null, META, Address) ==
    StructuredValueBinder.LowLevelKind.RECURSE

    when:
    def patch = binder.bindLowLevelStructured([street: "S"], declared, META, Address)

    then:
    patch == new StructuredPatch(
        [
          new StructuredMember("street", "street", new StructuredMemberState.Atomic("S"))
        ])
  }

  def "low-level engine reports nested failures at the supplied pointer"() {
    given:
    def binder = new StructuredValueBinder(JsonMapper.builder().build())
    def dimensions = JsonMapper.builder().build()
        .constructType(io.github.kazemek.jsonapi.testfixtures.domainpatch.Dimensions)

    expect:
    binder.lowLevelKind(dimensions, [width: null], null, null, META, io.github.kazemek.jsonapi.testfixtures.domainpatch.Dimensions) ==
    StructuredValueBinder.LowLevelKind.RECURSE

    when:
    binder.bindLowLevelStructured([width: null], dimensions, META, io.github.kazemek.jsonapi.testfixtures.domainpatch.Dimensions)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/meta/width"
  }

  def "low-level engine applies property-scoped @JsonDeserialize from a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(JsonMapper.builder().build())
    def declared = JsonMapper.builder().build()
        .constructType(io.github.kazemek.jsonapi.jackson3.testmodel.AddressWithLoudNote)

    when: // the same location-neutral machinery a structured meta mapping (KAZ-77) would reuse
    def patch = binder.bindLowLevelStructured(
        [note: "n"], declared, META, io.github.kazemek.jsonapi.jackson3.testmodel.AddressWithLoudNote)

    then: // the property-scoped UpperCaseStringDeserializer runs, producing "N" not "n"
    patch == new StructuredPatch(
        [
          new StructuredMember("note", "note", new StructuredMemberState.Atomic("N"))
        ])
  }

  def "low-level engine keeps a setter-customized bean-valued member atomic from a non-attribute pointer"() {
    given:
    def binder = new StructuredValueBinder(JsonMapper.builder().build())
    def declared = JsonMapper.builder().build()
        .constructType(io.github.kazemek.jsonapi.jackson3.testmodel.OuterWithSetterCustomDetails)

    when: // the details setter's @JsonDeserialize must make it atomic, not a recursed StructuredPatch
    def patch = binder.bindLowLevelStructured(
        [details: [name: "x"]],
        declared,
        META,
        io.github.kazemek.jsonapi.jackson3.testmodel.OuterWithSetterCustomDetails)

    then:
    patch == new StructuredPatch(
        [
          new StructuredMember(
          "details",
          "details",
          new StructuredMemberState.Atomic(
          new io.github.kazemek.jsonapi.jackson3.testmodel.Details("custom")))
        ])
  }

  def "low-level engine detects setter-level @JsonDeserialize(as=...) via the real property type"() {
    given:
    def binder = new StructuredValueBinder(JsonMapper.builder().build())
    def declared = JsonMapper.builder().build()
        .constructType(io.github.kazemek.jsonapi.jackson3.testmodel.OuterWithSetterAsProfile)

    when: // the profile setter's @JsonDeserialize(as=...) refinement is detected through the
    // resolved property type (a setter AnnotatedMethod.getType() is void, which would make the
    // refinement check throw); the member stays Atomic with the refined deserializer applied
    def patch = binder.bindLowLevelStructured(
        [profile: [name: "N", email: "E"]],
        declared,
        META,
        io.github.kazemek.jsonapi.jackson3.testmodel.OuterWithSetterAsProfile)

    then:
    patch == new StructuredPatch(
        [
          new StructuredMember(
          "profile",
          "profile",
          new StructuredMemberState.Atomic(
          new io.github.kazemek.jsonapi.jackson3.testmodel.ExtendedProfile("N", "E")))
        ])
  }
}
