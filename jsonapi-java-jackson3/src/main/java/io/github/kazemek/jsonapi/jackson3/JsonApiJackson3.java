package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson3.internal.DomainResourceBinder;
import io.github.kazemek.jsonapi.jackson3.internal.DomainResourceWriter;
import io.github.kazemek.jsonapi.jackson3.internal.JsonApiDocumentModule;
import io.github.kazemek.jsonapi.jackson3.internal.MappingDefinitionCache;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

/**
 * Factory for Jackson 3 JSON:API document writers, readers, resource mappers, and flat DTO binders.
 *
 * <p>Callers supply an existing {@link JsonMapper} or {@link JsonMapper.Builder}; this factory
 * always derives a <em>new</em> mapper via {@link JsonMapper#rebuild()} and never mutates or
 * replaces the caller's configuration in place. Public surface consists of {@link
 * JsonApiDocumentWriter}, {@link JsonApiDocumentReader}, {@link JsonApiResourceMapper}, {@link
 * JsonApiResourceBinder}, and {@link JsonApiDomainDocumentReader}.
 */
public final class JsonApiJackson3 {

  private static final String CONTEXT = "context";
  private static final String IDENTIFIER_CONVERTER = "identifierConverter";

  private JsonApiJackson3() {}

  /**
   * Returns a writer that validates with {@link ValidationContext#defaults()} then serializes
   * documents using a derived codec-configured mapper.
   */
  public static JsonApiDocumentWriter writer(JsonMapper base) {
    return writer(base, ValidationContext.defaults());
  }

  /**
   * Returns a writer that validates with the given context then serializes documents using a
   * derived codec-configured mapper.
   */
  public static JsonApiDocumentWriter writer(JsonMapper base, ValidationContext context) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(context, CONTEXT);
    return new JsonApiDocumentWriter(documentMapper(base), context);
  }

  /**
   * Returns a writer derived from a caller-supplied builder. The builder is not given the JSON:API
   * module; {@link JsonMapper.Builder#build()} is called, then a codec mapper is derived via {@link
   * JsonMapper#rebuild()}.
   */
  public static JsonApiDocumentWriter writer(JsonMapper.Builder base) {
    return writer(base, ValidationContext.defaults());
  }

  /**
   * Returns a writer derived from a caller-supplied builder and validation context. The builder is
   * not given the JSON:API module.
   */
  public static JsonApiDocumentWriter writer(JsonMapper.Builder base, ValidationContext context) {
    Objects.requireNonNull(base, "base");
    return writer(base.build(), context);
  }

  /**
   * Returns a reader bound to the given read context. Decoding is token-driven and does not use
   * document serializers, so the caller mapper is used as-is.
   */
  public static JsonApiDocumentReader reader(JsonMapper base, DocumentReadContext context) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(context, CONTEXT);
    return new JsonApiDocumentReader(base, context);
  }

  /**
   * Returns a reader derived from a caller-supplied builder and read context. The builder is not
   * given the JSON:API module.
   */
  public static JsonApiDocumentReader reader(JsonMapper.Builder base, DocumentReadContext context) {
    Objects.requireNonNull(base, "base");
    return reader(base.build(), context);
  }

  /**
   * Returns a resource mapper with default identifier conversion. Derives a new mapper via {@link
   * JsonMapper#rebuild()} and never mutates the caller's mapper.
   */
  public static JsonApiResourceMapper resourceMapper(JsonMapper base) {
    return resourceMapper(base, IdentifierConverter.defaults());
  }

  /**
   * Returns a resource mapper with the given identifier converter. Derives a new mapper via {@link
   * JsonMapper#rebuild()} and never mutates the caller's mapper.
   */
  public static JsonApiResourceMapper resourceMapper(
      JsonMapper base, IdentifierConverter identifierConverter) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(identifierConverter, IDENTIFIER_CONVERTER);
    JsonMapper derived = resourceMappingMapper(base);
    DomainResourceWriter writer =
        new DomainResourceWriter(derived, identifierConverter, new MappingDefinitionCache(derived));
    return new JsonApiResourceMapper(writer);
  }

  /**
   * Returns a resource mapper derived from a caller-supplied builder. The builder is not given the
   * JSON:API module.
   */
  public static JsonApiResourceMapper resourceMapper(JsonMapper.Builder base) {
    return resourceMapper(base, IdentifierConverter.defaults());
  }

  /**
   * Returns a resource mapper derived from a caller-supplied builder and identifier converter. The
   * builder is not given the JSON:API module.
   */
  public static JsonApiResourceMapper resourceMapper(
      JsonMapper.Builder base, IdentifierConverter identifierConverter) {
    Objects.requireNonNull(base, "base");
    return resourceMapper(base.build(), identifierConverter);
  }

  /**
   * Returns a flat DTO binder with default identifier conversion and no custom relationship linkage
   * mappers. Derives a new mapper via {@link JsonMapper#rebuild()} and never mutates the caller's
   * mapper.
   */
  public static JsonApiResourceBinder resourceBinder(JsonMapper base) {
    return resourceBinder(base, IdentifierConverter.defaults(), Map.of());
  }

  /**
   * Returns a flat DTO binder with the given identifier converter and no custom relationship
   * linkage mappers. Derives a new mapper via {@link JsonMapper#rebuild()} and never mutates the
   * caller's mapper.
   */
  public static JsonApiResourceBinder resourceBinder(
      JsonMapper base, IdentifierConverter identifierConverter) {
    return resourceBinder(base, identifierConverter, Map.of());
  }

  /**
   * Returns a flat DTO binder with the given identifier converter and relationship linkage mappers
   * keyed by relationship target class. Derives a new mapper via {@link JsonMapper#rebuild()} and
   * never mutates the caller's mapper.
   */
  public static JsonApiResourceBinder resourceBinder(
      JsonMapper base,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(identifierConverter, IDENTIFIER_CONVERTER);
    Objects.requireNonNull(linkageMappers, "linkageMappers");
    JsonMapper derived = resourceMappingMapper(base);
    DomainResourceBinder binder =
        new DomainResourceBinder(
            derived, identifierConverter, new MappingDefinitionCache(derived), linkageMappers);
    return new JsonApiResourceBinder(derived, binder);
  }

  /**
   * Returns a flat DTO binder derived from a caller-supplied builder with default identifier
   * conversion. The builder is not given the JSON:API module.
   */
  public static JsonApiResourceBinder resourceBinder(JsonMapper.Builder base) {
    return resourceBinder(base, IdentifierConverter.defaults(), Map.of());
  }

  /**
   * Returns a flat DTO binder derived from a caller-supplied builder and identifier converter. The
   * builder is not given the JSON:API module.
   */
  public static JsonApiResourceBinder resourceBinder(
      JsonMapper.Builder base, IdentifierConverter identifierConverter) {
    return resourceBinder(base, identifierConverter, Map.of());
  }

  /**
   * Returns a flat DTO binder derived from a caller-supplied builder, identifier converter, and
   * relationship linkage mappers. The builder is not given the JSON:API module.
   */
  public static JsonApiResourceBinder resourceBinder(
      JsonMapper.Builder base,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    Objects.requireNonNull(base, "base");
    return resourceBinder(base.build(), identifierConverter, linkageMappers);
  }

  /**
   * Returns a typed domain envelope reader with default identifier conversion and no custom
   * relationship linkage mappers. Document decoding/validation behaves exactly like {@link
   * #reader(JsonMapper, DocumentReadContext)}; primary and included resources bind through the
   * Phase 2.9 binder after a {@link ResourceTypeRegistry} lookup, using a mapper derived via {@link
   * JsonMapper#rebuild()} that never mutates the caller's mapper.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper base, DocumentReadContext context, ResourceTypeRegistry registry) {
    return domainDocumentReader(base, context, registry, IdentifierConverter.defaults(), Map.of());
  }

  /**
   * Returns a typed domain envelope reader with the given identifier converter and no custom
   * relationship linkage mappers. Derives a new mapper via {@link JsonMapper#rebuild()} and never
   * mutates the caller's mapper.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper base,
      DocumentReadContext context,
      ResourceTypeRegistry registry,
      IdentifierConverter identifierConverter) {
    return domainDocumentReader(base, context, registry, identifierConverter, Map.of());
  }

  /**
   * Returns a typed domain envelope reader with the given identifier converter and relationship
   * linkage mappers keyed by relationship target class. Derives a new mapper via {@link
   * JsonMapper#rebuild()} and never mutates the caller's mapper.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper base,
      DocumentReadContext context,
      ResourceTypeRegistry registry,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(context, CONTEXT);
    Objects.requireNonNull(registry, "registry");
    Objects.requireNonNull(identifierConverter, IDENTIFIER_CONVERTER);
    Objects.requireNonNull(linkageMappers, "linkageMappers");
    return new JsonApiDomainDocumentReader(
        base, context, registry, identifierConverter, linkageMappers);
  }

  /**
   * Returns a typed domain envelope reader derived from a caller-supplied builder with default
   * identifier conversion. The builder is not given the JSON:API module.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper.Builder base, DocumentReadContext context, ResourceTypeRegistry registry) {
    return domainDocumentReader(base, context, registry, IdentifierConverter.defaults(), Map.of());
  }

  /**
   * Returns a typed domain envelope reader derived from a caller-supplied builder and identifier
   * converter. The builder is not given the JSON:API module.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper.Builder base,
      DocumentReadContext context,
      ResourceTypeRegistry registry,
      IdentifierConverter identifierConverter) {
    return domainDocumentReader(base, context, registry, identifierConverter, Map.of());
  }

  /**
   * Returns a typed domain envelope reader derived from a caller-supplied builder, identifier
   * converter, and relationship linkage mappers. The builder is not given the JSON:API module.
   */
  public static JsonApiDomainDocumentReader domainDocumentReader(
      JsonMapper.Builder base,
      DocumentReadContext context,
      ResourceTypeRegistry registry,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    Objects.requireNonNull(base, "base");
    return domainDocumentReader(
        base.build(), context, registry, identifierConverter, linkageMappers);
  }

  /**
   * Derives a clean mapper for resource mapping introspection and attribute conversion. Does not
   * register the JSON:API document module because the resource mapper produces core model objects,
   * not serialized output.
   */
  private static JsonMapper resourceMappingMapper(JsonMapper base) {
    return base.rebuild().build();
  }

  /**
   * Derives a new mapper with JSON:API document serializers registered. Package-private so callers
   * cannot serialize documents without aggregate validation.
   */
  static JsonMapper documentMapper(JsonMapper base) {
    Objects.requireNonNull(base, "base");
    return base.rebuild().addModule(new JsonApiDocumentModule()).build();
  }
}
