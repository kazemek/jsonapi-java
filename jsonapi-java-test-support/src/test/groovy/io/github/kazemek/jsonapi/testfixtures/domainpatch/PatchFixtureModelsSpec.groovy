package io.github.kazemek.jsonapi.testfixtures.domainpatch

import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.StructuredMember
import io.github.kazemek.jsonapi.jackson.StructuredMemberState
import io.github.kazemek.jsonapi.jackson.StructuredPatch
import spock.lang.Specification

// Why this spec exists: catalog integrity only inspects scenario structure. The recursive structured
// value fixtures' bean surface, equals/hashCode, and accessors are not executed there, and Jackson
// adapter suites do not contribute JaCoCo coverage to this module. These tests pin that local
// surface so new-code coverage on the shared models stays above the Quality Gate.
class PatchFixtureModelsSpec extends Specification {

  def "mutable address patch exposes its bean surface"() {
    given:
    def patch = new MutableAddressPatch()
    def street = PatchPresence.present("S")

    when:
    patch.setStreet(street)
    patch.setCity(PatchPresence.omitted())

    then:
    patch.getStreet() == street
    patch.getCity() == PatchPresence.omitted()
  }

  def "mutable address patch equals and hashCode compare all fields"() {
    given:
    def a = new MutableAddressPatch(PatchPresence.present("S"), PatchPresence.omitted())
    def b = new MutableAddressPatch(PatchPresence.present("S"), PatchPresence.omitted())

    expect:
    a == a
    a == b
    a.hashCode() == b.hashCode()
    a != new MutableAddressPatch(PatchPresence.present("T"), PatchPresence.omitted())
    a != new MutableAddressPatch(PatchPresence.present("S"), PatchPresence.present("C"))
    a != "not a patch"
    a != null
  }

  def "mutable address exposes its bean surface"() {
    given:
    def address = new MutableAddress()

    when:
    address.setStreet("S")
    address.setCity("C")

    then:
    address.getStreet() == "S"
    address.getCity() == "C"
  }

  def "mutable address equals and hashCode compare all fields"() {
    given:
    def a = new MutableAddress("S", "C")
    def b = new MutableAddress("S", "C")

    expect:
    a == a
    a == b
    a.hashCode() == b.hashCode()
    a != new MutableAddress("T", "C")
    a != new MutableAddress("S", "T")
    a != "not an address"
    a != null
  }

  def "mutable articles expose their record accessors"() {
    expect:
    def dto = new MutableArticleWithAddressPatch(
        "1", PatchPresence.present(new MutableAddressPatch(PatchPresence.present("S"), PatchPresence.omitted())))
    dto.id() == "1"
    dto.address().value().getStreet() == PatchPresence.present("S")
    new MutableArticle("1", new MutableAddress("S", "C")).address().getCity() == "C"
  }

  def "presence-aware nested patch shapes expose accessors and equality"() {
    expect:
    def address = new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted())
    address.street() == PatchPresence.present("S")
    address.city() == PatchPresence.omitted()
    address == new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted())
    address.hashCode() == new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted()).hashCode()

    def geo = new GeoPatch(PatchPresence.present("1"), PatchPresence.omitted())
    geo.lat() == PatchPresence.present("1")
    geo.lon() == PatchPresence.omitted()

    def withGeo = new AddressWithGeoPatch(PatchPresence.present("S"), PatchPresence.present(geo))
    withGeo.geo().value() == geo

    def withOptionalCity = new AddressWithOptionalCityPatch(
        PatchPresence.present("S"), PatchPresence.present(Optional.empty()))
    withOptionalCity.city().value() == Optional.empty()

    def withTags = new AddressWithTagsPatch(PatchPresence.present("S"), PatchPresence.present(["a", "b"]))
    withTags.tags().value() == ["a", "b"]

    new MixedAddressPatch(PatchPresence.present("S"), "C").city() == "C"
    new RawPresenceAddressPatch(PatchPresence.present("S"), PatchPresence.omitted()).city().isOmitted()
    new DirectPresentAddressPatch(PatchPresence.present("S"), PatchPresence.omitted()).street() ==
        PatchPresence.present("S")
  }

  def "ordinary structured domain types expose accessors and equality"() {
    expect:
    def address = new Address("S", "C")
    address.street() == "S"
    address.city() == "C"
    address == new Address("S", "C")
    address.hashCode() == new Address("S", "C").hashCode()

    new Geo("1", "2").lat() == "1"
    new AddressWithGeo("S", new Geo("1", "2")).geo().lon() == "2"
    new AddressWithOptionalCity("S", Optional.of("C")).city() == Optional.of("C")
    new Dimensions(1.0, 2.0).width() == 1.0

    def article = new Article("1", new Address("S", "C"))
    article.id() == "1"
    article.address().street() == "S"
    new ArticleWithOptionalAddress("1", Optional.of(new Address("S", "C"))).address().isPresent()
    new ArticleWithOptionalCity("1", new AddressWithOptionalCity("S", Optional.of("C"))).address().city() ==
        Optional.of("C")
    new ArticleWithGeoAddress("1", new AddressWithGeo("S", new Geo("1", "2"))).address().geo().lat() == "1"
    new ArticleWithDimensions("1", new Dimensions(1.0, 2.0)).dimensions().height() == 2.0
    new ArticleWithTags("1", ["a", "b"]).tags() == ["a", "b"]
  }

  def "low-level PatchPresence-wrapped DTOs expose accessors"() {
    expect:
    new PatchPresenceTitleArticle("1", PatchPresence.present("T")).title().value() == "T"
    new PatchPresenceAddressArticle("1", PatchPresence.present(new Address("S", "C"))).address().value().city() == "C"
    new PatchPresenceAddressPatchArticle(
        "1", PatchPresence.present(new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted())))
        .address().value().street() == PatchPresence.present("S")
  }

  def "structured patch and member payloads expose accessors"() {
    expect:
    def member = new StructuredMember("street", "street", new StructuredMemberState.Atomic("S"))
    member.wireName() == "street"
    member.logicalName() == "street"
    member.state() == new StructuredMemberState.Atomic("S")
    new StructuredPatch([member]).members() == [member]
    new StructuredMemberState.Atomic("S").value() == "S"
    new StructuredMemberState.Structured([member]).members() == [member]
  }

  def "new typed PATCH DTO records expose their accessors"() {
    given:
    def address = PatchPresence.present(
        new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted()))
    def optionalAddress = PatchPresence.present(Optional.of(new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted())))
    def optionalCity = PatchPresence.present(
        new AddressWithOptionalCityPatch(PatchPresence.present("S"), PatchPresence.present(Optional.empty())))
    def tags = PatchPresence.present(new AddressWithTagsPatch(PatchPresence.present("S"), PatchPresence.present(["a", "b"])))
    def geo = PatchPresence.present(
        new AddressWithGeoPatch(PatchPresence.present("S"), PatchPresence.present(new GeoPatch(PatchPresence.present("1"), PatchPresence.omitted()))))
    def mixed = PatchPresence.present(new MixedAddressPatch(PatchPresence.present("S"), "C"))
    def raw = PatchPresence.present(new RawPresenceAddressPatch(PatchPresence.present("S"), PatchPresence.omitted()))
    def direct = PatchPresence.present(new DirectPresentAddressPatch(PatchPresence.present("S"), PatchPresence.omitted()))

    expect:
    new ArticleWithAddressPatch("1", address).address() == address
    new ArticleWithOptionalAddressPatch("1", optionalAddress).address() == optionalAddress
    new ArticleWithOptionalCityPatch("1", optionalCity).address() == optionalCity
    new ArticleWithAddressTagsPatch("1", tags).address() == tags
    new ArticleWithGeoPatch("1", geo).address() == geo
    new ArticleWithMixedAddressPatch("1", mixed).address() == mixed
    new ArticleWithRawAddressPatch("1", raw).address() == raw
    new ArticleWithDirectPresentAddressPatch("1", direct).address() == direct
  }

  def "new low-level PATCH DTO records expose their accessors"() {
    expect:
    new Article("1", new Address("S", "C")).id() == "1"
    new ArticleWithOptionalAddress("1", Optional.of(new Address("S", "C"))).id() == "1"
    new ArticleWithOptionalCity("1", new AddressWithOptionalCity("S", Optional.of("C"))).id() == "1"
    new ArticleWithGeoAddress("1", new AddressWithGeo("S", new Geo("1", "2"))).id() == "1"
    new ArticleWithDimensions("1", new Dimensions(1.0, 2.0)).id() == "1"
    new ArticleWithTags("1", ["a", "b"]).id() == "1"
    new MutableArticle("1", new MutableAddress("S", "C")).id() == "1"
    new MutableArticleWithAddressPatch(
        "1", PatchPresence.present(new MutableAddressPatch(PatchPresence.present("S"), PatchPresence.omitted())))
        .id() == "1"
    new PatchPresenceTitleArticle("1", PatchPresence.present("T")).id() == "1"
    new PatchPresenceAddressArticle("1", PatchPresence.present(new Address("S", "C"))).id() == "1"
    new PatchPresenceAddressPatchArticle("1", PatchPresence.present(
        new AddressPatch(PatchPresence.present("S"), PatchPresence.omitted()))).id() == "1"
  }

  def "generic and container structured fixtures expose accessors and equality"() {
    expect:
    def box = new Box([1, 2])
    box.numbers() == [1, 2]
    box == new Box([1, 2])
    box.hashCode() == new Box([1, 2]).hashCode()

    def boxPatch = new BoxPatch(PatchPresence.present([1, 2]))
    boxPatch.numbers() == PatchPresence.present([1, 2])
    boxPatch == new BoxPatch(PatchPresence.present([1, 2]))

    new ArticleWithBox("1", box).box().numbers() == [1, 2]
    new ArticleWithBoxPatch("1", PatchPresence.present(boxPatch)).box().value().numbers() ==
        PatchPresence.present([1, 2])
    new ArticleWithBoxList("1", new Box([[1, 2], [3]])).box().numbers() == [[1, 2], [3]]

    def containers = new AddressWithContainers("S", ["a", "b"] as Set, ["A", "B"] as String[], [x: 1])
    containers.street() == "S"
    containers.aliases() == ["a", "b"] as Set
    containers.initials() == ["A", "B"] as String[]
    containers.scores() == [x: 1]
    // Content-aware equality: array members compare by content, not identity.
    containers == containers
    containers == new AddressWithContainers("S", ["a", "b"] as Set, ["A", "B"] as String[], [x: 1])
    containers.hashCode() ==
        new AddressWithContainers("S", ["a", "b"] as Set, ["A", "B"] as String[], [x: 1]).hashCode()
    containers != new AddressWithContainers("S", ["a", "b"] as Set, ["B", "A"] as String[], [x: 1])
    containers != new AddressWithContainers("S", ["a", "b"] as Set, ["A", "B", "C"] as String[], [x: 1])
    containers != new AddressWithContainers("S", ["a", "b"] as Set, null, [x: 1])
    containers != new AddressWithContainers("T", ["a", "b"] as Set, ["A", "B"] as String[], [x: 1])
    containers != new AddressWithContainers("S", ["a", "b"] as Set, ["A", "B"] as String[], [x: 2])
    containers != null
    containers != "not an address"
    containers.toString() ==
        "AddressWithContainers[street=S, aliases=[a, b], initials=[A, B], scores={x=1}]"

    new ArticleWithContainerAddress("1", containers).address().aliases() == ["a", "b"] as Set

    def containersPatch = new AddressWithContainersPatch(
        PatchPresence.present("S"),
        PatchPresence.present(["a", "b"] as Set),
        PatchPresence.present(["A", "B"] as String[]),
        PatchPresence.present([x: 1]))
    containersPatch.aliases().value() == ["a", "b"] as Set
    containersPatch.initials().value() == ["A", "B"] as String[]
    new ArticleWithContainerAddressPatch("1", PatchPresence.present(containersPatch))
        .address().value().scores().value() == [x: 1]
  }
}
