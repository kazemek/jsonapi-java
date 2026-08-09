package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson3.internal.DomainResourceBinder;
import io.github.kazemek.jsonapi.jackson3.internal.MappingDefinitionCache;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads validated JSON:API documents into an immutable {@link JsonApiDomainDocument} with flat
 * primary DTOs and independently bound included DTOs.
 *
 * <p>Decoding and aggregate validation run exactly as in {@link JsonApiDocumentReader}: {@link
 * #readValue} overloads share its close/ownership rules and keep codec/validation failures as
 * {@link JsonApiDocumentReadException} with the same category, pointer, location, and rule code.
 * {@link #fromDocument(JsonApiDocument)} binds only and never re-parses or re-validates.
 *
 * <p>Primary resource data and every present {@code included} element are bound through the Phase
 * 2.9 binder after looking up {@link ResourceObject#type()} in the supplied {@link
 * ResourceTypeRegistry}; identifier primary data and error documents never attempt DTO binding.
 * Resource types absent from the registry fail with {@link
 * MappingDiagnostic#UNREGISTERED_RESOURCE_TYPE} at the document pointer before any envelope
 * escapes; other binder failures are rethrown with the document pointer joined to the binder path.
 * Relationship properties stay linkage-only and {@code included} is never injected.
 *
 * <p>Construct instances via {@link JsonApiJackson3#domainDocumentReader(JsonMapper,
 * DocumentReadContext, ResourceTypeRegistry)} or its overloads, never directly. The reader is safe
 * for concurrent use once created.
 */
public final class JsonApiDomainDocumentReader {

  private final JsonApiDocumentReader documentReader;
  private final ResourceTypeRegistry registry;
  private final JsonMapper binderMapper;
  private final DomainResourceBinder binder;
  private final JsonApiDomainDocument.MetaConverter metaConverter;

  JsonApiDomainDocumentReader(
      JsonMapper base,
      DocumentReadContext context,
      ResourceTypeRegistry registry,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.documentReader = new JsonApiDocumentReader(base, context);
    this.registry = Objects.requireNonNull(registry, "registry");
    this.binderMapper = base.rebuild().build();
    this.binder =
        new DomainResourceBinder(
            binderMapper,
            identifierConverter,
            new MappingDefinitionCache(binderMapper),
            linkageMappers);
    this.metaConverter = new BinderMetaConverter(binderMapper);
  }

  /** Decodes, validates, and binds the JSON:API document in the given string. */
  public JsonApiDomainDocument readValue(String json) {
    return fromDocument(documentReader.readValue(json));
  }

  /** Decodes, validates, and binds the UTF-8 JSON:API document in the given bytes. */
  public JsonApiDomainDocument readValue(byte[] utf8Json) {
    return fromDocument(documentReader.readValue(utf8Json));
  }

  /**
   * Decodes, validates, and binds one JSON:API document from a caller-owned UTF-8 stream. The
   * stream is not closed; only the parser created for this call is closed.
   */
  public JsonApiDomainDocument readValue(InputStream utf8Stream) {
    return fromDocument(documentReader.readValue(utf8Stream));
  }

  /**
   * Decodes, validates, and binds one JSON:API document from a caller-owned parser starting at the
   * current token (or the next token if none is current). The parser is not closed.
   */
  public JsonApiDomainDocument readValue(JsonParser parser) {
    return fromDocument(documentReader.readValue(parser));
  }

  /**
   * Binds an already-validated document into a domain envelope; never re-parses or re-validates.
   */
  public JsonApiDomainDocument fromDocument(JsonApiDocument document) {
    Objects.requireNonNull(document, "document");
    return new JsonApiDomainDocument(
        new JsonApiDomainDocument.Components(
            bindData(document.data()),
            document.errors(),
            document.meta(),
            document.jsonapi(),
            document.links(),
            bindIncluded(document.included()),
            document.additionalMembers()),
        metaConverter);
  }

  private @Nullable DomainData bindData(@Nullable DocumentData data) {
    if (data == null) {
      return null;
    }
    return switch (data) {
      case DocumentData.NullData() -> DomainData.NullData.INSTANCE;
      case DocumentData.SingleResource(ResourceObject resource) ->
          new DomainData.SingleResource(bindResource(resource, "/data"));
      case DocumentData.ResourceCollection(List<ResourceObject> resources) -> {
        List<Object> bound = new ArrayList<>(resources.size());
        for (int i = 0; i < resources.size(); i++) {
          bound.add(bindResource(resources.get(i), "/data/" + i));
        }
        yield new DomainData.ResourceCollection(bound);
      }
      case DocumentData.SingleIdentifier(ResourceIdentifier identifier) ->
          new DomainData.SingleIdentifier(identifier);
      case DocumentData.IdentifierCollection(List<ResourceIdentifier> identifiers) ->
          new DomainData.IdentifierCollection(identifiers);
    };
  }

  private @Nullable IncludedResources bindIncluded(@Nullable List<ResourceObject> included) {
    if (included == null) {
      return null;
    }
    List<Object> bound = new ArrayList<>(included.size());
    Map<ResourceIdentity, Object> index = new LinkedHashMap<>();
    for (int i = 0; i < included.size(); i++) {
      ResourceObject resource = included.get(i);
      String pointer = "/included/" + i;
      Object dto = bindResource(resource, pointer);
      bound.add(dto);
      if (resource.hasId()) {
        putIdentity(
            index,
            ResourceIdentity.ofId(resource.type(), Objects.requireNonNull(resource.id())),
            dto,
            pointer);
      }
      if (resource.hasLid()) {
        putIdentity(
            index,
            ResourceIdentity.ofLid(resource.type(), Objects.requireNonNull(resource.lid())),
            dto,
            pointer);
      }
    }
    return new IncludedResources(bound, index);
  }

  private static void putIdentity(
      Map<ResourceIdentity, Object> index, ResourceIdentity identity, Object dto, String pointer) {
    Object previous = index.putIfAbsent(identity, dto);
    if (previous != null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION,
          null,
          pointer,
          "Duplicate included identity " + identity);
    }
  }

  private Object bindResource(ResourceObject resource, String documentPointer) {
    ResourceObject checkedResource = Objects.requireNonNull(resource, "resource");
    ResourceTypeRegistry.RegisteredType registered = registry.resolve(checkedResource.type());
    if (registered == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE,
          null,
          documentPointer,
          "No DTO target registered for JSON:API resource type '" + checkedResource.type() + "'");
    }
    JavaType registeredType = registered.javaType();
    JavaType targetType =
        registeredType != null ? registeredType : binderMapper.constructType(registered.rawClass());
    try {
      return binder.fromResource(checkedResource, targetType);
    } catch (JsonApiMappingException ex) {
      String message = ex.getMessage() != null ? ex.getMessage() : ex.diagnostic().name();
      throw new JsonApiMappingException(
          ex.diagnostic(),
          ex.resourceClass(),
          joinPointer(documentPointer, ex.propertyPath()),
          message,
          ex);
    }
  }

  private static String joinPointer(String documentPointer, @Nullable String binderPath) {
    if (binderPath == null || binderPath.equals("/")) {
      return documentPointer;
    }
    return documentPointer + binderPath;
  }
}
