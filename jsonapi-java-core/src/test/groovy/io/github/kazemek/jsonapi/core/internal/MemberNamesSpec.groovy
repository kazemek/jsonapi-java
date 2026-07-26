package io.github.kazemek.jsonapi.core.internal

import spock.lang.Specification

class MemberNamesSpec extends Specification {

  def "valid implementation member names"() {
    expect:
    MemberNames.isValid("title")
    MemberNames.isValid("author_id")
    MemberNames.isValid("a1")
    MemberNames.isValid("Title")
    MemberNames.isValid("author name")
    MemberNames.isValid("café")
    !MemberNames.isValid("")
    !MemberNames.isValid("_bad")
    !MemberNames.isValid("bad+name")
    !MemberNames.isValid("-bad")
  }

  def "valid extension member names"() {
    expect:
    MemberNames.isValid("myext:version")
    MemberNames.isValid("myext:field-name")
    MemberNames.isValid("MyExt:version")
    MemberNames.isExtensionMember("MyExt:version")
    !MemberNames.isValid("myext:")
    !MemberNames.isValid("my-ext:version")
    !MemberNames.isExtensionMember("my-ext:version")
  }

  def "at members are recognized and validated"() {
    expect:
    MemberNames.isAtMember("@context")
    MemberNames.isValid("@context")
    MemberNames.isValid("@Context")
    !MemberNames.isAtMember("context")
    !MemberNames.isValid("@")
    !MemberNames.isValid("@_bad")
    !MemberNames.isExtensionMember("@context")
  }
}
