package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext;
import io.github.kazemek.jsonapi.jackson.IncludePath;
import io.github.kazemek.jsonapi.jackson.IncludePolicy;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * Pre-validates include paths and walks domain graphs for compound-document inclusion.
 *
 * <p>All visit, identity, and included-output state is allocated per {@link #collectIncluded}
 * invocation. The engine instance itself is immutable and safe to share. Included resources are
 * emitted through the fieldset-aware selective write path.
 */
public final class CompoundInclusionEngine {

  private final DomainResourceWriter writer;

  public CompoundInclusionEngine(DomainResourceWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  /**
   * Collects included resources for the given primary domain snapshot and context.
   *
   * @return included list {@code null} when no inclusion was requested (empty path list), plus an
   *     aggregated bit for relationships omitted by fieldsets during included selective writes
   */
  public IncludedResourcesResult collectIncluded(
      List<?> primarySnapshot,
      List<ResourceObject> primaryResources,
      CompoundSerializationContext context) {
    Objects.requireNonNull(primarySnapshot, "primarySnapshot");
    Objects.requireNonNull(primaryResources, "primaryResources");
    Objects.requireNonNull(context, "context");
    if (primarySnapshot.size() != primaryResources.size()) {
      throw new IllegalArgumentException("primary snapshot and resource lists must match in size");
    }

    List<IncludePath> paths = context.includePaths();
    if (paths.isEmpty()) {
      return new IncludedResourcesResult(null, false);
    }

    List<Class<?>> distinctTypes = distinctTypesInOrder(primarySnapshot);
    preValidate(distinctTypes, paths, context);

    return new Traversal(context, primarySnapshot, primaryResources).run();
  }

  private static List<Class<?>> distinctTypesInOrder(List<?> primarySnapshot) {
    Set<Class<?>> seen = new LinkedHashSet<>();
    List<Class<?>> types = new ArrayList<>();
    for (Object resource : primarySnapshot) {
      Class<?> type = resource.getClass();
      if (seen.add(type)) {
        types.add(type);
      }
    }
    return types;
  }

  private void preValidate(
      List<Class<?>> distinctTypes, List<IncludePath> paths, CompoundSerializationContext context) {
    for (IncludePath path : paths) {
      if (path.segments().size() > context.maxDepth()) {
        Class<?> resourceClass = distinctTypes.isEmpty() ? null : distinctTypes.getFirst();
        throw new JsonApiMappingException(
            MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED,
            resourceClass,
            path.dotted(),
            "Include path exceeds maxDepth " + context.maxDepth() + ": " + path.dotted());
      }
      for (Class<?> resourceClass : distinctTypes) {
        validatePathAgainstType(path, resourceClass, context);
      }
    }
  }

  private void validatePathAgainstType(
      IncludePath path, Class<?> resourceClass, CompoundSerializationContext context) {
    Class<?> currentClass = resourceClass;
    IncludePolicy policy = context.includePolicy();
    for (int i = 0; i < path.segments().size(); i++) {
      String segment = path.segments().get(i);
      String dottedThrough = path.dottedThrough(i);
      ResourceMapping mapping = writer.mappingFor(currentClass);
      MappingProperty property = findRelationship(mapping, segment);
      if (property == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_INCLUDE_PATH,
            currentClass,
            dottedThrough,
            "Unknown relationship '" + segment + "' on " + mapping.resourceType());
      }
      if (!policy.allows(mapping.resourceType(), segment)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE,
            currentClass,
            dottedThrough,
            "Include denied for " + mapping.resourceType() + "." + segment);
      }
      currentClass = resolveRelatedDomainClass(property, currentClass, dottedThrough);
    }
  }

  private static @Nullable MappingProperty findRelationship(
      ResourceMapping mapping, String jsonapiName) {
    for (MappingProperty property : mapping.relationships()) {
      if (property.jsonapiName().equals(jsonapiName)) {
        return property;
      }
    }
    return null;
  }

  private static Class<?> resolveRelatedDomainClass(
      MappingProperty property, Class<?> ownerClass, String propertyPath) {
    JavaType propertyType = property.accessor().getType();
    JavaType relatedType = unwrapOptionalType(propertyType);
    if (DomainResourceWriter.isToManyType(relatedType)) {
      JavaType contentType = DomainResourceWriter.resolveContentType(relatedType);
      if (contentType == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
            ownerClass,
            propertyPath,
            "Cannot resolve collection content type for include path");
      }
      relatedType = unwrapOptionalType(contentType);
    }
    return relatedType.getRawClass();
  }

  private static JavaType unwrapOptionalType(JavaType type) {
    if (type.isTypeOrSubTypeOf(Optional.class) && type.containedTypeCount() > 0) {
      return type.containedType(0);
    }
    return type;
  }

  private final class Traversal {
    private final CompoundSerializationContext context;
    private final List<?> primarySnapshot;
    private final List<ResourceObject> primaryResources;
    private final Set<ResourceIdentity> primaryIdentities = new HashSet<>();
    private final Map<ResourceIdentity, ResourceObject> includedByIdentity = new LinkedHashMap<>();
    private final Set<VisitKey> visited = new HashSet<>();
    private boolean relationshipOmittedByFieldset;

    Traversal(
        CompoundSerializationContext context,
        List<?> primarySnapshot,
        List<ResourceObject> primaryResources) {
      this.context = context;
      this.primarySnapshot = primarySnapshot;
      this.primaryResources = primaryResources;
    }

    IncludedResourcesResult run() {
      for (ResourceObject primary : primaryResources) {
        ResourceIdentity identity = primary.identityKey();
        if (identity != null) {
          primaryIdentities.add(identity);
        }
      }

      List<IncludePath> paths = context.includePaths();
      for (Object primaryDomain : primarySnapshot) {
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
          walkPath(primaryDomain, paths.get(pathIndex), pathIndex);
        }
      }
      return new IncludedResourcesResult(
          List.copyOf(includedByIdentity.values()), relationshipOmittedByFieldset);
    }

    private void walkPath(Object primaryDomain, IncludePath path, int pathIndex) {
      Queue<DomainAtSegment> queue = new ArrayDeque<>();
      queue.add(new DomainAtSegment(primaryDomain, 0));
      while (!queue.isEmpty()) {
        DomainAtSegment current = queue.poll();
        processSegment(current, path, pathIndex, queue);
      }
    }

    private void processSegment(
        DomainAtSegment current, IncludePath path, int pathIndex, Queue<DomainAtSegment> queue) {
      if (current.segmentIndex() >= path.segments().size()) {
        return;
      }
      Object domain = current.domain();
      ResourceIdentity identity = identityOf(domain);
      if (identity == null) {
        return;
      }
      VisitKey visitKey = new VisitKey(identity, pathIndex, current.segmentIndex());
      if (!visited.add(visitKey)) {
        return;
      }

      String segment = path.segments().get(current.segmentIndex());
      ResourceMapping mapping = writer.mappingFor(domain.getClass());
      MappingProperty property = findRelationship(mapping, segment);
      if (property == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_INCLUDE_PATH,
            domain.getClass(),
            path.dottedThrough(current.segmentIndex()),
            "Unknown relationship '" + segment + "'");
      }
      if (!context.includePolicy().allows(mapping.resourceType(), segment)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE,
            domain.getClass(),
            path.dottedThrough(current.segmentIndex()),
            "Include denied for " + mapping.resourceType() + "." + segment);
      }

      List<Object> related = readRelatedDomainObjects(domain, property);
      int nextSegment = current.segmentIndex() + 1;
      boolean lastSegment = nextSegment >= path.segments().size();
      for (Object relatedDomain : related) {
        ResourceIdentity relatedIdentity = identityOf(relatedDomain);
        if (relatedIdentity != null && primaryIdentities.contains(relatedIdentity)) {
          if (!lastSegment) {
            queue.add(new DomainAtSegment(relatedDomain, nextSegment));
          }
          continue;
        }
        DomainResourceWriter.SelectiveResource selective =
            writer.toResource(relatedDomain, context);
        relationshipOmittedByFieldset |= selective.relationshipOmittedByFieldset();
        offerIncluded(selective.resource(), path.dottedThrough(current.segmentIndex()));
        if (!lastSegment) {
          queue.add(new DomainAtSegment(relatedDomain, nextSegment));
        }
      }
    }

    private @Nullable ResourceIdentity identityOf(Object domain) {
      ResourceIdentifier identifier = writer.extractIdentifier(domain);
      if (identifier.hasId()) {
        return ResourceIdentity.ofId(identifier.type(), Objects.requireNonNull(identifier.id()));
      }
      if (identifier.hasLid()) {
        return ResourceIdentity.ofLid(identifier.type(), Objects.requireNonNull(identifier.lid()));
      }
      return null;
    }

    private void offerIncluded(ResourceObject candidate, String propertyPath) {
      ResourceIdentity identity = candidate.identityKey();
      if (identity == null) {
        return;
      }
      if (primaryIdentities.contains(identity)) {
        return;
      }
      ResourceObject existing = includedByIdentity.get(identity);
      if (existing != null) {
        if (!existing.equals(candidate)) {
          throw new JsonApiMappingException(
              MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION,
              null,
              propertyPath,
              "Conflicting included representation for " + identity);
        }
        return;
      }
      if (includedByIdentity.size() >= context.maxIncluded()) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INCLUDE_COUNT_EXCEEDED,
            null,
            propertyPath,
            "Included resource count exceeds maxIncluded " + context.maxIncluded());
      }
      includedByIdentity.put(identity, candidate);
    }

    private List<Object> readRelatedDomainObjects(Object domain, MappingProperty property) {
      Object raw = writer.readRelationshipValue(domain, property);
      Object value = DomainResourceWriter.unwrapOptional(raw);
      JavaType propertyType = unwrapOptionalType(property.accessor().getType());
      if (DomainResourceWriter.isToManyType(propertyType)) {
        if (value == null) {
          return List.of();
        }
        List<Object> items = DomainResourceWriter.convertToCollection(value);
        List<Object> domainObjects = new ArrayList<>();
        for (Object item : items) {
          Object unwrapped = DomainResourceWriter.unwrapOptional(item);
          if (isIncludableDomainObject(unwrapped)) {
            domainObjects.add(unwrapped);
          }
        }
        return domainObjects;
      }
      if (isIncludableDomainObject(value)) {
        return List.of(value);
      }
      return List.of();
    }

    private static boolean isIncludableDomainObject(@Nullable Object value) {
      return value != null
          && !(value instanceof ResourceIdentifier)
          && !(value instanceof RelationshipData);
    }
  }

  private record DomainAtSegment(Object domain, int segmentIndex) {}

  private record VisitKey(ResourceIdentity identity, int pathIndex, int segmentIndex) {}
}
