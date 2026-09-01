package io.github.kazemek.jsonapi.testsupport.domainwrite;

import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * One shared write-diagnostics case: a supplier building the mis-declared entity, the expected
 * major-neutral {@link MappingDiagnostic} category, and either the expected resource-relative JSON
 * Pointer location or null when the failure has no document member coordinate.
 *
 * <p>Adapter suites map the supplied entity through their own resource writer and assert the
 * semantic category and location; Jackson-specific cause chains and introspection details stay in
 * adapter-local tests. Ids are stable and looked up via {@code byId(String)}.
 */
public record WriteDiagnosticScenario(
    String id,
    String notes,
    Supplier<Object> entity,
    MappingDiagnostic diagnostic,
    @Nullable String propertyPath)
    implements Scenario {

  public WriteDiagnosticScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(notes, "notes");
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(diagnostic, "diagnostic");
  }

  @Override
  public String toString() {
    return id;
  }
}
