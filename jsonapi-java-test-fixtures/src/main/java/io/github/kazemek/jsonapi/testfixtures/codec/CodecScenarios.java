package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import java.util.List;
import java.util.function.Predicate;

/**
 * Explicit catalog of codec scenarios in manifest order. Tests select entries by capability (write,
 * read, exact UTF-8, canonical hreflang, schema kind); Jackson 2 parity tests reuse this list.
 */
public final class CodecScenarios {

  private static final FixtureCatalog<CodecScenario> CATALOG =
      FixtureCatalog.of(
          "codec",
          List.of(
              SingleResourceScenario.scenario(),
              ResourceCollectionScenario.scenario(),
              SingleIdentifierScenario.scenario(),
              IdentifierCollectionScenario.scenario(),
              NullDataScenario.scenario(),
              MetaOnlyScenario.scenario(),
              EmptyIdentifierCollectionScenario.scenario(),
              EmptyWrappersScenario.scenario(),
              EmptyErrorsScenario.scenario(),
              EmptyIncludedScenario.scenario(),
              OpenValuesScenario.scenario(),
              RelationshipNullLinkageScenario.scenario(),
              RelationshipEmptyToManyScenario.scenario(),
              RelationshipLinkOnlyScenario.scenario(),
              RelationshipMetaOnlyScenario.scenario(),
              StringAndObjectLinksScenario.scenario(),
              ErrorsDocumentScenario.scenario(),
              JsonApiObjectScenario.scenario(),
              CompoundDocumentScenario.scenario(),
              CompoundNestedIntermediateScenario.scenario(),
              CompoundSharedIdentityScenario.scenario(),
              LocalIdentifierScenario.scenario(),
              ExtensionAndAtMembersScenario.scenario(),
              MemberOrderScenario.scenario()));

  private CodecScenarios() {}

  public static FixtureCatalog<CodecScenario> catalog() {
    return CATALOG;
  }

  public static List<CodecScenario> all() {
    return CATALOG.all();
  }

  public static CodecScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<CodecScenario> where(Predicate<? super CodecScenario> predicate) {
    return CATALOG.where(predicate);
  }

  public static List<CodecScenario> writable() {
    return where(CodecScenario::writable);
  }

  public static List<CodecScenario> readable() {
    return where(CodecScenario::readable);
  }

  public static List<CodecScenario> schemaChecked() {
    return where(scenario -> scenario.schemaKind() != null);
  }

  public static List<CodecScenario> exactUtf8() {
    return where(CodecScenario::assertExactUtf8);
  }

  public static List<CodecScenario> hreflangArray() {
    return where(CodecScenario::assertHreflangArray);
  }
}
