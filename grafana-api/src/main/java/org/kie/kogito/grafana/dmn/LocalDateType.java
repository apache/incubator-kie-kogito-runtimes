package org.kie.kogito.grafana.dmn;

import java.time.LocalDate;
import java.util.TreeMap;

public class LocalDateType extends AbstractDmnType {

    public LocalDateType() {
        super(LocalDate.class, "date");
        addFunctions(new TreeMap<>());
    }
}