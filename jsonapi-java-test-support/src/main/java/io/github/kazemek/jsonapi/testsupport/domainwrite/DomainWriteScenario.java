package io.github.kazemek.jsonapi.testsupport.domainwrite;

import io.github.kazemek.jsonapi.jackson.DocumentEnvelope;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import org.jspecify.annotations.Nullable;

/**
 * One immutable flat write-mapping scenario: a stable id, exactly one {@link DomainWriteOperation},
 * a typed {@link DomainWriteInput}, an optional {@link DocumentEnvelope}, one discriminated {@link
 * DomainWriteOutcome}, and a non-null {@link DomainWriteComparisonPolicy}.
 */
public record DomainWriteScenario(
    String id,
    DomainWriteOperation operation,
    DomainWriteInput input,
    @Nullable DocumentEnvelope envelope,
    DomainWriteOutcome outcome,
    DomainWriteComparisonPolicy comparisonPolicy)
    implements Scenario {}
