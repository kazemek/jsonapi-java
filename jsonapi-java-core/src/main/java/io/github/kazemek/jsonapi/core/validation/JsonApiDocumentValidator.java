package io.github.kazemek.jsonapi.core.validation;

import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Aggregate document validation requiring full document context. */
public final class JsonApiDocumentValidator {

  private static final String PATH_DATA = "/data";
  private static final String PATH_META = "/meta";
  private static final String PATH_LINKS = "/links";
  private static final Set<String> PAGINATION_LINKS = Set.of("first", "last", "prev", "next");

  public void validate(JsonApiDocument document, ValidationContext context) {
    validateAdditionalMembers(document.additionalMembers(), "", context);
    if (document.meta() != null) {
      validateMeta(document.meta(), PATH_META, context);
    }
    if (document.jsonapi() != null) {
      validateJsonApiObject(document.jsonapi(), "/jsonapi", context);
    }
    if (document.links() != null) {
      validateLinks(
          document.links(), PATH_LINKS, context.withLinksContext(LinksContext.TOP_LEVEL), null);
    }
    if (document.errors() != null) {
      validateErrors(document.errors(), context);
    }
    if (document.data() != null) {
      validatePrimaryData(document.data(), context);
    }
    if (document.included() != null) {
      validateIncluded(document.included(), document.data(), context);
    }
  }

  private void validateErrors(List<ErrorObject> errors, ValidationContext context) {
    for (int index = 0; index < errors.size(); index++) {
      validateError(errors.get(index), "/errors/" + index, context);
    }
  }

  private void validatePrimaryData(DocumentData data, ValidationContext context) {
    switch (data) {
      case DocumentData.NullData ignored -> {
        // Explicit null primary data has no nested members to validate.
      }
      case DocumentData.SingleResource(ResourceObject resource) ->
          validateResource(resource, PATH_DATA, context);
      case DocumentData.ResourceCollection(List<ResourceObject> resources) -> {
        for (int index = 0; index < resources.size(); index++) {
          validateResource(resources.get(index), PATH_DATA + "/" + index, context);
        }
      }
      case DocumentData.SingleIdentifier(ResourceIdentifier identifier) ->
          validateIdentifier(identifier, PATH_DATA, context);
      case DocumentData.IdentifierCollection(List<ResourceIdentifier> identifiers) -> {
        for (int index = 0; index < identifiers.size(); index++) {
          validateIdentifier(identifiers.get(index), PATH_DATA + "/" + index, context);
        }
      }
    }
  }

  private void validateResource(ResourceObject resource, String path, ValidationContext context) {
    validateResourceIdentity(resource, path, context);
    if (resource.attributes() != null) {
      validateAdditionalMembers(
          resource.attributes().additionalMembers(), path + "/attributes", context);
    }
    if (resource.relationships() != null) {
      validateResourceRelationships(resource, path, context);
    }
    if (resource.links() != null) {
      validateLinks(
          resource.links(),
          path + PATH_LINKS,
          context.withLinksContext(LinksContext.RESOURCE),
          null);
    }
    if (resource.meta() != null) {
      validateMeta(resource.meta(), path + PATH_META, context);
    }
    validateAdditionalMembers(resource.additionalMembers(), path, context);
  }

  private void validateResourceRelationships(
      ResourceObject resource, String path, ValidationContext context) {
    validateAdditionalMembers(
        resource.relationships().additionalMembers(), path + "/relationships", context);
    for (Map.Entry<String, Relationship> entry :
        resource.relationships().relationships().entrySet()) {
      validateRelationship(
          entry.getValue(), path + "/relationships/" + entry.getKey(), context, entry.getKey());
    }
  }

  private void validateRelationship(
      Relationship relationship, String path, ValidationContext context, String relationshipName) {
    if (relationship.data() != null) {
      validateRelationshipData(relationship.data(), path + PATH_DATA, context);
    }
    if (relationship.links() != null) {
      validateLinks(
          relationship.links(),
          path + PATH_LINKS,
          context.withLinksContext(LinksContext.RELATIONSHIP),
          relationshipName);
    }
    if (relationship.meta() != null) {
      validateMeta(relationship.meta(), path + PATH_META, context);
    }
    validateAdditionalMembers(relationship.additionalMembers(), path, context);
  }

  private void validateRelationshipData(
      RelationshipData data, String path, ValidationContext context) {
    switch (data) {
      case RelationshipData.NullLinkage ignored -> {
        // Explicit null to-one linkage has no identifiers to validate.
      }
      case RelationshipData.SingleLinkage(ResourceIdentifier identifier) ->
          validateIdentifier(identifier, path, context);
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        for (int index = 0; index < identifiers.size(); index++) {
          validateIdentifier(identifiers.get(index), path + "/" + index, context);
        }
      }
    }
  }

  private void validateIdentifier(
      ResourceIdentifier identifier, String path, ValidationContext context) {
    if (identifier.meta() != null) {
      validateMeta(identifier.meta(), path + PATH_META, context);
    }
    validateAdditionalMembers(identifier.additionalMembers(), path, context);
  }

  private void validateResourceIdentity(
      ResourceObject resource, String path, ValidationContext context) {
    if (context.documentUsage() == DocumentUsage.CREATE_REQUEST) {
      return;
    }
    if (!resource.hasId()) {
      throw new JsonApiValidationException(
          ValidationRuleCode.RESOURCE_ID_REQUIRED,
          path + "/id",
          "Resource requires id outside create-request context");
    }
  }

  private void validateError(ErrorObject error, String path, ValidationContext context) {
    if (error.links() != null) {
      validateLinks(
          error.links(), path + PATH_LINKS, context.withLinksContext(LinksContext.ERROR), null);
    }
    if (error.meta() != null) {
      validateMeta(error.meta(), path + PATH_META, context);
    }
    validateAdditionalMembers(error.additionalMembers(), path, context);
  }

  private void validateMeta(Meta meta, String path, ValidationContext context) {
    for (String name : meta.members().keySet()) {
      if (!MemberNames.isExtensionMember(name)) {
        continue;
      }
      String namespace = name.substring(0, name.indexOf(':'));
      if (!context.allowedExtensionNamespaces().contains(namespace)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER,
            path + "/" + name,
            "Extension namespace not allowed: " + namespace);
      }
    }
  }

  private void validateJsonApiObject(
      JsonApiObject jsonapi, String path, ValidationContext context) {
    if (jsonapi.profile() != null) {
      validateProfileUris(jsonapi.profile(), path, context);
    }
    if (jsonapi.meta() != null) {
      validateMeta(jsonapi.meta(), path + PATH_META, context);
    }
    validateAdditionalMembers(jsonapi.additionalMembers(), path, context);
  }

  private void validateProfileUris(List<String> profile, String path, ValidationContext context) {
    if (context.allowedProfileUris().isEmpty()) {
      return;
    }
    for (int i = 0; i < profile.size(); i++) {
      String uri = profile.get(i);
      if (!context.allowedProfileUris().contains(uri)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER,
            path + "/profile/" + i,
            "Profile URI not allowed: " + uri);
      }
    }
  }

  private void validateIncluded(
      List<ResourceObject> included, DocumentData primaryData, ValidationContext context) {
    Map<String, ResourceObject> byIdentity = indexIncludedResources(included, context);
    if (context.sparseFieldsetException()) {
      return;
    }
    Set<String> linked = collectLinkedIdentities(primaryData, byIdentity);
    for (String identity : byIdentity.keySet()) {
      if (!linked.contains(identity)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.FULL_LINKAGE_VIOLATION,
            "/included",
            "Included resource lacks full linkage: " + identity);
      }
    }
  }

  private Map<String, ResourceObject> indexIncludedResources(
      List<ResourceObject> included, ValidationContext context) {
    Set<String> seen = new HashSet<>();
    Map<String, ResourceObject> byIdentity = new HashMap<>();
    Map<String, ResourceObject> byLid = new HashMap<>();
    for (int index = 0; index < included.size(); index++) {
      ResourceObject resource = included.get(index);
      String path = "/included/" + index;
      validateResource(resource, path, context);
      indexResourceIdentity(resource, path, seen, byIdentity, byLid);
    }
    return byIdentity;
  }

  private void indexResourceIdentity(
      ResourceObject resource,
      String path,
      Set<String> seen,
      Map<String, ResourceObject> byIdentity,
      Map<String, ResourceObject> byLid) {
    String identityKey = resource.identityKey();
    if (identityKey != null) {
      ensureUniqueIdentity(identityKey, resource, path, seen, byIdentity);
    }
    if (resource.hasLid()) {
      String lidKey = resource.type() + ":lid:" + resource.lid();
      ResourceObject previous = byLid.put(lidKey, resource);
      if (previous != null && !previous.equals(resource)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.INCONSISTENT_LOCAL_IDENTIFIER,
            path,
            "Inconsistent local identifier: " + lidKey);
      }
    }
  }

  private void ensureUniqueIdentity(
      String identityKey,
      ResourceObject resource,
      String path,
      Set<String> seen,
      Map<String, ResourceObject> byIdentity) {
    if (!seen.contains(identityKey)) {
      seen.add(identityKey);
      byIdentity.put(identityKey, resource);
      return;
    }
    ResourceObject previous = byIdentity.get(identityKey);
    if (previous != null && !previous.equals(resource)) {
      throw new JsonApiValidationException(
          ValidationRuleCode.INCONSISTENT_LOCAL_IDENTIFIER,
          path,
          "Inconsistent local identifier: " + identityKey);
    }
    throw new JsonApiValidationException(
        ValidationRuleCode.DUPLICATE_INCLUDED_RESOURCE,
        path,
        "Duplicate included resource: " + identityKey);
  }

  private Set<String> collectLinkedIdentities(
      DocumentData primaryData, Map<String, ResourceObject> includedByIdentity) {
    Set<String> linked = new HashSet<>();
    Queue<String> queue = new ArrayDeque<>();
    collectFromPrimaryData(primaryData, linked, queue);
    while (!queue.isEmpty()) {
      expandIncludedRelationships(queue.poll(), includedByIdentity, linked, queue);
    }
    return linked;
  }

  private void expandIncludedRelationships(
      String identity,
      Map<String, ResourceObject> includedByIdentity,
      Set<String> linked,
      Queue<String> queue) {
    ResourceObject included = includedByIdentity.get(identity);
    if (included == null || included.relationships() == null) {
      return;
    }
    for (Relationship relationship : included.relationships().relationships().values()) {
      if (relationship.data() != null) {
        collectFromRelationshipData(relationship.data(), linked, queue);
      }
    }
  }

  private void collectFromPrimaryData(DocumentData data, Set<String> linked, Queue<String> queue) {
    switch (data) {
      case DocumentData.NullData ignored -> {
        // No linkage from explicit null primary data.
      }
      case DocumentData.SingleResource(ResourceObject resource) ->
          collectFromResource(resource, linked, queue);
      case DocumentData.ResourceCollection(List<ResourceObject> resources) -> {
        for (ResourceObject resource : resources) {
          collectFromResource(resource, linked, queue);
        }
      }
      case DocumentData.SingleIdentifier(ResourceIdentifier identifier) ->
          addLinked(identifier.identityKey(), linked, queue);
      case DocumentData.IdentifierCollection(List<ResourceIdentifier> identifiers) -> {
        for (ResourceIdentifier identifier : identifiers) {
          addLinked(identifier.identityKey(), linked, queue);
        }
      }
    }
  }

  private void collectFromResource(
      ResourceObject resource, Set<String> linked, Queue<String> queue) {
    addLinked(resource.identityKey(), linked, queue);
    if (resource.relationships() == null) {
      return;
    }
    for (Relationship relationship : resource.relationships().relationships().values()) {
      if (relationship.data() != null) {
        collectFromRelationshipData(relationship.data(), linked, queue);
      }
    }
  }

  private void collectFromRelationshipData(
      RelationshipData data, Set<String> linked, Queue<String> queue) {
    switch (data) {
      case RelationshipData.NullLinkage ignored -> {
        // Explicit null linkage contributes no identifiers.
      }
      case RelationshipData.SingleLinkage(ResourceIdentifier identifier) ->
          addLinked(identifier.identityKey(), linked, queue);
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        for (ResourceIdentifier identifier : identifiers) {
          addLinked(identifier.identityKey(), linked, queue);
        }
      }
    }
  }

  private void addLinked(String identity, Set<String> linked, Queue<String> queue) {
    if (identity != null && linked.add(identity)) {
      queue.add(identity);
    }
  }

  private void validateLinks(
      Links links, String path, ValidationContext context, String relationshipName) {
    validateAdditionalMembers(links.additionalMembers(), path, context);
    for (Map.Entry<String, Link> entry : links.links().entrySet()) {
      validateLinkEntry(entry.getKey(), path, context, relationshipName);
    }
  }

  private void validateLinkEntry(
      String name, String path, ValidationContext context, String relationshipName) {
    if (!isAllowedLinkName(name, context)) {
      throw new JsonApiValidationException(
          ValidationRuleCode.INVALID_LINKS_CONTEXT,
          path + "/" + name,
          "Non-standard link in context " + context.linksContext() + ": " + name);
    }
    if (requiresPaginationHint(name, context, relationshipName)) {
      throw new JsonApiValidationException(
          ValidationRuleCode.RELATIONSHIP_PAGINATION_REQUIRES_HINT,
          path + "/" + name,
          "Relationship pagination link requires cardinality hint: " + relationshipName);
    }
  }

  private boolean isAllowedLinkName(String name, ValidationContext context) {
    return Links.standardMembers(context.linksContext()).contains(name)
        || MemberNames.isExtensionMember(name)
        || MemberNames.isAtMember(name);
  }

  private boolean requiresPaginationHint(
      String name, ValidationContext context, String relationshipName) {
    return context.linksContext() == LinksContext.RELATIONSHIP
        && PAGINATION_LINKS.contains(name)
        && relationshipName != null
        && !context.relationshipPaginationHint(relationshipName);
  }

  private void validateAdditionalMembers(
      Map<String, Object> members, String basePath, ValidationContext context) {
    for (Map.Entry<String, Object> entry : members.entrySet()) {
      validateAdditionalMember(entry.getKey(), basePath, context);
    }
  }

  private void validateAdditionalMember(String name, String basePath, ValidationContext context) {
    if (MemberNames.isAtMember(name)) {
      return;
    }
    if (MemberNames.isExtensionMember(name)) {
      String namespace = name.substring(0, name.indexOf(':'));
      if (!context.allowedExtensionNamespaces().contains(namespace)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER,
            basePath + "/" + name,
            "Extension namespace not allowed: " + namespace);
      }
      return;
    }
    if (!context.allowedProfileMemberNames().contains(name)) {
      throw new JsonApiValidationException(
          ValidationRuleCode.UNKNOWN_ADDITIONAL_MEMBER,
          basePath + "/" + name,
          "Unknown unnamespaced member: " + name);
    }
  }
}
