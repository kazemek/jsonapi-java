package io.github.kazemek.jsonapi.testfixtures.enveloperead;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.DomainData;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle;
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatLidArticle;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person;
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadExpectation.BoundEnvelope;
import io.github.kazemek.jsonapi.testfixtures.enveloperead.IncludedExpectation.IdentityProbe;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * The shared typed-envelope read catalog consumed by Jackson-major contract tests.
 *
 * <p>The catalog grows by addition: scenarios are added as the envelope surface grows, and adapter
 * suites pick them up through {@link #all()}. Consumers dispatch on {@link EnvelopeReadVariant} and
 * {@link EnvelopeReadExpectation}, never on a scenario id.
 */
public final class EnvelopeReadScenarios {

  private static final String ARTICLES = "articles";
  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";
  private static final String NODES = "nodes";
  private static final List<Class<?>> NO_TARGETS = List.of();
  private static final List<Class<?>> ARTICLE_TARGETS = List.of(FlatArticle.class);
  private static final List<Class<?>> ARTICLE_AND_PERSON = List.of(FlatArticle.class, Person.class);
  private static final List<Class<?>> ARTICLE_PERSON_STRICT =
      List.of(FlatArticle.class, Person.class, FlatStrictArticle.class);
  private static final Map<String, @Nullable Object> NO_ADDITIONAL = Map.of();

  private static List<Object> objects(Object... values) {
    return List.of(values);
  }

  private static final List<EnvelopeReadScenario> SCENARIOS =
      List.of(
          singleResource(),
          homogeneousCollection(),
          heterogeneousCollection(),
          nullData(),
          metaOnly(),
          identifierPassThrough(),
          errorsDocument(),
          jsonapiLinksAndMembers(),
          absentVersusEmptyIncluded(),
          includedWireOrder(),
          compoundSharedIdentity(),
          sharedIdAndLidInstance(),
          duplicateIncludedIdentities(),
          unregisteredPrimary(),
          unregisteredIncluded(),
          duplicateRegistryTypes(),
          registrationRejectsAnnotations(),
          binderFailures(),
          rootLevelBinderFailure(),
          cyclicLinkage(),
          independentEnvelopes(),
          mutationSafeCollections());

  private static final FixtureCatalog<EnvelopeReadScenario> CATALOG =
      FixtureCatalog.of("envelope-read", SCENARIOS);

  private EnvelopeReadScenarios() {}

  public static FixtureCatalog<EnvelopeReadScenario> catalog() {
    return CATALOG;
  }

  /** The shared catalog in catalog order; the list is immutable. */
  public static List<EnvelopeReadScenario> all() {
    return CATALOG.all();
  }

  /** Looks up a scenario by its stable id. */
  public static EnvelopeReadScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<EnvelopeReadScenario> where(
      Predicate<? super EnvelopeReadScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static EnvelopeReadScenario singleResource() {
    return documentBinding(
        "binds a single-resource document into a flat DTO envelope",
        ARTICLE_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.SINGLE_RESOURCE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            single(
                new FlatArticle(
                    "1",
                    "JSON:API paints my bikeshed!",
                    "Content",
                    ResourceIdentifier.of(PEOPLE, "p1"),
                    List.of(ResourceIdentifier.of(COMMENTS, "c1"))),
                null)));
  }

  private static EnvelopeReadScenario homogeneousCollection() {
    return documentBinding(
        "binds a homogeneous resource collection in wire order",
        ARTICLE_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "resource-collection",
            EnvelopeReaderContext.CODEC_DERIVED,
            collection(
                objects(
                    new FlatArticle("1", "First", null, null, null),
                    new FlatArticle("2", "Second", null, null, null)),
                null)));
  }

  private static EnvelopeReadScenario heterogeneousCollection() {
    return documentBinding(
        "binds a heterogeneous collection through the registry",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.HETEROGENEOUS_COLLECTION,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            collection(
                objects(new FlatArticle("1", "First", null, null, null), new Person("9", "Dan")),
                null)));
  }

  private static EnvelopeReadScenario nullData() {
    return documentBinding(
        "preserves explicit null data as NullData",
        NO_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "null-data",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                DomainData.NullData.INSTANCE,
                null,
                null,
                null,
                null,
                requireMeta("null-data"),
                NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario metaOnly() {
    return documentBinding(
        "preserves absent data on a meta-only document",
        NO_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "meta-only",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                null, null, null, null, null, requireMeta("meta-only"), NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario identifierPassThrough() {
    return documentBinding(
        "passes through identifier primary data without DTO binding",
        NO_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "single-identifier",
            EnvelopeReaderContext.IDENTIFIER_DEFAULTS,
            EnvelopeReadExpectation.bound(
                new DomainData.SingleIdentifier(ResourceIdentifier.of(ARTICLES, "1")),
                null,
                null,
                null,
                null,
                null,
                NO_ADDITIONAL)),
        codecCase(
            "identifier-collection",
            EnvelopeReaderContext.IDENTIFIER_DEFAULTS,
            EnvelopeReadExpectation.bound(
                new DomainData.IdentifierCollection(
                    List.of(
                        ResourceIdentifier.of(ARTICLES, "1"),
                        ResourceIdentifier.of(ARTICLES, "2"))),
                null,
                null,
                null,
                null,
                null,
                NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario errorsDocument() {
    return documentBinding(
        "preserves errors without binding anything",
        NO_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "errors-document",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                null, null, requireErrors("errors-document"), null, null, null, NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario jsonapiLinksAndMembers() {
    Map<String, @Nullable Object> extMembers = new LinkedHashMap<>();
    extMembers.put("ext:request-id", "abc-123");
    Map<String, @Nullable Object> atMembers = new LinkedHashMap<>();
    atMembers.put("@request-id", "req-1");
    return documentBinding(
        "preserves jsonapi object, nullable links, and additional members",
        ARTICLE_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "jsonapi-object",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                new DomainData.SingleResource(new FlatArticle("1", null, null, null, null)),
                null,
                null,
                requireJsonapi("jsonapi-object"),
                null,
                null,
                NO_ADDITIONAL)),
        codecCase(
            "string-and-object-links",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                new DomainData.ResourceCollection(
                    objects(new FlatArticle("1", null, null, null, null))),
                null,
                null,
                null,
                requireLinks("string-and-object-links"),
                null,
                NO_ADDITIONAL)),
        codecCase(
            "extension-and-at-members",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                new DomainData.SingleResource(new FlatArticle("1", "Hello", null, null, null)),
                null,
                null,
                null,
                null,
                null,
                extMembers)),
        bindingCase(
            EnvelopeBindingDocument.AT_MEMBER_DOCUMENT,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.bound(
                new DomainData.SingleResource(new FlatArticle("1", "Hello", null, null, null)),
                null,
                null,
                null,
                null,
                null,
                atMembers)));
  }

  private static EnvelopeReadScenario absentVersusEmptyIncluded() {
    return documentBinding(
        "absent included stays null while present-empty included is a non-null empty IncludedResources",
        ARTICLE_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.SINGLE_RESOURCE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            single(
                new FlatArticle(
                    "1",
                    "JSON:API paints my bikeshed!",
                    "Content",
                    ResourceIdentifier.of(PEOPLE, "p1"),
                    List.of(ResourceIdentifier.of(COMMENTS, "c1"))),
                null)),
        codecCase(
            "empty-included",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.bound(
                new DomainData.SingleResource(new FlatArticle("1", null, null, null, null)),
                IncludedExpectation.of(
                    objects(), IdentityProbe.absent(ResourceIdentity.ofId(PEOPLE, "9"))),
                null,
                null,
                null,
                null,
                NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario includedWireOrder() {
    Person dan = new Person("9", "Dan");
    return documentBinding(
        "binds included resources preserving wire order with identity lookup",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "compound-document",
            EnvelopeReaderContext.CODEC_DERIVED,
            single(
                new FlatArticle("1", null, null, ResourceIdentifier.of(PEOPLE, "9"), null),
                IncludedExpectation.of(
                    objects(dan),
                    IdentityProbe.present(ResourceIdentity.ofId(PEOPLE, "9")),
                    IdentityProbe.absent(ResourceIdentity.ofLid(PEOPLE, "9"))))));
  }

  private static EnvelopeReadScenario compoundSharedIdentity() {
    Person dan = new Person("9", "Dan");
    ResourceIdentifier author = ResourceIdentifier.of(PEOPLE, "9");
    return documentBinding(
        "compound shared identity binds one included DTO reachable from both primary resources",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "compound-shared-identity",
            EnvelopeReaderContext.CODEC_DERIVED,
            collection(
                objects(
                    new FlatArticle("1", null, null, author, null),
                    new FlatArticle("2", null, null, author, null)),
                IncludedExpectation.of(
                    objects(dan), IdentityProbe.present(ResourceIdentity.ofId(PEOPLE, "9"))))));
  }

  private static EnvelopeReadScenario sharedIdAndLidInstance() {
    Person dan = new Person("9", "Dan");
    return documentBinding(
        "shared identity yields one DTO instance reachable from both id and lid keys",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.FROM_DOCUMENT,
        coreCase(
            EnvelopeBindingDocument.SHARED_IDENTITY_ID_AND_LID,
            sharedIdentityDocument(),
            EnvelopeReadExpectation.bound(
                new DomainData.SingleResource(new FlatArticle("1", null, null, null, null)),
                IncludedExpectation.sharedInstance(
                    objects(dan),
                    IdentityProbe.present(ResourceIdentity.ofId(PEOPLE, "9")),
                    IdentityProbe.present(ResourceIdentity.ofLid(PEOPLE, "tmp-9"))),
                null,
                null,
                null,
                null,
                NO_ADDITIONAL)));
  }

  private static EnvelopeReadScenario duplicateIncludedIdentities() {
    return documentBinding(
        "fromDocument fails fast on duplicate included identities",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.FROM_DOCUMENT,
        coreCase(
            EnvelopeBindingDocument.DUPLICATE_INCLUDED_IDENTITIES,
            duplicateIncludedDocument(),
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION, "/included/1")));
  }

  private static EnvelopeReadScenario unregisteredPrimary() {
    return documentBinding(
        "unregistered resource-shaped primary fails at the document pointer with null resourceClass",
        NO_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.UNREGISTERED_PRIMARY_SINGLE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/data")),
        bindingCase(
            EnvelopeBindingDocument.UNREGISTERED_PRIMARY_COLLECTION,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/data/0")));
  }

  private static EnvelopeReadScenario unregisteredIncluded() {
    return documentBinding(
        "unregistered included type fails at the included index",
        ARTICLE_TARGETS,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "compound-document",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/included/0")));
  }

  private static EnvelopeReadScenario duplicateRegistryTypes() {
    return new EnvelopeReadScenario(
        "duplicate registry type names fail at build with the later registrant",
        new EnvelopeReadVariant.Registry(
            List.of(
                new EnvelopeReadVariant.RegistryAttempt(
                    List.of(FlatArticle.class, FlatLidArticle.class),
                    MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION,
                    ARTICLES,
                    FlatLidArticle.class))));
  }

  private static EnvelopeReadScenario registrationRejectsAnnotations() {
    return new EnvelopeReadScenario(
        "registration rejects missing, empty, and invalid resource annotations",
        new EnvelopeReadVariant.Registry(
            List.of(
                new EnvelopeReadVariant.RegistryAttempt(
                    List.of(UnannotatedBindingTarget.class),
                    MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
                    null,
                    UnannotatedBindingTarget.class),
                new EnvelopeReadVariant.RegistryAttempt(
                    List.of(EmptyResourceType.class),
                    MappingDiagnostic.INVALID_RESOURCE_TYPE,
                    null,
                    EmptyResourceType.class),
                new EnvelopeReadVariant.RegistryAttempt(
                    List.of(InvalidResourceType.class),
                    MappingDiagnostic.INVALID_RESOURCE_TYPE,
                    null,
                    InvalidResourceType.class))));
  }

  private static EnvelopeReadScenario binderFailures() {
    return documentBinding(
        "binder failures surface with the document pointer joined to the binder path",
        ARTICLE_PERSON_STRICT,
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.BINDER_FAILURE_COLLECTION,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                "/data/0/relationships/author/data",
                ResourceIdentifier.class)),
        bindingCase(
            EnvelopeBindingDocument.BINDER_FAILURE_SINGLE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
                "/data/relationships/author/data",
                ResourceIdentifier.class)),
        bindingCase(
            EnvelopeBindingDocument.BINDER_FAILURE_INCLUDED,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
                "/included/1/title",
                FlatStrictArticle.class)));
  }

  private static EnvelopeReadScenario rootLevelBinderFailure() {
    return documentBinding(
        "root-level binder failures join to the document pointer without a trailing slash",
        List.of(FlatThrowingArticle.class),
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.ROOT_LEVEL_FAILURE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            EnvelopeReadExpectation.failure(
                MappingDiagnostic.MISSING_CREATOR_INPUT, "/data", FlatThrowingArticle.class)));
  }

  private static EnvelopeReadScenario cyclicLinkage() {
    return documentBinding(
        "cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate",
        List.of(FlatNode.class),
        EnvelopeEntryPoint.READ_VALUE,
        bindingCase(
            EnvelopeBindingDocument.CYCLIC_LINKAGE,
            EnvelopeReaderContext.RESOURCE_DEFAULTS,
            single(
                new FlatNode("1", ResourceIdentifier.of(NODES, "2")),
                IncludedExpectation.of(
                    objects(new FlatNode("2", ResourceIdentifier.of(NODES, "1")))))));
  }

  private static EnvelopeReadScenario independentEnvelopes() {
    ResourceIdentifier author = ResourceIdentifier.of(PEOPLE, "9");
    FlatArticle primary = new FlatArticle("1", null, null, author, null);
    return documentBinding(
        "independent envelopes sharing linkage never inject included DTOs",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.FROM_DOCUMENT,
        coreCase(
            EnvelopeBindingDocument.INDEPENDENT_ENVELOPES_MATCHING,
            independentMatchingDocument(),
            single(primary, IncludedExpectation.of(objects(new Person("9", "Dan"))))),
        coreCase(
            EnvelopeBindingDocument.INDEPENDENT_ENVELOPES_UNRELATED,
            independentUnrelatedDocument(),
            single(primary, IncludedExpectation.of(objects(new Person("99", "Other"))))));
  }

  private static EnvelopeReadScenario mutationSafeCollections() {
    Person dan = new Person("9", "Dan");
    BoundEnvelope compound =
        EnvelopeReadExpectation.bound(
            new DomainData.SingleResource(
                new FlatArticle("1", null, null, ResourceIdentifier.of(PEOPLE, "9"), null)),
            IncludedExpectation.of(
                objects(dan), IdentityProbe.present(ResourceIdentity.ofId(PEOPLE, "9"))),
            null,
            null,
            null,
            null,
            NO_ADDITIONAL);
    BoundEnvelope errors =
        EnvelopeReadExpectation.bound(
            null, null, requireErrors("errors-document"), null, null, null, NO_ADDITIONAL);
    return documentBinding(
        "reader-derived envelope collections are mutation-safe",
        ARTICLE_AND_PERSON,
        EnvelopeEntryPoint.READ_VALUE,
        codecCase(
            "compound-document",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.mutationSafe(compound, true, false, true)),
        codecCase(
            "errors-document",
            EnvelopeReaderContext.CODEC_DERIVED,
            EnvelopeReadExpectation.mutationSafe(errors, false, true, false)));
  }

  private static EnvelopeReadScenario documentBinding(
      String id,
      List<Class<?>> targetClasses,
      EnvelopeEntryPoint entryPoint,
      EnvelopeReadCase... cases) {
    return new EnvelopeReadScenario(
        id, new EnvelopeReadVariant.DocumentBinding(targetClasses, entryPoint, List.of(cases)));
  }

  private static EnvelopeReadCase codecCase(
      String codecScenarioId,
      EnvelopeReaderContext readerContext,
      EnvelopeReadExpectation expectation) {
    return new EnvelopeReadCase(
        EnvelopeReadInput.codec(codecScenarioId), readerContext, expectation);
  }

  private static EnvelopeReadCase bindingCase(
      EnvelopeBindingDocument document,
      EnvelopeReaderContext readerContext,
      EnvelopeReadExpectation expectation) {
    return new EnvelopeReadCase(EnvelopeReadInput.binding(document), readerContext, expectation);
  }

  private static EnvelopeReadCase coreCase(
      EnvelopeBindingDocument wireForm,
      JsonApiDocument document,
      EnvelopeReadExpectation expectation) {
    return new EnvelopeReadCase(
        EnvelopeReadInput.core(wireForm, document),
        EnvelopeReaderContext.RESOURCE_DEFAULTS,
        expectation);
  }

  private static BoundEnvelope single(Object resource, @Nullable IncludedExpectation included) {
    return EnvelopeReadExpectation.bound(
        new DomainData.SingleResource(resource), included, null, null, null, null, NO_ADDITIONAL);
  }

  private static BoundEnvelope collection(
      List<Object> resources, @Nullable IncludedExpectation included) {
    return EnvelopeReadExpectation.bound(
        new DomainData.ResourceCollection(resources),
        included,
        null,
        null,
        null,
        null,
        NO_ADDITIONAL);
  }

  private static JsonApiDocument sharedIdentityDocument() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("name", "Dan");
    return new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of(ARTICLES, "1")),
        null,
        null,
        null,
        null,
        List.of(
            new ResourceObject(
                PEOPLE,
                "9",
                "tmp-9",
                Attributes.ofAttributes(attributes),
                null,
                null,
                null,
                Map.of())),
        Map.of());
  }

  private static JsonApiDocument duplicateIncludedDocument() {
    Map<String, Object> dan = new LinkedHashMap<>();
    dan.put("name", "Dan");
    Map<String, Object> other = new LinkedHashMap<>();
    other.put("name", "Other");
    return new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of(ARTICLES, "1")),
        null,
        null,
        null,
        null,
        List.of(
            new ResourceObject(
                PEOPLE, "9", null, Attributes.ofAttributes(dan), null, null, null, Map.of()),
            new ResourceObject(
                PEOPLE, "9", null, Attributes.ofAttributes(other), null, null, null, Map.of())),
        Map.of());
  }

  private static JsonApiDocument independentMatchingDocument() {
    return independentEnvelopeDocument("9", "Dan");
  }

  private static JsonApiDocument independentUnrelatedDocument() {
    return independentEnvelopeDocument("99", "Other");
  }

  private static JsonApiDocument independentEnvelopeDocument(String includedId, String name) {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(ResourceIdentifier.of(PEOPLE, "9"))));
    ResourceObject article =
        new ResourceObject(
            ARTICLES,
            "1",
            null,
            null,
            Relationships.ofRelationships(relationships),
            null,
            null,
            Map.of());
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("name", name);
    return new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        List.of(
            new ResourceObject(
                PEOPLE,
                includedId,
                null,
                Attributes.ofAttributes(attributes),
                null,
                null,
                null,
                Map.of())),
        Map.of());
  }

  private static Meta requireMeta(String codecId) {
    return Objects.requireNonNull(CodecScenarios.byId(codecId).document().meta(), codecId);
  }

  private static List<ErrorObject> requireErrors(String codecId) {
    return Objects.requireNonNull(CodecScenarios.byId(codecId).document().errors(), codecId);
  }

  private static JsonApiObject requireJsonapi(String codecId) {
    return Objects.requireNonNull(CodecScenarios.byId(codecId).document().jsonapi(), codecId);
  }

  private static Links requireLinks(String codecId) {
    return Objects.requireNonNull(CodecScenarios.byId(codecId).document().links(), codecId);
  }
}
