package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundDocumentCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundNestedIntermediateCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.CompoundSharedIdentityCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyErrorsCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyIdentifierCollectionCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyIncludedCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.EmptyWrappersCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ErrorsDocumentCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ExtensionAndAtMembersCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.IdentifierCollectionCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.JsonApiObjectCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.LocalIdentifierCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.MemberOrderCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.MetaOnlyCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.NullDataCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.OpenValuesCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipEmptyToManyCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipLinkOnlyCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipMetaOnlyCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.RelationshipNullLinkageCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.ResourceCollectionCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.SingleIdentifierCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.SingleResourceCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.StringAndObjectLinksCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explicit catalog of codec fixtures in manifest order. Tests select cases by capability (write,
 * read, exact UTF-8, canonical hreflang, schema kind); Jackson 2 parity tests reuse this list.
 */
public final class CodecFixtures {

  private static final List<CodecFixture> ALL =
      List.of(
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
          MemberOrderCase.fixture());

  private static final Map<String, CodecFixture> BY_ID = indexById(ALL);

  private CodecFixtures() {}

  public static List<CodecFixture> all() {
    return ALL;
  }

  public static CodecFixture byId(String id) {
    CodecFixture fixture = BY_ID.get(id);
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown codec fixture id: " + id);
    }
    return fixture;
  }

  public static List<CodecFixture> writable() {
    return ALL.stream().filter(CodecFixture::writable).toList();
  }

  public static List<CodecFixture> readable() {
    return ALL.stream().filter(CodecFixture::readable).toList();
  }

  public static List<CodecFixture> schemaChecked() {
    return ALL.stream().filter(fixture -> fixture.schemaKind() != null).toList();
  }

  public static List<CodecFixture> exactUtf8() {
    return ALL.stream().filter(CodecFixture::assertExactUtf8).toList();
  }

  public static List<CodecFixture> hreflangArray() {
    return ALL.stream().filter(CodecFixture::assertHreflangArray).toList();
  }

  private static Map<String, CodecFixture> indexById(List<CodecFixture> fixtures) {
    Map<String, CodecFixture> index = new LinkedHashMap<>();
    for (CodecFixture fixture : fixtures) {
      index.put(fixture.id(), fixture);
    }
    return Map.copyOf(index);
  }
}
