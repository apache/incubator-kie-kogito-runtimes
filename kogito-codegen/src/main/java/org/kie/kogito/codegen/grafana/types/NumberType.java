package org.kie.kogito.codegen.grafana.types;

import java.math.BigDecimal;
import java.util.HashMap;

public class NumberType extends AbstractDmnType {

    public NumberType() {
        super(BigDecimal.class, "number");
        addFunctions(new HashMap<>());
    }
}
