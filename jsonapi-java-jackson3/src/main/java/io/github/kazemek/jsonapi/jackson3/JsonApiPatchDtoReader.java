package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.DocumentUsage;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson.DocumentReadContext;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.jackson3.internal.DomainPatchDtoBinder;
import io.github.kazemek.jsonapi.jackson3.internal.MappingDefinitionCache;
import io.github.kazemek.jsonapi.jackson3.internal.MetaBindingModule;
import io.github.kazemek.jsonapi.jackson3.internal.PatchPresenceModule;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads a validated JSON:API resource-update document directly into an application-owned annotated
 * PATCH DTO.
 *
 * <p>{@link #readValue} decodes and aggregate-validates through a factory-composed {@link
 * DocumentReadContext} ({@link PrimaryDataKind#RESOURCE} with {@link
 * DocumentUsage#UPDATE_REQUEST}), then binds the whole update into the PATCH DTO: patchable
 * attributes and relationships declared as {@link io.github.kazemek.jsonapi.jackson.PatchPresence}
 * receive {@code omitted()}, {@code present(value)}, or {@code present(null)} (explicit JSON {@code
 * null} / null relationship linkage), and the identifier binds through {@link IdentifierConverter}.
 * {@link #fromDocument} binds without re-validation. Codec and aggregate failures stay {@link
 * io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException}; bind failures stay {@link
 * io.github.kazemek.jsonapi.jackson.JsonApiMappingException} with resource-relative pointers and
 * are never prefixed with {@code /data}.
 *
 * <p>Close/ownership rules match {@link JsonApiDocumentReader}: convenience overloads close parsers
 * they create; caller-owned streams and parsers stay open. Construct via {@link
 * JsonApiJackson3#patchDtoReader(JsonMapper)} or its overloads. Safe for concurrent use once
 * created.
 */
public final class JsonApiPatchDtoReader {

  private static final String DTO_TYPE = "dtoType";

  private final JsonApiDocumentReader documentReader;
  private final JsonMapper binderMapper;
  private final DomainPatchDtoBinder binder;

  JsonApiPatchDtoReader(
      JsonMapper base,
      ValidationContext validationContext,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    ValidationContext forced =
        Objects.requireNonNull(validationContext, "validationContext")
            .withDocumentUsage(DocumentUsage.UPDATE_REQUEST);
    DocumentReadContext readContext = DocumentReadContext.of(forced, PrimaryDataKind.RESOURCE);
    this.documentReader = new JsonApiDocumentReader(base, readContext);
    this.binderMapper =
        base.rebuild()
            .addModule(new PatchPresenceModule())
            .addModule(new MetaBindingModule())
            .build();
    this.binder =
        new DomainPatchDtoBinder(
            binderMapper,
            identifierConverter,
            new MappingDefinitionCache(binderMapper),
            linkageMappers);
  }

  /** Decodes, validates, and binds the JSON:API update document in the given string. */
  public <T> T readValue(String json, Class<T> dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(json), dtoType);
  }

  /** Decodes, validates, and binds the UTF-8 JSON:API update document in the given bytes. */
  public <T> T readValue(byte[] utf8Json, Class<T> dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(utf8Json), dtoType);
  }

  /**
   * Decodes, validates, and binds one JSON:API update document from a caller-owned UTF-8 stream.
   * The stream is not closed.
   */
  public <T> T readValue(InputStream utf8Stream, Class<T> dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(utf8Stream), dtoType);
  }

  /**
   * Decodes, validates, and binds one JSON:API update document from a caller-owned parser. The
   * parser is not closed.
   */
  public <T> T readValue(JsonParser parser, Class<T> dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(parser), dtoType);
  }

  /** Decodes, validates, and binds using a {@link JavaType}, preserving full parameterization. */
  @SuppressWarnings("java:S1452")
  public Object readValue(String json, JavaType dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(json), dtoType);
  }

  /** Byte-array overload of {@link #readValue(String, JavaType)}. */
  @SuppressWarnings("java:S1452")
  public Object readValue(byte[] utf8Json, JavaType dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(utf8Json), dtoType);
  }

  /** Stream overload of {@link #readValue(String, JavaType)}. */
  @SuppressWarnings("java:S1452")
  public Object readValue(InputStream utf8Stream, JavaType dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(utf8Stream), dtoType);
  }

  /** Parser overload of {@link #readValue(String, JavaType)}. */
  @SuppressWarnings("java:S1452")
  public Object readValue(JsonParser parser, JavaType dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return fromDocument(documentReader.readValue(parser), dtoType);
  }

  /**
   * Binds an already-validated document without re-parsing or re-validating. Requires non-null
   * {@link DocumentData.SingleResource} primary data; other primary-data states throw {@link
   * IllegalArgumentException}.
   */
  @SuppressWarnings("unchecked")
  public <T> T fromDocument(JsonApiDocument document, Class<T> dtoType) {
    Objects.requireNonNull(dtoType, DTO_TYPE);
    return (T) fromDocument(document, binderMapper.constructType(dtoType));
  }

  /**
   * Binds an already-validated document without re-parsing or re-validating, preserving full {@link
   * JavaType} parameterization. Requires non-null {@link DocumentData.SingleResource} primary data;
   * other primary-data states throw {@link IllegalArgumentException}.
   */
  @SuppressWarnings("java:S1452")
  public Object fromDocument(JsonApiDocument document, JavaType dtoType) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(dtoType, DTO_TYPE);
    DocumentData data = document.data();
    if (!(data instanceof DocumentData.SingleResource(ResourceObject resource))) {
      throw new IllegalArgumentException(
          "fromDocument requires DocumentData.SingleResource primary data");
    }
    return binder.fromResource(resource, dtoType);
  }
}
