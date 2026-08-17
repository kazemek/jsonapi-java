package io.github.kazemek.jsonapi.jackson

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import spock.lang.Specification

class JacksonCommonContractsSpec extends Specification {

  // CompoundSerializationContext

  def "defaults request no inclusion, deny-all policy, and empty fieldsets"() {
    when:
    def defaults = CompoundSerializationContext.defaults()

    then:
    defaults.includePaths().isEmpty()
    defaults.includePolicy() == IncludePolicy.denyAll()
    defaults.maxDepth() == 10
    defaults.maxIncluded() == 100
    defaults.fieldsets().isEmpty()
    defaults.fieldPolicy() == FieldPolicy.allowAll()
  }

  def "negative limits are rejected"() {
    when:
    CompoundSerializationContext.defaults().withMaxDepth(-1)

    then:
    thrown(IllegalArgumentException)

    when:
    CompoundSerializationContext.defaults().withMaxIncluded(-1)

    then:
    thrown(IllegalArgumentException)
  }

  def "defensive copy isolates fieldset map and duplicate names collapse"() {
    given:
    def mutableFields = new ArrayList<>(["title", "title", "author"])
    def mutableMap = new LinkedHashMap<String, List<String>>()
    mutableMap.put("articles", mutableFields)
    def context = CompoundSerializationContext.defaults().withFieldsets(mutableMap)

    when:
    mutableFields.add("body-text")
    mutableMap.put("comments", ["body"])

    then:
    context.fieldsets() == [articles: ["title", "author"]]
    context.fieldsets()["articles"] == ["title", "author"]
  }

  // IncludePath

  def "factory-time malformed paths fail with raw input"() {
    when:
    IncludePath.of(input)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == null
    e.propertyPath() == input

    where:
    input << [
      "",
      ".a",
      "a.",
      "a..b",
      " ",
      "a. .b"
    ]
  }

  def "canonical constructor rejects whitespace and dotted segments"() {
    when:
    new IncludePath(List.of(segment))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_INCLUDE_PATH
    e.resourceClass() == null

    where:
    segment << [" ", "comments.author"]
  }

  def "dotted forms round-trip segments"() {
    expect:
    IncludePath.of("comments.author").segments() == ["comments", "author"]
    IncludePath.of("comments.author").dotted() == "comments.author"
    IncludePath.of("comments.author").dottedThrough(1) == "comments.author"
    IncludePath.of("comments.author").dottedThrough(0) == "comments"
  }

  // IncludePolicy / RelationshipAllowance

  def "equivalent policies compare equal"() {
    expect:
    IncludePolicy.denyAll() == IncludePolicy.denyAll()
    IncludePolicy.allowAll() == IncludePolicy.allowAll()
    IncludePolicy.allowing(Set.of(RelationshipAllowance.of("articles", "author"))) ==
        IncludePolicy.allowing(Set.of(RelationshipAllowance.of("articles", "author")))
  }

  def "policy modes gate relationship traversal"() {
    given:
    def allowance = RelationshipAllowance.of("articles", "author")

    expect:
    !IncludePolicy.denyAll().allows("articles", "author")
    IncludePolicy.allowAll().allows("articles", "author")
    IncludePolicy.allowing(Set.of(allowance)).allows("articles", "author")
    !IncludePolicy.allowing(Set.of(allowance)).allows("articles", "comments")
  }

  def "allowing policy defensively copies its allowance set"() {
    given:
    def mutable = new HashSet<RelationshipAllowance>([
      RelationshipAllowance.of("articles", "author")
    ])

    when:
    def policy = IncludePolicy.allowing(mutable)
    mutable.add(RelationshipAllowance.of("articles", "comments"))

    then:
    !policy.allows("articles", "comments")
    policy.allows("articles", "author")
  }

  // FieldPolicy / FieldAllowance

  def "field policy modes gate fieldset fields"() {
    given:
    def allowance = FieldAllowance.of("articles", "title")

    expect:
    !FieldPolicy.denyAll().allows("articles", "title")
    FieldPolicy.allowAll().allows("articles", "title")
    FieldPolicy.allowing(Set.of(allowance)).allows("articles", "title")
    !FieldPolicy.allowing(Set.of(allowance)).allows("articles", "author")
  }

  def "allowing field policy defensively copies its allowance set"() {
    given:
    def mutable = new HashSet<FieldAllowance>([
      FieldAllowance.of("articles", "title")
    ])

    when:
    def policy = FieldPolicy.allowing(mutable)
    mutable.add(FieldAllowance.of("articles", "author"))

    then:
    !policy.allows("articles", "author")
    policy.allows("articles", "title")
  }

  // DomainData

  def "resource collection is defensively copied and unmodifiable"() {
    given:
    def source = ["x"] as List<Object>

    when:
    def collection = new DomainData.ResourceCollection(source)
    source.add("y")

    then:
    collection.resources() == ["x"]

    when:
    collection.resources().add("z")
    then:
    thrown(UnsupportedOperationException)
  }

  // IncludedResources

  def "of preserves wire order and resolves identities through declared positions"() {
    given:
    def dto = "dto"
    def included =
        IncludedResources.of(
        ["x", dto] as List<Object>,
        [
          [
            ResourceIdentity.ofId("people", "9"),
            ResourceIdentity.ofLid("people", "9")
          ] as Set<ResourceIdentity>,
          [] as Set<ResourceIdentity>
        ] as List<Set<ResourceIdentity>>)

    expect:
    included.resources() == ["x", "dto"]
    included.find(ResourceIdentity.ofId("people", "9")).get() == "x"
    included.find(ResourceIdentity.ofLid("people", "9")).get() == "x"
    included.find(ResourceIdentity.ofId("people", "99")).isEmpty()
  }

  def "identity resolves only to the position that declared it"() {
    given:
    def first = "first"
    def second = "second"
    def included =
        IncludedResources.of(
        [first, second] as List<Object>,
        [
          [
            ResourceIdentity.ofId("people", "9")
          ] as Set<ResourceIdentity>,
          [
            ResourceIdentity.ofId("people", "2")
          ] as Set<ResourceIdentity>
        ] as List<Set<ResourceIdentity>>)

    expect:
    included.find(ResourceIdentity.ofId("people", "9")).get() == first
    included.find(ResourceIdentity.ofId("people", "2")).get() == second
    // an identity never declared anywhere resolves empty, never a non-declaring position
    included.find(ResourceIdentity.ofId("people", "99")).isEmpty()
  }

  def "of rejects identity declarations that do not match the resource count"() {
    when:
    IncludedResources.of(
        ["x"] as List<Object>,
        [
          [
            ResourceIdentity.ofId("people", "9")
          ] as Set<ResourceIdentity>,
          [] as Set<ResourceIdentity>
        ] as List<Set<ResourceIdentity>>)

    then:
    thrown(IllegalArgumentException)

    when:
    IncludedResources.of(
        ["x", "y"] as List<Object>,
        [
          [
            ResourceIdentity.ofId("people", "9")
          ] as Set<ResourceIdentity>
        ])

    then:
    thrown(IllegalArgumentException)
  }

  def "of rejects the same identity declared for more than one position"() {
    when:
    IncludedResources.of(
        ["x", "y"] as List<Object>,
        [
          [
            ResourceIdentity.ofId("people", "9")
          ] as Set<ResourceIdentity>,
          [
            ResourceIdentity.ofId("people", "9")
          ] as Set<ResourceIdentity>
        ] as List<Set<ResourceIdentity>>)

    then:
    thrown(IllegalArgumentException)
  }

  def "of defensively copies construction sources"() {
    given:
    def sourceList = ["x"] as List<Object>
    def mutableIdentities = new LinkedHashSet<ResourceIdentity>([
      ResourceIdentity.ofId("people", "9")
    ])
    List<Set<ResourceIdentity>> sourceIdentities = [mutableIdentities]

    when:
    def included = IncludedResources.of(sourceList, sourceIdentities)
    sourceList.add("y")
    mutableIdentities.add(ResourceIdentity.ofId("people", "99"))

    then:
    included.resources() == ["x"]
    included.find(ResourceIdentity.ofId("people", "9")).get() == "x"
    included.find(ResourceIdentity.ofId("people", "99")).isEmpty()

    when:
    included.resources().add("z")
    then:
    thrown(UnsupportedOperationException)
  }

  // DocumentReadContext

  def "read context defaults and derivations"() {
    expect:
    DocumentReadContext.resourceDefaults().primaryDataKind() == PrimaryDataKind.RESOURCE
    DocumentReadContext.identifierDefaults().primaryDataKind() == PrimaryDataKind.RESOURCE_IDENTIFIER
    DocumentReadContext.of(ValidationContext.defaults(), PrimaryDataKind.RESOURCE) ==
        DocumentReadContext.resourceDefaults()
    DocumentReadContext.resourceDefaults()
        .withPrimaryDataKind(PrimaryDataKind.RESOURCE_IDENTIFIER) ==
        DocumentReadContext.identifierDefaults()
  }

  // MappedDocument

  def "applyTo enables the sparse-fieldset exception only when flagged"() {
    given:
    def base = ValidationContext.defaults()
    def document =
        new JsonApiDocument(DocumentData.NullData.INSTANCE, null, null, null, null, null, Map.of())

    expect:
    new MappedDocument(document, false).applyTo(base).is(base)
    new MappedDocument(document, true).applyTo(base) != base
    new MappedDocument(document, true).applyTo(base).sparseFieldsetException()
  }

  // IdentifierConverter

  def "defaults converter delegates to toString and parse returns the wire string"() {
    given:
    def converter = IdentifierConverter.defaults()

    expect:
    converter.convert(42L) == "42"
    converter.convert(null) == null
    converter.parse("9") == "9"
    converter.parse(null) == null
  }

  // Diagnostics and source location

  def "mapping exception carries stable diagnostic, class, and property path"() {
    when:
    def e = new JsonApiMappingException(
        MappingDiagnostic.MISSING_IDENTIFIER, String, "id", "message", null)

    then:
    e.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
    e.resourceClass() == String
    e.propertyPath() == "id"
    e.message == "message"
  }

  def "read exception carries category, pointer, location, and rule code"() {
    given:
    def location = new SourceLocation(1, 2, 3L, 4L)

    when:
    def e = new JsonApiDocumentReadException(
        CodecFailureCategory.MALFORMED_JSON, "/data", location, "message")

    then:
    e.category() == CodecFailureCategory.MALFORMED_JSON
    e.jsonPointer() == "/data"
    e.sourceLocation() == location
    e.ruleCode() == null
    e.message == "message"
  }

  def "source location distinguishes known and unknown positions"() {
    expect:
    !SourceLocation.UNKNOWN.isKnown()
    new SourceLocation(0, 0, 0L, 0L).isKnown()
    new SourceLocation(-1, 2, -1L, -1L).isKnown()
  }

  // PatchCommand / PatchChange

  def "patch change compact constructors reject null names"() {
    when:
    new PatchChange.AttributeChange(null, "title", "x")

    then:
    thrown(NullPointerException)

    when:
    new PatchChange.AttributeChange("title", null, "x")

    then:
    thrown(NullPointerException)

    when:
    new PatchChange.RelationshipChange(null, "author", null)

    then:
    thrown(NullPointerException)

    when:
    new PatchChange.RelationshipChange("author", null, null)

    then:
    thrown(NullPointerException)
  }

  def "patch command rejects null components"() {
    when:
    new PatchCommand<>(null, "1", List.of())

    then:
    thrown(NullPointerException)

    when:
    new PatchCommand<>(String, null, List.of())

    then:
    thrown(NullPointerException)

    when:
    new PatchCommand<>(String, "1", null)

    then:
    thrown(NullPointerException)

    when:
    new PatchCommand<>(String, "1", [null])

    then:
    thrown(NullPointerException)
  }

  def "mutating changes or list or set or array values cannot affect the command"() {
    given:
    def mutableList = new ArrayList<>(["a"])
    def mutableSet = new LinkedHashSet<>(["s"])
    def mutableArray = ["x"] as String[]
    def changes = new ArrayList<PatchChange>()
    changes.add(new PatchChange.AttributeChange("title", "title", mutableList))
    changes.add(new PatchChange.AttributeChange("tags", "tags", mutableSet))
    changes.add(new PatchChange.AttributeChange("names", "names", mutableArray))
    def command = new PatchCommand<>(String, "1", changes)

    when:
    changes.add(new PatchChange.AttributeChange("extra", "extra", "y"))
    mutableList.add("b")
    mutableSet.add("t")
    mutableArray[0] = "z"
    command.changes().add(new PatchChange.AttributeChange("nope", "nope", "n"))

    then:
    thrown(UnsupportedOperationException)
    command.changes().size() == 3
    (command.changes()[0].value() as List) == ["a"]
    (command.changes()[1].value() as Set) == ["s"] as Set
    (command.changes()[2].value() as String[])[0] == "x"
  }

  def "mutating an array returned from value cannot affect the stored change"() {
    given:
    def change = new PatchChange.AttributeChange("names", "names", ["a"] as String[])
    def exposed = change.value() as String[]

    when:
    exposed[0] = "changed"

    then:
    (change.value() as String[])[0] == "a"
  }

  def "mutating a map value cannot affect the stored change"() {
    given:
    def mutableMap = new LinkedHashMap<String, Object>()
    mutableMap.put("type", "tags")
    mutableMap.put("id", "t1")
    mutableMap.put("lid", null)
    def change = new PatchChange.RelationshipChange("author", "author", mutableMap)

    when:
    mutableMap.put("id", "mutated")
    (change.value() as Map).put("id", "exposed")

    then:
    thrown(UnsupportedOperationException)
    (change.value() as Map).id == "t1"
    (change.value() as Map).type == "tags"
  }

  def "mutating a primitive array returned from value cannot affect the stored change"() {
    given:
    def change = new PatchChange.RelationshipChange("counts", "counts", [1, 2] as int[])
    def exposed = change.value() as int[]

    when:
    exposed[0] = 99

    then:
    (change.value() as int[])[0] == 1
    (change.value() as int[])[1] == 2
  }

  // PatchPresence

  def "PatchPresence omitted and present factories"() {
    expect:
    PatchPresence.<String>omitted() instanceof PatchPresence.Omitted
    PatchPresence.present("hello") instanceof PatchPresence.Present
    (PatchPresence.present("hello") as PatchPresence.Present).value() == "hello"
    (PatchPresence.<String>present(null) as PatchPresence.Present).value() == null
  }
}
