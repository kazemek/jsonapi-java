package io.github.kazemek.jsonapi.testfixtures.codec

import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundDocumentCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundNestedIntermediateCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundSharedIdentityCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyErrorsCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyIdentifierCollectionCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyIncludedCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyWrappersCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ErrorsDocumentCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ExtensionAndAtMembersCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.IdentifierCollectionCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.JsonApiObjectCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.LocalIdentifierCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.MemberOrderCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.MetaOnlyCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.NullDataCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.OpenValuesCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipEmptyToManyCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipLinkOnlyCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipMetaOnlyCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipNullLinkageCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ResourceCollectionCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.SingleIdentifierCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.SingleResourceCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.StringAndObjectLinksCase

/**
 * Explicit catalog of codec fixtures in manifest order. Tests select cases by capability
 * (write, read, exact UTF-8, canonical hreflang, schema kind); Jackson 2 parity tests reuse this
 * list.
 */
final class CodecFixtures {

  private static final List<CodecFixture> ALL = List.copyOf([
    SingleResourceCase.fixture(),
    ResourceCollectionCase.fixture(),
    SingleIdentifierCase.fixture(),
    IdentifierCollectionCase.fixture(),
    NullDataCase.fixture(),
    MetaOnlyCase.fixture(),
    EmptyIdentifierCollectionCase.fixture(),
    EmptyWrappersCase.fixture(),
    EmptyErrorsCase.fixture(),
    EmptyIncludedCase.fixture(),
    OpenValuesCase.fixture(),
    RelationshipNullLinkageCase.fixture(),
    RelationshipEmptyToManyCase.fixture(),
    RelationshipLinkOnlyCase.fixture(),
    RelationshipMetaOnlyCase.fixture(),
    StringAndObjectLinksCase.fixture(),
    ErrorsDocumentCase.fixture(),
    JsonApiObjectCase.fixture(),
    CompoundDocumentCase.fixture(),
    CompoundNestedIntermediateCase.fixture(),
    CompoundSharedIdentityCase.fixture(),
    LocalIdentifierCase.fixture(),
    ExtensionAndAtMembersCase.fixture(),
    MemberOrderCase.fixture(),
  ])

  private static final Map<String, CodecFixture> BY_ID =
  ALL.collectEntries { [(it.id): it] } as Map<String, CodecFixture>

  private CodecFixtures() {}

  static List<CodecFixture> all() {
    return ALL
  }

  static CodecFixture byId(String id) {
    def fixture = BY_ID[id]
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown codec fixture id: " + id)
    }
    return fixture
  }

  static List<CodecFixture> writable() {
    return ALL.findAll { it.writable }
  }

  static List<CodecFixture> readable() {
    return ALL.findAll { it.readable }
  }

  static List<CodecFixture> schemaChecked() {
    return ALL.findAll { it.schemaKind != null }
  }

  static List<CodecFixture> exactUtf8() {
    return ALL.findAll { it.assertExactUtf8 }
  }

  static List<CodecFixture> hreflangArray() {
    return ALL.findAll { it.assertHreflangArray }
  }
}
