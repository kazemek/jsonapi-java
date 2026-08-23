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

  private static final String INCLUDE_PATH_CONTEXT = " in include path '";

  private final DomainResourceWriter writer;

  public CompoundInclusionEngine(DomainResourceWriter writer) {
    this.writer = Objects.requireNonNull(writer, "writer");
  }

  /**
   * Collects included resources for the given primary domain snapshot and context.
   *
   * @return included list {@code null} when no inclusion was requested (empty path list), plus the
   *     identities of included resources whose inbound linkage was removed by an applied fieldset
   *     while inclusion still traversed the linking relationship
   */
  public IncludedResourcesResult collectIncluded(
      List<?> primarySnapshot,
      List<JavaType> primaryTypes,
      List<ResourceObject> primaryResources,
      CompoundSerializationContext context) {
    Objects.requireNonNull(primarySnapshot, "primarySnapshot");
    Objects.requireNonNull(primaryTypes, "primaryTypes");
    Objects.requireNonNull(primaryResources, "primaryResources");
    Objects.requireNonNull(context, "context");
    if (primarySnapshot.size() != primaryResources.size()
        || primarySnapshot.size() != primaryTypes.size()) {
      throw new IllegalArgumentException(
          "primary snapshot, type, and resource lists must match in size");
    }

    List<IncludePath> paths = context.includePaths();
    if (paths.isEmpty()) {
      return new IncludedResourcesResult(null, Set.of());
    }

    List<JavaType> distinctTypes = distinctTypesInOrder(primaryTypes);
    preValidate(distinctTypes, paths, context);

    return new Traversal(context, primarySnapshot, primaryTypes, primaryResources).run();
  }

  private static List<JavaType> distinctTypesInOrder(List<JavaType> primaryTypes) {
    Set<JavaType> seen = new LinkedHashSet<>();
    List<JavaType> types = new ArrayList<>();
    for (JavaType type : primaryTypes) {
      if (seen.add(type)) {
        types.add(type);
      }
    }
    return types;
  }

  private void preValidate(
      List<JavaType> distinctTypes, List<IncludePath> paths, CompoundSerializationContext context) {
    for (IncludePath path : paths) {
      if (path.segments().size() > context.maxDepth()) {
        Class<?> resourceClass =
            distinctTypes.isEmpty() ? null : distinctTypes.getFirst().getRawClass();
        // Include-path specification failures have no document member location; the dotted path
        // stays in the message per the mapping-location contract.
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.INCLUDE_DEPTH_EXCEEDED,
            resourceClass,
            "Include path exceeds maxDepth " + context.maxDepth() + ": " + path.dotted());
      }
      for (JavaType resourceType : distinctTypes) {
        validatePathAgainstType(path, resourceType, context);
      }
    }
  }

  private void validatePathAgainstType(
      IncludePath path, JavaType resourceType, CompoundSerializationContext context) {
    JavaType currentType = resourceType;
    IncludePolicy policy = context.includePolicy();
    for (int i = 0; i < path.segments().size(); i++) {
      String segment = path.segments().get(i);
      String dottedThrough = path.dottedThrough(i);
      ResourceMapping mapping = writer.mappingFor(currentType);
      MappingProperty property = findRelationship(mapping, segment);
      if (property == null) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.INVALID_INCLUDE_PATH,
            currentType.getRawClass(),
            "Unknown relationship '"
                + segment
                + "' on "
                + mapping.resourceType()
                + INCLUDE_PATH_CONTEXT
                + dottedThrough
                + "'");
      }
      if (!policy.allows(mapping.resourceType(), segment)) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE,
            currentType.getRawClass(),
            "Include denied for "
                + mapping.resourceType()
                + "."
                + segment
                + INCLUDE_PATH_CONTEXT
                + dottedThrough
                + "'");
      }
      currentType = resolveRelatedDomainType(property, currentType, dottedThrough);
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

  private static JavaType resolveRelatedDomainType(
      MappingProperty property, JavaType ownerType, String dottedThrough) {
    JavaType propertyType = property.accessor().getType();
    JavaType relatedType = unwrapOptionalType(propertyType);
    if (DomainResourceWriter.isToManyType(relatedType)) {
      JavaType contentType = DomainResourceWriter.resolveContentType(relatedType);
      if (contentType == null) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
            ownerType.getRawClass(),
            "Cannot resolve collection content type for include path '" + dottedThrough + "'");
      }
      relatedType = unwrapOptionalType(contentType);
    }
    return relatedType;
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
    private final List<JavaType> primaryTypes;
    private final List<ResourceObject> primaryResources;
    private final Set<ResourceIdentity> primaryIdentities = new HashSet<>();
    private final Map<ResourceIdentity, ResourceObject> includedByIdentity = new LinkedHashMap<>();
    private final Set<VisitKey> visited = new HashSet<>();
    private final Set<ResourceIdentity> linkageExemptions = new LinkedHashSet<>();

    Traversal(
        CompoundSerializationContext context,
        List<?> primarySnapshot,
        List<JavaType> primaryTypes,
        List<ResourceObject> primaryResources) {
      this.context = context;
      this.primarySnapshot = primarySnapshot;
      this.primaryTypes = primaryTypes;
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
      for (int primaryIndex = 0; primaryIndex < primarySnapshot.size(); primaryIndex++) {
        Object primaryDomain = primarySnapshot.get(primaryIndex);
        JavaType primaryType = primaryTypes.get(primaryIndex);
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
          walkPath(primaryDomain, primaryType, paths.get(pathIndex), pathIndex);
        }
      }
      return new IncludedResourcesResult(
          List.copyOf(includedByIdentity.values()), linkageExemptions);
    }

    private void walkPath(
        Object primaryDomain, JavaType primaryType, IncludePath path, int pathIndex) {
      Queue<DomainAtSegment> queue = new ArrayDeque<>();
      queue.add(new DomainAtSegment(primaryDomain, primaryType, 0));
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
      JavaType declaredType = current.declaredType();
      ResourceIdentity identity = identityOf(domain, declaredType);
      if (identity == null) {
        return;
      }
      VisitKey visitKey = new VisitKey(identity, declaredType, pathIndex, current.segmentIndex());
      if (!visited.add(visitKey)) {
        return;
      }

      String segment = path.segments().get(current.segmentIndex());
      ResourceMapping mapping = writer.mappingFor(declaredType);
      MappingProperty property = findRelationship(mapping, segment);
      if (property == null) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.INVALID_INCLUDE_PATH,
            declaredType.getRawClass(),
            "Unknown relationship '"
                + segment
                + "' on "
                + mapping.resourceType()
                + INCLUDE_PATH_CONTEXT
                + path.dottedThrough(current.segmentIndex())
                + "'");
      }
      if (!context.includePolicy().allows(mapping.resourceType(), segment)) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.DENIED_RELATIONSHIP_INCLUDE,
            domain.getClass(),
            "Include denied for "
                + mapping.resourceType()
                + "."
                + segment
                + INCLUDE_PATH_CONTEXT
                + path.dottedThrough(current.segmentIndex())
                + "'");
      }

      List<Object> related = readRelatedDomainObjects(domain, property);
      String propertyPath = path.dottedThrough(current.segmentIndex());
      JavaType relatedType = resolveRelatedDomainType(property, declaredType, propertyPath);
      int nextSegment = current.segmentIndex() + 1;
      boolean lastSegment = nextSegment >= path.segments().size();
      // When the owning resource's fieldset omits this segment, the traversed relationship is
      // absent from its wire representation while inclusion still follows it; resources reached
      // through such an edge legitimately lack inbound linkage in the produced document.
      List<String> ownerFields = DomainResourceWriter.fieldsFor(context, mapping.resourceType());
      boolean edgeOmittedByFieldset = ownerFields != null && !ownerFields.contains(segment);
      for (Object relatedDomain : related) {
        processRelated(
            relatedDomain,
            relatedType,
            edgeOmittedByFieldset,
            nextSegment,
            lastSegment,
            propertyPath,
            queue);
      }
    }

    /** Handles one related domain object reached through the relationship of one path segment. */
    private void processRelated(
        Object relatedDomain,
        JavaType relatedType,
        boolean edgeOmittedByFieldset,
        int nextSegment,
        boolean lastSegment,
        String propertyPath,
        Queue<DomainAtSegment> queue) {
      JavaType effectiveRelatedType = writer.effectiveType(relatedDomain, relatedType);
      ResourceIdentity relatedIdentity = identityOf(relatedDomain, effectiveRelatedType);
      if (relatedIdentity != null && primaryIdentities.contains(relatedIdentity)) {
        enqueueNextSegment(relatedDomain, effectiveRelatedType, nextSegment, lastSegment, queue);
        return;
      }
      if (edgeOmittedByFieldset && relatedIdentity != null) {
        linkageExemptions.add(relatedIdentity);
      }
      ResourceObject relatedResource =
          writer.toResource(relatedDomain, effectiveRelatedType, context);
      offerIncluded(relatedResource, propertyPath);
      enqueueNextSegment(relatedDomain, effectiveRelatedType, nextSegment, lastSegment, queue);
    }

    private void enqueueNextSegment(
        Object domain,
        JavaType declaredType,
        int nextSegment,
        boolean lastSegment,
        Queue<DomainAtSegment> queue) {
      if (!lastSegment) {
        queue.add(new DomainAtSegment(domain, declaredType, nextSegment));
      }
    }

    private @Nullable ResourceIdentity identityOf(Object domain, JavaType declaredType) {
      ResourceIdentifier identifier = writer.extractIdentifier(domain, declaredType);
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
          throw JsonApiMappingException.withoutLocation(
              MappingDiagnostic.CONFLICTING_INCLUDED_REPRESENTATION,
              null,
              "Conflicting included representation for "
                  + identity
                  + " reached via include path '"
                  + propertyPath
                  + "'");
        }
        return;
      }
      if (includedByIdentity.size() >= context.maxIncluded()) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.INCLUDE_COUNT_EXCEEDED,
            null,
            "Included resource count exceeds maxIncluded "
                + context.maxIncluded()
                + " via include path '"
                + propertyPath
                + "'");
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

  private record DomainAtSegment(Object domain, JavaType declaredType, int segmentIndex) {}

  private record VisitKey(
      ResourceIdentity identity, JavaType declaredType, int pathIndex, int segmentIndex) {}
}
