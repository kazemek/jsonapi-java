package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
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
 * <p>For custom identifier conversion, supply an {@link IdentifierConverter} at construction time.
 * The default converter delegates to {@link Object#toString()}.
 */
public final class JsonApiResourceMapper {

  private final DomainResourceWriter writer;

  JsonApiResourceMapper(DomainResourceWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  public ResourceObject toResource(Object resource) {
    return writer.toResource(resource);
  }

  public JsonApiDocument toDocument(Object resource) {
    return toDocument(resource, null);
  }

  public JsonApiDocument toDocument(Object resource, @Nullable DocumentEnvelope envelope) {
    ResourceObject resourceObject = writer.toResource(resource);
    return buildDocument(new DocumentData.SingleResource(resourceObject), envelope);
  }

  public JsonApiDocument toResourceCollection(Iterable<?> resources) {
    return toResourceCollection(resources, null);
  }

  public JsonApiDocument toResourceCollection(
      Iterable<?> resources, @Nullable DocumentEnvelope envelope) {
    Objects.requireNonNull(resources, "resources");
    List<ResourceObject> resourceObjects = new ArrayList<>();
    for (Object resource : resources) {
      Objects.requireNonNull(resource, "resource element");
      resourceObjects.add(writer.toResource(resource));
    }
    return buildDocument(
        new DocumentData.ResourceCollection(List.copyOf(resourceObjects)), envelope);
  }

  private static JsonApiDocument buildDocument(
      DocumentData data, @Nullable DocumentEnvelope envelope) {
    if (envelope == null) {
      return new JsonApiDocument(data, null, null, null, null, null, Map.of());
    }
    return new JsonApiDocument(
        data, null, envelope.meta(), envelope.jsonapi(), envelope.links(), null, Map.of());
  }
}
