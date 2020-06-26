package org.kie.kogito.grafana.dmn;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.kie.kogito.grafana.model.panel.common.YAxis;

public class DurationType extends AbstractDmnType {

    public DurationType() {
        super(Duration.class, "days and time duration");
        addFunctions(new TreeMap<>());
        List<YAxis> yaxes = new ArrayList<>();
        yaxes.add(new YAxis("dtdurationms", true));
        yaxes.add(new YAxis("ms", false));
        setYAxes(yaxes);
    }
}
