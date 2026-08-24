package io.github.kazemek.jsonapi.testsupport.domainwrite;

/** The flat write-mapping entry points a scenario exercises on an adapter's resource mapper. */
public enum DomainWriteOperation {
  TO_RESOURCE,
  TO_DOCUMENT,
  TO_DOCUMENT_WITH_ENVELOPE,
  TO_RESOURCE_COLLECTION
}
