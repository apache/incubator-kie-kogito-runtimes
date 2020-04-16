package org.kie.kogito.dmn;

import java.util.UUID;

import org.kie.kogito.ExecutionIdSupplier;

public class DmnExecutionIdSupplier implements ExecutionIdSupplier {

    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }

}
