package io.github.kazemek.jsonapi.testfixtures.enveloperead;

/**
 * Named version-neutral documents under the test-support corpus {@code envelope-binding/}
 * directory. Ids and relative paths are stable across Jackson majors.
 */
public enum EnvelopeBindingDocument {
  SINGLE_RESOURCE("single-resource"),
  HETEROGENEOUS_COLLECTION("heterogeneous-collection"),
  AT_MEMBER_DOCUMENT("at-member-document"),
  UNREGISTERED_PRIMARY_SINGLE("unregistered-primary-single"),
  UNREGISTERED_PRIMARY_COLLECTION("unregistered-primary-collection"),
  BINDER_FAILURE_COLLECTION("binder-failure-collection"),
  BINDER_FAILURE_SINGLE("binder-failure-single"),
  BINDER_FAILURE_INCLUDED("binder-failure-included"),
  ROOT_LEVEL_FAILURE("root-level-failure"),
  CYCLIC_LINKAGE("cyclic-linkage"),
  SHARED_IDENTITY_ID_AND_LID("shared-identity-id-and-lid"),
  DUPLICATE_INCLUDED_IDENTITIES("duplicate-included-identities"),
  INDEPENDENT_ENVELOPES_MATCHING("independent-envelopes-matching"),
  INDEPENDENT_ENVELOPES_UNRELATED("independent-envelopes-unrelated");

  private final String fileStem;

  EnvelopeBindingDocument(String fileStem) {
    this.fileStem = fileStem;
  }

  /** Path relative to the shared JSON:API 1.1 corpus root. */
  public String relativePath() {
    return "envelope-binding/" + fileStem + ".json";
  }

  /**
   * {@code true} only for the duplicate-identity wire form, which cannot pass the validated public
   * read path and is not part of the Phase 2.12 negative corpus.
   */
  public boolean validationInvalid() {
    return this == DUPLICATE_INCLUDED_IDENTITIES;
  }
}
