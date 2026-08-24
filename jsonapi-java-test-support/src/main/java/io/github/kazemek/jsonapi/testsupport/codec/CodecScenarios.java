package io.github.kazemek.jsonapi.testsupport.codec;

import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.codec.cases.CompoundDocumentScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.CompoundNestedIntermediateScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.CompoundSharedIdentityScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.EmptyErrorsScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.EmptyIdentifierCollectionScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.EmptyIncludedScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.EmptyWrappersScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.ErrorsDocumentScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.ExtensionAndAtMembersScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.IdentifierCollectionScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.JsonApiObjectScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.LocalIdentifierScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.MemberOrderScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.MetaOnlyScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.NullDataScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.OpenValuesScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.RelationshipEmptyToManyScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.RelationshipLinkOnlyScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.RelationshipMetaOnlyScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.RelationshipNullLinkageScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.ResourceCollectionScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.SingleIdentifierScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.SingleResourceScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.StringAndObjectLinksScenario;
import java.util.List;

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








}
