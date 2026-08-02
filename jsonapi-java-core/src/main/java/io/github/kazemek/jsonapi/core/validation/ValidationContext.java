package io.github.kazemek.jsonapi.core.validation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Context for aggregate document validation.
 *
 * <p>Carries document usage (for example create, update, or response), allowed extension namespaces
 * and profile URIs/member names, the sparse-fieldset full-linkage exception, the current links
 * context, occurrence-keyed relationship pagination hints for link-only relationships, and an
 * optional expected endpoint identity compared against {@link DocumentUsage#UPDATE_REQUEST}
 * documents.
 *
 * <p>{@link #defaults()} uses {@link DocumentUsage#RESPONSE_OR_OTHER}, empty policy sets, no sparse
 * fieldset exception, {@link LinksContext#TOP_LEVEL}, no pagination hints, and no expected endpoint
 * identity—suitable for base-spec response documents without extensions or profiles.
 */
public record ValidationContext(
    DocumentUsage documentUsage,
    Set<String> allowedExtensionNamespaces,
    Set<String> allowedProfileUris,
    Set<String> allowedProfileMemberNames,
    boolean sparseFieldsetException,
    LinksContext linksContext,
    Map<RelationshipPaginationKey, RelationshipCardinality> relationshipPaginationHints,
    @Nullable EndpointIdentity expectedEndpointIdentity) {

  private static final String PATH_RELATIONSHIP_PAGINATION_HINTS = "/relationshipPaginationHints";

  public ValidationContext {
    documentUsage =
        LocalValidation.requireNonNull(
            documentUsage, "/documentUsage", "documentUsage must not be null");
    linksContext =
        LocalValidation.requireNonNull(
            linksContext, "/linksContext", "linksContext must not be null");
    allowedExtensionNamespaces =
        copyRequiredStringSet(
            allowedExtensionNamespaces,
            "/allowedExtensionNamespaces",
            "allowedExtensionNamespaces");
    allowedProfileUris =
        copyRequiredStringSet(allowedProfileUris, "/allowedProfileUris", "allowedProfileUris");
    allowedProfileMemberNames =
        copyRequiredStringSet(
            allowedProfileMemberNames, "/allowedProfileMemberNames", "allowedProfileMemberNames");
    relationshipPaginationHints =
        copyRequiredHints(
            LocalValidation.requireNonNull(
                relationshipPaginationHints,
                PATH_RELATIONSHIP_PAGINATION_HINTS,
                "relationshipPaginationHints must not be null"));
  }

  public static ValidationContext defaults() {
    return new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of(),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of(),
        null);
  }

  public ValidationContext withDocumentUsage(DocumentUsage usage) {
    return new ValidationContext(
        usage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        sparseFieldsetException,
        linksContext,
        relationshipPaginationHints,
        expectedEndpointIdentity);
  }

  public ValidationContext withLinksContext(LinksContext context) {
    return new ValidationContext(
        documentUsage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        sparseFieldsetException,
        context,
        relationshipPaginationHints,
        expectedEndpointIdentity);
  }

  public ValidationContext withSparseFieldsetException(boolean enabled) {
    return new ValidationContext(
        documentUsage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        enabled,
        linksContext,
        relationshipPaginationHints,
        expectedEndpointIdentity);
  }

  /** Returns a context whose expected endpoint identity is compared for update documents. */
  public ValidationContext withExpectedEndpointIdentity(@Nullable EndpointIdentity identity) {
    return new ValidationContext(
        documentUsage,
        allowedExtensionNamespaces,
        allowedProfileUris,
        allowedProfileMemberNames,
        sparseFieldsetException,
        linksContext,
        relationshipPaginationHints,
        identity);
  }

  /** Returns the explicit cardinality hint for a relationship occurrence, if present. */
  public Optional<RelationshipCardinality> relationshipPaginationHint(
      String resourceType, String relationshipName) {
    return Optional.ofNullable(
        relationshipPaginationHints.get(
            RelationshipPaginationKey.of(resourceType, relationshipName)));
  }

  private static Set<String> copyRequiredStringSet(Set<String> source, String path, String label) {
    LocalValidation.requireNonNull(source, path, label + " must not be null");
    Set<String> copy = new LinkedHashSet<>();
    int index = 0;
    for (String element : source) {
      copy.add(
          LocalValidation.requireNonNull(
              element, path + "/" + index, label + " element must not be null"));
      index++;
    }
    return Set.copyOf(copy);
  }

  private static Map<RelationshipPaginationKey, RelationshipCardinality> copyRequiredHints(
      Map<RelationshipPaginationKey, RelationshipCardinality> source) {
    Map<RelationshipPaginationKey, RelationshipCardinality> hintCopy = new LinkedHashMap<>();
    for (Map.Entry<RelationshipPaginationKey, RelationshipCardinality> entry : source.entrySet()) {
      hintCopy.put(
          LocalValidation.requireNonNull(
              entry.getKey(),
              PATH_RELATIONSHIP_PAGINATION_HINTS,
              "Pagination hint key must not be null"),
          LocalValidation.requireNonNull(
              entry.getValue(),
              PATH_RELATIONSHIP_PAGINATION_HINTS,
              "Pagination hint value must not be null"));
    }
    return Collections.unmodifiableMap(hintCopy);
  }
}
