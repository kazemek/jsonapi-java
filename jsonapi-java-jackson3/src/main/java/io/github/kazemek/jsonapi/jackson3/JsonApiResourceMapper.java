package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson3.internal.CompoundInclusionEngine;
import io.github.kazemek.jsonapi.jackson3.internal.DomainResourceWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Maps annotated domain objects to JSON:API {@link ResourceObject} instances and documents.
 *
 * <p>Construct instances via {@link
 * JsonApiJackson3#resourceMapper(tools.jackson.databind.json.JsonMapper)} or its overloads, never
 * directly. The mapper is safe for concurrent use once created. Mapping uses Jackson's logical
 * property model and caches resolved definitions by type and configuration identity.
 *
 * <p>Mapping is write-only: this mapper produces core model objects. Feed them to a {@link
 * JsonApiDocumentWriter} for serialization. Read-side binding is deferred to later milestones.
 *
 * <p>Compound inclusion is opt-in via {@link CompoundSerializationContext} on the three-argument
 * {@link #toDocument(Object, DocumentEnvelope, CompoundSerializationContext)} and {@link
 * #toResourceCollection(Iterable, DocumentEnvelope, CompoundSerializationContext)} overloads.
 * Relationship mapping alone never requests inclusion. Callers without an envelope pass {@code
 * null} for the envelope argument.
 *
 * <p>For custom identifier conversion, supply an {@link IdentifierConverter} at construction time.
 * The default converter delegates to {@link Object#toString()}.
 */
public final class JsonApiResourceMapper {

  private final DomainResourceWriter writer;
  private final CompoundInclusionEngine inclusionEngine;

  JsonApiResourceMapper(DomainResourceWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
    this.inclusionEngine = new CompoundInclusionEngine(writer);
  }

  public ResourceObject toResource(Object resource) {
    return writer.toResource(resource);
  }

  public JsonApiDocument toDocument(Object resource) {
    return toDocument(resource, null);
  }

  public JsonApiDocument toDocument(Object resource, @Nullable DocumentEnvelope envelope) {
    ResourceObject resourceObject = writer.toResource(resource);
    return buildDocument(new DocumentData.SingleResource(resourceObject), envelope, null);
  }

  /**
   * Maps a domain object to a document with optional envelope members and explicit compound
   * inclusion from {@code context}.
   */
  public JsonApiDocument toDocument(
      Object resource, @Nullable DocumentEnvelope envelope, CompoundSerializationContext context) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(context, "context");
    List<Object> snapshot = List.of(resource);
    ResourceObject resourceObject = writer.toResource(resource);
    List<ResourceObject> primary = List.of(resourceObject);
    List<ResourceObject> included = inclusionEngine.collectIncluded(snapshot, primary, context);
    return buildDocument(new DocumentData.SingleResource(resourceObject), envelope, included);
  }

  public JsonApiDocument toResourceCollection(Iterable<?> resources) {
    return toResourceCollection(resources, null);
  }

  public JsonApiDocument toResourceCollection(
      Iterable<?> resources, @Nullable DocumentEnvelope envelope) {
    Objects.requireNonNull(resources, "resources");
    List<Object> snapshot = materialize(resources);
    List<ResourceObject> resourceObjects = new ArrayList<>(snapshot.size());
    for (Object resource : snapshot) {
      resourceObjects.add(writer.toResource(resource));
    }
    return buildDocument(
        new DocumentData.ResourceCollection(List.copyOf(resourceObjects)), envelope, null);
  }

  /**
   * Maps a primary collection to a document with optional envelope members and explicit compound
   * inclusion from {@code context}. The iterable is materialized once and reused for type
   * validation, primary mapping, and inclusion traversal.
   */
  public JsonApiDocument toResourceCollection(
      Iterable<?> resources,
      @Nullable DocumentEnvelope envelope,
      CompoundSerializationContext context) {
    Objects.requireNonNull(resources, "resources");
    Objects.requireNonNull(context, "context");
    List<Object> snapshot = materialize(resources);
    List<ResourceObject> resourceObjects = new ArrayList<>(snapshot.size());
    for (Object resource : snapshot) {
      resourceObjects.add(writer.toResource(resource));
    }
    List<ResourceObject> primary = List.copyOf(resourceObjects);
    List<ResourceObject> included = inclusionEngine.collectIncluded(snapshot, primary, context);
    return buildDocument(new DocumentData.ResourceCollection(primary), envelope, included);
  }

  private static List<Object> materialize(Iterable<?> resources) {
    List<Object> snapshot = new ArrayList<>();
    for (Object resource : resources) {
      Objects.requireNonNull(resource, "resource element");
      snapshot.add(resource);
    }
    return List.copyOf(snapshot);
  }

  private static JsonApiDocument buildDocument(
      DocumentData data,
      @Nullable DocumentEnvelope envelope,
      @Nullable List<ResourceObject> included) {
    if (envelope == null) {
      return new JsonApiDocument(data, null, null, null, null, included, Map.of());
    }
    return new JsonApiDocument(
        data, null, envelope.meta(), envelope.jsonapi(), envelope.links(), included, Map.of());
  }
}
