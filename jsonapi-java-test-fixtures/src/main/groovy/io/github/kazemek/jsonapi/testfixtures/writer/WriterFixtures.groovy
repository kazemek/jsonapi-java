package io.github.kazemek.jsonapi.testfixtures.writer

import io.github.kazemek.jsonapi.testfixtures.writer.cases.CompoundDocumentCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.CompoundNestedIntermediateCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.CompoundSharedIdentityCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.EmptyErrorsCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.EmptyIdentifierCollectionCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.EmptyIncludedCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.EmptyWrappersCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.ErrorsDocumentCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.ExtensionAndAtMembersCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.IdentifierCollectionCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.JsonApiObjectCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.LocalIdentifierCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.MemberOrderCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.MetaOnlyCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.NullDataCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.OpenValuesCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.RelationshipEmptyToManyCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.RelationshipLinkOnlyCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.RelationshipMetaOnlyCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.RelationshipNullLinkageCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.ResourceCollectionCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.SingleIdentifierCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.SingleResourceCase
import io.github.kazemek.jsonapi.testfixtures.writer.cases.StringAndObjectLinksCase

/**
 * Explicit catalog of writer fixtures in manifest order. Phase 2.16 Jackson 2 tests reuse this list.
 */
final class WriterFixtures {

  private static final List<WriterFixture> ALL = List.copyOf([
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

  private static final Map<String, WriterFixture> BY_ID =
  ALL.collectEntries { [(it.id): it] } as Map<String, WriterFixture>

  private WriterFixtures() {}

  static List<WriterFixture> all() {
    return ALL
  }

  static WriterFixture byId(String id) {
    def fixture = BY_ID[id]
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown writer fixture id: " + id)
    }
    return fixture
  }

  static List<WriterFixture> exactUtf8() {
    return ALL.findAll { it.assertExactUtf8 }
  }

  static List<WriterFixture> hreflangArray() {
    return ALL.findAll { it.assertHreflangArray }
  }
}
