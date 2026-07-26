package io.github.kazemek.jsonapi.core.validation;

import java.util.Map;
import java.util.Set;

/** Context for aggregate document validation. */
public record ValidationContext(
    DocumentUsage documentUsage,
    Set<String> allowedExtensionNamespaces,
    Set<String> allowedProfileUris,
    Set<String> allowedProfileMemberNames,
    boolean sparseFieldsetException,
    LinksContext linksContext,
    Map<String, Boolean> relationshipPaginationHints) {

  public static ValidationContext defaults() {
    return new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of());
  }

  public ValidationContext withDocumentUsage(DocumentUsage usage) {
    return new ValidationContext(
        usage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        sparseFieldsetException,
        linksContext,
        relationshipPaginationHints);
  }

  public ValidationContext withLinksContext(LinksContext context) {
    return new ValidationContext(
        documentUsage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        sparseFieldsetException,
        context,
        relationshipPaginationHints);
  }

  public ValidationContext withSparseFieldsetException(boolean enabled) {
    return new ValidationContext(
        documentUsage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        enabled,
        linksContext,
        relationshipPaginationHints);
  }

  public boolean relationshipPaginationHint(String relationshipName) {
    return relationshipPaginationHints.getOrDefault(relationshipName, false);
  }
}
