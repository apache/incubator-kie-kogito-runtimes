package org.kie.kogito.grafana.dmn;

import java.time.LocalTime;
import java.util.TreeMap;

public class LocalTimeType extends AbstractDmnType {

    public LocalTimeType() {
        super(LocalTime.class, "time");
        addFunctions(new TreeMap<>());
    }
}