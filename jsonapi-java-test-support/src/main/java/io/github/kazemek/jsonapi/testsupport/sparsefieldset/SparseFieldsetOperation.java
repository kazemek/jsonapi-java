package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

/**
 * The sparse-fieldset entry points a scenario exercises on an adapter's resource mapper.
 *
 * <p>The two {@code FIELDSETS_REQUIRE_MAPPED_DOCUMENT} rejections are distinguishable from each
 * other only through this discriminator; success versus rejection is carried by the discriminated
 * outcome.
 */
public enum SparseFieldsetOperation {
  TO_DOCUMENT,
  TO_RESOURCE_COLLECTION,
  TO_MAPPED_DOCUMENT,
  TO_MAPPED_RESOURCE_COLLECTION
}
