package io.github.kazemek.jsonapi.testsupport.codec;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.ErrorSource;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Explicit catalog of codec scenarios in manifest order. Each entry is one declarative descriptor:
 * core document model, expected corpus path, validation context, and capability metadata (write,
 * read, schema kind, primary-data kind, exact UTF-8, canonical hreflang, known draft-schema
 * disagreement). Tests select entries by capability; Jackson 2 parity tests reuse this list.
 */
public final class CodecScenarios {

  private static final String ARTICLES = "articles";
  private static final String PEOPLE = "people";
  private static final String COMMENTS = "comments";
  private static final String TITLE = "title";
  private static final String AUTHOR = "author";
  private static final String RELATED = "related";
  private static final String KEYWORD = "keyword";
  private static final String COPYRIGHT_2026 = "Copyright 2026";

  private static final FixtureCatalog<CodecScenario> CATALOG =
      FixtureCatalog.of(
          "codec",
          List.of(
              singleResource(),
              resourceCollection(),
              singleIdentifier(),
              identifierCollection(),
              nullData(),
              metaOnly(),
              emptyIdentifierCollection(),
              emptyWrappers(),
              emptyErrors(),
              emptyIncluded(),
              openValues(),
              relationshipNullLinkage(),
              relationshipEmptyToMany(),
              relationshipLinkOnly(),
              relationshipMetaOnly(),
              stringAndObjectLinks(),
              errorsDocument(),
              jsonApiObject(),
              compoundDocument(),
              compoundNestedIntermediate(),
              compoundSharedIdentity(),
              localIdentifier(),
              extensionAndAtMembers(),
              memberOrder()));

  private CodecScenarios() {}

  public static FixtureCatalog<CodecScenario> catalog() {
    return CATALOG;
  }

  private static CodecScenario singleResource() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(TITLE, "JSON:API paints my bikeshed!");
    var article = Models.resource(ARTICLES, "1", Attributes.ofAttributes(attributes));
    return CodecScenario.of(
        "single-resource",
        "Single resource primary data",
        "documents/single-resource.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario resourceCollection() {
    Map<String, Object> firstAttributes = new LinkedHashMap<>();
    firstAttributes.put(TITLE, "First");
    Map<String, Object> secondAttributes = new LinkedHashMap<>();
    secondAttributes.put(TITLE, "Second");
    var first = Models.resource(ARTICLES, "1", Attributes.ofAttributes(firstAttributes));
    var second = Models.resource(ARTICLES, "2", Attributes.ofAttributes(secondAttributes));
    return CodecScenario.of(
        "resource-collection",
        "Resource collection primary data",
        "documents/resource-collection.json",
        JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of(first, second))),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario singleIdentifier() {
    return CodecScenario.of(
        "single-identifier",
        "Single resource identifier primary data",
        "documents/single-identifier.json",
        JsonApiDocument.withData(
            new DocumentData.SingleIdentifier(Models.identifier(ARTICLES, "1"))),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario identifierCollection() {
    return CodecScenario.of(
        "identifier-collection",
        "Identifier collection primary data",
        "documents/identifier-collection.json",
        JsonApiDocument.withData(
            new DocumentData.IdentifierCollection(
                List.of(Models.identifier(ARTICLES, "1"), Models.identifier(ARTICLES, "2")))),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario nullData() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("reason", "deleted");
    return CodecScenario.of(
        "null-data",
        "Explicit data null with meta",
        "documents/null-data.json",
        new JsonApiDocument(
            DocumentData.NullData.INSTANCE, null, Meta.of(meta), null, null, null, Map.of()),
        null,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario metaOnly() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("copyright", COPYRIGHT_2026);
    return CodecScenario.of(
        "meta-only",
        "Absent data; meta-only document",
        "documents/meta-only.json",
        JsonApiDocument.withMeta(Meta.of(meta)),
        null,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario emptyIdentifierCollection() {
    return CodecScenario.of(
        "empty-identifier-collection",
        "Empty primary data array",
        "documents/empty-identifier-collection.json",
        JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of())),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario emptyWrappers() {
    var article =
        Models.resource(
            ARTICLES, "1", Attributes.empty(), Relationships.empty(), Links.empty(), Meta.empty());
    return CodecScenario.of(
        "empty-wrappers",
        "Present-empty attributes, relationships, links, meta",
        "documents/empty-wrappers.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario emptyErrors() {
    return CodecScenario.of(
        "empty-errors",
        "Present-empty errors array",
        "documents/empty-errors.json",
        JsonApiDocument.withErrors(List.of()),
        null,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario emptyIncluded() {
    var article = Models.resource(ARTICLES, "1");
    return CodecScenario.of(
        "empty-included",
        "Present-empty included array with primary data",
        "documents/empty-included.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article), null, null, null, null, List.of(), Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario openValues() {
    Map<String, @Nullable Object> nested = new LinkedHashMap<>();
    nested.put("tags", List.of("a", "b"));
    Map<String, Object> counts = new LinkedHashMap<>();
    counts.put("views", 2);
    nested.put("counts", counts);

    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    attributes.put("nullable", null);
    attributes.put("nested", nested);
    attributes.put("intValue", 42);
    attributes.put("longValue", 9007199254740991L);
    attributes.put("floatValue", 1.5f);
    attributes.put("doubleValue", 2.25d);
    attributes.put("bigIntValue", new BigInteger("123456789012345678901234567890"));
    attributes.put("bigDecimalValue", new BigDecimal("1234567890.123456789"));

    var article = Models.resource(ARTICLES, "1", Attributes.ofAttributes(attributes));

    Map<String, @Nullable Object> meta = new LinkedHashMap<>();
    meta.put("flag", true);
    meta.put("nullMeta", null);

    return CodecScenario.of(
        "open-values",
        "Open JSON null, nested object/array, and numeric families in attributes/meta",
        "documents/open-values.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            Meta.of(meta),
            null,
            null,
            null,
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario relationshipNullLinkage() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(AUTHOR, Relationship.withData(RelationshipData.NullLinkage.INSTANCE));
    var article = Models.resource(ARTICLES, "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-null-linkage",
        "Explicit null to-one relationship data",
        "documents/relationship-null-linkage.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario relationshipEmptyToMany() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        COMMENTS, Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty()));
    var article = Models.resource(ARTICLES, "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-empty-to-many",
        "Empty to-many relationship data array",
        "documents/relationship-empty-to-many.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario relationshipLinkOnly() {
    Map<String, @Nullable Link> authorLinkEntries = new LinkedHashMap<>();
    authorLinkEntries.put(
        "self", Models.stringLink("http://example.com/articles/1/relationships/author"));
    authorLinkEntries.put(RELATED, Models.stringLink("http://example.com/articles/1/author"));
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(AUTHOR, Relationship.linkOnly(Models.links(authorLinkEntries)));
    var article = Models.resource(ARTICLES, "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-link-only",
        "Link-only relationship without data",
        "documents/relationship-link-only.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario relationshipMetaOnly() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("inferred", true);
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(AUTHOR, Relationship.metaOnly(Meta.of(meta)));
    var article = Models.resource(ARTICLES, "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-meta-only",
        "Meta-only relationship without data",
        "documents/relationship-meta-only.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario stringAndObjectLinks() {
    String selfHref = "http://example.com/articles/1";
    Map<String, @Nullable Link> resourceLinks = new LinkedHashMap<>();
    resourceLinks.put("self", Models.stringLink(selfHref));
    var article = Models.resource(ARTICLES, "1", Models.links(resourceLinks));

    Map<String, Object> relatedMeta = new LinkedHashMap<>();
    relatedMeta.put("count", 1);
    var related =
        Models.objectLink(
            "http://example.com/articles/1/related",
            RELATED,
            "Related",
            "application/vnd.api+json",
            List.of("en"),
            Meta.of(relatedMeta));

    Map<String, @Nullable Link> topLinkEntries = new LinkedHashMap<>();
    topLinkEntries.put("self", Models.stringLink(selfHref));
    topLinkEntries.put(RELATED, related);
    topLinkEntries.put("next", null);

    return new CodecScenario(
        "string-and-object-links",
        "String link, object link, null link, canonical hreflang array",
        "documents/string-and-object-links.json",
        new JsonApiDocument(
            new DocumentData.ResourceCollection(List.of(article)),
            null,
            null,
            null,
            Models.links(topLinkEntries),
            null,
            Map.of()),
        ValidationContext.defaults(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "hreflang canonical list form; draft linkObject.hreflang only accepts a string",
            List.of(Map.of(KEYWORD, "type", "path", "/links/related/hreflang"))),
        false,
        null,
        true);
  }

  private static CodecScenario errorsDocument() {
    Map<String, @Nullable Link> errorLinks = new LinkedHashMap<>();
    errorLinks.put("about", Models.stringLink("http://example.com/docs/errors/invalid"));
    var error =
        new ErrorObject(
            "1",
            Models.links(errorLinks),
            "422",
            "invalid",
            "Invalid Attribute",
            "Title is required",
            new ErrorSource("/data/attributes/title", null, null, Map.of()),
            null,
            Map.of());
    return CodecScenario.of(
        "errors-document",
        "Top-level errors with source and links",
        "documents/errors-document.json",
        JsonApiDocument.withErrors(List.of(error)),
        null,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario jsonApiObject() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("impl", "jsonapi-java");
    var jsonapi =
        new JsonApiObject(
            "1.1",
            List.of("https://jsonapi.org/ext/atomic"),
            List.of("https://example.com/profiles/flex"),
            Meta.of(meta),
            Map.of());
    return new CodecScenario(
        "jsonapi-object",
        "jsonapi version, ext, profile, and meta",
        "documents/jsonapi-object.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(ResourceObject.of(ARTICLES, "1")),
            null,
            null,
            jsonapi,
            null,
            null,
            Map.of()),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        null,
        false,
        null,
        false);
  }

  private static CodecScenario compoundDocument() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "9"))));
    var article = Models.resource(ARTICLES, "1", Relationships.ofRelationships(relationships));
    Map<String, Object> includedAttributes = new LinkedHashMap<>();
    includedAttributes.put("name", "Dan");
    var included = Models.resource(PEOPLE, "9", Attributes.ofAttributes(includedAttributes));
    return CodecScenario.of(
        "compound-document",
        "Compound document with included resources",
        "documents/compound-document.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            List.of(included),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario compoundNestedIntermediate() {
    Map<String, @Nullable Relationship> articleRelationships = new LinkedHashMap<>();
    articleRelationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "9"))));
    articleRelationships.put(
        COMMENTS,
        Relationship.withData(
            new RelationshipData.IdentifierCollectionLinkage(
                List.of(Models.identifier(COMMENTS, "5"), Models.identifier(COMMENTS, "12")))));
    var article =
        Models.resource(ARTICLES, "1", Relationships.ofRelationships(articleRelationships));

    Map<String, @Nullable Relationship> comment5Relationships = new LinkedHashMap<>();
    comment5Relationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "2"))));
    var comment5 =
        Models.resource(
            COMMENTS,
            "5",
            Attributes.ofAttributes(attribute("body", "First!")),
            Relationships.ofRelationships(comment5Relationships));

    var person2 = Models.resource(PEOPLE, "2", Attributes.ofAttributes(attribute("name", "Ezra")));

    Map<String, @Nullable Relationship> comment12Relationships = new LinkedHashMap<>();
    comment12Relationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "9"))));
    var comment12 =
        Models.resource(
            COMMENTS,
            "12",
            Attributes.ofAttributes(attribute("body", "I like XML better")),
            Relationships.ofRelationships(comment12Relationships));

    var person9 = Models.resource(PEOPLE, "9", Attributes.ofAttributes(attribute("name", "Dan")));

    return CodecScenario.of(
        "compound-nested-intermediate",
        "Compound document with nested comments.author intermediates",
        "documents/compound-nested-intermediate.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            List.of(comment5, comment12, person2, person9),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario compoundSharedIdentity() {
    var article1 = Models.resource(ARTICLES, "1", sharedAuthorRelationship());
    var article2 = Models.resource(ARTICLES, "2", sharedAuthorRelationship());
    Map<String, Object> includedAttributes = new LinkedHashMap<>();
    includedAttributes.put("name", "Dan");
    var included = Models.resource(PEOPLE, "9", Attributes.ofAttributes(includedAttributes));
    return CodecScenario.of(
        "compound-shared-identity",
        "Compound collection sharing one included author identity",
        "documents/compound-shared-identity.json",
        new JsonApiDocument(
            new DocumentData.ResourceCollection(List.of(article1, article2)),
            null,
            null,
            null,
            null,
            List.of(included),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static CodecScenario localIdentifier() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.withLid(PEOPLE, "temp-author"))));
    var article =
        Models.resourceWithLid(ARTICLES, "temp-1", Relationships.ofRelationships(relationships));
    return new CodecScenario(
        "local-identifier",
        "Resource and linkage with lid",
        "documents/local-identifier.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        Models.createContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.CREATE,
        null,
        false,
        null,
        false);
  }

  private static CodecScenario extensionAndAtMembers() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(TITLE, "Hello");
    Map<String, Object> additionalMembers = new LinkedHashMap<>();
    additionalMembers.put("@copyright", COPYRIGHT_2026);
    additionalMembers.put("ext:version", 1);
    var article =
        Models.resource(ARTICLES, "1", Attributes.ofAttributes(attributes), additionalMembers);

    Map<String, Object> documentMembers = new LinkedHashMap<>();
    documentMembers.put("ext:request-id", "abc-123");

    return new CodecScenario(
        "extension-and-at-members",
        "Extension and @ members on document and resource",
        "documents/extension-and-at-members.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            null,
            documentMembers),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "top-level ext: member; PR json-api/json-api#1603 does not yet model extension members (see its description)",
            List.of(Map.of(KEYWORD, "unevaluatedProperties", "path", ""))),
        false,
        null,
        false);
  }

  private static CodecScenario memberOrder() {
    var self = Models.stringLink("http://example.com/articles/1");
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(TITLE, "Ordered");
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "9"))));
    Map<String, @Nullable Link> resourceLinks = new LinkedHashMap<>();
    resourceLinks.put("self", self);
    Map<String, Object> resourceMeta = new LinkedHashMap<>();
    resourceMeta.put("created", "2026-01-01");
    Map<String, Object> resourceMembers = new LinkedHashMap<>();
    resourceMembers.put("ext:flag", true);
    var article =
        new ResourceObject(
            ARTICLES,
            "1",
            "temp-1",
            Attributes.ofAttributes(attributes),
            Relationships.ofRelationships(relationships),
            Models.links(resourceLinks),
            Meta.of(resourceMeta),
            resourceMembers);

    Map<String, Object> documentMeta = new LinkedHashMap<>();
    documentMeta.put("copyright", COPYRIGHT_2026);
    Map<String, @Nullable Link> documentLinks = new LinkedHashMap<>();
    documentLinks.put("self", self);
    Map<String, Object> documentMembers = new LinkedHashMap<>();
    documentMembers.put("ext:trace", "t-1");

    return new CodecScenario(
        "member-order",
        "Canonical standard member order then additional members",
        "documents/member-order.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            Meta.of(documentMeta),
            JsonApiObject.ofVersion("1.1"),
            Models.links(documentLinks),
            List.of(ResourceObject.of(PEOPLE, "9")),
            documentMembers),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "response resource carries both id and lid and top-level ext: members; the draft schema requires id and forbids lid in response resources and only models @ members",
            List.of(
                Map.of(KEYWORD, "not", "path", "/data"),
                Map.of(KEYWORD, "unevaluatedProperties", "path", ""))),
        true,
        "documents/member-order.compact.json",
        false);
  }

  private static Relationships sharedAuthorRelationship() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        AUTHOR,
        Relationship.withData(new RelationshipData.SingleLinkage(Models.identifier(PEOPLE, "9"))));
    return Relationships.ofRelationships(relationships);
  }

  private static Map<String, Object> attribute(String name, String value) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(name, value);
    return attributes;
  }
}
