package org.kie.kogito;

import java.util.function.Supplier;

/**
 * Wraps logic to generate unique identifiers per execution (e.g. evaluation of DMN model)
 *
 * Each call of the {@link #get()} method is considered a separate request of a new ID.
 */
public interface ExecutionIdSupplier extends Supplier<String> {
}
